package top.totodile.pokemmo.server.game;

import java.util.List;

/** 平台仅提供 SQLite 执行接口，角色 SQL 和事务由 game 模块管理。 */
public interface GameDatabase extends AutoCloseable {
    void execute(String sql, Object... values);
    List<String[]> query(String sql, String... values);
    void transaction(Runnable operation);
    @Override void close();
}
