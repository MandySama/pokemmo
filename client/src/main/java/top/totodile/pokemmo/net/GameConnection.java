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

    public synchronized CompletableFuture<CharacterPackets.CharacterInfo> teleportRegion(int region, int group, int map, int x, int y) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game is disconnected"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        byte[] packet = new top.totodile.pokemmo.protocol.PacketData(8)
                .putByte(top.totodile.pokemmo.protocol.WorldPackets.REGION_TELEPORT).putByte(region)
                .putByte(group).putByte(map).putShort(x).putShort(y).written();
        if (!offerCommand(new Pending(packet, response))) return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(bytes -> {
            try {
                var info = CharacterPackets.decodeEntered(bytes);
                characters = characters.stream().map(current -> current.id() == info.id() ? info : current).toList();
                return info;
            } catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    public synchronized CompletableFuture<top.totodile.pokemmo.protocol.NpcPackets.Snapshot> npcCommand(byte[] packet) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(packet, response))) return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(bytes -> {
            try { return top.totodile.pokemmo.protocol.NpcPackets.decode(bytes); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
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

    public synchronized CompletableFuture<Boolean> setDexUnlocked(boolean unlocked) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(new byte[]{0x76, (byte) (unlocked ? 1 : 0)}, response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(packet -> {
            if (packet.length != 3 || packet[0] != 0x76 || packet[1] != 1 || packet[2] != (unlocked ? 1 : 0))
                throw new java.util.concurrent.CompletionException(new IOException("Pokedex update rejected"));
            return unlocked;
        });
    }

    public synchronized CompletableFuture<java.util.List<CharacterPackets.CharacterInfo>> leaveWorld() {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(new byte[]{0x78}, response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(packet -> {
            try { characters = CharacterPackets.decodeLeft(packet); return characters; }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    private record ClockSample(long seconds, long at) { }
    private volatile ClockSample worldClock;

    public synchronized CompletableFuture<top.totodile.pokemmo.protocol.SearchPackets.Result> searchCommand(int action, int id, int amount) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        var response = new CompletableFuture<byte[]>();
        if (!offerCommand(new Pending(top.totodile.pokemmo.protocol.SearchPackets.request(action, id, amount), response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(packet -> {
            try { return top.totodile.pokemmo.protocol.SearchPackets.decodeResponse(packet); }
            catch (IOException exception) { throw new java.util.concurrent.CompletionException(exception); }
        });
    }

    public long worldSeconds() {
        ClockSample sample = worldClock;
        return sample == null ? -1 : sample.seconds() + Math.max(0, System.nanoTime() - sample.at()) / 250_000_000L;
    }

    public synchronized CompletableFuture<top.totodile.pokemmo.protocol.TrainerStats> trainerStats() {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(new byte[]{top.totodile.pokemmo.protocol.TrainerStats.OPCODE}, response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(packet -> {
            try {
                var snapshot = top.totodile.pokemmo.protocol.TrainerStats.decodeSnapshot(packet);
                worldClock = new ClockSample(snapshot.worldSeconds(), System.nanoTime());
                return snapshot.stats();
            }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    public synchronized CompletableFuture<java.util.List<top.totodile.pokemmo.protocol.PartyMember>> moveParty(int from, int to) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(top.totodile.pokemmo.protocol.PartyPackets.move(from, to), response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(packet -> {
            try { return top.totodile.pokemmo.protocol.PartyPackets.decodeResponse(packet); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }
    public synchronized CompletableFuture<top.totodile.pokemmo.protocol.PartyMember> selectFollower(int index) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(top.totodile.pokemmo.protocol.FollowerPackets.request(index), response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(packet -> {
            try { return top.totodile.pokemmo.protocol.FollowerPackets.decodeResponse(packet); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    public synchronized CompletableFuture<top.totodile.pokemmo.protocol.PcPackets.PartyState> pcParty() {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(top.totodile.pokemmo.protocol.PcPackets.fetchParty(), response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(packet -> {
            try { return top.totodile.pokemmo.protocol.PcPackets.decodePartyResponse(packet); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    public synchronized CompletableFuture<top.totodile.pokemmo.protocol.PcPackets.BoxPage> pcBox(int box, int start) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(top.totodile.pokemmo.protocol.PcPackets.fetchBox(box, start), response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(packet -> {
            try { return top.totodile.pokemmo.protocol.PcPackets.decodeBoxResponse(packet); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    public synchronized CompletableFuture<top.totodile.pokemmo.protocol.PcPackets.Status> pcMove(
            top.totodile.pokemmo.protocol.PcPackets.Move move) {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(top.totodile.pokemmo.protocol.PcPackets.move(move), response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(packet -> {
            try { return top.totodile.pokemmo.protocol.PcPackets.decodeStatusResponse(packet, top.totodile.pokemmo.protocol.PcPackets.MOVE); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
    }

    public synchronized CompletableFuture<top.totodile.pokemmo.protocol.PcPackets.Status> pcCreateBox() {
        if (!connected || closed) return CompletableFuture.failedFuture(new IOException("Game connection closed"));
        CompletableFuture<byte[]> response = new CompletableFuture<>();
        if (!offerCommand(new Pending(top.totodile.pokemmo.protocol.PcPackets.createBox(), response)))
            return CompletableFuture.failedFuture(new IOException("Game command queue full"));
        return response.thenApply(packet -> {
            try { return top.totodile.pokemmo.protocol.PcPackets.decodeStatusResponse(packet, top.totodile.pokemmo.protocol.PcPackets.CREATE_BOX); }
            catch (IOException error) { throw new java.util.concurrent.CompletionException(error); }
        });
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
