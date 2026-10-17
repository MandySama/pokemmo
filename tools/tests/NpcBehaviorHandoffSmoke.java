import top.totodile.pokemmo.protocol.*;
import top.totodile.pokemmo.protocol.CharacterPackets.Position;
import java.io.*;
import java.util.Base64;
import java.util.zip.*;

public final class NpcBehaviorHandoffSmoke {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static NpcPackets.State girl(KantoNpcs npcs, Position p, long now) {
        return npcs.snapshot(p, now).states().stream().filter(n -> n.id() == 1).findFirst().orElseThrow();
    }
    private static void finish(GbaScriptRuntime vm, long now) throws Exception {
        for (int i = 0; i < 300 && vm.active(); i++) {
            now += 600_000_000L; vm.tick(now);
            if (vm.mode() == GbaScriptRuntime.BUTTON)
                vm.acknowledge(vm.serial(), StoryPackets.ADVANCE, 0, "", now);
        }
        check(!vm.active(), "native dialogue finishes");
    }
    private static void still(KantoNpcs npcs, Position p, long now, int x, int y) {
        for (int i = 0; i < 100; i++) {
            now += 1_000_000_000L; npcs.update(p, now);
            var state = girl(npcs, p, now);
            check(state.x() == x && state.y() == y, "scripted standing does not wander");
        }
    }
    public static void main(String[] args) throws Exception {
        long now = System.nanoTime(); var p = new Position(3, 0, 5, 16, 3, 1);
        var npcs = new KantoNpcs(new FireRedWorldRules(0));
        var vm = new GbaScriptRuntime("", p, (op, a, b, text) -> 1);
        vm.onPosition(p, now); finish(vm, now);
        String legacy = legacy(vm.save());
        vm.resumeNpcs(npcs, now + 200_000_000_000L);
        var state = girl(npcs, p, now);
        check(state.x() == 5 && state.y() == 15, "map initialization moves girl to sign");
        still(npcs, p, now + 201_000_000_000L, 5, 15);
        check(vm.interact(p, 1, now + 400_000_000_000L), "native sign interaction");
        finish(vm, now + 400_000_000_000L);
        vm.resumeNpcs(npcs, now + 700_000_000_000L);
        state = girl(npcs, p, now);
        check(state.x() == 4 && state.y() == 15, "ROM left sidestep");
        still(npcs, p, now + 701_000_000_000L, 4, 15);
        vm = new GbaScriptRuntime(vm.save(), p, (op, a, b, text) -> 1);
        vm.resetNpcs(npcs, now + 900_000_000_000L);
        still(npcs, p, now + 901_000_000_000L, 4, 15);
        for (int i = 0; i < 20; i++) npcs.update(new Position(1, i, 0, 0, 3, 1), now + 1_100_000_000_000L + i);
        still(npcs, p, now + 1_101_000_000_000L, 4, 15);
        // A roaming rule transferred outside the original bounds uses the new permanent origin.
        npcs.resume(3, 0, new NpcPackets.State(1, 12, 8, 12, 8, 1, 0), 2, 12, 8, now);
        boolean moved = false;
        for (int i = 0; i < 300; i++) {
            npcs.update(p, now + (i + 1) * 1_000_000_000L); state = girl(npcs, p, now);
            check(Math.abs(state.x() - 12) <= 1 && Math.abs(state.y() - 8) <= 4, "new roaming bounds");
            moved |= state.x() != 12 || state.y() != 8;
        }
        check(moved, "roaming outside the original origin is not frozen");
        vm.resetScene(StoryProgressPackets.OPENING, now + 2_000_000_000_000L);
        vm.resetNpcs(npcs, now + 2_001_000_000_000L);
        state = girl(npcs, p, now);
        check(state.x() == 5 && state.y() == 15, "rollback clears stale NPC cache");
        var old = new GbaScriptRuntime(legacy, p, (op, a, b, text) -> 1);
        old.onPosition(p, now); finish(old, now); old.resetNpcs(npcs, now + 300_000_000_000L);
        still(npcs, p, now + 301_000_000_000L, 5, 15);
        System.out.println("PASS native map initialization, standing, sidestep, permanent origin, restored behavior, cache eviction, relocated roaming, rollback cache and v2 migration");
    }

    private static String legacy(String state) throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var in = new DataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(state))); var out = new DataOutputStream(bytes)) {
            out.writeUTF(in.readUTF()); out.writeUTF(oldCore(in.readUTF()));
            int count = in.readInt(); out.writeInt(count);
            for (int i = 0; i < count; i++) { out.writeInt(in.readInt()); out.writeUTF(oldCore(in.readUTF())); }
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }
    private static String oldCore(String value) throws Exception {
        byte[] bytes = Base64.getDecoder().decode(value);
        InputStream stream = new ByteArrayInputStream(bytes);
        if ((bytes[0] & 255) == 31 && (bytes[1] & 255) == 139) stream = new GZIPInputStream(stream);
        var raw = new ByteArrayOutputStream();
        try (var in = new DataInputStream(stream); var out = new DataOutputStream(raw)) {
            check(in.readInt() == 3, "fixture format"); out.writeInt(2);
            for (int i = 0; i < 14; i++) out.writeInt(in.readInt());
            out.writeLong(in.readLong()); out.writeBoolean(in.readBoolean()); out.writeBoolean(in.readBoolean());
            for (int i = 0; i < 8; i++) out.writeInt(in.readInt());
            for (int i = 0; i < 2; i++) { int count = in.readInt(); out.writeInt(count); for (int j = 0; j < count * 2; j++) out.writeInt(in.readInt()); }
            for (int i = 0; i < 3; i++) { int count = in.readInt(); out.writeInt(count); for (int j = 0; j < count; j++) out.writeInt(in.readInt()); }
            int count = in.readInt(); out.writeInt(count);
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < 12; j++) out.writeInt(in.readInt());
                out.writeBoolean(in.readBoolean()); out.writeBoolean(in.readBoolean()); out.writeLong(in.readLong());
                int size = in.readInt(); out.writeInt(size); out.write(in.readNBytes(size));
                in.readInt(); in.readInt(); in.readInt();
            }
        }
        var compressed = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(compressed)) { gzip.write(raw.toByteArray()); }
        return Base64.getEncoder().encodeToString(compressed.toByteArray());
    }
}
