package top.totodile.pokemmo.game;

import top.totodile.pokemmo.protocol.HandshakeKeys;
import top.totodile.pokemmo.protocol.ServerKeys;

import java.io.IOException;
import java.net.InetSocketAddress;

/** 独立启动游戏服务，所有会话和凭证消费记录仅保存在内存中。 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        String bind = System.getProperty("pokemmo.game.bind", "127.0.0.1");
        int port = Integer.getInteger("pokemmo.game.port", 7777);
        int id = Integer.getInteger("pokemmo.game.id", 1);
        int capacity = Integer.getInteger("pokemmo.game.capacity", 100);
        try (GameServer server = new GameServer(new InetSocketAddress(bind, port), id, capacity,
                ServerKeys.load(Main.class, "pokemmo.game.privateKey"), HandshakeKeys.rootPublicKey())) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.close();
                } catch (IOException exception) {
                    System.err.println("Unable to close game listener: " + exception.getMessage());
                }
            }, "game-shutdown"));
            System.out.println("Game server " + id + " listening on " + bind + ":" + server.port());
            server.serve();
        }
    }
}
