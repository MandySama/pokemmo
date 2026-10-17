package top.totodile.pokemmo.game;
import top.totodile.pokemmo.protocol.*;
import java.util.*;
import java.nio.file.*;
public final class BattleStatusFeedbackSmoke {
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    static PartyMember member(int species,int move,String status){
        var d=PokemonIndividual.create(species,50,false,1,"Test",0,0,0,0);
        d=d.battleState(List.of(new PokemonIndividual.Move(move,20,20,0)),status);
        int hp=d.stat(species,50,0);return new PartyMember(species,false,false,hp,hp,50,d);
    }
    static BattlePackets.State turn(ShowdownBattle b){var s=b.state();try{return b.handle(new BattlePackets.Request(BattlePackets.MOVE,s.battle(),s.revision(),1));}catch(Exception e){throw new RuntimeException(e);}}
    static boolean has(BattlePackets.State s,int kind){return s.events().stream().anyMatch(e->e.kind()==kind);}
    public static void main(String[]args)throws Exception{
        try(var b=new ShowdownBattle(List.of(member(25,609,"")),List.of(member(143,150,"")),false)){
            var initial=b.snapshot();Files.write(Path.of("target/battle-status-initial.bin"),BattlePackets.response(initial));
            var s=turn(b);check(s.enemyStatus()==2&&has(s,2),"real Nuzzle paralysis survives Java adapter");
            check(BattlePackets.decode(BattlePackets.response(s)).equals(s),"status response round trip");
            Files.write(Path.of("target/battle-status-result.bin"),BattlePackets.response(s));
            check(b.snapshot().enemyStatus()==2&&b.snapshot().events().isEmpty(),"recovery keeps status without event history");
            var snap=new BattleResumePackets.Snapshot(false,false,new EncounterCatalog.Selection(143,50,0,false),null,b.opponents(),0,b.snapshot());
            check(BattleResumePackets.decode(BattleResumePackets.response(snap)).state().enemyStatus()==2,"resume packet carries enemy status");
        }
        try(var b=new ShowdownBattle(List.of(member(25,105,"")),List.of(member(143,150,"")),false)){check(has(turn(b),BattlePackets.FAIL),"full HP Recover failure preserved");}
        try(var b=new ShowdownBattle(List.of(member(25,33,"slp")),List.of(member(143,150,"")),false)){var s=turn(b);check(s.events().stream().anyMatch(e->e.kind()==BattlePackets.CANT&&e.value()==3),"sleep cant preserved");}
        try(var b=new ShowdownBattle(List.of(member(25,33,"")),List.of(member(92,150,"")),false)){check(has(turn(b),BattlePackets.IMMUNE),"type immunity preserved");}
        try(var b=new ShowdownBattle(List.of(member(25,33,"")),List.of(member(143,182,"")),false)){check(has(turn(b),BattlePackets.BLOCK),"Protect block preserved");}
        System.out.println("PASS real Node/Java/protocol: status, persistent snapshot/recovery, Recover failure, sleep cant, immunity and Protect");
    }
}
