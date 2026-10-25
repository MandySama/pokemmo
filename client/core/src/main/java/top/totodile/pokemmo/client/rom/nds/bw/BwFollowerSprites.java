package top.totodile.pokemmo.client.rom.nds.bw;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;

import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;
import top.totodile.pokemmo.client.rom.nds.model.NitroTextureDictionary;
import top.totodile.pokemmo.server.game.GameService.Follower;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** BlackWhiteRom.Z10 and PokemonSpeciesGrowthRate.U: original walking textures, not party icons. */
public final class BwFollowerSprites {
    private final RomInfo rom;
    private final Map<Integer, Integer> modelIds = new HashMap<>();
    private NdsArchive models;
    private final FileHandle shinyPalettes;
    private final java.util.function.Function<String, FileHandle> assets;
    private boolean atlasLoaded;
    private int rows, columns;
    private final int[][] directions = new int[4][];
    private final Map<Integer, float[]> adjustments = new HashMap<>();
    private byte[] shinyPatch;

    public BwFollowerSprites(RomInfo rom, java.util.function.Function<String, FileHandle> assets)
            throws IOException {
        this.rom = rom;
        this.assets = assets;
        this.shinyPalettes = assets.apply("sprites/sp.pak");
        var mapping = new NdsArchive(RomReader.readFile(rom, "a/0/4/8"));
        var bytes = ByteBuffer.wrap(mapping.read(0)).order(ByteOrder.LITTLE_ENDIAN);
        int count = bytes.getInt();
        for (int i = 0; i < count; i++) {
            int at = bytes.position(),
                    id = Short.toUnsignedInt(bytes.getShort()),
                    model = Short.toUnsignedInt(bytes.getShort(at + 16));
            modelIds.put(id, model);
            bytes.position(at + 28);
        }
    }

    public boolean available(Follower follower) {
        return follower.species() > 0
                && (custom(follower) != null || modelIds.containsKey(key(follower)));
    }

    private FileHandle custom(Follower follower) {
        String suffix = follower.shiny() ? "s" : "n";
        for (String gender : new String[] {follower.gender() == 1 ? "f" : "m", "b"})
            for (String form :
                    follower.form() == 0
                            ? new String[] {"", "-0"}
                            : new String[] {"-" + follower.form()}) {
                var file =
                        assets.apply(
                                "sprites/followsprites/"
                                        + follower.species()
                                        + "-"
                                        + gender
                                        + "-"
                                        + suffix
                                        + form
                                        + ".png");
                if (file.exists()) return file;
            }
        return null;
    }

    private void atlas() throws IOException {
        if (atlasLoaded) return;
        var file = assets.apply("sprites/followsprites/atlasdata.txt");
        if (file.exists())
            for (String line : file.readString("UTF-8").split("\\R")) {
                if (line.strip().startsWith(";") || !line.contains("=")) continue;
                String[] parts = line.split("=", 2);
                String key = parts[0].strip().toLowerCase(Locale.ROOT), value = parts[1].strip();
                try {
                    if (key.equals("rows")) rows = Math.min(8, Integer.parseInt(value));
                    else if (key.equals("columns")) columns = Math.min(8, Integer.parseInt(value));
                    else {
                        int direction =
                                switch (key) {
                                    case "north" -> 0;
                                    case "south" -> 1;
                                    case "west" -> 2;
                                    case "east" -> 3;
                                    default -> -1;
                                };
                        String[] values = value.split(",");
                        if (direction >= 0) {
                            int[] frames = new int[values.length];
                            for (int i = 0; i < frames.length; i++)
                                frames[i] = Integer.parseInt(values[i].strip());
                            directions[direction] = frames;
                        } else {
                            float[] offsets = new float[4];
                            for (int i = 0; i < Math.min(4, values.length); i++)
                                offsets[i] = Float.parseFloat(values[i].strip());
                            adjustments.put(Integer.parseInt(key), offsets);
                        }
                    }
                } catch (NumberFormatException invalid) {
                    throw new IOException("Invalid follower atlas value: " + line, invalid);
                }
            }
        atlasLoaded = true;
    }

