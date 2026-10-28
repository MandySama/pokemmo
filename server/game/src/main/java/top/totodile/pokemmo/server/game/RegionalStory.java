package top.totodile.pokemmo.server.game;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import static top.totodile.pokemmo.server.game.StoryData.*;

/** Region-owned chapter state behind the existing scene and battle protocol. */
abstract class RegionalStory {
    abstract State state();
    abstract List<MapEntities.MapView> entities();
    abstract Scene request(Request request,GameService.Position position,Map<Integer,GameService.ItemTemplate> items);
    abstract Action waiting();
    abstract boolean takeHealed();
    abstract boolean takeChanged();
    abstract boolean has(String flag);
    abstract EncounterData battle();
    abstract void finishBattle(boolean won);
    abstract JsonObject checkpoint();
    abstract StoryInspection.Report inspect(GameService.Position position);
}
