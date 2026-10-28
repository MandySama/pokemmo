package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import top.totodile.pokemmo.db.Database;
import java.util.*;
import java.util.function.IntFunction;

/** Emerald chapter state and bindings; actions are executed by the existing GBA story interpreter. */
final class HoennStory {
    static final JsonObject PROGRAM=KantoMainline.read("/story/hoenn/opening.json");
    static final JsonObject MECHANISMS=KantoMainline.read("/story/hoenn/mechanisms.json");
    static final List<JsonObject> PROGRAMS=List.of(PROGRAM,HoennMainline.PROGRAM);
    static JsonObject program(String id){return PROGRAMS.stream().filter(p->p.get("id").getAsString().equals(id)).findFirst().orElseThrow();}
    private final Database db;
    private final String owner;
    private boolean strength;
    private String lastMap="";
    private final Set<Integer> crackedIce=new HashSet<>();
    private int iceSteps=1;
    private int lastIceTile=-1;
    HoennStory(Database db,String owner){this.db=db;this.owner=owner;}
    static void initialize(Database db){db.execute("CREATE TABLE IF NOT EXISTS character_hoenn_story(character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE,stage INTEGER NOT NULL DEFAULT 0,starter INTEGER NOT NULL DEFAULT 3,clock_time INTEGER NOT NULL DEFAULT 0,resume TEXT NOT NULL DEFAULT '')");HoennMainline.initialize(db);}
    static void create(Database db,String owner){db.execute("INSERT INTO character_hoenn_story(character_id) VALUES (?)",owner);}
    StoryData.State state(){var rows=db.query("SELECT stage,starter FROM character_hoenn_story WHERE character_id=?",owner);return rows.isEmpty()?new StoryData.State(false,0,3):new StoryData.State(true,Integer.parseInt(rows.get(0)[0]),Integer.parseInt(rows.get(0)[1]));}
    int gender(){return Integer.parseInt(db.query("SELECT gender FROM characters WHERE id=?",owner).get(0)[0]);}
    JsonObject starter(int choice){return PROGRAM.getAsJsonObject("resources").getAsJsonArray("starters").get(choice).getAsJsonObject();}
    int species(){return state().starter()<3?starter(state().starter()).get("species").getAsInt():0;}
    List<MapEntities.MapView> entities(){
        var views=HoennWorld.entities(state().stage(),gender(),this::condition);var switchState=KantoMechanisms.get(db,owner,"hoenn/mauville","0,-1").split(",");
        int variant=Integer.parseInt(switchState[0]),pressed=Integer.parseInt(switchState[1]);
        return views.stream().map(m->{
            var p=new GameService.Position(m.bank(),m.map(),0,0,0);
            if(HoennWorld.key(p).equals("SootopolisCity_Gym_1F")){
                var data=HoennMainline.PROGRAM.getAsJsonObject("ice");int width=HoennWorld.map(p).get("width").getAsInt();var tiles=new ArrayList<MapEntities.Tile>();
                for(int tile:crackedIce){int x=tile%width,y=tile/width;tiles.add(new MapEntities.Tile(x,y,(HoennWorld.block(p,x,y)&0xf000)|data.get("cracked").getAsInt()));}
                for(int[] stair:new int[][]{{8,15,8},{8,10,28},{8,4,67}})if(iceSteps>stair[2])for(int y=stair[1];y<=stair[1]+1;y++)tiles.add(new MapEntities.Tile(stair[0],y,(HoennWorld.block(p,stair[0],y)&0xf000)|data.get("stairs").getAsInt()));
                return new MapEntities.MapView(m.bank(),m.map(),m.npcs(),m.backgrounds(),tiles);
            }
            if(!HoennWorld.key(p).equals("MauvilleCity_Gym"))return m;
            var g=HoennMainline.PROGRAM.getAsJsonArray("mechanisms").get(0).getAsJsonObject();var tiles=new ArrayList<MapEntities.Tile>();
            for(var tv:g.getAsJsonArray("variants").get(variant).getAsJsonArray()){var t=tv.getAsJsonObject();tiles.add(new MapEntities.Tile(t.get("x").getAsInt(),t.get("y").getAsInt(),t.get("block").getAsInt()));}
            int i=0;for(var sv:g.getAsJsonArray("switches")){var s=sv.getAsJsonObject();int x=s.get("x").getAsInt(),y=s.get("y").getAsInt();tiles.add(new MapEntities.Tile(x,y,(HoennWorld.block(p,x,y)&0xf000)|s.get(i++==pressed?"pressed":"raised").getAsInt()));}
            return new MapEntities.MapView(m.bank(),m.map(),m.npcs(),m.backgrounds(),tiles);
        }).map(m->{
            if(!HoennMainline.PROGRAM.has("tileGroups"))return m;String key=HoennWorld.key(new GameService.Position(m.bank(),m.map(),0,0,0));var tiles=new ArrayList<>(m.tiles());
            for(var gv:HoennMainline.PROGRAM.getAsJsonArray("tileGroups")){var g=gv.getAsJsonObject();if(!g.get("map").getAsString().equals(key))continue;int stage=state().stage();if(stage<g.get("min").getAsInt()||stage>g.get("max").getAsInt())continue;
                for(var tv:g.getAsJsonArray("tiles")){var t=tv.getAsJsonObject();int x=t.get("x").getAsInt(),y=t.get("y").getAsInt();int elevation=t.has("elevation")?t.get("elevation").getAsInt():HoennWorld.block(new GameService.Position(m.bank(),m.map(),0,0,0),x,y)&0xf000;tiles.add(new MapEntities.Tile(x,y,elevation|t.get("tile").getAsInt()|(t.get("blocked").getAsBoolean()?0xc00:0)));}
            }
            return tiles.isEmpty()?m:new MapEntities.MapView(m.bank(),m.map(),m.npcs(),m.backgrounds(),tiles);
        }).toList();
    }
    void arrive(GameService.Position p){String map=HoennWorld.key(p);if(!map.equals(lastMap)){strength=false;lastMap=map;if(map.equals("SootopolisCity_Gym_1F")){crackedIce.clear();iceSteps=1;lastIceTile=p.y()*HoennWorld.map(p).get("width").getAsInt()+p.x();}}}
    GameService.Position ice(StoryData.Request r,GameService.Position p){
        if(!r.event().equals("COORDINATE"))return null;
        String map=HoennWorld.key(p);
        // The first story visit uses native clean Sky Pillar layouts, not the damaged ROM layout.
        if(!(map.startsWith("SkyPillar_")&&state().stage()<=45))for(var value:MECHANISMS.getAsJsonArray("holeWarps")){
            var hole=value.getAsJsonObject();if(!hole.get("map").getAsString().equals(map))continue;
            int behavior=HoennWorld.behavior(p);boolean fall=behavior==0x66;
            if(behavior==0xd2&&hole.get("crackedFloor").getAsBoolean()){
                var travel=db.query("SELECT bicycle FROM character_travel_state WHERE character_id=?",owner);
                fall=travel.isEmpty()||Integer.parseInt(travel.get(0)[0])==0;
            }
            if(fall)return HoennWorld.position(hole.get("target").getAsString(),p.x(),p.y(),p.direction());
        }
        if(!map.equals("SootopolisCity_Gym_1F"))return null;
        int tile=p.y()*HoennWorld.map(p).get("width").getAsInt()+p.x();if(tile==lastIceTile)return null;lastIceTile=tile;
        if(HoennWorld.behavior(p)!=0x26)return null;
        if(!crackedIce.add(tile)){crackedIce.clear();iceSteps=1;return HoennWorld.position("SootopolisCity_Gym_B1F",p.x(),p.y(),p.direction());}
        iceSteps++;if(iceSteps==8||iceSteps==28||iceSteps==67)iceSteps++;return null;
    }
    boolean knows(int move){return new PokemonStore(db,Map.of()).load(owner).pokemons().stream().filter(p->p.container()==1&&!p.egg()).anyMatch(p->p.moves().contains(move));}
    void removed(int actor,GameService.Position p){if(HoennWorld.key(p).equals("RusturfTunnel")&&(actor==1||actor==7))KantoMainline.eventSet(db,owner,"hoenn/rusturf_rock");}
    String fieldNode(StoryData.Request r,GameService.Position p,List<MapEntities.MapView> maps){
        if(r.event().equals("FIELD")){
            if(diveTarget(p)==null)return null;boolean surface=HoennWorld.key(p).startsWith("Underwater");String kind=surface?"surface":"dive";
            return "field_"+kind+((HoennMainline.badges(db,owner)&64)!=0&&KantoMainline.hasItem(db,owner,346)&&knows(291)?"":"_no");
        }
        if(!r.event().equals("NPC")&&!r.event().equals("PUSH"))return null;
        var n=maps.stream().filter(m->m.bank()==p.bank()&&m.map()==p.map()).flatMap(m->m.npcs().stream()).filter(a->a.id()==r.npc()).findFirst().orElse(null);
        if(n==null||Math.abs(n.x()-p.x())+Math.abs(n.y()-p.y())!=1)return null;
        int badge=HoennMainline.badges(db,owner);String kind;boolean allowed;
        if(n.graphics()==82){kind="cut";allowed=(badge&1)!=0&&KantoMainline.hasItem(db,owner,339)&&knows(15);}
        else if(n.graphics()==86){kind="smash";allowed=(badge&4)!=0&&KantoMainline.hasItem(db,owner,344)&&knows(249);}
        else if(n.graphics()==87&&!strength){kind="strength";allowed=(badge&8)!=0&&KantoMainline.hasItem(db,owner,342)&&knows(70);}
        else return null;
        return "field_"+kind+(allowed?"":"_no");
    }
    JsonArray push(StoryData.Request r,GameService.Position p,List<MapEntities.MapView> maps){
        if(!strength||!Set.of("PUSH","NPC").contains(r.event()))return null;
        var npcs=maps.stream().filter(m->m.bank()==p.bank()&&m.map()==p.map()).flatMap(m->m.npcs().stream()).toList();var n=npcs.stream().filter(a->a.id()==r.npc()&&a.graphics()==87).findFirst().orElse(null);if(n==null)return null;
        int dx=p.direction()==2?-1:p.direction()==3?1:0,dy=p.direction()==0?1:p.direction()==1?-1:0;
        if(n.x()!=p.x()+dx||n.y()!=p.y()+dy)return null;int x=n.x()+dx,y=n.y()+dy,block;
        try{block=HoennWorld.block(p,x,y);}catch(IllegalArgumentException outside){return null;}
        if((block&0xc00)!=0||block>>>12==15||npcs.stream().anyMatch(a->a.id()!=n.id()&&a.x()==x&&a.y()==y))return null;
        int oldElevation=HoennWorld.block(p,n.x(),n.y())>>>12,newElevation=block>>>12,behavior=HoennWorld.behavior(p,x,y);
        if(oldElevation!=0&&newElevation!=0&&oldElevation!=newElevation||behavior>=0x10&&behavior<=0x15||behavior>=0x50&&behavior<=0x53||behavior==0x60)return null;
        var a=new JsonObject();a.addProperty("type","MOVE_NPC");a.addProperty("actorIndex",n.id());a.addProperty("message","STRENGTH_PUSH");var t=new JsonObject();t.addProperty("x",x);t.addProperty("y",y);a.add("target",t);
        var player=new JsonObject();player.addProperty("type","MOVE_PLAYER");player.addProperty("x",n.x());player.addProperty("y",n.y());var close=new JsonObject();close.addProperty("type","CLOSE_SCENE");var actions=new JsonArray();actions.add(a);actions.add(player);actions.add(close);return actions;
    }
    GameService.Position diveTarget(GameService.Position p){
        var m=HoennWorld.map(p);if(m==null)return null;boolean underwater=HoennWorld.key(p).startsWith("Underwater");int b=HoennWorld.behavior(p);
        if(underwater?(b==0x19||b==0x2a):!(b==0x12||b==0x14))return null;
        for(var wv:HoennMainline.PROGRAM.getAsJsonArray("diveWarps")){var w=wv.getAsJsonObject();if(w.get("map").getAsString().equals(HoennWorld.key(p)))return HoennWorld.position(w.get("target").getAsString(),w.get("x").getAsInt(),w.get("y").getAsInt(),p.direction());}
        for(var cv:m.getAsJsonArray("connections")){var c=cv.getAsJsonObject();if(c.get("direction").getAsString().equals(underwater?"EMERGE":"DIVE"))return new GameService.Position(c.get("targetBank").getAsInt()-50,c.get("targetMap").getAsInt(),p.x(),p.y(),p.direction());}
        return null;
    }
    void resume(JsonObject program,String node,GameService.Position p){var data=new JsonObject();data.addProperty("program",program.get("id").getAsString());data.addProperty("node",node);data.add("position",new Gson().toJsonTree(p));db.execute("UPDATE character_hoenn_story SET resume=? WHERE character_id=?",data.toString(),owner);}
    JsonObject resume(){var r=db.query("SELECT resume FROM character_hoenn_story WHERE character_id=?",owner);return r.isEmpty()||r.get(0)[0].isEmpty()?null:JsonParser.parseString(r.get(0)[0]).getAsJsonObject();}
    void clearResume(){db.execute("UPDATE character_hoenn_story SET resume='' WHERE character_id=?",owner);}
    boolean condition(JsonObject c){int v=c.has("value")?c.get("value").getAsInt():0;return switch(c.get("type").getAsString()){case "STORY_STAGE_EQUALS"->state().stage()==v;case "STORY_STAGE_AT_LEAST"->state().stage()>=v;case "STORY_STAGE_BELOW"->state().stage()<v;case "SEX_EQUALS"->gender()==v;case "HOENN_FLAG_SET"->KantoMainline.event(db,owner,"hoenn/"+c.get("flag").getAsString());case "HOENN_FLAG_CLEAR"->!KantoMainline.event(db,owner,"hoenn/"+c.get("flag").getAsString());case "HOENN_BADGES"->(HoennMainline.badges(db,owner)&v)==v;default->false;};}
    String trigger(JsonObject program,StoryData.Request request,GameService.Position p,List<MapEntities.MapView> maps){
        for(var value:program.getAsJsonArray("triggers")) {
            var t=value.getAsJsonObject();if(!t.get("type").getAsString().equals(request.event())||!t.get("map").getAsString().equals(HoennWorld.key(p)))continue;
            if(t.has("x")&&(t.get("x").getAsInt()!=p.x()||t.get("y").getAsInt()!=p.y()))continue;
            boolean matches=true;for(var c:t.getAsJsonArray("conditions"))if(!condition(c.getAsJsonObject()))matches=false;if(!matches)continue;
            if(request.event().equals("NPC")){
                if(t.has("entityIdx")&&t.get("entityIdx").getAsInt()!=request.npc())continue;
                if(t.has("script")&&!t.get("script").getAsString().equals(HoennWorld.script(p,request.npc())))continue;
                boolean near=maps.stream().filter(m->m.bank()==p.bank()&&m.map()==p.map()).flatMap(m->m.npcs().stream()).anyMatch(n->n.id()==request.npc()&&Math.abs(n.x()-p.x())+Math.abs(n.y()-p.y())<=2+Math.max(n.rangeX(),n.rangeY()));if(!near)continue;
            }
            if(request.event().equals("BACKGROUND")){
                if(t.has("script")&&!t.get("script").getAsString().equals(HoennWorld.background(p,request.npc())))continue;
                boolean near=maps.stream().filter(m->m.bank()==p.bank()&&m.map()==p.map()).flatMap(m->m.backgrounds().stream()).anyMatch(b->b.id()==request.npc()&&b.facing(p,b.elevation()));if(!near)continue;
            }
            return t.get("startNode").getAsString();
        }
        return null;
    }
    void operation(JsonObject a,Map<Integer,GameService.ItemTemplate> items,IntFunction<PokemonData.Pokemon> starterFactory){
        db.transaction(()->{
            var state=state();switch(a.get("operation").getAsString()){
                case "STAGE"->db.execute("UPDATE character_hoenn_story SET stage=MAX(stage,?) WHERE character_id=?",a.get("stage").getAsInt(),owner);
                case "CLOCK"->db.execute("UPDATE character_hoenn_story SET clock_time=?,stage=MAX(stage,4) WHERE character_id=?",System.currentTimeMillis(),owner);
                case "CLAIM_STARTER"->{
                    if(state.starter()!=3)break;
                    if(state.stage()!=9)throw new IllegalStateException("Choose a starter at Birch's bag first");
                    int choice=a.get("choice").getAsInt();var pokemon=starterFactory.apply(choice);new PokemonStore(db,Map.of()).insert(owner,pokemon);
                    db.execute("UPDATE pokemon SET friend_value=70,normal_ribbon='2',moves_pp='-1,-1,-1,-1' WHERE id=?",pokemon.id());
                    db.execute("INSERT INTO character_pokedex_entry(character_id,species,flags) VALUES (?,?,3) ON CONFLICT(character_id,species) DO UPDATE SET flags=flags|3",owner,pokemon.species());
                    db.execute("UPDATE character_hoenn_story SET starter=?,stage=10 WHERE character_id=?",choice,owner);
                }
                case "POKEDEX"->{db.execute("INSERT INTO character_pokedex_state(character_id,unlocked) VALUES (?,1) ON CONFLICT(character_id) DO UPDATE SET unlocked=1",owner);db.execute("UPDATE character_hoenn_story SET stage=MAX(stage,14) WHERE character_id=?",owner);}
                case "BALLS"->{if(state.stage()<15){KantoMainline.grant(db,owner,5004,5,items);db.execute("UPDATE character_hoenn_story SET stage=15 WHERE character_id=?",owner);}}
                case "GIVE_ITEM","TAKE_ITEM"->{
                    int item=a.get("itemId").getAsInt();String key="hoenn/"+a.get("operation").getAsString()+"/"+(a.has("key")?a.get("key").getAsString():Integer.toString(item));
                    if(KantoMainline.event(db,owner,key))break;
                    if(a.get("operation").getAsString().equals("GIVE_ITEM"))KantoMainline.grant(db,owner,item,1,items);else KantoMainline.remove(db,owner,item,1);
                    KantoMainline.eventSet(db,owner,key);
                }
                case "FLAG"->KantoMainline.eventSet(db,owner,"hoenn/"+a.get("flag").getAsString());
                case "BADGE"->{HoennMainline.ensure(db,owner);db.execute("UPDATE character_hoenn_progress SET badges=badges|? WHERE character_id=?",1<<a.get("bit").getAsInt(),owner);}
                case "CHAMPION"->{HoennMainline.ensure(db,owner);db.execute("UPDATE character_hoenn_progress SET champion=1 WHERE character_id=?",owner);}
                case "STRENGTH"->strength=true;
                case "MAUVILLE_SWITCH"->{int index=a.get("index").getAsInt();var old=KantoMechanisms.get(db,owner,"hoenn/mauville","0,-1").split(",");if(Integer.parseInt(old[1])!=index)db.execute("INSERT INTO character_map_mechanism VALUES (?,?,?) ON CONFLICT(character_id,name) DO UPDATE SET value=excluded.value",owner,"hoenn/mauville",(1-Integer.parseInt(old[0]))+","+index);}
                default->throw new IllegalArgumentException("Unsupported Hoenn operation");
            }
        });
    }
    void finishBattle(boolean won,JsonObject program,JsonObject action,GameService.Position position){
        String next=action.get(won?"finishNode":"defeatNode").getAsString();resume(program,next,position);
    }
}
