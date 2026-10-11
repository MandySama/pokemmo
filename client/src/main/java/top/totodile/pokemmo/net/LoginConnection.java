package top.totodile.pokemmo.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/** 参考 ProtocolSession 的连接和初始握手；当前阶段仅接入服务端不可用的失败链路。 */
public final class LoginConnection implements AutoCloseable {
    public enum Failure {
        // 参考 ChatHistoryViewportComponent.Y20：网络组件已建立时，连接失败使用 2009。
        COMMUNICATION("与验证服务器通信失败. 请稍后再试."),
        SYSTEM("系统错误. 请稍候再试.");

        private final String message;

        Failure(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private Socket socket;
    private Thread worker;
    private boolean closed;

    public synchronized CompletableFuture<Failure> connect() {
        if (closed) throw new IllegalStateException("Login connection is closed");
        String host = System.getProperty("pokemmo.login.host", "127.0.0.1");
        int port = Integer.getInteger("pokemmo.login.port", 2106);
        var result = new CompletableFuture<Failure>();
        worker = Thread.ofPlatform().daemon().name("login-connection").start(() -> {
            try {
                Failure failure = request(host, port);
                // 参考 PacketFramingParser.lPT2 的 100ms 轮询周期，避免立即失败在同帧恢复表单。
                Thread.sleep(100);
                result.complete(failure);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                result.complete(Failure.COMMUNICATION);
            } catch (RuntimeException exception) {
                result.complete(Failure.SYSTEM);
            }
        });
        return result;
    }

    private Failure request(String host, int port) {
        // 参考单地址 ServerEndpointAddressResolver：每轮两次连接，超时由 10 秒切到 30 秒。
        for (int timeout : new int[]{10_000, 10_000, 30_000, 30_000}) {
            try (Socket connection = new Socket()) {
                synchronized (this) {
                    if (closed) return Failure.COMMUNICATION;
                    socket = connection;
                }
                connection.connect(new InetSocketAddress(host, port), timeout);
                connection.setSoTimeout(10_000);
                // 参考 HC / ClientKeepAlivePacket：大端包长、opcode 0、随机数与时间戳。
                long seed = ThreadLocalRandom.current().nextLong();
                byte[] handshake = ByteBuffer.allocate(19).putShort((short) 19).put((byte) 0)
                        .putLong(seed ^ 0x2C9CA13689DB65C8L)
                        .putLong(System.currentTimeMillis() ^ seed ^ 0xC5828CD837901279L).array();
                connection.getOutputStream().write(handshake);
                connection.getOutputStream().flush();
                if (connection.getInputStream().read() == -1) continue;
                // 收到响应后应进入 RSA/AES 握手和鉴权，后续与服务端一起接入。
                // 不把任意响应伪装成密码错误或登录成功。
                return Failure.SYSTEM;
            } catch (IOException exception) {
                // 连接拒绝、握手超时或断开均按参考流程尝试下一个连接。
            } finally {
                synchronized (this) {
                    socket = null;
                }
            }
        }
        return Failure.COMMUNICATION;
    }

    @Override
    public synchronized void close() {
        closed = true;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
        if (worker != null) worker.interrupt();
    }
}
