package top.totodile.pokemmo.server.chat;

import java.util.*;
import java.util.function.Consumer;

/** Embedded equivalent of ChatBroadcastListener: one broadcast pool per server. */
public final class LocalChatService {
    public record Message(ChatChannel channel, String sender, String text) {}

    private static final Map<String, Set<Session>> POOLS = new HashMap<>();

    private LocalChatService() {}

    public static Session connect(
            String server, String character, String name, Consumer<Message> receive) {
        var session = new Session(server, character, name, receive);
        synchronized (POOLS) {
            var pool = POOLS.computeIfAbsent(server, key -> new LinkedHashSet<>());
            pool.removeIf(
                    old -> {
                        if (old.character.equals(character)) {
                            old.closed = true;
                            return true;
                        }
                        return false;
                    });
            pool.add(session);
        }
        return session;
    }

    public static final class Session implements AutoCloseable {
        private final String server, character, name;
        private final Consumer<Message> receive;
        private volatile boolean closed;

        private Session(String server, String character, String name, Consumer<Message> receive) {
            this.server = Objects.requireNonNull(server);
            this.character = Objects.requireNonNull(character);
            this.name = Objects.requireNonNull(name);
            this.receive = Objects.requireNonNull(receive);
        }

        public boolean send(ChatChannel channel, String text) {
            if (channel == null
                    || !channel.broadcastInput()
                    || text == null
                    || text.isBlank()
                    || text.length() > 150) return false;
            List<Session> targets;
            synchronized (POOLS) {
                if (closed) return false;
                targets = List.copyOf(POOLS.getOrDefault(server, Set.of()));
            }
            var message = new Message(channel, name, text.strip());
            for (var target : targets)
                if (!target.closed)
                    try {
                        target.receive.accept(message);
                    } catch (RuntimeException error) {
                        System.getLogger(LocalChatService.class.getName())
                                .log(
                                        System.Logger.Level.WARNING,
                                        "Cannot deliver chat message",
                                        error);
                    }
            return true;
        }

        @Override
        public void close() {
            synchronized (POOLS) {
                closed = true;
                var pool = POOLS.get(server);
                if (pool != null) {
                    pool.remove(this);
                    if (pool.isEmpty()) POOLS.remove(server);
                }
            }
        }
    }
}
