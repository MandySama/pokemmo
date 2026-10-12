package top.totodile.pokemmo.login;

import top.totodile.pokemmo.protocol.ServerKeys;

import java.io.IOException;
import java.net.InetSocketAddress;

/** 本地登录服务入口，使用开发握手密钥和 PostgreSQL 账号。 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        String username = environment("POKEMMO_LOGIN_USERNAME", "MandySa");
        String password = environment("POKEMMO_LOGIN_PASSWORD", "yousa98672");
        String bind = System.getProperty("pokemmo.login.bind", "127.0.0.1");
        int port = Integer.getInteger("pokemmo.login.port", 2106);
        var database = new top.totodile.pokemmo.persistence.Database();
        database.initialize();
        var store = new top.totodile.pokemmo.persistence.AccountStore(database);
        store.create(username, password);
        var accounts = new PostgresAccountRepository(store);
        try (LoginServer server = new LoginServer(new InetSocketAddress(bind, port), accounts, ServerKeys.load(Main.class, "pokemmo.login.privateKey"))) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.close();
                } catch (IOException exception) {
                    System.err.println("Unable to close login listener: " + exception.getMessage());
                }
            }, "login-shutdown"));
            System.out.println("Login server listening on " + bind + ":" + server.port() + " (PostgreSQL accounts)");
            server.serve();
        }
    }

    private static String environment(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }

}
