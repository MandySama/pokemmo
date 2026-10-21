package top.totodile.pokemmo.server.login;

import java.util.Objects;

/** 登录服务入口；当前客户端统一使用单机分支。 */
public final class LoginServices {
    public enum Mode { SINGLE_PLAYER, ONLINE }

    private LoginServices() {}

    public static LoginService create() {
        return create(Mode.SINGLE_PLAYER);
    }

    public static LoginService create(Mode mode) {
        return switch (Objects.requireNonNull(mode)) {
            case SINGLE_PLAYER -> new LocalLoginService();
            case ONLINE -> throw new UnsupportedOperationException("Online login is not implemented");
        };
    }
}
