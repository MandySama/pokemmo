package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.List;

/** 入图后查询当前角色未确认离场的战斗，快照不重播已结算事件。 */
public final class BattleResumePackets {
    public static final int OPCODE = 0x8a;
    public record Snapshot(boolean trainerBattle, boolean scriptBattle, EncounterCatalog.Selection encounter,
                           TrainerPackets.Ready trainer, List<PartyMember> opponents, int faintMask, BattlePackets.State state) {
        public Snapshot { opponents = List.copyOf(opponents); }
        public static Snapshot empty() { return new Snapshot(false, false, null, null, List.of(), 0, null); }
    }
    private BattleResumePackets() { }
    public static byte[] request() { return new byte[]{(byte) OPCODE}; }
    public static byte[] response(Snapshot s) {
        var d = new PacketData(32000).putByte(OPCODE).putByte(s.state() == null ? 0 : 1);
        if (s.state() != null) {
            d.putByte(s.trainerBattle() ? 1 : 0).putByte(s.scriptBattle() ? 1 : 0);
            EncounterPackets.writeSelection(d, s.encounter());
            d.putByte(s.trainer() == null ? 0 : 1);
            if (s.trainer() != null) d.putShort(s.trainer().id()).putString(s.trainer().name()).putInt(s.trainer().defeat()).putInt(s.trainer().after());
            PartyMember.write(d, s.opponents()); d.putByte(s.faintMask());
            byte[] state = BattlePackets.response(s.state()); d.putShort(state.length).putBytes(state);
        }
        return d.written();
    }
    public static Snapshot decode(byte[] bytes) throws IOException {
        var d = new PacketData(bytes);
        if (d.byteValue() != OPCODE) throw new IOException("Invalid battle recovery response");
        if (!d.booleanValue()) { d.end(); return Snapshot.empty(); }
        boolean trainer = d.booleanValue(), script = d.booleanValue();
        var encounter = EncounterPackets.readSelection(d);
        var details = d.booleanValue() ? new TrainerPackets.Ready(1,d.shortValue(),d.string(16),d.intValue(),d.intValue(),List.of()) : null;
        var opponents = PartyMember.read(d); int mask = d.byteValue();
        if (details != null) details = new TrainerPackets.Ready(1,details.id(),details.name(),details.defeat(),details.after(),opponents);
        var state = BattlePackets.decode(d.bytes(d.shortValue())); d.end();
        if (opponents.isEmpty() || opponents.size() > 6 || state.enemyActive() >= opponents.size()
                || state.active() >= state.party().size() || (!trainer && encounter == null)) throw new IOException("Invalid battle recovery state");
        return new Snapshot(trainer, script, encounter, details, opponents, mask, state);
    }
}
