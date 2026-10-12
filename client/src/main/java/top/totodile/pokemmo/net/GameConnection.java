package top.totodile.pokemmo.net;

import top.totodile.pokemmo.protocol.GameAdmission;
import top.totodile.pokemmo.protocol.GameEndpoint;
import top.totodile.pokemmo.protocol.HandshakeKeys;
import top.totodile.pokemmo.protocol.SecureChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.concurrent.CompletableFuture;

/** 独立游戏连接使用临时凭证入服；认证后定期保活，关闭界面时释放连接。 */
public final class GameConnection implements AutoCloseable {
    public enum Result { CONNECTED, REJECTED, COMMUNICATION }

    private final Socket socket = new Socket();
    private volatile boolean connected;
    private volatile boolean closed;
    private Thread worker;

    public synchronized CompletableFuture<Result> connect(GameEndpoint endpoint) {
        if (closed || worker != null) throw new IllegalStateException("Game connection already used");
        CompletableFuture<Result> result = new CompletableFuture<>();
        worker = Thread.ofPlatform().daemon().name("game-connection").start(() -> {
            try (SecureChannel channel = new SecureChannel(socket, true)) {
                socket.connect(new InetSocketAddress(endpoint.host(), endpoint.port()), 10_000);
                socket.setSoTimeout(10_000);
                socket.setTcpNoDelay(true);
                channel.clientHandshake(HandshakeKeys.rootPublicKey());
                channel.send(GameAdmission.request(endpoint));
                if (!GameAdmission.decodeResponse(channel.receive())) {
                    result.complete(Result.REJECTED);
                    return;
                }
                connected = true;
                result.complete(Result.CONNECTED);
                while (!closed) {
                    channel.send(GameAdmission.heartbeatRequest());
                    GameAdmission.verifyHeartbeat(channel.receive());
                    Thread.sleep(15_000);
                }
            } catch (IOException | GeneralSecurityException exception) {
                result.complete(Result.COMMUNICATION);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                result.complete(Result.COMMUNICATION);
            } finally {
                connected = false;
            }
        });
        return result;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public synchronized void close() {
        closed = true;
        connected = false;
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        if (worker != null) worker.interrupt();
    }
}
