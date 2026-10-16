package top.totodile.pokemmo.world;

import static org.lwjgl.opengl.GL11.*;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.GdxNativesLoader;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.avatar.AvatarAssets;
import top.totodile.pokemmo.protocol.*;
import top.totodile.pokemmo.protocol.CharacterPackets.*;
import top.totodile.pokemmo.rom.BlackWhiteMapPreview;
import top.totodile.pokemmo.rom.FireRedAtlas;
import top.totodile.pokemmo.rom.NdsMaps;
import top.totodile.pokemmo.rom.NdsNpcSprites;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/** NDS 三维场景中的角色步行；移动和传送沿用游戏服务裁决。 */
public class NdsWorld implements ClientWorld {
    static {
        GdxNativesLoader.load();
    }

    private record SceneKey(int header, int chunk) {}

    private record Sprite(int texture, int width, int height) {}

    private static final int[][] FRAMES = {
        {0, 3, 0, 4},
        {1, 5, 1, 6},
        {2, 7, 2, 8},
        {9, 10, 9, 11},
        {12, 13, 12, 14},
        {15, 16, 15, 17},
        {18, 21, 18, 22},
        {19, 23, 19, 24},
        {20, 25, 20, 26}
    };
    private final NdsMaps maps;
    private final BlackWhiteWorldRules rules;
    private final AvatarAssets assets;
    private final NdsNpcSprites npcImages;
    private final NdsNpcs localNpcs;
    private Function<byte[], CompletableFuture<NpcPackets.Snapshot>> npcAuthority;
    private CompletableFuture<NpcPackets.Snapshot> npcReply;
    private final Map<Integer, NpcPackets.State> npcStates = new HashMap<>();
    private long npcClock, nextNpcPoll;
    private boolean conversing;

    private record NpcSprite(int texture, int width, int height, int foot) {}

    private final Map<Long, NpcSprite> npcSprites = new LinkedHashMap<>(128, .75f, true);
    private final java.util.Set<Integer> missingNpcSprites = new java.util.HashSet<>();
    private EncounterCatalog.Selection wildEncounter;

    @Override
    public EncounterCatalog.Selection takeWildEncounter() {
        if (stepping || entering) return null;
        var result = wildEncounter;
        wildEncounter = null;
        return result;
    }

    @Override
    public int encounterBehavior() {
        return rules.encounterBehavior(position);
    }

    private Appearance appearance;
    private final AvatarAssets.Item[] items = new AvatarAssets.Item[12];
    private final PerspectiveCamera camera = new PerspectiveCamera(35, 1024, 768);
    private final Map<SceneKey, BlackWhiteMapPreview> scenes = new LinkedHashMap<>(16, 0.75f, true);
    private final Map<Integer, Sprite> sprites = new HashMap<>();
    private final boolean[] held = new boolean[4];
    private final boolean[] toggles = new boolean[2];
    private Position position;
    private Position previous;
    private long stepStarted, stepNanos, nextRequest, fadeStarted, transferStarted;
    private boolean stepping, entering, transferring, running, cycling, noclip, followerFailure;
    private int multiplier = 1, lastPressed;
    private Position pendingTransfer;
    private Function<byte[], CompletableFuture<WorldPackets.Result>> authority;
    private CompletableFuture<WorldPackets.Result> reply;
    private byte[] sentSettings;

    protected NdsWorld(NdsMaps maps, AvatarAssets assets, Appearance appearance)
            throws IOException {
        this.maps = maps;
        this.assets = assets;
        this.appearance = appearance;
        rules = new BlackWhiteWorldRules(appearance.region());
        localNpcs = new NdsNpcs(region(), rules);
        npcImages = new NdsNpcSprites(maps.rom(), region());
        for (int category = 1; category < items.length; category++) {
            for (var item : assets.choices(category, appearance.gender(), true)) {
                if (item.id == appearance.items()[category]) {
                    items[category] = item;
                    break;
                }
            }
        }
        if (items[11] == null) {
            var bicycles = assets.choices(11, appearance.gender());
            if (!bicycles.isEmpty()) items[11] = bicycles.getFirst();
        }
        camera.near = 0.01f;
        camera.far = 80;
        position = previous = Position.spawn(appearance.region(), appearance.gender());
    }

