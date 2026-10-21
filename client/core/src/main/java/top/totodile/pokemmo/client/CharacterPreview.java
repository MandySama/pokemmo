package top.totodile.pokemmo.client;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import static top.totodile.pokemmo.client.CharacterAppearance.Slot.*;

/** 沿用参考的三层合成、五种预览状态和动画帧序列。纹理按实际访问帧加载。 */
final class CharacterPreview extends Widget implements AutoCloseable {
    private static final int[][] FRAMES = {
            {0,3,0,4}, {1,5,1,6}, {2,7,2,8},
            {9,10,9,11}, {12,13,12,14}, {15,16,15,17},
            {18,21,18,22}, {19,23,19,24}, {20,25,20,26}
    };
    private static final int[] BACK_FRAMES = {0,0,0,0,0,0,0,0,0,1,2,3,4,4,4,4,4,0,0,0};
    private static final int[] GROUP_LAYERS = {3,0,6};
    private static final int[] LAYER_OFFSETS = {0,2,1};
    private static final Set<Integer> HAND_FRAMES = Set.of(1,5,6,12,13,14,19,23,24);
    private final CharacterAppearance appearance;
    private final GdxTwlRenderer renderer;
    private final boolean mobile;
    private final IntSupplier skin, gender;
    private final LongSupplier clock;
    private final Map<Key,Cached> cache = new HashMap<>();
    private final Set<Key> loading = new HashSet<>();
    private int mode;
    private int centerX;
    private boolean portrait;
    private CharacterAppearance.Slot colorSlot;
    private int pendingColor;
    private record Key(int slot, int id, int view, int frame, int layer) {}
    private static final class Cached {
        final Pixmap pixels;
        final Texture texture;
        final Image image;
        final Map<Integer,Image> colors = new HashMap<>();
        Cached(Pixmap pixels, Texture texture, Image image) {
            this.pixels = pixels; this.texture = texture; this.image = image;
        }
    }

    CharacterPreview(CharacterAppearance appearance, GdxTwlRenderer renderer, boolean mobile,
                     IntSupplier skin, IntSupplier gender, LongSupplier clock) {
        this.appearance = appearance;
        this.renderer = renderer;
        this.mobile = mobile;
        this.skin = skin;
        this.gender = gender;
        this.clock = clock;
        setTheme("");
        setCanAcceptKeyboardFocus(false);
    }
    void reset() { mode = 0; colorSlot = null; }
    int mode() { return mode; }
    void turn(int delta) { mode = Math.floorMod(mode + delta, 5); }
    void color(CharacterAppearance.Slot slot, int color) { colorSlot = slot; pendingColor = color; }
    boolean coloring() { return colorSlot != null; }
    void centerBetween(int left, int right) { centerX = (left + right) / 2; }
    void portrait() { portrait = true; }

    @Override protected boolean handleEvent(Event event) { return false; }
    @Override protected boolean isMouseInside(Event event) { return false; }
    @Override protected void paintWidget(GUI gui) {
        if (portrait) {
            drawCharacter(2, gender.getAsInt(), getX(), getY(), mobile ? 2 : 1);
            return;
        }
        int scale = mode < 3 ? (mobile ? 5 : 4) : (mobile ? 4 : 3);
        int view = mode == 3 ? 2 : mode == 4 ? 1 : 0;
        long time = clock.getAsLong();
        for (int direction = 0; direction < 3; direction++) {
            if (mode >= 3 && direction != 1) continue;
            int frame = mode < 3 ? FRAMES[mode * 3 + direction][(int)(time / 160 % 4)]
                    : mode == 3 ? gender.getAsInt() : BACK_FRAMES[(int)(time / 150 % BACK_FRAMES.length)];
            Cached body = load(new Key(-1, skin.getAsInt(), view, frame, 0));
            if (body == null) continue;
            int x = centerX - getX() - body.image.getWidth() * scale / 2;
            int y;
            if (mobile) {
                x += (direction - 1) * (coloring() ? 220 : 200);
                y = 270;
            } else if (coloring()) {
                x += (direction - 1) * 160;
                y = 250;
            } else {
                y = 120 + direction * 160;
            }
            drawCharacter(view, frame, getX() + x, getY() + y, scale);
        }
    }

