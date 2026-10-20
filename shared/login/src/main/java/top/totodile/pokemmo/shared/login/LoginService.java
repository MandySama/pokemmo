package top.totodile.pokemmo.shared.login;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/** 客户端与单机登录模块之间的服务契约。 */
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
