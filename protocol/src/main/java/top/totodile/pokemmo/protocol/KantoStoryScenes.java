package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 关都剧情目录绑定 ROM 入口与有序的剧情边界。 */
public final class KantoStoryScenes {
    public record Entry(
            String scene,
            int script,
            int npc,
            int variable,
            int expected,
            int rank,
            boolean primary) {}

    private static final Map<String, Entry> ENTRIES = load();

    private KantoStoryScenes() {}

    private static Map<String, Entry> load() {
        var result = new LinkedHashMap<String, Entry>();
        try (var input =
                KantoStoryScenes.class.getResourceAsStream("/maps/kanto-story-entries.csv")) {
            if (input == null) throw new IOException("Missing Kanto story entries");
            for (var line : new String(input.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (line.isBlank() || line.startsWith("#")) continue;
                var f = line.split(",");
                var entry =
                        new Entry(
                                f[0],
                                Integer.parseInt(f[1]),
                                Integer.parseInt(f[2]),
                                Integer.parseInt(f[3]),
                                Integer.parseInt(f[4]),
                                Integer.parseInt(f[5]),
                                f[6].equals("1"));
                if (StoryProgressPackets.scene(entry.scene()) == null
                        || result.put(entry.scene(), entry) != null)
                    throw new IOException("Invalid Kanto story entry");
            }
            if (result.keySet().stream().map(KantoStoryScenes::marker).distinct().count()
                    != result.size()) throw new IOException("Colliding Kanto scene markers");
            return Collections.unmodifiableMap(result);
        } catch (IOException error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    public record Followup(
            String after, int script, int group, int map, int x, int y, int npc, int facing) {}

    private static final List<Followup> FOLLOWUPS = loadFollowups();

    private static List<Followup> loadFollowups() {
        var result = new ArrayList<Followup>();
        try (var input =
                KantoStoryScenes.class.getResourceAsStream("/maps/kanto-story-followups.csv")) {
            if (input == null) throw new IOException("Missing Kanto story followups");
            for (String line :
                    new String(input.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (line.isBlank() || line.startsWith("#")) continue;
                var f = line.split(",");
                result.add(
                        new Followup(
                                f[0],
                                Integer.parseInt(f[1]),
                                Integer.parseInt(f[2]),
                                Integer.parseInt(f[3]),
                                Integer.parseInt(f[4]),
                                Integer.parseInt(f[5]),
                                Integer.parseInt(f[6]),
                                Integer.parseInt(f[7])));
            }
            return List.copyOf(result);
        } catch (IOException error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    public static List<Followup> prerequisites(String scene) {
        return FOLLOWUPS.stream()
                .filter(
                        extra ->
                                extra.after().startsWith("before_")
                                        && scene.endsWith("/" + extra.after().substring(7)))
                .toList();
    }

    public static List<Followup> followups(String scene) {
        return FOLLOWUPS.stream().filter(extra -> scene.endsWith("/" + extra.after())).toList();
    }

    public static List<Entry> entries() {
        return List.copyOf(ENTRIES.values());
    }

    public static int marker(String scene) {
        return 0x10000000 | (scene.hashCode() & 0x0fffffff);
    }

    public static Entry markedEntry(int marker) {
        return ENTRIES.values().stream()
                .filter(entry -> marker(entry.scene()) == marker)
                .findFirst()
                .orElseThrow();
    }

    public static int ordinal(String scene) {
        return entries().stream().map(Entry::scene).toList().indexOf(scene);
    }

    public static boolean supports(int group, int map) {
        return ENTRIES.keySet().stream()
                        .map(StoryProgressPackets::scene)
                        .anyMatch(scene -> scene.group() == group && scene.map() == map)
                || FOLLOWUPS.stream()
                        .anyMatch(extra -> extra.group() == group && extra.map() == map);
    }

    public static boolean frameScript(int script) {
        return ENTRIES.values().stream().anyMatch(entry -> entry.script() == script)
                || FOLLOWUPS.stream()
                        .anyMatch(
                                extra ->
                                        extra.after().startsWith("before_")
                                                && extra.script() == script);
    }

    public static boolean backgroundScript(int script) {
        return GbaScriptProgram.forRegion(0).backgroundScript(script);
    }

    public static boolean npcScript(int script) {
        return ENTRIES.values().stream()
                        .anyMatch(entry -> entry.npc() != 0 && entry.script() == script)
                || FOLLOWUPS.stream()
                        .anyMatch(extra -> extra.npc() != 0 && extra.script() == script);
    }

    public static Entry entry(String scene) {
        return ENTRIES.get(scene);
    }

    public static List<Entry> progression() {
        return ENTRIES.values().stream()
                .filter(Entry::primary)
                .sorted(Comparator.comparingInt(Entry::rank))
                .toList();
    }

    public static Set<String> before(String scene) {
        var target = entry(scene);
        if (target == null) return Set.of();
        var result = new HashSet<String>();
        for (var candidate : ENTRIES.values())
            if (candidate.rank() < target.rank()) result.add(candidate.scene());
        if (target.rank() >= 1) result.add(StoryProgressPackets.ESCORT);
        return Set.copyOf(result);
    }
}
