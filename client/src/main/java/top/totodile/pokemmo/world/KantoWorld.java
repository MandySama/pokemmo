package top.totodile.pokemmo.world;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.protocol.CharacterPackets.Appearance;
import top.totodile.pokemmo.protocol.CharacterPackets.ReturnPoint;
import top.totodile.pokemmo.protocol.CharacterPackets.Position;
import top.totodile.pokemmo.protocol.WorldPackets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import top.totodile.pokemmo.rom.FireRedMap;
import top.totodile.pokemmo.rom.FireRedAtlas;
import top.totodile.pokemmo.rom.GbaMapPreview;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/** 火红地图的格子步行、传送与装扮渲染；纹理均由绘制线程管理。 */
public final class KantoWorld implements AutoCloseable {
    private record Sprite(int texture, int width, int height, float scale) { }
    private static final int[][] WALK = {
            {0, 3, 0, 4}, {1, 5, 1, 6}, {2, 7, 2, 8},
            {9, 10, 9, 11}, {12, 13, 12, 14}, {15, 16, 15, 17},
            {18, 21, 18, 22}, {19, 23, 19, 24}, {20, 25, 20, 26}
    };
    private static final long STEP_NANOS = 275_000_000L;
    private static final long ENTRY_FADE_NANOS = 500_000_000L;
    private record Scene(FireRedMap map, GbaMapPreview renderer, List<FireRedAtlas.Npc> npcs) { }
    private FireRedMap map;
    private GbaMapPreview renderer;
    private FireRedAtlas atlas;
    private FireRedAtlas.Key current = new FireRedAtlas.Key(4, 1);
    private final Map<FireRedAtlas.Key, Scene> scenes = new LinkedHashMap<>(16, 0.75f, true);
    private MapTestWorld layout;
    private ReturnPoint returnPoint;
    private List<FireRedAtlas.Npc> npcs = List.of();
    private final AvatarAssets assets;
    private final Appearance appearance;
    private final AvatarAssets.Item[] items = new AvatarAssets.Item[12];
    private final Map<Integer, Sprite> sprites = new HashMap<>();
    private final boolean[] held = new boolean[4];
    private final long started = System.nanoTime();
    private int x = 6, y = 6, fromX = 6, fromY = 6, facing, lastPressed;
    private long stepStarted;
    private long stepNanos = STEP_NANOS;
    private boolean running;
    private boolean cycling;
    private final boolean[] modeKeys = new boolean[2];
    private long animationStarted;
    private boolean animating;
    private boolean stepping;
    private boolean noclip;

    private int speedMultiplier = 1;

    public int speedMultiplier() { return speedMultiplier; }

    public void setSpeedMultiplier(int multiplier) {
        if (multiplier != 1 && multiplier != 2 && multiplier != 4 && multiplier != 8)
            throw new IllegalArgumentException("Invalid movement speed multiplier");
        speedMultiplier = multiplier;
    }

    public boolean noclip() { return noclip; }

    /** 会话内的碰撞开关；地图边界与传送判定始终保留。 */
    public void setNoclip(boolean enabled) { noclip = enabled; }
    private long entryFadeStarted;
    private boolean entering;
    private record Transfer(FireRedAtlas.Key key, Scene scene, int x, int y, int facing) { }
    private Transfer transfer;
    private Transfer pendingTransfer;
    private Function<byte[], CompletableFuture<WorldPackets.Result>> authority;
    private CompletableFuture<WorldPackets.Result> worldReply;
    private boolean replyMoves;
    private byte[] sentSettings;
    private long nextRequestAt;
    private boolean disposed;

    public void setAuthority(Function<byte[], CompletableFuture<WorldPackets.Result>> authority) {
        this.authority = authority;
        acknowledgeLoaded();
    }

    private void acknowledgeLoaded() {
        if (authority != null) {
            replyMoves = false;
            worldReply = authority.apply(new byte[]{WorldPackets.LOADED});
        }
    }

