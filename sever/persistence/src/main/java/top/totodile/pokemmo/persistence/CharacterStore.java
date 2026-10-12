package top.totodile.pokemmo.persistence;

import top.totodile.pokemmo.protocol.CharacterPackets;
import top.totodile.pokemmo.protocol.CharacterPackets.*;
import java.sql.*;
import java.util.*;

/** 锁定账号行串行化同账号创建；角色名唯一性由数据库约束处理。 */
public final class CharacterStore {
    private final Database database;
    private final AvatarCatalog catalog;
    public CharacterStore(Database database, AvatarCatalog catalog) { this.database = database; this.catalog = catalog; }
    public boolean ownsAccount(int id, String username) throws SQLException {
        try (Connection connection = database.connect(); PreparedStatement query = connection.prepareStatement("SELECT 1 FROM accounts WHERE id=? AND username=?")) {
            query.setInt(1, id); query.setString(2, username);
            try (ResultSet row = query.executeQuery()) { return row.next(); }
        }
    }
    public List<CharacterInfo> list(int account) throws SQLException {
        try (Connection connection = database.connect()) { return list(connection, account); }
    }
    private List<CharacterInfo> list(Connection connection, int account) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT id,name,gender,skin,region,items,colors FROM characters WHERE account_id=? ORDER BY id")) {
            query.setInt(1, account);
            List<CharacterInfo> result = new ArrayList<>();
            try (ResultSet rows = query.executeQuery()) {
                while (rows.next()) result.add(new CharacterInfo(rows.getLong(1), new Appearance(rows.getString(2),
                        rows.getInt(3), rows.getInt(4), rows.getInt(5), integers(rows.getArray(6)), integers(rows.getArray(7)))));
            }
            return List.copyOf(result);
        }
    }
    public CreationResult create(int account, Appearance appearance) throws SQLException {
        if (!catalog.valid(appearance)) return new CreationResult(Status.INVALID, List.of());
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement lock = connection.prepareStatement("SELECT id FROM accounts WHERE id=? FOR UPDATE")) {
                    lock.setInt(1, account);
                    try (ResultSet row = lock.executeQuery()) {
                        if (!row.next()) { connection.rollback(); return new CreationResult(Status.SYSTEM_ERROR, List.of()); }
                    }
                }
                if (list(connection, account).size() >= CharacterPackets.LIMIT) {
                    connection.rollback(); return new CreationResult(Status.LIMIT_REACHED, List.of());
                }
                try (PreparedStatement insert = connection.prepareStatement("INSERT INTO characters(account_id,name,gender,skin,region,items,colors) VALUES (?,?,?,?,?,?,?)")) {
                    insert.setInt(1, account); insert.setString(2, appearance.name()); insert.setInt(3, appearance.gender());
                    insert.setInt(4, appearance.skin()); insert.setInt(5, appearance.region());
                    Array items = connection.createArrayOf("smallint", boxed(appearance.items()));
                    Array colors = connection.createArrayOf("smallint", boxed(appearance.colors()));
                    try { insert.setArray(6, items); insert.setArray(7, colors); insert.executeUpdate(); }
                    finally { items.free(); colors.free(); }
                }
                List<CharacterInfo> characters = list(connection, account);
                connection.commit();
                return new CreationResult(Status.SUCCESS, characters);
            } catch (SQLException exception) {
                connection.rollback();
                if ("23505".equals(exception.getSQLState())) return new CreationResult(Status.NAME_TAKEN, List.of());
                throw exception;
            }
        }
    }
    public CreationResult delete(int account, long id) throws SQLException {
        try (Connection connection = database.connect()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement lock = connection.prepareStatement("SELECT id FROM accounts WHERE id=? FOR UPDATE")) {
                    lock.setInt(1, account);
                    try (ResultSet row = lock.executeQuery()) {
                        if (!row.next()) { connection.rollback(); return new CreationResult(Status.INVALID, List.of()); }
                    }
                }
                try (PreparedStatement delete = connection.prepareStatement("DELETE FROM characters WHERE id=? AND account_id=?")) {
                    delete.setLong(1, id);
                    delete.setInt(2, account);
                    if (delete.executeUpdate() != 1) { connection.rollback(); return new CreationResult(Status.INVALID, List.of()); }
                }
                List<CharacterInfo> remaining = list(connection, account);
                connection.commit();
                return new CreationResult(Status.SUCCESS, remaining);
            } catch (SQLException exception) { connection.rollback(); throw exception; }
        }
    }
    private static Integer[] boxed(int[] values) { return Arrays.stream(values).boxed().toArray(Integer[]::new); }
    private static int[] integers(Array value) throws SQLException {
        try {
            Object[] entries = (Object[]) value.getArray();
            int[] result = new int[entries.length];
            for (int i = 0; i < entries.length; i++) result[i] = ((Number) entries[i]).intValue();
            return result;
        } finally { value.free(); }
    }
}
