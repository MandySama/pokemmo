package top.totodile.pokemmo.client.rom.nds.gen4;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** DpptTextBank.VJ0: encrypted offset table and advancing XOR key, with 9-bit strings. */
final class Gen4Text {
    private Gen4Text() {}

    static String[] decode(byte[] bytes) throws IOException {
        try {
            var data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            int count = Short.toUnsignedInt(data.getShort()),
                    seed = Short.toUnsignedInt(data.getShort()) * 765 & 65535;
            if (count > 4096 || 4L + count * 8L > bytes.length)
                throw new IOException("Invalid Gen4 message table");
            String[] result = new String[count];
            for (int i = 0; i < count; i++) {
                int tableKey = seed * (i + 1) & 65535;
                tableKey |= tableKey << 16;
                int offset = data.getInt(4 + i * 8) ^ tableKey,
                        length = data.getInt(8 + i * 8) ^ tableKey;
                if (offset < 0 || length < 0 || offset + length * 2L > bytes.length)
                    throw new IOException("Invalid Gen4 message entry");
                int key = (i + 1) * 596947 & 65535;
                int[] text = new int[length];
                for (int j = 0; j < length; j++) {
                    text[j] = Short.toUnsignedInt(data.getShort(offset + j * 2)) ^ key;
                    key = (key + 18749) & 65535;
                }
                var out = new StringBuilder();
                for (int j = 0; j < text.length; j++) {
                    int value = text[j];
                    if (value == 65535) break;
                    if (value == 0xf100) {
                        int bits = 0, packed = 0;
                        compressed:
                        while (++j < text.length) {
                            packed |= (text[j] & 32767) << bits;
                            bits += 15;
                            while (bits >= 9) {
                                int code = packed & 511;
                                packed >>>= 9;
                                bits -= 9;
                                if ((code & 255) == 255) break compressed;
                                append(out, code);
                            }
                        }
                        break;
                    }
                    if (value == 0xe000 || value == 9660 || value == 9661) {
                        out.append(' ');
                        continue;
                    }
                    // Location labels contain no variable substitutions; preserve only their text.
                    if (value == 0xfffe) {
                        if (j + 2 >= text.length) break;
                        int arguments = text[j + 2];
                        j += 2 + arguments;
                        continue;
                    }
                    append(out, value);
                }
                result[i] = out.toString().trim();
            }
            return result;
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Truncated Gen4 message bank", e);
        }
    }

    private static void append(StringBuilder out, int code) {
        if (code == 480) out.append("PK");
        else if (code == 481) out.append("MN");
        else if (code > 1 && code < Gen4TextCharacters.STANDARD.length())
            out.append(Gen4TextCharacters.STANDARD.charAt(code));
    }
}
