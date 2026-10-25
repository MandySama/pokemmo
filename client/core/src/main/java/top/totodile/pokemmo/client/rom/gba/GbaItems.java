package top.totodile.pokemmo.client.rom.gba;

import top.totodile.pokemmo.client.rom.*;

import java.io.IOException;
import java.nio.*;
import java.nio.file.Files;
import java.util.*;

/** GbaRom's 44-byte item records; GbaRomRegistry.Wa0 uses the pointer at ROM offset 456. */
public final class GbaItems {
    public record Item(int id, String name, String description, int price, int pocket) {}

    public static List<Item> load(RomInfo rom) throws IOException {
        boolean emerald = rom.type() == RomType.EMERALD;
        if (!emerald && rom.type() != RomType.FIRE_RED)
            throw new IllegalArgumentException("GBA ROM required");
        byte[] bytes = Files.readAllBytes(rom.path());
        var input = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int table = input.getInt(456) - 0x08000000, count = emerald ? 377 : 375;
        if (table < 0 || table > bytes.length - count * 44)
            throw new IOException("Invalid GBA item table");
        var result = new ArrayList<Item>();
        for (int i = 0; i < count; i++) {
            int p = table + i * 44,
                    id = Short.toUnsignedInt(input.getShort(p + 14)),
                    price = Short.toUnsignedInt(input.getShort(p + 16)),
                    pocket = bytes[p + 26] & 255;
            if (emerald)
                pocket =
                        switch (pocket) {
                            case 1 -> 1;
                            case 2 -> 3;
                            case 3 -> 4;
                            case 4 -> 5;
                            case 5 -> 2;
                            default -> 1;
                        };
            int description = input.getInt(p + 20) - 0x08000000;
            result.add(
                    new Item(
                            id,
                            GbaText.decode(bytes, p),
                            GbaText.decode(bytes, description),
                            price,
                            pocket));
        }
        return List.copyOf(result);
    }
}