    @Override
    public void replaceOutfit(Appearance next) {
        if (next.region() != appearance.region()
                || next.gender() != appearance.gender()
                || next.skin() != appearance.skin())
            throw new IllegalArgumentException("Outfit identity mismatch");
        appearance = next;
        java.util.Arrays.fill(items, null);
        for (int category = 1; category < items.length; category++) {
            for (var item : assets.choices(category, next.gender(), true)) {
                if (item.id == next.items()[category]) {
                    items[category] = item;
                    break;
                }
            }
        }
        sprites.values().forEach(sprite -> glDeleteTextures(sprite.texture()));
        sprites.clear();
    }

    @Override
    public int region() {
        return appearance.region();
    }

    @Override
    public FireRedAtlas atlas() {
        return null;
    }

    @Override
    public Position position() {
        return position;
    }

    @Override
    public String locationName() {
        try {
            return ClientStrings.get(
                    140000 + (region() == 5 ? 4 : region()) * 1000 + maps.header(id()).name());
        } catch (IllegalArgumentException exception) {
            return ClientStrings.get(250000 + region());
        }
    }

    @Override
    public int speedMultiplier() {
        return multiplier;
    }

    @Override
    public boolean noclip() {
        return noclip;
    }

    @Override
    public void setNoclip(boolean enabled) {
        noclip = enabled;
        syncCycling();
    }

    @Override
    public void setFollower(PartyMember member) {
        followerFailure = member != null;
    }

    @Override
    public boolean takeFollowerFailure() {
        boolean value = followerFailure;
        followerFailure = false;
        return value;
    }

    @Override
    public void setSpeedMultiplier(int multiplier) {
        if (multiplier != 1 && multiplier != 2 && multiplier != 4 && multiplier != 8)
            throw new IllegalArgumentException("Invalid speed multiplier");
        this.multiplier = multiplier;
        syncCycling();
    }

    private int id() {
        return position.group() * 256 + position.map();
    }

    @Override
    public void restore(Position position) throws IOException {
        if (!position.valid(region())) throw new IOException("Invalid NDS position");
        this.position = previous = position;
        npcStates.clear();
        conversing = false;
        nextNpcPoll = 0;
        stepping = false;
        prepare();
    }

    private void prepare() throws IOException {
        var header = rules.header(id());
        var matrix = rules.matrix(id());
        int column = position.x() / header.tileWidth(), row = position.y() / header.tileHeight();
        for (int y = Math.max(0, row - 1); y <= Math.min(matrix.height() - 1, row + 1); y++) {
            for (int x = Math.max(0, column - 1);
                    x <= Math.min(matrix.width() - 1, column + 1);
                    x++) {
                int cell = y * matrix.width() + x, chunk = matrix.chunks()[cell];
                if (chunk < 0) continue;
                int owner = matrix.headers() == null ? id() : matrix.headers()[cell];
                if (owner < 0 || owner >= maps.headers().size()) owner = id();
                SceneKey key = new SceneKey(owner, chunk);
                if (!scenes.containsKey(key))
                    scenes.put(key, new BlackWhiteMapPreview(maps, owner, chunk));
            }
        }
        while (scenes.size() > 24) {
            var iterator = scenes.entrySet().iterator();
            var oldest = iterator.next();
            oldest.getValue().close();
            iterator.remove();
        }
    }

    @Override
    public void setAuthority(Function<byte[], CompletableFuture<WorldPackets.Result>> authority) {
        this.authority = authority;
        if (authority != null) reply = authority.apply(new byte[] {WorldPackets.LOADED});
    }

    @Override
    public void beginEntry(long now) {
        clearKeys();
        fadeStarted = now;
        entering = true;
    }

    @Override
    public void clearKeys() {
        Arrays.fill(held, false);
        Arrays.fill(toggles, false);
    }

    @Override
    public void toggleBicycle() {
        if (transferring || entering) return;
        cycling = !cycling;
        syncCycling();
    }

    @Override
    public void restoreCycling(boolean cycling) {
        this.cycling = cycling;
    }

    private void syncCycling() {
        if (authority != null) {
            sentSettings = WorldPackets.settings(cycling, multiplier, noclip);
            authority.apply(sentSettings);
        }
    }

