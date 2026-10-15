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
    private int region;
    private boolean wildEnabled = true;
    private EncounterCatalog.Selection activeEncounter;
    private final WildEncounterState encounters = new WildEncounterState();

    private KantoNpcs npcs;
    private KantoOpening opening;
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
                 ScheduledExecutorService scheduler) throws SQLException, IOException {
        this.rules = rules;
        region = info.appearance().region();
        if (info.appearance().region() == 0) npcs = new KantoNpcs((FireRedWorldRules) rules);
        this.store = store;
        this.account = account;
        character = info.id();
        if (npcs != null) opening = new KantoOpening(store.kantoOpeningComplete(account, character));
        onlineBase = savedOnline = info.stats().onlineSeconds();
        position = saved = info.position();
        cycling = savedCycling = info.cycling();
        noclip = debugEnabled() && info.noclip();
        multiplier = debugEnabled() ? info.speedMultiplier() : 1;
        wildEnabled = !debugEnabled() || info.wildEncounters();
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
        if (npcs != null && !loading && (opening == null || !opening.active())) npcs.update(position, npcNow < readyAt ? movementFrom : null, npcNow);
        if (opcode == EncounterPackets.OPCODE) {
            PacketData request = new PacketData(packet); request.byteValue();
            int action = request.byteValue(); boolean enabled = request.booleanValue(); request.end();
            boolean accepted = true;
            if (action == EncounterPackets.SET) {
                if (!debugEnabled() && !enabled) accepted = false;
                else if (!store.setWildEncounters(account, character, enabled)) accepted = false;
                else { wildEnabled = enabled; encounters.reset(); }
            } else if (action == EncounterPackets.FINISH) {
                activeEncounter = null; encounters.reset();
            } else if (action == EncounterPackets.SCENT) {
                if (activeEncounter != null || loading || npcNow < readyAt || opening != null && opening.active()
                        || npcs != null && npcs.talking()) accepted = false;
                else {
                    var table = currentEncounterTable();
                    if (table == null || !hasAliveParty()) accepted = false;
                    else { activeEncounter = table.rollHorde(java.util.concurrent.ThreadLocalRandom.current()); encounters.reset(); }
                }
            } else if (action != EncounterPackets.QUERY) throw new IOException("Invalid encounter operation");
            return EncounterPackets.response(new EncounterPackets.Snapshot(wildEnabled, accepted, activeEncounter));
        }
        if (opcode == StoryPackets.OPCODE) {
            if (packet.length != 3 || (packet[1] & 255) > StoryPackets.START || (packet[2] & 255) > 4)
                throw new IOException("Invalid story operation");
            if (opening == null) return StoryPackets.response(new StoryPackets.Snapshot(4, 0, 0, 0, null));
            int action = packet[1] & 255;
            if (action >= StoryPackets.RESET) {
                if (!debugEnabled()) throw new IOException("Story debug disabled");
                if (loading || npcNow < readyAt) return StoryPackets.response(opening.snapshot(npcNow));
                if (action == StoryPackets.START) opening.onPosition(position);
                else {
                    store.saveKantoOpening(account, character, action == StoryPackets.SKIP);
                    opening.reset(action == StoryPackets.SKIP);
                }
            } else if (action == StoryPackets.ADVANCE) {
                // 完成存档先提交，失败时保留原阶段，不能向客户端宣称完成。
                if ((packet[2] & 255) == 3 && opening.snapshot(npcNow).phase() == 3)
                    store.saveKantoOpening(account, character, true);
                if ((packet[2] & 255) == 1 && opening.snapshot(npcNow).phase() == 1)
                    position = new Position(position.group(), position.map(), position.x(), position.y(),
                            position.elevation(), 0, position.returnPoint());
                opening.advance(packet[2] & 255, npcNow);
            } else if (!loading && npcNow >= readyAt) opening.onPosition(position);
            return StoryPackets.response(opening.snapshot(npcNow));
        }
        if (opcode == NpcPackets.OPCODE) {
            if (packet.length != 3 || (packet[1] & 255) > 2) throw new IOException("Invalid NPC operation");
            if (npcs == null) return NpcPackets.response(new NpcPackets.Snapshot(position.group(), position.map(), -1, java.util.List.of()));
            int action = packet[1] & 255;
            if (action == 2) npcs.talk(position, 255, npcNow);
            else if (action == 1 && (opening == null || !opening.active()) && !loading && !closed && npcNow >= readyAt) npcs.talk(position, packet[2] & 255, npcNow);
            return NpcPackets.response(npcs.snapshot(position, npcNow));
        }
        if (opcode == WorldPackets.MOVE) {
            var request = WorldPackets.decodeMove(packet);
            duration = Math.max(1, (cycling ? 80 : request.running() ? 135 : 275) / multiplier);
            long now = System.nanoTime();
            if (loading || closed || activeEncounter != null || opening != null && opening.active() || npcs != null && npcs.talking() || now < readyAt || request.x() != position.x() || request.y() != position.y())
                return response(false, false, duration);
            var result = npcs == null ? rules.move(position, request.direction(), noclip)
                    : ((FireRedWorldRules) rules).move(position, request.direction(), noclip,
                    p -> npcs.occupied(p.group(), p.map(), p.x(), p.y(), now));
            movementFrom = position;
            position = result.position();
            if (opening != null && result.moved() && !result.warp()) opening.onPosition(position);
            if (result.moved()) {
                readyAt = now + TimeUnit.MILLISECONDS.toNanos(duration);
                loading = result.warp();
                if (loading) loadDeadline = now + TimeUnit.SECONDS.toNanos(30);
            }
            EncounterCatalog.Selection encounter = null;
            if (result.moved()) {
                if (result.warp() || movementFrom.group() != position.group() || movementFrom.map() != position.map()) encounters.reset();
                else if (wildEnabled && (opening == null || !opening.active())) {
                    encounter = encounters.step(currentEncounterTable(), rules.encounterBehavior(position), cycling,
                            java.util.concurrent.ThreadLocalRandom.current());
                    if (encounter != null && hasAliveParty()) activeEncounter = encounter;
                    else encounter = null;
                }
            }
            return WorldPackets.result(new WorldPackets.Result(position, result.moved(), result.warp(),
                    duration, result.approach(), encounter));
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
            if (activeEncounter != null || opening != null && opening.active()) return response(false, false, duration);
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
        if (activeEncounter != null || opening != null && opening.active()) throw new IOException("World interaction in progress");
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
                this.region = region;
                encounters.reset();
                npcs = region == 0 ? new KantoNpcs((FireRedWorldRules) rules) : null;
                opening = region == 0 ? new KantoOpening(store.kantoOpeningComplete(account, character)) : null;
                position = saved = target;
                savedOnline = online; savedCycling = cycling;
                loading = true; readyAt = 0;
                loadDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
            }
            return store.list(account).stream().filter(info -> info.id() == character).findFirst()
                    .orElseThrow(() -> new SQLException("Transferred character missing"));
        }
    }

    private boolean hasAliveParty() throws SQLException {
        return store.pcParty(account, character).party().stream().anyMatch(member -> member.hp() > 0);
    }

    private EncounterCatalog.Table currentEncounterTable() {
        int season = (java.time.LocalDate.now(java.time.ZoneOffset.UTC).getMonthValue() - 1) % 4;
        int hour = (int) Math.floorMod(java.time.Instant.now().getEpochSecond() * 4 / 3600, 24);
        int time = hour >= 4 && hour < 10 ? 0 : hour >= 10 && hour < 20 ? 1 : 2;
        return EncounterCatalog.walking(region, position, rules.encounterBehavior(position), season, time);
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
