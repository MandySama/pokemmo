package top.totodile.pokemmo.chat;

import top.totodile.pokemmo.protocol.ChatHandshake;

public final class Main {
    public static void main(String[] args) throws Exception {
        String host = System.getProperty("pokemmo.chat.bind", "127.0.0.1");
        int port = Integer.getInteger("pokemmo.chat.port", ChatHandshake.DEFAULT_PORT);
        try (var server = new ChatServer(host, port)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { server.close(); } catch (Exception ignored) { }
            }));
            System.out.println("Chat listening on " + host + ":" + server.port());
            server.serve();
        }
    }
}
