package top.totodile.pokemmo.rom;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Nitro 文件的 LZ10/LZ11 有界解压。 */
final class NdsCompression {
    static ByteBuffer decode(ByteBuffer source) throws IOException {
        int type = source.get(0) & 255;
        if (type != 0x10 && type != 0x11) return source.duplicate().order(ByteOrder.LITTLE_ENDIAN);
        try {
            int size = (source.getInt(0) >>> 8), read = 4;
            if (size == 0) { size = source.getInt(4); read = 8; }
            if (size < 1 || size > 16 * 1024 * 1024) throw new IOException("Invalid Nitro decompression size");
            byte[] result = new byte[size]; int out = 0;
            while (out < size) {
                int flags = source.get(read++) & 255;
                for (int bit = 7; bit >= 0 && out < size; bit--) {
                    if ((flags & 1 << bit) == 0) { result[out++] = source.get(read++); continue; }
                    int a = source.get(read++) & 255, b = source.get(read++) & 255, length, distance;
                    if (type == 0x10) { length = (a >> 4) + 3; distance = ((a & 15) << 8 | b) + 1; }
                    else if ((a >> 4) == 0) { int c = source.get(read++) & 255; length = ((a & 15) << 4 | b >> 4) + 0x11; distance = ((b & 15) << 8 | c) + 1; }
                    else if ((a >> 4) == 1) { int c = source.get(read++) & 255, d = source.get(read++) & 255; length = ((a & 15) << 12 | b << 4 | c >> 4) + 0x111; distance = ((c & 15) << 8 | d) + 1; }
                    else { length = (a >> 4) + 1; distance = ((a & 15) << 8 | b) + 1; }
                    if (distance > out || length > size - out) throw new IOException("Invalid Nitro compression reference");
                    for (int i = 0; i < length; i++) { result[out] = result[out - distance]; out++; }
                }
            }
            return ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
        } catch (IndexOutOfBoundsException exception) { throw new IOException("Truncated Nitro compressed data", exception); }
    }
}