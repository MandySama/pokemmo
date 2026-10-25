package top.totodile.pokemmo.client.rom.nds.bw;

import com.badlogic.gdx.files.FileHandle;

import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * The same personal / learnset / evolution archives as BlackWhiteRom.loadSpecies, National 1–649.
 */
public final class BwPokedex {
    public static final int LAST_SPECIES = 649;

    public record Evolution(int method, int parameter, int target) {}

    public record LearnedMove(int level, int move, int source) {
        public LearnedMove(int level, int move) {
            this(level, move, -1);
        }
    }

    public record Location(
            String kind,
            int region,
            String name,
            int minimum,
            int maximum,
            String rarity,
            int season,
            int timeMask) {}

    public record Extra(
            int genderRatio,
            int catchRate,
            int growthRate,
            int baseExperience,
            int formCount,
            int formSprite,
            List<Integer> tiers,
            List<Location> locations,
            Map<Integer, Integer> regional,
            boolean obtainable) {}

    public record Move(
            int id, int type, int category, int power, int accuracy, int pp, int target) {
        public Move(int id, int type, int category, int power, int accuracy, int pp) {
            this(id, type, category, power, accuracy, pp, 0);
        }

        /** Native MoveTargetType: all adjacent combatants, all enemies, or all debut combatants. */
        public boolean spread() {
            return target == 4 || target == 5 || target == 8;
        }

        public boolean automaticTarget() {
            return target == 1
                    || target == 2
                    || target == 17
                    || target == 6
                    || target == 7
                    || target == 9
                    || target == 10
                    || target == 11
                    || target == 12;
        }
    }

    public record Species(
            int id,
            List<Integer> stats,
            int type1,
            int type2,
            int height,
            int weight,
            List<Integer> abilities,
            List<Integer> eggGroups,
            List<Integer> evYield,
            List<Integer> heldItems,
            List<LearnedMove> moves,
            List<Evolution> evolutions) {
        public int total() {
            return stats.stream().mapToInt(Integer::intValue).sum();
        }
    }

    final Map<Integer, Integer> rarityClasses = new HashMap<>();

    public boolean legendary(int species) {
        int value = rarityClasses.getOrDefault(species, 0);
        return value == 1 || value == 2;
    }

    private final RomInfo rom;
    final ClientStrings strings;
    final List<Species> species = new ArrayList<>();
    private final List<Move> moves = new ArrayList<>();
    private final Map<Integer, Integer> parents = new HashMap<>();
    final Map<Integer, Extra> extras = new HashMap<>();
    final Map<Integer, Move> moveOverrides = new HashMap<>();
    private final NdsArchive text;
    private final Map<Integer, String[]> textTables = new HashMap<>();

