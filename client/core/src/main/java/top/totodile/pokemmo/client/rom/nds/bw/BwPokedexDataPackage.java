package top.totodile.pokemmo.client.rom.nds.bw;

import static top.totodile.pokemmo.client.rom.nds.bw.BwPokedex.*;

import com.badlogic.gdx.files.FileHandle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Pokédex sections of Pokemmo2Pro-Client DataPackageLoader.ha0/xu0, data.pak v133. */
public final class BwPokedexDataPackage {
    private final BwPokedex dex;

    BwPokedexDataPackage(BwPokedex dex) {
        this.dex = dex;
    }

    public record Section(int type, ByteBuffer data) {}

    /** Shared DataPackageLoader.ha0/xu0 framing; every registry uses the same version check. */
    public static List<Section> sections(FileHandle file) throws IOException {
        try (var stream = new GZIPInputStream(file.read())) {
            ByteBuffer input =
                    ByteBuffer.wrap(stream.readAllBytes()).order(ByteOrder.LITTLE_ENDIAN);
            byte[] signature = {7, 'P', 'O', 'K', 'E', 'M', 'M', 'O'};
            for (byte value : signature)
                if (input.get() != value) throw new IOException("Invalid data.pak header");
            int version = input.getInt();
            if (version != 133)
                throw new IOException("Mismatched data.pak version. Expected 133 got " + version);
            int sections = u8(input);
            var result = new ArrayList<Section>();
            for (int i = 0; i < sections; i++) {
                int type = u8(input), length = input.getInt();
                if (length < 0 || length > input.remaining())
                    throw new IOException("Invalid data.pak section length: " + type);
                ByteBuffer section = input.slice().order(ByteOrder.LITTLE_ENDIAN);
                section.limit(length);
                result.add(new Section(type, section));
                input.position(input.position() + length);
            }
            if (input.hasRemaining()) throw new IOException("Trailing data.pak bytes");
            return List.copyOf(result);
        } catch (RuntimeException error) {
            throw new IOException("Cannot read data.pak", error);
        }
    }

    void load(FileHandle file) throws IOException {
        try {
            for (var entry : sections(file)) {
                var section = entry.data();
                boolean handled = true;
                switch (entry.type()) {
                    case 1 -> levelMoves(section);
                    case 2 -> otherMoves(section);
                    case 4 -> moves(section);
                    case 5 -> locations(section);
                    case 6 -> species(section);
                    case 11 -> regions(section);
                    default -> handled = false;
                }
                if (handled && section.hasRemaining())
                    throw new IOException(
                            "Unread data.pak section " + entry.type() + ": " + section.remaining());
            }
        } catch (RuntimeException error) {
            throw new IOException("Cannot read data.pak", error);
        }
    }

    public static void skipEffectParameter(ByteBuffer input) {
        parameter(input);
    }

    private static int u8(ByteBuffer b) {
        return Byte.toUnsignedInt(b.get());
    }

    private static int u16(ByteBuffer b) {
        return Short.toUnsignedInt(b.getShort());
    }

    private static boolean supported(int id) {
        return id >= 1 && id <= LAST_SPECIES;
    }

    private void learned(int id, List<LearnedMove> learned) {
        if (!supported(id)) return;
        Species old = dex.get(id);
        dex.species.set(
                id - 1,
                new Species(
                        id,
                        old.stats(),
                        old.type1(),
                        old.type2(),
                        old.height(),
                        old.weight(),
                        old.abilities(),
                        old.eggGroups(),
                        old.evYield(),
                        old.heldItems(),
                        List.copyOf(learned),
                        old.evolutions()));
    }

    private void levelMoves(ByteBuffer b) {
        int count = u16(b);
        for (int i = 0; i < count; i++) {
            int id = u16(b), size = u8(b);
            var learned = new ArrayList<LearnedMove>();
            if (supported(id))
                for (var move : dex.get(id).moves()) if (move.source() >= 0) learned.add(move);
            for (int j = 0; j < size; j++) {
                int move = u16(b), level = u8(b);
                learned.add(new LearnedMove(level, move));
            }
            learned(id, learned);
        }
    }

