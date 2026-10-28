package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import top.totodile.pokemmo.db.Database;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static top.totodile.pokemmo.server.game.StoryData.*;

/** Platinum chapter adapted to the existing dialogue/battle protocol; no ROM VM runs on the server. */
final class SinnohStory extends RegionalStory {
    static final JsonObject PROGRAM = KantoMainline.read("/story/sinnoh/first-badge.json");
    private static final AtomicLong SEQUENCES = new AtomicLong();
    private final Database db;
    private final String owner;
    private final Map<Integer,GameService.SpeciesTemplate> species;
    private final Random random = new Random();
    private Action waiting;
    private GameService.Position position;
    private boolean healed, changed;
    SinnohStory(Database db,String owner,Map<Integer,GameService.SpeciesTemplate> species) {
        this.db=db;this.owner=owner;this.species=species;
    }
    static void initialize(Database db) {
        db.execute("CREATE TABLE IF NOT EXISTS character_sinnoh_story(character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE,stage INTEGER NOT NULL DEFAULT 0,starter INTEGER NOT NULL DEFAULT 3,badges INTEGER NOT NULL DEFAULT 0,node TEXT NOT NULL DEFAULT '',action_index INTEGER NOT NULL DEFAULT 0,position TEXT NOT NULL DEFAULT '',flags TEXT NOT NULL DEFAULT '[]')");
    }
    static void create(Database db,String owner) {db.execute("INSERT INTO character_sinnoh_story(character_id) VALUES (?)",owner);}
    private String[] row() {var rows=db.query("SELECT stage,starter,badges,node,action_index,position,flags FROM character_sinnoh_story WHERE character_id=?",owner);return rows.isEmpty()?null:rows.get(0);}
    State state() {var r=row();return r==null?new State(false,0,3):new State(true,Integer.parseInt(r[0]),Integer.parseInt(r[1]));}
    static int badges(Database db,String owner) {var r=db.query("SELECT badges FROM character_sinnoh_story WHERE character_id=?",owner);return r.isEmpty()?0:Integer.parseInt(r.get(0)[0]);}
    private Set<String> flags() {var r=row();var result=new HashSet<String>();if(r!=null)for(var f:JsonParser.parseString(r[6]).getAsJsonArray())result.add(f.getAsString());return result;}
    private void flag(String name) {var f=flags();f.add(name);db.execute("UPDATE character_sinnoh_story SET flags=? WHERE character_id=?",new Gson().toJson(f),owner);changed=true;}
    boolean has(String name){return flags().contains(name);}
    Action waiting(){return waiting;}
    boolean takeHealed(){boolean v=healed;healed=false;return v;}
    boolean takeChanged(){boolean v=changed;changed=false;return v;}
    private static int map(GameService.Position p){return p.bank()*256+p.map();}
    private boolean matches(JsonObject rule) {
        var s=state();
        if(rule.has("min")&&s.stage()<rule.get("min").getAsInt()||rule.has("max")&&s.stage()>rule.get("max").getAsInt())return false;
        if(rule.has("starter")&&s.starter()!=rule.get("starter").getAsInt())return false;
        if(rule.has("set")&&!has(rule.get("set").getAsString())||rule.has("clear")&&has(rule.get("clear").getAsString()))return false;
        if(rule.has("all"))for(var f:rule.getAsJsonArray("all"))if(!has(f.getAsString()))return false;
        return true;
    }
    List<MapEntities.MapView> entities() {
        if(!state().enabled())return List.of();
        var result=new ArrayList<MapEntities.MapView>();
        for(var value:PROGRAM.getAsJsonArray("maps")) {
            var m=value.getAsJsonObject();int id=m.get("id").getAsInt();var npcs=new ArrayList<MapEntities.Npc>();
            for(var nv:m.getAsJsonArray("npcs")) {
                var n=nv.getAsJsonObject();if(!matches(n))continue;
                npcs.add(new MapEntities.Npc(n.get("id").getAsInt(),n.get("graphics").getAsInt(),n.get("x").getAsInt(),n.get("y").getAsInt(),0,n.get("direction").getAsInt(),0,0,0));
            }
            result.add(new MapEntities.MapView(id/256,id%256,npcs,List.of()));
        }
        return List.copyOf(result);
    }
    private JsonArray actions(String node){return PROGRAM.getAsJsonObject("nodes").getAsJsonArray(node);}
    private JsonObject current(){var r=row();return actions(r[3]).get(Integer.parseInt(r[4])).getAsJsonObject();}
    private void node(String name) {db.execute("UPDATE character_sinnoh_story SET node=?,action_index=0,position=? WHERE character_id=?",name,new Gson().toJson(position),owner);waiting=null;}
    private void next(){db.execute("UPDATE character_sinnoh_story SET action_index=action_index+1,position=? WHERE character_id=?",new Gson().toJson(position),owner);waiting=null;}
    private void close(){db.execute("UPDATE character_sinnoh_story SET node='',action_index=0,position='' WHERE character_id=?",owner);waiting=null;}
    Scene request(Request request,GameService.Position p,Map<Integer,GameService.ItemTemplate> items) {
        position=p;if(!state().enabled())return scene();
        if(request.event().equals("REPLY")) {
            if(waiting==null||request.sequence()!=waiting.sequence())return scene();
            if(waiting.message().equals("SINNOH_RESUME")){
                if(!p.equals(waiting.target()))throw new IllegalStateException("Resume warp has not completed");
                waiting=null;return advance(items);
            }
            var a=current();String type=a.get("type").getAsString();
            if(type.equals("START_BATTLE"))throw new IllegalStateException("Battle must be settled by the battle engine");
            if(type.equals("CHANGE_MAP")&&!p.equals(waiting.target()))throw new IllegalStateException("Story warp has not completed");
            if(type.equals("SELECT_OPTION")||type.equals("YES_NO")) {
                int answer=request.answer();var targets=a.getAsJsonArray("nodes");
                if(answer<0||answer>=targets.size())return scene();
                node(targets.get(answer).getAsString());
            }else next();
        } else {
            if(waiting!=null)return scene();
            var r=row();
            if(!r[3].isEmpty()) {
                var saved=new Gson().fromJson(r[5],GameService.Position.class);
                // Resume only in the scene's map. An interrupted movement is itself a saved action.
                if(saved!=null&&map(saved)!=map(p)&&!current().get("type").getAsString().equals("CHANGE_MAP")) {
                    position=saved;
                    waiting=new Action(SEQUENCES.incrementAndGet(),"CHANGE_MAP",0,-1,0,saved,null,0,"SINNOH_RESUME");
                    return scene();
                }
            } else {
                for(var value:PROGRAM.getAsJsonArray("triggers")) {
                    var t=value.getAsJsonObject();
                    if(t.get("map").getAsInt()!=map(p)||!t.get("event").getAsString().equals(request.event())||!matches(t))continue;
                    if(t.has("x")&&(p.x()!=t.get("x").getAsInt()||p.y()!=t.get("y").getAsInt()))continue;
                    if(t.has("yMin")&&(p.y()<t.get("yMin").getAsInt()||p.y()>t.get("yMax").getAsInt()))continue;
                    if(t.has("actor")) {
                        int actor=t.get("actor").getAsInt();if(actor!=request.npc())continue;
                        if(entities().stream().filter(m->m.bank()==p.bank()&&m.map()==p.map()).flatMap(m->m.npcs().stream()).noneMatch(n->n.id()==actor&&Math.abs(n.x()-p.x())+Math.abs(n.y()-p.y())<=2))continue;
                    }
                    node(t.get("node").getAsString());break;
                }
            }
        }
        return advance(items);
    }
    private Scene scene(){return new Scene(state(),waiting,entities(),healed,changed);}
    private Scene advance(Map<Integer,GameService.ItemTemplate> items) {
        for(int guard=0;guard<100;guard++) {
            var r=row();if(r[3].isEmpty())return scene();
            if(Integer.parseInt(r[4])>=actions(r[3]).size()){close();return scene();}
            var a=current();String type=a.get("type").getAsString();
            switch(type) {
                case "GOTO" -> node(a.get("node").getAsString());
                case "BRANCH_STARTER" -> node(a.getAsJsonArray("nodes").get(state().starter()).getAsString());
                case "OP" -> {
                    db.transaction(()->{operation(a,items);next();});
                }
                case "CLOSE_SCENE" -> {close();return scene();}
                default -> {
                    int text=a.has("text")?a.get("text").getAsInt():0;
                    GameService.Position target=null;
                    if(type.equals("CHANGE_MAP")){int m=a.get("map").getAsInt();target=new GameService.Position(m/256,m%256,a.get("x").getAsInt(),a.get("y").getAsInt(),a.has("direction")?a.get("direction").getAsInt():0);}
                    var options=new ArrayList<String>();if(a.has("options"))for(var v:a.getAsJsonArray("options"))options.add(v.getAsString());
                    int selected=state().starter()<3?new int[]{387,390,393}[state().starter()]:0;
                    waiting=new Action(SEQUENCES.incrementAndGet(),type,text,a.has("actor")?a.get("actor").getAsInt():-1,selected,target,null,0,"",0,options);
                    return scene();
                }
            }
        }
        throw new IllegalStateException("Sinnoh scene loop");
    }
    private void operation(JsonObject a,Map<Integer,GameService.ItemTemplate> items) {
        String op=a.get("op").getAsString();
        switch(op) {
            case "stage" -> db.execute("UPDATE character_sinnoh_story SET stage=MAX(stage,?) WHERE character_id=?",a.get("value").getAsInt(),owner);
            case "flag" -> flag(a.get("value").getAsString());
            case "heal" -> {for(var p:party())db.execute("UPDATE pokemon SET current_hp=?,status=0,moves_pp='-1,-1,-1,-1' WHERE id=? AND trainer_id=?",p.maxHp(),p.id(),owner);healed=true;}
            case "starter" -> {
                if(state().starter()!=3)break;
                if(state().stage()!=1)throw new IllegalStateException("Starter selection unavailable");
                int choice=a.get("value").getAsInt();gift(new int[]{387,390,393}[choice],5);
                db.execute("UPDATE character_sinnoh_story SET starter=?,stage=2 WHERE character_id=?",choice,owner);
            }
            case "dex" -> db.execute("INSERT INTO character_pokedex_state(character_id,unlocked) VALUES (?,1) ON CONFLICT(character_id) DO UPDATE SET unlocked=1",owner);
            case "item" -> {String key=a.get("key").getAsString();if(!has(key)){KantoMainline.grant(db,owner,a.get("item").getAsInt(),a.get("count").getAsInt(),items);flag(key);}}
            case "remove" -> KantoMainline.remove(db,owner,a.get("item").getAsInt(),a.get("count").getAsInt());
            case "badge" -> db.execute("UPDATE character_sinnoh_story SET badges=badges|1 WHERE character_id=?",owner);
            default -> throw new IllegalArgumentException("Unknown Sinnoh operation "+op);
        }
        changed=true;
    }
    private List<PokemonData.Pokemon> party(){return new PokemonStore(db,species).load(owner).pokemons().stream().filter(p->p.container()==1).toList();}
    private void gift(int number,int level) {
        int slot=0;var party=party();while(slot<6){int s=slot;if(party.stream().noneMatch(p->p.position()==s))break;slot++;}
        if(slot==6)throw new IllegalStateException("Party is full");
        var base=createPokemon(number,level,1,slot,15);
        new PokemonStore(db,species).insert(owner,base);
        db.execute("UPDATE pokemon SET normal_ribbon='2' WHERE id=?",base.id());
        db.execute("INSERT INTO character_pokedex_entry(character_id,species,flags) VALUES (?,?,3) ON CONFLICT(character_id,species) DO UPDATE SET flags=flags|3",owner,number);
    }
    private PokemonData.Pokemon createPokemon(int number,int level,int container,int slot,int iv) {
        var template=Objects.requireNonNull(species.get(number),"Missing Sinnoh species "+number);
        var base=PokemonStore.createWildPokemon(template,level,container,slot,random);
        var gson=new Gson();var data=gson.toJsonTree(base).getAsJsonObject();
        var stats=new ArrayList<Integer>();int[] natureStats={1,2,5,3,4};
        for(int i=0;i<6;i++) {
            int value=((template.stats().get(i)*2+iv+(i==0?100:0))*level)/100+(i==0?10:5);
            int up=natureStats[base.nature()/5],down=natureStats[base.nature()%5];
            stats.add((int)(value*(up==down?1:i==up?1.1:i==down?.9:1)));
        }
        data.add("ivs",gson.toJsonTree(Collections.nCopies(6,iv)));data.add("stats",gson.toJsonTree(stats));
        data.addProperty("hp",stats.get(0));data.addProperty("shiny",false);data.addProperty("secretShiny",false);
        return gson.fromJson(data,PokemonData.Pokemon.class);
    }
    EncounterData battle() {
        var a=current();if(!a.get("type").getAsString().equals("START_BATTLE"))throw new IllegalStateException("No Sinnoh battle pending");
        int id=a.has("trainers")?a.getAsJsonArray("trainers").get(state().starter()).getAsInt():a.get("trainer").getAsInt();
        var trainer=PROGRAM.getAsJsonObject("trainers").getAsJsonObject(Integer.toString(id));var team=new ArrayList<PokemonData.Pokemon>();var gson=new Gson();
        for(var v:trainer.getAsJsonArray("team")) {
            var mon=v.getAsJsonObject();int number=mon.get("species").getAsInt(),level=mon.get("level").getAsInt();
            var base=createPokemon(number,level,9,team.size(),mon.get("iv").getAsInt());
            var data=gson.toJsonTree(base).getAsJsonObject();
            if(mon.has("moves"))data.add("moves",mon.get("moves"));
            // Bw item 155 is Oran Berry; the existing server registry uses 5155.
            if(mon.has("item") && mon.get("item").getAsInt()==155)data.addProperty("item",5155);
            data.addProperty("shiny",false);data.addProperty("secretShiny",false);
            team.add(gson.fromJson(data,PokemonData.Pokemon.class));
        }
        var party=party();return new EncounterData(party.stream().filter(p->p.hp()>0&&!p.egg()).findFirst().orElseThrow(),team,party,new EncounterData.Trainer(id,3,trainer.get("model").getAsInt(),trainer.get("money").getAsInt()));
    }
    void finishBattle(boolean won) {
        var r=row();if(r==null||r[3].isEmpty())return;
        var a=current();if(!a.get("type").getAsString().equals("START_BATTLE"))return;
        db.transaction(()->node(a.get(won?"win":"lose").getAsString()));
    }
    StoryInspection.Report inspect(GameService.Position p) {
        var r=row();String status=r==null?"此角色没有神奥剧情存档":"神奥进度："+r[0]+" / 13；徽章："+r[2];
        var next=new ArrayList<String>();
        for(var v:PROGRAM.getAsJsonArray("triggers")){var t=v.getAsJsonObject();if(matches(t))next.add("地图 "+t.get("map").getAsInt()+" · "+t.get("event").getAsString()+" · "+t.get("node").getAsString());}
        var npcs=new ArrayList<StoryInspection.Npc>();
        for(var m:entities())if(m.bank()==p.bank()&&m.map()==p.map())for(var n:m.npcs())npcs.add(new StoryInspection.Npc(n.id(),n.graphics(),n.x(),n.y(),true,"sinnoh.first-badge","神奥剧情",List.of()));
        return new StoryInspection.Report(3,p.bank(),p.map(),"Pt "+map(p),status,next,npcs);
    }
    JsonObject checkpoint(){var o=new JsonObject();o.addProperty("sinnoh",true);return o;}
}

