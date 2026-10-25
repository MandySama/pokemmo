package top.totodile.pokemmo.client.ui.pokedex;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokemonSprites;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

/** Small icons stay cached; large detail sprites use a bounded texture cache. */
public final class PokedexImages implements AutoCloseable {
    private record Entry(Texture texture, Image image) {}

    private final BwPokemonSprites source;
    private final GdxTwlRenderer renderer;
    private final Map<Integer, Entry> icons = new HashMap<>();
    private final Map<Integer, Entry> fronts = new LinkedHashMap<>(16, .75f, true);
    private final Map<Integer, PokedexSpriteAnimation> animations =
            new LinkedHashMap<>(4, .75f, true);
    private TextureAtlas atlas;
    private final Map<Image, Image> faintIcons = new IdentityHashMap<>();
    private String language = "zh";
    private ClientResources resources;
    private String glowFragment;

    PokedexImages(BwPokemonSprites source, GdxTwlRenderer renderer) {
        this.source = source;
        this.renderer = renderer;
    }

    public PokedexImages(
            BwPokemonSprites source, GdxTwlRenderer renderer, ClientResources resources) {
        this(source, renderer);
        this.resources = resources;
        try {
            var patch = resources.apply("sprites/sp-icon.pak");
            if (patch.exists()) source.loadShinyIcons(patch.readBytes());
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
        language = resources.strings().language().split("-")[0];
        atlas = new TextureAtlas(resources.apply("sprites/atlas/main.atlas"));
    }

    public void drawAlpha(
            Image image,
            de.matthiasmann.twl.renderer.AnimationState state,
            int x,
            int y,
            int width,
            int height,
            boolean shiny) {
        if (glowFragment == null)
            glowFragment = resources.apply("shaders/twl.fragment.glsl").readString("UTF-8");
        renderer.drawSpriteGlow(image, state, x, y, width, height, shiny, glowFragment);
    }

    public Image atlas(String name) {
        if (atlas == null) return null;
        var region = atlas.findRegion(name);
        return region == null ? null : renderer.image(region);
    }

    private final Map<String, Image> grayAtlasImages = new HashMap<>();

    public Image grayAtlas(String name) {
        if (atlas == null) return null;
        var cached = grayAtlasImages.get(name);
        if (cached != null) return cached;
        var region = atlas.findRegion(name);
        if (region == null) return null;
        try {
            var url =
                    new java.net.URL(
                            null,
                            "asset:/sprites/atlas/main.png",
                            new top.totodile.pokemmo.client.resources.AssetUrlStreamHandler(
                                    resources));
            var image =
                    renderer.grayscale(
                            url,
                            region.getRegionX(),
                            region.getRegionY(),
                            region.getRegionWidth(),
                            region.getRegionHeight());
            grayAtlasImages.put(name, image);
            return image;
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    public Image atlas(String name, int index) {
        if (atlas == null) return null;
        var region = atlas.findRegion(name, index);
        return region == null ? null : renderer.image(region);
    }

    public Image skillType(int type) {
        int id = type >= 9 ? type + 1 : type;
        Image image = atlas("skill_type_" + id + "_" + language);
        return image == null ? atlas("skill_type_" + id + "_en") : image;
    }

    public Image type(int type) {
        int id = type >= 9 ? type + 1 : type;
        Image image = atlas("monster_type_" + id + "_" + language);
        return image == null ? atlas("monster_type_" + id + "_en") : image;
    }

    public Image icon(int id) {
        return load(id, false);
    }

    public Image icon(int id, int form, int gender, boolean egg) {
        return iconVariant(id, form, gender, egg, false);
    }

    private Image iconVariant(int id, int form, int gender, boolean egg, boolean alpha) {
        return iconVariant(id, form, gender, egg, alpha, false);
    }

    private Image iconVariant(
            int id, int form, int gender, boolean egg, boolean alpha, boolean gold) {
        return iconVariant(id, form, gender, egg, alpha, gold, false);
    }

    private Image iconVariant(
            int id, int form, int gender, boolean egg, boolean alpha, boolean gold, boolean shiny) {
        int key =
                1 << 24
                        | id
                        | form << 10
                        | (gender + 1) << 20
                        | (egg ? 1 << 23 : 0)
                        | (alpha ? 1 << 25 : 0)
                        | (gold ? 1 << 26 : 0)
                        | (shiny ? 1 << 27 : 0);
        Entry entry = icons.get(key);
        if (entry == null) {
            try {
                var pixels = source.icon(id, form, gender, egg, shiny);
                if (alpha && !egg)
                    pixels = BwPokemonSprites.outlined(pixels, gold ? 0xffd033ff : 0xc2141dff);
                var pixmap = new Pixmap(pixels.width(), pixels.height(), Pixmap.Format.RGBA8888);
                try {
                    pixmap.setBlending(Pixmap.Blending.None);
                    for (int y = 0; y < pixels.height(); y++)
                        for (int x = 0; x < pixels.width(); x++)
                            pixmap.drawPixel(x, y, pixels.rgba()[y * pixels.width() + x]);
                    var texture = new Texture(pixmap);
                    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                    entry = new Entry(texture, renderer.image(texture));
                    icons.put(key, entry);
                } finally {
                    pixmap.dispose();
                }
            } catch (IOException error) {
                throw new UncheckedIOException(error);
            }
        }
        return entry.image;
    }

    public Image icon(int id, int form, int gender, boolean egg, boolean fainted) {
        var image = icon(id, form, gender, egg);
        return fainted
                ? faintIcons.computeIfAbsent(
                        image,
                        value ->
                                value.createTintedVersion(
                                        new de.matthiasmann.twl.Color(0xb0ffffff)))
                : image;
    }

    public Image icon(int id, int form, int gender, boolean egg, boolean fainted, boolean alpha) {
        var image = iconVariant(id, form, gender, egg, alpha && !egg);
        return fainted
                ? faintIcons.computeIfAbsent(
                        image,
                        value ->
                                value.createTintedVersion(
                                        new de.matthiasmann.twl.Color(0xb0ffffff)))
                : image;
    }

    /** Dedicated ROM small icons, patched by the latest official sp-icon.pak for shiny colours. */
    public Image partyIcon(
            int id,
            int form,
            int gender,
            boolean egg,
            boolean fainted,
            boolean shiny,
            boolean alpha) {
        var image =
                iconVariant(
                        id,
                        form,
                        gender,
                        egg,
                        alpha && !egg,
                        shiny && alpha && !egg,
                        shiny && !egg);
        return fainted
                ? faintIcons.computeIfAbsent(
                        image,
                        value ->
                                value.createTintedVersion(
                                        new de.matthiasmann.twl.Color(0xb0ffffff)))
                : image;
    }

    public Image icon(
            int id,
            int form,
            int gender,
            boolean egg,
            boolean fainted,
            boolean alpha,
            boolean shiny) {
        var image =
                iconVariant(
                        id,
                        form,
                        gender,
                        egg,
                        alpha && !egg,
                        shiny && alpha && !egg,
                        shiny && !egg);
        return fainted
                ? faintIcons.computeIfAbsent(
                        image,
                        value ->
                                value.createTintedVersion(
                                        new de.matthiasmann.twl.Color(0xb0ffffff)))
                : image;
    }

    public Image egg() {
        return load(Integer.MAX_VALUE, true);
    }

    public Image heldItem() {
        return load(Integer.MIN_VALUE, false);
    }

    public Image ball(int type) {
        int key = (1 << 30) | type;
        var entry = icons.get(key);
        if (entry != null) return entry.image;
        try {
            var pixels = source.ball(type);
            var pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
            try {
                pixmap.setBlending(Pixmap.Blending.None);
                for (int y = 0; y < 16; y++)
                    for (int x = 0; x < 16; x++) pixmap.drawPixel(x, y, pixels.rgba()[y * 16 + x]);
                var texture = new Texture(pixmap);
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                entry = new Entry(texture, renderer.image(texture));
                icons.put(key, entry);
                return entry.image;
            } finally {
                pixmap.dispose();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Image ribbon(int row, int column) {
        int key = (1 << 29) | row << 8 | column;
        var entry = icons.get(key);
        if (entry != null) return entry.image;
        try {
            var pixels = source.ribbon(row, column);
            var pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
            try {
                pixmap.setBlending(Pixmap.Blending.None);
                for (int y = 0; y < 32; y++)
                    for (int x = 0; x < 32; x++) pixmap.drawPixel(x, y, pixels.rgba()[y * 32 + x]);
                var texture = new Texture(pixmap);
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                entry = new Entry(texture, renderer.image(texture));
                icons.put(key, entry);
                return entry.image;
            } finally {
                pixmap.dispose();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final Map<String, Entry> particleTextures = new HashMap<>();

    public Image particle(int id) {
        return particle(id, 0, 0, false);
    }

    /**
     * ParticleManager.RY: select APS frames, concatenate the range or center-composite its
     * endpoints.
     */
    public Image particle(int id, int low, int high, boolean mix) {
        return particle(id, low, high, mix, false, false);
    }

    /** ParticleManager.He mirrors APS cells before concatenating the animation strip. */
    public Image particle(
            int id, int low, int high, boolean mix, boolean repeatX, boolean repeatY) {
        return particle(id, low, high, mix, repeatX, repeatY, false, false);
    }

    public Image particle(
            int id,
            int low,
            int high,
            boolean mix,
            boolean repeatX,
            boolean repeatY,
            boolean flipX,
            boolean flipY) {
        String key =
                id + ":" + low + ":" + high + ":" + mix + ":" + repeatX + ":" + repeatY + ":"
                        + flipX + ":" + flipY;
        var entry = particleTextures.get(key);
        if (entry != null) return entry.image;
        try {
            var first = particlePixels(id, low, repeatX, repeatY);
            var last = particlePixels(id, high, repeatX, repeatY);
            int width =
                    mix ? Math.max(first.width(), last.width()) : first.width() * (high - low + 1);
            int height =
                    mix
                            ? (first.width() >= last.width() ? first.height() : last.height())
                            : first.height();
            var pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            try {
                pixmap.setBlending(Pixmap.Blending.None);
                if (mix && low != high) {
                    var base = first.width() >= last.width() ? first : last;
                    var overlay = base == first ? last : first;
                    copyParticle(
                            pixmap, base, 0,
                            0); // RY preserves the decoded base pixmap's None blending.
                    copyParticle(
                            pixmap,
                            overlay,
                            (width - overlay.width()) / 2,
                            (height - overlay.height()) / 2);
                } else
                    for (int f = low; f <= high; f++)
                        copyParticle(
                                pixmap,
                                particlePixels(id, f, repeatX, repeatY),
                                (f - low) * first.width(),
                                0);
                if (flipX || flipY) {
                    int cellWidth = mix ? width : first.width();
                    int[] original = new int[width * height];
                    for (int y = 0; y < height; y++)
                        for (int x = 0; x < width; x++)
                            original[y * width + x] = pixmap.getPixel(x, y);
                    for (int y = 0; y < height; y++)
                        for (int x = 0; x < width; x++) {
                            int
                                    sourceX =
                                            flipX
                                                    ? x / cellWidth * cellWidth
                                                            + cellWidth
                                                            - 1
                                                            - x % cellWidth
                                                    : x,
                                    sourceY = flipY ? height - 1 - y : y;
                            pixmap.drawPixel(x, y, original[sourceY * width + sourceX]);
                        }
                }
                var texture = new Texture(pixmap);
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                entry = new Entry(texture, renderer.image(texture));
                particleTextures.put(key, entry);
                return entry.image;
            } finally {
                pixmap.dispose();
            }
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private BwPokemonSprites.Pixels particlePixels(
            int id, int frame, boolean repeatX, boolean repeatY) throws IOException {
        var base = source.particle(id, frame);
        if (!repeatX && !repeatY) return base;
        int width = base.width() * (repeatX ? 2 : 1), height = base.height() * (repeatY ? 2 : 1);
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int sx = x < base.width() ? x : width - 1 - x,
                        sy = y < base.height() ? y : height - 1 - y;
                pixels[y * width + x] = base.rgba()[sy * base.width() + sx];
            }
        return new BwPokemonSprites.Pixels(width, height, pixels);
    }

    private static void copyParticle(
            Pixmap target, BwPokemonSprites.Pixels pixels, int left, int top) {
        for (int y = 0; y < pixels.height(); y++)
            for (int x = 0; x < pixels.width(); x++)
                target.drawPixel(left + x, top + y, pixels.rgba()[y * pixels.width() + x]);
    }

    public Image battleBackground(int id) {
        String key = "battle-background:" + id;
        var cached = particleTextures.get(key);
        if (cached != null) return cached.image;
        try {
            var pixels = source.battleBackground(id);
            var pixmap = new Pixmap(pixels.width(), pixels.height(), Pixmap.Format.RGBA8888);
            try {
                pixmap.setBlending(Pixmap.Blending.None);
                copyParticle(pixmap, pixels, 0, 0);
                var texture = new Texture(pixmap);
                texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
                var entry = new Entry(texture, renderer.image(texture));
                particleTextures.put(key, entry);
                return entry.image;
            } finally {
                pixmap.dispose();
            }
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    Image icon(int id, boolean seen) {
        Image image = icon(id);
        return seen
                ? image
                : image.createTintedVersion(
                        new de.matthiasmann.twl.Color((byte) 0, (byte) 0, (byte) 0, (byte) 180));
    }

    public Image item(int id) {
        return load(-id, false);
    }

    Image front(int id) {
        return load(id, true);
    }

    Image front(int id, int form, boolean shiny) {
        return load(id | form << 10 | (shiny ? 1 << 20 : 0), true);
    }

    public Image animatedFront(int id, int form, boolean shiny, double seconds) {
        return animated(id, form, shiny, false, seconds);
    }

    public Image animated(int id, int form, boolean shiny, boolean back, double seconds) {
        int key = id | form << 10 | (shiny ? 1 << 20 : 0) | (back ? 1 << 21 : 0);
        try {
            var animation = animations.get(key);
            if (animation == null) {
                animation =
                        new PokedexSpriteAnimation(
                                source.animation(id, form, shiny, back), renderer);
                animations.put(key, animation);
                if (animations.size() > 4) {
                    var entries = animations.values().iterator();
                    entries.next().close();
                    entries.remove();
                }
            }
            return animation.at(seconds);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
    }

    private Image load(int id, boolean front) {
        var cache = front ? fronts : icons;
        Entry entry = cache.get(id);
        if (entry == null) {
            try {
                var pixels =
                        id == Integer.MAX_VALUE
                                ? source.egg()
                                : id == Integer.MIN_VALUE
                                        ? source.heldItem()
                                        : front
                                                ? source.front(
                                                        id & 1023,
                                                        id >> 10 & 1023,
                                                        (id & (1 << 20)) != 0)
                                                : id < 0 ? source.item(-id) : source.icon(id);
                Pixmap pixmap = new Pixmap(pixels.width(), pixels.height(), Pixmap.Format.RGBA8888);
                try {
                    pixmap.setBlending(Pixmap.Blending.None);
                    for (int y = 0; y < pixels.height(); y++)
                        for (int x = 0; x < pixels.width(); x++)
                            pixmap.drawPixel(x, y, pixels.rgba()[y * pixels.width() + x]);
                    Texture texture = new Texture(pixmap);
                    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                    entry = new Entry(texture, renderer.image(texture));
                    cache.put(id, entry);
                } finally {
                    pixmap.dispose();
                }
                if (fronts.size() > 16) {
                    var iterator = fronts.values().iterator();
                    iterator.next().texture.dispose();
                    iterator.remove();
                }
            } catch (IOException error) {
                throw new UncheckedIOException(error);
            }
        }
        return entry.image;
    }

    @Override
    public void close() {
        particleTextures.values().forEach(e -> e.texture.dispose());
        particleTextures.clear();
        icons.values().forEach(e -> e.texture.dispose());
        fronts.values().forEach(e -> e.texture.dispose());
        animations.values().forEach(PokedexSpriteAnimation::close);
        icons.clear();
        faintIcons.clear();
        fronts.clear();
        animations.clear();
        if (atlas != null) atlas.dispose();
    }
}
