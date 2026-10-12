package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** 有界封包字段读写；数值和以零结尾的 UTF-16 字符串使用小端序。 */
public final class PacketData {
    private final ByteBuffer buffer;

    public PacketData(byte[] data) {
        buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }

    public PacketData(int capacity) {
        buffer = ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);
    }

    public int byteValue() throws IOException {
        require(1);
        return Byte.toUnsignedInt(buffer.get());
    }

    public int shortValue() throws IOException {
        require(2);
        return Short.toUnsignedInt(buffer.getShort());
    }

    public int intValue() throws IOException {
        require(4);
        return buffer.getInt();
    }

    public long longValue() throws IOException {
        require(8);
        return buffer.getLong();
    }

    public boolean booleanValue() throws IOException {
        int value = byteValue();
        if (value > 1) throw new IOException("Invalid boolean field");
        return value == 1;
    }

    public byte[] bytes(int length) throws IOException {
        require(length);
        byte[] result = new byte[length];
        buffer.get(result);
        return result;
    }

    public String string(int maxChars) throws IOException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i <= maxChars; i++) {
            char value = (char) shortValue();
            if (value == 0) return result.toString();
            if (i == maxChars) break;
            result.append(value);
        }
        throw new IOException("String field exceeds limit");
    }

    public PacketData putByte(int value) {
        buffer.put((byte) value);
        return this;
    }

    public PacketData putShort(int value) {
        buffer.putShort((short) value);
        return this;
    }

    public PacketData putInt(int value) {
        buffer.putInt(value);
        return this;
    }

    public PacketData putLong(long value) {
        buffer.putLong(value);
        return this;
    }

    public PacketData putBytes(byte[] value) {
        buffer.put(value);
        return this;
    }

    public PacketData putString(String value) {
        if (value.indexOf('\0') >= 0) throw new IllegalArgumentException("Embedded string terminator");
        for (int i = 0; i < value.length(); i++) buffer.putChar(value.charAt(i));
        buffer.putChar('\0');
        return this;
    }

    public byte[] written() {
        return java.util.Arrays.copyOf(buffer.array(), buffer.position());
    }

    public void end() throws IOException {
        if (buffer.hasRemaining()) throw new IOException("Unexpected trailing packet data");
    }

    private void require(int length) throws IOException {
        if (length < 0 || buffer.remaining() < length) throw new IOException("Truncated packet");
    }
}