    public BwPokedex(RomInfo rom, ClientStrings strings) throws IOException {
        if (rom == null || rom.type() != RomType.BLACK_WHITE)
            throw new IOException("Black/White ROM required");
        this.rom = rom;
        this.strings = strings;
        var personal = archive("a/0/1/6");
        var learnsets = archive("a/0/1/8");
        var evolutions = archive("a/0/1/9");
        text = archive("a/0/0/2");
        if (personal.size() <= LAST_SPECIES)
            throw new IOException("Incomplete BW personal archive");
        for (int id = 1; id <= LAST_SPECIES; id++) {
            byte[] p = personal.readRaw(id), l = learnsets.readRaw(id), e = evolutions.readRaw(id);
            if (p.length < 40 || e.length < 42) throw new IOException("Truncated species " + id);
            var learned = new ArrayList<LearnedMove>();
            for (int offset = 0; offset + 4 <= l.length; offset += 4) {
                int move = u16(l, offset), level = u16(l, offset + 2);
                if (move == 65535 || level == 65535) break;
                learned.add(new LearnedMove(level, move));
            }
            var next = new ArrayList<Evolution>();
            for (int offset = 0; offset < 42; offset += 6) {
                int method = u16(e, offset), target = u16(e, offset + 4);
                if (method > 0 && target > 0 && target <= LAST_SPECIES) {
                    next.add(new Evolution(method, u16(e, offset + 2), target));
                    parents.putIfAbsent(target, id);
                }
            }
            // ROM order is HP, Attack, Defense, Speed, Sp. Attack, Sp. Defense.
            List<Integer> stats =
                    List.of(u8(p, 0), u8(p, 1), u8(p, 2), u8(p, 4), u8(p, 5), u8(p, 3));
            int ev = u16(p, 10);
            List<Integer> yield =
                    List.of(
                            ev & 3,
                            ev >> 2 & 3,
                            ev >> 4 & 3,
                            ev >> 8 & 3,
                            ev >> 10 & 3,
                            ev >> 6 & 3);
            species.add(
                    new Species(
                            id,
                            stats,
                            u8(p, 6),
                            u8(p, 7),
                            u16(p, 36),
                            u16(p, 38),
                            List.of(u8(p, 24), u8(p, 25), u8(p, 26)),
                            List.of(u8(p, 22), u8(p, 23)),
                            yield,
                            List.of(u16(p, 12), u16(p, 14), u16(p, 16)),
                            List.copyOf(learned),
                            List.copyOf(next)));
            extras.put(
                    id,
                    new Extra(
                            u8(p, 18),
                            u8(p, 8),
                            u8(p, 21),
                            u16(p, 34),
                            u8(p, 32),
                            u16(p, 30),
                            List.of(5),
                            List.of(),
                            Map.of(),
                            false));
        }
        var moveData = archive("a/0/2/1");
        for (int id = 0; id < moveData.size(); id++) {
            byte[] p = moveData.readRaw(id);
            if (p.length < 6) throw new IOException("Truncated move " + id);
            moves.add(new Move(id, u8(p, 0), u8(p, 2), u8(p, 3), u8(p, 4), u8(p, 5), u8(p, 20)));
        }
    }

    /** ROM first, then data/data.pak, as in the reference DataPackageLoader.ha0/xu0. */
    public void loadDataPackage(FileHandle file) throws IOException {
        new BwPokedexDataPackage(this).load(file);
    }