    private void otherMoves(ByteBuffer b) {
        int count = u16(b);
        for (int i = 0; i < count; i++) {
            int id = u16(b), source = u8(b), size = u16(b);
            var learned = new ArrayList<LearnedMove>();
            if (supported(id))
                for (var move : dex.get(id).moves()) if (move.source() != source) learned.add(move);
            for (int j = 0; j < size; j++) learned.add(new LearnedMove(0, u16(b), source));
            learned(id, learned);
        }
    }

    private void moves(ByteBuffer b) {
        int count = u16(b);
        for (int i = 0; i < count; i++) {
            int id = b.getShort(), category = u8(b), flags = u16(b);
            Move old = id >= 0 ? dex.moveOrNull(id) : null;
            int accuracy = old == null ? 0 : old.accuracy(),
                    power = old == null ? 0 : old.power(),
                    pp = old == null ? 0 : old.pp(),
                    type = old == null ? 17 : old.type();
            if ((flags & 1) != 0) accuracy = u8(b);
            if ((flags & 2) != 0) power = u16(b);
            if ((flags & 4) != 0) pp = u8(b);
            if ((flags & 8) != 0) {
                int packedType = u8(b);
                type = packedType >= 10 ? packedType - 1 : packedType;
            }
            if ((flags & 16) != 0) b.getInt();
            if ((flags & 32) != 0) b.get();
            if ((flags & 64) != 0) b.get();
            if ((flags & 256) != 0) b.get();
            if ((flags & 512) != 0) {
                skip(b, u8(b));
                b.get();
                skip(b, u8(b));
                skip(b, 3);
            }
            int effects = u8(b);
            for (int j = 0; j < effects; j++) {
                b.getInt();
                int parameters = u8(b);
                for (int k = 0; k < parameters; k++) parameter(b);
            }
            // Reference yw_0: physical=0, special=1, status=2; this UI uses BW ROM categories.
            if (id >= 0)
                dex.moveOverrides.put(
                        id,
                        new Move(
                                id,
                                type,
                                switch (category) {
                                    case 1 -> 2;
                                    case 2 -> 0;
                                    default -> 1;
                                },
                                power,
                                accuracy,
                                pp));
        }
    }

    /** Consume only the effect arguments; Pokédex move rows do not use battle effects. */
    private static void parameter(ByteBuffer b) {
        b.get();
        int type = u8(b);
        if ((type & 128) != 0) {
            type &= 127;
            b.get();
        }
        switch (type) {
            case 28 -> {}
            case 30 -> b.getLong();
            case 9, 10, 17 -> b.getInt();
            case 5, 18 -> {
                while (b.getChar() != 0) {}
            }
            default -> skip(b, u8(b) * 2);
        }
    }

    private static void skip(ByteBuffer b, int count) {
        b.position(b.position() + count);
    }

    private void locations(ByteBuffer b) {
        int count = u16(b);
        for (int i = 0; i < count; i++) {
            int id = u16(b), size = u16(b);
            var entries = new ArrayList<Location>();
            for (int j = 0; j < size; j++) {
                int region = b.get();
                b.getShort();
                int place = u8(b),
                        kind = u8(b),
                        subtype = b.get(),
                        times = u8(b),
                        season = b.get(),
                        flags = u16(b),
                        min = u8(b),
                        max = u8(b);
                int[] kinds = {1785, 1786, 1787, 1788, 1789, 1785, 1786, 1788, 1796, 1797, 1780};
                String method = dex.strings.get(kinds[kind]),
                        name = dex.strings.getOrDefault(140000 + region * 1000 + place, "???"),
                        rarity = "???";
                if ((flags & 1) != 0) rarity = dex.strings.get(1774);
                int[] bits = {2, 4, 8, 16, 64, 128, 256},
                        labels = {1782, 1783, 1784, 1773, 1795, 1799, 1794};
                for (int k = 0; k < bits.length; k++)
                    if ((flags & bits[k]) != 0) {
                        rarity = dex.strings.get(labels[k]);
                        break;
                    }
                if ((flags & 32) != 0) {
                    name = dex.strings.get(1798);
                    method = name;
                }
                if (kind == 0) {
                    if (subtype == 1) method = dex.strings.get(1792);
                    else if (subtype == 2) method = dex.strings.get(1793);
                }
                if (kind == 3)
                    method = dex.strings.get(245445 + (subtype < 0 || region == 2 ? 2 : subtype));
                // OrderedMoveRequirement.COM5 treats an empty time mask as unrestricted.
                entries.add(
                        new Location(
                                method,
                                region,
                                name,
                                min,
                                max,
                                rarity,
                                season,
                                times == 0 ? 7 : times));
            }
            if (supported(id)) {
                Extra old = dex.extra(id);
                dex.extras.put(
                        id,
                        new Extra(
                                old.genderRatio(),
                                old.catchRate(),
                                old.growthRate(),
                                old.baseExperience(),
                                old.formCount(),
                                old.formSprite(),
                                old.tiers(),
                                List.copyOf(entries),
                                old.regional(),
                                old.obtainable()));
            }
        }
    }

