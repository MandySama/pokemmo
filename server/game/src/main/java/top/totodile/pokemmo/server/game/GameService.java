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
        public Position {
            if(bank<0 || bank>255 || map<0 || map>255 || x<0 || x>1023 || y<0 || y>1023 || direction<0 || direction>3)
                throw new IllegalArgumentException("Invalid character position");
        }
    }
    record Entry(Character character, Position position) {}
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
    @Override void close();
}
