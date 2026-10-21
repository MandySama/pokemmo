package top.totodile.pokemmo.client;

import java.sql.*;
import java.util.*;
import top.totodile.pokemmo.server.game.GameDatabase;

final class DesktopGameDatabase implements GameDatabase {
    private final Connection connection;
    DesktopGameDatabase(String file) {
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
            try { operation.run(); connection.commit(); }
            catch (RuntimeException error) { connection.rollback(); throw error; }
            finally { connection.setAutoCommit(true); }
        } catch (SQLException error) { throw new IllegalStateException(error); }
    }
    @Override public void close() {
        try { connection.close(); } catch (SQLException error) { throw new IllegalStateException(error); }
    }
}
