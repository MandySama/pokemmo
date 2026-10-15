package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.*;
import top.totodile.pokemmo.*;
import top.totodile.pokemmo.rom.*;
import top.totodile.pokemmo.protocol.ItemCatalog;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

/** CPU 解码在后台完成；仅 GUI 线程创建和销毁道具、种族图集。 */
final class CatalogIcons implements AutoCloseable {
    private record Pixels(BufferedImage items, BufferedImage pokemon) { }
    private CompletableFuture<Pixels> pending;
    private Texture items, pokemon;
    private final java.util.Map<Integer, Image> itemImages = new java.util.HashMap<>();
    private final Image[] pokemonImages = new Image[top.totodile.pokemmo.protocol.PokemonCatalog.MAX_SPECIES + 1];
    void open() {
        if (pending != null || items != null) return;
        pending = CompletableFuture.supplyAsync(() -> {
            try {
                var rom = new RomFiles(ClientFiles.data("../roms")).selected(0);
                var source = new ItemIconSources(new RomFiles(ClientFiles.data("../roms")));
                var atlas = new BufferedImage(1536, (ItemCatalog.items().size() + 63) / 64 * 24, BufferedImage.TYPE_INT_ARGB);
                var graphics = atlas.createGraphics();
                graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                int index = 0;
                try {
                    for (var item : ItemCatalog.items()) {
                        var image = source.image(item);
                        graphics.drawImage(image, index % 64 * 24, index / 64 * 24, 24, 24, null);
                        index++;
                    }
                } finally { graphics.dispose(); }
                return new Pixels(atlas, rom == null ? null : PokedexIcons.load(rom));
            } catch (java.io.IOException exception) {
                System.getLogger(CatalogIcons.class.getName()).log(System.Logger.Level.WARNING, "Unable to load catalog icons", exception);
                return null;
            }
        });
    }
    Image image(GUI gui, boolean isPokemon, int id) {
        if (pending != null && pending.isDone()) {
            var pixels = pending.getNow(null);
            pending = null;
            if (pixels != null) {
                var renderer = (GlfwTwlRenderer) gui.getRenderer();
                items = renderer.uploadImage(pixels.items());
                if (pixels.pokemon() != null) pokemon = renderer.uploadImage(pixels.pokemon());
                int index = 0;
                for (var item : ItemCatalog.items()) {
                    itemImages.put(item.id(), items.getImage(index % 64 * 24, index / 64 * 24, 24, 24, Color.WHITE, false, Texture.Rotation.NONE));
                    index++;
                }
                if (pokemon != null) for (int i = 1; i <= top.totodile.pokemmo.protocol.PokemonCatalog.MAX_SPECIES; i++) pokemonImages[i] = pokemon.getImage(1024 + (i - 1) % 32 * 32, (i - 1) / 32 * 32, 32, 32, Color.WHITE, false, Texture.Rotation.NONE);
            }
        }
        return isPokemon ? pokemonImages[id] : itemImages.get(id);
    }
    public void close() {
        if (pending != null) pending.cancel(true);
        pending = null;
        if (items != null) items.destroy();
        if (pokemon != null) pokemon.destroy();
        items = pokemon = null;
        itemImages.clear();
        java.util.Arrays.fill(pokemonImages, null);
    }
}
