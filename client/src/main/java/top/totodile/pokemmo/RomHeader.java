package top.totodile.pokemmo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/** 仅读取卡带头的游戏代码、修订号和区段范围，不解析游戏资源。 */
final class RomHeader {
    private static final String[][] CODES = {{"IRA", "IRB"}, {"BPR"}, {"BPE"}, {"CPU"}, {"IPG", "IPK"}};
    private static final int[] LABELS = {92, 90, 91, 93, 94};

    static void validate(int index, Path file) throws IOException {
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) throw error(1183, index, file);
        byte[] header;
        try (InputStream input = Files.newInputStream(file)) {
            header = input.readNBytes(2048);
        }
        String archive = archive(header);
        if (archive != null) throw new IOException(ClientStrings.get(1181).replace("{00}", archive));
        boolean gba = index == 1 || index == 2;
        int codeOffset = gba ? 0xAC : 0x0C;
        if (header.length < (gba ? 255 : 2048)) throw error(1183, index, file);
        String code = new String(header, codeOffset, 4, StandardCharsets.US_ASCII);
        if (!Set.of(CODES[index]).contains(code.substring(0, 3))) throw error(1183, index, file);
        if (gba) {
            int revision = header[0xBC] & 255;
            boolean supported = Set.of("BPRE", "BPRF", "BPRD", "BPRS", "BPRI",
                    "BPEE", "BPEF", "BPED", "BPES", "BPEI").contains(code)
                    && (revision == 0 || code.equals("BPRE") && revision == 1);
            if (!supported) throw error(1147, index, file);
        } else {
            // ARM9、ARM7、文件名表和文件分配表必须完整落在文件范围内。
            ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
            long size = Files.size(file);
            for (int[] section : new int[][]{{0x20, 0x2C}, {0x30, 0x3C}, {0x40, 0x44}, {0x48, 0x4C}}) {
                long offset = Integer.toUnsignedLong(buffer.getInt(section[0]));
                long length = Integer.toUnsignedLong(buffer.getInt(section[1]));
                if (offset < 0x200 || length == 0 || offset + length > size) throw error(1182, index, file);
            }
        }
    }

    private static IOException error(int id, int index, Path file) {
        String kind = ClientStrings.get(LABELS[index]);
        return new IOException(ClientStrings.get(id).replace("{00}", id == 1147 ? kind : file.getFileName().toString())
                .replace("{01}", kind));
    }

    private static String archive(byte[] bytes) {
        if (bytes.length < 6) return null;
        if (bytes[0] == 'P' && bytes[1] == 'K' && (bytes[2] == 3 || bytes[2] == 5 || bytes[2] == 7)) return "ZIP";
        if (bytes[0] == 'R' && bytes[1] == 'a' && bytes[2] == 'r' && bytes[3] == '!') return "RAR";
        if (bytes[0] == '7' && bytes[1] == 'z' && (bytes[2] & 255) == 0xBC && (bytes[3] & 255) == 0xAF) return "7Z";
        if ((bytes[0] & 255) == 0x1F && (bytes[1] & 255) == 0x8B) return "GZIP";
        return null;
    }
}
