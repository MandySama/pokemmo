package top.totodile.pokemmo.protocol;

import java.io.IOException;

/** 游戏连接的入服请求与结果；版本、ROM 和设备扩展字段保留在封包中。 */
public final class GameAdmission {
    private GameAdmission() {
    }

    public record Request(int accountId, byte[] ticket) {
    }

    public static byte[] request(GameEndpoint endpoint) {
        byte[] ticket = endpoint.ticket();
        return new PacketData(512).putByte(1).putByte(0).putInt(endpoint.accountId())
                .putByte(ticket.length).putBytes(ticket).putBytes(new byte[6]).putInt(0).putInt(0)
                .putByte(0).putShort(0).putShort(0).putByte(0).putByte(0).putByte(0)
                .putByte(0).putByte(0).putByte(0).putByte(0).putBytes(new byte[32]).written();
    }

    public static Request decodeRequest(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 1 || data.booleanValue()) throw new IOException("Unsupported game login");
        int accountId = data.intValue();
        byte[] ticket = data.bytes(data.byteValue());
        data.bytes(6);
        data.intValue();
        data.intValue();
        data.byteValue();
        data.shortValue();
        data.shortValue();
        data.byteValue();
        int roms = data.byteValue();
        if (roms > 5) throw new IOException("Too many ROM entries");
        for (int i = 0; i < roms; i++) {
            data.string(64);
            data.byteValue();
            data.byteValue();
        }
        int info = data.byteValue();
        if (info > 16) throw new IOException("Too many client fields");
        for (int i = 0; i < info; i++) {
            data.byteValue();
            data.string(256);
        }
        data.bytes(4);
        data.bytes(32);
        data.end();
        return new Request(accountId, ticket);
    }

    public static byte[] response(boolean success) {
        PacketData data = new PacketData(32).putByte(1).putByte(success ? 1 : 0);
        if (success) {
            data.putString("").putByte(0).putInt(0).putInt(0).putInt(0)
                    .putInt(1640995200).putInt((int) (System.currentTimeMillis() / 1000));
        }
        return data.written();
    }

    public static boolean decodeResponse(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 1) throw new IOException("Unexpected game response");
        boolean success = data.booleanValue();
        if (success) {
            data.string(256);
            data.byteValue();
            for (int i = 0; i < 5; i++) data.intValue();
        }
        data.end();
        return success;
    }

    public static byte[] heartbeatRequest() {
        return new PacketData(34).putByte(0x20).putInt(0).putInt(0).putShort(0)
                .putLong(System.currentTimeMillis()).putLong(System.nanoTime()).putBytes(new byte[6])
                .putByte(0).written();
    }

    public static byte[] heartbeatResponse(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 0x20) throw new IOException("Unexpected game packet");
        data.bytes(33);
        data.end();
        return new PacketData(5).putByte(0x20).putInt((int) (System.currentTimeMillis() / 1000)).written();
    }

    public static void verifyHeartbeat(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 0x20) throw new IOException("Unexpected heartbeat response");
        data.intValue();
        data.end();
    }
}
