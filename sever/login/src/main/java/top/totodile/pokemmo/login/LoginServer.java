package top.totodile.pokemmo.login;

import top.totodile.pokemmo.protocol.LoginRequest;
import top.totodile.pokemmo.protocol.LoginStatus;
import top.totodile.pokemmo.protocol.SecureChannel;
import top.totodile.pokemmo.protocol.GameEndpoint;
import top.totodile.pokemmo.protocol.GameTicket;
import top.totodile.pokemmo.protocol.GameServerInfo;
import top.totodile.pokemmo.protocol.PacketData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/** 认证后保留连接，提供服务器列表及签名入服凭证；断开后清理登录状态。 */
public final class LoginServer implements AutoCloseable {
    private static final System.Logger LOGGER = System.getLogger(LoginServer.class.getName());
    private final ServerSocket listener = new ServerSocket();
    private final ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();
    private final Set<Socket> connections = ConcurrentHashMap.newKeySet();
    private final Semaphore capacity = new Semaphore(64);
    private final LoginService service;
    private final PrivateKey rootKey;
    private final GameDirectory directory;
    private final ConcurrentHashMap<String, Integer> accountIds = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicInteger nextAccountId = new java.util.concurrent.atomic.AtomicInteger();

    public LoginServer(InetSocketAddress address, AccountRepository accounts, PrivateKey rootKey) throws IOException {
        this(address, accounts, rootKey, GameDirectory.configured());
    }

    public LoginServer(InetSocketAddress address, AccountRepository accounts, PrivateKey rootKey,
                       GameDirectory directory) throws IOException {
        service = new LoginService(accounts, rootKey);
        this.rootKey = rootKey;
        this.directory = directory;
        listener.bind(address);
    }

    public int port() {
        return listener.getLocalPort();
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
            if (!capacity.tryAcquire()) {
                socket.close();
                continue;
            }
            connections.add(socket);
            try {
                workers.submit(() -> handle(socket));
            } catch (RuntimeException exception) {
                connections.remove(socket);
                capacity.release();
                socket.close();
                if (!listener.isClosed()) throw exception;
            }
        }
    }

    private void handle(Socket socket) {
        try (SecureChannel channel = new SecureChannel(socket)) {
            socket.setSoTimeout(10_000);
            socket.setTcpNoDelay(true);
            channel.serverHandshake(rootKey);
            byte[] packet = channel.receive();
            LoginStatus result;
            LoginRequest request = null;
            try {
                request = LoginRequest.decode(packet);
                result = service.authenticate(request);
            } catch (IOException | RuntimeException exception) {
                LOGGER.log(System.Logger.Level.WARNING, "Login request failed: {0}", exception.getClass().getSimpleName());
                result = LoginStatus.SYSTEM_ERROR;
            }
            channel.send(result.encode());
            if (result != LoginStatus.AUTHENTICATED) return;
            String credential = service.issueCredential(request);
            if (!credential.isEmpty()) {
                channel.send(new PacketData(1024).putByte(7).putString(request.username()).putString(credential).written());
            }
            int accountId = accountIds.computeIfAbsent(request.username(), name -> nextAccountId.incrementAndGet());
            String username = request.username();
            // 服务端选择阶段最多空闲两分钟，客户端收到断开时重新登录。
            socket.setSoTimeout(120_000);
            while (!socket.isClosed()) {
                PacketData command = new PacketData(channel.receive());
                int opcode = command.byteValue();
                if (opcode == 2) {
                    command.end();
                    channel.send(GameServerInfo.encodeList(directory.list()));
                } else if (opcode == 3) {
                    int nodeId = command.byteValue();
                    command.end();
                    GameDirectory.Node node = directory.node(nodeId);
                    GameServerInfo status = node == null ? null : directory.status(node);
                    if (status == null || !status.joinable()) {
                        channel.send(new byte[]{3, 8});
                    } else if (status.load() >= status.capacity()) {
                        channel.send(new byte[]{3, 6});
                    } else {
                        byte[] ticket = GameTicket.issue(accountId, username, nodeId, rootKey);
                        channel.send(new GameEndpoint(accountId, nodeId, node.host(), node.port(), ticket).encode());
                    }
                } else {
                    throw new IOException("Unexpected authenticated login packet");
                }
            }
        } catch (IOException | GeneralSecurityException exception) {
            // 握手失败和封包损坏时直接断开，不在未验证的通道上发送业务结果。
            LOGGER.log(System.Logger.Level.DEBUG, "Login connection closed: {0}", exception.getClass().getSimpleName());
        } finally {
            connections.remove(socket);
            capacity.release();
        }
    }

    @Override
    public void close() throws IOException {
        listener.close();
        for (Socket connection : connections) {
            try {
                connection.close();
            } catch (IOException exception) {
                LOGGER.log(System.Logger.Level.DEBUG, "Unable to close connection", exception);
            }
        }
        workers.shutdownNow();
    }
}
