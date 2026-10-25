package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;

import de.matthiasmann.twl.renderer.*;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.character.*;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.client.ui.pokemon.AttributeParticleScene;

import java.io.IOException;
import java.util.*;
import java.util.zip.ZipInputStream;

/** PC subset of Gdx3DModelBatch.w9 and BattleJ40Animation.Px. No synthetic sprite frames. */
final class EncounterEntrance implements AutoCloseable {
    static final long WILD = 3500, THROW = 1000, RELEASE = 1410, DURATION = 6810;
    private final CharacterAppearance appearance;
    private final CharacterPreview trainer;
    private final java.util.List<com.badlogic.gdx.graphics.Texture> textures = new ArrayList<>();
    private final java.util.List<Image> balls = new ArrayList<>();
    private final CatmullRomSpline<Vector3> path =
            new CatmullRomSpline<>(
                    new Vector3[] {
                        new Vector3(.5f, 2, 6),
                        new Vector3(.5f, 2, 6),
                        new Vector3(1.5f, 4.5f, 4),
                        new Vector3(3, 3.5f, 4),
                        new Vector3(3, 3.5f, 3.5f)
                    },
                    false);
    private final AttributeParticleScene particles;
    private Image recallBall;
    private float particleTime;
    private final boolean floating, burrowing;
    // BattleJ40Animation.zh0 selects fv for these species, coM9 for Diglett/Dugtrio, Px otherwise.
    private static final Set<Integer> FLOATING =
            Set.of(
                    12, 15, 17, 22, 41, 42, 49, 63, 70, 71, 72, 73, 81, 82, 90, 91, 92, 93, 109,
                    110, 116, 117, 118, 119, 120, 121, 129, 130, 137, 138, 139, 140, 142, 145, 146,
                    151, 166, 169, 170, 171, 174, 187, 188, 189, 193, 200, 201, 211, 223, 226, 230,
                    233, 249, 250, 251, 267, 269, 270, 278, 279, 284, 291, 292, 307, 313, 314, 318,
                    319, 320, 321, 330, 333, 337, 338, 339, 340, 344, 347, 349, 351, 353, 355, 358,
                    362, 367, 368, 369, 370, 374, 375, 380, 381, 382, 384, 385, 412, 413, 414, 415,
                    416, 425, 426, 429, 433, 436, 437, 455, 456, 457, 458, 462, 468, 469, 472, 474,
                    476, 477, 478, 479, 480, 481, 482, 488, 489, 491, 517, 518, 527, 528, 550, 561,
                    562, 563, 564, 567, 577, 578, 579, 582, 583, 584, 587, 589, 592, 593, 594, 598,
                    599, 601, 602, 605, 606, 608, 609, 615, 618, 628, 635, 637, 641, 642, 645);

