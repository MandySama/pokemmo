package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import top.totodile.pokemmo.db.Database;
import java.util.*;

/** Finite map mechanisms; native geometry and destinations, owner-scoped reversible state. */
final class KantoMechanisms {
    static final JsonObject DATA=KantoMainline.read("/story/kanto/mechanisms.json");
    static JsonArray resource(String key){return DATA.getAsJsonObject("resources").getAsJsonArray(key);}
    record Plan(JsonArray actions,int actor) {}
    private record Pose(int x,int y,boolean hidden) {}
    private record Push(String map,int npc,int x,int y,JsonObject fall,JsonObject pressure) {}
    private final Database db;
    private final String owner;
    private final Random random;
    private final Map<String,Pose> temporary=new HashMap<>();
    private boolean strength;
    private Push pending;
    private String lift;
    private String leagueEntering;
    KantoMechanisms(Database db,String owner,Random random){this.db=db;this.owner=owner;this.random=random;}
    static void initialize(Database db){
        db.execute("CREATE TABLE IF NOT EXISTS character_map_mechanism(character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,name TEXT NOT NULL,value TEXT NOT NULL,PRIMARY KEY(character_id,name))");
        db.execute("CREATE TABLE IF NOT EXISTS character_map_boulder(character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,map_key TEXT NOT NULL,npc INTEGER NOT NULL,x INTEGER NOT NULL,y INTEGER NOT NULL,hidden INTEGER NOT NULL,PRIMARY KEY(character_id,map_key,npc))");
    }
    static String get(Database db,String owner,String key,String fallback){var rows=db.query("SELECT value FROM character_map_mechanism WHERE character_id=? AND name=?",owner,key);return rows.isEmpty()?fallback:rows.get(0)[0];}
    private void set(String key,String value){db.execute("INSERT INTO character_map_mechanism VALUES (?,?,?) ON CONFLICT(character_id,name) DO UPDATE SET value=excluded.value",owner,key,value);}
    private static String actorKey(String map,int npc){return map+":"+npc;}
    private static JsonObject object(String type){var o=new JsonObject();o.addProperty("type",type);return o;}
    private static JsonObject message(String text){var o=object("DIALOGUE");o.addProperty("message",text);return o;}
    private static Plan plan(int actor,JsonObject... actions){var a=new JsonArray();for(var o:actions)a.add(o);return new Plan(a,actor);}
    private static Plan text(int actor,String text){return plan(actor,message(text),object("CLOSE_SCENE"));}
    private static JsonObject find(JsonArray list,String field,String value){return list.asList().stream().map(JsonElement::getAsJsonObject).filter(o->o.get(field).getAsString().equals(value)).findFirst().orElse(null);}
    private static JsonObject boulder(String map,int npc){return resource("boulders").asList().stream().map(JsonElement::getAsJsonObject).filter(o->o.get("map").getAsString().equals(map)&&o.get("npc").getAsInt()==npc).findFirst().orElse(null);}
    static JsonObject program(){return KantoMainline.program("kanto.map_mechanisms");}
    private static Plan node(String name,int actor){return new Plan(program().getAsJsonObject("nodes").getAsJsonObject(name).getAsJsonArray("actions"),actor);}
    void arrive(GameService.Position before,GameService.Position p,String event){
        if(!event.equals("MAP_READY")||before!=null&&before.bank()==p.bank()&&before.map()==p.map())return;
        temporary.clear();strength=false;pending=null;leagueEntering=null;
        String map=MapEntities.mapKey(p.bank(),p.map());
        var elevator=find(resource("elevators"),"map",map);
        if(elevator!=null&&before!=null){
            String source=MapEntities.mapKey(before.bank(),before.map());
            for(var f:elevator.getAsJsonArray("floors"))if(f.getAsJsonObject().get("map").getAsString().equals(source))set("lift/"+map,source);
        }
        if(map.equals("VermilionCity_Gym")&&!KantoMainline.event(db,owner,"kanto/mechanism/vermilion_locks"))resetBins();
    }
    private void resetBins(){
        int first=random.nextInt(15),x=first%5,y=first/5;var neighbors=new ArrayList<Integer>();
        if(x>0)neighbors.add(first-1);if(x<4)neighbors.add(first+1);if(y>0)neighbors.add(first-5);if(y<2)neighbors.add(first+5);
        set("gym_pair",first+","+neighbors.get(random.nextInt(neighbors.size()))+",0");
    }
    Plan request(StoryData.Request request,GameService.Position p,List<MapEntities.MapView> views){
        String map=MapEntities.mapKey(p.bank(),p.map());
        if(request.event().equals("MAP_READY")||request.event().equals("COORDINATE")){
            var room=find(resource("leagueRooms"),"map",map);
            if(room!=null&&request.event().equals("MAP_READY")&&p.x()==room.get("x").getAsInt()&&p.y()==room.get("y").getAsInt()){
                leagueEntering=map;return node(room.get("node").getAsString(),-1);
            }
            if(room!=null){var resume=room.getAsJsonObject("resume");
                if(p.x()==resume.get("x").getAsInt()&&p.y()>=resume.get("minY").getAsInt()&&p.y()<=resume.get("maxY").getAsInt()){
                    leagueEntering=map;return node(resume.get("node").getAsString(),-1);
                }
            }
        }
        if(request.event().equals("COORDINATE")){
            var elevator=find(resource("elevators"),"map",map);
            if(elevator!=null&&p.x()==2&&p.y()==5)return exitLift(elevator);
        }
        if(request.event().equals("BACKGROUND")){
            var bg=MapEntities.background(p,request.npc());if(bg==null)return null;
            String script=MapEntities.backgroundScript(p.bank(),p.map(),bg.id());
            if(script.endsWith("_EventScript_FloorSelect")){
                var elevator=find(resource("elevators"),"map",map);if(elevator==null)return null;
                if(elevator.get("keyItem").getAsInt()>0&&!KantoMainline.hasItem(db,owner,elevator.get("keyItem").getAsInt()))return text(-1,"电梯需要电梯钥匙。先寻找火箭队遗落的钥匙。");
                lift=map;var choose=object("SELECT_OPTION");var options=new JsonArray();
                String current=get(db,owner,"lift/"+map,baseFloor(elevator));
                for(var value:elevator.getAsJsonArray("floors")){var f=value.getAsJsonObject();options.add(f.get("label").getAsString()+(f.get("map").getAsString().equals(current)?"（当前）":""));}
                options.add("取消");choose.add("options",options);choose.addProperty("message","要前往哪一层？");return plan(-1,choose);
            }
            if(map.startsWith("PokemonMansion_")&&script.endsWith("_EventScript_Statue"))return node("mansion",-1);
            if(map.equals("VermilionCity_Gym")&&script.contains("_EventScript_TrashCan"))return trash(Integer.parseInt(script.substring(script.lastIndexOf("TrashCan")+8))-1);
        }
        if(request.event().equals("NPC")||request.event().equals("PUSH")){
            var definition=boulder(map,request.npc());if(definition==null||!definition.get("pushable").getAsBoolean())return null;
            var view=views.stream().filter(v->v.bank()==p.bank()&&v.map()==p.map()).findFirst().orElse(null);if(view==null)return null;
            var actor=view.npcs().stream().filter(n->n.id()==request.npc()).findFirst().orElse(null);if(actor==null||Math.abs(actor.x()-p.x())+Math.abs(actor.y()-p.y())!=1)return null;
            int[] dx={0,0,-1,1},dy={1,-1,0,0};int d=p.direction();
            if(actor.x()!=p.x()+dx[d]||actor.y()!=p.y()+dy[d])return null;
            if(!strength){
                if(!KantoMainline.progress(db,owner).bit("badges",3)||!KantoMainline.hasItem(db,owner,342))return text(actor.id(),"巨石纹丝不动。取得彩虹徽章和园长赠送的怪力秘传机后，才能推动它。");
                return node("strength",actor.id());
            }
            int nx=actor.x()+dx[d],ny=actor.y()+dy[d];int block;
            try{block=MapEntities.block(p.bank(),p.map(),nx,ny);}catch(IllegalArgumentException outside){return plan(actor.id(),object("CLOSE_SCENE"));}
            for(var t:view.tiles())if(t.x()==nx&&t.y()==ny)block=t.block();
            JsonObject fall=null,pressure=null;
            for(var v:resource("falls")){var f=v.getAsJsonObject();if(f.get("map").getAsString().equals(map)&&f.get("x").getAsInt()==nx&&f.get("y").getAsInt()==ny)fall=f;}
            int elevation=MapEntities.block(p.bank(),p.map(),p.x(),p.y())>>>12,nextElevation=block>>>12;
            final int tx=nx,ty=ny;
            if(fall==null&&((block&0xc00)!=0||nextElevation==15||nextElevation==1||elevation!=0&&nextElevation!=0&&elevation!=nextElevation||MapEntities.warpTile(p.bank(),p.map(),nx,ny)))return plan(actor.id(),object("CLOSE_SCENE"));
            if(view.npcs().stream().anyMatch(n->n.id()!=actor.id()&&n.x()==tx&&n.y()==ty))return plan(actor.id(),object("CLOSE_SCENE"));
            for(var v:resource("tileGroups")){var g=v.getAsJsonObject();if(g.get("kind").getAsString().equals("pressure")&&g.get("map").getAsString().equals(map)&&g.get("x").getAsInt()==nx&&g.get("y").getAsInt()==ny)pressure=g;}
            pending=new Push(map,actor.id(),nx,ny,fall,pressure);
            var move=object("MOVE_NPC");move.addProperty("actorIndex",actor.id());move.addProperty("message","STRENGTH_PUSH");var target=new JsonObject();target.addProperty("x",nx);target.addProperty("y",ny);move.add("target",target);
            var player=object("MOVE_PLAYER");player.addProperty("x",actor.x());player.addProperty("y",actor.y());
            return plan(actor.id(),move,player,object("CLOSE_SCENE"));
        }
        return null;
    }
    void operate(String operation){
        if(operation.equals("LEAGUE_CLOSE_ENTRY")){leagueEntering=null;return;}
        if(operation.equals("STRENGTH_ON")){strength=true;return;}
        if(operation.equals("MANSION_TOGGLE")){
            db.transaction(()->{String flag="kanto/mechanism/mansion";if(KantoMainline.event(db,owner,flag))db.execute("DELETE FROM character_story_event WHERE character_id=? AND flag=?",owner,flag);else KantoMainline.eventSet(db,owner,flag);});return;
        }
        throw new IllegalArgumentException("Unknown map mechanism "+operation);
    }
    Plan choose(int index){
        var elevator=find(resource("elevators"),"map",lift);if(elevator==null)throw new IllegalStateException("No elevator choice pending");
        var floors=elevator.getAsJsonArray("floors");if(index<0||index>floors.size())throw new IllegalArgumentException("Invalid elevator floor");
        if(index==floors.size())return plan(-1,object("CLOSE_SCENE"));
        var floor=floors.get(index).getAsJsonObject();String target=floor.get("map").getAsString();
        String old=get(db,owner,"lift/"+lift,"");set("lift/"+lift,target);
        if(target.equals(old))return plan(-1,object("CLOSE_SCENE"));
        var wait=object("WAIT");wait.addProperty("duration",600);return plan(-1,wait,object("CLOSE_SCENE"));
    }
    private static String baseFloor(JsonObject elevator){
        var floors=elevator.getAsJsonArray("floors");
        for(var f:floors){var o=f.getAsJsonObject();if(o.get("label").getAsString().equals(elevator.get("keyItem").getAsInt()>0?"B1F":"1F"))return o.get("map").getAsString();}
        throw new IllegalStateException("Missing elevator base floor");
    }
    private Plan exitLift(JsonObject elevator){
        String selected=get(db,owner,"lift/"+elevator.get("map").getAsString(),baseFloor(elevator));
        var floor=find(elevator.getAsJsonArray("floors"),"map",selected);if(floor==null)throw new IllegalStateException("Invalid saved elevator floor");
        var move=floor.deepCopy();move.addProperty("type","CHANGE_MAP");return plan(-1,move,object("CLOSE_SCENE"));
    }
    private Plan trash(int bin){
        String flag="kanto/mechanism/vermilion_locks";if(KantoMainline.event(db,owner,flag))return text(-1,"电流已经关闭，可以前往挑战马志士。");
        String value=get(db,owner,"gym_pair","");if(value.isEmpty()){resetBins();value=get(db,owner,"gym_pair","");}
        var state=value.split(",");int first=Integer.parseInt(state[0]),second=Integer.parseInt(state[1]);
        if(state[2].equals("0")){
            if(bin!=first)return text(-1,"里面只有垃圾。");set("gym_pair",first+","+second+",1");return text(-1,"找到第一个开关！第一道电流关闭了，第二个开关就在相邻的垃圾桶中。");
        }
        if(bin==second){KantoMainline.eventSet(db,owner,flag);return text(-1,"找到第二个开关！两道电流都关闭了。");}
        resetBins();return text(-1,"第二个开关不在这里，两道锁已经重置。请重新寻找。");
    }
    private void savePose(String map,int npc,Pose p){db.execute("INSERT INTO character_map_boulder VALUES (?,?,?,?,?,?) ON CONFLICT(character_id,map_key,npc) DO UPDATE SET x=excluded.x,y=excluded.y,hidden=excluded.hidden",owner,map,npc,p.x,p.y,p.hidden?1:0);}
    void acknowledge(StoryData.Action action){
        if(pending==null||!action.type().equals("MOVE_NPC")||pending.npc!=action.actor())return;
        var push=pending;var pose=new Pose(push.x,push.y,false);
        db.transaction(()->{
            if(push.fall!=null){
                savePose(push.map,push.npc,new Pose(push.x,push.y,true));
                String target=push.fall.get("targetMap").getAsString();int npc=push.fall.get("targetNpc").getAsInt();var definition=boulder(target,npc);
                savePose(target,npc,new Pose(definition.get("x").getAsInt(),definition.get("y").getAsInt(),false));
                if(target.equals("SeafoamIslands_B3F")||target.equals("SeafoamIslands_B4F")){var count=db.query("SELECT COUNT(*) FROM character_map_boulder WHERE character_id=? AND map_key=? AND npc IN (0,1) AND hidden=0",owner,target);if(count.get(0)[0].equals("2"))KantoMainline.eventSet(db,owner,"kanto/mechanism/current/"+target);}
            }else if(push.pressure!=null){
                savePose(push.map,push.npc,pose);KantoMainline.eventSet(db,owner,push.pressure.get("flag").getAsString());
                if(push.map.equals("VictoryRoad_3F"))for(var e:temporary.entrySet())if(e.getKey().startsWith(push.map+":"))savePose(push.map,Integer.parseInt(e.getKey().substring(e.getKey().lastIndexOf(':')+1)),e.getValue());
            }else if(!db.query("SELECT npc FROM character_map_boulder WHERE character_id=? AND map_key=? AND npc=?",owner,push.map,Integer.toString(push.npc)).isEmpty())savePose(push.map,push.npc,pose);
        });
        temporary.put(actorKey(push.map,push.npc),push.fall==null?pose:new Pose(push.x,push.y,true));pending=null;
    }
    static List<MapEntities.Tile> tiles(Database db,String owner,int bank,int map,StoryData.State state){
        if(!state.enabled()||state.stage()<6)return List.of();String key=MapEntities.mapKey(bank,map);var result=new ArrayList<MapEntities.Tile>();
        for(var value:resource("tileGroups")){
            var group=value.getAsJsonObject();if(!group.get("map").getAsString().equals(key))continue;String kind=group.get("kind").getAsString();
            boolean enabled=group.has("flag")&&KantoMainline.event(db,owner,group.get("flag").getAsString());String mode=kind.equals("mansion")?(enabled?"on":"off"):(enabled?"off":"on");
            if(kind.equals("leagueExit")){var progress=KantoMainline.progress(db,owner);mode=progress.champion()&&!KantoPostgame.leagueActive(db,owner)||progress.elite()>group.get("stage").getAsInt()?"off":"on";}
            if(kind.equals("quiz")&&(KantoMainline.progress(db,owner).bit("badges",6)||KantoMainline.event(db,owner,group.get("trainerFlag").getAsString())))mode="off";
            if(kind.equals("gym")){if(KantoMainline.progress(db,owner).bit("badges",2))mode="off";else if(!enabled&&get(db,owner,"gym_pair",",,0").endsWith(",1"))mode="half";}
            for(var t:group.getAsJsonObject("states").getAsJsonArray(mode)){var o=t.getAsJsonObject();int x=o.get("x").getAsInt(),y=o.get("y").getAsInt(),original=MapEntities.block(bank,map,x,y);result.add(new MapEntities.Tile(x,y,o.has("block")?o.get("block").getAsInt():(original&0xf000)|o.get("metatile").getAsInt()|o.get("collision").getAsInt()<<10));}
        }
        return List.copyOf(result);
    }
    static List<MapEntities.MapView> projectSaved(Database db,String owner,List<MapEntities.MapView> views){return project(db,owner,views,Map.of());}
    static List<MapEntities.MapView> project(Database db,String owner,List<MapEntities.MapView> views,Map<String,Pose> temporary){
        var saved=new HashMap<String,Pose>();for(var row:db.query("SELECT map_key,npc,x,y,hidden FROM character_map_boulder WHERE character_id=?",owner))saved.put(actorKey(row[0],Integer.parseInt(row[1])),new Pose(Integer.parseInt(row[2]),Integer.parseInt(row[3]),!row[4].equals("0")));
        return views.stream().map(v->{String key=MapEntities.mapKey(v.bank(),v.map());var list=new ArrayList<MapEntities.Npc>();
            for(var n:v.npcs()) {var definition=boulder(key,n.id());if(definition==null){list.add(n);continue;}
                var pose=temporary.getOrDefault(actorKey(key,n.id()),saved.getOrDefault(actorKey(key,n.id()),new Pose(definition.get("x").getAsInt(),definition.get("y").getAsInt(),definition.get("initialHidden").getAsBoolean())));
                if(!pose.hidden)list.add(new MapEntities.Npc(n.id(),n.graphics(),pose.x,pose.y,n.elevation(),n.direction(),n.movement(),n.rangeX(),n.rangeY()));
            }return new MapEntities.MapView(v.bank(),v.map(),list,v.backgrounds(),v.tiles());}).toList();
    }
    List<MapEntities.MapView> project(List<MapEntities.MapView> views){
        var projected=project(db,owner,views,temporary);
        if(leagueEntering==null)return projected;
        return projected.stream().map(v->{if(!MapEntities.mapKey(v.bank(),v.map()).equals(leagueEntering))return v;
            var tiles=new ArrayList<>(v.tiles());
            for(var value:resource("tileGroups")){var g=value.getAsJsonObject();if(!g.get("kind").getAsString().equals("leagueEntry")||!g.get("map").getAsString().equals(leagueEntering))continue;
                for(var tile:g.getAsJsonObject("states").getAsJsonArray("off")){var t=tile.getAsJsonObject();int x=t.get("x").getAsInt(),y=t.get("y").getAsInt();tiles.removeIf(old->old.x()==x&&old.y()==y);tiles.add(new MapEntities.Tile(x,y,t.get("block").getAsInt()));}
            }return new MapEntities.MapView(v.bank(),v.map(),v.npcs(),v.backgrounds(),tiles);
        }).toList();
    }
}