    private void species(ByteBuffer b) {
        int count = u16(b);
        for (int i = 0; i < count; i++) {
            int id = u16(b), flags = u16(b);
            Species old = supported(id) ? dex.get(id) : null;
            var stats =
                    old == null
                            ? new ArrayList<>(Collections.nCopies(6, 0))
                            : new ArrayList<>(old.stats());
            List<Integer> groups = old == null ? List.of(1, 1) : old.eggGroups(),
                    abilities = old == null ? List.of(0, 0, 0) : old.abilities(),
                    yield = old == null ? List.of(0, 0, 0, 0, 0, 0) : old.evYield();
            if ((flags & 1) != 0) groups = List.of(u8(b), u8(b));
            // Package gc_2.ME order is HP/Attack/Defense/Speed/Sp.Atk/Sp.Def;
            // this model stores the display order, with Speed last (gc_2.fe0).
            int[] statOrder = {0, 1, 2, 5, 3, 4};
            if ((flags & 4) != 0)
                for (int j = 0; j < 6; j++) {
                    int value = b.getShort();
                    if (value != -1) stats.set(statOrder[j], value);
                }
            if ((flags & 8) != 0) abilities = List.of(u16(b), u16(b), u16(b));
            if ((flags & 16) != 0) {
                int ev = u16(b);
                yield =
                        List.of(
                                ev & 3,
                                ev >> 2 & 3,
                                ev >> 4 & 3,
                                ev >> 8 & 3,
                                ev >> 10 & 3,
                                ev >> 6 & 3);
            }
            if ((flags & 32) != 0) skip(b, u8(b) * 2);
            if ((flags & 128) != 0) b.getShort();
            if ((flags & 256) != 0) {
                int rarity = u8(b);
                if (old != null) dex.rarityClasses.put(id, rarity);
            }
            if (old != null) {
                dex.species.set(
                        id - 1,
                        new Species(
                                id,
                                List.copyOf(stats),
                                old.type1(),
                                old.type2(),
                                old.height(),
                                old.weight(),
                                abilities,
                                groups,
                                yield,
                                old.heldItems(),
                                old.moves(),
                                old.evolutions()));
                Extra extra = dex.extra(id);
                dex.extras.put(
                        id,
                        new Extra(
                                extra.genderRatio(),
                                extra.catchRate(),
                                extra.growthRate(),
                                extra.baseExperience(),
                                extra.formCount(),
                                extra.formSprite(),
                                extra.tiers(),
                                extra.locations(),
                                extra.regional(),
                                extra.obtainable() || (flags & 2) != 0));
            }
        }
    }

    private void regions(ByteBuffer b) {
        int count = u8(b);
        for (int i = 0; i < count; i++) {
            int region = u8(b), size = u16(b);
            for (int rank = 1; rank <= size; rank++) {
                int id = u16(b);
                if (!supported(id)) continue;
                Extra old = dex.extra(id);
                var numbers = new HashMap<>(old.regional());
                numbers.put(region, rank);
                dex.extras.put(
                        id,
                        new Extra(
                                old.genderRatio(),
                                old.catchRate(),
                                old.growthRate(),
                                old.baseExperience(),
                                old.formCount(),
                                old.formSprite(),
                                old.tiers(),
                                old.locations(),
                                Map.copyOf(numbers),
                                old.obtainable()));
            }
        }
    }
}
