package top.totodile.pokemmo.persistence;

import top.totodile.pokemmo.protocol.LoginRequest;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.sql.*;
import java.util.*;

/** 对线上密码摘要再次加盐派生；数据库不保存明文密码或可直接登录的线上摘要。 */
public final class AccountStore {
    private static final int ITERATIONS = 210000;
    private final Database database;
    public record Account(int id, byte[] salt, byte[] hash, int iterations) { }
    public AccountStore(Database database) { this.database = database; }
    public Optional<Account> find(String username) {
        try (Connection connection = database.connect();
             PreparedStatement query = connection.prepareStatement("SELECT id,password_salt,password_hash,password_iterations FROM accounts WHERE username=?")) {
            query.setString(1, username);
            try (ResultSet row = query.executeQuery()) {
                return row.next() ? Optional.of(new Account(row.getInt(1), row.getBytes(2), row.getBytes(3), row.getInt(4))) : Optional.empty();
            }
        } catch (SQLException exception) { throw new IllegalStateException("Unable to read account", exception); }
    }
    public boolean verify(String username, byte[] wireDigest) {
        Optional<Account> account = find(username);
        return account.isPresent() && MessageDigest.isEqual(account.get().hash(),
                derive(wireDigest, account.get().salt(), account.get().iterations()));
    }
    public boolean create(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || username.length() > 64 || username.indexOf('\0') >= 0
                || password == null || password.isEmpty()) throw new IllegalArgumentException("Invalid account");
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derive(HexFormat.of().parseHex(LoginRequest.digest(password)), salt, ITERATIONS);
        try (Connection connection = database.connect(); PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO accounts(username,password_salt,password_hash,password_iterations) VALUES (?,?,?,?) ON CONFLICT(username) DO NOTHING")) {
            insert.setString(1, username); insert.setBytes(2, salt); insert.setBytes(3, hash); insert.setInt(4, ITERATIONS);
            return insert.executeUpdate() == 1;
        }
    }
    private static byte[] derive(byte[] digest, byte[] salt, int iterations) {
        char[] chars = HexFormat.of().formatHex(digest).toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (GeneralSecurityException exception) { throw new IllegalStateException(exception); }
        finally { spec.clearPassword(); Arrays.fill(chars, '\0'); }
    }
}
