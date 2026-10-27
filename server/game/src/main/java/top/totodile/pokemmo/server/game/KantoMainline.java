package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import top.totodile.pokemmo.db.Database;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Finite reference StoryRuntime/StoryActionTransaction adapters. No arbitrary script execution. */
final class KantoMainline {
    static final List<JsonObject> PROGRAMS=List.of(read("/story/kanto/catch-tutorial.json"),read("/story/kanto/early-route.json"),read("/story/kanto/mainline.json"),read("/story/kanto/middle.json"),read("/story/kanto/mechanisms.json"));
    static final JsonObject PICKUPS=read("/world/kanto-items.json");
    static final JsonObject TRAINERS=read("/world/kanto-trainers.json");
    static final Set<String> DURABLE=Set.of("GRANT_ITEM","REMOVE_ITEM","GRANT_POKEMON","SET_STORY_BIT","SET_EXTRA_STORY_BIT","SET_BADGE","SET_CHAMPION","SET_EVENT_FLAG");
    static JsonObject read(String path) {
        try(var in=KantoMainline.class.getResourceAsStream(path)) {
            if(in==null)throw new FileNotFoundException(path);
            return JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    static void initialize(Database db) {
        KantoMechanisms.initialize(db);
        MoveLearning.initialize(db);
        PokemonEvolution.initialize(db);
        db.execute("CREATE TABLE IF NOT EXISTS character_kanto_mainline(character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE,story INTEGER NOT NULL DEFAULT 0,extra INTEGER NOT NULL DEFAULT 0,champion INTEGER NOT NULL DEFAULT 0,elite INTEGER NOT NULL DEFAULT 0,safari_steps INTEGER NOT NULL DEFAULT 0,safari_balls INTEGER NOT NULL DEFAULT 0)");
        db.execute("CREATE TABLE IF NOT EXISTS character_story_action(character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,action_key TEXT NOT NULL,PRIMARY KEY(character_id,action_key))");
        db.execute("CREATE TABLE IF NOT EXISTS character_story_event(character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,flag TEXT NOT NULL,PRIMARY KEY(character_id,flag))");
        db.execute("CREATE TABLE IF NOT EXISTS character_story_resume(character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE,program TEXT NOT NULL,node TEXT NOT NULL,bank INTEGER NOT NULL,map INTEGER NOT NULL)");
    }
    record Progress(int story,int extra,int badges,boolean champion,int elite) {
        boolean bit(String field,int bit){return ((field.equals("story")?story:field.equals("extra")?extra:badges)&(1<<bit))!=0;}
    }
    static Progress progress(Database db,String owner) {
        var r=db.query("SELECT story,extra,champion,elite FROM character_kanto_mainline WHERE character_id=?",owner);
        var b=db.query("SELECT badges FROM character_trainer WHERE character_id=?",owner);
        return new Progress(r.isEmpty()?0:Integer.parseInt(r.get(0)[0]),r.isEmpty()?0:Integer.parseInt(r.get(0)[1]),b.isEmpty()?0:Integer.parseInt(b.get(0)[0]),!r.isEmpty()&&!r.get(0)[2].equals("0"),r.isEmpty()?0:Integer.parseInt(r.get(0)[3]));
    }
    static void ensure(Database db,String owner){db.execute("INSERT OR IGNORE INTO character_kanto_mainline(character_id) VALUES (?)",owner);}
    static boolean hasItem(Database db,String owner,int item) {
        return !db.query("SELECT item_id FROM owned_item WHERE owner_id=? AND inventory_id=1 AND item_index_id=? AND item_amount>0 LIMIT 1",owner,Integer.toString(item)).isEmpty();
    }
    static boolean event(Database db,String owner,String flag){return !db.query("SELECT flag FROM character_story_event WHERE character_id=? AND flag=?",owner,flag).isEmpty();}
    static void eventSet(Database db,String owner,String flag){db.execute("INSERT OR IGNORE INTO character_story_event VALUES (?,?)",owner,flag);}
    static boolean condition(Database db,String owner,JsonObject c) {
        String type=c.get("type").getAsString();int v=c.has("value")&&c.get("value").getAsJsonPrimitive().isNumber()?c.get("value").getAsInt():0;
        var p=progress(db,owner);
        return switch(type) {
            case "CAPTURE_TUTORIAL_COMPLETE" -> p.bit("story",5);
            case "CAPTURE_TUTORIAL_INCOMPLETE" -> !p.bit("story",5);
            case "HAS_BATTLE_POKEMON" -> !db.query("SELECT id FROM pokemon WHERE trainer_id=? AND container_id=1 AND is_egg=0 AND current_hp>0 LIMIT 1",owner).isEmpty();
            case "HAS_CAPTURE_BALL" -> hasItem(db,owner,5004);
            case "NO_CAPTURE_BALL" -> !hasItem(db,owner,5004);
            case "HAS_ITEM" -> hasItem(db,owner,v);
            case "NO_ITEM" -> !hasItem(db,owner,v);
            case "STORY_BIT_SET" -> p.bit("story",v);
            case "STORY_BIT_CLEAR" -> !p.bit("story",v);
            case "EXTRA_STORY_BIT_SET" -> p.bit("extra",v);
            case "EXTRA_STORY_BIT_CLEAR" -> !p.bit("extra",v);
            case "BADGE_SET" -> p.bit("badges",v);
            case "BADGE_CLEAR" -> !p.bit("badges",v);
            case "CHAMPION_SET" -> p.champion();
            case "CHAMPION_CLEAR" -> !p.champion();
            case "ELITE_STAGE_EQUALS" -> p.elite()==v;
            case "EVENT_FLAG_SET" -> event(db,owner,c.get("flag").getAsString());
            case "EVENT_FLAG_CLEAR" -> !event(db,owner,c.get("flag").getAsString());
            case "NOT_IN_BATTLE","NOT_IN_TRADE","INTERACTION_IDLE" -> true;
            default -> throw new IllegalStateException("Unsupported mainline condition "+type);
        };
    }
    static void flag(Database db,String owner,String type,int bit) {
        ensure(db,owner);
        if(bit<0||bit>15)throw new IllegalArgumentException("Invalid story bit");
        if(type.equals("SET_BADGE")) {
            db.execute("INSERT OR IGNORE INTO character_trainer(character_id) VALUES (?)",owner);
            db.execute("UPDATE character_trainer SET badges=badges | ? WHERE character_id=?",1<<bit,owner);
        } else {
            String column=type.equals("SET_STORY_BIT")?"story":"extra";
            db.execute("UPDATE character_kanto_mainline SET "+column+"="+column+" | ? WHERE character_id=?",1<<bit,owner);
        }
    }
    static void grant(Database db,String owner,int item,int amount,Map<Integer,GameService.ItemTemplate> items) {
        var template=items.get(item);
        if(template==null||amount<=0||template.maximumStack()<1)throw new IllegalStateException("Missing story item data "+item);
        int remaining=amount,max=Math.min(Short.MAX_VALUE,template.maximumStack());
        for(var r:db.query("SELECT item_id,item_amount FROM owned_item WHERE owner_id=? AND inventory_id=1 AND item_index_id=? AND region=-1 ORDER BY item_id",owner,Integer.toString(item))) {
            int add=Math.min(remaining,Math.max(0,max-Integer.parseInt(r[1])));
            if(add>0)db.execute("UPDATE owned_item SET item_amount=item_amount+? WHERE item_id=? AND owner_id=?",add,r[0],owner);
            remaining-=add;if(remaining==0)break;
        }
        while(remaining>0){int add=Math.min(max,remaining);db.execute("INSERT INTO owned_item(item_id,owner_id,item_index_id,item_amount,inventory_id,region) VALUES (?,?,?,?,1,-1)",UUID.randomUUID().toString(),owner,item,add);remaining-=add;}
    }
    static void remove(Database db,String owner,int item,int amount) {
        int remaining=amount;
        for(var r:db.query("SELECT item_id,item_amount FROM owned_item WHERE owner_id=? AND inventory_id=1 AND item_index_id=? ORDER BY item_id",owner,Integer.toString(item))) {
            int n=Integer.parseInt(r[1]),take=Math.min(n,remaining);
            if(take==n)db.execute("DELETE FROM owned_item WHERE item_id=? AND owner_id=?",r[0],owner);
            else db.execute("UPDATE owned_item SET item_amount=item_amount-? WHERE item_id=? AND owner_id=?",take,r[0],owner);
            remaining-=take;if(remaining==0)break;
        }
        if(remaining!=0)throw new IllegalStateException("Missing required story item "+item);
    }
    static void commitNode(Database db,String owner,String key,JsonArray actions,Map<Integer,GameService.ItemTemplate> items,Map<Integer,GameService.SpeciesTemplate> species,Random random) {
        if(actions.asList().stream().noneMatch(a->DURABLE.contains(a.getAsJsonObject().get("type").getAsString())))return;
        db.transaction(()->{
            if(!db.query("SELECT action_key FROM character_story_action WHERE character_id=? AND action_key=?",owner,key).isEmpty())return;
            ensure(db,owner);
            for(var value:actions) {
                var a=value.getAsJsonObject();String type=a.get("type").getAsString();
                switch(type) {
                    case "GRANT_ITEM" -> grant(db,owner,a.get("itemId").getAsInt(),a.get("amount").getAsInt(),items);
                    case "REMOVE_ITEM" -> remove(db,owner,a.get("itemId").getAsInt(),a.get("amount").getAsInt());
                    case "SET_STORY_BIT","SET_EXTRA_STORY_BIT","SET_BADGE" -> flag(db,owner,type,a.get("bit").getAsInt());
                    case "SET_EVENT_FLAG" -> eventSet(db,owner,a.get("flag").getAsString());
                    case "SET_CHAMPION" -> db.execute("UPDATE character_kanto_mainline SET champion=1 WHERE character_id=?",owner);
                    case "GRANT_POKEMON" -> {
                        var template=species.get(a.get("species").getAsInt());if(template==null)throw new IllegalStateException("Missing gift species");
                        var candidate=PokemonStore.createWildPokemon(template,a.get("level").getAsInt(),9,0,random);
                        var store=new PokemonStore(db,species);var gift=store.capture(owner,candidate,3);
                        if(a.has("moves"))db.execute("UPDATE pokemon SET moves=?,moves_pp='-1,-1,-1,-1' WHERE id=?",String.join(",",a.getAsJsonArray("moves").asList().stream().map(JsonElement::getAsString).toList()),gift.id());
                        db.execute("INSERT INTO character_pokedex_entry VALUES (?,?,3) ON CONFLICT(character_id,species) DO UPDATE SET flags=flags | 3",owner,gift.species());
                    }
                    default -> {}
                }
            }
            db.execute("INSERT INTO character_story_action VALUES (?,?)",owner,key);
        });
    }
    static JsonObject team(int id) {
        for(var v:TRAINERS.getAsJsonArray("trainerTeams"))if(v.getAsJsonObject().get("trainerTeamId").getAsInt()==id)return v.getAsJsonObject();
        throw new IllegalStateException("Missing native trainer team "+id);
    }
    static EncounterData inline(JsonObject action,List<PokemonData.Pokemon> party,Map<Integer,GameService.SpeciesTemplate> species,Random random) {
        var team=new ArrayList<PokemonData.Pokemon>();var gson=new Gson();
        int[] reorder={0,1,2,4,5,3};
        for(var value:action.getAsJsonArray("team")) {
            var c=value.getAsJsonObject();var template=species.get(c.get("species").getAsInt());int level=c.get("level").getAsInt();
            var base=PokemonStore.createWildPokemon(template,level,9,team.size(),random);var o=gson.toJsonTree(base).getAsJsonObject();
            var iv=new ArrayList<Integer>();var ev=new ArrayList<Integer>();var stats=new ArrayList<Integer>();
            for(int i=0;i<6;i++){iv.add(c.has("ivs")?c.getAsJsonArray("ivs").get(reorder[i]).getAsInt():10);ev.add(c.has("evs")?c.getAsJsonArray("evs").get(reorder[i]).getAsInt():0);stats.add((2*template.stats().get(i)+iv.get(i)+ev.get(i)/4)*level/100+(i==0?level+10:5));}
            o.addProperty("nature",0);o.addProperty("shiny",false);o.addProperty("secretShiny",false);o.addProperty("hp",stats.get(0));o.add("stats",gson.toJsonTree(stats));o.add("ivs",gson.toJsonTree(iv));o.add("evs",gson.toJsonTree(ev));o.add("moves",c.get("moves"));
            team.add(gson.fromJson(o,PokemonData.Pokemon.class));
        }
        return new EncounterData(party.stream().filter(p->!p.egg()&&p.hp()>0).findFirst().orElseThrow(),team,party,new EncounterData.Trainer(-1,0,action.get("trainerModel").getAsInt(),action.get("money").getAsInt()));
    }
    static JsonObject pickup(String map,int npc) {
        for(var v:PICKUPS.getAsJsonArray("pickups")){var b=v.getAsJsonObject();if(b.get("map").getAsString().equals(map)&&b.get("entityIdx").getAsInt()==npc)return b;}return null;
    }
    static String pickupFlag(JsonObject p){String flag=p.get("hideFlag").getAsString();return flag.isBlank()?"kanto/item/"+p.get("map").getAsString()+"/"+p.get("script").getAsString():flag;}
    static JsonObject binding(String map,int npc) {
        for(var v:TRAINERS.getAsJsonArray("npcBindings")){var b=v.getAsJsonObject();if(b.get("map").getAsString().equals(map)&&b.get("entityIdx").getAsInt()==npc)return b;}
        return null;
    }
    static String trainerFlag(String map,JsonObject binding){return "kanto/trainer/"+map+"/"+binding.get("script").getAsString();}
    static void resume(Database db,String owner,JsonObject program,String node,GameService.Position p) {
        db.execute("INSERT INTO character_story_resume VALUES (?,?,?,?,?) ON CONFLICT(character_id) DO UPDATE SET program=excluded.program,node=excluded.node,bank=excluded.bank,map=excluded.map",owner,program.get("id").getAsString(),node,p.bank(),p.map());
    }
    static JsonObject program(String id){return PROGRAMS.stream().filter(p->p.get("id").getAsString().equals(id)).findFirst().orElse(null);}
}
