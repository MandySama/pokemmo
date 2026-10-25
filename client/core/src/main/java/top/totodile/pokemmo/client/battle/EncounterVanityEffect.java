package top.totodile.pokemmo.client.battle;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;

import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.resources.ParticleResources;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.client.ui.pokemon.AttributeParticleScene;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * BattleParticleVanityAction / ParticleVanityMapping: selected entrance particles, independent of
 * ball release and move animation. The reference starts these after appearance, without waiting
 * before enabling commands. Audio is intentionally omitted.
 */
final class EncounterVanityEffect implements AutoCloseable {
    private final List<Texture> textures = new ArrayList<>();
    private AttributeParticleScene particles;
    private EncounterMoveAnimation shiny;
    private long started = -1;

    EncounterVanityEffect(
            ClientResources resources,
            PokedexImages images,
            GdxTwlRenderer renderer,
            Pokemon pokemon,
            boolean enemy,
            Vector3 origin)
            throws IOException {
        if (pokemon.egg()) return;
        int selected = pokemon.battleParticle();
        if (selected == 0 && !pokemon.secretShiny()) {
            shiny = EncounterMoveAnimation.shinyEntrance(resources, images, origin);
            return;
        }
        String asset = asset(selected, enemy);
        if (asset == null) return;
        try {
            var effect = new JsonReader().parse(read(resources, "custom/" + asset + ".vfx"));
            particles = new AttributeParticleScene(images, effect, origin);
            if (selected == 26 || selected == 35 || selected == 36) {
                String prefix =
                        selected == 26
                                ? "way_of_the_dragon"
                                : selected == 36 ? "shiny_phantom" : "phantom";
                int count = selected == 26 ? 24 : 41;
                List<Image> frames = new ArrayList<>();
                for (int i = 1; i <= count; i++) {
                    String path = "sprites/" + prefix + "/" + i + ".png";
                    byte[] bytes = ParticleResources.read(resources, path);
                    var pixels = new com.badlogic.gdx.graphics.Pixmap(bytes, 0, bytes.length);
                    try {
                        var texture = new Texture(pixels);
                        textures.add(texture);
                        frames.add(renderer.image(texture));
                    } finally {
                        pixels.dispose();
                    }
                }
                // DragonAnimatedRegionInfluencer: 24 forward frames, four reverse, seven held.
                if (selected == 26) {
                    for (int i = 24; i < 28; i++) frames.add(frames.get(46 - i));
                    for (int i = 28; i < 35; i++) frames.add(frames.get(18));
                }
                particles.particleImages(
                        (name, index, seconds, life) ->
                                "dummy".equals(name)
                                        ? frames.get(
                                                Math.min(
                                                        frames.size() - 1,
                                                        (int) (life * frames.size())))
                                        : null);
            } else if (selected >= 28 && selected <= 31) {
                boolean alternate = selected == 29 || selected == 31;
                boolean anniversary = selected <= 29;
                particles.particleImages(
                        (name, index, seconds, life) -> {
                            if (anniversary && index < 3)
                                return images.animated(
                                        new int[] {1, 4, 7}[index], 0, alternate, !enemy, seconds);
                            if (!anniversary && "spirit".equals(name))
                                return images.animated(442, 0, alternate, !enemy, seconds);
                            return null;
                        });
            }
        } catch (IOException | RuntimeException failure) {
            close();
            throw failure;
        }
    }

    static String read(ClientResources resources, String path) throws IOException {
        return new String(
                ParticleResources.read(resources, "particle/" + path), StandardCharsets.UTF_8);
    }

    /**
     * IDs are ParticleVanity IDs, not move IDs. Side suffixes match the individual reference
     * classes.
     */
    static String asset(int id, boolean enemy) {
        String side = enemy ? "_enemy" : "_friendly";
        return switch (id) {
            case -3, -2, -1, 1 -> null;
            case 0 -> "spawn_secret_shiny";
            case 2, 34 -> "spawn_hitodama";
            case 3 -> "spawn_bats";
            case 4 -> "spawn_ghost";
            case 5 -> "spawn_eye";
            case 6 -> "spawn_pumpkings_delight";
            case 7 -> "spawn_rising_star";
            case 8 -> "spawn_snowflakes";
            case 9 -> "spawn_present";
            case 10 -> "spawn_lanterns";
            case 11 -> "spawn_fireworks";
            case 12 -> "spawn_pungent_stench";
            case 13 -> "spawn_black_hole";
            case 14 -> "spawn_witchs_haze";
            case 15 -> "spawn_pumpkids_treat";
            case 16 -> "spawn_spiders";
            case 17 -> "spawn_white_cat" + side;
            case 18 -> "spawn_black_cat" + side;
            case 19 -> "spawn_scythe" + side;
            case 20 -> "spawn_zodiac" + side;
            case 21 -> "spawn_zombie_hands" + side;
            case 22 -> "spawn_pumpcat" + side;
            case 23 -> "spawn_eerie_howl" + side;
            case 24 -> "spawn_graveyard" + side;
            case 25 -> "spawn_fall_of_the_king" + side;
            case 26 -> "way_of_the_dragon" + (enemy ? "_enemy" : "");
            case 27 -> "spawn_cake" + side;
            case 28 -> "spawn_10th_anniv_friends" + side;
            case 29 -> "spawn_10th_anniv_friends_shiny" + side;
            // SpiritombParticleEffect and SpiritOfSpringParticleEffect use fixed variants.
            case 30 -> "spawn_menacing_spirit_enemy";
            case 31 -> "spawn_shiny_menacing_spirit_enemy";
            case 32 -> "spawn_xmas_wires";
            case 33 -> "spawn_spirit_of_spring_friendly";
            case 35 -> "spawn_phantom" + (enemy ? "_enemy" : "");
            case 36 -> "spawn_shiny_phantom" + (enemy ? "_enemy" : "");
            case 37 -> "spawn_dark_crystal" + side;
            case 38 -> "spawn_dark_crystal_shiny" + side;
            default -> throw new IllegalArgumentException("Unknown selected particle " + id);
        };
    }

    void draw(
            long now,
            EncounterScene scene,
            GdxTwlRenderer renderer,
            int x,
            int y,
            int width,
            int height) {
        if (started < 0) started = now;
        long elapsed = now - started;
        if (particles != null && !particles.isComplete())
            particles.drawAt(renderer, elapsed / 1000f, x, y, width, height, scene.camera());
        if (shiny != null) {
            shiny.update(elapsed);
            shiny.draw(renderer, scene, elapsed, x, y, width, height);
        }
    }

    @Override
    public void close() {
        if (particles != null) particles.close();
        particles = null;
        if (shiny != null) shiny.close();
        shiny = null;
        textures.forEach(Texture::dispose);
        textures.clear();
    }
}