    @Override
    public void key(int key, boolean pressed) {
        int direction =
                switch (key) {
                    case GLFW.GLFW_KEY_S -> 0;
                    case GLFW.GLFW_KEY_W -> 1;
                    case GLFW.GLFW_KEY_A -> 2;
                    case GLFW.GLFW_KEY_D -> 3;
                    default -> -1;
                };
        if (direction >= 0) {
            held[direction] = pressed;
            if (pressed) lastPressed = direction;
            return;
        }
        int toggle = key == GLFW.GLFW_KEY_K ? 0 : key == GLFW.GLFW_KEY_2 ? 1 : -1;
        if (toggle >= 0) {
            if (pressed && !toggles[toggle]) {
                if (toggle == 0) running = !running;
                else toggleBicycle();
            }
            toggles[toggle] = pressed;
        }
    }

    @Override
    public void teleport(int group, int map, int x, int y, long now) throws IOException {
        if (reply != null || transferring || entering || stepping) return;
        clearKeys();
        if (authority == null) {
            pendingTransfer = rules.at(group, map, x, y, 0, null);
            transferStarted = now;
            transferring = true;
        } else
            reply =
                    authority.apply(
                            new PacketData(7)
                                    .putByte(WorldPackets.TELEPORT)
                                    .putByte(group)
                                    .putByte(map)
                                    .putShort(x)
                                    .putShort(y)
                                    .written());
    }

    private void accept(WorldPackets.Result result, long now) throws IOException {
        if (result.encounter() != null) {
            wildEncounter = result.encounter();
            clearKeys();
        }
        if (result.warp()) {
            pendingTransfer = result.position();
            transferring = true;
            transferStarted = now;
            clearKeys();
            stepping = false;
        } else if (result.moved()) {
            previous = position;
            position = result.position();
            stepStarted = now;
            stepNanos = result.millis() * 1_000_000L;
            stepping = true;
        } else position = result.position();
        nextRequest = now + result.millis() * 1_000_000L;
    }

    private void update(long now) throws IOException {
        if (entering && now - fadeStarted >= 500_000_000L) entering = false;
        if (transferring) {
            if (pendingTransfer != null && now - transferStarted >= 500_000_000L) {
                restore(pendingTransfer);
                pendingTransfer = null;
                clearKeys();
                if (cycling && maps.indoor(id())) {
                    cycling = false;
                    syncCycling();
                }
                transferStarted = System.nanoTime();
                fadeStarted = transferStarted;
                entering = true;
                transferring = false;
                if (authority != null) reply = authority.apply(new byte[] {WorldPackets.LOADED});
            }
            return;
        }
        if (stepping && now - stepStarted >= stepNanos) {
            stepping = false;
            previous = position;
        }
        if (reply != null && reply.isDone()) {
            if (reply.isCompletedExceptionally()) {
                reply = null;
                clearKeys();
                return;
            }
            var result = reply.join();
            reply = null;
            accept(result, now);
        }
        if (reply != null
                || entering
                || stepping
                || wildEncounter != null
                || conversing
                || transferring
                || now < nextRequest) return;
        int direction = held[lastPressed] ? lastPressed : -1;
        if (direction < 0)
            for (int i = 0; i < held.length; i++)
                if (held[i]) {
                    direction = i;
                    break;
                }
        if (direction < 0) return;
        if (authority == null) {
            var result = rules.move(position, direction, noclip);
            if (!noclip && result.moved() && localNpcs.occupied(result.position(), now))
                result =
                        new FireRedWorldRules.Decision(
                                new Position(
                                        position.group(),
                                        position.map(),
                                        position.x(),
                                        position.y(),
                                        position.elevation(),
                                        direction),
                                false,
                                false);
            accept(
                    new WorldPackets.Result(
                            result.position(),
                            result.moved(),
                            result.warp(),
                            Math.max(1, (cycling ? 80 : running ? 135 : 275) / multiplier),
                            null),
                    now);
        } else {
            byte[] settings = WorldPackets.settings(cycling, multiplier, noclip);
            byte[] command = WorldPackets.move(position.x(), position.y(), direction, running);
            if (Arrays.equals(settings, sentSettings)) reply = authority.apply(command);
            else {
                sentSettings = settings;
                reply = authority.apply(settings).thenCompose(ignored -> authority.apply(command));
            }
        }
    }

