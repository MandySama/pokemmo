package top.totodile.pokemmo.client.rom.gba;

import top.totodile.pokemmo.client.rom.*;

import java.io.IOException;
import java.util.*;

/** GdxParticleController's region-0 sprite 159, loaded through the reference GBA sprite tables. */
public final class GbaSurfSprites {
    public record Frame(int width, int height, int[] rgba) {}

    public static List<Frame> load(RomInfo rom) throws IOException {
        if (rom == null) return List.of();
        int[] tables = tables(rom);
        return load(rom, tables);
    }

    static int[] tables(RomInfo rom) throws IOException {
        // GbaMultilingualRomRegistry: rK (99), ZV (100), BJ0 (101).
        return
                switch (rom.code()) {
                    case "BPRE" ->
                            rom.revision() == 1
                                    ? new int[] {3800608, 3822024, 3822512}
                                    : new int[] {3800496, 3821912, 3822400};
                    case "BPRD" -> new int[] {3800180, 3821596, 3822084};
                    case "BPRF" -> new int[] {3776928, 3798344, 3798832};
                    case "BPRI" -> new int[] {3771952, 3793368, 3793856};
                    case "BPRS" -> new int[] {3781912, 3803328, 3803816};
                    default ->
                            throw new IOException(
                                    "Missing reference Surf sprite offsets for " + rom.code());
                };
    }

    private static List<Frame> load(RomInfo rom, int[] tables) throws IOException {
        byte[] data = java.nio.file.Files.readAllBytes(rom.path());
        int record = pointer(data, tables[0] + 159 * 4);
        if (data[record + 3] != (byte) 255) throw new IOException("Invalid Surf sprite descriptor");
        int paletteId = data[record + 2] + 26, frameTable = pointer(data, record + 12);
        var palettes = new HashMap<Integer, Integer>();
        for (int section = 1; section < 3; section++)
            for (int i = 0; i < 64; i++) {
                int row = tables[section] + i * 8, address = u32(data, row);
                if ((address & 0xfe000000) != 0x08000000) break;
                palettes.put((int) data[row + 4] + (section == 2 ? 26 : 0), address & 0x1ffffff);
            }
        Integer palette = palettes.getOrDefault(paletteId, palettes.get(0));
        if (palette == null) throw new IOException("Missing Surf palette");
        var result = new ArrayList<Frame>();
        for (int frame = 0; frame < 6; frame++) {
            int pixels = pointer(data, frameTable + frame * 8);
            int[] rgba = new int[1024];
            for (int y = 0; y < 32; y++)
                for (int x = 0; x < 32; x++) {
                    int value =
                            data[pixels + ((y / 8) * 4 + x / 8) * 32 + (y & 7) * 4 + (x & 7) / 2]
                                            >> ((x & 1) * 4)
                                    & 15;
                    if (value == 0) continue;
                    int color =
                            (data[palette + value * 2] & 255)
                                    | ((data[palette + value * 2 + 1] & 255) << 8);
                    rgba[y * 32 + x] =
                            ((color & 31) * 255 / 31) << 24
                                    | ((color >> 5 & 31) * 255 / 31) << 16
                                    | ((color >> 10 & 31) * 255 / 31) << 8
                                    | 255;
                }
            result.add(new Frame(32, 32, rgba));
        }
        return List.copyOf(result);
    }

    private static int u32(byte[] data, int offset) {
        return (data[offset] & 255)
                | ((data[offset + 1] & 255) << 8)
                | ((data[offset + 2] & 255) << 16)
                | (data[offset + 3] << 24);
    }

    private static int pointer(byte[] data, int offset) throws IOException {
        int value = u32(data, offset);
        if ((value & 0xfe000000) != 0x08000000 || (value & 0x1ffffff) >= data.length)
            throw new IOException("Invalid Surf sprite pointer");
        return value & 0x1ffffff;
    }
}
