package top.totodile.pokemmo.game;

import top.totodile.pokemmo.persistence.CharacterStore;
import top.totodile.pokemmo.protocol.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static top.totodile.pokemmo.protocol.CharacterPackets.*;

/** 会话持有权威位置；网络操作更新内存，定时提交与退出提交串行化。 */
final class WorldSession implements AutoCloseable {
    private WorldRules rules;
    private KantoNpcs npcs;
    private final CharacterStore store;
    private final int account;
    private final long character;
    private final ScheduledFuture<?> saving;
    private Position position, saved, movementFrom;
    private final long onlineBase;
    private final long onlineStarted = System.nanoTime();
    private long onlineStopped;
    private long savedOnline;
    private boolean loading = true, cycling, savedCycling, noclip, closed;
    private int multiplier = 1;
    private long readyAt;
    private long loadDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
    private final Object saveLock = new Object();
    private volatile SQLException saveFailure;

    WorldSession(WorldRules rules, CharacterStore store, int account, CharacterInfo info,
                 ScheduledExecutorService scheduler) {
        this.rules = rules;
        if (info.appearance().region() == 0) npcs = new KantoNpcs((FireRedWorldRules) rules);
        this.store = store;
        this.account = account;
        character = info.id();
        onlineBase = savedOnline = info.stats().onlineSeconds();
        position = saved = info.position();
        cycling = savedCycling = info.cycling();
        noclip = debugEnabled() && info.noclip();
        multiplier = debugEnabled() ? info.speedMultiplier() : 1;
        saving = scheduler.scheduleWithFixedDelay(() -> {
            try {
                flush();
            } catch (SQLException error) {
                saveFailure = error;
            }
        }, 200, 200, TimeUnit.MILLISECONDS);
    }

    synchronized byte[] handle(byte[] packet) throws IOException, SQLException {
        checkHealthy();
        int opcode = packet[0] & 255;
        int duration = (cycling ? 80 : 275) / multiplier;
        long npcNow = System.nanoTime();
        if (npcs != null && !loading) npcs.update(position, npcNow < readyAt ? movementFrom : null, npcNow);
        if (opcode == NpcPackets.OPCODE) {
            if (packet.length != 3 || (packet[1] & 255) > 2) throw new IOException("Invalid NPC operation");
            if (npcs == null) return NpcPackets.response(new NpcPackets.Snapshot(position.group(), position.map(), -1, java.util.List.of()));
            int action = packet[1] & 255;
            if (action == 2) npcs.talk(position, 255, npcNow);
            else if (action == 1 && !loading && !closed && npcNow >= readyAt) npcs.talk(position, packet[2] & 255, npcNow);
            return NpcPackets.response(npcs.snapshot(position, npcNow));
        }
        if (opcode == WorldPackets.MOVE) {
            var request = WorldPackets.decodeMove(packet);
            duration = Math.max(1, (cycling ? 80 : request.running() ? 135 : 275) / multiplier);
            long now = System.nanoTime();
            if (loading || closed || npcs != null && npcs.talking() || now < readyAt || request.x() != position.x() || request.y() != position.y())
                return response(false, false, duration);
            var result = npcs == null ? rules.move(position, request.direction(), noclip)
                    : ((FireRedWorldRules) rules).move(position, request.direction(), noclip,
                    p -> npcs.occupied(p.group(), p.map(), p.x(), p.y(), now));
            movementFrom = position;
            position = result.position();
            if (result.moved()) {
                readyAt = now + TimeUnit.MILLISECONDS.toNanos(duration);
                loading = result.warp();
                if (loading) loadDeadline = now + TimeUnit.SECONDS.toNanos(30);
            }
            return WorldPackets.result(new WorldPackets.Result(position, result.moved(), result.warp(),
                    duration, result.approach()));
        }
        if (opcode == WorldPackets.LOADED) {
            if (packet.length != 1) throw new IOException("Invalid loaded acknowledgement");
            if (loading) {
                loading = false;
                readyAt = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(500);
            }
            return response(false, false, duration);
        }
        if (opcode == WorldPackets.SETTINGS) {
            PacketData data = new PacketData(packet);
            data.byteValue();
            boolean bike = data.booleanValue();
            int speed = data.byteValue();
            boolean clip = data.booleanValue();
            data.end();
            if (speed != 1 && speed != 2 && speed != 4 && speed != 8)
                throw new IOException("Invalid movement settings");
            if (!debugEnabled() && (speed != 1 || clip)) throw new IOException("Debug movement disabled");
            if (!store.saveDebugSettings(account, character, clip, speed)) throw new SQLException("Debug settings save rejected");
            cycling = bike;
            multiplier = speed;
            noclip = clip;
            return response(false, false, duration);
        }
        if (opcode == WorldPackets.TELEPORT) {
            if (!debugEnabled()) throw new IOException("Debug teleport disabled");
            PacketData data = new PacketData(packet);
            data.byteValue();
            int group = data.byteValue(), map = data.byteValue(), x = data.shortValue(), y = data.shortValue();
            data.end();
            try {
                position = rules.at(group, map, x, y, 0, null);
            } catch (IllegalArgumentException error) {
                throw new IOException("Invalid teleport", error);
            }
            if (npcs != null) npcs.talk(position, 255, System.nanoTime());
            loading = true;
            loadDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            return response(true, true, duration);
        }
        throw new IOException("Unknown world operation");
    }

