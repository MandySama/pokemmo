package top.totodile.pokemmo.server.login;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/** 登录模块提供的登录、服务器选择和注销服务。 */
public interface LoginService {
    CompletionStage<Session> login(String username, String password);

    CompletionStage<Selection> selectServer(Session session, String serverId);

    CompletionStage<Void> logout(Session session);

    record Server(String id, String name) {
        public Server {
            Objects.requireNonNull(id);
            Objects.requireNonNull(name);
        }
    }

    record Session(String id, String username, List<Server> servers) {
        public Session {
            Objects.requireNonNull(id);
            Objects.requireNonNull(username);
            servers = List.copyOf(servers);
        }
    }

    /** 交给后续游戏入口的选择结果，不包含密码。 */
    record Selection(Session session, Server server) {}
}
