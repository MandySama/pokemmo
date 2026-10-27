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
    private EncounterMoveAnimation effectAnimation;
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
        result.effectAnimation = EncounterMoveAnimation.healing(resources, images, origin);
        return result;
    }

    static BattleSettlementEffect trapping(
            ClientResources resources, PokedexImages images, Vector3 origin, long now, int move)
            throws IOException {
        var result = new BattleSettlementEffect(now, 0);
        result.effectAnimation =
                EncounterMoveAnimation.trappingResidual(resources, images, origin, move);
        result.trapping = true;
        return result;
    }

    private boolean trapping;

    void audioTarget(boolean playerTarget) {
        // Trapping's reference caster is the opposite side; recovery's caster is its target.
        if (effectAnimation != null)
            effectAnimation.audioSide(trapping ? !playerTarget : playerTarget);
    }

    EncounterMoveAnimation animation(long now) {
        if (!trapping || now < started) return null;
        effectAnimation.update(now - started);
        return effectAnimation;
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
        if (effectAnimation != null) {
            effectAnimation.update(elapsed);
            effectAnimation.draw(renderer, scene, elapsed, x, y, width, height);
        }
    }

    boolean complete(long now) {
        return drawn
                && now >= started + delay
                && (particles == null || particles.isComplete())
                && (effectAnimation == null || effectAnimation.complete(now - started));
    }

    @Override
    public void close() {
        if (particles != null) particles.close();
        if (effectAnimation != null) effectAnimation.close();
    }
}
