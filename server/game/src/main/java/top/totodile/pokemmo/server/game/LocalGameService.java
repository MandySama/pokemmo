package top.totodile.pokemmo.server.game;

import java.util.*;
import top.totodile.pokemmo.db.Database;
import java.util.concurrent.*;
import java.util.function.Supplier;
import top.totodile.pokemmo.server.login.LoginService.Selection;

/** 单机角色服务；所有数据库操作在同一后台线程执行。 */
public final class LocalGameService implements GameService {
    private final Supplier<Database> factory;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "local-game"); t.setDaemon(true); return t;
    });
    private Database database;
    private final java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean();
    public LocalGameService(Supplier<Database> factory) { this.factory = factory; }
    private Database db() {
        if (database == null) {
            Database opened = Objects.requireNonNull(factory.get());
            try {
                opened.execute("PRAGMA foreign_keys=ON");
                opened.execute("CREATE TABLE IF NOT EXISTS characters (id TEXT PRIMARY KEY, account TEXT NOT NULL, server TEXT NOT NULL, name TEXT NOT NULL, name_key TEXT NOT NULL, gender INTEGER NOT NULL, skin INTEGER NOT NULL, region INTEGER NOT NULL, created_at INTEGER NOT NULL, UNIQUE(server,name_key))");
                opened.execute("CREATE TABLE IF NOT EXISTS character_parts (character_id TEXT NOT NULL REFERENCES characters(id) ON DELETE CASCADE, slot INTEGER NOT NULL, style INTEGER NOT NULL, color INTEGER NOT NULL, PRIMARY KEY(character_id,slot))");
                opened.execute("CREATE TABLE IF NOT EXISTS character_positions (character_id TEXT PRIMARY KEY REFERENCES characters(id) ON DELETE CASCADE, bank INTEGER NOT NULL, map INTEGER NOT NULL, x INTEGER NOT NULL, y INTEGER NOT NULL, direction INTEGER NOT NULL)");
                database = opened;
            } catch (RuntimeException | Error error) {
                try { opened.close(); }
                catch (RuntimeException | Error closeError) { error.addSuppressed(closeError); }
                throw error;
            }
        }
        return database;
    }
    private static void identity(Selection selection) {
        Objects.requireNonNull(selection);
        if (selection.session().username().isBlank()) throw new IllegalArgumentException("Invalid local identity");
    }
    @Override public CompletionStage<List<Character>> characters(Selection selection) {
        return CompletableFuture.supplyAsync(() -> {
            identity(selection);
            List<Character> result = new ArrayList<>();
            for (String[] row : db().query("SELECT id,name,gender,skin,region,created_at FROM characters WHERE account=? AND server=? ORDER BY created_at,id",
                    selection.session().username(), selection.server().id())) {
                Map<Integer,Part> parts = new HashMap<>();
                for (String[] part : db().query("SELECT slot,style,color FROM character_parts WHERE character_id=?", row[0]))
                    parts.put(Integer.parseInt(part[0]), new Part(Integer.parseInt(part[1]), Integer.parseInt(part[2])));
                result.add(new Character(row[0], row[1], new Appearance(Integer.parseInt(row[2]), Integer.parseInt(row[3]), Integer.parseInt(row[4]), parts), Long.parseLong(row[5])));
            }
            return List.copyOf(result);
        }, worker);
    }
    @Override public CompletionStage<Character> create(Selection selection, String name, Appearance appearance) {
        return CompletableFuture.supplyAsync(() -> {
            identity(selection);
            if (name == null || name.codePointCount(0, name.length()) < 2 || name.codePointCount(0, name.length()) > 16
                    || !name.codePoints().allMatch(java.lang.Character::isLetter)) throw new Rejected(Failure.INVALID_NAME);
            if (appearance.gender() < 0 || appearance.gender() > 1 || appearance.skin() < 0 || appearance.skin() > 4
                    || appearance.region() != 0 || !appearance.parts().keySet().equals(Set.of(1,2,3,4,5,6,7,8,9,11)))
                throw new IllegalArgumentException("Invalid character appearance");
            String key = name.toLowerCase(Locale.ROOT), server = selection.server().id();
            Character character = new Character(UUID.randomUUID().toString(), name, appearance, System.currentTimeMillis());
            db().transaction(() -> {
                if (Integer.parseInt(db().query("SELECT COUNT(*) FROM characters WHERE account=?", selection.session().username()).get(0)[0]) >= 3)
                    throw new Rejected(Failure.ACCOUNT_FULL);
                if (!db().query("SELECT id FROM characters WHERE server=? AND name_key=?", server, key).isEmpty())
                    throw new Rejected(Failure.NAME_TAKEN);
                db().execute("INSERT INTO characters VALUES (?,?,?,?,?,?,?,?,?)", character.id(), selection.session().username(), server, name, key,
                        appearance.gender(), appearance.skin(), appearance.region(), character.createdAt());
                appearance.parts().forEach((slot, part) -> db().execute("INSERT INTO character_parts VALUES (?,?,?,?)", character.id(), slot, part.style(), part.color()));
            });
            return character;
        }, worker);
    }
    @Override public CompletionStage<Void> delete(Selection selection, String characterId) {
        return CompletableFuture.runAsync(() -> {
            identity(selection);
            db().transaction(() -> {
                String[] values = {characterId, selection.session().username(), selection.server().id()};
                if (db().query("SELECT id FROM characters WHERE id=? AND account=? AND server=?", values).isEmpty())
                    throw new Rejected(Failure.NOT_FOUND);
                db().execute("DELETE FROM characters WHERE id=? AND account=? AND server=?", (Object[])values);
            });
        }, worker);
    }
    @Override public CompletionStage<Entry> enter(Selection selection, String characterId) {
        return characters(selection).thenApplyAsync(values -> {
            Character character=values.stream().filter(c -> c.id().equals(characterId)).findFirst()
                    .orElseThrow(() -> new Rejected(Failure.NOT_FOUND));
            var rows=db().query("SELECT bank,map,x,y,direction FROM character_positions WHERE character_id=?",characterId);
            Position position=Position.kantoStart();
            if(!rows.isEmpty()) {
                String[] row=rows.get(0);
                position=new Position(Integer.parseInt(row[0]),Integer.parseInt(row[1]),Integer.parseInt(row[2]),
                        Integer.parseInt(row[3]),Integer.parseInt(row[4]));
            }
            return new Entry(character,position);
        },worker);
    }
    @Override public CompletionStage<Void> savePosition(Selection selection,String characterId,Position position) {
        Objects.requireNonNull(position);
        return CompletableFuture.runAsync(() -> {
            identity(selection);
            db().transaction(() -> {
                if(db().query("SELECT id FROM characters WHERE id=? AND account=? AND server=?",characterId,
                        selection.session().username(),selection.server().id()).isEmpty()) throw new Rejected(Failure.NOT_FOUND);
                db().execute("INSERT OR REPLACE INTO character_positions VALUES (?,?,?,?,?,?)",characterId,
                        position.bank(),position.map(),position.x(),position.y(),position.direction());
            });
        },worker);
    }
    @Override public void close() {
        if (!closed.compareAndSet(false, true)) return;
        worker.execute(() -> { if (database != null) database.close(); });
        worker.shutdown();
    }
}