    public int[][] directions(Follower follower) throws IOException {
        if (custom(follower) != null) {
            atlas();
            for (var direction : directions)
                if (direction == null || direction.length < 2)
                    throw new IOException("Missing follower direction frames");
            return directions;
        }
        int[] offsets =
                nativeDirections(follower) ? new int[] {3, 0, 6, 9} : new int[] {2, 0, 4, 6};
        int[][] result = new int[4][];
        for (int i = 0; i < 4; i++)
            result[i] = new int[] {offsets[i], offsets[i] + 1, offsets[i], offsets[i] + 1};
        return result;
    }

    public float[] adjustments(Follower follower) throws IOException {
        atlas();
        return adjustments.getOrDefault(follower.species(), new float[4]);
    }

    private int key(Follower follower) {
        int nativeId = FollowerSpriteIds.nativeModel(follower.form(), (short) follower.species());
        int model =
                FollowerSpriteIds.modelId(
                        (short) follower.species(), follower.gender() == 1, false, follower.form());
        return nativeId > 0 ? nativeId : model > 0 ? 4095 + model : -1;
    }

    public List<Pixmap> frames(Follower follower) throws IOException {
        var custom = custom(follower);
        if (custom != null) {
            atlas();
            if (rows < 1 || columns < 1)
                throw new IOException("Missing follower atlas rows/columns");
            var image = new Pixmap(custom);
            var frames = new ArrayList<Pixmap>();
            try {
                int width = image.getWidth() / rows, height = image.getHeight() / columns;
                if (width < 1 || height < 1)
                    throw new IOException("Invalid follower atlas dimensions");
                for (int column = 0; column < columns; column++)
                    for (int row = 0; row < rows; row++) {
                        var frame = new Pixmap(width, height, Pixmap.Format.RGBA8888);
                        frame.setBlending(Pixmap.Blending.None);
                        frame.drawPixmap(image, 0, 0, row * width, column * height, width, height);
                        frames.add(frame);
                    }
                return frames;
            } catch (IOException | RuntimeException failure) {
                frames.forEach(Pixmap::dispose);
                throw failure;
            } finally {
                image.dispose();
            }
        }
        Integer model = modelIds.get(key(follower));
        if (model == null) throw new IOException("Missing follower sprite");
        if (models == null) models = new NdsArchive(RomReader.readFile(rom, "a/0/4/9"));
        var dictionary = new NitroTextureDictionary(models.read(model));
        if (follower.shiny()) {
            if (shinyPatch == null) {
                shinyPatch = new byte[0];
                if (shinyPalettes.exists()) {
                    byte[] encoded = shinyPalettes.readBytes();
                    try (var stream =
                            encoded.length > 2
                                            && (encoded[0] & 255) == 31
                                            && (encoded[1] & 255) == 139
                                    ? new GZIPInputStream(new ByteArrayInputStream(encoded))
                                    : new ByteArrayInputStream(encoded)) {
                        shinyPatch = stream.readAllBytes();
                    }
                }
            }
            dictionary.xorFirstPalette(shinyPatch, model);
        }
        if (dictionary.paletteNames().isEmpty()) throw new IOException("Missing follower palette");
        String palette = dictionary.paletteNames().iterator().next();
        var frames = new ArrayList<Pixmap>();
        try {
            for (String texture : dictionary.names())
                frames.add(dictionary.pixels(texture, palette, false, false));
            return frames;
        } catch (IOException | RuntimeException error) {
            frames.forEach(Pixmap::dispose);
            throw error;
        }
    }

    public static boolean nativeDirections(Follower follower) {
        return FollowerSpriteIds.nativeModel(follower.form(), (short) follower.species()) > 0;
    }
}
