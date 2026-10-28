package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import java.util.Arrays;

/** Owner-scoped field access; inventory IDs follow the reference Item registry. */
final class TraversalAccess {
    private TraversalAccess() {}
    static boolean bicycle(Database db,String owner,int region){
        int[] ids=switch(region){case 0->new int[]{360};case 1->new int[]{259,272};
            case 2->new int[]{5450};case 3->new int[]{8450};case 4->new int[]{9450};default->new int[0];};
        for(int id:ids)if(owned(db,owner,id,region))return true;
        return false;
    }
    private static boolean owned(Database db,String owner,int item,int region){
        return !db.query("SELECT item_id FROM owned_item WHERE owner_id=? AND inventory_id=1 AND item_index_id=? AND item_amount>0 AND (region=? OR region IN (-1,10)) LIMIT 1",
                owner,Integer.toString(item),Integer.toString(region)).isEmpty();
    }
    static boolean surf(Database db,String owner,int region){
        // Only Kanto and Hoenn currently expose authoritative badge progression here.
        if(region!=0&&region!=1)return false;
        int badges=region==0?KantoMainline.progress(db,owner).badges():HoennMainline.badges(db,owner);
        if((badges&16)==0)return false;
        if(KantoMainline.hasItem(db,owner,1182))return true;
        return owned(db,owner,341,region)&&db.query("SELECT moves FROM pokemon WHERE trainer_id=? AND container_id=1 AND is_egg=0",owner).stream()
                .anyMatch(p->Arrays.asList(p[0].split(",")).contains("57"));
    }
}
