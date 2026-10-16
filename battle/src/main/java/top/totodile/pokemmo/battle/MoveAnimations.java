package top.totodile.pokemmo.battle;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 招式目录保存执行时间轴后的节点起点、粒子参数与可选阶段。 */
public final class MoveAnimations {
    public record Effect(int archive, int controller, int target, long start, long duration, float adjustment,
                         int destination, int position, float travelDuration) {
        public Effect(int archive, int controller, int target, long start, long duration, float adjustment) {
            this(archive, controller, target, start, duration, adjustment, 8, -1, 0);
        }
        public Effect(int archive, int controller, int target, long start, long duration, float adjustment, int destination) {
            this(archive, controller, target, start, duration, adjustment, destination, -1, 0);
        }
    }
    public record Command(String operation, long start, List<Float> arguments) { }
    public record Animation(int id, long duration, List<Effect> effects, List<Command> commands) { }
    private static final Map<Integer, String> RESOURCE_NAMES = new HashMap<>();
    private static final Map<String, Animation> VARIANTS = new LinkedHashMap<>();
    private static final Map<Integer, Animation> ANIMATIONS = load();

    private MoveAnimations() { }
    public static String resource(int archive) { return RESOURCE_NAMES.getOrDefault(archive, (archive >= 1000 ? "custom/" : "auto/") + archive); }
    public static List<Integer> ids() { return ANIMATIONS.keySet().stream().sorted().toList(); }
    public static boolean supports(int id) { return ANIMATIONS.containsKey(id); }
    public static Animation get(int id) {
        var animation = ANIMATIONS.get(id);
        if (animation == null) throw new IllegalArgumentException("Unsupported move animation " + id);
        return animation;
    }
    public static Animation get(int id, String variant) { return VARIANTS.getOrDefault(id + ":" + variant, get(id)); }
    public static List<String> variants(int id) { return VARIANTS.keySet().stream().filter(key -> key.startsWith(id + ":")).map(key -> key.substring(key.indexOf(':') + 1)).toList(); }
    public static Animation playback(int id) { return ANIMATIONS.getOrDefault(id, new Animation(id, 0, List.of(), List.of())); }

    private static Map<Integer, Animation> load() {
        var durations = new LinkedHashMap<String, Long>();
        var effects = new HashMap<String, List<Effect>>();
        var commands = new HashMap<String, List<Command>>();
        for (String name : List.of("animations.csv", "animation-variants.csv")) {
            try (var input = MoveAnimations.class.getResourceAsStream("/battle/" + name)) {
                if (input == null) throw new IOException("Missing animation directory " + name);
                try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                    for (String line; (line = reader.readLine()) != null;) {
                        String[] fields = line.split(",");
                        if (fields[0].equals("R")) { RESOURCE_NAMES.put(Integer.parseInt(fields[1]), fields[2]); continue; }
                        if (fields[0].equals("M")) { durations.put(fields[1], Long.parseLong(fields[2])); continue; }
                        String key = fields[0];
                        if (fields[1].equals("E")) {
                            effects.computeIfAbsent(key, ignored -> new ArrayList<>()).add(new Effect(
                                    Integer.parseInt(fields[2]), Integer.parseInt(fields[3]), Integer.parseInt(fields[4]),
                                    Long.parseLong(fields[5]), Long.parseLong(fields[6]), Float.parseFloat(fields[7]),
                                    Integer.parseInt(fields[8]), Integer.parseInt(fields[9]), Float.parseFloat(fields[10])));
                        } else {
                            List<Float> args = new ArrayList<>();
                            for (int i = 4; i < fields.length; i++) args.add(Float.parseFloat(fields[i]));
                            commands.computeIfAbsent(key, ignored -> new ArrayList<>()).add(new Command(fields[2], Long.parseLong(fields[3]), List.copyOf(args)));
                        }
                    }
                }
            } catch (IOException | RuntimeException error) { throw new ExceptionInInitializerError(error); }
        }
        var result = new LinkedHashMap<Integer, Animation>();
        durations.forEach((key, duration) -> {
            int id = Integer.parseInt(key.split(":", 2)[0]);
            var animation = new Animation(id, duration, List.copyOf(effects.getOrDefault(key, List.of())), List.copyOf(commands.getOrDefault(key, List.of())));
            if (key.contains(":")) VARIANTS.put(key, animation); else result.put(id, animation);
        });
        return Collections.unmodifiableMap(result);
    }
}
