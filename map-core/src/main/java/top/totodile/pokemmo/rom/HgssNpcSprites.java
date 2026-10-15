package top.totodile.pokemmo.rom;


import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static top.totodile.pokemmo.rom.NdsArchive.*;
import static top.totodile.pokemmo.rom.NitroData.*;

/** 按外部映射读取心金魂银 NPC 纹理，CPU 图像缓存不持有 OpenGL 资源。 */
final class HgssNpcSprites {
    private static final int[] FRAMES_16 = {11, 0, 2};
    private static final int[] FRAMES_17 = {12, 0, 2};
    private static final int[] FRAMES_8 = {2, 0, 4};
    private final NdsArchive archive;
    private final Map<Integer, Integer> replacements = new HashMap<>();
    private final Map<Integer, BufferedImage> images = new HashMap<>();

    HgssNpcSprites(Path rom) throws IOException { this(rom,0); }

    HgssNpcSprites(Path rom,int region) throws IOException {
        archive = new NdsArchive(rom, "/a/0/8/1");
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var entries = factory.newDocumentBuilder().parse(MapFiles.data(region==1?"maps/emerald-npcs.xml":"maps/fire-red-npcs.xml").toFile())
                    .getElementsByTagName("sprite");
            for (int i = 0; i < entries.getLength(); i++) {
                var entry = (org.w3c.dom.Element) entries.item(i);
                replacements.put(Integer.parseInt(entry.getAttribute("gba")), Integer.parseInt(entry.getAttribute("hgss")));
            }
        } catch (Exception exception) {
            throw new IOException("Invalid NPC replacement mapping", exception);
        }
    }

    BufferedImage image(int graphics, int direction) throws IOException {
        return image(graphics, direction, 0);
    }

    BufferedImage image(int graphics, int direction, int walkFrame) throws IOException {
        Integer replacement = replacements.get(graphics);
        if (replacement == null) return null;
        int key = graphics * 16 + direction * 3 + walkFrame;
        if (images.containsKey(key)) return images.get(key);
        try {
            // 外观编号从 1 开始，NARC 分配表的文件索引从 0 开始。
            ByteBuffer data = archive.file(replacement - 1);
            if (!text(data, 0, 4).equals("BTX0")) return null;
            int base = data.getInt(16);
            if (!text(data, base, 4).equals("TEX0")) return null;
            var textures = dictionary(data, base + 60, 8);
            var palettes = dictionary(data, base + 60 + ushort(data, base + 62), 4);
            if (textures.isEmpty() || palettes.isEmpty()) return null;
            int frame = switch (textures.size()) {
                case 16 -> FRAMES_16[direction];
                case 17 -> FRAMES_17[direction];
                case 8 -> FRAMES_8[direction];
                default -> Math.min(direction, textures.size() - 1);
            };
            if (walkFrame != 0) {
                frame = switch (textures.size()) {
                    case 16 -> new int[][]{{12, 14}, {8, 10}, {1, 3}}[direction][walkFrame - 1];
                    case 17 -> new int[][]{{13, 15}, {9, 11}, {1, 3}}[direction][walkFrame - 1];
                    case 8 -> new int[]{3, 1, 5}[direction];
                    default -> frame;
                };
            }
            var entry = textures.get(frame);
            int params = ushort(data, entry.offset() + 2);
            int format = params >> 10 & 7;
            if (format != 3 && format != 4) return null;
            int width = 8 << (params >> 4 & 7), height = 8 << (params >> 7 & 7);
            if (width > 128 || height > 128) throw new IOException("Invalid NPC texture dimensions");
            int pixels = base + data.getInt(base + 20) + ushort(data, entry.offset()) * 8;
            int palette = base + data.getInt(base + 56) + (ushort(data, palettes.getFirst().offset()) & 8191) * 8;
            var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < width * height; i++) {
                int packed = data.get(pixels + (format == 3 ? i / 2 : i)) & 255;
                int index = format == 3 ? packed >> (i % 2 * 4) & 15 : packed;
                if (index == 0 && (params & 8192) != 0) continue;
                int color = ushort(data, palette + index * 2);
                image.setRGB(i % width, i / width, 0xFF000000 | (color & 31) * 8 << 16
                        | (color >> 5 & 31) * 8 << 8 | (color >> 10 & 31) * 8);
            }
            images.put(key, image);
            return image;
        } catch (IndexOutOfBoundsException | IllegalArgumentException exception) {
            throw new IOException("Invalid HGSS NPC resource " + replacement, exception);
        }
    }
}
