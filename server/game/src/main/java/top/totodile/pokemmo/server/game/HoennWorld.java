package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import java.util.*;

/** Reference Hoenn MapFile data, normalized to the native Emerald map group IDs. */
final class HoennWorld {
    static final JsonObject DATA=KantoMainline.read("/world/hoenn-maps.json");
    private static final Map<String,JsonObject> BY_ID=new HashMap<>(), BY_NAME=new HashMap<>();
    private static final Map<String,byte[]> BLOCKS=new HashMap<>(),BEHAVIORS=new HashMap<>();
    static {
        for(var value:DATA.getAsJsonArray("maps")) {
            var m=value.getAsJsonObject();BY_ID.put(m.get("bank").getAsInt()+":"+m.get("map").getAsInt(),m);
            BY_NAME.put(m.get("key").getAsString(),m);
            BLOCKS.put(m.get("key").getAsString(),Base64.getDecoder().decode(m.get("blockData").getAsString()));
            BEHAVIORS.put(m.get("key").getAsString(),Base64.getDecoder().decode(m.get("behaviorData").getAsString()));
        }
    }
    static JsonObject map(GameService.Position p){return BY_ID.get(p.bank()+":"+p.map());}
    static String key(GameService.Position p){var m=map(p);return m==null?"":m.get("key").getAsString();}
    static int block(GameService.Position p,int x,int y){var m=map(p);if(m==null||x<0||y<0||x>=m.get("width").getAsInt()||y>=m.get("height").getAsInt())throw new IllegalArgumentException("Hoenn tile outside map");int i=(y*m.get("width").getAsInt()+x)*2;var b=BLOCKS.get(key(p));return (b[i]&255)|((b[i+1]&255)<<8);}
    static int behavior(GameService.Position p){var b=BEHAVIORS.get(key(p));int tile=block(p,p.x(),p.y())&1023;return tile<b.length?b[tile]&255:0;}
    static int behavior(GameService.Position p,int x,int y){return behavior(new GameService.Position(p.bank(),p.map(),x,y,p.direction()));}
    static GameService.Position position(String key,int x,int y,int direction){var m=BY_NAME.get(key);if(m==null)throw new IllegalArgumentException("Missing Hoenn map "+key);return new GameService.Position(m.get("bank").getAsInt(),m.get("map").getAsInt(),x,y,direction);}
    static String script(GameService.Position p,int npc){var m=map(p);if(m==null)return "";for(var v:m.getAsJsonArray("npcs")){var n=v.getAsJsonObject();if(n.get("entityIdx").getAsInt()==npc)return n.get("script").getAsString();}return "";}
    static String background(GameService.Position p,int index){var m=map(p);if(m==null||index<0||index>=m.getAsJsonArray("backgrounds").size())return "";return m.getAsJsonArray("backgrounds").get(index).getAsJsonObject().get("script").getAsString();}
    static List<MapEntities.MapView> entities(int stage,int gender,java.util.function.Predicate<JsonObject> condition) {
        var result=new ArrayList<MapEntities.MapView>();String own=gender==0?"Brendans":"Mays";
        for(var m:BY_NAME.values()) {
            String key=m.get("key").getAsString();var npcs=new ArrayList<MapEntities.Npc>();
            for(var value:m.getAsJsonArray("npcs")) {
                var n=value.getAsJsonObject();int id=n.get("entityIdx").getAsInt(),gfx=n.get("graphicsId").getAsInt(),x=n.get("x").getAsInt(),y=n.get("y").getAsInt(),movement=n.get("rawMovementType").getAsInt();
                int direction=direction(n.get("facing").getAsString());String flag=n.has("hideFlag")?n.get("hideFlag").getAsString():"",script=n.get("script").getAsString();
                boolean visible=!flag.contains("FLAG_HIDE_")&&gfx!=0;boolean house=key.contains("House"), ownHouse=key.contains(own+"House");
                if(stage>=16&&(gfx==82||gfx==86||gfx==87))visible=true;
                if(key.equals("LittlerootTown")) {
                    if(id==0&&stage<11){x=9;y=1;movement=8;direction=0;}
                    if(id==1)visible=stage>=3;
                    if(id==3){visible=stage==1||stage==15;x=gender==0?5:14;y=stage==1?10:9;direction=2;movement=8;}
                    if(id==4||id==5)visible=stage<=1&&(id==4)==(gender==0);
                    if(id==6||id==7)visible=false;
                }
                if(key.startsWith("LittlerootTown_")&&house&&key.endsWith("_1F")) {
                    visible=switch(id){case 0->ownHouse;case 1,2->ownHouse&&stage<=4;case 3,5->!ownHouse;default->false;};
                    if(id==0&&ownHouse&&stage==2){x=gender==0?7:3;y=8;direction=gender==0?3:2;movement=8;}
                }
                if(key.startsWith("LittlerootTown_")&&house&&key.endsWith("_2F")) {
                    if(id==0){var entered=new JsonObject();entered.addProperty("type","HOENN_FLAG_SET");entered.addProperty("flag","rival_entered");visible=!ownHouse&&stage<11&&(stage>=8||stage==7&&condition.test(entered));if(stage>=8){x=gender==0?8:0;y=2;}}
                    if(gfx==0)visible=false;
                    if(id==13){visible=ownHouse&&stage==4;x=gender==0?6:2;y=2;direction=gender==0?2:3;movement=8;}
                    if(script.endsWith("RivalsPokeBall"))visible=!ownHouse&&stage<8;
                }
                if(key.equals("LittlerootTown_ProfessorBirchsLab")) {
                    visible=id==0||id==1&&stage>=11||id==2&&stage>=13&&stage<=14;
                    if(id==2)gfx=gender==0?105:100;
                }
                if(key.equals("Route101")) {
                    if(id==1||id==2||id==3)visible=stage>=8&&stage<=10;
                    if(id==4)visible=false;
                    if(id==5)visible=stage>=12;
                }
                if(key.equals("Route103")&&id==1){visible=stage==12;gfx=gender==0?105:100;}
                if(stage>=16){
                    if(!visible&&HoennMainline.triggeredNpc(key,id,condition))visible=true;
                    for(var rv:HoennMainline.PROGRAM.getAsJsonArray("visibility")){
                        var r=rv.getAsJsonObject();if(!r.get("map").getAsString().equals(key)||r.get("npc").getAsInt()!=id)continue;
                        visible=stage>=r.get("min").getAsInt()&&stage<=r.get("max").getAsInt();
                        if(r.has("graphics"))gfx=r.get("graphics").getAsInt();
                    }
                    if(key.equals("PetalburgCity_Gym")&&id==0){visible=true;if(stage<30){x=4;y=107;}}
                    if(key.equals("EverGrandeCity_ChampionsRoom")&&id==0)visible=true;
                }
                if(visible)npcs.add(new MapEntities.Npc(id,gfx,x,y,n.get("elevation").getAsInt(),direction,movement,n.get("movementRangeX").getAsInt(),n.get("movementRangeY").getAsInt()));
            }
            var backgrounds=new ArrayList<MapEntities.Background>();int index=0;
            for(var value:m.getAsJsonArray("backgrounds")) {var b=value.getAsJsonObject();String facing=b.get("facingDir").getAsString();backgrounds.add(new MapEntities.Background(index++,b.get("x").getAsInt(),b.get("y").getAsInt(),b.get("elevation").getAsInt(),facing.equals("ANY")?-1:direction(facing)));}
            result.add(new MapEntities.MapView(m.get("bank").getAsInt(),m.get("map").getAsInt(),npcs,backgrounds));
        }
        return List.copyOf(result);
    }
    private static int direction(String value){return switch(value.substring(value.lastIndexOf('.')+1)){case "NORTH","UP"->1;case "WEST","LEFT"->2;case "EAST","RIGHT"->3;default->0;};}
}
