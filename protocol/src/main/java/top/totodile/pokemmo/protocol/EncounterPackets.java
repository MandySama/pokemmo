package top.totodile.pokemmo.protocol;

import java.io.IOException;

/** 遭遇开关、主动诱敌与离场确认；位置只取世界会话。 */
public final class EncounterPackets {
    public static final int OPCODE = 0x86, QUERY = 0, SET = 1, SCENT = 2, FINISH = 3;
    public record Snapshot(boolean enabled, boolean accepted, EncounterCatalog.Selection encounter) { }
    private EncounterPackets() { }
    public static byte[] request(int action, boolean enabled) {
        return new PacketData(3).putByte(OPCODE).putByte(action).putByte(enabled ? 1 : 0).written();
    }
    public static byte[] response(Snapshot result) {
        var data = new PacketData(32).putByte(OPCODE).putByte(result.enabled() ? 1 : 0).putByte(result.accepted() ? 1 : 0);
        writeSelection(data, result.encounter());
        return data.written();
    }
    public static Snapshot decode(byte[] packet) throws IOException {
        var data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Unexpected encounter response");
        var result = new Snapshot(data.booleanValue(), data.booleanValue(), readSelection(data));
        data.end(); return result;
    }
    static void writeSelection(PacketData data, EncounterCatalog.Selection encounter) {
        data.putByte(encounter == null ? 0 : encounter.selections().size());
        if (encounter != null) for (var member : encounter.selections())
            data.putShort(member.species()).putByte(member.level()).putByte(member.kind()).putByte(member.shiny() ? 1 : 0);
    }

    static EncounterCatalog.Selection readSelection(PacketData data) throws IOException {
        int count = data.byteValue();
        if (count == 0) return null;
        if (count != 1 && count != 3 && count != 5) throw new IOException("Invalid encounter size");
        try {
            var members = new java.util.ArrayList<EncounterCatalog.Selection>();
            for (int i = 0; i < count; i++)
                members.add(new EncounterCatalog.Selection(data.shortValue(), data.byteValue(), data.byteValue(), data.booleanValue()));
            var first = members.getFirst();
            return new EncounterCatalog.Selection(first.species(), first.level(), first.kind(), first.shiny(), members.subList(1, count));
        } catch (IllegalArgumentException error) { throw new IOException("Invalid encounter", error); }
    }
}
