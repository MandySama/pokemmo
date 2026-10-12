package top.totodile.pokemmo.login;

import top.totodile.pokemmo.persistence.AccountStore;
import java.util.Optional;

/** 保存凭据绑定数据库中的加盐校验值，服务重启后仍可验证。 */
public final class PostgresAccountRepository implements AccountRepository {
    private final AccountStore store;
    public PostgresAccountRepository(AccountStore store) { this.store = store; }
    @Override public Optional<byte[]> passwordDigest(String username) { return store.find(username).map(AccountStore.Account::hash); }
    @Override public boolean verifyPassword(String username, byte[] supplied) { return store.verify(username, supplied); }
    @Override public int accountId(String username) { return store.find(username).orElseThrow().id(); }
}
