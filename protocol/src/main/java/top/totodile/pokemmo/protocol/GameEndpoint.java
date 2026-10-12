package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.net.InetAddress;

/** 选择服务器响应 0x03，成功时携带账号、临时凭证和连接地址。 */
public record GameEndpoint(int accountId, int serverId, String host, int port, byte[] ticket) {
    public static final class Rejected extends IOException {
        private final int status;

        public Rejected(int status) {
            super("Game server unavailable: " + status);
            this.status = status;
        }

        public int status() {
            return status;
        }
    }

    public GameEndpoint {
        ticket = ticket.clone();
    }

    @Override
    public byte[] ticket() {
        return ticket.clone();
    }

    public byte[] encode() throws IOException {
        byte[] address = InetAddress.getByName(host).getAddress();
        reverse(address);
        return new PacketData(900).putByte(3).putByte(0).putInt(accountId).putByte(ticket.length)
                .putBytes(ticket).putByte(serverId).putByte(address.length).putBytes(address).putString(host)
                .putInt(port).putByte(0).written();
    }

    public static GameEndpoint decode(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 3) throw new IOException("Unexpected join result opcode");
        int status = data.byteValue();
        if (status != 0) {
            data.end();
            throw new Rejected(status);
        }
        int accountId = data.intValue();
        byte[] ticket = data.bytes(data.byteValue());
        int serverId = data.byteValue();
        int length = data.byteValue();
        if (length != 4 && length != 16) throw new IOException("Invalid game address");
        byte[] address = data.bytes(length);
        reverse(address);
        String host = data.string(253);
        int port = data.intValue();
        if (data.byteValue() != 0) throw new IOException("Unexpected game endpoints");
        data.end();
        if (accountId < 1 || serverId < 1 || port < 1 || port > 65535 || ticket.length == 0) {
            throw new IOException("Invalid game endpoint");
        }
        return new GameEndpoint(accountId, serverId,
                host.isBlank() ? InetAddress.getByAddress(address).getHostAddress() : host, port, ticket);
    }

    private static void reverse(byte[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) {
            byte value = bytes[i];
            bytes[i] = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = value;
        }
    }
}
