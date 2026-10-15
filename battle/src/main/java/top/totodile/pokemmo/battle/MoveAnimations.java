package top.totodile.pokemmo.battle;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 招式展示目录只包含编号、时间轴与效果参数，不依赖客户端或图形库。 */
public final class MoveAnimations {
    public record Effect(int archive, int controller, int target, long start, long duration, float adjustment) { }
    public record Command(String operation, long start, List<Float> arguments) { }
    public record Animation(int id, long duration, List<Effect> effects, List<Command> commands) { }
    private static final Map<Integer,String> RESOURCE_NAMES = new HashMap<>();
    private static final Map<Integer, Animation> ANIMATIONS = load();
    private MoveAnimations() { }
    public static String resource(int archive) { return RESOURCE_NAMES.getOrDefault(archive,(archive>=1000?"custom/":"auto/")+archive); }
    public static List<Integer> ids() { return ANIMATIONS.keySet().stream().sorted().toList(); }
    public static boolean supports(int id) { return ANIMATIONS.containsKey(id); }
    public static Animation get(int id) {
        var animation = ANIMATIONS.get(id);
        if (animation == null) throw new IllegalArgumentException("Unsupported move animation " + id);
        return animation;
    }
    private static Map<Integer, Animation> load() {
        var durations = new LinkedHashMap<Integer, Long>();
        var effects = new HashMap<Integer, List<Effect>>();
        var commands = new HashMap<Integer, List<Command>>();
        try (var input = MoveAnimations.class.getResourceAsStream("/battle/animations.csv")) {
            if (input == null) throw new IOException("Missing move animation directory");
            try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null;) {
                    String[] f = line.split(",");
                    if (f[0].equals("R")) {RESOURCE_NAMES.put(Integer.parseInt(f[1]),f[2]);continue;}
                    if (f[0].equals("M")) { durations.put(Integer.parseInt(f[1]), Long.parseLong(f[2])); continue; }
                    int id = Integer.parseInt(f[0]);
                    if (f[1].equals("E")) effects.computeIfAbsent(id, ignored -> new ArrayList<>()).add(new Effect(Integer.parseInt(f[2]),Integer.parseInt(f[3]),Integer.parseInt(f[4]),Long.parseLong(f[5]),Long.parseLong(f[6]),Float.parseFloat(f[7])));
                    else {
                        List<Float> args = new ArrayList<>(); for (int i=4;i<f.length;i++) args.add(Float.parseFloat(f[i]));
                        commands.computeIfAbsent(id, ignored -> new ArrayList<>()).add(new Command(f[2],Long.parseLong(f[3]),List.copyOf(args)));
                    }
                }
            }
        } catch (IOException | RuntimeException error) { throw new ExceptionInInitializerError(error); }
        var result = new LinkedHashMap<Integer, Animation>();
        durations.forEach((id, duration) -> result.put(id,new Animation(id,duration,List.copyOf(effects.getOrDefault(id,List.of())),List.copyOf(commands.getOrDefault(id,List.of())))));
        return Collections.unmodifiableMap(result);
    }
}
