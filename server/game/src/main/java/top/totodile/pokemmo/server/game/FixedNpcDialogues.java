package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Message-only native scripts, bound to the same map NPCs as MapFile.toNpc. */
final class FixedNpcDialogues {
    private static final Map<String, JsonObject> PROGRAMS = load();
    private static String key(int bank, int map, int npc) { return bank + ":" + map + ":" + npc; }

    static JsonObject find(int bank, int map, int npc) { return PROGRAMS.get(key(bank, map, npc)); }

    private static Map<String, JsonObject> load() {
        try (var input = FixedNpcDialogues.class.getResourceAsStream("/story/kanto/fixed-dialogues.json")) {
            if (input == null) throw new FileNotFoundException("fixed-dialogues.json");
            var root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            var programs = new HashMap<String, JsonObject>();
            for (var value : root.getAsJsonArray("entries")) {
                var row = value.getAsJsonObject();
                int bank = row.get("bank").getAsInt(), map = row.get("map").getAsInt(), npc = row.get("npc").getAsInt();
                if (!row.get("script").getAsString().equals(MapEntities.script(bank, map, npc)))
                    throw new IOException("Fixed NPC dialogue binding mismatch: " + key(bank, map, npc));
                var program = new JsonObject();
                program.add("actors", new JsonObject());
                var text = new JsonObject();
                var actions = new JsonArray();
                int index = 0;
                for (var offset : row.getAsJsonArray("texts")) {
                    String name = "line" + index++;
                    text.add(name, offset);
                    var action = new JsonObject();
                    action.addProperty("type", "DIALOGUE");
                    action.addProperty("text", name);
                    action.addProperty("facePlayer", row.get("facePlayer").getAsBoolean());
                    actions.add(action);
                }
                var close = new JsonObject(); close.addProperty("type", "CLOSE_SCENE"); actions.add(close);
                var node = new JsonObject(); node.add("actions", actions);
                var nodes = new JsonObject(); nodes.add("talk", node);
                program.add("text", text); program.add("nodes", nodes);
                if (programs.put(key(bank, map, npc), program) != null)
                    throw new IOException("Duplicate fixed NPC dialogue binding");
            }
            return Map.copyOf(programs);
        } catch (IOException error) { throw new UncheckedIOException(error); }
    }
}
