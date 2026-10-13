package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/** 从 ARM9 的物品目录定位 Nitro 图块和调色板，只解码所需的 24 像素图标。 */
public final class BlackWhiteItemIcons {
    private final ByteBuffer table;
    private final NdsArchive archive;

    public BlackWhiteItemIcons(Path rom) throws IOException {
        try (var channel = FileChannel.open(rom, StandardOpenOption.READ)) {
            ByteBuffer header = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
            while (header.hasRemaining()) if (channel.read(header) < 0) throw new IOException("Truncated NDS header");
            int offset = header.getInt(0x20), size = header.getInt(0x2c);
            if (offset < 0 || size < 8 || size > 8 * 1024 * 1024 || (long) offset + size > channel.size())
                throw new IOException("Invalid ARM9 range");
            ByteBuffer arm9 = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
            while (arm9.hasRemaining()) if (channel.read(arm9, offset + arm9.position()) < 0) throw new IOException("Truncated ARM9");
            byte[] decoded = decompress(arm9.array());
            ByteBuffer data = ByteBuffer.wrap(decoded).order(ByteOrder.LITTLE_ENDIAN);
            int start = -1;
            for (int i = 0; i <= decoded.length - 16 - 626 * 4; i++) {
                if (data.getInt(i) == 19071103 && data.getInt(i + 4) == 16318992
                        && data.getInt(i + 8) == 555 && data.getInt(i + 12) == 65668073) { start = i + 16; break; }
            }
            if (start < 0) throw new IOException("Missing item icon directory");
            table = data.slice(start, 626 * 4).order(ByteOrder.LITTLE_ENDIAN);
        }
        archive = new NdsArchive(rom, "/a/0/2/5");
    }

    public BufferedImage image(int item) throws IOException {
        if (item < 5001 || item > 5626) throw new IOException("Invalid item icon ID");
        try {
            int offset = (item - 5001) * 4;
            ByteBuffer palette = archive.file(Short.toUnsignedInt(table.getShort(offset + 2)));
            ByteBuffer tiles = archive.file(Short.toUnsignedInt(table.getShort(offset)));
            if (palette.getInt(0) != 0x4e434c52 || tiles.getInt(0) != 0x4e434752) throw new IOException("Invalid Nitro item icon");
            int paletteStart = 24 + palette.getInt(36);
            int tileStart = 48;
            int[] colors = new int[16];
            for (int i = 1; i < colors.length; i++) {
                int c = Short.toUnsignedInt(palette.getShort(paletteStart + i * 2));
                colors[i] = 0xff000000 | ((c & 31) * 255 / 31) << 16 | ((c >> 5 & 31) * 255 / 31) << 8 | (c >> 10 & 31) * 255 / 31;
            }
            BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 24; y++) for (int x = 0; x < 24; x++) {
                int pixel = ((y / 8) * 4 + x / 8) * 64 + (y % 8) * 8 + x % 8;
                int index = (tiles.get(tileStart + pixel / 2) >>> ((pixel & 1) * 4)) & 15;
                image.setRGB(x, y, colors[index]);
            }
            return image;
        } catch (IndexOutOfBoundsException exception) { throw new IOException("Invalid item icon data", exception); }
    }

    private static byte[] decompress(byte[] input) throws IOException {
        int size = input.length;
        ByteBuffer footer = ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN);
        int extra = footer.getInt(size - 4);
        if (extra == 0) return input;
        int header = Byte.toUnsignedInt(input[size - 5]);
        int encoded = footer.getInt(size - 8) & 0xffffff;
        if (extra < 0 || (long) size + extra > 16 * 1024 * 1024 || header < 8 || header > 11 || encoded > size || encoded < header)
            throw new IOException("Invalid BLZ footer");
        byte[] output = Arrays.copyOf(input, size + extra);
        int source = size - header, end = size - encoded, destination = output.length;
        while (source > end && destination > end) {
            int flags = Byte.toUnsignedInt(input[--source]);
            for (int mask = 128; mask != 0 && source > end && destination > end; mask >>= 1) {
                if ((flags & mask) == 0) output[--destination] = input[--source];
                else {
                    if (source - 2 < end) throw new IOException("Truncated BLZ token");
                    int token = Byte.toUnsignedInt(input[--source]) << 8 | Byte.toUnsignedInt(input[--source]);
                    int length = (token >>> 12) + 3, distance = (token & 4095) + 3;
                    for (int i = 0; i < length && destination > end; i++) {
                        --destination;
                        if (destination + distance >= output.length) throw new IOException("Invalid BLZ distance");
                        output[destination] = output[destination + distance];
                    }
                }
            }
        }
        if (destination != end) throw new IOException("Incomplete BLZ stream");
        return output;
    }
}
