package top.totodile.pokemmo.server.game;

import top.totodile.pokemmo.db.Database;
import java.util.List;

/** Local debug session: an uncommitted SQLite savepoint includes every gameplay write. */
final class StoryTestDatabase implements Database {
    private final Database delegate;
    private String owner;
    private long sequence;
    StoryTestDatabase(Database delegate) { this.delegate = delegate; }
    boolean active() { return owner != null; }
    String owner() { return owner; }
    void begin(String id) {
        if (active()) {
            if (!owner.equals(id)) throw new IllegalStateException("请先结束当前角色的临时测试");
            return;
        }
        delegate.execute("SAVEPOINT story_test_session");
        owner = id;
    }
    void restore() {
        if (!active()) return;
        delegate.execute("ROLLBACK TO SAVEPOINT story_test_session");
        delegate.execute("RELEASE SAVEPOINT story_test_session");
        owner = null;
    }
    public void execute(String sql,Object... values) { delegate.execute(sql, values); }
    public List<String[]> query(String sql,String... values) { return delegate.query(sql, values); }
    public void transaction(Runnable operation) {
        if (!active()) { delegate.transaction(operation); return; }
        String name = "story_test_action_" + ++sequence;
        delegate.execute("SAVEPOINT " + name);
        try { operation.run(); }
        catch (RuntimeException | Error e) {
            try { delegate.execute("ROLLBACK TO SAVEPOINT " + name); }
            catch (RuntimeException rollback) { e.addSuppressed(rollback); }
            throw e;
        } finally { delegate.execute("RELEASE SAVEPOINT " + name); }
    }
    public void close() { try { restore(); } finally { delegate.close(); } }
}
