package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import top.totodile.pokemmo.server.game.battle.BattleState;
import java.util.*;
import java.util.stream.Collectors;

/** Persistent PvE only. BattleContextState experience pool; SendBattleFinish monetary settlement. */
final class BattleProgress {
    private EncounterData initial;
    private final Map<String, Set<String>> participants = new HashMap<>();
    private final Set<String> settled = new HashSet<>();
    private int payday;
    private boolean finished;
    BattleProgress(EncounterData initial) {
        this.initial=initial;
        for(var enemy:initial.wilds()) participants.put(enemy.id(),new LinkedHashSet<>());
        participants.get(initial.wild().id()).add(initial.player().id());
    }
    static int experience(int enemyLevel, int yield, int playerLevel, int count, boolean trainer) {
        double correction=enemyLevel/(double)(enemyLevel+playerLevel);
        if(correction<.5) correction*=1.5;
        int base=(int)(enemyLevel*yield/7.0*correction+1);
        if(trainer) base=(int)(base*1.5);
        int split=Math.max(1,base/count);
        // Reference BattleType.isPve -> PokemonGetExpData trainerBattleBonus (.1), also wild.
        return split+(int)(split*.1);
    }
    BattleState settle(Database db, String owner, Map<Integer,GameService.SpeciesTemplate> templates,
            Map<Integer,GameService.BattleYield> yields, BattleState state) {
        if(finished) return new BattleState(state.engine(),state.turn(),state.ended(),state.winner(),
                state.forceSwitch(),state.pokemon(),state.logs(),state.actions(),List.of(),List.of());
        var events=new ArrayList<>(state.events());
        var current=state.apply(initial);
        var active=state.pokemon().stream().filter(p->p.side()==0&&p.active()).findFirst().orElse(null);
        for(var enemy:state.pokemon()) if(enemy.side()==1 && enemy.active() && active!=null)
            participants.get(enemy.id()).add(active.id());
        // Only an actual direct damage result from Pay Day counts; misses, immunity and previews do not.
        BattleState.Event lastMove=null;
        for(var event:state.events()) {
            if(event.type().equals("MOVE")) lastMove=event;
            if(event.type().equals("DAMAGE") && event.direct() && lastMove!=null && lastMove.move()==6) {
                var hit=lastMove;
                if(initial.party().stream().noneMatch(p->p.id().equals(hit.casterId()))) continue;
                var enemy=initial.wilds().stream().filter(p->p.id().equals(event.targetId())).findFirst().orElse(null);
                if(enemy!=null) payday=Math.max(payday,enemy.level()*10);
            }
        }
        db.transaction(() -> {
            for(var enemy:current.wilds()) if(enemy.hp()==0 && !settled.contains(enemy.id())) {
                var eligible=current.party().stream().filter(p->participants.get(enemy.id()).contains(p.id())
                        && p.hp()>0 && p.level()<100).toList();
                if(!eligible.isEmpty()) {
                    var yield=yields.get(enemy.species());
                    int killerLevel=active==null?eligible.get(0).level():current.party().stream()
                            .filter(p->p.id().equals(active.id())).findFirst().orElse(eligible.get(0)).level();
                    int amount=experience(enemy.level(),yield==null?50:yield.experience(),killerLevel,eligible.size(),initial.trainer()!=null);
                    for(var pokemon:eligible) {
                        var savedPokemon=new PokemonStore(db,templates).load(owner).pokemons().stream()
                                .filter(p->p.id().equals(pokemon.id())).findFirst().orElseThrow();
                        grow(db,owner,savedPokemon,amount,templates.get(pokemon.species()),yield);
                        events.add(new BattleState.Event("EXPERIENCE","",pokemon.id(),0,0,0,"",amount,"",false,false));
                        var grown=new PokemonStore(db,templates).load(owner).pokemons().stream()
                                .filter(p->p.id().equals(pokemon.id())).findFirst().orElseThrow();
                        if(grown.level()>savedPokemon.level())
                            events.add(new BattleState.Event("LEVEL","",pokemon.id(),0,0,0,"",grown.level(),"",false,false));
                    }
                }
            }
            if(state.ended() && "Player".equals(state.winner()) && initial.trainer()!=null && initial.trainer().money()>0) {
                int money=initial.trainer().money();
                db.execute("UPDATE characters SET money=MIN(2147483647,money+?) WHERE id=?",money,owner);
                events.add(new BattleState.Event("MONEY","",initial.player().id(),0,0,0,"",money,"",false,false));
            }
            // Ordinary wild victories award no base money. Pay Day is once per battle, highest hit level.
            if(state.ended() && "Player".equals(state.winner()) && payday>0) {
                db.execute("UPDATE characters SET money=MIN(2147483647,money+?) WHERE id=?",payday,owner);
                events.add(new BattleState.Event("PAYDAY","",initial.player().id(),0,0,0,"",payday,"",false,false));
            }
            for(var pokemon:current.party()) {
                var original=initial.party().stream().filter(p->p.id().equals(pokemon.id())).findFirst().orElseThrow();
                var savedPokemon=new PokemonStore(db,templates).load(owner).pokemons().stream().filter(p->p.id().equals(pokemon.id())).findFirst().orElseThrow();
                var pp=new ArrayList<>(savedPokemon.pp());
                for(int i=0;i<4;i++) if(savedPokemon.moves().get(i).equals(original.moves().get(i))&&original.moves().get(i).equals(pokemon.moves().get(i))) pp.set(i,pokemon.pp().get(i));
                db.execute("UPDATE pokemon SET current_hp=?,status=?,moves_pp=? WHERE trainer_id=? AND id=?",
                        pokemon.hp(),pokemon.status(),encode(pp),owner,pokemon.id());
            }
        });
        for(var enemy:current.wilds()) if(enemy.hp()==0) settled.add(enemy.id());
        finished=state.ended();
        var saved=new PokemonStore(db,templates).load(owner).pokemons().stream()
                .filter(p->p.container()==PokemonData.PARTY).toList();
        initial=new EncounterData(saved.stream().filter(p->p.id().equals(current.player().id())).findFirst().orElseThrow(),
                current.wilds(),saved,initial.trainer());
        return new BattleState(state.engine(),state.turn(),state.ended(),state.winner(),state.forceSwitch(),
                state.pokemon(),state.logs(),state.actions(),events,saved);
    }
    private static String encode(List<Integer> values) {
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
    static void grow(Database db,String owner,PokemonData.Pokemon p,int amount,
            GameService.SpeciesTemplate template,GameService.BattleYield yield) {
        if(template==null) throw new IllegalStateException("Missing growth template");
        int exp=Math.min(PokemonExperience.atLevel(template.growthRate(),100),
                Math.max(0,p.experience())+amount);
        int level=p.level();
        while(level<100 && exp>=PokemonExperience.atLevel(template.growthRate(),level+1)) level++;
        final int reachedLevel=level;
        learn(db,owner,p,template.moves().stream().filter(m->m.level()>p.level()&&m.level()<=reachedLevel).sorted(Comparator.comparingInt(GameService.LearnedMove::level)).toList());
        var evs=new ArrayList<>(p.evs());
        if(yield!=null) for(int i=0;i<6;i++) evs.set(i,Math.min(252,evs.get(i)+yield.evs().get(i)));
        var stats=stats(p,template,level,evs);
        db.execute("UPDATE pokemon SET exp=?,level_value=?,ev_values=?,stats=? WHERE trainer_id=? AND id=?",
                exp,level,encode(evs),encode(stats),owner,p.id());
        if(level>p.level()&&p.hp()>0){
            var grown=new PokemonStore(db,Map.of(template.id(),template)).load(owner).pokemons().stream().filter(mon->mon.id().equals(p.id())).findFirst().orElseThrow();
            PokemonEvolution.enqueue(db,owner,grown,Map.of(template.id(),template));
        }
    }
    static void learn(Database db,String owner,PokemonData.Pokemon p,List<GameService.LearnedMove> candidates){
        var moves=new ArrayList<>(p.moves());var pp=new ArrayList<>(p.pp());var ups=new ArrayList<>(p.ppUps());
        // Native empty slots learn automatically. A full set must never silently forget a move.
        for(var learned:candidates){
            if(learned.id()<=0||moves.contains(learned.id()))continue;
            int slot=moves.indexOf(0);if(slot<0){db.execute("INSERT OR IGNORE INTO character_move_learning VALUES (?,?,?,?)",owner,p.id(),learned.id(),learned.level());continue;}
            moves.set(slot,learned.id());pp.set(slot,-1);ups.set(slot,0);
        }
        db.execute("UPDATE pokemon SET moves=?,moves_pp=?,pp_up_times=? WHERE trainer_id=? AND id=?",encode(moves),encode(pp),encode(ups),owner,p.id());
    }
    static List<Integer> stats(PokemonData.Pokemon p,GameService.SpeciesTemplate template,int level,List<Integer> evs){
        var stats=new ArrayList<Integer>();
        int[] nature={1,2,5,3,4};
        for(int i=0;i<6;i++) {
            int stat=(2*template.stats().get(i)+p.ivs().get(i)+evs.get(i)/4)*level/100+(i==0?level+10:5);
            int up=nature[p.nature()/5],down=nature[p.nature()%5];
            if(i!=0&&up!=down) stat=(int)(stat*(i==up?1.1:i==down?.9:1));
            if(i==0&&template.id()==292) stat=1;
            stats.add(stat);
        }
        return stats;
    }
}
