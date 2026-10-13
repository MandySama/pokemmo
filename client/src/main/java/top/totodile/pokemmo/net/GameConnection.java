package top.totodile.pokemmo.net;

import top.totodile.pokemmo.protocol.GameAdmission;
import top.totodile.pokemmo.protocol.CharacterPackets;
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

    private static final class Pending {
        private final byte[] packet;
        private final CompletableFuture<byte[]> future;
        private Pending(byte[] packet, CompletableFuture<byte[]> future) {
            this.packet = packet;
            this.future = future;
        }

        private CompletableFuture<byte[]> future() { return future; }
    }

    private final java.util.concurrent.BlockingQueue<Pending> commands = new java.util.concurrent.ArrayBlockingQueue<>(16);
    private volatile java.util.List<CharacterPackets.CharacterInfo> characters = java.util.List.of();

    public synchronized CompletableFuture<Result> connect(GameEndpoint endpoint) {
        if (closed || worker != null) throw new IllegalStateException("Game connection already used");
        CompletableFuture<Result> result = new CompletableFuture<>();
        worker = Thread.ofPlatform().name("game-connection").start(() -> {
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
                channel.send(CharacterPackets.listRequest());
                characters = CharacterPackets.decodeList(channel.receive());
                connected = true;
                result.complete(Result.CONNECTED);
                while (!closed || !commands.isEmpty()) {
                    Pending pending = commands.poll(15, java.util.concurrent.TimeUnit.SECONDS);
                    if (pending == null) {
                        channel.send(GameAdmission.heartbeatRequest());
                        GameAdmission.verifyHeartbeat(channel.receive());
                    } else {
                        try {
                            channel.send(pending.packet);
                            pending.future().complete(channel.receive());
                        } catch (IOException exception) {
                            pending.future().completeExceptionally(exception);
                            throw exception;
                        }
                    }
                }
            } catch (IOException | GeneralSecurityException exception) {
                result.complete(Result.COMMUNICATION);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                result.complete(Result.COMMUNICATION);
            } finally {
                synchronized (GameConnection.this) {
                    connected = false;
                    Pending pending;
                    while ((pending = commands.poll()) != null) pending.future().completeExceptionally(new IOException("Game connection closed"));
                }
            }
        });
        return result;
    }

    public java.util.List<CharacterPackets.CharacterInfo> characters() { return characters; }

    public synchronized CompletableFuture<CharacterPackets.CreationResult> createCharacter(CharacterPackets.Appearance appearance) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(CharacterPackets.createRequest(appearance), response))) {
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        }
        return response.thenApply(packet -> {
            try {
                CharacterPackets.CreationResult result = CharacterPackets.decodeResult(packet);
                if (result.status() == CharacterPackets.Status.SUCCESS) characters = result.characters();
                return result;
            } catch (IOException exception) { throw new java.util.concurrent.CompletionException(exception); }
        });
    }

    public synchronized CompletableFuture<CharacterPackets.CreationResult> deleteCharacter(long id) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(CharacterPackets.deleteRequest(id), response))) {
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        }
        return response.thenApply(packet -> {
            try {
                var result = CharacterPackets.decodeDeleteResult(packet);
                if (result.status() == CharacterPackets.Status.SUCCESS) characters = result.characters();
                return result;
            } catch (IOException exception) { throw new java.util.concurrent.CompletionException(exception); }
        });
    }

    public synchronized CompletableFuture<CharacterPackets.CharacterInfo> enterCharacter(long id) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game is disconnected"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(CharacterPackets.enterRequest(id), response))) {
            return CompletableFuture.failedFuture(new IOException("Game command queue is full"));
        }
        return response.thenCompose(packet -> {
            if (packet.length != 2 || packet[0] != 4 || packet[1] != 1)
                return CompletableFuture.failedFuture(new IOException("Character selection rejected"));
            CompletableFuture<byte[]> loaded = new CompletableFuture<>();
            synchronized (GameConnection.this) {
                if (!offerCommand(new Pending(new byte[]{5}, loaded)))
                    return CompletableFuture.failedFuture(new IOException("Game command queue full"));
            }
            return loaded.thenApply(bytes -> {
                try { return CharacterPackets.decodeEntered(bytes); }
                catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
            });
        });
    }

    public synchronized CompletableFuture<top.totodile.pokemmo.protocol.WorldPackets.Result> worldCommand(byte[] packet) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(packet, response))) return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(bytes -> {
            try { return top.totodile.pokemmo.protocol.WorldPackets.decodeResult(bytes); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    /** 确认服务端当前状态已经落库，不接收客户端坐标。 */
    public synchronized CompletableFuture<Void> flushWorld() {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(new byte[]{top.totodile.pokemmo.protocol.WorldPackets.SAVE}, response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return positionAcknowledged(response);
    }

    private boolean offerCommand(Pending pending) {
        return commands.offer(pending);
    }

    private static CompletableFuture<Void> positionAcknowledged(CompletableFuture<byte[]> response) {
        // 确认表示这一批的最新快照已提交；网络或数据库失败仍向调用方报告。
        return response.thenAccept(packet -> {
            if (packet.length != 1 || packet[0] != top.totodile.pokemmo.protocol.WorldPackets.SAVE) throw new java.util.concurrent.CompletionException(new IOException("Position save failed"));
        });
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        connected = false;
        if (worker == null) {
            try { socket.close(); } catch (IOException ignored) { }
        } else {
            commands.offer(new Pending(GameAdmission.heartbeatRequest(), new CompletableFuture<>()));
        }
    }
}