    @Override
    public void render(int width, int height, long now) throws IOException {
        updateNpcs(now);
        update(now);
        prepare();
        float progress = stepping ? Math.min(1, (float) (now - stepStarted) / stepNanos) : 1;
        float x = previous.x() + (position.x() - previous.x()) * progress;
        float y = previous.y() + (position.y() - previous.y()) * progress;
        var header = rules.header(id());
        var matrix = rules.matrix(id());
        float oldHeight =
                rules.height(
                        previous.group() * 256 + previous.map(),
                        previous.x(),
                        previous.y(),
                        previous.elevation());
        float newHeight = rules.height(id(), position.x(), position.y(), position.elevation());
        float ground = (oldHeight + (newHeight - oldHeight) * progress) / 4;
        float focusX = (x - header.tileWidth() * 0.5f + 0.5f) / 4;
        float focusZ = (y - header.tileHeight() * 0.5f + 0.5f) / 4;
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.position.set(focusX, ground + 3.8f, focusZ + 4.2f);
        camera.up.set(0, 1, 0);
        camera.lookAt(focusX, ground, focusZ);
        camera.update();
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadMatrixf(camera.projection.val);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadMatrixf(camera.view.val);
        try {
            glDisable(GL_SCISSOR_TEST);
            glClearColor(0, 0, 0, 1);
            glDepthMask(true);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LEQUAL);
            glDisable(GL_LIGHTING);
            glFrontFace(GL_CCW);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(GL_GREATER, 0.01f);
            int column = position.x() / header.tileWidth(),
                    row = position.y() / header.tileHeight();
            for (boolean translucent : new boolean[] {false, true}) {
                for (int dy = Math.max(0, row - 1);
                        dy <= Math.min(matrix.height() - 1, row + 1);
                        dy++) {
                    for (int dx = Math.max(0, column - 1);
                            dx <= Math.min(matrix.width() - 1, column + 1);
                            dx++) {
                        int cell = dy * matrix.width() + dx, chunk = matrix.chunks()[cell];
                        if (chunk < 0) continue;
                        int owner = matrix.headers() == null ? id() : matrix.headers()[cell];
                        if (owner < 0 || owner >= maps.headers().size()) owner = id();
                        var scene = scenes.get(new SceneKey(owner, chunk));
                        glPushMatrix();
                        glTranslatef(
                                dx * header.tileWidth() / 4f,
                                matrix.offsets()[cell],
                                dy * header.tileHeight() / 4f);
                        scene.draw(now / 1_000_000_000f * 30, translucent);
                        glPopMatrix();
                    }
                }
                if (!translucent) {
                    drawNpcs(header.tileWidth(), header.tileHeight(), now);
                    drawAvatar(focusX, ground, focusZ, now);
                }
            }
        } finally {
            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glPopAttrib();
            glMatrixMode(GL_MODELVIEW);
        }
    }

    @Override
    public void setNpcAuthority(
            Function<byte[], CompletableFuture<NpcPackets.Snapshot>> authority) {
        npcAuthority = authority;
    }

    private void applyNpcs(NpcPackets.Snapshot snapshot, long now) {
        if (snapshot.group() * 256 + snapshot.map() != id()) return;
        npcStates.clear();
        for (var state : snapshot.states()) npcStates.put(state.id(), state);
        npcClock = now;
    }

    private void updateNpcs(long now) {
        if (npcReply != null && npcReply.isDone()) {
            if (!npcReply.isCompletedExceptionally() && !npcReply.isCancelled())
                applyNpcs(npcReply.join(), now);
            npcReply = null;
        }
        if (transferring || entering) return;
        if (npcAuthority == null) {
            localNpcs.update(position, stepping ? previous : null, now);
            applyNpcs(localNpcs.snapshot(position, now), now);
        } else if (npcReply == null && now >= nextNpcPoll) {
            npcReply = npcAuthority.apply(NpcPackets.request(0, 0));
            nextNpcPoll = now + 100_000_000L;
        }
    }

    private NdsNpcs.Definition facingNpc() {
        int x = position.x() + (position.facing() == 2 ? -1 : position.facing() == 3 ? 1 : 0);
        int y = position.y() + (position.facing() == 1 ? -1 : position.facing() == 0 ? 1 : 0);
        for (var npc : NdsNpcs.definitions(region(), id())) {
            var state = npcStates.get(npc.id());
            if ((state == null ? npc.x() : state.x()) == x
                    && (state == null ? npc.y() : state.y()) == y
                    && npc.elevation() == position.elevation()) return npc;
        }
        return null;
    }

    @Override
    public boolean hasNpcInFront() {
        return !stepping && !entering && !transferring && facingNpc() != null;
    }

    @Override
    public CompletableFuture<List<Integer>> interactNpc() {
        if (stepping || entering || transferring || reply != null || conversing)
            return CompletableFuture.completedFuture(List.of());
        var npc = facingNpc();
        if (npc == null) return CompletableFuture.completedFuture(List.of());
        clearKeys();
        conversing = true;
        CompletableFuture<NpcPackets.Snapshot> response;
        if (npcAuthority != null) response = npcAuthority.apply(NpcPackets.request(1, npc.id()));
        else {
            long now = System.nanoTime();
            localNpcs.talk(position, npc.id(), now);
            response = CompletableFuture.completedFuture(localNpcs.snapshot(position, now));
        }
        return response.thenApply(
                snapshot ->
                        snapshot.talking() == npc.id() && snapshot.dialogueText() > 0
                                ? List.of(snapshot.dialogueText())
                                : List.of());
    }

    @Override
    public void endNpcConversation() {
        conversing = false;
        clearKeys();
        if (npcAuthority != null) npcAuthority.apply(NpcPackets.request(2, 255));
        else localNpcs.end(System.nanoTime());
    }

    private void drawNpcs(int tileWidth, int tileHeight, long now) {
        for (var npc : NdsNpcs.definitions(region(), id())) {
            var state = npcStates.get(npc.id());
            int facing = state == null ? npc.facing() : state.facing();
            float progress =
                    state == null || state.remaining() == 0
                            ? 1
                            : Math.clamp(
                                    1 - state.remaining() / 275f + (now - npcClock) / 275_000_000f,
                                    0,
                                    1);
            float x =
                    state == null
                            ? npc.x()
                            : state.fromX() + (state.x() - state.fromX()) * progress;
            float y =
                    state == null
                            ? npc.y()
                            : state.fromY() + (state.y() - state.fromY()) * progress;
            if (Math.abs(x - position.x()) > 40
                    || Math.abs(y - position.y()) > 40
                    || missingNpcSprites.contains(npc.graphics())) continue;
            int walking = progress < 1 ? (int) (now / 160_000_000L % 4) : 0;
            long key = (long) npc.graphics() << 8 | facing << 4 | walking;
            try {
                NpcSprite sprite = npcSprites.get(key);
                if (sprite == null) {
                    var image = npcImages.image(npc.graphics(), facing, walking);
                    if (image == null) {
                        missingNpcSprites.add(npc.graphics());
                        continue;
                    }
                    var buffer =
                            BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
                    int foot = 1;
                    for (int py = 0; py < image.getHeight(); py++)
                        for (int px = 0; px < image.getWidth(); px++) {
                            int color = image.getRGB(px, py);
                            if ((color >>> 24) != 0) foot = Math.max(foot, py + 1);
                            buffer.put((byte) (color >> 16))
                                    .put((byte) (color >> 8))
                                    .put((byte) color)
                                    .put((byte) (color >> 24));
                        }
                    buffer.flip();
                    int texture = glGenTextures();
                    glBindTexture(GL_TEXTURE_2D, texture);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                    glTexImage2D(
                            GL_TEXTURE_2D,
                            0,
                            GL_RGBA8,
                            image.getWidth(),
                            image.getHeight(),
                            0,
                            GL_RGBA,
                            GL_UNSIGNED_BYTE,
                            buffer);
                    sprite = new NpcSprite(texture, image.getWidth(), image.getHeight(), foot);
                    npcSprites.put(key, sprite);
                    if (npcSprites.size() > 128) {
                        var first = npcSprites.entrySet().iterator();
                        glDeleteTextures(first.next().getValue().texture());
                        first.remove();
                    }
                }
                float ground =
                        rules.height(id(), Math.round(x), Math.round(y), npc.elevation()) / 4;
                glPushMatrix();
                try {
                    glTranslatef(
                            (x - tileWidth * .5f + .5f) / 4,
                            ground + .02f,
                            (y - tileHeight * .5f + .5f) / 4);
                    glRotatef((float) Math.toDegrees(Math.atan2(4.2, 3.8)) - 90, 1, 0, 0);
                    glDisable(GL_CULL_FACE);
                    glEnable(GL_TEXTURE_2D);
                    glBindTexture(GL_TEXTURE_2D, sprite.texture());
                    glColor4f(1, 1, 1, 1);
                    float left = -sprite.width() / 128f,
                            right = -left,
                            top = sprite.foot() / 64f,
                            bottom = top - sprite.height() / 64f;
                    glBegin(GL_QUADS);
                    glTexCoord2f(0, 0);
                    glVertex3f(left, top, 0);
                    glTexCoord2f(1, 0);
                    glVertex3f(right, top, 0);
                    glTexCoord2f(1, 1);
                    glVertex3f(right, bottom, 0);
                    glTexCoord2f(0, 1);
                    glVertex3f(left, bottom, 0);
                    glEnd();
                } finally {
                    glPopMatrix();
                }
            } catch (IOException | IllegalArgumentException error) {
                missingNpcSprites.add(npc.graphics());
                System.getLogger(getClass().getName())
                        .log(
                                System.Logger.Level.WARNING,
                                "Unable to draw NDS NPC " + npc.graphics(),
                                error);
            }
        }
    }

    private void drawAvatar(float x, float y, float z, long now) throws IOException {
        int direction = position.facing();
        int motion = cycling ? 2 : running ? 1 : 0;
        int row = motion * 3 + (direction < 2 ? direction : 2);
        int frame =
                FRAMES[row][
                        stepping
                                ? (int)
                                        ((now
                                                        / 1_000_000
                                                        * multiplier
                                                        / (running && !cycling ? 140 : 160))
                                                % 4)
                                : 0];
        int key = frame * 4 + direction;
        Sprite sprite = sprites.get(key);
        if (sprite == null) {
            var image =
                    assets.renderOverworld(
                            appearance.skin(), items, appearance.colors(), frame, direction);
            var buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
            for (int py = 0; py < image.getHeight(); py++)
                for (int px = 0; px < image.getWidth(); px++) {
                    int color = image.getRGB(px, py);
                    buffer.put((byte) (color >> 16))
                            .put((byte) (color >> 8))
                            .put((byte) color)
                            .put((byte) (color >> 24));
                }
            buffer.flip();
            int texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    image.getWidth(),
                    image.getHeight(),
                    0,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    buffer);
            sprite = new Sprite(texture, image.getWidth(), image.getHeight());
            sprites.put(key, sprite);
        }
        glPushMatrix();
        glTranslatef(x, y + 0.02f, z);
        glRotatef((float) Math.toDegrees(Math.atan2(4.2, 3.8)) - 90, 1, 0, 0);
        glDisable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, sprite.texture());
        glColor4f(1, 1, 1, 1);
        float left = -21.5f / 64, top = 36f / 64, right = left + sprite.width() * 0.75f / 64;
        float bottom = top - sprite.height() * 0.75f / 64;
        glBegin(GL_QUADS);
        float u0 = direction == 3 ? 1 : 0, u1 = 1 - u0;
        glTexCoord2f(u0, 0);
        glVertex3f(left, top, 0);
        glTexCoord2f(u1, 0);
        glVertex3f(right, top, 0);
        glTexCoord2f(u1, 1);
        glVertex3f(right, bottom, 0);
        glTexCoord2f(u0, 1);
        glVertex3f(left, bottom, 0);
        glEnd();
        glPopMatrix();
    }

    @Override
    public void renderEntryFade(int width, int height, long now) {
        float alpha =
                transferring
                        ? Math.min(1, (now - transferStarted) / 500_000_000f)
                        : entering
                                ? 1 - Math.min(1, Math.max(0, now - fadeStarted) / 500_000_000f)
                                : 0;
        if (alpha <= 0) return;
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_SCISSOR_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(0, 0, 0, alpha);
        glBegin(GL_QUADS);
        glVertex2f(0, 0);
        glVertex2f(width, 0);
        glVertex2f(width, height);
        glVertex2f(0, height);
        glEnd();
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glPopAttrib();
        glMatrixMode(GL_MODELVIEW);
    }

    @Override
    public void close() {
        if (npcReply != null) npcReply.cancel(false);
        npcReply = null;
        npcSprites.values().forEach(sprite -> glDeleteTextures(sprite.texture()));
        npcSprites.clear();
        npcStates.clear();
        clearKeys();
        scenes.values().forEach(BlackWhiteMapPreview::close);
        scenes.clear();
        sprites.values().forEach(sprite -> glDeleteTextures(sprite.texture()));
        sprites.clear();
        authority = null;
        reply = null;
    }
}
