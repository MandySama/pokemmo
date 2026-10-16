package top.totodile.pokemmo.protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.random.RandomGenerator;
import java.util.zip.GZIPInputStream;

/** 五地区的地图遭遇槽；服务端抽取与图鉴分布使用同一份目录。 */
public final class EncounterCatalog {
    public static final int LAND = 0, DARK_GRASS = 1, RUSTLING = 2, WATER = 3, RIPPLE = 4,
            FISHING = 5, RIPPLE_FISHING = 6, ROCK = 7, OLD_ROD = 8, GOOD_ROD = 9, SUPER_ROD = 10;
    public record Slot(int species, int min, int max, int weight) {
        public Slot {
            if (species < 1 || species > PokemonCatalog.MAX_SPECIES || min < 1 || max > 100 || min > max || weight < 1)
                throw new IllegalArgumentException("Invalid encounter slot");
        }
    }
    public record Table(int region, int group, int map, int name, int kind, int season, int time,
                        int rate, boolean cave, List<Slot> slots) {
        public Table {
            slots = List.copyOf(slots);
            if (region < 0 || region > 5 || region == 4 || group < 0 || group > 255 || map < 0 || map > 255
                    || name < 1 || kind < 0 || kind > 10 || season < -1 || season > 3 || time < -1 || time > 2
                    || rate < 1 || rate > 100 || slots.isEmpty() || slots.size() > 64)
                throw new IllegalArgumentException("Invalid encounter table");
        }
        /** 香气随机吸引三只或五只，每只独立抽取地图槽和闪光。 */
        public Selection rollHorde(RandomGenerator random) {
            int count = random.nextBoolean() ? 3 : 5;
            Selection first = roll(random);
            List<Selection> companions = new ArrayList<>();
            for (int i = 1; i < count; i++) companions.add(roll(random));
            return new Selection(first.species(), first.level(), first.kind(), first.shiny(), companions);
        }

        /** 野外闪光率暂设为 1/2，每只只抽取一次。 */
        public Selection roll(RandomGenerator random) {
            int roll = random.nextInt(slots.stream().mapToInt(Slot::weight).sum());
            for (var slot : slots) {
                roll -= slot.weight();
                if (roll < 0) return new Selection(slot.species(), random.nextInt(slot.min(), slot.max() + 1), kind, random.nextBoolean());
            }
            throw new IllegalStateException("Empty encounter table");
        }
    }
    public record Selection(int species, int level, int kind, boolean shiny, List<Selection> companions) {
        public Selection(int species, int level, int kind, boolean shiny) {
            this(species, level, kind, shiny, List.of());
        }
        public Selection(int species, int level, int kind) {
            this(species, level, kind, false);
        }
        public Selection {
            companions = List.copyOf(companions);
            if (!companions.isEmpty() && companions.size() != 2 && companions.size() != 4
                    || companions.stream().anyMatch(member -> !member.companions().isEmpty()))
                throw new IllegalArgumentException("Invalid wild horde");
            if (species < 1 || species > PokemonCatalog.MAX_SPECIES || level < 1 || level > 100 || kind < 0 || kind > 10)
                throw new IllegalArgumentException("Invalid wild encounter");
        }
        public List<Selection> selections() {
            List<Selection> result = new ArrayList<>();
            result.add(new Selection(species, level, kind, shiny));
            result.addAll(companions);
            return List.copyOf(result);
        }
        public PartyMember member() {
            int hp = PokemonCatalog.hp(species, level);
            return new PartyMember(species, shiny, false, hp, hp, level);
        }
    }
    public record Distribution(Table table, int min, int max, int weight) { }
    private static final List<Table> TABLES = load();
    private static final Map<Integer, List<Table>> MAPS = index();
    private EncounterCatalog() { }

