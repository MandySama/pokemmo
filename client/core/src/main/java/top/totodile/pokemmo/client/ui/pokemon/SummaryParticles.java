package top.totodile.pokemmo.client.ui.pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

import java.util.*;
import java.util.zip.ZipInputStream;

/** PC projection of ParticleManager's special/type_N: official APS sprites and emitter curves. */
final class SummaryParticles extends Widget {
    private final PokemonController controller;
    private final boolean storage;
    private AttributeParticleScene scene;
    private int type = -1, loadedType = -1;
    private final java.util.concurrent.CompletableFuture<Map<Integer, JsonValue>> definitions;

    SummaryParticles(PokemonController controller) {
        this(controller, false);
    }

    SummaryParticles(PokemonController controller, boolean storage) {
        this.storage = storage;
        this.controller = controller;
        definitions = controller.typeEffects();
        setTheme("");
        setClip(true);
    }

    void type(int romType) {
        int next = romType >= 9 ? romType + 1 : romType;
        if (next == type) return;
        type = next;
        loadedType = -1;
        if (scene != null) {
            scene.close();
            scene = null;
        }
    }

    private void loadType() {
        if (type < 0 || loadedType == type || !definitions.isDone()) return;
        loadedType = type;
        var effect = definitions.join().get(type);
        if (effect == null)
            throw new IllegalStateException("Missing official attribute effect " + type);
        scene = new AttributeParticleScene(controller, effect);
    }

    /**
     * ResourceData's type effects, read once off the render thread and shared by PC and summary.
     */
    static Map<Integer, JsonValue> readEffects(com.badlogic.gdx.files.FileHandle resource)
            throws java.io.IOException {
        var result = new HashMap<Integer, JsonValue>();
        try (var zip = new ZipInputStream(resource.read())) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                String name = entry.getName(), prefix = "particle/special/type_";
                if (name.startsWith(prefix) && name.endsWith(".vfx")) {
                    try {
                        int id =
                                Integer.parseInt(
                                        name.substring(prefix.length(), name.length() - 4));
                        if (id >= 0 && id < 18)
                            result.put(
                                    id,
                                    new JsonReader()
                                            .parse(
                                                    new String(
                                                            zip.readAllBytes(),
                                                            java.nio.charset.StandardCharsets
                                                                    .UTF_8)));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return Map.copyOf(result);
    }

    static JsonValue readEffect(com.badlogic.gdx.files.FileHandle resource, int type)
            throws java.io.IOException {
        // ClientResources handles may represent bundled/overlaid assets, not a working-directory
        // file.
        try (var zip = new ZipInputStream(resource.read())) {
            String path = "particle/special/type_" + type + ".vfx";
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().equals(path))
                    return new JsonReader()
                            .parse(
                                    new String(
                                            zip.readAllBytes(),
                                            java.nio.charset.StandardCharsets.UTF_8));
            }
            throw new java.io.IOException("Missing official effect " + path);
        }
    }

    void reset() {
        if (scene != null) scene.reset();
    }

    @Override
    protected void paintWidget(GUI gui) {
        if (!(gui.getRenderer() instanceof GdxTwlRenderer renderer)) return;
        loadType();
        if (scene != null)
            scene.draw(
                    renderer,
                    Gdx.graphics.getDeltaTime(),
                    getX(),
                    getY(),
                    getWidth(),
                    getHeight(),
                    type,
                    storage);
    }

    @Override
    protected boolean handleEvent(Event event) {
        return false;
    }
}
