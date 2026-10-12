package top.totodile.pokemmo.login;

import java.util.Optional;

/** 账号查询边界；返回密码摘要，不依赖具体存储方式。 */
public interface AccountRepository {
    Optional<byte[]> passwordDigest(String username);
}
