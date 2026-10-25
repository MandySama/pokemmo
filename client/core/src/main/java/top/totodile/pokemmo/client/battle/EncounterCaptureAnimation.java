package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;

import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.client.rom.nds.bw.BwPokemonSprites;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

import java.io.IOException;
import java.util.*;

/** BattleOk01Animation: capture arc, absorption, bounce, native ball shakes and camera tracks. */
final class EncounterCaptureAnimation implements AutoCloseable {
    private final java.util.List<Texture> textures = new ArrayList<>();
    private final java.util.List<java.util.List<Image>> frames = new ArrayList<>();
    private final Image open;
    private final long started;
    final int result;
    private final Vector3 target;
    private final CatmullRomSpline<Vector3> path;
    private float time;
    private static final float ABSORB = 1.005f, DROP = 2.255f, LAND = 3.255f;

    EncounterCaptureAnimation(
            BwPokemonSprites sprites,
            GdxTwlRenderer renderer,
            int ball,
            int result,
            long now,
            Vector3 target)
            throws IOException {
        this.result = result;
        this.started = now;
        this.target = target.cpy();
        path =
                new CatmullRomSpline<>(
                        new Vector3[] {
                            new Vector3(3, 3.85f, 5),
                            new Vector3(3, 3.6f, 5),
                            new Vector3(3.5f, 4.1f, 2),
                            target.cpy().add(.05f, 0, .1f),
                            target.cpy().add(.05f, 0, .1f)
                        },
                        false);
        try {
            open = image(sprites.recallBall(ball), renderer);
            for (int i = 0; i <= 4; i++) {
                var sequence = new ArrayList<Image>();
                for (var pixels : sprites.ballAnimation(ball, i))
                    sequence.add(image(pixels, renderer));
                frames.add(sequence);
            }
        } catch (IOException | RuntimeException error) {
            close();
            throw error;
        }
    }

    private Image image(BwPokemonSprites.Pixels pixels, GdxTwlRenderer renderer) {
        var pixmap = new Pixmap(pixels.width(), pixels.height(), Pixmap.Format.RGBA8888);
        try {
            pixmap.setBlending(Pixmap.Blending.None);
            for (int y = 0; y < pixels.height(); y++)
                for (int x = 0; x < pixels.width(); x++)
                    pixmap.drawPixel(x, y, pixels.rgba()[y * pixels.width() + x]);
            var texture = new Texture(pixmap);
            textures.add(texture);
            return renderer.image(texture);
        } finally {
            pixmap.dispose();
        }
    }

    void update(long now) {
        time = Math.max(0, (now - started) / 1000f);
    }

    private float resolution() {
        return LAND + 1.25f * Math.min(result, 3) + (result == 4 ? 0 : .5f);
    }

    boolean complete() {
        return time >= resolution() + (result == 4 ? 2.7f : 2.01f);
    }

    float scale() {
        if (time < ABSORB) return 1;
        if (time < ABSORB + .5f) return 1 - ease((time - ABSORB) / .5f);
        return result == 4 ? 0 : ease((time - resolution()) / .5f);
    }

    float shade() {
        if (time < ABSORB) return 0;
        if (time < ABSORB + .5f) return ease((time - ABSORB) / .2f);
        return result == 4 ? 1 : 1 - ease(time - resolution());
    }

    float height() {
        return result == 4 || time < resolution()
                ? 0
                : -.5f * (1 - ease((time - resolution()) / .5f));
    }

    void camera(PerspectiveCamera camera) {
        float close = ease((time - .755f) / .25f);
        float returnAt = resolution() + (result == 4 ? 1.2f : 1.51f);
        close *= 1 - ease((time - returnAt) / (result == 4 ? 1.5f : .5f));
        var position = new Vector3(3.5f, 3.4f, 2.5f).lerp(target.cpy().add(0, .5f, 1), close);
        var look = new Vector3(3.5f, 3.25f, 4).lerp(target.cpy().add(-.5f, -1, -1), close);
        camera.up.set(Vector3.Y);
        camera.position.set(position).add(0, 0, 3);
        camera.lookAt(look);
        camera.rotateAround(look, camera.direction.cpy().crs(camera.up).nor(), -10);
        camera.update();
    }

    void draw(
            EncounterScene scene,
            GdxTwlRenderer renderer,
            AnimationState state,
            int x,
            int y,
            int width,
            int height) {
        if (time < .5f || complete()) return;
        var point =
                time < ABSORB
                        ? path.valueAt(new Vector3(), Math.min(.99f, (time - .5f) / .5f))
                        : target.cpy().add(.05f, 0, .1f);
        if (time >= DROP) point.y -= .35f * Interpolation.bounceOut.apply(Math.min(1, time - DROP));
        Image frame = open;
        float animationTime = 0;
        int animation = -1;
        if (time >= ABSORB + 1) animation = 0;
        if (time >= DROP && time < LAND - .2f) {
            animation = 4;
            animationTime = time - DROP;
        }
        if (time >= LAND)
            for (int i = 0; i < Math.min(result, 3); i++) {
                float start = LAND + .5f + i * 1.25f;
                if (time >= start) {
                    animation = i == 1 ? 2 : 1;
                    animationTime = time - start;
                }
            }
        float sizeFactor = 1;
        if (result < 4 && time >= resolution()) {
            frame = open;
            animation = -1;
            if (time >= resolution() + .5f) return;
        }
        if (animation >= 0) {
            var sequence = frames.get(animation);
            frame = sequence.get(Math.min(sequence.size() - 1, (int) (animationTime / .04f)));
            if (animation == 4)
                frame = sequence.get((int) (animationTime / .04f) % sequence.size());
        }
        var projected = scene.project(point);
        int size =
                Math.max(
                        1,
                        Math.round(
                                .32f
                                        * sizeFactor
                                        * height
                                        / (2
                                                * (float) Math.tan(Math.toRadians(25))
                                                * scene.camera().position.dst(point))));
        float tint =
                result == 4 && time >= resolution()
                        ? 1 - .6f * ease((time - resolution()) / .2f)
                        : 1;
        renderer.pushGlobalTintColor(tint, tint, tint, 1);
        try {
            frame.draw(
                    state,
                    x + Math.round(projected.x) - size / 2,
                    y + height - Math.round(projected.y) - size / 2,
                    size,
                    size);
        } finally {
            renderer.popGlobalTintColor();
        }
    }

    private static float ease(float t) {
        t = Math.max(0, Math.min(1, t));
        return t < .5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
    }

    public void close() {
        textures.forEach(Texture::dispose);
        textures.clear();
    }
}
