package top.totodile.pokemmo.client.ui.character;

import com.badlogic.gdx.files.FileHandle;

import top.totodile.pokemmo.client.resources.ClientStrings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** 参考 addons.pak 的外观目录、精灵帧索引和新角色筛选规则。 */
public final class CharacterAppearance {
    enum Slot {
        FOREHEAD(0, true),
        HAT(1, true),
        HAIR(2, false),
        EYES(3, false),
        FACE(4, true),
        BACK(5, true),
        TOP(6, false),
        GLOVES(7, true),
        SHOES(8, false),
        PANTS(9, false),
        ROD(10, false),
        BICYCLE(11, false);
        final int id;
        final boolean optional;

        Slot(int id, boolean optional) {
            this.id = id;
            this.optional = optional;
        }
    }

    record Entry(int slot, int id, String fallbackName, int flags) {
        String name(ClientStrings strings) {
            return id < 0
                    ? strings.get(2803)
                    : strings.getOrDefault(20000 + slot * 1000 + id, fallbackName);
        }

        boolean colorable() {
            return (flags & 16) != 0;
        }

        boolean coversHair() {
            return (flags & 65536) != 0;
        }

        boolean available(int gender) {
            int restriction = (flags & 1) != 0 ? 0 : (flags & 2) != 0 ? 1 : -1;
            return (restriction < 0 || restriction == gender)
                    && (flags & (4 | 1024)) == 0
                    && (slot != 3 || id >= 2);
        }
    }

    private boolean allStyles;
    private final List<Entry> entries;
    private final ClientStrings strings;
    private final EnumMap<Slot, List<Entry>> choices = new EnumMap<>(Slot.class);
    private final EnumMap<Slot, Entry> selected = new EnumMap<>(Slot.class);
    private final EnumMap<Slot, Integer> colors = new EnumMap<>(Slot.class);
    private final EnumMap<Slot, Integer> optionColors = new EnumMap<>(Slot.class);

    record Frame(int offset, int length, int source, int dx, int dy) {}

    private record FrameKey(int slot, int id, int view, int variant, int frame, int layer) {}

    private Map<FrameKey, Frame> frames = new HashMap<>();
    private byte[] archive;

    private record Track(int step, byte[] frames) {}

    private Map<Long, Track> animationTracks = new HashMap<>();

    private static long trackKey(int slot, int id) {
        return ((long) slot << 32) | (id & 0xffffffffL);
    }

    int variant(int slot, int id, long time) {
        var track = animationTracks.get(trackKey(slot, id));
        return track == null || track.frames.length == 0
                ? 0
                : track.frames[
                                (int)
                                        (Math.max(0, time)
                                                / Math.max(1, track.step)
                                                % track.frames.length)]
                        & 255;
    }

    // Modern_Battle_Ss0: subordinate effect IDs belong to a complete mount.
    static int bicycleStyle(int id) {
        if (id >= 13 && id <= 16) return 12;
        if (id >= 18 && id <= 27) return 17;
        if (id == 29) return 28;
        if (id == 31) return 30;
        if (id >= 41 && id <= 50) return 40;
        if (id >= 53 && id <= 56) return 52;
        return id;
    }

    Frame frame(int slot, int id, int view, int frame, int layer, int variant) {
        return frames.get(new FrameKey(slot, id, view, variant, frame, layer));
    }

    Frame frame(int slot, int id, int view, int direction, int layer) {
        return frames.get(new FrameKey(slot, id, view, 0, direction, layer));
    }

    byte[] archive() {
        return archive;
    }

    public void dispose() {
        frames = Map.of();
        archive = null;
    }

    public CharacterAppearance(FileHandle file, ClientStrings strings) {
        this.strings = strings;
        entries = read(file);
    }

    private CharacterAppearance(CharacterAppearance source) {
        strings = source.strings;
        entries = source.entries;
        frames = source.frames;
        archive = source.archive;
        animationTracks = source.animationTracks;
    }

    /** 参考共享 addon 目录；仅复用只读索引和归档，角色选择与颜色保持独立。 */
    public CharacterAppearance copy() {
        return new CharacterAppearance(this);
    }

    public record VanityItem(int itemId, int slot, int style, String name, int flags) {}

