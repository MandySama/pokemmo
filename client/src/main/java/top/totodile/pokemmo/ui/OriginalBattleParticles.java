package top.totodile.pokemmo.ui;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.Image;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.battle.MoveAnimations;
import top.totodile.pokemmo.rom.BattleScene;
import top.totodile.pokemmo.rom.ParticleTextures;

import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** CPU 控制器运行于独立类加载器，宿主只管理纹理上传及 Billboard 绘制。 */
final class OriginalBattleParticles {
    record Definition(String source, MoveAnimations.Effect effect, List<BufferedImage> textures) { }
    private static final Object LOCK = new Object();
    private static final Constructor<?> CONSTRUCTOR;
    private static final Method CAMERA;
    private static final Method UPDATE;
    private static final Method PARTICLES;
    private static final Method CLOSE;
    private static final URLClassLoader LOADER;

    static {
        try {
            LOADER = new URLClassLoader(new java.net.URL[]{ClientFiles.data("battle/particle-runtime.jar").toUri().toURL()}, ClassLoader.getPlatformClassLoader());
            var type = Class.forName("top.totodile.particlebridge.ParticleBridge", true, LOADER);
            CONSTRUCTOR = type.getConstructor(String.class, int.class, int.class, int.class, float.class, boolean.class, int.class, float.class);
            CAMERA = type.getMethod("camera", float[].class, float[].class, int.class, int.class);
            UPDATE = type.getMethod("update", float.class);
            PARTICLES = type.getMethod("particles");
            CLOSE = type.getMethod("close");
        } catch (ReflectiveOperationException | IOException error) { throw new ExceptionInInitializerError(error); }
    }

    static Class<?> runtimeClass(String name) throws ClassNotFoundException {
        synchronized (LOCK) {
            ClassLoader previous = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(LOADER);
            try { return Class.forName(name, true, LOADER); }
            finally { Thread.currentThread().setContextClassLoader(previous); }
        }
    }

    static List<BattleParticles.Definition> load(Path rom, int move) throws IOException {
        return load(rom, MoveAnimations.get(move));
    }

    static List<BattleParticles.Definition> load(Path rom, MoveAnimations.Animation animation) throws IOException {
        List<BattleParticles.Definition> definitions = new ArrayList<>();
        Map<Integer, List<BufferedImage>> cache = new HashMap<>();
        Map<Integer, String> resources = new HashMap<>();
        Map<String, BufferedImage> atlases = new HashMap<>();
        try (var zip = new ZipFile(ClientFiles.data("battle/particles.zip").toFile())) {
            for (var effect : animation.effects()) {
                String source = resources.get(effect.archive());
                if (source == null) {
                    var entry = zip.getEntry("particle/" + MoveAnimations.resource(effect.archive()) + ".vfx");
                    if (entry == null) throw new IOException("Missing particle resource " + effect.archive());
                    try (var input = zip.getInputStream(entry)) { source = new String(input.readAllBytes(), StandardCharsets.UTF_8); }
                    resources.put(effect.archive(), source);
                }
                var controllers = new JsonReader().parse(source).get("resource").get("controllers");
                var controller = controllers.get(effect.controller());
                int trail = controller.getInt("trailController", -1);
                List<BufferedImage> textures = new ArrayList<>();
                // 通道输出顺序为背景拖尾、父粒子。
                if (trail >= 0) textures.add(atlas(controllers, trail, effect.archive(), rom, zip, cache, atlases));
                textures.add(atlas(controllers, effect.controller(), effect.archive(), rom, zip, cache, atlases));
                definitions.add(new BattleParticles.Definition(effect, new Definition(source, effect, List.copyOf(textures))));
            }
        }
        return List.copyOf(definitions);
    }

    private static BufferedImage atlas(JsonValue controllers, int index, int resource, Path rom, ZipFile zip,
                                       Map<Integer, List<BufferedImage>> pixels, Map<String, BufferedImage> atlases) throws IOException {
        String key = resource + ":" + index;
        BufferedImage image = atlases.get(key);
        if (image == null) { image = texture(controllers.get(index), rom, zip, pixels); atlases.put(key, image); }
        return image;
    }

