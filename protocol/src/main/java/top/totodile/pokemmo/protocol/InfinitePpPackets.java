package top.totodile.pokemmo.protocol;

import java.io.IOException;

/** 当前认证角色的无限 PP 开关；查询不改变状态。 */
public final class InfinitePpPackets {
    public static final int OPCODE = 0x8B, QUERY = 0, SET = 1;
    public record Request(int action, boolean enabled) { }
    public record State(boolean enabled, boolean accepted) { }
    private InfinitePpPackets() { }
    public static byte[] request(int action, boolean enabled) {
        return new PacketData(3).putByte(OPCODE).putByte(action).putByte(enabled ? 1 : 0).written();
    }
    public static Request read(byte[] bytes) throws IOException {
        var data = new PacketData(bytes);
        if (data.byteValue() != OPCODE) throw new IOException("Invalid infinite PP opcode");
        var request = new Request(data.byteValue(), data.booleanValue()); data.end();
        if (request.action() > SET) throw new IOException("Invalid infinite PP action");
        return request;
    }
    public static byte[] response(State state) {
        return new PacketData(3).putByte(OPCODE).putByte(state.enabled() ? 1 : 0).putByte(state.accepted() ? 1 : 0).written();
    }
    public static State decode(byte[] bytes) throws IOException {
        var data = new PacketData(bytes);
        if (data.byteValue() != OPCODE) throw new IOException("Invalid infinite PP response");
        var state = new State(data.booleanValue(), data.booleanValue()); data.end(); return state;
    }
}
