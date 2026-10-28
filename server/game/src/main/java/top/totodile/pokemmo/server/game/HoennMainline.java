package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import top.totodile.pokemmo.db.Database;
import java.util.*;

/** Native Emerald parties and owner-scoped mainline progress, using the existing battle engine. */
final class HoennMainline {
    static final JsonObject PROGRAM=KantoMainline.read("/story/hoenn/mainline.json");
    private static final Map<String,List<JsonObject>> NPC_TRIGGERS=new HashMap<>();
    static {for(var v:PROGRAM.getAsJsonArray("triggers")){var t=v.getAsJsonObject();if(t.has("entityIdx")&&!t.get("startNode").getAsString().startsWith("trainer_")&&!t.get("startNode").getAsString().startsWith("fixed_"))NPC_TRIGGERS.computeIfAbsent(t.get("map").getAsString()+":"+t.get("entityIdx").getAsInt(),key->new ArrayList<>()).add(t);}}
    static boolean triggeredNpc(String map,int id,java.util.function.Predicate<JsonObject> condition){return NPC_TRIGGERS.getOrDefault(map+":"+id,List.of()).stream().anyMatch(t->t.getAsJsonArray("conditions").asList().stream().map(JsonElement::getAsJsonObject).allMatch(condition));}
    private HoennMainline(){}
    static void initialize(Database db){
        db.execute("CREATE TABLE IF NOT EXISTS character_hoenn_progress(character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE,badges INTEGER NOT NULL DEFAULT 0,champion INTEGER NOT NULL DEFAULT 0,team TEXT NOT NULL DEFAULT '',completed INTEGER NOT NULL DEFAULT 0)");
        db.execute("INSERT OR IGNORE INTO character_hoenn_progress(character_id) SELECT character_id FROM character_hoenn_story");
    }
    static int badges(Database db,String owner){var r=db.query("SELECT badges FROM character_hoenn_progress WHERE character_id=?",owner);return r.isEmpty()?0:Integer.parseInt(r.get(0)[0]);}
    static boolean champion(Database db,String owner){var r=db.query("SELECT champion FROM character_hoenn_progress WHERE character_id=?",owner);return !r.isEmpty()&&r.get(0)[0].equals("1");}
    static void ensure(Database db,String owner){db.execute("INSERT OR IGNORE INTO character_hoenn_progress(character_id) VALUES (?)",owner);}
    static List<String> hall(Database db,String owner,Map<Integer,GameService.SpeciesTemplate> templates){
        if(!champion(db,owner))throw new IllegalStateException("Hoenn Hall of Fame requires champion");
        var team=db.query("SELECT team FROM character_hoenn_progress WHERE character_id=?",owner).get(0)[0];
        if(team.isEmpty()){
            var party=new PokemonStore(db,templates).load(owner).pokemons().stream().filter(p->p.container()==1).sorted(Comparator.comparingInt(PokemonData.Pokemon::position)).toList();
            if(party.isEmpty())throw new IllegalStateException("Empty Hall of Fame party");
            team=new Gson().toJson(party);db.execute("UPDATE character_hoenn_progress SET team=? WHERE character_id=?",team,owner);
        }
        return JsonParser.parseString(team).getAsJsonArray().asList().stream().map(JsonElement::toString).toList();
    }
    static EncounterData battle(JsonObject action,List<PokemonData.Pokemon> party,Map<Integer,GameService.SpeciesTemplate> templates,Random random,int starter){
        String key=action.has("nativeTrainers")?action.getAsJsonArray("nativeTrainers").get(starter).getAsString():action.get("nativeTrainer").getAsString();
        var nativeTrainer=PROGRAM.getAsJsonObject("trainers").getAsJsonObject(key);var team=new ArrayList<PokemonData.Pokemon>();var gson=new Gson();
        for(var value:nativeTrainer.getAsJsonArray("team")){
            var mon=value.getAsJsonObject();int species=mon.get("species").getAsInt(),level=mon.get("level").getAsInt(),iv=mon.get("iv").getAsInt();var template=templates.get(species);
            if(template==null)throw new IllegalStateException("Missing native Hoenn species "+species);
            var base=PokemonStore.createWildPokemon(template,level,9,team.size(),random);var o=gson.toJsonTree(base).getAsJsonObject();var stats=new ArrayList<Integer>();
            for(int i=0;i<6;i++)stats.add((2*template.stats().get(i)+iv)*level/100+(i==0?level+10:5));
            int item=switch(mon.get("item").getAsInt()){case 0->0;case 110->5092;case 134->5150;case 139->5155;case 142->5158;case 180->5214;case 188->5222;case 194->5228;case 207->5241;case 216->5250;default->throw new IllegalArgumentException("Unverified Hoenn held item");};
            o.addProperty("nature",0);o.addProperty("shiny",false);o.addProperty("secretShiny",false);o.addProperty("hp",stats.get(0));o.addProperty("item",item);
            o.add("stats",gson.toJsonTree(stats));o.add("ivs",gson.toJsonTree(Collections.nCopies(6,iv)));o.add("evs",gson.toJsonTree(Collections.nCopies(6,0)));
            if(!mon.getAsJsonArray("moves").isEmpty())o.add("moves",mon.get("moves"));
            team.add(gson.fromJson(o,PokemonData.Pokemon.class));
        }
        return new EncounterData(party.stream().filter(p->!p.egg()&&p.hp()>0).findFirst().orElseThrow(),team,party,new EncounterData.Trainer(nativeTrainer.get("id").getAsInt(),1,nativeTrainer.get("model").getAsInt(),nativeTrainer.get("money").getAsInt()));
    }
}
