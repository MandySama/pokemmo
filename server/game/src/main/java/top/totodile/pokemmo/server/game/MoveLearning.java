package top.totodile.pokemmo.server.game;
import top.totodile.pokemmo.db.Database;
import java.util.*;
/** Native MoveLearningBubble: deferred full-slot choices, never auto-forget existing moves. */
final class MoveLearning {
    record Pending(String pokemon,int move,int level){}
    static void initialize(Database db){db.execute("CREATE TABLE IF NOT EXISTS character_move_learning(character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,pokemon_id TEXT NOT NULL REFERENCES pokemon(id) ON DELETE CASCADE,move INTEGER NOT NULL,level INTEGER NOT NULL,PRIMARY KEY(character_id,pokemon_id,move))");}
    static Pending first(Database db,String owner){var rows=db.query("SELECT pokemon_id,move,level FROM character_move_learning WHERE character_id=? ORDER BY rowid LIMIT 1",owner);return rows.isEmpty()?null:new Pending(rows.get(0)[0],Integer.parseInt(rows.get(0)[1]),Integer.parseInt(rows.get(0)[2]));}
    static void choose(Database db,String owner,Pending pending,int slot,Map<Integer,GameService.SpeciesTemplate> templates){
        if(slot<0||slot>4)throw new IllegalArgumentException("Invalid move learning choice");
        db.transaction(()->{
            if(!Objects.equals(first(db,owner),pending))throw new IllegalStateException("Stale move learning choice");
            var pokemon=new PokemonStore(db,templates).load(owner).pokemons().stream().filter(p->p.id().equals(pending.pokemon)).findFirst().orElseThrow();
            if(slot<4&&!pokemon.moves().contains(pending.move)){
                var moves=new ArrayList<>(pokemon.moves());var pp=new ArrayList<>(pokemon.pp());var ups=new ArrayList<>(pokemon.ppUps());
                moves.set(slot,pending.move);pp.set(slot,-1);ups.set(slot,0);
                db.execute("UPDATE pokemon SET moves=?,moves_pp=?,pp_up_times=? WHERE trainer_id=? AND id=?",encode(moves),encode(pp),encode(ups),owner,pending.pokemon);
            }
            db.execute("DELETE FROM character_move_learning WHERE character_id=? AND pokemon_id=? AND move=?",owner,pending.pokemon,pending.move);
        });
    }
    private static String encode(List<Integer> values){return String.join(",",values.stream().map(String::valueOf).toList());}
}
