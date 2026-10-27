package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Reference bgEvent bindings, with only verified native message scripts admitted. */
final class BackgroundDialogues {
    private static final Map<String, JsonObject> PROGRAMS = load();

    static JsonObject find(int bank, int map, int background) {
        return PROGRAMS.get(bank + ":" + map + ":" + background);
    }

    private static Map<String, JsonObject> load() {
        try (var input = BackgroundDialogues.class.getResourceAsStream("/story/kanto/background-dialogues.json")) {
            if (input == null) throw new FileNotFoundException("background-dialogues.json");
            var root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            var result = new HashMap<String, JsonObject>();
            for (var value : root.getAsJsonArray("entries")) {
                var row = value.getAsJsonObject();
                int bank = row.get("bank").getAsInt(), map = row.get("map").getAsInt(), id = row.get("background").getAsInt();
                if (!row.get("script").getAsString().equals(MapEntities.backgroundScript(bank, map, id)))
                    throw new IOException("Background script binding mismatch");
                var program = new JsonObject();
                program.add("actors", new JsonObject());
                var text = new JsonObject();
                var actions = new JsonArray();
                int index = 0;
                for (var operation : row.getAsJsonArray("operations")) {
                    var action = operation.getAsJsonObject().deepCopy();
                    switch (action.get("type").getAsString()) {
                        case "DIALOGUE" -> {
                            String key = "line" + index++;
                            text.add(key, action.get("text"));
                            action.addProperty("text", key);
                        }
                        case "SET_NATIVE_VAR" -> {
                            if (bank != 3 || map != 0 || id != 4
                                    || action.get("variable").getAsInt() != 0x4070
                                    || action.get("value").getAsInt() != 1)
                                throw new IOException("Unsupported background variable write");
                        }
                        case "SET_FAME_CHECKER_FLAG" -> {
                            if (bank != 3 || map != 0 || id != 0
                                    || action.get("person").getAsInt() != 0 || action.get("entry").getAsInt() != 0)
                                throw new IOException("Unsupported background fame checker operation");
                        }
                        default -> throw new IOException("Unsupported background operation");
                    }
                    actions.add(action);
                }
                var close = new JsonObject(); close.addProperty("type", "CLOSE_SCENE"); actions.add(close);
                var node = new JsonObject(); node.add("actions", actions);
                var nodes = new JsonObject(); nodes.add("talk", node);
                program.add("text", text); program.add("nodes", nodes);
                if (result.put(bank + ":" + map + ":" + id, program) != null)
                    throw new IOException("Duplicate background binding");
            }
            return Map.copyOf(result);
        } catch (IOException error) { throw new UncheckedIOException(error); }
    }
}
