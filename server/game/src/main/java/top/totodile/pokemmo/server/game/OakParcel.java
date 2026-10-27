package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import java.util.*;

/** Local transaction adapter for reference OakParcelProgress/OakParcelStore/StoryInventory. */
final class OakParcel {
    private OakParcel() {}
    static void initialize(Database db) {
        db.execute("CREATE TABLE IF NOT EXISTS character_oak_parcel (character_id TEXT PRIMARY KEY"
                + " REFERENCES characters(id) ON DELETE CASCADE,status INTEGER NOT NULL DEFAULT 0)");
    }
    static String phase(Database db,String owner,StoryData.State state) {
        if(!state.enabled() || state.stage()<4)return "OPENING";
        if(state.stage()>=6)return "COMPLETE";
        var rows=db.query("SELECT status FROM character_oak_parcel WHERE character_id=?",owner);
        int status=rows.isEmpty()?0:Integer.parseInt(rows.get(0)[0]);
        return state.stage()==5||status==2?"POKEDEX":status==1?"DELIVER":"PICKUP";
    }
    static boolean hasParcel(Database db,String owner) {
        return !db.query("SELECT item_id FROM owned_item WHERE owner_id=? AND inventory_id=1"
                + " AND item_index_id=349 AND item_amount>0 LIMIT 1",owner).isEmpty();
    }
    static boolean needsPickup(Database db,String owner,StoryData.State state) {
        String phase=phase(db,owner,state);
        return phase.equals("PICKUP")||phase.equals("DELIVER")&&!hasParcel(db,owner);
    }
    static void commit(Database db,String owner,StoryData.State state,String operation,
                       Map<Integer,GameService.ItemTemplate> items) {
        db.transaction(()->{
            String phase=phase(db,owner,state);
            if(phase.equals("OPENING"))throw new IllegalStateException("请先完成首次劲敌战斗");
            if(phase.equals("COMPLETE"))return;
            switch(operation) {
                case "PICKUP" -> {
                    if(!needsPickup(db,owner,state))return;
                    if(!hasParcel(db,owner))insert(db,owner,349,1,0);
                    status(db,owner,1);
                }
                case "DELIVER" -> {
                    if(phase.equals("POKEDEX"))return;
                    var rows=db.query("SELECT item_id,item_amount FROM owned_item WHERE owner_id=?"
                            + " AND inventory_id=1 AND item_index_id=349 AND item_amount>0 ORDER BY item_id LIMIT 1",owner);
                    if(!phase.equals("DELIVER")||rows.isEmpty())throw new IllegalStateException("请回常磐市商店领取大木的包裹");
                    var row=rows.get(0);
                    if(Integer.parseInt(row[1])==1)db.execute("DELETE FROM owned_item WHERE item_id=? AND owner_id=?",row[0],owner);
                    else db.execute("UPDATE owned_item SET item_amount=item_amount-1 WHERE item_id=? AND owner_id=?",row[0],owner);
                    status(db,owner,2);
                    db.execute("UPDATE character_kanto_story SET stage=5 WHERE character_id=?",owner);
                }
                case "RECEIVE_POKEDEX" -> {
                    if(!phase.equals("POKEDEX"))throw new IllegalStateException("请先把包裹交给大木博士");
                    var metadata=items.get(5004);
                    if(metadata==null||metadata.maximumStack()<5)throw new IllegalStateException("缺少剧情精灵球数据");
                    int remaining=5;
                    for(var row:db.query("SELECT item_id,item_amount FROM owned_item WHERE owner_id=?"
                            + " AND inventory_id=1 AND item_index_id=5004 AND region=-1 ORDER BY item_id",owner)) {
                        int addition=Math.min(remaining,Math.max(0,metadata.maximumStack()-Integer.parseInt(row[1])));
                        if(addition>0)db.execute("UPDATE owned_item SET item_amount=item_amount+? WHERE item_id=? AND owner_id=?",addition,row[0],owner);
                        remaining-=addition;
                        if(remaining==0)break;
                    }
                    if(remaining>0)insert(db,owner,5004,remaining,-1);
                    db.execute("UPDATE character_kanto_story SET stage=6 WHERE character_id=?",owner);
                }
                default -> throw new IllegalArgumentException("未知包裹操作");
            }
        });
    }
    private static void status(Database db,String owner,int value) {
        db.execute("INSERT INTO character_oak_parcel(character_id,status) VALUES (?,?)"
                + " ON CONFLICT(character_id) DO UPDATE SET status=excluded.status",owner,value);
    }
    private static void insert(Database db,String owner,int item,int count,int region) {
        db.execute("INSERT INTO owned_item(item_id,owner_id,item_index_id,item_amount,inventory_id,region)"
                + " VALUES (?,?,?,?,1,?)",UUID.randomUUID().toString(),owner,item,count,region);
    }
}
