package top.totodile.pokemmo.client.ui.login;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import top.totodile.pokemmo.server.login.LoginService.*;
import top.totodile.pokemmo.server.login.LoginService;

/** 登录流程状态，不依赖 TWL、LibGDX 或具体服务实现。 */
public final class LoginController implements AutoCloseable {
    public enum State { LOGIN, LOGGING_IN, SERVERS, SELECTING, SELECTED }
    private final LoginService service;
    private final Consumer<Runnable> dispatch;
    private Runnable listener = () -> {};
    private long revision;
    private boolean closed;
    private State state = State.LOGIN;
    private Session session;
    private Selection selection;
    private boolean failed;

    public LoginController(LoginService service, Consumer<Runnable> dispatch) {
        this.service = Objects.requireNonNull(service);
        this.dispatch = Objects.requireNonNull(dispatch);
    }
    public State state() { return state; }
    public Session session() { return session; }
    public Selection selection() { return selection; }
    public boolean failed() { return failed; }
    public void listen(Runnable listener) { this.listener = listener; }

    public void login(String username, String password, Consumer<Session> authenticated) {
        if (closed || state != State.LOGIN) return;
        long request = ++revision;
        failed = false;
        state = State.LOGGING_IN;
        listener.run();
        CompletionStage<Session> operation;
        try { operation = service.login(username, password); }
        catch (RuntimeException error) { fail(State.LOGIN); return; }
        operation.whenComplete((result, error) -> dispatch.accept(() -> {
                if (closed || request != revision) {
                    if (result != null) service.logout(result);
                    return;
                }
                if (error != null || result == null) {
                    fail(State.LOGIN);
                    return;
                }
                session = result;
                state = State.SERVERS;
                try { authenticated.accept(result); }
                finally { listener.run(); }
            }));
    }

    public void select(String serverId) {
        if (closed || state != State.SERVERS) return;
        long request = ++revision;
        failed = false;
        state = State.SELECTING;
        listener.run();
        try {
            service.selectServer(session, serverId).whenComplete((result, error) -> dispatch.accept(() -> {
                if (closed || request != revision) return;
                if (error != null || result == null) { fail(State.SERVERS); return; }
                selection = result;
                state = State.SELECTED;
                listener.run();
            }));
        } catch (RuntimeException error) { fail(State.SERVERS); }
    }

    private void fail(State next) {
        failed = true;
        state = next;
        listener.run();
    }

    public void back() {
        if (closed) return;
        ++revision;
        if (session != null) service.logout(session);
        session = null;
        selection = null;
        failed = false;
        state = State.LOGIN;
        listener.run();
    }
    @Override public void close() {
        if (closed) return;
        back();
        closed = true;
        listener = () -> {};
    }
}