    private void requestMove(int direction) {
        var packets = WorldPackets.settings(cycling, speedMultiplier, noclip);
        byte[] move = WorldPackets.move(x, y, direction, running);
        replyMoves = true;
        if (java.util.Arrays.equals(packets, sentSettings)) worldReply = authority.apply(move);
        else {
            sentSettings = packets;
            worldReply = authority.apply(packets).thenCompose(ignored -> authority.apply(move));
        }
    }

    private void applyReply(long now) {
        if (worldReply.isCompletedExceptionally()) {
            worldReply = null;
            clearKeys();
            return;
        }
        var reply = worldReply.join();
        worldReply = null;
        if (!replyMoves || disposed) return;
        if (!reply.moved()) nextRequestAt = now + 100_000_000L;
        var p = reply.position();
        facing = p.facing();
        if (reply.warp()) {
            try {
                var key = new FireRedAtlas.Key(p.group(), p.map());
                transfer = new Transfer(key, scene(key), p.x(), p.y(), p.facing());
                returnPoint = p.returnPoint();
                fadeOutStarted = now;
                fromX = x;
                fromY = y;
                stepping = animating = false;
                clearKeys();
                if (reply.approach() != null) {
                    pendingTransfer = transfer;
                    transfer = null;
                    var approach = reply.approach();
                    var approachKey = new FireRedAtlas.Key(approach.group(), approach.map());
                    Scene on = scene(approachKey);
                    current = approachKey;
                    map = on.map();
                    renderer = on.renderer();
                    npcs = on.npcs();
                    layout.select(current);
                    facing = approach.facing();
                    fromX = approach.x() - (facing == 2 ? -1 : facing == 3 ? 1 : 0);
                    fromY = approach.y() - (facing == 1 ? -1 : facing == 0 ? 1 : 0);
                    x = approach.x();
                    y = approach.y();
                    stepping = animating = true;
                    animationStarted = stepStarted = now;
                    stepNanos = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(reply.millis());
                }
            } catch (IOException error) { throw new IllegalStateException(error); }
        } else if (reply.moved()) {
            try {
                var key = new FireRedAtlas.Key(p.group(), p.map());
                Scene destination = scene(key);
                fromX = p.x() - (facing == 2 ? -1 : facing == 3 ? 1 : 0);
                fromY = p.y() - (facing == 1 ? -1 : facing == 0 ? 1 : 0);
                current = key;
                map = destination.map();
                renderer = destination.renderer();
                npcs = destination.npcs();
                layout.select(key);
                returnPoint = p.returnPoint();
                x = p.x();
                y = p.y();
                if (!animating || now - stepStarted > 320_000_000L) animationStarted = now;
                stepping = animating = true;
                stepStarted = now;
                stepNanos = java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(reply.millis());
            } catch (IOException error) { throw new IllegalStateException(error); }
        } else if (p.group() != current.group() || p.map() != current.map() || p.x() != x || p.y() != y) {
            try { restore(p); } catch (IOException error) { throw new IllegalStateException(error); }
        }
    }
    private long fadeOutStarted;
    private java.util.function.Consumer<Position> onPosition = p -> { };
    private Position lastPublished;

    public void restore(Position p) throws IOException {
        var key = new FireRedAtlas.Key(p.group(), p.map());
        Scene scene = atlas == null ? scenes.get(key) : scene(key);
        if (!p.valid() || scene == null || p.x() >= scene.map().width() / 16 || p.y() >= scene.map().height() / 16)
            throw new IOException("Invalid saved position");
        current = key; map = scene.map(); renderer = scene.renderer(); npcs = scene.npcs();
        if (layout != null) layout.select(key);
        returnPoint = p.returnPoint();
        x = fromX = p.x(); y = fromY = p.y(); facing = p.facing();
        lastPublished = position();
    }

    public void setOnPosition(java.util.function.Consumer<Position> listener) {
        onPosition = listener;
    }

    private Position position() {
        return new Position(current.group(), current.map(), x, y, map.elevation(x, y) + 1, facing, returnPoint);
    }

    /** 只发布已走完的格子和传送落点，插值中的坐标不进入存档。 */
    private void publishPosition() {
        if (authority != null) return;
        var p = position();
        if (!p.equals(lastPublished)) { onPosition.accept(p); lastPublished = p; }
    }

