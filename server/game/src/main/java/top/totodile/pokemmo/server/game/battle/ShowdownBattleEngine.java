package top.totodile.pokemmo.server.game.battle;

import com.google.gson.*;

import top.totodile.pokemmo.server.game.EncounterData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** One local Node subprocess per battle, using the official BattleStream standard-IO API. */
public final class ShowdownBattleEngine implements BattleEngine {
    private static Path bridge() {
        String configured = System.getProperty("pokemmo.showdown.bridge", "");
        if (!configured.isBlank()) return Path.of(configured).toAbsolutePath();
        Path current = Path.of("").toAbsolutePath();
        for (Path p = current; p != null; p = p.getParent()) {
            var candidate = p.resolve("server/game/showdown/bridge.cjs");
            if (Files.isRegularFile(candidate)) return candidate;
        }
        throw new IllegalStateException(
                "Showdown bridge not found; set -Dpokemmo.showdown.bridge=/path/to/bridge.cjs and"
                        + " run npm ci in its directory");
    }

    @Override
    public Session start(EncounterData encounter) {
        return new ProcessSession(bridge(), encounter);
    }

    private static final class ProcessSession implements Session {
        private final Gson gson = new Gson();
        private final Process process;
        private final BufferedWriter writer;
        private final BlockingQueue<String> replies = new LinkedBlockingQueue<>();
        private BattleState state;
        private boolean closed;

        ProcessSession(Path bridge, EncounterData encounter) {
            try {
                var builder =
                        new ProcessBuilder(
                                System.getProperty("pokemmo.showdown.node", "node"),
                                bridge.toString());
                builder.directory(bridge.getParent().toFile());
                builder.redirectError(ProcessBuilder.Redirect.INHERIT);
                process = builder.start();
                writer =
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        process.getOutputStream(), StandardCharsets.UTF_8));
                var reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        process.getInputStream(), StandardCharsets.UTF_8));
                Thread thread =
                        new Thread(
                                () -> {
                                    try {
                                        for (String line; (line = reader.readLine()) != null; )
                                            replies.offer(line);
                                    } catch (IOException ignored) {
                                    } finally {
                                        replies.offer(
                                                gson.toJson(
                                                        Map.of(
                                                                "ok",
                                                                false,
                                                                "error",
                                                                "Showdown process exited")));
                                    }
                                },
                                "showdown-output");
                thread.setDaemon(true);
                thread.start();
                try {
                    state = request(Map.of("op", "start", "encounter", encounter));
                } catch (RuntimeException error) {
                    close();
                    throw error;
                }
            } catch (IOException error) {
                throw new IllegalStateException(
                        "Cannot launch Showdown: install Node.js and run npm ci in"
                                + " server/game/showdown",
                        error);
            }
        }

        private BattleState request(Object request) {
            if (closed) throw new IllegalStateException("Battle engine is closed");
            try {
                writer.write(gson.toJson(request));
                writer.newLine();
                writer.flush();
                String line = replies.poll(15, TimeUnit.SECONDS);
                if (line == null) {
                    close();
                    throw new IllegalStateException("Showdown did not respond within 15 seconds");
                }
                var response = JsonParser.parseString(line).getAsJsonObject();
                if (!response.get("ok").getAsBoolean())
                    throw new IllegalArgumentException(response.get("error").getAsString());
                return gson.fromJson(response.get("result"), BattleState.class);
            } catch (IOException error) {
                close();
                throw new IllegalStateException("Showdown communication failed", error);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                close();
                throw new IllegalStateException("Showdown interrupted", error);
            }
        }

        @Override
        public synchronized BattleState state() {
            return state;
        }

        @Override
        public synchronized BattleState choose(Action action) {
            state = request(Map.of("op", "choose", "action", action));
            return state;
        }

        @Override
        public synchronized void close() {
            if (closed) return;
            closed = true;
            try {
                writer.close();
            } catch (IOException ignored) {
            }
            process.destroy();
            try {
                if (!process.waitFor(1, TimeUnit.SECONDS)) process.destroyForcibly();
            } catch (InterruptedException error) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
    }
}
