import top.totodile.pokemmo.protocol.*;
import top.totodile.pokemmo.protocol.CharacterPackets.Position;
import java.util.*;

public final class ScriptDiagnosticSmoke {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static void finish(GbaScriptRuntime vm, long now) throws Exception {
        for (int i=0; i<300 && vm.active(); i++) {
            now += 600_000_000L; vm.tick(now);
            if (vm.mode()==GbaScriptRuntime.BUTTON) vm.acknowledge(vm.serial(), StoryPackets.ADVANCE, 0, "", now);
        }
        check(!vm.active(), "dialogue completes");
    }
    public static void main(String[] args) throws Exception {
        long now=System.nanoTime(); var p=new Position(3,0,5,16,3,1);
        var npcs=new KantoNpcs(new FireRedWorldRules());
        var vm=new GbaScriptRuntime("",p,(op,a,b,text)->1);
        vm.onPosition(p,now); finish(vm,now); vm.resumeNpcs(npcs,now+300_000_000_000L);
        npcs.update(p,now+301_000_000_000L);
        var before=vm.save(); long version=vm.version(); var daily=npcs.diagnostic(3,0,1);
        var snapshot=vm.diagnostics(npcs,now);
        for(int i=0;i<100;i++) vm.diagnostics(npcs,now+i*1_000_000_000L);
        check(vm.save().equals(before) && vm.version()==version,"queries never mutate saved state or version");
        check(daily.equals(npcs.diagnostic(3,0,1)),"queries never advance daily NPCs");
        check(snapshot.rows().stream().anyMatch(r->r.label()==252043&&r.value().startsWith("1 / 23  (5, 15)")),"live girl location");
        check(ScriptDiagnosticPackets.decode(ScriptDiagnosticPackets.response(snapshot)).equals(snapshot),"packet round trip");
        try { ScriptDiagnosticPackets.readRequest(new byte[]{(byte)0x8c,1}); throw new AssertionError("trailing request accepted"); } catch(java.io.IOException expected) { }
        check(vm.interact(vm.position(),1,now+400_000_000_000L),"native interaction"); finish(vm,now+400_000_000_000L);
        vm.resumeNpcs(npcs,now+700_000_000_000L); snapshot=vm.diagnostics(npcs,now);
        for(int label:new int[]{252052,252053,252059,252062,252063,252067}) {
            final int id=label;check(snapshot.rows().stream().anyMatch(r->r.label()==id),"native trace label "+label);
        }
        check(snapshot.rows().stream().anyMatch(r->r.label()==252054||r.label()==252055),"actual branch result");
        check(snapshot.rows().stream().anyMatch(r->r.label()==252043&&r.value().startsWith("1 / 23  (4, 15)")),"sidestep result");
        var restored=new GbaScriptRuntime(vm.save(),p,(op,a,b,text)->1);
        check(restored.diagnostics(null,now).rows().stream().anyMatch(r->r.label()==252051&&r.value().equals("0")),"trace is not saved");
        for(int i=0;i<10;i++){vm.onPosition(new Position(3,0,4,16,3,1),now+i*900_000_000_000L);finish(vm,now+i*900_000_000_000L);vm.interact(vm.position(),1,now+i*900_000_000_000L);finish(vm,now+i*900_000_000_000L);}
        check(vm.diagnostics(npcs,now).rows().stream().filter(r->r.label()==252051).allMatch(r->Integer.parseInt(r.value())<=96),"bounded trace");
        System.out.println("PASS read-only state/version/NPCs, packet validation, native Pallet branches/dialogue/movement, bounded nonpersistent trace");
    }
}
