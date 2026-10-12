package top.totodile.pokemmo.persistence;

import top.totodile.pokemmo.protocol.CharacterPackets.Appearance;
import java.io.IOException;
import java.nio.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

/** 服务端只读取外观元数据；纹理区段跳过，不分配图像或图形上下文。 */
public final class AvatarCatalog {
    private record Item(int flags) { }
    private final Map<Integer, Item> items = new HashMap<>();
    public AvatarCatalog(Path path) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length < 32 || bytes.length > 64 * 1024 * 1024) throw new IOException("Invalid avatar pack size");
            byte[] actual = MessageDigest.getInstance("SHA-1").digest(Arrays.copyOf(bytes, bytes.length - 20));
            if (!MessageDigest.isEqual(actual, Arrays.copyOfRange(bytes, bytes.length - 20, bytes.length))) throw new IOException("Avatar checksum mismatch");
            ByteBuffer data = ByteBuffer.wrap(bytes, 0, bytes.length - 20).order(ByteOrder.LITTLE_ENDIAN);
            byte[] magic = new byte[8]; data.get(magic);
            if (!Arrays.equals(magic, "\7POKEMMO".getBytes(StandardCharsets.US_ASCII))) throw new IOException("Invalid avatar header");
            data.position(10);
            int bodies = u8(data);
            if (bodies != 5) throw new IOException("Invalid avatar bodies");
            for (int i = 0; i < bodies; i++) for (int view = 0; view < 3; view++) {
                int frames = u8(data);
                for (int frame = 0; frame < frames; frame++) skip(data, data.getInt());
            }
            int count = data.getInt();
            if (count < 0 || count > 10000) throw new IOException("Invalid avatar count");
            for (int n = 0; n < count; n++) {
                int id = Short.toUnsignedInt(data.getShort()), category = u8(data);
                byte[] text = new byte[u8(data)]; data.get(text);
                int flags = data.getInt() & -8417;
                int masks = u8(data);
                for (int m = 0; m < masks; m++) skip(data, Short.toUnsignedInt(data.getShort()) * 2);
                int variants = u8(data), tracks = u8(data);
                for (int t = 0; t < tracks; t++) { skip(data, 3); skip(data, u8(data)); }
                for (int variant = 0; variant < variants; variant++) for (int view = 0; view < 3; view++) {
                    int frames = u8(data);
                    for (int frame = 0; frame < frames; frame++) {
                        int layers = u8(data);
                        for (int layer = 0; layer < layers; layer++) {
                            boolean reference = view == 0 && u8(data) != 0;
                            u8(data);
                            skip(data, reference ? 12 : data.getInt());
                        }
                    }
                }
                if (category >= 12) throw new IOException("Invalid avatar category");
                if (!new String(text, StandardCharsets.UTF_8).trim().equals("UNUSED")) items.put(category * 65536 + id, new Item(flags));
            }
            if (data.hasRemaining()) throw new IOException("Trailing avatar data");
        } catch (IOException exception) { throw exception; }
        catch (Exception exception) { throw new IOException("Invalid avatar metadata", exception); }
    }
    private static int u8(ByteBuffer data) { return Byte.toUnsignedInt(data.get()); }
    private static void skip(ByteBuffer data, int count) throws IOException {
        if (count < 0 || count > data.remaining()) throw new IOException("Avatar section outside pack");
        data.position(data.position() + count);
    }
    public boolean valid(Appearance appearance) {
        if (!appearance.valid()) return false;
        int[] ids = appearance.items(), colors = appearance.colors();
        for (int category = 0; category < 12; category++) {
            int id = ids[category];
            if (category == 0) { if (id != -1 || colors[category] != 0) return false; continue; }
            if (category == 10) { if (id != 0 || colors[category] != 0) return false; continue; }
            if (id == -1) {
                if ((category != 1 && category != 4 && category != 5 && category != 7) || colors[category] != 0) return false;
                continue;
            }
            Item item = items.get(category * 65536 + id);
            if (item == null) return false;
            int flags = item.flags();
            int gender = (flags & 1) != 0 ? 0 : (flags & 2) != 0 ? 1 : -1;
            if ((flags & (4 | 1024)) != 0 || gender != -1 && gender != appearance.gender()
                    || category == 3 && id < 2 || (flags & 16) == 0 && colors[category] != 0) return false;
        }
        Item hair = items.get(2 * 65536 + ids[2]);
        return hair == null || (hair.flags() & 65536) == 0 || ids[1] == -1;
    }
}
