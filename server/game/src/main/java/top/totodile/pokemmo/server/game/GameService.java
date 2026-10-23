package top.totodile.pokemmo.server.game;

import java.util.*;
import java.util.concurrent.CompletionStage;
import top.totodile.pokemmo.server.login.LoginService.Selection;

public interface GameService extends AutoCloseable {
    record Part(int style, int color) {}
    record Appearance(int gender, int skin, int region, Map<Integer, Part> parts) {
        public Appearance { parts = Map.copyOf(parts); }
    }
    record Character(String id, String name, Appearance appearance, long createdAt) {}
    record Position(int bank, int map, int x, int y, int direction) {
        public static Position kantoStart() { return new Position(4,1,6,6,0); }
        /** Starting map IDs use ROM headers; existing saved positions always take precedence. */
        public static Position start(int region) {
            return switch(region) {
                case 0 -> kantoStart();
                case 1 -> new Position(0,9,5,9,0);
                case 2 -> new Position(1,133,14,14,0);
                case 3 -> new Position(1,155,9,12,0);
                case 4 -> new Position(0,60,12,10,0);
                default -> throw new IllegalArgumentException("Invalid starting region");
            };
        }
        public Position {
            if(bank<0 || bank>255 || map<0 || map>255 || x<0 || x>1023 || y<0 || y>1023 || direction<0 || direction>3)
                throw new IllegalArgumentException("Invalid character position");
        }
    }
    record TravelState(boolean noClip,float speed,boolean bicycle) {
        public static TravelState defaults() { return new TravelState(false,1,false); }
        public TravelState {
            if(!Float.isFinite(speed) || speed<=0 || speed>16)throw new IllegalArgumentException("Invalid character speed");
        }
    }
    /** Local Pokédex currently exposes the all-unlocked / empty debug states. */
    record PokedexState(boolean unlocked) {
        public static PokedexState defaults() { return new PokedexState(false); }
    }
    /** CharacterData money/channel and SendJoinGameWorldResponsePacket's game clock origin. */
    record WorldInfo(int money,int channel,long timeOrigin) {
        public static WorldInfo defaults(){return new WorldInfo(0,0,1640995200L);}
    }
    record Entry(Character character, Position position,TravelState travelState,PokedexState pokedexState,WorldInfo worldInfo) {
        public Entry(Character character,Position position) {this(character,position,TravelState.defaults());}
        public Entry(Character character,Position position,TravelState travelState) {this(character,position,travelState,PokedexState.defaults());}
        public Entry(Character character,Position position,TravelState travelState,PokedexState pokedexState) {
            this(character,position,travelState,pokedexState,WorldInfo.defaults());
        }
    }
    enum Failure { INVALID_NAME, NAME_TAKEN, ACCOUNT_FULL, NOT_FOUND }
    final class Rejected extends RuntimeException {
        public final Failure reason;
        public Rejected(Failure reason) { super(reason.name()); this.reason = reason; }
    }
    CompletionStage<List<Character>> characters(Selection selection);
    CompletionStage<Character> create(Selection selection, String name, Appearance appearance);
    CompletionStage<Void> delete(Selection selection, String characterId);
    CompletionStage<Entry> enter(Selection selection, String characterId);
    CompletionStage<Void> savePosition(Selection selection, String characterId, Position position);
    CompletionStage<Void> saveTravelState(Selection selection,String characterId,TravelState state);
    CompletionStage<Void> savePokedexState(Selection selection,String characterId,PokedexState state);
    /** Payload of the reference SendPokemonPvpLevelInfoPacket (client opcode 79). */
    default byte[] pokedexTiers(){return new byte[0];}
    @Override void close();
}
