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
    enum Failure { INVALID_NAME, NAME_TAKEN, ACCOUNT_FULL, NOT_FOUND }
    final class Rejected extends RuntimeException {
        public final Failure reason;
        public Rejected(Failure reason) { super(reason.name()); this.reason = reason; }
    }
    CompletionStage<List<Character>> characters(Selection selection);
    CompletionStage<Character> create(Selection selection, String name, Appearance appearance);
    CompletionStage<Void> delete(Selection selection, String characterId);
    @Override void close();
}
