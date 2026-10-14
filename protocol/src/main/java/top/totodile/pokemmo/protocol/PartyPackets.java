package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.List;

/** 队伍排序请求仅包含槽位，角色与账号由游戏会话确定。 */
public final class PartyPackets {
    public static final int OPCODE = 0x79;
    private PartyPackets() { }
    public static byte[] move(int from, int to) {
        if (from < 0 || from >= 6 || to < 0 || to >= 6 || from == to) throw new IllegalArgumentException("Invalid party move");
        return new PacketData(3).putByte(OPCODE).putByte(from).putByte(to).written();
    }
    public static int[] decodeMove(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Unexpected party command");
        int from = data.byteValue(), to = data.byteValue();
        data.end();
        if (from >= 6 || to >= 6 || from == to) throw new IOException("Invalid party move");
        return new int[]{from, to};
    }
    public static byte[] response(boolean success, List<PartyMember> party) {
        PacketData data = new PacketData(8192).putByte(OPCODE).putByte(success ? 1 : 0);
        PartyMember.write(data, party);
        return data.written();
    }
    public static List<PartyMember> decodeResponse(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != OPCODE || !data.booleanValue()) throw new IOException("Party move rejected");
        List<PartyMember> party = PartyMember.read(data);
        data.end();
        return party;
    }
}