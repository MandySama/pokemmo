package top.totodile.pokemmo.client.rom.nds.bw;

import java.io.IOException;
import java.nio.*;
import java.util.*;

/**
 * D:/PokeMMO revision 32920, data/sprites/sp-icon.pak: palette XOR and per-frame pixel corrections.
 */
final class BwShinyIcons {
    private record Pixel(int frame, int x, int y, int palette, int index) {}

    private record Patch(Map<Integer, Integer> xor, int[] extra, List<Pixel> pixels) {}

    private final Map<Integer, Patch> patches = new HashMap<>();

    BwShinyIcons(byte[] bytes) throws IOException {
        var data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        try {
            while (data.hasRemaining()) {
                int id = Short.toUnsignedInt(data.getShort()),
                        gender = Byte.toUnsignedInt(data.get());
                if (id < 1 || id > 711 || gender > 1)
                    throw new IOException("Invalid shiny icon key");
                int count = Byte.toUnsignedInt(data.get());
                var xor = new LinkedHashMap<Integer, Integer>();
                for (int i = 0; i < count; i++) {
                    int index = Byte.toUnsignedInt(data.get());
                    int delta = data.getInt();
                    if (index >= 16 || (delta & 255) != 0)
                        throw new IOException("Invalid shiny icon palette delta");
                    xor.put(index, delta);
                }
                int[] extra = new int[Byte.toUnsignedInt(data.get())];
                for (int i = 0; i < extra.length; i++) extra[i] = data.getInt();
                var pixels = new ArrayList<Pixel>();
                count = Byte.toUnsignedInt(data.get());
                for (int i = 0; i < count; i++) {
                    int frame = Byte.toUnsignedInt(data.get()),
                            x = Byte.toUnsignedInt(data.get()),
                            y = Byte.toUnsignedInt(data.get()),
                            palette = Byte.toUnsignedInt(data.get()),
                            index = Byte.toUnsignedInt(data.get());
                    if (frame > 1
                            || x >= 36
                            || y >= 36
                            || palette > 2
                            || index >= (palette == 2 ? extra.length : 16))
                        throw new IOException("Invalid shiny icon pixel correction");
                    pixels.add(new Pixel(frame, x, y, palette, index));
                }
                if (patches.put(
                                id | gender << 16,
                                new Patch(Map.copyOf(xor), extra, List.copyOf(pixels)))
                        != null) throw new IOException("Duplicate shiny icon patch");
            }
        } catch (BufferUnderflowException error) {
            throw new IOException("Truncated shiny icon patch", error);
        }
    }

    private Patch patch(int id, int gender) {
        return patches.getOrDefault(id | Math.max(0, gender) << 16, patches.get(id));
    }

    int[] palette(int id, int gender, int[] original) {
        int[] result = original.clone();
        var patch = patch(id, gender);
        if (patch != null) patch.xor.forEach((index, delta) -> result[index] ^= delta);
        return result;
    }

    BwPokemonSprites.Pixels pixels(
            int id,
            int gender,
            int frame,
            BwPokemonSprites.Pixels image,
            int[] original,
            int[] shiny) {
        var patch = patch(id, gender);
        if (patch == null || patch.pixels.isEmpty()) return image;
        int[] rgba = image.rgba().clone();
        for (var pixel : patch.pixels)
            if (pixel.frame == frame)
                rgba[pixel.y * image.width() + pixel.x] =
                        switch (pixel.palette) {
                            case 0 -> original[pixel.index];
                            case 1 -> shiny[pixel.index];
                            default -> patch.extra[pixel.index];
                        };
        return new BwPokemonSprites.Pixels(image.width(), image.height(), rgba);
    }
}