    private static BufferedImage texture(JsonValue controller, Path rom, ZipFile zip, Map<Integer, List<BufferedImage>> cache) throws IOException {
        int id = controller.getInt("aps");
        boolean custom = controller.getString("type", "").equals("CUSTOM");
        int key = custom ? -id - 1 : id;
        List<BufferedImage> pixels = cache.get(key);
        if (pixels == null) {
            if (custom) {
                var entry = zip.getEntry("sprites/" + id + ".png");
                if (entry == null) throw new IOException("Missing particle texture " + id);
                try (var input = zip.getInputStream(entry)) { pixels = List.of(ImageIO.read(input)); }
            } else pixels = ParticleTextures.load(rom, id);
            cache.put(key, pixels);
        }
        var range = controller.get("aps_texture_range");
        int first = custom ? 0 : (int) range.getFloat("lowMin", 0);
        int last = custom ? 0 : (int) range.getFloat("lowMax", first);
        if (first < 0 || last < first || last >= pixels.size()) throw new IOException("Invalid particle texture range " + id);
        boolean repeatX = controller.getBoolean("repeat_x", false), repeatY = controller.getBoolean("repeat_y", false);
        BufferedImage result;
        if (!custom && controller.getBoolean("mix", false) && first != last) {
            BufferedImage low = repeat(pixels.get(first), repeatX, repeatY);
            BufferedImage high = repeat(pixels.get(last), repeatX, repeatY);
            BufferedImage base = low.getWidth() >= high.getWidth() ? low : high;
            BufferedImage overlay = base == low ? high : low;
            result = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
            var graphics = result.createGraphics();
            try {
                graphics.drawImage(base, 0, 0, null);
                graphics.drawImage(overlay, (base.getWidth() - overlay.getWidth()) / 2, (base.getHeight() - overlay.getHeight()) / 2, null);
            } finally { graphics.dispose(); }
        } else if (first == last) {
            result = repeat(pixels.get(first), repeatX, repeatY);
        } else {
            boolean average = controller.getBoolean("avg", false);
            int width = 0, height = 0;
            if (average) {
                for (int i = first; i <= last; i++) {
                    BufferedImage image = pixels.get(i);
                    width = Math.max(width, image.getWidth());
                    if (height < image.getHeight()) height = image.getWidth();
                }
            }
            BufferedImage firstFrame = repeat(pixels.get(first), repeatX, repeatY);
            if (width == 0 || height == 0) { width = firstFrame.getWidth(); height = firstFrame.getHeight(); }
            result = new BufferedImage(width * (last - first + 1), height, BufferedImage.TYPE_INT_ARGB);
            for (int i = first; i <= last; i++) {
                BufferedImage frame = i == first ? firstFrame : repeat(pixels.get(i), repeatX, repeatY);
                var graphics = result.createGraphics();
                try {
                    graphics.setComposite(AlphaComposite.Src);
                    graphics.clipRect((i - first) * width, 0, width, height);
                    if (average && frame.getWidth() != width) graphics.drawImage(frame, (i - first) * width, 0, width, height, null);
                    else graphics.drawImage(frame, (i - first) * width, 0, null);
                } finally { graphics.dispose(); }
            }
        }
        boolean flipX = controller.getBoolean("flip_x", false), flipY = controller.getBoolean("flip_y", false);
        return flipX || flipY ? flip(result, flipX, flipY) : result;
    }

    private static BufferedImage repeat(BufferedImage image, boolean horizontal, boolean vertical) {
        if (!horizontal && !vertical) return image;
        var result = new BufferedImage(image.getWidth() * (horizontal ? 2 : 1), image.getHeight() * (vertical ? 2 : 1), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < result.getHeight(); y++) for (int x = 0; x < result.getWidth(); x++) {
            int sourceX = x < image.getWidth() ? x : result.getWidth() - 1 - x;
            int sourceY = y < image.getHeight() ? y : result.getHeight() - 1 - y;
            result.setRGB(x, y, image.getRGB(sourceX, sourceY));
        }
        return result;
    }

    private static BufferedImage flip(BufferedImage image, boolean horizontal, boolean vertical) {
        var result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++)
            result.setRGB(x, y, image.getRGB(horizontal ? image.getWidth() - 1 - x : x, vertical ? image.getHeight() - 1 - y : y));
        return result;
    }

    private final Definition definition;
    private final List<Image> images;
    private Object controller;
    private boolean reversed;
    private double simulated;
    private long previous = -1;
    private int drawn;

    OriginalBattleParticles(Definition definition, List<Image> images) { this.definition = definition; this.images = List.copyOf(images); }

    void draw(Widget widget, long milliseconds, BattleScene scene, boolean reverse, int x, int y, int width, int height) {
        long local = milliseconds - definition.effect().start();
        if (local < 0) return;
        synchronized (LOCK) {
            ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(LOADER);
            try {
                if (controller == null || milliseconds < previous || reversed != reverse) {
                    close();
                    var effect = definition.effect();
                    controller = CONSTRUCTOR.newInstance(definition.source(), effect.controller(), effect.target(), effect.destination(), effect.adjustment(), reverse, effect.position(), effect.travelDuration());
                    simulated = 0;
                    reversed = reverse;
                }
                previous = milliseconds;
                CAMERA.invoke(controller, scene.particleCameraMatrix(), scene.particleCameraView(), width, height);
                double requested = local / 1000.0;
                while (simulated + 1e-9 < requested) {
                    float step = (float) Math.min(1.0 / 60, requested - simulated);
                    UPDATE.invoke(controller, step);
                    simulated += step;
                }
                float[][] layers = (float[][]) PARTICLES.invoke(controller);
                var renderer = (GlfwTwlRenderer) widget.getGUI().getRenderer();
                drawn = 0;
                for (int layer = 0; layer < layers.length; layer++) {
                    float[] particles = layers[layer];
                    for (int offset = 0; offset < particles.length; offset += 17) {
                        if (particles[offset + 12] <= 0) continue;
                        float[] vertices = scene.particleQuad(particles, offset, x, y, width, height);
                        if (vertices == null) continue;
                        renderer.drawParticleQuad(images.get(layer), vertices,
                                particles[offset + 3], particles[offset + 4], particles[offset + 5], particles[offset + 6],
                                particles[offset + 9], particles[offset + 10], particles[offset + 11], particles[offset + 12]);
                        drawn++;
                    }
                }
            } catch (ReflectiveOperationException error) {
                Throwable cause = error instanceof InvocationTargetException invocation ? invocation.getCause() : error;
                throw new IllegalStateException("Particle controller failed " + definition.effect(), cause);
            } finally { Thread.currentThread().setContextClassLoader(previousLoader); }
        }
    }

    int drawnParticles() { return drawn; }

    void close() {
        synchronized (LOCK) {
            if (controller != null) {
                try { CLOSE.invoke(controller); }
                catch (ReflectiveOperationException error) { throw new IllegalStateException("Unable to release CPU particles", error); }
                finally { controller = null; }
            }
        }
    }
}
