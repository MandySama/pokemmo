package top.totodile.pokemmo.server.game;

import java.util.*;
import top.totodile.pokemmo.db.Database;
import static top.totodile.pokemmo.server.game.PokemonData.*;

/** Local DB adapter for reference PokemonContainerService: slots, batch swaps and level sort. */
final class PokemonStore {
    private final Database db;
    PokemonStore(Database db){this.db=db;}
    static void initialize(Database db){
        db.execute("CREATE TABLE IF NOT EXISTS pokemon (id TEXT PRIMARY KEY,trainer_id TEXT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,container_id INTEGER NOT NULL,container_position INTEGER NOT NULL,dex_id INTEGER NOT NULL,form_type INTEGER NOT NULL DEFAULT 0,gender INTEGER NOT NULL DEFAULT -1,is_shiny INTEGER NOT NULL DEFAULT 0,is_egg INTEGER NOT NULL DEFAULT 0,name TEXT NOT NULL DEFAULT '',level_value INTEGER NOT NULL,current_hp INTEGER NOT NULL,status INTEGER NOT NULL DEFAULT 0,item INTEGER NOT NULL DEFAULT 0,nature INTEGER NOT NULL DEFAULT 0,ability INTEGER NOT NULL DEFAULT 0,stats TEXT NOT NULL,iv_values TEXT NOT NULL,ev_values TEXT NOT NULL,moves TEXT NOT NULL)");
        db.execute("CREATE INDEX IF NOT EXISTS pokemon_owner_container ON pokemon(trainer_id,container_id,container_position)");
    }
    Containers load(String id){
        var pokemons=db.query("SELECT id,container_id,container_position,dex_id,form_type,gender,is_shiny,is_egg,name,level_value,current_hp,status,item,nature,ability,stats,iv_values,ev_values,moves FROM pokemon WHERE trainer_id=? AND container_id IN (0,1) ORDER BY container_id,container_position",id).stream().map(r->new Pokemon(r[0],n(r[1]),n(r[2]),n(r[3]),n(r[4]),n(r[5]),n(r[6])!=0,n(r[7])!=0,r[8],n(r[9]),n(r[10]),n(r[11]),n(r[12]),n(r[13]),n(r[14]),values(r[15]),values(r[16]),values(r[17]),values(r[18]))).toList();
        return new Containers(pokemons,BASE_BOXES);
    }
    private static int n(String v){return Integer.parseInt(v);}
    private static List<Integer> values(String value){return Arrays.stream(value.split(",",-1)).map(Integer::valueOf).toList();}
    private record Slot(int container,int position) {}
    private static Slot source(Move m){return new Slot(m.oldContainer(),m.oldPosition());}
    private static Slot target(Move m){return new Slot(m.newContainer(),m.newPosition());}
    private static boolean valid(Slot s,int boxes){return s.position>=0&&(s.container==PARTY?s.position<PARTY_SIZE:s.container==PC&&s.position<boxes*BOX_SIZE);}
    private static void reject(){throw new Rejected(Failure.INVALID_MOVE);}
    Containers move(String id,List<Move> changes){
        if(changes==null||changes.isEmpty()||changes.size()>127)reject();
        db.transaction(()->{
            var current=load(id);var occupied=new HashMap<Slot,Pokemon>();var original=new HashMap<String,Slot>();
            for(var p:current.pokemons()){
                var s=new Slot(p.container(),p.position());if(occupied.put(s,p)!=null||!valid(s,current.boxes()))reject();original.put(p.id(),s);
            }
            var finalSlots=new HashMap<>(original);var sources=new HashSet<Slot>();var targets=new HashSet<Slot>();var moved=new HashSet<String>();
            for(var m:changes){
                if(m==null)reject();var from=source(m);var to=target(m);var p=occupied.get(from);
                if(!valid(from,current.boxes())||!valid(to,current.boxes())||from.equals(to)||p==null||!sources.add(from)||!targets.add(to)||!moved.add(p.id()))reject();
                finalSlots.put(p.id(),to);
            }
            // Occupied targets outside the same batch swap back to the corresponding source.
            for(var m:changes){var p=occupied.get(target(m));if(p!=null&&!moved.contains(p.id()))finalSlots.put(p.id(),source(m));}
            if(new HashSet<>(finalSlots.values()).size()!=finalSlots.size())reject();
            if(original.values().stream().anyMatch(s->s.container==PARTY)&&finalSlots.values().stream().noneMatch(s->s.container==PARTY))throw new Rejected(Failure.LAST_PARTY_POKEMON);
            for(var e:finalSlots.entrySet())if(!e.getValue().equals(original.get(e.getKey())))
                db.execute("UPDATE pokemon SET container_id=?,container_position=? WHERE id=? AND trainer_id=?",e.getValue().container,e.getValue().position,e.getKey(),id);
        });return load(id);
    }
    Containers sort(String id,int box){
        db.transaction(()->{
            var current=load(id);if(box<0||box>=current.boxes())reject();
            var values=current.pokemons().stream().filter(p->p.container()==PC&&p.position()/BOX_SIZE==box)
                .sorted(Comparator.comparingInt(Pokemon::level).thenComparingInt(Pokemon::position).thenComparing(Pokemon::id)).toList();
            for(int i=0;i<values.size();i++)db.execute("UPDATE pokemon SET container_position=? WHERE id=? AND trainer_id=?",box*BOX_SIZE+i,values.get(i).id(),id);
        });return load(id);
    }
    Pokemon add(String id,GameService.SpeciesTemplate species,int level,Random random){
        if(species==null||species.id()<1||species.id()>649)throw new IllegalArgumentException("Unknown Pokemon index id.");
        if(level<1||level>100)throw new IllegalArgumentException("Pokemon level must be an integer from 1 to 100.");
        var added=new Pokemon[1];db.transaction(()->{
            var current=load(id);int container=PARTY,position=0;
            while(position<PARTY_SIZE&&current.at(PARTY,position)!=null)position++;
            if(position==PARTY_SIZE){container=PC;position=0;while(position<current.boxes()*BOX_SIZE&&current.at(PC,position)!=null)position++;}
            if(container==PC&&position>=current.boxes()*BOX_SIZE)throw new IllegalStateException("The target has no free party or PC slot.");
            var p=createWildPokemon(species,level,container,position,random);
            db.execute("INSERT INTO pokemon(id,trainer_id,container_id,container_position,dex_id,form_type,gender,is_shiny,is_egg,name,level_value,current_hp,status,item,nature,ability,stats,iv_values,ev_values,moves) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",p.id(),id,p.container(),p.position(),p.species(),p.form(),p.gender(),p.shiny()?1:0,p.egg()?1:0,p.name(),p.level(),p.hp(),p.status(),p.item(),p.nature(),p.ability(),encoded(p.stats()),encoded(p.ivs()),encoded(p.evs()),encoded(p.moves()));added[0]=p;
        });return added[0];
    }
    private static String encoded(List<Integer> values){return values.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));}

    /** PokemonManager.createWildPokemon / PokemonDexData.getPokemonAbilityValue, using the loaded registry. */
    // PokemonStatType's internal order is HP, Atk, Def, Speed, SpA, SpD; snapshots use display order.
    private static final int[] NATURE_STATS={1,2,5,3,4};
    static PokemonData.Pokemon createWildPokemon(GameService.SpeciesTemplate species,int level,int container,int position,Random random){
        int[] rawIvs=new int[6];for(int i=0;i<6;i++)rawIvs[i]=random.nextInt(31);
        int personality=random.nextInt(Integer.MAX_VALUE),nature=Math.floorMod(personality,25);
        var ivs=List.of(rawIvs[0],rawIvs[1],rawIvs[2],rawIvs[4],rawIvs[5],rawIvs[3]);
        var stats=new ArrayList<Integer>();
        for(int i=0;i<6;i++){
            int value=((species.stats().get(i)*2+ivs.get(i)+(i==0?100:0))*level)/100+(i==0?10:5);
            int up=NATURE_STATS[nature/5],down=NATURE_STATS[nature%5];
            double multiplier=up==down?1:i==up?1.1:i==down?.9:1;
            stats.add((int)(value*multiplier));
        }
        var moves=new ArrayList<>(species.moves().stream().filter(m->m.level()<=level)
            .sorted(Comparator.comparingInt(GameService.LearnedMove::level).reversed()).limit(4).map(GameService.LearnedMove::id).toList());
        while(moves.size()<4)moves.add(0);
        int ratio=species.genderRatio();
        // ActivePokemon.Dg0 handles the male/female-only and genderless species sent by the server.
        int gender=ratio==255?-1:ratio==254?1:ratio==0?0:(personality&255)>=ratio?0:1;
        // The supplied reference server is explicitly in its wild-rarity test mode (denominator 1).
        boolean shiny=random.nextInt(1)==0;if(shiny)random.nextInt(2);
        return new PokemonData.Pokemon(UUID.randomUUID().toString(),container,position,species.id(),0,gender,shiny,false,"",level,stats.get(0),0,0,nature,species.ability(),stats,ivs,List.of(0,0,0,0,0,0),moves);
    }
}
