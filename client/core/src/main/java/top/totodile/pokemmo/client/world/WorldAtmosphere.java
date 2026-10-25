package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.JsonReader;

import top.totodile.pokemmo.client.resources.*;
import top.totodile.pokemmo.client.rom.RomType;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.client.ui.pokemon.AttributeParticleScene;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

/** WorldTimeManager / ColorPaletteResource and the map managers' native weather VFX. */
public final class WorldAtmosphere implements AutoCloseable {
    public enum Weather {
        AUTO("自动", null),
        CLEAR("晴天", null),
        RAIN("雨天", "rain"),
        THUNDER("雷雨", "rain"),
        HEAVY_RAIN("暴雨", "heavy_rain"),
        SNOW("下雪", "snow"),
        HEAVY_SNOW("暴雪", "heavy_snow"),
        HAIL("冰雹", "hail"),
        SANDSTORM("沙暴", "sandstorm"),
        ASH("火山灰", "ash");
        public final String label;
        final String effect;

        Weather(String label, String effect) {
            this.label = label;
            this.effect = effect;
        }

        boolean thunder() {
            return this == THUNDER || this == HEAVY_RAIN;
        }
    }

    private static final int[] HOURS = {-1, 8, 12, 19, 22, 2};
    private static final String[] TIMES = {"自动", "早晨", "白天", "黄昏", "夜晚", "深夜"};
    private int timeIndex;
    private Weather selected = Weather.AUTO, active;
    private ClientResources resources;
    private GdxTwlRenderer renderer;
    private PokedexImages images;
    private AttributeParticleScene weather, thunder;
    private long nextThunder;
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private Texture pixel;
    private String failure;

    public void configure(ClientResources resources, GdxTwlRenderer renderer) {
        this.resources = resources;
        this.renderer = renderer;
    }

    public String timeLabel() {
        return TIMES[timeIndex];
    }

    public String weatherLabel() {
        return selected.label;
    }

    public int forcedHour() {
        return HOURS[timeIndex];
    }

    public void nextTime() {
        timeIndex = (timeIndex + 1) % HOURS.length;
    }

    public void nextWeather() {
        selected = Weather.values()[(selected.ordinal() + 1) % Weather.values().length];
    }

    public Weather resolvedWeather(Weather nativeWeather) {
        return selected == Weather.AUTO ? nativeWeather : selected;
    }

    public long seconds(long millis, long origin) {
        return forcedHour() < 0 ? (Math.floorDiv(millis, 1000) - origin) * 4 : forcedHour() * 3600L;
    }

    /** GdxParticleSystemRenderer: native dawn/dusk ramps, ColorPaletteResource.Ru0 factors 2/4. */
    Color light(long seconds, boolean outside) {
        if (!outside) return Color.WHITE;
        int minute = (int) Math.floorMod(Math.floorDiv(seconds, 60), 1440);
        int value = minute / 60 * 100 + (int) ((minute % 60) * 1.65);
        int darkness =
                value >= 700 && value <= 2000
                        ? 0
                        : value > 2000 && value < 2255
                                ? value - 2000
                                : value > 445 && value < 700 ? 255 - (value - 445) : 255;
        return new Color(1 - darkness / 510f, 1 - darkness / 510f, 1 - darkness / 1020f, 1);
    }

    public void draw(
            SpriteBatch batch,
            float delta,
            int width,
            int height,
            long seconds,
            boolean outside,
            Weather nativeWeather,
            float tileX,
            float tileY) {
        Color light = light(seconds, outside);
        if (!light.equals(Color.WHITE)) {
            if (pixel == null) {
                var pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(Color.WHITE);
                pixmap.fill();
                pixel = new Texture(pixmap);
                pixmap.dispose();
            }
            var projection = new Matrix4(batch.getProjectionMatrix());
            batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, width, height));
            batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
            batch.setColor(light);
            batch.begin();
            batch.draw(pixel, 0, 0, width, height);
            batch.end();
            batch.setColor(Color.WHITE);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setProjectionMatrix(projection);
        }
        Weather desired = selected == Weather.AUTO ? nativeWeather : selected;
        if (resources == null || renderer == null) return;
        try {
            if (desired != active) {
                clearEffects();
                active = desired;
                failure = null;
                if (desired.effect != null) weather = load(desired.effect);
                if (desired.thunder())
                    nextThunder = System.currentTimeMillis() + randomThunderDelay();
            }
            if (weather == null) return;
            // GdxMapEnvironmentRenderer: 35 degrees, distance 7.5, pitch 56; .25 per tile.
            camera.fieldOfView = 35;
            camera.near = .01f;
            camera.far = 100;
            camera.up.set(Vector3.Y);
            camera.position.set(
                    tileX * .25f,
                    7.5f * MathUtils.sinDeg(56),
                    tileY * .25f + 7.5f * MathUtils.cosDeg(56));
            camera.lookAt(tileX * .25f, 0, tileY * .25f);
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
            // BattleFieldStageController uses OverworldCamera.rj (look target), not its position.
            var target = new Vector3(tileX * .25f, 0, tileY * .25f);
            weather.origin(target);
            long now = System.currentTimeMillis();
            if (desired.thunder() && now >= nextThunder) {
                if (thunder != null) thunder.close();
                thunder = load("thunder");
                nextThunder = now + randomThunderDelay();
            }
            renderer.startRendering();
            try {
                weather.draw(renderer, delta, 0, 0, width, height, camera);
                if (thunder != null) {
                    thunder.origin(target);
                    thunder.draw(renderer, delta, 0, 0, width, height, camera);
                    if (thunder.isComplete()) {
                        thunder.close();
                        thunder = null;
                    }
                }
            } finally {
                renderer.endRendering();
            }
        } catch (IOException | RuntimeException error) {
            clearEffects();
            if (!desired.name().equals(failure)) {
                failure = desired.name();
                com.badlogic.gdx.Gdx.app.error(
                        "World", "Unable to load native weather " + desired, error);
            }
        }
    }

    private long randomThunderDelay() {
        return ThreadLocalRandom.current().nextLong(25000, 45001);
    }

    private AttributeParticleScene load(String name) throws IOException {
        if (images == null) {
            var rom = resources.roms().get(RomType.BLACK_WHITE);
            if (rom == null)
                throw new IOException("Black/White ROM required for native weather particles");
            var dex = new BwPokedex(rom, resources.strings());
            images = new PokedexImages(new BwPokemonSprites(dex), renderer, resources);
        }
        var json =
                new JsonReader()
                        .parse(
                                new String(
                                        ParticleResources.read(
                                                resources, "particle/weather/" + name + ".vfx"),
                                        StandardCharsets.UTF_8));
        return new AttributeParticleScene(images, json, new Vector3());
    }

    private void clearEffects() {
        if (weather != null) weather.close();
        if (thunder != null) thunder.close();
        weather = null;
        thunder = null;
    }

    public void close() {
        clearEffects();
        if (images != null) images.close();
        images = null;
        if (pixel != null) pixel.dispose();
        pixel = null;
    }
}
