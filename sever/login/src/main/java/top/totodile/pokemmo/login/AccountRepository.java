package top.totodile.pokemmo.login;

import java.util.Optional;

/** 账号查询边界；提供密码校验及凭据绑定值，不依赖具体存储方式。 */
public interface AccountRepository {
    Optional<byte[]> passwordDigest(String username);

    default boolean verifyPassword(String username, byte[] supplied) {
        return passwordDigest(username).map(expected -> java.security.MessageDigest.isEqual(expected, supplied)).orElse(false);
    }

    int accountId(String username);
}
