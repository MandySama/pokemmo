package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import top.totodile.pokemmo.db.Database;
import java.util.*;

/** Native FireRed Sevii flags adapted to the existing finite story runtime. */
final class KantoPostgame {
    static final String NATIONAL="kanto/FLAG_SYS_NATIONAL_DEX",LINK="kanto/FLAG_SYS_CAN_LINK_WITH_RS";
    static boolean flag(Database db,String owner,String name){return KantoMainline.event(db,owner,name);}
    static int owned(Database db,String owner){return Integer.parseInt(db.query("SELECT COUNT(*) FROM character_pokedex_entry WHERE character_id=? AND species BETWEEN 1 AND 151 AND (flags & 2)!=0",owner).get(0)[0]);}
    static boolean leagueActive(Database db,String owner){return KantoMechanisms.get(db,owner,"postgame/league_active","0").equals("1");}
    static void active(Database db,String owner,boolean active){db.execute("INSERT INTO character_map_mechanism VALUES (?,'postgame/league_active',?) ON CONFLICT(character_id,name) DO UPDATE SET value=excluded.value",owner,active?"1":"0");}
    static void arrive(Database db,String owner,StoryData.Request request,GameService.Position p){
        if(!request.event().equals("MAP_READY"))return;
        String map=MapEntities.mapKey(p.bank(),p.map());
        if(map.equals("OneIsland")||map.startsWith("OneIsland_"))KantoMainline.eventSet(db,owner,"kanto/FLAG_WORLD_MAP_ONE_ISLAND");
        if(map.equals("IndigoPlateau_PokemonCenter_1F")&&KantoMainline.progress(db,owner).champion()&&leagueActive(db,owner)){
            db.transaction(()->{active(db,owner,false);db.execute("UPDATE character_kanto_mainline SET elite=0 WHERE character_id=?",owner);});
        }
        if(map.equals("PokemonLeague_LoreleisRoom")&&KantoMainline.progress(db,owner).champion()&&flag(db,owner,LINK)&&!leagueActive(db,owner)){
            db.transaction(()->{KantoMainline.ensure(db,owner);db.execute("UPDATE character_kanto_mainline SET elite=0 WHERE character_id=?",owner);active(db,owner,true);});
        }
    }
    static boolean visible(Database db,String owner,String map,int npc){
        if(map.equals("CinnabarIsland")&&(npc==2||npc==3))return KantoMainline.progress(db,owner).champion()&&!flag(db,owner,"kanto/FLAG_SEVII_DETOUR_FINISHED");
        if(map.equals("FourIsland_LoreleisHouse")&&npc==0)return flag(db,owner,"kanto/FLAG_HIDE_RUIN_VALLEY_SCIENTIST")&&!flag(db,owner,LINK);
        if(map.equals("ThreeIsland_BerryForest")&&npc==0)return !flag(db,owner,"kanto/FLAG_RESCUED_LOSTELLE");
        if(map.equals("TwoIsland_JoyfulGameCorner")&&npc==2)return flag(db,owner,"kanto/FLAG_RESCUED_LOSTELLE");
        if(map.equals("OneIsland_PokemonCenter_1F")&&npc==1)return !flag(db,owner,"kanto/FLAG_SEVII_DETOUR_FINISHED");
        if(map.equals("MtEmber_Exterior")&&(npc==1||npc==2))return !(flag(db,owner,"kanto/postgame/ember_grunt1")&&flag(db,owner,"kanto/postgame/ember_grunt2"));
        if(map.equals("SixIsland_DottedHole_SapphireRoom")&&npc==1)return false; // Native addobject actor exists only during the theft scene.
        return true;
    }
    /** Null is an ordinary trainer; empty means this quest trainer is not currently eligible. */
    static String trainerNode(Database db,String owner,String map,int npc){
        if(map.equals("FiveIsland_RocketWarehouse")&&(npc==2||npc==3||npc==5)){
            if(!flag(db,owner,"kanto/FLAG_UNLOCKED_ROCKET_WAREHOUSE"))return "";
            if(npc==5)return !flag(db,owner,"kanto/FLAG_RECOVERED_SAPPHIRE")&&flag(db,owner,"kanto/FLAG_DEFEATED_ROCKETS_IN_WAREHOUSE")?"warehouse_5_fight":"";
            return flag(db,owner,"kanto/postgame/warehouse_"+npc)?"":"warehouse_"+npc+"_fight";
        }
        return null;
    }
    static List<MapEntities.Tile> tiles(Database db,String owner,int bank,int map){
        var result=new ArrayList<MapEntities.Tile>();var program=KantoMainline.program("kanto.postgame");if(program==null)return result;
        String key=MapEntities.mapKey(bank,map);
        for(var v:program.getAsJsonArray("tiles")){
            var t=v.getAsJsonObject();if(!t.get("map").getAsString().equals(key))continue;
            boolean open=flag(db,owner,t.get("flag").getAsString());
            if(!open&&!t.has("closed"))continue;
            int x=t.get("x").getAsInt(),y=t.get("y").getAsInt();int original=MapEntities.block(bank,map,x,y);
            int block=(original&0xf000)|(open?t.get("open").getAsInt():t.get("closed").getAsInt());result.add(new MapEntities.Tile(x,y,block));
        }
        return result;
    }
    static List<String> next(Database db,String owner){
        if(!flag(db,owner,"kanto/FLAG_SEVII_DETOUR_FINISHED"))return List.of(KantoMainline.hasItem(db,owner,367)?"七岛前置：寻找洛丝丽，交付陨石后返回一之岛与正辉交谈。":"七岛前置：前往红莲岛，与正辉交谈后到一之岛中心领取三岛通行证。");
        if(!flag(db,owner,NATIONAL))return List.of("全国图鉴：已拥有的关都种类 "+owned(db,owner)+"/60；满足后回大木研究所与博士交谈。");
        if(!flag(db,owner,"kanto/FLAG_GOT_RUBY"))return List.of("与一之岛锦辉交谈，前往灯火山击败入口两名火箭队员并寻找红宝石。");
        if(!flag(db,owner,"kanto/postgame/ruby_delivered"))return List.of("把红宝石交给锦辉，换取七岛通行证。");
        if(!flag(db,owner,"kanto/FLAG_HIDE_RUIN_VALLEY_SCIENTIST"))return List.of("前往四之岛冰瀑洞窟，协助科拿击退火箭队。");
        if(!flag(db,owner,"kanto/FLAG_LEARNED_YES_NAH_CHANSEY"))return List.of("在六之岛遗迹山谷使用居合斩打开点之穴，调查蓝宝石。");
        if(!flag(db,owner,"kanto/FLAG_RECOVERED_SAPPHIRE"))return List.of("用两段密码打开五之岛仓库，击败干部和科学家夺回蓝宝石。");
        if(!flag(db,owner,LINK))return List.of("把蓝宝石交给一之岛锦辉，完成网络机器。");
        return List.of("七岛红蓝宝石任务已完成；可挑战强化四天王，或前往华蓝洞窟。");
    }
}
