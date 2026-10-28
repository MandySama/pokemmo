package top.totodile.pokemmo.server.game.battle;

import com.google.gson.*;

import top.totodile.pokemmo.server.game.EncounterData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** One local Node subprocess per battle, using the official BattleStream standard-IO API. */
public final class ShowdownBattleEngine implements BattleEngine,AutoCloseable {
    private ProcessSession prepared;
    private boolean closed;
    public synchronized void prepare(){if(!closed&&prepared==null)prepared=new ProcessSession(bridge());}
    @Override public synchronized void close(){closed=true;if(prepared!=null){prepared.close();prepared=null;}}
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
    public synchronized Session start(EncounterData encounter) {
        if(closed)throw new IllegalStateException("Battle engine is closed");
        var session=prepared;prepared=null;
        if(session==null)session=new ProcessSession(bridge());
        session.begin(encounter,false,null);return session;
    }

    public Session startMoveTest(EncounterData encounter) {
        return new ProcessSession(bridge(), encounter, true);
    }

    public static Session restore(String checkpoint){
        var data=JsonParser.parseString(checkpoint).getAsJsonObject();var gson=new Gson();
        var start=data.getAsJsonObject("start");
        var session=new ProcessSession(bridge(),gson.fromJson(start.get("encounter"),EncounterData.class),false,start);
        try {
            for(var command:data.getAsJsonArray("commands")){
                var c=command.getAsJsonObject();var next=session.request(c);
                if(c.get("op").getAsString().equals("choose"))session.state=next;
                session.commands.add(c.deepCopy());
            }
            session.state=session.request(Map.of("op","state"));
            return session;
        }catch(RuntimeException error){session.close();throw error;}
    }

    private static final class ProcessSession implements Session {
        private final Gson gson = new Gson();
        private final Process process;
        private final BufferedWriter writer;
        private final BlockingQueue<String> replies = new LinkedBlockingQueue<>();
        private BattleState state;
        private boolean closed;
        private JsonObject start;
        private final JsonArray commands=new JsonArray();

        ProcessSession(Path bridge, EncounterData encounter, boolean moveTest) {
            this(bridge,encounter,moveTest,null);
        }
        ProcessSession(Path bridge, EncounterData encounter, boolean moveTest,JsonObject savedStart) {
            this(bridge);begin(encounter,moveTest,savedStart);
        }
        ProcessSession(Path bridge) {
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
                    request(Map.of("op","warm"));
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
        void begin(EncounterData encounter,boolean moveTest,JsonObject savedStart){
            try{
                start=savedStart==null?gson.toJsonTree(Map.of("op","start","encounter",encounter,
                        "seed",java.util.stream.IntStream.range(0,4).map(i->ThreadLocalRandom.current().nextInt(65536)).boxed().toList(),
                        "testEnemyHp",moveTest?999999:0)).getAsJsonObject():savedStart.deepCopy();state=request(start);
            }catch(RuntimeException error){close();throw error;}
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

        @Override public synchronized void growth(java.util.List<top.totodile.pokemmo.server.game.PokemonData.Pokemon> pokemon) {
            if(!state.ended()) {var c=gson.toJsonTree(Map.of("op","growth","pokemon",pokemon));state=request(c);commands.add(c);}
        }

        @Override public synchronized String checkpoint(){var data=new JsonObject();data.add("start",start);data.add("commands",commands);return gson.toJson(data);}

        @Override
        public synchronized BattleState choose(Action action) {
            var c=gson.toJsonTree(Map.of("op","choose","action",action));state=request(c);commands.add(c);
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