    /** 地图纹理准备完成后开始淡入，过渡期间保持出生位置。 */
    public void beginEntry(long now) {
        clearKeys();
        entryFadeStarted = now;
        entering = true;
    }

    public KantoWorld(FireRedAtlas atlas, AvatarAssets assets, Appearance appearance) throws IOException {
        this(atlas.load(new FireRedAtlas.Key(4, 1), true), assets, appearance);
        this.atlas = atlas;
        scenes.put(current, new Scene(map, renderer, npcs));
        try {
            layout = new MapTestWorld(atlas);
            layout.select(current);
        } catch (IOException | RuntimeException exception) {
            close();
            throw exception;
        }
    }

    private Scene scene(FireRedAtlas.Key key) throws IOException {
        Scene cached = scenes.get(key);
        if (cached != null) return cached;
        loadScene(key);
        return scenes.get(key);
    }

    private void loadScene(FireRedAtlas.Key key) throws IOException {
        var floor = atlas.load(key, true);
        var objects = atlas.npcs(key).stream().filter(npc -> npc.flag() == 0 && npc.hasStaticGraphics())
                .sorted(java.util.Comparator.comparingInt(FireRedAtlas.Npc::y)).toList();
        for (var npc : objects) {
            int spriteKey = npcSpriteKey(npc);
            if (!sprites.containsKey(spriteKey)) {
                var sprite = atlas.npcSprite(npc, true, 1);
                upload(spriteKey, sprite.image(), sprite.scale());
            }
        }
        scenes.put(key, new Scene(floor, new GbaMapPreview(floor), objects));
    }

    private static int npcSpriteKey(FireRedAtlas.Npc npc) {
        return 128 + npc.graphics() * 4 + npc.direction();
    }

    public KantoWorld(FireRedMap map, AvatarAssets assets, Appearance appearance) throws IOException {
        this.map = map;
        this.assets = assets;
        this.appearance = appearance;
        int[] ids = appearance.items();
        for (int category = 1; category < items.length; category++) {
            for (var item : assets.choices(category, appearance.gender())) {
                if (item.id == ids[category]) { items[category] = item; break; }
            }
        }
        if (items[11] == null) {
            var bicycles = assets.choices(11, appearance.gender());
            if (!bicycles.isEmpty()) items[11] = bicycles.getFirst();
        }
        renderer = new GbaMapPreview(map);
        try {
            upload(64, GameSprites.shadow());
            for (int direction = 0; direction < 4; direction++) {
                for (int frame : WALK[Math.min(direction, 2)]) sprite(direction, frame);
            }
        } catch (IOException | RuntimeException exception) {
            close();
            throw exception;
        }
    }

    public void key(int key, boolean pressed) {
        int modeKey = key == GLFW.GLFW_KEY_K ? 0 : key == GLFW.GLFW_KEY_2 ? 1 : -1;
        if (modeKey >= 0) {
            if (pressed && !modeKeys[modeKey] && transfer == null && !entering) {
                if (modeKey == 0) running = !running;
                else cycling = !cycling;
            }
            modeKeys[modeKey] = pressed;
            return;
        }
        int direction = switch (key) {
            case GLFW.GLFW_KEY_S -> 0;
            case GLFW.GLFW_KEY_W -> 1;
            case GLFW.GLFW_KEY_A -> 2;
            case GLFW.GLFW_KEY_D -> 3;
            default -> -1;
        };
        if (direction >= 0) {
            held[direction] = pressed;
            if (pressed) lastPressed = direction;
        }
    }