    /** The reference ServerOpcode079Packet supplies tiers separately from the data package. */
    public void loadTiers(byte[] payload) throws IOException {
        if (payload.length == 0) return;
        try {
            ByteBuffer input = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);
            var primary = new HashMap<Integer, Integer>();
            int count = Short.toUnsignedInt(input.getShort());
            for (int i = 0; i < count; i++)
                primary.put(Short.toUnsignedInt(input.getShort()), Byte.toUnsignedInt(input.get()));
            var rules = new HashMap<Integer, Set<Integer>>();
            count = Byte.toUnsignedInt(input.get());
            for (int i = 0; i < count; i++) {
                int tier = Byte.toUnsignedInt(input.get()),
                        size = Short.toUnsignedInt(input.getShort());
                var ids = new HashSet<Integer>();
                for (int j = 0; j < size; j++) ids.add(Short.toUnsignedInt(input.getShort()));
                rules.put(tier, ids);
            }
            if (input.hasRemaining()) throw new IOException("Trailing Pokédex tier data");
            for (int id = 1; id <= LAST_SPECIES; id++) {
                Extra old = extra(id);
                var tiers = new ArrayList<Integer>();
                tiers.add(primary.getOrDefault(id, 5));
                for (var rule : rules.entrySet())
                    if (rule.getValue().contains(id) && !tiers.contains(rule.getKey()))
                        tiers.add(rule.getKey());
                extras.put(
                        id,
                        new Extra(
                                old.genderRatio,
                                old.catchRate,
                                old.growthRate,
                                old.baseExperience,
                                old.formCount,
                                old.formSprite,
                                List.copyOf(tiers),
                                old.locations,
                                old.regional,
                                old.obtainable));
            }
        } catch (RuntimeException error) {
            throw new IOException("Invalid Pokédex tier packet", error);
        }
    }

    public NdsArchive archive(String path) throws IOException {
        return new NdsArchive(RomReader.readFile(rom, path));
    }

    RomInfo rom() {
        return rom;
    }

    public List<Species> all() {
        return Collections.unmodifiableList(species);
    }

    public Species get(int id) {
        if (id < 1 || id > LAST_SPECIES) throw new IllegalArgumentException("National ID " + id);
        return species.get(id - 1);
    }

    Move moveOrNull(int id) {
        Move override = moveOverrides.get(id);
        return override != null ? override : id >= 0 && id < moves.size() ? moves.get(id) : null;
    }

    /** Loaded ROM and reference move-table entries, without species learnset filtering. */
    public List<Move> allMoves() {
        var result = new java.util.TreeMap<Integer, Move>();
        for (var move : moves) if (move.id() > 0) result.put(move.id(), move);
        for (var move : moveOverrides.values()) if (move.id() > 0) result.put(move.id(), move);
        return List.copyOf(result.values());
    }

    public Move move(int id) {
        Move move = moveOrNull(id);
        if (move == null) throw new IllegalArgumentException("Move ID " + id);
        return move;
    }

    public Extra extra(int id) {
        get(id);
        return extras.get(id);
    }

    public double effectiveness(int attack, int id) {
        Species s = get(id);
        return BwTypeEffectiveness.against(attack, s.type1)
                * (s.type1 == s.type2 ? 1 : BwTypeEffectiveness.against(attack, s.type2));
    }

    public List<LearnedMove> learnset(int id) {
        var result = new ArrayList<>(get(id).moves);
        var seen = new HashSet<Integer>();
        for (LearnedMove move : result) if (move.source == 0) seen.add(move.move);
        int parent = parent(id);
        var visited = new HashSet<Integer>();
        while (parent > 0 && visited.add(parent)) {
            for (LearnedMove move : get(parent).moves)
                if (move.source == 0 && seen.add(move.move)) result.add(move);
            parent = parent(parent);
        }
        return result;
    }

    public int number(int id, int region) {
        return region < 0 ? id : extra(id).regional.getOrDefault(region, 0);
    }

    public String regionName(int region) {
        return strings.getOrDefault(250000 + region, "");
    }

    public String moveDescription(int id) {
        return strings.getOrDefault(120000 + id, "");
    }

    public String moveSource(LearnedMove learned) {
        if (learned.source == 4) return "TM";
        if (learned.source >= 0) return strings.get(1750 + learned.source);
        return learned.level <= 1
                ? strings.get(1732)
                : strings.format(1731, Integer.toString(learned.level));
    }

    public int backgroundType(int id) {
        var species = get(id);
        return species.type1() == 0 && species.type2() != 0 ? species.type2() : species.type1();
    }

    public int experienceAtLevel(int id, int level) {
        return top.totodile.pokemmo.server.game.PokemonExperience.atLevel(
                extra(id).growthRate(), level);
    }

    public long experienceAt100(int id) {
        return experienceAtLevel(id, 100);
    }

    public String growthName(int id) {
        return strings.get(
                switch (extra(id).growthRate) {
                    case 0 -> 6483;
                    case 1 -> 6484;
                    case 2 -> 6485;
                    case 3 -> 6486;
                    case 4 -> 6487;
                    default -> 6488;
                });
    }

    public String gender(int id) {
        int ratio = extra(id).genderRatio;
        if (ratio == 255) return strings.getOrDefault(181016, "—");
        double female = ratio == 254 ? 100 : ratio == 0 ? 0 : (ratio + 1) * 100.0 / 256;
        return strings.format(
                6491,
                String.format(Locale.ROOT, "%s%%", decimal(100 - female)),
                String.format(Locale.ROOT, "%s%%", decimal(female)));
    }

    private static String decimal(double value) {
        return value == Math.rint(value)
                ? Integer.toString((int) value)
                : String.format(Locale.ROOT, "%.1f", value);
    }

    public List<Integer> evolutionFamily(int id) {
        return top.totodile.pokemmo.server.game.PokemonEvolutionChain.family(
                id,
                all().stream()
                        .collect(
                                java.util.stream.Collectors.toMap(
                                        Species::id,
                                        s ->
                                                s.evolutions().stream()
                                                        .map(Evolution::target)
                                                        .toList())));
    }

    public int parent(int id) {
        return parents.getOrDefault(id, 0);
    }

    public String name(int id) {
        return strings.getOrDefault(150000 + id, "#" + id);
    }

    public String moveName(int id) {
        return strings.getOrDefault(110000 + id, "#" + id);
    }

    /** LocalizationMessageFormatter registers BW ability names from bank 182; slot zero is None. */
    public List<Integer> allAbilities() {
        String[] names =
                textTables.computeIfAbsent(
                        182,
                        bank -> {
                            try {
                                return BwText.decode(text.readRaw(bank));
                            } catch (IOException e) {
                                throw new IllegalStateException("Cannot read ability names", e);
                            }
                        });
        return java.util.stream.IntStream.range(1, names.length)
                .filter(id -> !names[id].isBlank())
                .boxed()
                .toList();
    }

    public String ability(int id) {
        return strings.getOrDefault(210000 + id, text(182, id));
    }

    /** LocalizationMessageFormatter maps nature names to the original BW text bank 24. */
    public String nature(int id) {
        return text(24, id);
    }

    /** PokemonSoundEffectSource.BO: single wild encounter, BW bank 15, entry 1. */
    public String encounterMessage(int species) throws IOException {
        String template = strings.archive(2, 0, 15, 0, 1);
        if (template == null) template = BwText.decodeFormatted(text.readRaw(15))[1];
        // The supplied official PC capture displays this announcement on one line.
        // Some language MODs retain the handheld line break before the species slot.
        return template.replace("\n", "").replace("{00}", name(species));
    }

    /** AudioStreamSource.T8: one party member, BW bank 15 entry 17. */
    public String sendOutMessage(String trainer, String pokemon) throws IOException {
        String template = strings.archive(2, 0, 15, 0, 17);
        if (template == null) template = BwText.decodeFormatted(text.readRaw(15))[17];
        return template.replace("\n", "").replace("{00}", trainer).replace("{01}", pokemon);
    }

    private String[] formattedBattleText;

    private String battleText(int entry) {
        String translated = strings.archive(2, 0, 15, 0, entry);
        if (translated != null) return translated;
        if (formattedBattleText == null)
            try {
                formattedBattleText = BwText.decodeFormatted(text.readRaw(15));
            } catch (IOException error) {
                throw new IllegalStateException("Cannot read formatted battle text", error);
            }
        return formattedBattleText[entry];
    }

    /** BattleActiveSlotModifier: one of the five native recall lines (inclusive 25..29). */
    public String recallMessage(String pokemon) {
        return battleText(java.util.concurrent.ThreadLocalRandom.current().nextInt(25, 30))
                .replace("\n", "")
                .replace("{00}", pokemon);
    }

    /** BattleSoundEffectTask: encouragement depends on the opposing active slot's HP. */
    public String switchMessage(String pokemon, int hp, int maxHp) {
        int entry = hp >= maxHp * .75 ? 11 : hp >= maxHp * .5 ? 21 : hp >= maxHp * .25 ? 22 : 24;
        return battleText(entry).replace("\n", "").replace("{00}", pokemon);
    }

    /** BattleMegaButtonModifier: failure lines 61..64, success line 65. */
    public String captureMessage(int result, String pokemon) {
        return battleText(result == 4 ? 65 : 61 + result)
                .replace("\n", "")
                .replace("{00}", pokemon);
    }

    /** BattleItemBagModifier: NORMAL_SUCCESS uses BW message bank 15, entry 72. */
    /** BattleActionPromptModifier.QC: BW bank 15, entry 33. */
    public String moveUsedMessage(String pokemon, int move) {
        return battleText(33).replace("{00}", pokemon).replace("{01}", moveName(move));
    }

    private String[] formattedResultText;

    /** StatAction001Packet.qv / AudioMixerChannelDispatcher.QX: BW bank 14, wild variant +1. */
    public String statChangeMessage(String pokemon, boolean wild, String stat, int amount) {
        int order =
                switch (stat) {
                    case "atk" -> 1;
                    case "def" -> 2;
                    case "spa" -> 3;
                    case "spd" -> 4;
                    case "spe" -> 5;
                    case "accuracy" -> 6;
                    case "evasion" -> 7;
                    default -> 0;
                };
        if (order == 0 || amount == 0) return "";
        int index =
                27
                        + (order - 1) * 3
                        + (Math.min(3, Math.abs(amount)) - 1) * 21
                        + (amount < 0 ? 63 : 0)
                        + (wild ? 1 : 0);
        return resultText(index).replace("{00}", pokemon).replace("\n", " ");
    }

    /** BattleFaintSlotModifier.Dz: failure 15/71; immune 14/210; miss 14/213; protect 14/517. */
    public String moveRejectedMessage(String pokemon, boolean wild, String result) {
        if (result.equals("FAIL")) return battleText(71).replace("\n", " ");
        int index =
                switch (result) {
                    case "IMMUNE" -> 210;
                    case "MISS" -> 213;
                    case "BLOCK" -> 517;
                    default -> throw new IllegalArgumentException(result);
                };
        return resultText(index + (wild ? 1 : 0)).replace("{00}", pokemon).replace("\n", " ");
    }

    /** BattleAction046Packet / BattleCombatLayout: Disable start, blocked choice, and expiry. */
    public String disableMessage(String pokemon, boolean wild, int move, String type) {
        int index =
                switch (type) {
                    case "DISABLE_START" -> 592;
                    case "DISABLE_END" -> 598;
                    default -> 595;
                };
        return resultText(index + (wild ? 1 : 0))
                .replace("{00}", pokemon)
                .replace("{01}", moveName(move))
                .replace("\n", " ");
    }

    private String resultText(int index) {
        String template = strings.archive(2, 0, 14, 0, index);
        if (template == null) {
            if (formattedResultText == null)
                try {
                    formattedResultText = BwText.decodeFormatted(text.readRaw(14));
                } catch (IOException error) {
                    throw new IllegalStateException("Cannot read battle result text", error);
                }
            template = formattedResultText[index];
        }
        return template;
    }

    public String escapedMessage() {
        return text(15, 72);
    }

    /** Summary history uses BW message bank 188, entries 53..82. */
    public String ribbonText(int entry) {
        return text(170, entry);
    }

    public String characteristic(int stat, int iv) {
        return text(188, 53 + stat * 5 + iv % 5);
    }

    public String abilityDescription(int id) {
        return strings.getOrDefault(220000 + id, "");
    }

    public String item(int id) {
        return strings.getOrDefault(245000 + id, "#" + id);
    }

    public String type(int id) {
        return strings.getOrDefault(230000 + (id >= 9 ? id + 1 : id), "—");
    }

    public String eggGroup(int id) {
        return strings.getOrDefault(181000 + id, "—");
    }

    public String category(int id) {
        return text(260, id);
    }

    public String description(int id) {
        return text(rom.code().startsWith("IRA") ? 236 : 235, id).replace('\n', ' ');
    }

    public String nationalTitle() {
        String title = text(276, 1);
        return title.equalsIgnoreCase("National Pokédex") ? "National Pokédex" : title;
    }

    private String text(int table, int id) {
        String translated = strings.archive(2, 0, table, 0, id);
        if (translated != null) return translated;
        String[] entries =
                textTables.computeIfAbsent(
                        table,
                        key -> {
                            try {
                                return BwText.decode(text.readRaw(key));
                            } catch (IOException error) {
                                throw new IllegalStateException(
                                        "Cannot read BW text " + key, error);
                            }
                        });
        return id < entries.length ? entries[id] : "—";
    }

    public List<Species> search(String query) {
        String term = query.strip().toLowerCase(Locale.ROOT).replaceFirst("^#", "");
        return species.stream()
                .filter(
                        s ->
                                term.isEmpty()
                                        || name(s.id()).toLowerCase(Locale.ROOT).contains(term)
                                        || Integer.toString(s.id()).equals(term)
                                        || String.format(Locale.ROOT, "%03d", s.id()).equals(term))
                .toList();
    }

    public String evolution(Evolution evolution) {
        int value = evolution.parameter();
        String parameter =
                switch (evolution.method()) {
                    case 6, 8, 17, 18, 19, 20 -> item(value);
                    case 21 -> moveName(value);
                    case 7 -> name(evolution.target() == 617 ? 588 : 616);
                    case 22 -> value >= 1 && value <= LAST_SPECIES ? name(value) : "—";
                    default -> Integer.toString(value);
                };
        return strings.format(2550 + evolution.method(), parameter);
    }

    static int u8(byte[] data, int offset) {
        return data[offset] & 255;
    }

    static int u16(byte[] data, int offset) {
        return u8(data, offset) | (u8(data, offset + 1) << 8);
    }
}
