package top.totodile.pokemmo.game;

import top.totodile.pokemmo.protocol.*;
import java.io.IOException;
import java.util.*;

/** 游戏服务持有的单打会话；引擎只接收正式种族和服务端个体快照。 */
final class ShowdownBattle implements AutoCloseable {
    private final long id=java.util.concurrent.ThreadLocalRandom.current().nextLong(1,Long.MAX_VALUE);
    private long generation;
    private List<PartyMember> party;
    private BattlePackets.State state;
    private final boolean trainerBattle;
    ShowdownBattle(List<PartyMember> party, EncounterCatalog.Selection encounter) throws IOException {
        this(party,wildParty(encounter),false);
    }
    private static List<PartyMember> wildParty(EncounterCatalog.Selection encounter) throws IOException {
        if(encounter.selections().size()!=1)throw new IOException("Single battle required");
        var d=PokemonIndividual.create(encounter.species(),encounter.level(),false,1,"Wild",0,0,0,0);
        int hp=d.stat(encounter.species(),encounter.level(),0);
        return List.of(new PartyMember(encounter.species(),encounter.shiny(),false,hp,hp,encounter.level(),d));
    }
    ShowdownBattle(List<PartyMember> party,List<PartyMember> opponents,boolean trainer) throws IOException {
        this.party=List.copyOf(party);trainerBattle=trainer;
        if(party.isEmpty()||party.stream().noneMatch(p->p.hp()>0)||opponents.isEmpty()||opponents.size()>6)throw new IOException("Invalid battle parties");
        byte[] data=ShowdownWorker.call("{\"action\":\"start\",\"battle\":\""+id+"\",\"party\":"+team(party,false)+",\"enemy\":"+team(opponents,true)+",\"allowRun\":"+!trainer+"}",0);
        generation=ShowdownWorker.generation();state=read(data);
    }
    BattlePackets.State state() {return state;}
    BattlePackets.State handle(BattlePackets.Request request) throws IOException {
        if(request.action()==BattlePackets.START)return state;
        if(request.battle()!=id||request.revision()!=state.revision()||state.outcome()!=0)return rejected();
        if(trainerBattle&&request.action()==BattlePackets.RUN)return rejected();
        String action=switch(request.action()){case BattlePackets.MOVE->"move";case BattlePackets.SWITCH->"switch";case BattlePackets.RUN->"run";default->throw new IOException("Invalid battle action");};
        state=read(ShowdownWorker.call("{\"action\":\""+action+"\",\"battle\":\""+id+"\",\"revision\":"+request.revision()+",\"slot\":"+request.slot()+"}",generation));
        return state;
    }
    private BattlePackets.State rejected(){return new BattlePackets.State(false,id,state.revision(),state.outcome(),state.active(),state.forceSwitch(),state.enemyHp(),state.enemyMaxHp(),party,List.of(),state.legalMoves(),state.options(),state.enemyActive());}
    private BattlePackets.State read(byte[] bytes) throws IOException {
        var d=new PacketData(bytes);boolean accepted=d.booleanValue();int revision=d.shortValue(),outcome=d.byteValue(),active=d.byteValue();
        boolean forced=d.booleanValue();int enemyHp=d.shortValue(),enemyMax=d.shortValue(),count=d.byteValue();
        if(count!=party.size())throw new IOException("Engine party mismatch");
        var next=new ArrayList<PartyMember>();String[] statuses={"","brn","par","slp","frz","psn","tox"};
        for(var p:party){int hp=d.shortValue(),status=d.byteValue(),n=d.byteValue();var individual=p.individual();var moves=new ArrayList<PokemonIndividual.Move>();
            if(status>=statuses.length||n!=Math.max(1,individual.moves().size()))throw new IOException("Engine move mismatch");
            for(int i=0;i<n;i++){int pp=d.byteValue();if(i<individual.moves().size()){var old=individual.moves().get(i);moves.add(new PokemonIndividual.Move(old.id(),pp,old.maxPp(),old.ppUps()));}}
            next.add(new PartyMember(p.species(),p.shiny(),p.alpha(),hp,p.maxHp(),p.level(),individual.battleState(moves,statuses[status])));
        }
        int n=d.shortValue();if(n>1024)throw new IOException("Engine event overflow");var events=new ArrayList<BattlePackets.Event>();
        for(int i=0;i<n;i++)events.add(new BattlePackets.Event(d.byteValue(),d.byteValue(),d.shortValue()));
        n=d.byteValue();if(n>4)throw new IOException("Engine move overflow");var legal=new ArrayList<Integer>();for(int i=0;i<n;i++)legal.add(d.byteValue());
        n=d.byteValue();if(n>4)throw new IOException("Engine option overflow");var options=new ArrayList<BattlePackets.MoveOption>();
        for(int i=0;i<n;i++)options.add(new BattlePackets.MoveOption(d.byteValue(),d.shortValue(),d.byteValue(),d.byteValue()));int enemyActive=d.byteValue();d.end();
        if(active>=count||outcome>4||enemyHp>enemyMax)throw new IOException("Invalid engine state");
        party=List.copyOf(next);return new BattlePackets.State(accepted,id,revision,outcome,active,forced,enemyHp,enemyMax,party,List.copyOf(events),List.copyOf(legal),List.copyOf(options),enemyActive);
    }
    private static String team(List<PartyMember> party,boolean allowItems) throws IOException {
        var result=new ArrayList<String>();
        for(var p:party){var d=p.individual();if(d==null)throw new IOException("Missing individual");
            var stats=new ArrayList<Integer>();for(int i=0;i<6;i++)stats.add(d.stat(p.species(),p.level(),i));
            String moves=d.moves().stream().map(m->"["+m.id()+","+m.pp()+","+m.maxPp()+"]").collect(java.util.stream.Collectors.joining(",","[","]"));
            // 本项目道具编号与引擎编号尚未建立完整映射，先拒绝携带物而非使用错误效果。
            if(!allowItems&&d.heldItem()!=0)throw new IOException("Held items require battle mapping");
            result.add("{\"species\":"+p.species()+",\"level\":"+p.level()+",\"hp\":"+p.hp()+",\"ability\":"+d.ability()+",\"gender\":"+d.gender()+",\"friendship\":"+d.friendship()+",\"item\":"+d.heldItem()+",\"ivs\":"+d.ivs()+",\"evs\":"+d.evs()+",\"stats\":"+stats+",\"status\":\""+d.status()+"\",\"moves\":"+moves+"}");
        }return "["+String.join(",",result)+"]";
    }
    @Override public void close(){try{ShowdownWorker.call("{\"action\":\"close\",\"battle\":\""+id+"\"}",generation);}catch(IOException ignored){}}
}
