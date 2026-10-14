package top.totodile.pokemmo.rom;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;

/** 读取物品参数与名称文本，名称沿用 ROM 的 UTF-16 和九位压缩编码。 */
public final class BlackWhiteItems {
    public record Item(int id, int pocket, int maxStack, String name) { }
    private BlackWhiteItems() { }
    public static List<Item> load(Path rom) throws IOException {
        var parameters = new NdsArchive(rom, "/a/0/2/4");
        var names = names(new NdsArchive(rom, "/a/0/0/2").file(54));
        List<Item> items = new ArrayList<>();
        for (int index = 1; index <= 626 && index < names.size(); index++) {
            ByteBuffer data = parameters.file(index);
            if (data.limit() < 14) throw new IOException("Truncated item parameters");
            int category = (Short.toUnsignedInt(data.getShort(8)) >>> 7) & 7;
            int kind = Byte.toUnsignedInt(data.get(13));
            int pocket = switch (category) { case 1 -> 1; case 2 -> 5; case 3 -> 6; case 4 -> 3; default -> 0; };
            if (kind == 4) pocket = 4;
            else if (kind == 3) pocket = 2;
            items.add(new Item(index + 5000, pocket, pocket == 3 ? 1 : 9999, names.get(index)));
        }
        return List.copyOf(items);
    }

    private static List<String> names(ByteBuffer data) throws IOException {
        try {
            int sections = Short.toUnsignedInt(data.getShort(0)), count = Short.toUnsignedInt(data.getShort(2));
            if (sections < 1 || sections > 16 || count < 627 || count > 4096) throw new IOException("Invalid item text header");
            int section = data.getInt(12), seed = 31881;
            List<String> result = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int offset = data.getInt(section + 4 + i * 8), length = Short.toUnsignedInt(data.getShort(section + 8 + i * 8));
                int key = seed, bits = 0, value = 0;
                boolean compressed = false, ended = false;
                StringBuilder text = new StringBuilder();
                for (int j = 0; j < length && !ended; j++) {
                    int ch = Short.toUnsignedInt(data.getShort(section + offset + j * 2)) ^ key;
                    key = ((key << 3) | (key >>> 13)) & 65535;
                    if (compressed) {
                        value |= ch << bits;
                        bits += 16;
                        while (bits >= 9) {
                            int c = value & 511;
                            value >>>= 9; bits -= 9;
                            if ((c & 255) == 255) { ended = true; break; }
                            if (c > 1) text.append((char) c);
                        }
                    } else if (ch == 65535) break;
                    else if (ch == 0xF100) compressed = true;
                    else if (ch == 65534) text.append(' ');
                    else if (ch == 0xF000) throw new IOException("Unsupported control code in item name");
                    else if (ch > 10) text.append((char) ch);
                }
                result.add(text.toString().trim());
                seed = (seed + 10627) & 65535;
            }
            return List.copyOf(result);
        } catch (IndexOutOfBoundsException exception) { throw new IOException("Invalid item text data", exception); }
    }
}
