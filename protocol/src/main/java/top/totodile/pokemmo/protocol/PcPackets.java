package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 电脑箱子的队伍、箱子查询与搬移；角色与账号由游戏会话确定。
 * 单帧上限 4096 字节且不做分片，箱子按占用格的实际编码大小分页返回，
 * 客户端拿到的 {@code more} 为真时以 {@code start + entries.size()} 续取。
 */
public final class PcPackets {
    public static final int OPCODE = 0x7C;
    public static final int FETCH_PARTY = 0, FETCH_BOX = 1, MOVE = 2, CREATE_BOX = 3;
    public static final int PARTY = 0, BOX = 1;
    public static final int MAX_BOXES = 13, BOX_COLUMNS = 10, BOX_ROWS = 6, BOX_SLOTS = BOX_COLUMNS * BOX_ROWS;
    /** 单页个体数据预算，留出封包头与哈希长度后仍小于通道帧上限。 */
    public static final int PAGE_BUDGET = 3400;

    public enum Status { SUCCESS, DISABLED, INVALID, PARTY_FULL, BOX_FULL, SYSTEM_ERROR }

    public record Entry(int slot, PartyMember member) { }

    public record PartyState(int boxes, List<PartyMember> party) { }

    public record BoxPage(int box, int start, boolean more, List<Entry> entries) { }

    public record Move(int fromKind, int fromBox, int fromSlot, int toKind, int toBox, int toSlot) {
        public Move {
            if (!valid(fromKind, fromBox, fromSlot) || !valid(toKind, toBox, toSlot))
                throw new IllegalArgumentException("Invalid PC move");
        }

        private static boolean valid(int kind, int box, int slot) {
            if (kind == PARTY) return slot >= 0 && slot < 6;
            return kind == BOX && box >= 0 && box < MAX_BOXES && slot >= 0 && slot < BOX_SLOTS;
        }
    }

    private PcPackets() { }

    public static byte[] fetchParty() {
        return new PacketData(2).putByte(OPCODE).putByte(FETCH_PARTY).written();
    }

    public static byte[] fetchBox(int box, int start) {
        if (box < 0 || box >= MAX_BOXES || start < 0 || start >= BOX_SLOTS) throw new IllegalArgumentException("Invalid PC box page");
        return new PacketData(4).putByte(OPCODE).putByte(FETCH_BOX).putByte(box).putByte(start).written();
    }

    public static byte[] move(Move move) {
        return new PacketData(9).putByte(OPCODE).putByte(MOVE)
                .putByte(move.fromKind()).putByte(move.fromBox()).putByte(move.fromSlot())
                .putByte(move.toKind()).putByte(move.toBox()).putByte(move.toSlot()).written();
    }

    public static byte[] createBox() {
        return new PacketData(2).putByte(OPCODE).putByte(CREATE_BOX).written();
    }

    public static byte[] partyResponse(Status status, int boxes, List<PartyMember> party) {
        PacketData data = new PacketData(4096).putByte(OPCODE).putByte(FETCH_PARTY).putByte(status.ordinal()).putByte(boxes);
        PartyMember.write(data, party);
        return data.written();
    }

    public static PartyState decodePartyResponse(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != OPCODE || data.byteValue() != FETCH_PARTY) throw new IOException("Unexpected PC party response");
        Status status = status(data);
        int boxes = data.byteValue();
        if (boxes < 1 || boxes > MAX_BOXES) throw new IOException("Invalid PC box count");
        List<PartyMember> party = PartyMember.read(data);
        data.end();
        if (status != Status.SUCCESS) return new PartyState(boxes, List.of());
        return new PartyState(boxes, party);
    }

    /** 容量按占用格的实际编码大小预留；是否分页仍由服务端装箱逻辑决定。 */
    public static byte[] boxResponse(Status status, int box, int start, boolean more, List<Entry> entries) {
        int capacity = 16;
        for (Entry entry : entries) capacity += 16 + (entry.member().individual() == null ? 0 : entry.member().individual().encode().length);
        PacketData data = new PacketData(capacity).putByte(OPCODE).putByte(FETCH_BOX).putByte(status.ordinal())
                .putByte(box).putByte(start).putByte(more ? 1 : 0).putByte(entries.size());
        for (Entry entry : entries) {
            data.putByte(entry.slot());
            PartyMember.write(data, List.of(entry.member()));
        }
        return data.written();
    }

    public static BoxPage decodeBoxResponse(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != OPCODE || data.byteValue() != FETCH_BOX) throw new IOException("Unexpected PC box response");
        Status status = status(data);
        int box = data.byteValue(), start = data.byteValue();
        boolean more = data.booleanValue();
        int count = data.byteValue();
        if (box < 0 || box >= MAX_BOXES || start < 0 || start >= BOX_SLOTS || count > BOX_SLOTS)
            throw new IOException("Invalid PC box page");
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int slot = data.byteValue();
            List<PartyMember> member = PartyMember.read(data);
            if (member.size() != 1 || slot < start || slot >= BOX_SLOTS) throw new IOException("Invalid PC box entry");
            entries.add(new Entry(slot, member.getFirst()));
        }
        data.end();
        if (status != Status.SUCCESS) return new BoxPage(box, start, false, List.of());
        return new BoxPage(box, start, more, List.copyOf(entries));
    }

    public static byte[] statusResponse(int action, Status status) {
        return new PacketData(3).putByte(OPCODE).putByte(action).putByte(status.ordinal()).written();
    }

    public static Status decodeStatusResponse(byte[] packet, int action) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != OPCODE || data.byteValue() != action) throw new IOException("Unexpected PC result");
        Status status = status(data);
        data.end();
        return status;
    }

    public static Move decodeMove(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != OPCODE || data.byteValue() != MOVE) throw new IOException("Unexpected PC move");
        Move move = new Move(data.byteValue(), data.byteValue(), data.byteValue(), data.byteValue(), data.byteValue(), data.byteValue());
        data.end();
        return move;
    }

    public static int[] decodeBoxRequest(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != OPCODE || data.byteValue() != FETCH_BOX) throw new IOException("Unexpected PC box request");
        int box = data.byteValue(), start = data.byteValue();
        data.end();
        if (box >= MAX_BOXES || start >= BOX_SLOTS) throw new IOException("Invalid PC box request");
        return new int[]{box, start};
    }

    /** 只读取动作字节；后续字段由各动作自己的解码方法校验。 */
    public static int decodeAction(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Unexpected PC command");
        int action = data.byteValue();
        if (action < FETCH_PARTY || action > CREATE_BOX) throw new IOException("Unknown PC command");
        return action;
    }

    private static Status status(PacketData data) throws IOException {
        int value = data.byteValue();
        if (value >= Status.values().length) throw new IOException("Invalid PC status");
        return Status.values()[value];
    }
}
