package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.List;

/** 只发送队伍槽位；跟随对象由服务端从当前认证角色的队伍读取。 */
public final class FollowerPackets {
    public static final int OPCODE = 0x7a;
    private FollowerPackets() { }
    public static byte[] request(int index) {
        if (index < -1 || index > 5) throw new IllegalArgumentException("Invalid follower slot");
        return new byte[]{(byte) OPCODE, (byte) index};
    }
    public static int decodeRequest(byte[] packet) throws IOException {
        if (packet.length != 2 || packet[0] != OPCODE) throw new IOException("Invalid follower request");
        int index = packet[1] & 255;
        if (index == 255) return -1;
        if (index > 5) throw new IOException("Invalid follower slot");
        return index;
    }
    public static byte[] response(boolean success, PartyMember member) {
        PacketData data = new PacketData(2048).putByte(OPCODE).putByte(success ? 1 : 0);
        PartyMember.write(data, member == null ? List.of() : List.of(member));
        return data.written();
    }
    public static PartyMember decodeResponse(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != OPCODE || !data.booleanValue()) throw new IOException("Follower rejected");
        List<PartyMember> members = PartyMember.read(data);
        data.end();
        if (members.size() > 1) throw new IOException("Invalid follower response");
        return members.isEmpty() ? null : members.getFirst();
    }
}