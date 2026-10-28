package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import java.util.*;

/** Native RivalsHouse OnTransition and Daisy dialogue state, independent of Oak's checkpoint. */
final class PalletDaisy {
    static final String FLAG = "kanto/FLAG_HIDE_TOWN_MAP";
    private final Database db;
    private final String owner;
    private String lastMap = "";
    private boolean ownedOnEntry;

    PalletDaisy(Database db, String owner) { this.db = db; this.owner = owner; }
    static boolean received(Database db, String owner) {
        return KantoMainline.event(db, owner, FLAG) || KantoMainline.hasItem(db, owner, 361);
    }
    boolean arrive(StoryData.Request request, GameService.Position p) {
        String map = p.bank() + ":" + p.map();
        if (!map.equals(lastMap)) { lastMap = map; ownedOnEntry = received(db, owner); return true; }
        return false;
    }
    String node(StoryData.State state, Map<Integer,GameService.SpeciesTemplate> templates) {
        if (KantoMainline.progress(db,owner).champion()) {
            var lead = new PokemonStore(db,templates).load(owner).pokemons().stream()
                    .filter(p -> p.container()==1 && !p.egg())
                    .min(Comparator.comparingInt(PokemonData.Pokemon::position));
            int f = lead.map(PokemonData.Pokemon::friendship).orElse(0);
            int rank = f==255?6:f>=200?5:f>=150?4:f>=100?3:f>=50?2:f>0?1:0;
            return "friendship" + rank;
        }
        if (received(db,owner)) return ownedOnEntry ? "rest" : "explain";
        if (state.enabled() && state.stage()>=6) return "give";
        return state.enabled() && state.stage()>=4 ? "battle" : "intro";
    }
    void giveMap(Map<Integer,GameService.ItemTemplate> items) {
        db.transaction(() -> {
            if (!received(db,owner)) KantoMainline.grant(db,owner,361,1,items);
            KantoMainline.eventSet(db,owner,FLAG);
        });
    }
    static List<MapEntities.MapView> project(Database db,String owner,List<MapEntities.MapView> maps) {
        boolean owned=received(db,owner);
        return maps.stream().map(m -> m.bank()!=4 || m.map()!=2 ? m :
                new MapEntities.MapView(4,2,m.npcs().stream()
                        .filter(n -> !owned || n.id()!=1)
                        .map(n -> !owned && n.id()==0
                                ? new MapEntities.Npc(n.id(),n.graphics(),5,4,n.elevation(),3,10,0,0)
                                : n).toList(),m.backgrounds(),m.tiles())).toList();
    }
}
