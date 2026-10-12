package top.totodile.pokemmo.game;

import top.totodile.pokemmo.protocol.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/** 游戏服务入口连接：校验短期入服凭证，维护在线会话及加密保活。 */
public final class GameServer implements AutoCloseable {
    private static final System.Logger LOGGER = System.getLogger(GameServer.class.getName());
    private final ServerSocket listener = new ServerSocket();
    private final java.util.concurrent.ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();
    private final Set<Socket> connections = ConcurrentHashMap.newKeySet();
    private final Set<Integer> accounts = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Long> consumed = new ConcurrentHashMap<>();
    private final Semaphore connectionLimit = new Semaphore(128);
    private final int serverId;
    private final int capacity;
    private final PrivateKey handshakeKey;
    private final PublicKey ticketKey;

    public GameServer(InetSocketAddress address, int serverId, int capacity, PrivateKey handshakeKey,
                      PublicKey ticketKey) throws IOException {
        if (serverId < 1 || serverId > 127 || capacity < 1 || capacity > 100) {
            throw new IllegalArgumentException("Invalid game server configuration");
        }
        this.serverId = serverId;
        this.capacity = capacity;
        this.handshakeKey = handshakeKey;
        this.ticketKey = ticketKey;
        listener.bind(address);
    }

    public int port() {
        return listener.getLocalPort();
    }

    public int onlineCount() {
        return accounts.size();
    }

    public void serve() throws IOException {
        while (!listener.isClosed()) {
            Socket socket;
            try {
                socket = listener.accept();
            } catch (SocketException exception) {
                if (listener.isClosed()) return;
                throw exception;
            }
            if (!connectionLimit.tryAcquire()) {
                socket.close();
                continue;
            }
            connections.add(socket);
            try {
                workers.submit(() -> handle(socket));
            } catch (RuntimeException exception) {
                connections.remove(socket);
                connectionLimit.release();
                socket.close();
                if (!listener.isClosed()) throw exception;
            }
        }
    }

    private synchronized boolean admit(GameTicket.Claims claims) {
        long now = System.currentTimeMillis();
        consumed.entrySet().removeIf(entry -> entry.getValue() <= now);
        // 一分钟内最多保留 4096 次消费记录；容量不足时拒绝而不丢弃防重放记录。
        if (consumed.size() >= 4096 || accounts.size() >= capacity || accounts.contains(claims.accountId())
                || consumed.putIfAbsent(claims.nonce(), claims.expiresAt()) != null) return false;
        accounts.add(claims.accountId());
        return true;
    }

    private void handle(Socket socket) {
        Integer accountId = null;
        try (SecureChannel channel = new SecureChannel(socket, true)) {
            socket.setSoTimeout(10_000);
            socket.setTcpNoDelay(true);
            channel.serverHandshake(handshakeKey);
            byte[] packet = channel.receive();
            if (packet.length == 1 && packet[0] == 0x7E) {
                channel.send(new PacketData(6).putByte(0x7E).putByte(serverId).putShort(accounts.size())
                        .putShort(capacity).written());
                return;
            }
            boolean accepted = false;
            try {
                GameAdmission.Request request = GameAdmission.decodeRequest(packet);
                GameTicket.Claims claims = GameTicket.verify(request.ticket(), ticketKey, serverId,
                        System.currentTimeMillis());
                if (request.accountId() == claims.accountId() && admit(claims)) {
                    accountId = claims.accountId();
                    accepted = true;
                }
            } catch (IOException | GeneralSecurityException exception) {
                LOGGER.log(System.Logger.Level.DEBUG, "Game admission rejected: {0}", exception.getClass().getSimpleName());
            }
            channel.send(GameAdmission.response(accepted));
            if (!accepted) return;
            socket.setSoTimeout(45_000);
            while (!socket.isClosed()) {
                channel.send(GameAdmission.heartbeatResponse(channel.receive()));
            }
        } catch (IOException | GeneralSecurityException exception) {
            LOGGER.log(System.Logger.Level.DEBUG, "Game connection closed: {0}", exception.getClass().getSimpleName());
        } finally {
            if (accountId != null) accounts.remove(accountId);
            connections.remove(socket);
            connectionLimit.release();
        }
    }

    @Override
    public void close() throws IOException {
        listener.close();
        for (Socket socket : connections) {
            try {
                socket.close();
            } catch (IOException exception) {
                LOGGER.log(System.Logger.Level.DEBUG, "Unable to close game connection", exception);
            }
        }
        workers.shutdownNow();
    }
}