    public CharacterAppearance previewVanity(
            VanityItem item, top.totodile.pokemmo.server.game.GameService.Appearance saved) {
        CharacterAppearance result;
        if (saved == null) {
            result = copy();
            result.reset(0);
        } else result = restore(saved);
        for (Slot slot : Slot.values())
            if (slot.id == item.slot()) {
                var entry =
                        entries.stream()
                                .filter(e -> e.slot() == item.slot() && e.id() == item.style())
                                .findFirst()
                                .orElseThrow();
                result.selected.put(slot, entry);
                result.colors.put(slot, -1);
                break;
            }
        return result;
    }

    /** MapWarpConnectionData.nW/KE0 and VanityItemDefinitionRegistry.vK0. */
    public List<VanityItem> vanityItems() {
        var result = new ArrayList<VanityItem>();
        for (var entry : entries) {
            boolean itemCategory =
                    entry.slot() == 0
                            || entry.slot() == 2
                            || entry.slot() == 3
                            || entry.slot() == 4;
            if (!itemCategory && (entry.flags() & 4) == 0) continue;
            int id =
                    entry.slot() == 1 && entry.id() > 255
                            ? entry.id() - 4320
                            : entry.slot() * 256 + 2000 + entry.id();
            if (id > 0 && id <= Short.MAX_VALUE)
                result.add(
                        new VanityItem(
                                id, entry.slot(), entry.id(), entry.fallbackName(), entry.flags()));
        }
        return List.copyOf(result);
    }

    top.totodile.pokemmo.server.game.GameService.Appearance snapshot(
            int gender, int skin, int region) {
        var parts = new HashMap<Integer, top.totodile.pokemmo.server.game.GameService.Part>();
        for (Slot slot : Slot.values())
            if (allStyles || slot.id != 0 && slot.id != 10)
                parts.put(
                        slot.id,
                        new top.totodile.pokemmo.server.game.GameService.Part(
                                selected(slot).id(), color(slot)));
        return new top.totodile.pokemmo.server.game.GameService.Appearance(
                gender, skin, region, parts);
    }

    public CharacterAppearance restore(
            top.totodile.pokemmo.server.game.GameService.Appearance saved) {
        var result = new CharacterAppearance(this);
        result.loadSaved(saved, true);
        return result;
    }

    void loadSaved(top.totodile.pokemmo.server.game.GameService.Appearance saved, boolean all) {
        allStyles = all;
        reset(saved.gender());
        for (Slot slot : Slot.values()) {
            var part = saved.parts().get(slot.id);
            if (part == null) {
                if (slot.id == 0) {
                    selected.put(slot, new Entry(slot.id, -1, "", 0));
                    colors.put(slot, -1);
                } else if (slot.id == 10) {
                    var entry =
                            choices(slot).stream()
                                    .min(Comparator.comparingInt(Entry::id))
                                    .orElseThrow();
                    selected.put(slot, entry);
                    colors.put(slot, 0);
                }
                continue;
            }
            int style = slot == Slot.BICYCLE ? bicycleStyle(part.style()) : part.style();
            var entry =
                    entries.stream()
                            .filter(e -> e.slot() == slot.id && e.id() == style)
                            .findFirst()
                            .orElse(null);
            if (entry == null && part.style() == -1) entry = new Entry(slot.id, -1, "", 0);
            if (entry == null) throw new IllegalArgumentException("Unknown saved appearance style");
            selected.put(slot, entry);
            colors.put(slot, part.color());
            optionColors.put(slot, part.color());
        }
    }

    List<Entry> choices(Slot slot) {
        return choices.get(slot);
    }

    Entry selected(Slot slot) {
        return selected.get(slot);
    }

    int color(Slot slot) {
        return colors.get(slot);
    }

    void color(Slot slot, int color) {
        if (!selected(slot).colorable() || color < 0 || color >= AppearancePalette.COLORS.length)
            throw new IllegalArgumentException("Invalid appearance color");
        colors.put(slot, color);
    }

    boolean enabled(Slot slot) {
        return choices(slot).size() > 1
                && (allStyles || slot != Slot.HAT || !selected(Slot.HAIR).coversHair());
    }

