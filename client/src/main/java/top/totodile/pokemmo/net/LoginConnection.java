package top.totodile.pokemmo.net;

import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.protocol.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** 在后台完成认证和服务器选择，同一登录通道按顺序收发封包。 */
public final class LoginConnection implements AutoCloseable {
    public enum Result {
        AUTHENTICATED(50000), INVALID_PASSWORD(2001), COMMUNICATION(2009), SYSTEM(2000);

        private final int stringId;

        Result(int stringId) {
            this.stringId = stringId;
        }

        public String getMessage() {
            return ClientStrings.get(stringId) + (this == INVALID_PASSWORD ? " (2)" : "");
        }
    }

    private volatile Socket socket;
    private volatile SecureChannel channel;
    private volatile List<GameServerInfo> servers = List.of();
    private Thread worker;
    private CompletableFuture<?> pending;
    private volatile long generation;
    private boolean closed;
    private volatile String issuedCredential = "";

    public String issuedCredential() {
        return issuedCredential;
    }

    public List<GameServerInfo> servers() {
        return servers;
    }

    public synchronized CompletableFuture<Result> connect(String username, String password, boolean remember,
                                                          String language) {
        return connect(new LoginRequest(username, LoginRequest.digest(password), remember, language));
    }

    public synchronized CompletableFuture<Result> connectSaved(String username, byte[] credential, String language) {
        return connect(new LoginRequest(username, "", false, language, credential.clone()));
    }

    private CompletableFuture<Result> connect(LoginRequest request) {
        if (closed) throw new IllegalStateException("Login connection is closed");
        disconnect();
        issuedCredential = "";
        String host = System.getProperty("pokemmo.login.host", "127.0.0.1");
        int port = Integer.getInteger("pokemmo.login.port", 2106);
        long expected = generation;
        return start(() -> {
            Result response = authenticate(host, port, request, expected);
            Thread.sleep(100);
            return response;
        });
    }

    public synchronized CompletableFuture<GameEndpoint> select(int serverId) {
        SecureChannel active = channel;
        if (active == null) return CompletableFuture.failedFuture(new IOException("Login session disconnected"));
        return start(() -> {
            active.send(new byte[]{3, (byte) serverId});
            return GameEndpoint.decode(active.receive());
        });
    }

    public synchronized CompletableFuture<List<GameServerInfo>> refresh() {
        SecureChannel active = channel;
        if (active == null) return CompletableFuture.failedFuture(new IOException("Login session disconnected"));
        return start(() -> {
            active.send(new byte[]{2});
            List<GameServerInfo> updated = GameServerInfo.decodeList(active.receive());
            servers = updated;
            return updated;
        });
    }

    private synchronized <T> CompletableFuture<T> start(Operation<T> operation) {
        if (closed) throw new IllegalStateException("Login connection is closed");
        if (pending != null && !pending.isDone()) throw new IllegalStateException("Login operation in progress");
        long expected = generation;
        CompletableFuture<T> result = new CompletableFuture<>();
        pending = result;
        worker = Thread.ofPlatform().daemon().name("login-connection").start(() -> {
            try {
                T value = operation.run();
                if (generation == expected) result.complete(value);
                else result.cancel(false);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                result.cancel(false);
            } catch (Exception exception) {
                result.completeExceptionally(exception);
            }
        });
        return result;
    }

    private Result authenticate(String host, int port, LoginRequest request, long expected) {
        for (int timeout : new int[]{10_000, 10_000, 30_000, 30_000}) {
            if (generation != expected || Thread.currentThread().isInterrupted()) return Result.COMMUNICATION;
            Socket connection = new Socket();
            SecureChannel secure = new SecureChannel(connection);
            boolean retained = false;
            try {
                synchronized (this) {
                    if (generation != expected) return Result.COMMUNICATION;
                    socket = connection;
                }
                connection.connect(new InetSocketAddress(host, port), timeout);
                connection.setSoTimeout(10_000);
                connection.setTcpNoDelay(true);
                secure.clientHandshake(HandshakeKeys.rootPublicKey());
                secure.send(request.encode());
                byte[] response = secure.receive();
                LoginStatus status = LoginStatus.decode(response);
                if (status == LoginStatus.INVALID_PASSWORD) return Result.INVALID_PASSWORD;
                if (status == LoginStatus.SYSTEM_ERROR) return Result.SYSTEM;
                secure.send(new byte[]{2});
                byte[] list = secure.receive();
                String credential = "";
                if (list.length > 0 && list[0] == 7) {
                    PacketData fields = new PacketData(list);
                    fields.byteValue();
                    if (!fields.string(64).equals(request.username())) throw new IOException("Credential account mismatch");
                    credential = fields.string(512);
                    fields.end();
                    list = secure.receive();
                }
                List<GameServerInfo> available = GameServerInfo.decodeList(list);
                synchronized (this) {
                    if (generation != expected) return Result.COMMUNICATION;
                    servers = available;
                    issuedCredential = credential;
                    channel = secure;
                    retained = true;
                }
                return Result.AUTHENTICATED;
            } catch (GeneralSecurityException exception) {
                return Result.COMMUNICATION;
            } catch (IOException exception) {
                // 连接失败与账号错误分别处理，未验证响应不能进入服务器选择阶段。
            } finally {
                if (!retained) {
                    try {
                        secure.close();
                    } catch (IOException ignored) {
                    }
                    synchronized (this) {
                        if (socket == connection) socket = null;
                    }
                }
            }
        }
        return Result.COMMUNICATION;
    }

    public synchronized void disconnect() {
        generation++;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
        socket = null;
        channel = null;
        servers = List.of();
        if (worker != null) worker.interrupt();
        if (pending != null) pending.cancel(false);
    }

    @Override
    public synchronized void close() {
        closed = true;
        disconnect();
    }

    @FunctionalInterface
    private interface Operation<T> {
        T run() throws Exception;
    }
}
