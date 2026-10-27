package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import de.matthiasmann.twl.renderer.Image;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;
import java.io.IOException;
import java.util.List;

/** Presentation-only NPC side. Gdx3DModelBatch.w9 cases 1/5 precede the player's case 7. */
final class NpcEncounter implements AutoCloseable {
    static final long INTRO_DURATION = 3500;
    final BwNpcTrainer trainer;
    List<Pokemon> team;
    private final Texture texture;
    private final Image image;
    private final BwPokedex dex;
    private final PokedexImages images;
    private final ClientResources resources;
    private final GdxTwlRenderer renderer;
    EncounterEntrance entrance;
    int index;
    final java.util.Set<Integer> revealed = new java.util.HashSet<>();
    long spawnAt = -1, faintAt = -1;
    long faintDropAt = -1;
    boolean exhausted;
    String text = "";
    boolean loggedSendOut;

    NpcEncounter(BwPokedex dex, PokedexImages images, ClientResources resources,
            GdxTwlRenderer renderer, List<Pokemon> team) throws IOException {
        this.dex = dex; this.images = images; this.resources = resources;
        this.renderer = renderer; this.team = List.copyOf(team);
        trainer = BwNpcTrainer.random(dex, java.util.concurrent.ThreadLocalRandom.current());
        var pixels = trainer.pixels();
        var pixmap = new Pixmap(pixels.width(), pixels.height(), Pixmap.Format.RGBA8888);
        try {
            pixmap.setBlending(Pixmap.Blending.None);
            for (int y = 0; y < pixels.height(); y++)
                for (int x = 0; x < pixels.width(); x++)
                    pixmap.drawPixel(x, y, pixels.rgba()[y * pixels.width() + x]);
            texture = new Texture(pixmap);
        } finally { pixmap.dispose(); }
        image = renderer.image(texture);
        try { loadEntrance(); } catch (IOException | RuntimeException error) {
            texture.dispose(); throw error;
        }
    }

    void loadEntrance() throws IOException {
        var replacement = new EncounterEntrance(null, 0, 0, new BwPokemonSprites(dex),
                team.get(index), images, resources, renderer, true);
        if (entrance != null) entrance.close();
        entrance = replacement;
        text = dex.npcSendOut(trainer.type(), trainer.name(), dex.name(team.get(index).species()));
    }

    boolean busy() { return faintAt >= 0 || spawnAt >= 0; }

    long elapsed(long now, long intro) {
        return spawnAt >= 0 ? EncounterEntrance.WILD + now - spawnAt : intro;
    }

    void drawTrainer(EncounterScene scene, long elapsed) {
        if (elapsed >= 3500) return;
        float retreat = Math.max(0, Math.min(1, (elapsed - 2500) / 1000f));
        retreat *= retreat;
        // PlatinumBattleSceneLoader.Dt: front portrait at enemy slot + .25, -.025;
        // w9 case 5 moves it +.5 X / -1 Z before opening the ball.
        var center = new Vector3(5.25f + .5f * retreat, 3.055f, 1 - retreat);
        renderer.drawBattleCombatant(image, center, .8f, .8f, scene.camera(), 0, 0, 0,
                .75f * (1 - Math.max(0, Math.min(1, elapsed / 1500f))));
    }

    public void close() { entrance.close(); texture.dispose(); }
}
