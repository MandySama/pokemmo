package top.totodile.pokemmo.rom;

import static top.totodile.pokemmo.rom.NdsArchive.*;
import static top.totodile.pokemmo.rom.NitroData.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;

/** 四向 NPC 从各地区的模型纹理归档读取，CPU 缓存与 GPU 生命周期分离。 */
public final class NdsNpcSprites {
    private record Model(int file, int kind) {}

    private final int region;
    private final NdsArchive archive;
    private final Map<Integer, Model> models = new HashMap<>();
    private final Map<Long, BufferedImage> cache = new LinkedHashMap<>(64, .75f, true);

    public NdsNpcSprites(Path rom, int region) throws IOException {
        this.region = region;
        archive =
                new NdsArchive(
                        rom,
                        region == 2
                                ? "/a/0/4/9"
                                : region == 3 ? "/data/mmodel/mmodel.narc" : "/a/0/8/1");
        if (region == 2) {
            ByteBuffer map = new NdsArchive(rom, "/a/0/4/8").file(0);
            int count = map.getInt();
            for (int i = 0; i < count; i++) {
                int start = map.position();
                int id = ushort(map, start), file = ushort(map, start + 16);
                models.put(id, new Model(file, map.get(start + 9) & 255));
                map.position(start + 28);
            }
        }
    }

    public BufferedImage image(int graphics, int direction, int walking) throws IOException {
        long key = (long) graphics << 8 | direction << 4 | walking;
        if (cache.containsKey(key)) return cache.get(key);
        Model model = region == 2 ? models.get(graphics) : new Model(graphics, 0);
        if (model == null) return null;
        try {
            ByteBuffer data = archive.file(model.file());
            int base = -1;
            for (int i = 0; i < ushort(data, 14); i++) {
                int offset = data.getInt(16 + i * 4);
                if (text(data, offset, 4).equals("TEX0")) {
                    base = offset;
                    break;
                }
            }
            if (base < 0) return null;
            var textures = dictionary(data, base + 60, 8);
            var palettes = dictionary(data, base + 60 + ushort(data, base + 62), 4);
            if (textures.isEmpty()) return null;
            int frame;
            if (region == 2) {
                int cycle = new int[] {0, 1, 0, 2}[walking];
                int block = direction == 1 ? 0 : direction == 0 ? 3 : direction == 2 ? 6 : 9;
                frame = block + cycle;
                if (model.kind() == 5 || model.kind() == 12)
                    frame = (direction == 1 ? 0 : direction == 0 ? 2 : 4) + (walking == 0 ? 0 : 1);
                if (model.kind() == 8 || model.kind() == 19) frame = 0;
            } else {
                int dir = direction == 3 ? 2 : direction;
                frame =
                        switch (textures.size()) {
                            case 16 ->
                                    walking == 0
                                            ? new int[] {11, 0, 2}[dir]
                                            : new int[][] {{12, 14}, {8, 10}, {1, 3}}
                                                    [dir][walking == 1 ? 0 : 1];
                            case 17 ->
                                    walking == 0
                                            ? new int[] {12, 0, 2}[dir]
                                            : new int[][] {{13, 15}, {9, 11}, {1, 3}}
                                                    [dir][walking == 1 ? 0 : 1];
                            case 8 ->
                                    walking == 0
                                            ? new int[] {2, 0, 4}[dir]
                                            : new int[] {3, 1, 5}[dir];
                            default -> Math.min(direction, textures.size() - 1);
                        };
            }
            if (frame >= textures.size()) frame = 0;
            var entry = textures.get(frame);
            int params = ushort(data, entry.offset() + 2), format = params >> 10 & 7;
            if (format < 1 || format > 7 || format == 5 || format != 7 && palettes.isEmpty())
                return null;
            int width = 8 << (params >> 4 & 7), height = 8 << (params >> 7 & 7);
            if (width > 256 || height > 256) throw new IOException("Invalid NPC image bounds");
            int pixels = base + data.getInt(base + 20) + ushort(data, entry.offset()) * 8;
            int palette =
                    palettes.isEmpty()
                            ? 0
                            : base
                                    + data.getInt(base + 56)
                                    + (ushort(data, palettes.getFirst().offset()) & 8191) * 8;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < width * height; i++) {
                int at =
                        pixels
                                + switch (format) {
                                    case 2 -> i / 4;
                                    case 3 -> i / 2;
                                    case 7 -> i * 2;
                                    default -> i;
                                };
                int packed = data.get(at) & 255, alpha = 255;
                int index =
                        switch (format) {
                            case 1 -> {
                                alpha = (packed >> 5) * 255 / 7;
                                yield packed & 31;
                            }
                            case 2 -> packed >> (i % 4 * 2) & 3;
                            case 3 -> packed >> (i % 2 * 4) & 15;
                            case 4 -> packed;
                            case 6 -> {
                                alpha = (packed >> 3) * 255 / 31;
                                yield packed & 7;
                            }
                            default -> 0;
                        };
                if (index == 0 && (params & 8192) != 0 && format >= 2 && format <= 4) continue;
                int color = ushort(data, format == 7 ? at : palette + index * 2), x = i % width;
                if (format == 7) alpha = (color & 32768) == 0 ? 0 : 255;
                if (region != 2 && direction == 3) x = width - 1 - x;
                image.setRGB(
                        x,
                        i / width,
                        alpha << 24
                                | (color & 31) * 255 / 31 << 16
                                | (color >> 5 & 31) * 255 / 31 << 8
                                | (color >> 10 & 31) * 255 / 31);
            }
            cache.put(key, image);
            if (cache.size() > 256) cache.remove(cache.keySet().iterator().next());
            return image;
        } catch (IndexOutOfBoundsException | IllegalArgumentException error) {
            throw new IOException("Invalid NDS NPC graphics " + graphics, error);
        }
    }
}
