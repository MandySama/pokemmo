package top.totodile.pokemmo.client.ui.character;

import static top.totodile.pokemmo.client.ui.character.CharacterAppearance.Slot.*;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

/** 沿用参考的三层合成、五种预览状态和动画帧序列。纹理按实际访问帧加载。 */
public final class CharacterPreview extends Widget implements AutoCloseable {
    private static final int[][] FRAMES = {
        {0, 3, 0, 4}, {1, 5, 1, 6}, {2, 7, 2, 8},
        {9, 10, 9, 11}, {12, 13, 12, 14}, {15, 16, 15, 17},
        {18, 21, 18, 22}, {19, 23, 19, 24}, {20, 25, 20, 26}
    };
    private static final int[] BACK_FRAMES = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 4, 4, 4, 4, 0, 0, 0
    };
    private static final int[] GROUP_LAYERS = {3, 0, 6};
    private static final int[] LAYER_OFFSETS = {0, 2, 1};
    private static final Set<Integer> HAND_FRAMES = Set.of(1, 5, 6, 12, 13, 14, 19, 23, 24);
    private final CharacterAppearance appearance;
    private final GdxTwlRenderer renderer;
    private final boolean mobile;
    private final IntSupplier skin, gender;
    private final LongSupplier clock;
    private final Map<Key, Cached> cache = new HashMap<>();
    private final Set<Key> loading = new HashSet<>();
    private int mode;
    private int centerX;
    private boolean portrait;
    private boolean customization;

    void customization(boolean value) {
        customization = value;
    }

    private boolean tooltipRow;
    private boolean measuringTooltip;
    private int tooltipMinX, tooltipMinY, tooltipMaxX, tooltipMaxY;
    private boolean tooltipMeasured;
    private int portraitScale;
    private com.badlogic.gdx.graphics.g2d.SpriteBatch worldBatch;
    private boolean worldFlip;
    // GdxAddonSpriteManager.eI：地图中的所有 addon 层统一使用 0.75 倍。
    private static final float WORLD_SCALE = .75f;
    private float worldHeight, worldX, worldY;
    private int worldGroundPadding = -1;
    private CharacterAppearance.Slot colorSlot;
    private int pendingColor;

    private record Key(int slot, int id, int view, int frame, int layer, int variant) {
        Key(int slot, int id, int view, int frame, int layer) {
            this(slot, id, view, frame, layer, 0);
        }
    }

    private static final class Cached {
        final Pixmap pixels;
        final Texture texture;
        final Image image;
        final Map<Integer, Image> colors = new HashMap<>();
        int[] inkBounds;

        Cached(Pixmap pixels, Texture texture, Image image) {
            this.pixels = pixels;
            this.texture = texture;
            this.image = image;
        }
    }

    public CharacterPreview(
            CharacterAppearance appearance,
            GdxTwlRenderer renderer,
            boolean mobile,
            IntSupplier skin,
            IntSupplier gender,
            LongSupplier clock) {
        this.appearance = appearance;
        this.renderer = renderer;
        this.mobile = mobile;
        this.skin = skin;
        this.gender = gender;
        this.clock = clock;
        setTheme("");
        setCanAcceptKeyboardFocus(false);
    }

    void reset() {
        mode = 0;
        colorSlot = null;
    }

    int mode() {
        return mode;
    }

    void turn(int delta) {
        mode = Math.floorMod(mode + delta, 5);
    }

    void color(CharacterAppearance.Slot slot, int color) {
        colorSlot = slot;
        pendingColor = color;
    }

    boolean coloring() {
        return colorSlot != null;
    }

    // 保存相对坐标，父窗口移动时预览中心随控件一起移动。
    void centerBetween(int left, int right) {
        centerX = (left + right) / 2 - getX();
    }

    public void portrait() {
        portrait = true;
    }

    /** GdxAddonSpriteManager.Rc0: the five saved-outfit backsprite throwing poses. */
    public void drawBattleBack(int frame, int x, int y, int scale) {
        drawCharacter(1, Math.max(0, Math.min(4, frame)), x, y, scale);
    }

    public void tooltipRow() {
        tooltipRow = true;
    }

    private void measureTooltip() {
        if (tooltipMeasured) return;
        tooltipMinX = tooltipMinY = Integer.MAX_VALUE;
        tooltipMaxX = tooltipMaxY = Integer.MIN_VALUE;
        measuringTooltip = true;
        try {
            for (int direction = 0; direction < 3; direction++)
                drawCharacter(0, direction, direction * 84, 0, 3);
        } finally {
            measuringTooltip = false;
        }
        if (tooltipMinX == Integer.MAX_VALUE) {
            tooltipMinX = tooltipMinY = 0;
            tooltipMaxX = 251;
            tooltipMaxY = 107;
        }
        tooltipMeasured = true;
    }

    @Override
    public int getPreferredInnerWidth() {
        if (!tooltipRow) return super.getPreferredInnerWidth();
        measureTooltip();
        return Math.max(324, tooltipMaxX - tooltipMinX + 21);
    }

    @Override
    public int getPreferredInnerHeight() {
        if (!tooltipRow) return super.getPreferredInnerHeight();
        measureTooltip();
        return tooltipMaxY - tooltipMinY + 21;
    }

    /** TrainerCardWindow's PlayerAvatarAppearanceState.CF0(2), independent of UI platform. */
    public void portrait(int scale) {
        portrait = true;
        portraitScale = scale;
    }

    void drawWorld(
            com.badlogic.gdx.graphics.g2d.SpriteBatch batch,
            int x,
            int y,
            int frame,
            boolean flip) {
        Cached body = load(new Key(-1, skin.getAsInt(), 0, frame, 0));
        if (body == null) return;
        if (worldGroundPadding < 0) {
            Cached standing = load(new Key(-1, skin.getAsInt(), 0, 0, 0));
            worldGroundPadding = 0;
            if (standing != null) {
                int bottom = -1;
                for (int py = 0; py < standing.pixels.getHeight(); py++)
                    for (int px = 0; px < standing.pixels.getWidth(); px++)
                        if ((standing.pixels.getPixel(px, py) & 255) != 0) bottom = py;
                worldGroundPadding = bottom < 0 ? 0 : standing.pixels.getHeight() - bottom - 1;
            }
        }
        worldBatch = batch;
        worldFlip = flip;
        worldHeight = body.pixels.getHeight() * WORLD_SCALE;
        worldX = x - body.pixels.getWidth() * WORLD_SCALE / 2;
        worldY = y - worldGroundPadding * WORLD_SCALE;
        try {
            drawCharacter(0, frame, 0, 0, 1);
        } finally {
            worldBatch = null;
            batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }
    }

    @Override
    protected boolean handleEvent(Event event) {
        return false;
    }

    @Override
    protected boolean isMouseInside(Event event) {
        return false;
    }

    @Override
    protected void paintWidget(GUI gui) {
        if (tooltipRow) {
            measureTooltip();
            long time = clock.getAsLong();
            int
                    originX =
                            getInnerX()
                                    + (getInnerWidth() - (tooltipMaxX - tooltipMinX + 1)) / 2
                                    - tooltipMinX,
                    originY = getInnerY() + 10 - tooltipMinY;
            for (int direction = 0; direction < 3; direction++) {
                int frame = FRAMES[direction][(int) (time / 160 % 4)];
                drawCharacter(0, frame, originX + direction * 84, originY, 3);
            }
            return;
        }
        if (portrait) {
            drawCharacter(
                    2,
                    gender.getAsInt(),
                    getX(),
                    getY(),
                    portraitScale > 0 ? portraitScale : mobile ? 2 : 1);
            return;
        }
        int scale = mode < 3 ? (mobile ? 5 : 4) : (mobile ? 4 : 3);
        int view = mode == 3 ? 2 : mode == 4 ? 1 : 0;
        long time = clock.getAsLong();
        for (int direction = 0; direction < 3; direction++) {
            if (mode >= 3 && direction != 1) continue;
            int frame =
                    mode < 3
                            ? FRAMES[mode * 3 + direction][(int) (time / 160 % 4)]
                            : mode == 3
                                    ? gender.getAsInt()
                                    : BACK_FRAMES[(int) (time / 150 % BACK_FRAMES.length)];
            Cached body = load(new Key(-1, skin.getAsInt(), view, frame, 0));
            if (body == null) continue;
            int x = centerX - body.image.getWidth() * scale / 2;
            int y;
            if (mobile) {
                x += (direction - 1) * (coloring() ? 220 : 200);
                y = 270;
            } else if (coloring()) {
                x += (direction - 1) * 160;
                y = 250;
            } else {
                y = (customization ? 10 : 120) + direction * 160;
            }
            drawCharacter(view, frame, getX() + x, getY() + y, scale);
        }
    }

    private void drawCharacter(int view, int frame, int x, int y, int scale) {
        int bicycleFrame = frame, bicycle = appearance.selected(BICYCLE).id();
        boolean riding = view == 0 && frame >= 18 && frame <= 26;
        if (riding) {
            int direction =
                    frame == 18 || frame == 21 || frame == 22
                            ? 0
                            : frame == 19 || frame == 23 || frame == 24 ? 1 : 2;
            if (bicycle == 2
                    || bicycle == 12
                    || bicycle == 17
                    || bicycle == 28
                    || bicycle == 30
                    || bicycle == 40
                    || bicycle == 51) {
                frame = 18 + direction;
                if (bicycle == 17 || bicycle == 40) bicycleFrame = frame;
            } else if (bicycle == 52) {
                frame = 27 + direction;
                bicycleFrame = 18 + direction;
            }
        }
        boolean layered =
                riding && (bicycle == 12 || bicycle == 17 || bicycle == 40 || bicycle == 51);
        if (layered) bicycleEffect(bicycle, bicycleFrame, false, view, x, y, scale);
        for (int group = 0; group < 3; group++) {
            if (group == 1) draw(new Key(-1, skin.getAsInt(), view, frame, 0), x, y, scale, null);
            if (!layered) part(BICYCLE, view, bicycleFrame, group, x, y, scale);
            part(ROD, view, frame, group, x, y, scale);
            int topFlags = appearance.selected(TOP).flags();
            if ((topFlags & 4096) == 0) part(SHOES, view, frame, group, x, y, scale);
            if ((topFlags & 2048) == 0) part(PANTS, view, frame, group, x, y, scale);
            boolean handsFirst =
                    appearance.selected(GLOVES).id() == 1 || HAND_FRAMES.contains(frame);
            if (handsFirst) part(GLOVES, view, frame, group, x, y, scale);
            part(TOP, view, frame, group, x, y, scale);
            part(BACK, view, frame, group, x, y, scale);
            part(FACE, view, frame, group, x, y, scale);
            part(EYES, view, frame, group, x, y, scale);
            part(HAIR, view, frame, group, x, y, scale);
            part(HAT, view, frame, group, x, y, scale);
            part(FOREHEAD, view, frame, group, x, y, scale);
            if (!handsFirst) part(GLOVES, view, frame, group, x, y, scale);
        }
        if (layered) bicycleEffect(bicycle, bicycleFrame, true, view, x, y, scale);
    }

    // PlayerAvatarAppearanceState.Bd: mount body and trail straddle the rider.
    private void bicycleEffect(
            int id, int frame, boolean front, int view, int x, int y, int scale) {
        int phase = (int) (clock.getAsLong() / 200 % 4), step = phase == 3 ? 1 : phase;
        int main = id, trail = 13 + phase;
        if (id == 17) {
            main = 18 + step;
            trail = 23 + step;
        } else if (id == 40) {
            main = 41 + step;
            trail = 46 + step;
        }
        boolean north = frame == 19 || frame == 23 || frame == 24;
        if (!front && !north)
            draw(
                    new Key(
                            11,
                            trail,
                            view,
                            frame,
                            0,
                            appearance.variant(11, trail, clock.getAsLong())),
                    x,
                    y,
                    scale,
                    null);
        int layer = front ? 0 : 2;
        draw(
                new Key(
                        11,
                        main,
                        view,
                        frame,
                        layer,
                        appearance.variant(11, main, clock.getAsLong())),
                x,
                y,
                scale,
                null);
        if (front && north)
            draw(
                    new Key(
                            11,
                            trail,
                            view,
                            frame,
                            0,
                            appearance.variant(11, trail, clock.getAsLong())),
                    x,
                    y,
                    scale,
                    null);
    }

    private void part(
            CharacterAppearance.Slot slot,
            int view,
            int frame,
            int group,
            int x,
            int y,
            int scale) {
        var entry = appearance.selected(slot);
        if (entry.id() < 0) return;
        int layer = GROUP_LAYERS[group];
        int color = slot == colorSlot ? pendingColor : appearance.color(slot);
        Integer tint = entry.colorable() && color >= 0 ? AppearancePalette.tint(color) : null;
        int style = entry.id();
        // PlayerAvatarAppearanceState.HX uses the three Xuanwu loop parts.
        if (slot == BICYCLE && style == 52) {
            int phase = (int) (clock.getAsLong() / 200 % 4);
            style = 54 + (phase == 3 ? 1 : phase);
        }
        for (int offset : LAYER_OFFSETS) {
            Key key =
                    new Key(
                            slot.id,
                            style,
                            view,
                            frame,
                            layer + offset,
                            appearance.variant(slot.id, style, clock.getAsLong()));
            draw(key, x, y, scale, offset == 2 ? null : tint);
        }
    }

    private void draw(Key key, int x, int y, int scale, Integer tint) {
        Cached sprite = load(key);
        if (sprite == null) return;
        if (measuringTooltip) {
            if (sprite.inkBounds == null) {
                int left = sprite.pixels.getWidth(),
                        top = sprite.pixels.getHeight(),
                        right = -1,
                        bottom = -1;
                for (int py = 0; py < sprite.pixels.getHeight(); py++)
                    for (int px = 0; px < sprite.pixels.getWidth(); px++)
                        if ((sprite.pixels.getPixel(px, py) & 255) != 0) {
                            left = Math.min(left, px);
                            right = Math.max(right, px);
                            top = Math.min(top, py);
                            bottom = Math.max(bottom, py);
                        }
                sprite.inkBounds = new int[] {left, top, right, bottom};
            }
            var b = sprite.inkBounds;
            if (b[2] >= b[0]) {
                tooltipMinX = Math.min(tooltipMinX, x + b[0] * scale);
                tooltipMinY = Math.min(tooltipMinY, y + b[1] * scale);
                tooltipMaxX = Math.max(tooltipMaxX, x + (b[2] + 1) * scale - 1);
                tooltipMaxY = Math.max(tooltipMaxY, y + (b[3] + 1) * scale - 1);
            }
            return;
        }
        if (worldBatch != null) {
            float width = sprite.pixels.getWidth() * WORLD_SCALE,
                    height = sprite.pixels.getHeight() * WORLD_SCALE;
            float left = worldFlip ? worldX + width : worldX,
                    bottom = worldY + worldHeight - height;
            worldBatch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
            worldBatch.draw(sprite.texture, left, bottom, worldFlip ? -width : width, height);
            if (key.slot >= 0 && key.layer % 3 != 2) {
                if (tint != null)
                    worldBatch.setColor(
                            ((tint >>> 16) & 255) / 255f,
                            ((tint >>> 8) & 255) / 255f,
                            (tint & 255) / 255f,
                            (tint >>> 24) / 255f);
                worldBatch.draw(sprite.texture, left, bottom, worldFlip ? -width : width, height);
            }
            return;
        }
        int width = sprite.image.getWidth() * scale, height = sprite.image.getHeight() * scale;
        sprite.image.draw(getAnimationState(), x, y, width, height);
        if (key.slot >= 0 && key.layer % 3 != 2) {
            Image colored =
                    tint == null
                            ? sprite.image
                            : sprite.colors.computeIfAbsent(
                                    tint,
                                    value -> sprite.image.createTintedVersion(new Color(value)));
            colored.draw(getAnimationState(), x, y, width, height);
        }
    }

    private Cached load(Key key) {
        if (cache.containsKey(key)) return cache.get(key);
        CharacterAppearance.Frame frame =
                appearance.frame(key.slot, key.id, key.view, key.frame, key.layer, key.variant);
        if (frame == null || frame.length() == 0 && frame.source() < 0) {
            cache.put(key, null);
            return null;
        }
        if (!loading.add(key)) throw new IllegalArgumentException("Cyclic addon sprite reference");
        Pixmap pixels = null;
        try {
            if (frame.source() >= 0) {
                Cached source =
                        load(
                                new Key(
                                        key.slot,
                                        key.id,
                                        key.view,
                                        frame.source(),
                                        key.layer,
                                        key.variant));
                if (source == null) {
                    cache.put(key, null);
                    return null;
                }
                pixels =
                        new Pixmap(
                                source.pixels.getWidth(),
                                source.pixels.getHeight(),
                                Pixmap.Format.RGBA8888);
                pixels.setBlending(Pixmap.Blending.None);
                pixels.drawPixmap(source.pixels, frame.dx(), frame.dy());
            } else {
                pixels = new Pixmap(appearance.archive(), frame.offset(), frame.length());
                if (key.slot >= 0) {
                    pixels.setBlending(Pixmap.Blending.None);
                    for (int y = 0; y < pixels.getHeight(); y++)
                        for (int x = 0; x < pixels.getWidth(); x++)
                            if (pixels.getPixel(x, y) == 0xFF1493FF) pixels.drawPixel(x, y, 0);
                }
            }
            Texture texture = new Texture(pixels);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            Cached sprite =
                    new Cached(pixels, texture, renderer == null ? null : renderer.image(texture));
            cache.put(key, sprite);
            return sprite;
        } catch (RuntimeException error) {
            if (pixels != null) pixels.dispose();
            throw error;
        } finally {
            loading.remove(key);
        }
    }

    @Override
    public void close() {
        for (Cached sprite : cache.values())
            if (sprite != null) {
                sprite.texture.dispose();
                sprite.pixels.dispose();
            }
        cache.clear();
    }
}
