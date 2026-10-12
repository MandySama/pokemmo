package top.totodile.pokemmo.login;

import top.totodile.pokemmo.protocol.ServerKeys;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

/** 本地登录服务入口，使用开发握手密钥和启动时初始化的内存账号。 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        String username = environment("POKEMMO_LOGIN_USERNAME", "test");
        String password = environment("POKEMMO_LOGIN_PASSWORD", "test123456");
        String bind = System.getProperty("pokemmo.login.bind", "127.0.0.1");
        int port = Integer.getInteger("pokemmo.login.port", 2106);
        var accounts = new MemoryAccountRepository(Map.of(username, password));
        try (LoginServer server = new LoginServer(new InetSocketAddress(bind, port), accounts, ServerKeys.load(Main.class, "pokemmo.login.privateKey"))) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.close();
                } catch (IOException exception) {
                    System.err.println("Unable to close login listener: " + exception.getMessage());
                }
            }, "login-shutdown"));
            System.out.println("Login server listening on " + bind + ":" + server.port() + " (in-memory accounts)");
            server.serve();
        }
    }

    private static String environment(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }

}
