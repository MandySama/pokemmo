package top.totodile.pokemmo.db.desktop;

import java.sql.*;
import java.util.*;
import top.totodile.pokemmo.db.Database;

public final class JdbcDatabase implements Database {
    private final Connection connection;
    public JdbcDatabase(String file) {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Path.of(file).toAbsolutePath().getParent());
            connection = DriverManager.getConnection("jdbc:sqlite:" + file);
        } catch (Exception error) { throw new IllegalStateException(error); }
    }
    @Override public void execute(String sql, Object... values) {
        try (var statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            statement.executeUpdate();
        } catch (SQLException error) { throw new IllegalStateException(error); }
    }
    @Override public List<String[]> query(String sql, String... values) {
        try (var statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) statement.setString(i + 1, values[i]);
            try (var rows = statement.executeQuery()) {
                List<String[]> result = new ArrayList<>();
                while (rows.next()) {
                    String[] row = new String[rows.getMetaData().getColumnCount()];
                    for (int i = 0; i < row.length; i++) row[i] = rows.getString(i + 1);
                    result.add(row);
                }
                return result;
            }
        } catch (SQLException error) { throw new IllegalStateException(error); }
    }
    @Override public void transaction(Runnable operation) {
        try {
            connection.setAutoCommit(false);
            Throwable failure = null;
            boolean restoreAutoCommit = true;
            try { operation.run(); connection.commit(); }
            catch (SQLException | RuntimeException | Error error) {
                failure = error;
                try { connection.rollback(); }
                catch (SQLException rollbackError) {
                    error.addSuppressed(rollbackError);
                    // 回滚失败时关闭连接，不能恢复自动提交而提交未完成的写入。
                    restoreAutoCommit = false;
                    try { connection.close(); }
                    catch (SQLException closeError) { error.addSuppressed(closeError); }
                }
                throw error;
            } finally {
                if (restoreAutoCommit) {
                    try { connection.setAutoCommit(true); }
                    catch (SQLException resetError) {
                        if (failure != null) failure.addSuppressed(resetError);
                        else throw resetError;
                    }
                }
            }
        } catch (SQLException error) { throw new IllegalStateException(error); }
    }
    @Override public void close() {
        try { connection.close(); } catch (SQLException error) { throw new IllegalStateException(error); }
    }
}
