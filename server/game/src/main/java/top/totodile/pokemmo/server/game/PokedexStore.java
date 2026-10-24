package top.totodile.pokemmo.server.game;

import java.util.*;
import top.totodile.pokemmo.db.Database;

/** SQLite adaptation of WorldService's four persisted dex bitsets. */
final class PokedexStore {
    private final Database db;
    PokedexStore(Database db){this.db=db;}
    static void initialize(Database db){
        db.execute("CREATE TABLE IF NOT EXISTS character_pokedex_entry (character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,species INTEGER NOT NULL CHECK(species BETWEEN 1 AND 649),flags INTEGER NOT NULL CHECK(flags BETWEEN 1 AND 15),PRIMARY KEY(character_id,species))");
        db.execute("CREATE TABLE IF NOT EXISTS character_pokedex_initialized (character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE)");
        // The legacy switch is read once; every subsequent reader uses the entries.
        for(var row:db.query("SELECT s.character_id,s.unlocked FROM character_pokedex_state s LEFT JOIN character_pokedex_initialized i ON i.character_id=s.character_id WHERE i.character_id IS NULL"))
            new PokedexStore(db).save(row[0],new GameService.PokedexState(Integer.parseInt(row[1])!=0));
    }
    GameService.PokedexState load(String id){
        var entries=new HashMap<Integer,Integer>();
        for(var row:db.query("SELECT species,flags FROM character_pokedex_entry WHERE character_id=?",id))entries.put(Integer.parseInt(row[0]),Integer.parseInt(row[1]));
        return new GameService.PokedexState(entries);
    }
    void save(String id,GameService.PokedexState state){
        db.transaction(()->{
            db.execute("DELETE FROM character_pokedex_entry WHERE character_id=?",id);
            for(var entry:state.entries().entrySet())db.execute("INSERT INTO character_pokedex_entry VALUES (?,?,?)",id,entry.getKey(),entry.getValue());
            db.execute("INSERT OR IGNORE INTO character_pokedex_initialized VALUES (?)",id);
        });
    }
}