    CharacterInfo transferRegion(int region, int group, int map, int x, int y, WorldRules targetRules) throws IOException, SQLException {
        if (!debugEnabled()) throw new IOException("Debug teleport disabled");
        Position target;
        try { target = targetRules.at(group, map, x, y, 0, null); }
        catch (IllegalArgumentException error) { throw new IOException("Invalid region teleport", error); }
        synchronized (saveLock) {
            synchronized (this) {
                checkHealthy();
                if (closed) throw new IOException("World is closed");
                long online = onlineSeconds();
                if (!store.transferRegion(account, character, region, target, online, cycling)) throw new SQLException("Region transfer rejected");
                rules = targetRules;
                npcs = region == 0 ? new KantoNpcs((FireRedWorldRules) rules) : null; position = saved = target;
                savedOnline = online; savedCycling = cycling;
                loading = true; readyAt = 0;
                loadDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            }
            return store.list(account).stream().filter(info -> info.id() == character).findFirst()
                    .orElseThrow(() -> new SQLException("Transferred character missing"));
        }
    }

    private static boolean debugEnabled() {
        return Boolean.parseBoolean(System.getProperty("pokemmo.game.debug", "true"));
    }

    synchronized void checkHealthy() throws IOException, SQLException {
        if (saveFailure != null) throw saveFailure;
        if (loading && System.nanoTime() >= loadDeadline) throw new IOException("Map load acknowledgement timed out");
    }

    private byte[] response(boolean moved, boolean warp, int duration) {
        return WorldPackets.result(new WorldPackets.Result(position, moved, warp, Math.max(1, duration)));
    }

    private synchronized long onlineSeconds() {
        return onlineBase + TimeUnit.NANOSECONDS.toSeconds(Math.max(0, (closed ? onlineStopped : System.nanoTime()) - onlineStarted));
    }

    TrainerStats stats() throws SQLException {
        return store.stats(account, character, onlineSeconds());
    }

    void flush() throws SQLException {
        // 不在 JDBC 等待期间持有会话监视器，数据库阻塞不妨碍网络裁决。
        synchronized (saveLock) {
            Position snapshot;
            long online;
            boolean bike;
            synchronized (this) {
                snapshot = position;
                online = onlineSeconds();
                bike = cycling;
                if (snapshot.equals(saved) && online == savedOnline && bike == savedCycling) return;
            }
            if (!store.saveWorldState(account, character, snapshot, online, bike)) throw new SQLException("World save rejected");
            synchronized (this) {
                saved = snapshot;
                savedOnline = online;
                savedCycling = bike;
            }
        }
    }

    @Override
    public void close() throws SQLException {
        synchronized (this) {
            if (!closed) {
                onlineStopped = System.nanoTime();
                closed = true;
            }
        }
        saving.cancel(false);
        flush();
    }
}