    public static List<Table> tables() { return TABLES; }
    public static List<Table> tables(int region, int group, int map) {
        return MAPS.getOrDefault(region << 16 | group << 8 | map, List.of());
    }
    public static Table walking(int region, CharacterPackets.Position position, int behavior, int season, int time) {
        for (var table : tables(region, position.group(), position.map())) {
            if (table.season() >= 0 && table.season() != season || table.time() >= 0 && table.time() != time) continue;
            int kind = terrainKind(region, behavior, table.cave());
            if (table.kind() == kind) return table;
        }
        return null;
    }
    public static int terrainKind(int region, int behavior, boolean cave) {
        if (behavior < 0 || region == 4) return -1;
        if (region == 2) {
            if (behavior == 6 || behavior == 7 || behavior == 34) return DARK_GRASS;
            if (behavior == 4 || behavior == 5 || behavior == 33 || behavior == 8 || behavior == 9) return LAND;
            if (cave && (behavior == 0 || behavior == 1)) return LAND;
        } else if (region == 3 || region == 5) {
            if (behavior == 2 || behavior == 3) return LAND;
            if (cave && (behavior == 0 || behavior == 1 || behavior == 8 || behavior == 9 || behavior == 10 || behavior == 11 || behavior == 12)) return LAND;
        } else {
            if (behavior == 2 || region == 1 && behavior == 3 || behavior == 8) return LAND;
            if (cave && (behavior == 0 || behavior == 1 || behavior == 7 || behavior == 9 || behavior == 10)) return LAND;
        }
        return -1;
    }
    public static List<Distribution> distribution(int species) {
        List<Distribution> result = new ArrayList<>();
        for (var table : TABLES) {
            int min = 101, max = 0, weight = 0;
            for (var slot : table.slots()) if (slot.species() == species) {
                min = Math.min(min, slot.min()); max = Math.max(max, slot.max()); weight += slot.weight();
            }
            if (weight > 0) result.add(new Distribution(table, min, max, weight));
        }
        return List.copyOf(result);
    }
    private static Map<Integer, List<Table>> index() {
        Map<Integer, List<Table>> result = new HashMap<>();
        for (var table : TABLES) result.computeIfAbsent(table.region() << 16 | table.group() << 8 | table.map(), ignored -> new ArrayList<>()).add(table);
        result.replaceAll((key, value) -> List.copyOf(value));
        return Map.copyOf(result);
    }
    private static List<Table> load() {
        Map<List<Integer>, List<Slot>> rows = new LinkedHashMap<>();
        String[] names = {"fire-red", "emerald", "unova", "sinnoh", "", "johto"};
        for (int region : new int[]{0, 1, 2, 3, 5}) {
            try (var raw = EncounterCatalog.class.getResourceAsStream("/maps/" + names[region] + "-encounters.csv.gz")) {
                if (raw == null) throw new IllegalStateException("Missing encounter catalog: " + region);
                try (var reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(raw), StandardCharsets.UTF_8))) {
                    for (String line; (line = reader.readLine()) != null;) {
                        if (line.startsWith("#") || line.isBlank()) continue;
                        int[] fields = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
                        if (fields.length != 13 || fields[0] != region || !FireRedMaps.contains(region, fields[1], fields[2], 0, 0)
                                || fields[4] < 0 || fields[4] > 10 || fields[5] < -1 || fields[5] > 3 || fields[6] < -1 || fields[6] > 2
                                || fields[7] < 1 || fields[7] > 100) throw new IllegalStateException("Invalid encounter table: " + line);
                        List<Integer> key = Arrays.stream(fields, 0, 9).boxed().toList();
                        rows.computeIfAbsent(key, ignored -> new ArrayList<>()).add(new Slot(fields[9], fields[10], fields[11], fields[12]));
                    }
                }
            } catch (Exception error) { throw new ExceptionInInitializerError(error); }
        }
        List<Table> result = new ArrayList<>();
        rows.forEach((k, v) -> result.add(new Table(k.get(0), k.get(1), k.get(2), k.get(3), k.get(4), k.get(5), k.get(6), k.get(7), k.get(8) == 1, v)));
        return List.copyOf(result);
    }
}
