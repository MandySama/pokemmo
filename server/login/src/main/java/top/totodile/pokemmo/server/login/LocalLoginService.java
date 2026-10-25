package top.totodile.pokemmo.server.login;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/** 单机入口：非空账号作为本地身份，不执行联网认证，也不保留密码。 */
public final class LocalLoginService implements LoginService {
    private static final List<Server> SERVERS = List.of(new Server("local", "Live"));
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    @Override
    public CompletionStage<Session> login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty())
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Invalid credentials"));
        Session session = new Session(UUID.randomUUID().toString(), username, SERVERS);
        sessions.put(session.id(), session);
        return CompletableFuture.completedFuture(session);
    }

    @Override
    public CompletionStage<Selection> selectServer(Session session, String serverId) {
        if (session == null || !session.equals(sessions.get(session.id())))
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid session"));
        return session.servers().stream()
                .filter(server -> server.id().equals(serverId))
                .findFirst()
                .<CompletionStage<Selection>>map(
                        server -> CompletableFuture.completedFuture(new Selection(session, server)))
                .orElseGet(
                        () ->
                                CompletableFuture.failedFuture(
                                        new IllegalArgumentException("Invalid server ID")));
    }

    @Override
    public CompletionStage<Void> logout(Session session) {
        if (session != null) sessions.remove(session.id(), session);
        return CompletableFuture.completedFuture(null);
    }
}
