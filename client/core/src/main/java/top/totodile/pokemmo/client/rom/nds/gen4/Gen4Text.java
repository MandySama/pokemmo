package top.totodile.pokemmo.client.rom.nds.gen4;

import java.io.IOException;
import top.totodile.pokemmo.client.rom.RomInfo;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** DpptTextBank.VJ0: encrypted offset table and advancing XOR key, with 9-bit strings. */
public final class Gen4Text {
    private Gen4Text() {}

    public static String[] decode(byte[] bytes) throws IOException { return decode(bytes, null, false); }
    public static String[] decode(byte[] bytes, RomInfo rom) throws IOException { return decode(bytes, rom, false); }
    public static String[] decodeFormatted(byte[] bytes) throws IOException { return decode(bytes, null, true); }
    public static String[] decodeFormatted(byte[] bytes, RomInfo rom) throws IOException { return decode(bytes, rom, true); }
    private static String[] decode(byte[] bytes, RomInfo rom, boolean formatted) throws IOException {
        // Match HeartGoldSoulSilverRom.pG0 / PlatinumRom.pG0 language detection.
        boolean chinese = rom != null && rom.code().endsWith("J") && rom.files().entrySet().stream()
                .anyMatch(e -> (e.getKey().equals("a/0/1/6") || e.getKey().equals("graphic/pl_font.narc")) && e.getValue().length() > 500000);
        String charset = chinese ? Gen4TextCharacters.CHINESE : Gen4TextCharacters.STANDARD;
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
                                append(out, code, charset);
                            }
                        }
                        break;
                    }
                    if (value == 0xe000 || value == 9660 || value == 9661) {
                        out.append(formatted ? (value == 0xe000 ? "\n" : "\n\n") : " ");
                        continue;
                    }
                    // Preserve native variable slots for the shared dialogue formatter.
                    if (value == 0xfffe) {
                        if (j + 1 >= text.length) break;
                        int command = text[j + 1];
                        if(command == 517 || command == 518){j++;continue;}
                        if (j + 2 >= text.length) break;
                        int arguments = text[j + 2];
                        if (formatted && ((command >= 256 && command < 512) || (command >= 1536 && command < 1552)) && arguments > 0 && j + 3 < text.length)
                            out.append(String.format("{%02X}", text[j + 3]));
                        j += 2 + arguments;
                        continue;
                    }
                    append(out, value, charset);
                }
                result[i] = out.toString().trim();
            }
            return result;
        } catch (IndexOutOfBoundsException | java.nio.BufferUnderflowException e) {
            throw new IOException("Truncated Gen4 message bank", e);
        }
    }

    private static void append(StringBuilder out, int code, String charset) {
        if (code == 480) out.append("PK");
        else if (code == 481) out.append("MN");
        else if (code > 1 && code < charset.length())
            out.append(charset.charAt(code));
    }
}