    /** 准备目标场景后切换；清除旧室内返回点，避免跨区域出口回到旧地图。 */
    public void teleport(int group, int id, int targetX, int targetY, long now) throws IOException {
        if (authority != null) {
            if (worldReply != null || transfer != null || pendingTransfer != null || entering) return;
            clearKeys(); stepping = animating = false;
            replyMoves = true;
            worldReply = authority.apply(new top.totodile.pokemmo.protocol.PacketData(7)
                    .putByte(WorldPackets.TELEPORT).putByte(group).putByte(id)
                    .putShort(targetX).putShort(targetY).written());
            return;
        }
        if (atlas == null || !top.totodile.pokemmo.protocol.FireRedMaps.contains(group, id, targetX, targetY))
            throw new IOException("Invalid teleport destination");
        var key = new FireRedAtlas.Key(group, id);
        Scene destination = scene(key);
        if (targetX >= destination.map().width() / 16 || targetY >= destination.map().height() / 16)
            throw new IOException("Teleport destination outside map");
        clearKeys();
        stepping = false;
        animating = false;
        entering = false;
        returnPoint = null;
        transfer = new Transfer(key, destination, targetX, targetY, 0);
        fadeOutStarted = now;
        fromX = x;
        fromY = y;
    }

    public void clearKeys() { java.util.Arrays.fill(held, false); java.util.Arrays.fill(modeKeys, false); }

    /** 一次只走一个格子；长帧不积攒补走，松键后完成当前步。 */
    public void update(long now) {
        if (worldReply != null) {
            if (!worldReply.isDone()) return;
            applyReply(now);
        }
        if (transfer != null) {
            if (now - fadeOutStarted < ENTRY_FADE_NANOS) return;
            current = transfer.key();
            try { layout.select(current); } catch (IOException exception) { throw new IllegalStateException(exception); }
            map = transfer.scene().map();
            renderer = transfer.scene().renderer();
            npcs = transfer.scene().npcs();
            x = fromX = transfer.x();
            y = fromY = transfer.y();
            facing = transfer.facing();
            transfer = null;
            publishPosition();
            beginEntry(now);
            acknowledgeLoaded();
            return;
        }
        if (entering) {
            if (now - entryFadeStarted < ENTRY_FADE_NANOS) return;
            entering = false;
        }
        if (stepping && now - stepStarted >= stepNanos) {
            stepping = false;
            if (pendingTransfer != null) {
                transfer = pendingTransfer; pendingTransfer = null;
                fadeOutStarted = now; animating = false; clearKeys(); return;
            }
            if (authority == null && warp(facing, now, true)) return;
            publishPosition();
        }
        if (stepping) return;
        int direction = held[lastPressed] ? lastPressed : -1;
        if (direction < 0) for (int i = 0; i < held.length; i++) if (held[i]) { direction = i; break; }
        if (direction < 0) return;
        if (authority != null) { if (now >= nextRequestAt) requestMove(direction); return; }
        facing = direction;
        if (warp(direction, now, false)) return;
        int nextX = x + (direction == 2 ? -1 : direction == 3 ? 1 : 0);
        int nextY = y + (direction == 1 ? -1 : direction == 0 ? 1 : 0);
        if (nextX < 0 || nextY < 0 || nextX >= map.width() / 16 || nextY >= map.height() / 16) {
            if (crossConnection(nextX, nextY, direction, now)) return;
            publishPosition();
            return;
        }
        if (!noclip && ((!map.canMove(x, y, nextX, nextY) && !canEnterDoor(nextX, nextY, direction))
                || occupied(nextX, nextY))) { publishPosition(); return; }
        fromX = x;
        fromY = y;
        x = nextX;
        y = nextY;
        if (!animating || now - stepStarted > 320_000_000L) animationStarted = now;
        animating = true;
        stepStarted = now;
        stepNanos = movementNanos();
        stepping = true;
    }