    private void drawCharacter(int view, int frame, int x, int y, int scale) {
        for (int group = 0; group < 3; group++) {
            if (group == 1) draw(new Key(-1, skin.getAsInt(), view, frame, 0), x, y, scale, null);
            part(BICYCLE, view, frame, group, x, y, scale);
            int topFlags = appearance.selected(TOP).flags();
            if ((topFlags & 4096) == 0) part(SHOES, view, frame, group, x, y, scale);
            if ((topFlags & 2048) == 0) part(PANTS, view, frame, group, x, y, scale);
            boolean handsFirst = appearance.selected(GLOVES).id() == 1 || HAND_FRAMES.contains(frame);
            if (handsFirst) part(GLOVES, view, frame, group, x, y, scale);
            part(TOP, view, frame, group, x, y, scale);
            part(BACK, view, frame, group, x, y, scale);
            part(FACE, view, frame, group, x, y, scale);
            part(EYES, view, frame, group, x, y, scale);
            part(HAIR, view, frame, group, x, y, scale);
            part(HAT, view, frame, group, x, y, scale);
            if (!handsFirst) part(GLOVES, view, frame, group, x, y, scale);
        }
    }
    private void part(CharacterAppearance.Slot slot, int view, int frame, int group, int x, int y, int scale) {
        var entry = appearance.selected(slot);
        if (entry.id() < 0) return;
        int layer = GROUP_LAYERS[group];
        int color = slot == colorSlot ? pendingColor : appearance.color(slot);
        Integer tint = entry.colorable() && color >= 0 ? AppearancePalette.tint(color) : null;
        for (int offset : LAYER_OFFSETS) {
            Key key = new Key(slot.id, entry.id(), view, frame, layer + offset);
            draw(key, x, y, scale, offset == 2 ? null : tint);
        }
    }
    private void draw(Key key, int x, int y, int scale, Integer tint) {
        Cached sprite = load(key);
        if (sprite == null) return;
        int width = sprite.image.getWidth() * scale, height = sprite.image.getHeight() * scale;
        sprite.image.draw(getAnimationState(), x, y, width, height);
        if (key.slot >= 0 && key.layer % 3 != 2) {
            Image colored = tint == null ? sprite.image : sprite.colors.computeIfAbsent(tint,
                    value -> sprite.image.createTintedVersion(new Color(value)));
            colored.draw(getAnimationState(), x, y, width, height);
        }
    }
    private Cached load(Key key) {
        if (cache.containsKey(key)) return cache.get(key);
        CharacterAppearance.Frame frame = appearance.frame(key.slot, key.id, key.view, key.frame, key.layer);
        if (frame == null || frame.length() == 0 && frame.source() < 0) {
            cache.put(key, null); return null;
        }
        if (!loading.add(key)) throw new IllegalArgumentException("Cyclic addon sprite reference");
        Pixmap pixels = null;
        try {
            if (frame.source() >= 0) {
                Cached source = load(new Key(key.slot, key.id, key.view, frame.source(), key.layer));
                if (source == null) { cache.put(key, null); return null; }
                pixels = new Pixmap(source.pixels.getWidth(), source.pixels.getHeight(), Pixmap.Format.RGBA8888);
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
            Cached sprite = new Cached(pixels, texture, renderer.image(texture));
            cache.put(key, sprite);
            return sprite;
        } catch (RuntimeException error) {
            if (pixels != null) pixels.dispose();
            throw error;
        } finally { loading.remove(key); }
    }
    @Override public void close() {
        for (Cached sprite : cache.values()) if (sprite != null) {
            sprite.texture.dispose(); sprite.pixels.dispose();
        }
        cache.clear();
    }
}
