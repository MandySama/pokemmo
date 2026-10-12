package top.totodile.pokemmo.login;

import top.totodile.pokemmo.protocol.LoginRequest;

import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;

/** 启动时初始化内存账号，仅保留摘要，进程退出后释放。 */
public final class MemoryAccountRepository implements AccountRepository {
    private final Map<String, byte[]> accounts;

    public MemoryAccountRepository(Map<String, String> passwords) {
        Map<String, byte[]> entries = new HashMap<>();
        passwords.forEach((name, password) -> {
            if (name.isBlank() || name.length() > 64 || name.indexOf('\0') >= 0 || password.isEmpty()) {
                throw new IllegalArgumentException("Invalid initial account");
            }
            entries.put(name, HexFormat.of().parseHex(LoginRequest.digest(password)));
        });
        accounts = Map.copyOf(entries);
    }

    @Override
    public int accountId(String username) {
        int id = 1;
        for (String name : new java.util.TreeSet<>(accounts.keySet())) {
            if (name.equals(username)) return id;
            id++;
        }
        throw new IllegalArgumentException("Unknown account");
    }

    @Override
    public Optional<byte[]> passwordDigest(String username) {
        byte[] digest = accounts.get(username);
        return digest == null ? Optional.empty() : Optional.of(digest.clone());
    }
}
