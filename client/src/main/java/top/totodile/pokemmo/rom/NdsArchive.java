package top.totodile.pokemmo.rom;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** 从 NitroFS 定位 NARC，只读取目标资源包，不展开整张卡带。 */
public final class NdsArchive {
    private final ByteBuffer data;
    private final List<int[]> files = new ArrayList<>();
    private int content;

    public NdsArchive(Path rom, String path) throws IOException {
        data = resource(rom, path);
        try {
            if (!text(data, 0, 4).equals("NARC")) throw new IOException("Invalid NARC archive");
            for (int offset = ushort(data, 12); offset < data.limit(); ) {
                String tag = text(data, offset, 4);
                int size = data.getInt(offset + 4);
                if (size < 8 || (long) offset + size > data.limit()) throw new IOException("Invalid NARC block");
                if (tag.equals("BTAF")) {
                    int count = ushort(data, offset + 8);
                    if (12L + count * 8L > size) throw new IOException("Invalid NARC allocation table");
                    for (int i = 0; i < count; i++) {
                        files.add(new int[]{data.getInt(offset + 12 + i * 8), data.getInt(offset + 16 + i * 8)});
                    }
                } else if (tag.equals("GMIF")) {
                    content = offset + 8;
                }
                offset += size;
            }
            if (content == 0 || files.isEmpty()) throw new IOException("Incomplete NARC archive");
        } catch (IndexOutOfBoundsException | ArithmeticException exception) {
            throw new IOException("Invalid ROM archive layout", exception);
        }
    }

    /** 定位 NitroFS 文件，供模型资源包和声音库共用；不写出 ROM 内容。 */
    public static ByteBuffer resource(Path rom, String path) throws IOException {
        try (FileChannel channel = FileChannel.open(rom, StandardOpenOption.READ)) {
            ByteBuffer header = read(channel, 0, 512);
            String code = text(header, 12, 3);
            if (!code.equals("IRA") && !code.equals("IRB") && !code.equals("IPK") && !code.equals("IPG")) {
                throw new IOException("Unsupported NitroFS ROM");
            }
            ByteBuffer names = read(channel, header.getInt(0x40), header.getInt(0x44));
            int directory = 0;
            int fileId = -1;
            String[] parts = path.substring(1).split("/");
            for (int part = 0; part < parts.length; part++) {
                int cursor = names.getInt(directory * 8);
                int id = ushort(names, directory * 8 + 4);
                boolean found = false;
                while (true) {
                    int length = names.get(cursor++) & 255;
                    if (length == 0) break;
                    boolean child = (length & 128) != 0;
                    String name = text(names, cursor, length & 127);
                    cursor += length & 127;
                    int childId = child ? ushort(names, cursor) & 4095 : -1;
                    if (child) cursor += 2;
                    if (name.equals(parts[part])) {
                        if (child && part < parts.length - 1) directory = childId;
                        else if (!child && part == parts.length - 1) fileId = id;
                        else break;
                        found = true;
                        break;
                    }
                    if (!child) id++;
                }
                if (!found) throw new IOException("Missing ROM resource: " + path);
            }
            if (fileId < 0 || (long) fileId * 8 + 8 > Integer.toUnsignedLong(header.getInt(0x4C))) {
                throw new IOException("Invalid NitroFS file index");
            }
            ByteBuffer allocation = read(channel, Integer.toUnsignedLong(header.getInt(0x48)) + fileId * 8L, 8);
            long start = Integer.toUnsignedLong(allocation.getInt());
            long end = Integer.toUnsignedLong(allocation.getInt());
            return read(channel, start, Math.toIntExact(end - start));
        } catch (IndexOutOfBoundsException | ArithmeticException exception) {
            throw new IOException("Invalid ROM archive layout", exception);
        }
    }

    public ByteBuffer file(int index) {
        int[] range = files.get(index);
        if (range[0] < 0 || range[1] < range[0] || (long) content + range[1] > data.limit()) {
            throw new IllegalArgumentException("Invalid NARC file range");
        }
        return data.slice(content + range[0], range[1] - range[0]).order(ByteOrder.LITTLE_ENDIAN);
    }

    private static ByteBuffer read(FileChannel channel, long offset, int length) throws IOException {
        if (offset < 0 || length < 0 || length > 64 * 1024 * 1024 || offset + length > channel.size()) {
            throw new IOException("Invalid ROM resource range");
        }
        ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.hasRemaining()) {
            if (channel.read(buffer, offset + buffer.position()) < 0) throw new IOException("Truncated ROM resource");
        }
        return buffer.flip();
    }

    static int ushort(ByteBuffer buffer, int offset) {
        return buffer.getShort(offset) & 65535;
    }

    static String text(ByteBuffer buffer, int offset, int length) {
        byte[] bytes = new byte[length];
        buffer.get(offset, bytes);
        return new String(bytes, StandardCharsets.US_ASCII).replace("\0", "").trim();
    }
}