    EncounterEntrance(
            CharacterAppearance appearance,
            int skin,
            int gender,
            BwPokemonSprites sprites,
            top.totodile.pokemmo.server.game.PokemonData.Pokemon pokemon,
            PokedexImages images,
            ClientResources resources,
            GdxTwlRenderer renderer)
            throws IOException {
        this.appearance = appearance;
        int ball = pokemon.ball();
        floating =
                FLOATING.contains(pokemon.species())
                        || pokemon.species() == 487 && pokemon.form() == 1
                        || pokemon.species() == 648 && pokemon.form() == 0;
        burrowing = pokemon.species() == 50 || pokemon.species() == 51;
        trainer =
                appearance == null
                        ? null
                        : new CharacterPreview(
                                appearance, renderer, false, () -> skin, () -> gender, () -> 0L);
        AttributeParticleScene loaded = null;
        try {
            var recall = sprites.recallBall(ball);
            var recallPixmap = new Pixmap(recall.width(), recall.height(), Pixmap.Format.RGBA8888);
            try {
                recallPixmap.setBlending(Pixmap.Blending.None);
                for (int y = 0; y < recall.height(); y++)
                    for (int x = 0; x < recall.width(); x++)
                        recallPixmap.drawPixel(x, y, recall.rgba()[y * recall.width() + x]);
                var texture = new com.badlogic.gdx.graphics.Texture(recallPixmap);
                textures.add(texture);
                recallBall = renderer.image(texture);
            } finally {
                recallPixmap.dispose();
            }
            for (var pixels : sprites.thrownBall(ball)) {
                var pixmap = new Pixmap(pixels.width(), pixels.height(), Pixmap.Format.RGBA8888);
                try {
                    pixmap.setBlending(Pixmap.Blending.None);
                    for (int y = 0; y < pixels.height(); y++)
                        for (int x = 0; x < pixels.width(); x++)
                            pixmap.drawPixel(x, y, pixels.rgba()[y * pixels.width() + x]);
                    var texture = new com.badlogic.gdx.graphics.Texture(pixmap);
                    textures.add(texture);
                    balls.add(renderer.image(texture));
                } finally {
                    pixmap.dispose();
                }
            }
            String name = "particle/special/" + (ball == 15 ? "spawn_cherish" : "spawn") + ".vfx";
            try (var zip = new ZipInputStream(resources.apply("sprites/particles.pak").read())) {
                for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry())
                    if (entry.getName().equals(name)) {
                        var effect =
                                new JsonReader()
                                        .parse(
                                                new String(
                                                        zip.readAllBytes(),
                                                        java.nio.charset.StandardCharsets.UTF_8));
                        loaded =
                                new AttributeParticleScene(
                                        images, effect, path.valueAt(new Vector3(), .99f));
                        break;
                    }
            }
            if (loaded == null) throw new IOException("Missing reference release effect " + name);
        } catch (IOException | RuntimeException error) {
            if (trainer != null) trainer.close();
            if (appearance != null) appearance.dispose();
            textures.forEach(com.badlogic.gdx.graphics.Texture::dispose);
            throw error;
        }
        particles = loaded;
    }

    /** J40's ball fade/move (.20 + .01), actor raise (.01), then growth delay (.40). */
    private long releaseAt() {
        return trainer == null ? 1620 : RELEASE;
    }

    long sendOutDuration() {
        return trainer == null
                ? releaseAt() + growth() + (floating ? 1000 : burrowing ? 400 : 500)
                : DURATION - WILD;
    }

    private int growth() {
        return floating ? 800 : burrowing ? 500 : 1000;
    }

    float pokemonScale(long elapsed) {
        float t = elapsed - WILD;
        if (t < releaseAt()) return 0;
        if (t < releaseAt() + growth()) return 1.1f * ease((t - releaseAt()) / growth());
        return 1.1f - .1f * ease((t - releaseAt() - growth()) / 400);
    }

    float white(long elapsed) {
        return 1 - ease((elapsed - WILD - releaseAt() - growth()) / 250f);
    }

    float height(long elapsed) {
        float t = elapsed - WILD - releaseAt();
        if (burrowing) return -.4f * (1 - ease(t / 500));
        if (floating) return (.55f + .35f * ease(t / 800)) * (1 - ease((t - 1200) / 600));
        return .55f * (1 - ease((t - (trainer == null ? 1000 : 1400)) / 500));
    }

    float wildShade(long elapsed) {
        return .75f * (1 - ease((elapsed - 1000) / 1500f));
    }

    void drawTrainer(long elapsed, int x, int y, int width, int height) {
        if (trainer == null) return;
        long time = elapsed - WILD;
        // GdxMaterialPart keeps the idle sprite below the viewport (tX=159).
        // Cq0 starts the visible frame sequence at tX=0 when the throw begins.
        if (time < 0 || time >= 625) return;
        float scale = height / 133f;
        // KeyframeTimelineSequence: 5 frames at 125 ms; Modern_Col_Kv2 moves left each 10 ms.
        int frame = (int) Math.max(0, time / 125), left = 45 - (int) Math.max(0, time / 10);
        // Ka0's camera translation anchors 80 logical pixels above the viewport bottom.
        trainer.drawBattleBack(frame, x + left * scale, y + height - 80 * scale, scale);
    }

    void drawBall(
            long elapsed,
            EncounterScene scene,
            GdxTwlRenderer renderer,
            AnimationState state,
            int x,
            int y,
            int width,
            int height) {
        long time = elapsed - WILD;
        if (time < 0 || time > THROW + 200) return;
        var point = path.valueAt(new Vector3(), Math.min(.99f, time / (float) THROW));
        var projected = scene.project(point);
        int size =
                Math.max(
                        1,
                        Math.round(
                                .32f
                                        * height
                                        / (2
                                                * (float) Math.tan(Math.toRadians(25))
                                                * scene.camera().position.dst(point))));
        renderer.pushGlobalTintColor(1, 1, 1, 1 - ease((time - THROW) / 200f));
        try {
            balls.get((int) (time / 40) % balls.size())
                    .draw(
                            state,
                            x + Math.round(projected.x) - size / 2,
                            y + height - Math.round(projected.y) - size / 2,
                            size,
                            size);
        } finally {
            renderer.popGlobalTintColor();
        }
    }

    void drawRelease(
            long elapsed,
            EncounterScene scene,
            GdxTwlRenderer renderer,
            int x,
            int y,
            int width,
            int height) {
        float time = (elapsed - WILD - (trainer == null ? 1220 : 1010)) / 1000f;
        if (time < 0 || time > 2) return;
        particles.draw(
                renderer, Math.max(0, time - particleTime), x, y, width, height, scene.camera());
        particleTime = time;
    }

    /** BattleBv00Animation: stationary ball at actor center minus (.1, .45, .05). */
    void drawRecall(
            long elapsed,
            EncounterScene scene,
            GdxTwlRenderer renderer,
            AnimationState state,
            int x,
            int y,
            int width,
            int height) {
        if (elapsed < 250 || elapsed >= 1450) return;
        var point = new Vector3(2.9f, 2.65f, 3.95f);
        var projected = scene.project(point);
        int size =
                Math.max(
                        1,
                        Math.round(
                                .16f
                                        * height
                                        / (2
                                                * (float) Math.tan(Math.toRadians(25))
                                                * scene.camera().position.dst(point))));
        recallBall.draw(
                state,
                x + Math.round(projected.x) - size / 2,
                y + height - Math.round(projected.y) - size / 2,
                size,
                size);
    }

    private static float ease(float t) {
        t = Math.max(0, Math.min(1, t));
        return t < .5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
    }

    public void close() {
        particles.close();
        if (trainer != null) trainer.close();
        if (appearance != null) appearance.dispose();
        textures.forEach(com.badlogic.gdx.graphics.Texture::dispose);
    }
}
