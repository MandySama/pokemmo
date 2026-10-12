package top.totodile.pokemmo.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;

/** 两个服务共用显式配置；在事务锁中检查并创建缺失的业务表。 */
public final class Database {
    private final String url;
    private final Properties credentials = new Properties();
    public Database() throws IOException {
        Properties config = new Properties();
        Path path = Path.of(System.getProperty("pokemmo.database.config", "config/database.properties"));
        try (var reader = Files.newBufferedReader(path)) { config.load(reader); }
        url = required(config, "url", "POKEMMO_DB_URL");
        if (!url.startsWith("jdbc:postgresql:")) throw new IOException("PostgreSQL JDBC URL required");
        credentials.setProperty("user", required(config, "user", "POKEMMO_DB_USER"));
        credentials.setProperty("password", required(config, "password", "POKEMMO_DB_PASSWORD"));
        credentials.setProperty("connectTimeout", "10");
        credentials.setProperty("socketTimeout", "30");
        credentials.setProperty("ApplicationName", "pokemmo");
    }
    private static String required(Properties config, String key, String environment) throws IOException {
        String value = System.getenv(environment);
        if (value == null) value = config.getProperty(key);
        if (value == null || value.isBlank()) throw new IOException("Missing database configuration: " + key);
        return value;
    }
    public void createNew() throws SQLException, IOException {
        java.net.URI address;
        try { address = java.net.URI.create(url.substring("jdbc:".length())); }
        catch (IllegalArgumentException exception) { throw new IOException("Invalid database URL", exception); }
        String name = address.getPath().substring(1);
        if (!name.matches("[a-z][a-z0-9_]{0,62}") || name.equals("postgres") || name.startsWith("template")) {
            throw new IOException("A dedicated database name is required");
        }
        String maintenance = "jdbc:postgresql://" + address.getRawAuthority() + "/postgres"
                + (address.getRawQuery() == null ? "" : "?" + address.getRawQuery());
        try (Connection connection = DriverManager.getConnection(maintenance, credentials)) {
            try (PreparedStatement exists = connection.prepareStatement("SELECT 1 FROM pg_database WHERE datname=?")) {
                exists.setString(1, name);
                try (ResultSet row = exists.executeQuery()) {
                    if (row.next()) throw new SQLException("Database already exists; refusing to reuse it");
                }
            }
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE DATABASE \"" + name + "\" TEMPLATE template0 ENCODING 'UTF8'");
            }
        }
        initialize();
    }

    public Connection connect() throws SQLException { return DriverManager.getConnection(url, credentials); }
    public void initialize() throws SQLException, IOException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute("SELECT pg_advisory_xact_lock(706963, 1)");
                try (var stream = Database.class.getResourceAsStream("/db/schema.sql")) {
                    if (stream == null) throw new IOException("Missing database schema");
                    String sql = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    for (String command : sql.split(";")) {
                        if (!command.isBlank()) statement.execute(command);
                    }
                }
                connection.commit();
            } catch (SQLException | IOException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}
