package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 关都剧情目录绑定 ROM 入口与有序的剧情边界。 */
public final class KantoStoryScenes {
    public record Entry(String scene, int script, int npc, int variable, int expected, int rank, boolean primary) { }
    private static final Map<String, Entry> ENTRIES = load();
    private KantoStoryScenes() { }
    private static Map<String, Entry> load() {
        var result = new LinkedHashMap<String, Entry>();
        try (var input = KantoStoryScenes.class.getResourceAsStream("/maps/kanto-story-entries.csv")) {
            if (input == null) throw new IOException("Missing Kanto story entries");
            for (var line : new String(input.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (line.isBlank() || line.startsWith("#")) continue;
                var f = line.split(",");
                var entry = new Entry(f[0], Integer.parseInt(f[1]), Integer.parseInt(f[2]), Integer.parseInt(f[3]), Integer.parseInt(f[4]), Integer.parseInt(f[5]), f[6].equals("1"));
                if (StoryProgressPackets.scene(entry.scene()) == null || result.put(entry.scene(), entry) != null)
                    throw new IOException("Invalid Kanto story entry");
            }
            return Collections.unmodifiableMap(result);
        } catch (IOException error) { throw new ExceptionInInitializerError(error); }
    }
    public static Entry entry(String scene) { return ENTRIES.get(scene); }
    public static List<Entry> progression() {
        return ENTRIES.values().stream().filter(Entry::primary).sorted(Comparator.comparingInt(Entry::rank)).toList();
    }
    public static Set<String> before(String scene) {
        var target = entry(scene);
        if (target == null) return Set.of();
        var result = new HashSet<String>();
        for (var candidate : ENTRIES.values()) if (candidate.rank() < target.rank()) result.add(candidate.scene());
        return Set.copyOf(result);
    }
}
