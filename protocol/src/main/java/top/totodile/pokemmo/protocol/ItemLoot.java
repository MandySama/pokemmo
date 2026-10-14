package top.totodile.pokemmo.protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.random.RandomGenerator;

/** 容器及权重来自外部目录；实际抽取只由服务端执行。 */
public final class ItemLoot {
    public record Reward(int id, int minimum, int maximum, int weight) { }
    private static final Map<Integer, List<Reward>> POOLS = load();
    private ItemLoot() { }

    public static boolean usable(int id) { return POOLS.containsKey(id); }
    public static Set<Integer> containers() { return POOLS.keySet(); }
    public static List<Reward> rewards(int id) { return POOLS.getOrDefault(id, List.of()); }

    public static Map<Integer, Integer> roll(int id, RandomGenerator random) {
        var rewards = POOLS.get(id);
        if (rewards == null) throw new IllegalArgumentException("Unsupported container");
        int ticket = random.nextInt(rewards.stream().mapToInt(Reward::weight).sum());
        for (var reward : rewards) {
            ticket -= reward.weight();
            if (ticket < 0) return Map.of(reward.id(), random.nextInt(reward.minimum(), reward.maximum() + 1));
        }
        throw new IllegalStateException("Empty reward pool");
    }

    private static Map<Integer, List<Reward>> load() {
        Map<Integer, List<Reward>> pools = new TreeMap<>();
        try (var stream = ItemLoot.class.getResourceAsStream("/catalog/item-loot.csv")) {
            if (stream == null) throw new IOException("Missing item loot");
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null;) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String[] fields = line.split(",");
                    if (fields.length != 5) throw new IOException("Invalid loot columns");
                    int container = Integer.parseInt(fields[0]);
                    var reward = new Reward(Integer.parseInt(fields[1]), Integer.parseInt(fields[2]),
                            Integer.parseInt(fields[3]), Integer.parseInt(fields[4]));
                    var item = ItemCatalog.get(reward.id());
                    if (ItemCatalog.get(container) == null || item == null || item.pocket() == 3
                            || reward.id() == container || reward.minimum() < 1 || reward.maximum() < reward.minimum()
                            || reward.maximum() > item.maxStack() || reward.weight() < 1 || reward.weight() > 10000)
                        throw new IOException("Invalid loot reward");
                    pools.computeIfAbsent(container, key -> new ArrayList<>()).add(reward);
                }
            }
            Map<Integer, List<Reward>> result = new TreeMap<>();
            for (var entry : pools.entrySet()) result.put(entry.getKey(), List.copyOf(entry.getValue()));
            return Map.copyOf(result);
        } catch (IOException | RuntimeException error) { throw new ExceptionInInitializerError(error); }
    }
}
