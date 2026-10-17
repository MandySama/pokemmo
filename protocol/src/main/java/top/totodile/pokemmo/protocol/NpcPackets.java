package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** NPC 快照仅含地图事件编号与运动状态；对话文本由客户端 ROM 脚本定位。 */
public final class NpcPackets {
    public static final int OPCODE = 0x82;

    public record State(int id, int x, int y, int fromX, int fromY, int facing, int remaining) {}

    public record Snapshot(int group, int map, int talking, List<State> states, int dialogueText) {
        public Snapshot(int group,int map,int talking,List<State> states) { this(group,map,talking,states,0); }
        public Snapshot {
            states = List.copyOf(states);
        }
    }

    private NpcPackets() {}

    public static byte[] request(int action, int id) {
        if (action < 0 || action > 3 || id < 0 || id > 255)
            throw new IllegalArgumentException("Invalid NPC request");
        return new byte[] {(byte) OPCODE, (byte) action, (byte) id};
    }

    public static byte[] response(Snapshot snapshot) {
        PacketData data =
                new PacketData(8192)
                        .putByte(OPCODE)
                        .putByte(snapshot.group())
                        .putByte(snapshot.map())
                        .putShort(snapshot.talking() + 1)
                        .putShort(snapshot.states().size());
        for (State state : snapshot.states())
            data.putByte(state.id())
                    .putShort(state.x())
                    .putShort(state.y())
                    .putShort(state.fromX())
                    .putShort(state.fromY())
                    .putByte(state.facing())
                    .putShort(state.remaining());
        data.putInt(snapshot.dialogueText());
        return data.written();
    }

    public static Snapshot decode(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Invalid NPC response");
        int group = data.byteValue(),
                map = data.byteValue(),
                talking = data.shortValue() - 1,
                count = data.shortValue();
        if (count > 255 || talking > 255) throw new IOException("Invalid NPC count");
        List<State> states = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            State state =
                    new State(
                            data.byteValue(),
                            data.shortValue(),
                            data.shortValue(),
                            data.shortValue(),
                            data.shortValue(),
                            data.byteValue(),
                            data.shortValue());
            if (state.facing() > 3
                    || state.remaining() > 550
                    || state.x() > 1023
                    || state.y() > 1023
                    || Math.abs(state.x() - state.fromX()) + Math.abs(state.y() - state.fromY())
                            > 1) throw new IOException("Invalid NPC state");
            states.add(state);
        }
        int dialogueText=data.intValue();
        data.end();
        return new Snapshot(group, map, talking, states,dialogueText);
    }
}
