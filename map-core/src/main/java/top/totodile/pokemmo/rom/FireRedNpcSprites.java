package top.totodile.pokemmo.rom;


import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/** 从精灵描述、帧指针与独立精灵调色板解码 GBA 4bpp 图像。 */
final class FireRedNpcSprites {
    private final byte[] rom;
    private final int table;
    private final int[] palettes = new int[46];
    private final Map<Integer, Integer> colors = new HashMap<>();

    FireRedNpcSprites(byte[] rom) throws IOException {
        this.rom = rom;
        table = locateGraphicsTable();
        int paletteTable = locate("21F801BC00470000FF110000", 0);
        for (int i = 0; i < 44; i++) {
            int entry = paletteTable + i * 8;
            if (integer(entry) == 0) break;
            int index = unsigned(entry + 4);
            if (index < palettes.length) palettes[index] = pointer(entry);
        }
        // 场景物件使用 0x1000 系列标签，独立于人物的 0x1100 系列调色板。
        int[] pattern = {8, 4, 3, -1, -1, -1, 8, 4, 3, -1, -1, -1, 8, 4, 7};
        outer: for (int offset = 0; offset < rom.length - 19; offset++) {
            for (int i = 0; i < pattern.length; i++) {
                if (pattern[i] >= 0 && unsigned(offset + i) != pattern[i]) continue outer;
            }
            int entry = pointer(offset + pattern.length);
            while (entry >= 8 && ushort(entry - 4) >= 0x1000 && ushort(entry - 4) < 0x1014) entry -= 8;
            for (int i = 0; i < 20 && integer(entry) != 0; i++, entry += 8) {
                int tag = ushort(entry + 4);
                if (tag < 0x1000 || tag >= 0x1014) break;
                palettes[26 + tag - 0x1000] = pointer(entry);
            }
            break;
        }
        if (rom[174] == 'E') return;
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var entries = factory.newDocumentBuilder().parse(MapFiles.data("maps/fire-red-npcs.xml").toFile())
                    .getElementsByTagName("palette");
            for (int i = 0; i < entries.getLength(); i++) {
                var entry = (org.w3c.dom.Element) entries.item(i);
                int key = Integer.parseInt(entry.getAttribute("season")) * 1024
                        + Integer.parseInt(entry.getAttribute("id")) * 16 + Integer.parseInt(entry.getAttribute("index"));
                colors.put(key, (int) Long.parseLong(entry.getAttribute("rgba")));
            }
        } catch (Exception exception) {
            throw new IOException("Invalid NPC palette mapping", exception);
        }
    }

    BufferedImage image(int graphics, int frame) throws IOException {
        return image(graphics, frame, 1, false);
    }

    BufferedImage image(int graphics, int frame, int season, boolean enhanced) throws IOException {
        if (graphics < 0 || graphics > 255 || frame < 0 || frame > 8) throw new IOException("Invalid NPC sprite");
        int info = pointer(table + graphics * 4);
        if (ushort(info) != 65535 || (unsigned(info + 3) != 17 && unsigned(info + 3) != 16)) {
            throw new IOException("Unsupported NPC sprite format");
        }
        int width = ushort(info + 8), height = ushort(info + 10);
        if (width < 8 || height < 8 || width > 128 || height > 128 || width % 8 != 0 || height % 8 != 0)
            throw new IOException("Invalid NPC sprite dimensions");
        int paletteIndex = unsigned(info + 2) + (unsigned(info + 3) == 16 ? 26 : 0);
        int palette = palettes[paletteIndex < palettes.length ? paletteIndex : 0];
        if (palette == 0) palette = palettes[0];
        if (palette == 0) throw new IOException("Missing NPC palette");
        int frames = pointer(info + 28);
        if (frame >= 3 && (!isFrame(frames + frame * 8, width, height))) frame = 0;
        int pixels = pointer(frames + frame * 8);
        if (ushort(frames + frame * 8 + 4) < width * height / 2) throw new IOException("Truncated NPC frame");
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int tile = (y / 8 * (width / 8) + x / 8) * 32;
                int index = unsigned(pixels + tile + y % 8 * 4 + x % 8 / 2) >> (x % 2 * 4) & 15;
                if (index == 0) continue;
                int color = ushort(palette + index * 2);
                int rgba = (color & 31) * 8 << 24 | (color >> 5 & 31) * 8 << 16
                        | (color >> 10 & 31) * 8 << 8 | 255;
                if (enhanced && season != 1) rgba = colors.getOrDefault(season * 1024 + paletteIndex * 16 + index, rgba);
                image.setRGB(x, y, rgba >>> 8 | rgba << 24);
            }
        }
        return image;
    }

    private boolean isFrame(int entry, int width, int height) throws IOException {
        if (entry < 0 || entry + 8 > rom.length) return false;
        long address = Integer.toUnsignedLong(integer(entry));
        return address >= 0x08000000L && address < 0x08000000L + rom.length
                && ushort(entry + 4) >= width * height / 2;
    }

    private int locateGraphicsTable() throws IOException {
        try {
            return locate("89000918086802BC08470000", 1);
        } catch (IOException missing) {
            // 扩展卡带可能替换查表函数；以连续主角描述定位新表，后置表覆盖旧副本。
            int selected = -1;
            for (int offset = 0; offset <= rom.length - 12; offset += 4) {
                int address = integer(offset) - 0x08000000;
                if (address < 0 || address > rom.length - 36 || ushort(address) != 65535) continue;
                try {
                    int first = pointer(offset), second = pointer(offset + 4), third = pointer(offset + 8);
                    if (ushort(first) == 65535 && ushort(second) == 65535 && ushort(third) == 65535
                            && ushort(first + 2) == 0x1100 && ushort(second + 2) == 0x1100
                            && ushort(first + 8) == 16 && ushort(first + 10) == 32
                            && ushort(second + 8) == 32 && ushort(second + 10) == 32) selected = offset;
                } catch (IOException invalid) {
                    // 非指针位置不属于描述表。
                }
            }
            if (selected < 0) throw missing;
            return selected;
        }
    }

    private int locate(String hex, int occurrence) throws IOException {
        byte[] pattern = HexFormat.of().parseHex(hex);
        outer: for (int offset = 0; offset <= rom.length - pattern.length - 4; offset++) {
            for (int i = 0; i < pattern.length; i++) if (rom[offset + i] != pattern[i]) continue outer;
            if (occurrence-- == 0) return pointer(offset + pattern.length);
        }
        throw new IOException("NPC table not found");
    }

    private int unsigned(int offset) throws IOException {
        if (offset < 0 || offset >= rom.length) throw new IOException("NPC ROM offset out of range");
        return rom[offset] & 255;
    }

    private int ushort(int offset) throws IOException { return unsigned(offset) | unsigned(offset + 1) << 8; }
    private int integer(int offset) throws IOException { return ushort(offset) | ushort(offset + 2) << 16; }
    private int pointer(int offset) throws IOException {
        int value = integer(offset) - 0x08000000;
        if (value < 0 || value >= rom.length) throw new IOException("Invalid NPC ROM pointer");
        return value;
    }
}