    void reset(int gender) {
        var random = ThreadLocalRandom.current();
        for (Slot slot : Slot.values()) {
            List<Entry> available = new ArrayList<>();
            if (slot.optional) available.add(new Entry(slot.id, -1, "", 0));
            for (Entry entry : entries)
                if ((allStyles || slot.id != 0)
                        && entry.slot() == slot.id
                        && (slot != Slot.BICYCLE || bicycleStyle(entry.id()) == entry.id())
                        && (allStyles || entry.available(gender))) available.add(entry);
            available.sort(
                    Comparator.comparingInt((Entry entry) -> entry.id() < 0 ? 0 : 1)
                            .thenComparing(entry -> entry.name(strings)));
            if (available.isEmpty())
                throw new IllegalStateException("Missing appearance options for " + slot);
            choices.put(slot, List.copyOf(available));
            // 参考新角色随机化：脸部始终从“无”开始。
            Entry entry =
                    available.get(
                            slot == Slot.FOREHEAD || slot == Slot.FACE && !allStyles
                                    ? 0
                                    : random.nextInt(available.size()));
            selected.put(slot, entry);
            colors.put(
                    slot, entry.colorable() ? random.nextInt(AppearancePalette.COLORS.length) : 0);
            // 参考 q9：重建下拉项时，各款式记下该栏当时的颜色。
            optionColors.put(slot, colors.get(slot));
        }
    }

    void select(Slot slot, int index) {
        Entry entry = choices(slot).get(index);
        selected.put(slot, entry);
        if (!entry.colorable()) colors.put(slot, entry.id() < 0 ? -1 : 0);
        else
            colors.put(
                    slot,
                    optionColors.get(slot) < 0
                            ? ThreadLocalRandom.current().nextInt(AppearancePalette.COLORS.length)
                            : optionColors.get(slot));
    }

    private List<Entry> read(FileHandle file) {
        try {
            byte[] bytes = file.readBytes();
            if (bytes.length < 22) throw new IllegalArgumentException("Truncated addons.pak");
            var hash = MessageDigest.getInstance("SHA-1");
            hash.update(bytes, 0, bytes.length - 20);
            if (!Arrays.equals(
                    hash.digest(), Arrays.copyOfRange(bytes, bytes.length - 20, bytes.length)))
                throw new IllegalArgumentException("Invalid addons.pak checksum");
            archive = bytes;
            ByteBuffer data =
                    ByteBuffer.wrap(bytes, 0, bytes.length - 20)
                            .slice()
                            .order(ByteOrder.LITTLE_ENDIAN);
            data.position(10);
            int skins = data.get();
            for (int skin = 0; skin < skins; skin++)
                for (int format = 0; format < 3; format++) {
                    int count = data.get();
                    for (int frame = 0; frame < count; frame++) {
                        int length = data.getInt();
                        frames.put(
                                new FrameKey(-1, skin, format, 0, frame, 0),
                                new Frame(data.position(), length, -1, 0, 0));
                        skip(data, length);
                    }
                }
            int count = data.getInt();
            List<Entry> result = new ArrayList<>();
            for (int item = 0; item < count; item++) {
                int id = data.getShort(), slot = data.get();
                byte[] name = new byte[data.get()];
                data.get(name);
                String fallback = new String(name, StandardCharsets.UTF_8).trim();
                int flags = data.getInt() & -8417;
                for (int table = data.get(); table > 0; table--) skip(data, data.getShort() * 2);
                int rows = data.get();
                for (int animation = data.get(); animation > 0; animation--) {
                    int type = data.get() & 255, step = data.getShort() & 65535;
                    byte[] sequence = new byte[data.get() & 255];
                    data.get(sequence);
                    if (type == 1)
                        animationTracks.put(trackKey(slot, id), new Track(step, sequence));
                }
                for (int row = 0; row < rows; row++) {
                    for (int format = 0; format < 3; format++) {
                        int frameCount = data.get();
                        for (int frame = 0; frame < frameCount; frame++) {
                            int layerCount = data.get();
                            for (int layer = 0; layer < layerCount; layer++) {
                                boolean external = format == 0 && data.get() != 0;
                                int target = data.get();
                                Frame image;
                                if (external)
                                    image =
                                            new Frame(
                                                    0,
                                                    0,
                                                    data.getInt() & 255,
                                                    data.getInt(),
                                                    data.getInt());
                                else {
                                    int length = data.getInt();
                                    image = new Frame(data.position(), length, -1, 0, 0);
                                    skip(data, length);
                                }
                                frames.put(
                                        new FrameKey(slot, id, format, row, frame, target), image);
                            }
                        }
                    }
                }
                if (!fallback.equals("UNUSED")) result.add(new Entry(slot, id, fallback, flags));
            }
            return List.copyOf(result);
        } catch (Exception error) {
            throw new IllegalArgumentException(
                    "Cannot load appearance catalog " + file.path(), error);
        }
    }

    private static void skip(ByteBuffer data, int length) {
        if (length < 0 || length > data.remaining())
            throw new IllegalArgumentException("Invalid appearance data length");
        data.position(data.position() + length);
    }
}
