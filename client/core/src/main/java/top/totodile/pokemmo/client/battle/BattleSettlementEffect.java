package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.client.ui.pokemon.AttributeParticleScene;

import java.io.IOException;

/** Original StatChangeParticleEffect / BattleKs0Animation resources and timelines. */
final class BattleSettlementEffect implements AutoCloseable {
    private AttributeParticleScene particles;
    private EncounterMoveAnimation healing;
    private final long started, delay;
    private boolean drawn;

    static BattleSettlementEffect stat(
            ClientResources resources, PokedexImages images, Vector3 origin, long now, boolean up)
            throws IOException {
        var result = new BattleSettlementEffect(now, 750);
        result.particles =
                new AttributeParticleScene(
                        images,
                        new JsonReader()
                                .parse(
                                        EncounterVanityEffect.read(
                                                resources,
                                                "status/" + (up ? "up" : "down") + ".vfx")),
                        origin);
        return result;
    }

    static BattleSettlementEffect healing(
            ClientResources resources, PokedexImages images, Vector3 origin, long now)
            throws IOException {
        var result = new BattleSettlementEffect(now, 0);
        result.healing = EncounterMoveAnimation.healing(resources, images, origin);
        return result;
    }

    private BattleSettlementEffect(long started, long delay) {
        this.started = started;
        this.delay = delay;
    }

    void draw(
            long now,
            EncounterScene scene,
            GdxTwlRenderer renderer,
            int x,
            int y,
            int width,
            int height) {
        long elapsed = now - started;
        if (elapsed < delay) return;
        drawn = true;
        if (particles != null)
            particles.drawAt(
                    renderer, (elapsed - delay) / 1000f, x, y, width, height, scene.camera());
        if (healing != null) {
            healing.update(elapsed);
            healing.draw(renderer, scene, elapsed, x, y, width, height);
        }
    }

    boolean complete(long now) {
        return drawn
                && now >= started + delay
                && (particles == null || particles.isComplete())
                && (healing == null || healing.complete(now - started));
    }

    @Override
    public void close() {
        if (particles != null) particles.close();
        if (healing != null) healing.close();
    }
}
