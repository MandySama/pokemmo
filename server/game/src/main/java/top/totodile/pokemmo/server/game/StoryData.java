package top.totodile.pokemmo.server.game;

import java.util.*;

/** Local equivalents of the reference story scene/dialogue/movement responses. */
public final class StoryData {
    private StoryData() {}

    /** Decode the immutable server-recorded team without exposing its JSON codec to the UI. */
    public static List<PokemonData.Pokemon> hallParty(Action action) {
        if(!action.type().equals("HALL_OF_FAME"))throw new IllegalArgumentException("Not a Hall of Fame action");
        var gson=new com.google.gson.Gson();
        return action.options().stream().map(value->gson.fromJson(value,PokemonData.Pokemon.class)).toList();
    }

    public record Request(String event, int npc, long sequence, int answer, GameService.Position position) {
        public Request(String event, int npc, long sequence, int answer) {
            this(event, npc, sequence, answer, null);
        }
        public Request at(GameService.Position position) {
            return new Request(event, npc, sequence, answer, position);
        }
        public static Request ready() {
            return new Request("MAP_READY", -1, 0, 0);
        }

        public static Request step() {
            return new Request("COORDINATE", -1, 0, 0);
        }

        public static Request npc(int npc) {
            return new Request("NPC", npc, 0, 0);
        }

        public static Request background(int background) {
            return new Request("BACKGROUND", background, 0, 0);
        }

        public static Request reply(long sequence, int answer) {
            return new Request("REPLY", -1, sequence, answer);
        }
    }

    public record State(boolean enabled, int stage, int starter) {}

    public record Action(
            long sequence,
            String type,
            int text,
            int actor,
            int species,
            GameService.Position target,
            GameService.Position playerTarget,
            int music,
            String message,
            int duration, List<String> options) {
        public Action {options=List.copyOf(options);}
        public Action(long sequence,String type,int text,int actor,int species,GameService.Position target,GameService.Position playerTarget,int music,String message,int duration){this(sequence,type,text,actor,species,target,playerTarget,music,message,duration,List.of());}
        public Action(long sequence,String type,int text,int actor,int species,
                      GameService.Position target,GameService.Position playerTarget,int music,String message) {
            this(sequence,type,text,actor,species,target,playerTarget,music,message,0);
        }
    }

    public record Scene(State state, Action action, List<MapEntities.MapView> entities, boolean partyHealed, boolean inventoryChanged) {
        public Scene(State state, Action action, List<MapEntities.MapView> entities, boolean partyHealed) {
            this(state, action, entities, partyHealed, false);
        }
        public Scene(State state, Action action, List<MapEntities.MapView> entities) {
            this(state, action, entities, false);
        }
        public Scene { entities = List.copyOf(entities); }
        public Scene(State state, Action action) { this(state, action, List.of()); }
    }
}