    /** 在界面绘制后覆盖全屏，使地图和 HUD 共用同一过渡。 */
    public void renderEntryFade(int width, int height, long now) {
        if (!entering && transfer == null) return;
        float progress = Math.max(0, Math.min(1, (now - (transfer == null ? entryFadeStarted : fadeOutStarted)) / (float) ENTRY_FADE_NANOS));
        float alpha = transfer == null ? 1 - progress : progress;
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glPushAttrib(GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT | GL_CURRENT_BIT);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_SCISSOR_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(0, 0, 0, alpha);
        glBegin(GL_QUADS);
        glVertex2i(0, 0);
        glVertex2i(width, 0);
        glVertex2i(width, height);
        glVertex2i(0, height);
        glEnd();
        glPopAttrib();
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    private boolean occupied(int x, int y) {
        for (var npc : npcs) if (npc.x() == x && npc.y() == y) return true;
        return false;
    }

    /** 门格自身带阻挡标记，由可用的向上入门事件接管通行。 */
    private boolean canEnterDoor(int x, int y, int direction) {
        if (atlas == null || direction != 1) return false;
        try {
            if (map.behavior(x, y) != 0x69) return false;
            return atlas.area(current).warps().stream()
                    .anyMatch(warp -> warp.x() == x && warp.y() == y && available(warp));
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    /** 楼梯和出口按方向触发，门按走入触发；完全淡出后才提交目标地图和落点。 */
    private boolean warp(int direction, long now, boolean entered) {
        if (atlas == null) return false;
        try {
            int stair = map.standingWarpDirection(x, y);
            if (entered ? stair >= 0 || map.behavior(x, y) == 0x69 && direction != 1 : stair != direction) return false;
            for (var warp : atlas.area(current).warps()) {
                if (warp.x() != x || warp.y() != y) continue;
                if (!available(warp)) continue;
                FireRedAtlas.Key destinationKey;
                int targetX, targetY, targetFacing = facing;
                if (dynamic(warp)) {
                    destinationKey = new FireRedAtlas.Key(returnPoint.group(), returnPoint.map());
                    targetX = returnPoint.x(); targetY = returnPoint.y(); targetFacing = returnPoint.facing();
                } else {
                    destinationKey = warp.target();
                    var arrival = atlas.area(destinationKey).warps().get(warp.targetWarp());
                    targetX = arrival.x(); targetY = arrival.y();
                }
                Scene destination = scene(destinationKey);
                if (!dynamic(warp) && atlas.area(destinationKey).warps().stream().anyMatch(KantoWorld::dynamic)) {
                    returnPoint = new ReturnPoint(current.group(), current.map(), x, y, facing);
                }
                // 室外门格落点向下移一格，人物朝下走出门口。
                if (destination.map().behavior(targetX, targetY) == 0x69
                        && destination.map().walkable(targetX, targetY + 1)) {
                    targetY++;
                    targetFacing = 0;
                }
                transfer = new Transfer(destinationKey, destination, targetX, targetY, targetFacing);
                fadeOutStarted = now;
                fromX = x;
                fromY = y;
                animating = false;
                clearKeys();
                return true;
            }
        } catch (IOException exception) {
            // 事件目标读取失败时终止当前地图操作。
            throw new IllegalStateException(exception);
        }
        return false;
    }

    private static boolean dynamic(FireRedAtlas.Warp warp) {
        return warp.target().group() == 127 || warp.target().map() == 127
                || warp.target().group() == 255 || warp.target().map() == 255 || warp.targetWarp() == 255;
    }

    private boolean available(FireRedAtlas.Warp warp) {
        try {
            if (dynamic(warp)) return returnPoint != null && returnPoint.valid();
            return warp.targetWarp() < atlas.area(warp.target()).warps().size();
        } catch (IOException exception) { return false; }
    }

    /** 连接偏移以目标地图格子为单位，过界时同步转换插值起点和镜头坐标。 */
    private boolean crossConnection(int nextX, int nextY, int direction, long now) {
        if (atlas == null) return false;
        int connectionDirection = switch (direction) { case 0 -> 1; case 1 -> 2; case 2 -> 3; default -> 4; };
        try {
            var from = atlas.area(current);
            for (var connection : from.connections()) {
                if (connection.direction() != connectionDirection) continue;
                var to = atlas.area(connection.target());
                int originX = switch (connectionDirection) { case 3 -> -to.columns(); case 4 -> from.columns(); default -> connection.offset(); };
                int originY = switch (connectionDirection) { case 1 -> from.rows(); case 2 -> -to.rows(); default -> connection.offset(); };
                int targetX = nextX - originX, targetY = nextY - originY;
                if (targetX < 0 || targetY < 0 || targetX >= to.columns() || targetY >= to.rows()) continue;
                Scene target = scene(to.key());
                if (!noclip && (!target.map().walkable(targetX, targetY)
                        || target.npcs().stream().anyMatch(n -> n.x() == targetX && n.y() == targetY))) return false;
                fromX = x - originX; fromY = y - originY; x = targetX; y = targetY;
                current = to.key(); map = target.map(); renderer = target.renderer(); npcs = target.npcs();
                layout.select(current);
                if (!animating || now - stepStarted > 320_000_000L) animationStarted = now;
                animating = stepping = true; stepStarted = now; stepNanos = movementNanos();
                return true;
            }
        } catch (IOException exception) { throw new IllegalStateException(exception); }
        return false;
    }

    public void render(int width, int height, long now) throws IOException {
        update(now);
        renderer.setBorderVisible(true);
        // 同帧创建后可能收到创建前采样的时间，首帧从动画零点开始。
        long elapsedMillis = Math.max(0L, now - started) / 1_000_000;
        double progress = stepping ? Math.min(1, (now - stepStarted) / (double) stepNanos) : 1;
        // 地图坐标先截断到 ROM 像素，再进入正交投影，人物和镜头使用相同位置。
        int px = (int) ((fromX + (x - fromX) * progress) * 16);
        int py = (int) ((fromY + (y - fromY) * progress) * 16);
        int scale = WorldCamera.scale(width);
        int left = WorldCamera.origin(width, px + 8, scale);
        int top = WorldCamera.origin(height, py + 12, scale);
        if (now - stepStarted > 320_000_000L) animating = false;
        int animation = animating ? (int) ((now - animationStarted) / ((running && !cycling ? 140_000_000L : 160_000_000L) / speedMultiplier) % 4) : 0;
        int mode = cycling ? 2 : running && animating ? 1 : 0;
        int frame = WALK[mode * 3 + Math.min(facing, 2)][animation];
        sprite(facing, frame);
        Sprite sprite = sprites.get(avatarSpriteKey(facing, frame));
        renderer.renderWithActors(width, height, 1, 12, 0, elapsedMillis, left, top, scale, layer -> {
            glColor4f(1, 1, 1, 1);
            for (var npc : npcs) if (npcLayer(npc) == layer && npc.y() * 16 <= py) drawNpc(npc, left, top, scale);
            if (layer == (map.elevation(x, y) >= 3 ? 2 : 1)) {
                Sprite shadow = sprites.get(64);
                draw(shadow, left + (px - 4.5f + shadow.width * 0.125f) * scale,
                        top + (py - 5 + shadow.height * 0.125f) * scale,
                        shadow.width * 0.75f * scale, shadow.height * 0.75f * scale, false);
                glBindTexture(GL_TEXTURE_2D, sprite.texture);
                // 普通站立与步行共用画布锚点，镜像只改变纹理方向。
                float actorLeft = left + (px - 13.5f) * scale;
                float actorTop = top + (py - 20) * scale;
                float actorWidth = sprite.width * 0.75f * scale;
                float actorHeight = sprite.height * 0.75f * scale;
                draw(sprite, actorLeft, actorTop, actorWidth, actorHeight, facing == 3);
            }
            for (var npc : npcs) if (npcLayer(npc) == layer && npc.y() * 16 > py) drawNpc(npc, left, top, scale);
        });
        if (layout != null) {
            java.util.Set<FireRedAtlas.Key> visible = new java.util.HashSet<>();
            visible.add(current);
            for (var entry : layout.positions().entrySet()) {
                if (entry.getKey().equals(current)) continue;
                var area = atlas.area(entry.getKey());
                int neighborLeft = left + entry.getValue().x() * 16 * scale;
                int neighborTop = top + entry.getValue().y() * 16 * scale;
                if (neighborLeft >= width || neighborTop >= height || neighborLeft + area.columns() * 16 * scale <= 0
                        || neighborTop + area.rows() * 16 * scale <= 0) continue;
                visible.add(entry.getKey());
                Scene neighbor = scene(entry.getKey());
                neighbor.renderer().setBorderVisible(false);
                neighbor.renderer().renderWithActors(width, height, 1, 12, 0, elapsedMillis,
                        neighborLeft, neighborTop, scale, layer -> {
                            glColor4f(1, 1, 1, 1);
                            for (var npc : neighbor.npcs()) {
                                if (npcLayer(npc) == layer) drawNpc(npc, neighborLeft, neighborTop, scale);
                            }
                        });
            }
            trimScenes(visible);
        }
    }

    private void trimScenes(java.util.Set<FireRedAtlas.Key> visible) {
        var iterator = scenes.entrySet().iterator();
        while (scenes.size() > 16 && iterator.hasNext()) {
            var entry = iterator.next();
            if (visible.contains(entry.getKey()) || transfer != null && entry.getValue() == transfer.scene()
                    || pendingTransfer != null && entry.getValue() == pendingTransfer.scene()) continue;
            entry.getValue().renderer().close();
            iterator.remove();
        }
        java.util.Set<Integer> used = new java.util.HashSet<>();
        for (Scene scene : scenes.values()) for (var npc : scene.npcs()) used.add(npcSpriteKey(npc));
        sprites.entrySet().removeIf(entry -> {
            if (entry.getKey() < 128 || used.contains(entry.getKey())) return false;
            glDeleteTextures(entry.getValue().texture()); return true;
        });
    }

    private static int npcLayer(FireRedAtlas.Npc npc) {
        return npc.elevation() < 3 ? 1 : 2;
    }

    private void drawNpc(FireRedAtlas.Npc npc, int left, int top, int scale) {
        Sprite sprite = sprites.get(npcSpriteKey(npc));
        int px = npc.x() * 16, py = npc.y() * 16;
        Sprite shadow = sprites.get(64);
        draw(shadow, left + (px - 4.5f + shadow.width * 0.125f) * scale,
                top + (py - 5 + shadow.height * 0.125f) * scale,
                shadow.width * 0.75f * scale, shadow.height * 0.75f * scale, false);
        float width = sprite.width * sprite.scale, height = sprite.height * sprite.scale;
        draw(sprite, left + (px + (sprite.scale == 0.75f ? 0.5f : 0) - (width - 16) / 2f) * scale,
                top + (py - (height - 16)) * scale,
                width * scale, height * scale, npc.direction() == 3);
    }

    private long movementNanos() {
        return (cycling ? 80_000_000L : running ? 135_000_000L : STEP_NANOS) / speedMultiplier;
    }

    private static int avatarSpriteKey(int direction, int frame) {
        return -1 - direction * 32 - frame;
    }

    private void sprite(int direction, int frame) throws IOException {
        int key = avatarSpriteKey(direction, frame);
        if (sprites.containsKey(key)) return;
        upload(key, assets.renderOverworld(appearance.skin(), items, appearance.colors(), frame, direction));
    }

    private void upload(int key, BufferedImage image) {
        upload(key, image, 1);
    }

    private void upload(int key, BufferedImage image, float scale) {
        ByteBuffer pixels = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
            int argb = image.getRGB(x, y);
            pixels.put((byte) (argb >>> 16)).put((byte) (argb >>> 8)).put((byte) argb).put((byte) (argb >>> 24));
        }
        pixels.flip();
        int texture = glGenTextures();
        sprites.put(key, new Sprite(texture, image.getWidth(), image.getHeight(), scale));
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private static void draw(Sprite sprite, float x, float y, float width, float height, boolean flip) {
        glBindTexture(GL_TEXTURE_2D, sprite.texture);
        float u0 = flip ? 1 : 0, u1 = 1 - u0;
        glBegin(GL_QUADS);
        glTexCoord2f(u0, 0); glVertex2f(x, y);
        glTexCoord2f(u1, 0); glVertex2f(x + width, y);
        glTexCoord2f(u1, 1); glVertex2f(x + width, y + height);
        glTexCoord2f(u0, 1); glVertex2f(x, y + height);
        glEnd();
    }

    @Override public void close() {
        disposed = true;
        clearKeys();
        if (scenes.isEmpty()) renderer.close();
        else for (Scene scene : scenes.values()) scene.renderer().close();
        scenes.clear();
        for (Sprite sprite : sprites.values()) glDeleteTextures(sprite.texture);
        sprites.clear();
    }
}
