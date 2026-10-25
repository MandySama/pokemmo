package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;

import top.totodile.pokemmo.client.resources.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.client.ui.pokemon.AttributeParticleScene;
import top.totodile.pokemmo.client.world.WorldAtmosphere.Weather;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Gdx3DModelBatch.fM and GdxParticleSystemRenderer.qq0: battle-specific weather and ambient. */
public final class EncounterAtmosphere implements AutoCloseable {
    public record State(long seconds, boolean outside, Weather weather) {}

    private AttributeParticleScene particles;
    private Weather weather;
    private long changedAt, drawnAt = -1;
    private float fromOverlay, targetOverlay;
    private final Color ambient = new Color(Color.WHITE), modelTint = new Color(Color.CLEAR);

    void update(State state, long now, ClientResources resources, PokedexImages images) {
        Weather selected = state == null ? Weather.CLEAR : state.weather();
        if (selected != weather) {
            fromOverlay = overlay(now);
            changedAt = now;
            weather = selected;
            String effect =
                    switch (selected) {
                        case RAIN, THUNDER, HEAVY_RAIN -> "rainbattle";
                        case SNOW, HEAVY_SNOW, HAIL -> "snowbattle";
                        case SANDSTORM -> "sandstormbattle";
                        default -> null;
                    };
            targetOverlay = effect == null ? 0 : .35f;
            if (particles != null) particles.close();
            particles = null;
            drawnAt = -1;
            if (effect != null)
                try {
                    var config =
                            new JsonReader()
                                    .parse(
                                            new String(
                                                    ParticleResources.read(
                                                            resources,
                                                            "particle/weather/" + effect + ".vfx"),
                                                    StandardCharsets.UTF_8));
                    particles = new AttributeParticleScene(images, config, new Vector3());
                } catch (IOException | RuntimeException error) {
                    com.badlogic.gdx.Gdx.app.error(
                            "Encounter", "Unable to load battle weather " + effect, error);
                }
        }
        modelTint.set(0, 0, 0, overlay(now));
        ambient.set(Color.WHITE);
        if (state == null || !state.outside()) return;
        int minute = (int) Math.floorMod(Math.floorDiv(state.seconds(), 60), 1440);
        int time = minute / 60 * 100 + (int) ((minute % 60) * 1.65);
        if (time >= 700 && time <= 2000) return;
        float daylight =
                time > 2000 && time < 2255
                        ? (2255 - time) / 255f
                        : time > 445 && time < 700 ? (time - 445) / 255f : 0;
        ambient.set(.29f + daylight * .71f, .29f + daylight * .71f, .5f + daylight * .5f, 1);
    }

    private float overlay(long now) {
        float t = Math.max(0, Math.min(1, (now - changedAt) / 1000f));
        float eased = t < .5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
        return fromOverlay + (targetOverlay - fromOverlay) * eased;
    }

    Color ambient() {
        return ambient;
    }

    Color modelTint() {
        return modelTint;
    }

    void draw(
            long now,
            EncounterScene scene,
            GdxTwlRenderer renderer,
            int x,
            int y,
            int width,
            int height) {
        if (particles == null) return;
        float delta = drawnAt < 0 ? 0 : Math.max(0, (now - drawnAt) / 1000f);
        drawnAt = now;
        renderer.clipEnter(x, y, width, height);
        try {
            particles.draw(renderer, delta, x, y, width, height, scene.camera());
        } finally {
            renderer.clipLeave();
        }
    }

    public void close() {
        if (particles != null) particles.close();
        particles = null;
        weather = null;
        drawnAt = -1;
        fromOverlay = targetOverlay = 0;
    }
}
