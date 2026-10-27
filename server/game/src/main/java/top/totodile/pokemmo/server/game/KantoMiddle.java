package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import top.totodile.pokemmo.db.Database;
import java.util.*;

/** User-authorized middle-story support; native map geometry, existing finite story actions. */
final class KantoMiddle {
    private KantoMiddle() {}
    static List<MapEntities.Tile> tiles(Database db,String owner,int bank,int map,StoryData.State state) {
        if(!state.enabled()||state.stage()<6)return List.of();
        String key=MapEntities.mapKey(bank,map);
        var result=new ArrayList<MapEntities.Tile>();
        var program=KantoMainline.program("kanto.middle_support");
        for(var value:program.getAsJsonObject("resources").getAsJsonArray("doors")) {
            var door=value.getAsJsonObject();
            if(!door.get("map").getAsString().equals(key))continue;
            boolean open=KantoMainline.event(db,owner,door.get("flag").getAsString());
            for(var tile:door.getAsJsonArray("tiles")) {
                var t=tile.getAsJsonObject();int x=t.get("x").getAsInt(),y=t.get("y").getAsInt();
                int original=MapEntities.block(bank,map,x,y);
                int block=open?original:(original&0xf000)|t.get("metatile").getAsInt()|(t.get("collision").getAsInt()<<10);
                result.add(new MapEntities.Tile(x,y,block));
            }
        }
        result.addAll(KantoMechanisms.tiles(db,owner,bank,map,state));
        return List.copyOf(result);
    }
}
