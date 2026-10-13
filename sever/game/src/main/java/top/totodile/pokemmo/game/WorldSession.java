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
    private final FireRedWorldRules rules;
    private final CharacterStore store;
    private final int account;
    private final long character;
    private final ScheduledFuture<?> saving;
    private Position position, saved;
    private boolean loading = true, cycling, noclip, closed;
    private int multiplier = 1;
    private long readyAt;
    private long loadDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
    private final Object saveLock = new Object();
    private volatile SQLException saveFailure;

    WorldSession(FireRedWorldRules rules, CharacterStore store, int account, CharacterInfo info,
                 ScheduledExecutorService scheduler) {
        this.rules = rules;
        this.store = store;
        this.account = account;
        character = info.id();
        position = saved = info.position();
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
        if (opcode == WorldPackets.MOVE) {
            var request = WorldPackets.decodeMove(packet);
            duration = Math.max(1, (cycling ? 80 : request.running() ? 135 : 275) / multiplier);
            long now = System.nanoTime();
            if (loading || closed || now < readyAt || request.x() != position.x() || request.y() != position.y())
                return response(false, false, duration);
            var result = rules.move(position, request.direction(), noclip);
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
            loading = true;
            loadDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            return response(true, true, duration);
        }
        throw new IOException("Unknown world operation");
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

    void flush() throws SQLException {
        // 不在 JDBC 等待期间持有会话监视器，数据库阻塞不妨碍网络裁决。
        synchronized (saveLock) {
            Position snapshot;
            synchronized (this) {
                snapshot = position;
                if (snapshot.equals(saved)) return;
            }
            if (!store.savePosition(account, character, snapshot)) throw new SQLException("World save rejected");
            synchronized (this) {
                saved = snapshot;
            }
        }
    }

    @Override
    public void close() throws SQLException {
        synchronized (this) {
            closed = true;
        }
        saving.cancel(false);
        flush();
    }
}
