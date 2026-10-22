package top.totodile.pokemmo.client.ui.character;

import com.badlogic.gdx.files.FileHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import top.totodile.pokemmo.client.resources.ClientStrings;

/** 参考 addons.pak 的外观目录、精灵帧索引和新角色筛选规则。 */
public final class CharacterAppearance {
    enum Slot {
        HAT(1, true), HAIR(2, false), EYES(3, false), FACE(4, true), BACK(5, true),
        TOP(6, false), GLOVES(7, true), SHOES(8, false), PANTS(9, false), BICYCLE(11, false);
        final int id;
        final boolean optional;
        Slot(int id, boolean optional) { this.id = id; this.optional = optional; }
    }
    record Entry(int slot, int id, String fallbackName, int flags) {
        String name(ClientStrings strings) {
            return id < 0 ? strings.get(2803) : strings.getOrDefault(20000 + slot * 1000 + id, fallbackName);
        }
        boolean colorable() { return (flags & 16) != 0; }
        boolean coversHair() { return (flags & 65536) != 0; }
        boolean available(int gender) {
            int restriction = (flags & 1) != 0 ? 0 : (flags & 2) != 0 ? 1 : -1;
            return (restriction < 0 || restriction == gender) && (flags & (4 | 1024)) == 0
                    && (slot != 3 || id >= 2);
        }
    }

    private final List<Entry> entries;
    private final ClientStrings strings;
    private final EnumMap<Slot,List<Entry>> choices = new EnumMap<>(Slot.class);
    private final EnumMap<Slot,Entry> selected = new EnumMap<>(Slot.class);
    private final EnumMap<Slot,Integer> colors = new EnumMap<>(Slot.class);
    private final EnumMap<Slot,Integer> optionColors = new EnumMap<>(Slot.class);
    record Frame(int offset, int length, int source, int dx, int dy) {}
    private record FrameKey(int slot, int id, int view, int variant, int frame, int layer) {}
    private Map<FrameKey,Frame> frames = new HashMap<>();
    private byte[] archive;

    Frame frame(int slot, int id, int view, int direction, int layer) {
        return frames.get(new FrameKey(slot, id, view, 0, direction, layer));
    }
    byte[] archive() { return archive; }

    void dispose() {
        frames = Map.of();
        archive = null;
    }

    public CharacterAppearance(FileHandle file, ClientStrings strings) {
        this.strings = strings;
        entries = read(file);
    }

    private CharacterAppearance(CharacterAppearance source) {
        strings = source.strings; entries = source.entries;
        frames = source.frames; archive = source.archive;
    }

    top.totodile.pokemmo.server.game.GameService.Appearance snapshot(int gender, int skin, int region) {
        var parts = new HashMap<Integer, top.totodile.pokemmo.server.game.GameService.Part>();
        for (Slot slot : Slot.values()) parts.put(slot.id,
                new top.totodile.pokemmo.server.game.GameService.Part(selected(slot).id(), color(slot)));
        return new top.totodile.pokemmo.server.game.GameService.Appearance(gender, skin, region, parts);
    }

    CharacterAppearance restore(top.totodile.pokemmo.server.game.GameService.Appearance saved) {
        var result = new CharacterAppearance(this);
        result.reset(saved.gender());
        for (Slot slot : Slot.values()) {
            var part = saved.parts().get(slot.id);
            if (part == null) throw new IllegalArgumentException("Missing saved appearance");
            var entry = result.choices(slot).stream().filter(value -> value.id() == part.style()).findFirst().orElseThrow();
            result.selected.put(slot, entry); result.colors.put(slot, part.color());
        }
        return result;
    }

    List<Entry> choices(Slot slot) { return choices.get(slot); }
    Entry selected(Slot slot) { return selected.get(slot); }
    int color(Slot slot) { return colors.get(slot); }
    void color(Slot slot, int color) {
        if (!selected(slot).colorable() || color < 0 || color >= AppearancePalette.COLORS.length)
            throw new IllegalArgumentException("Invalid appearance color");
        colors.put(slot, color);
    }
    boolean enabled(Slot slot) {
        return choices(slot).size() > 1 && (slot != Slot.HAT || !selected(Slot.HAIR).coversHair());
    }
    void reset(int gender) {
        var random = ThreadLocalRandom.current();
        for (Slot slot : Slot.values()) {
            List<Entry> available = new ArrayList<>();
            if (slot.optional) available.add(new Entry(slot.id, -1, "", 0));
            for (Entry entry : entries)
                if (entry.slot() == slot.id && entry.available(gender)) available.add(entry);
            available.sort(Comparator.comparingInt((Entry entry) -> entry.id() < 0 ? 0 : 1)
                    .thenComparing(entry -> entry.name(strings)));
            if (available.isEmpty()) throw new IllegalStateException("Missing appearance options for " + slot);
            choices.put(slot, List.copyOf(available));
            // 参考新角色随机化：脸部始终从“无”开始。
            Entry entry = available.get(slot == Slot.FACE ? 0 : random.nextInt(available.size()));
            selected.put(slot, entry);
            colors.put(slot, entry.colorable() ? random.nextInt(AppearancePalette.COLORS.length) : 0);
            // 参考 q9：重建下拉项时，各款式记下该栏当时的颜色。
            optionColors.put(slot, colors.get(slot));
        }
    }
    void select(Slot slot, int index) {
        Entry entry = choices(slot).get(index);
        selected.put(slot, entry);
        if (!entry.colorable()) colors.put(slot, entry.id() < 0 ? -1 : 0);
        else colors.put(slot, optionColors.get(slot) < 0
                ? ThreadLocalRandom.current().nextInt(AppearancePalette.COLORS.length) : optionColors.get(slot));
    }

    private List<Entry> read(FileHandle file) {
        try {
            byte[] bytes = file.readBytes();
            if (bytes.length < 22) throw new IllegalArgumentException("Truncated addons.pak");
            var hash = MessageDigest.getInstance("SHA-1");
            hash.update(bytes, 0, bytes.length - 20);
            if (!Arrays.equals(hash.digest(), Arrays.copyOfRange(bytes, bytes.length - 20, bytes.length)))
                throw new IllegalArgumentException("Invalid addons.pak checksum");
            archive = bytes;
            ByteBuffer data = ByteBuffer.wrap(bytes, 0, bytes.length - 20).slice().order(ByteOrder.LITTLE_ENDIAN);
            data.position(10);
            int skins = data.get();
            for (int skin = 0; skin < skins; skin++)
                for (int format = 0; format < 3; format++) {
                    int count = data.get();
                    for (int frame = 0; frame < count; frame++) {
                        int length = data.getInt();
                        frames.put(new FrameKey(-1, skin, format, 0, frame, 0),
                                new Frame(data.position(), length, -1, 0, 0));
                        skip(data, length);
                    }
                }
            int count = data.getInt();
            List<Entry> result = new ArrayList<>();
            for (int item = 0; item < count; item++) {
                int id = data.getShort(), slot = data.get();
                byte[] name = new byte[data.get()]; data.get(name);
                String fallback = new String(name, StandardCharsets.UTF_8).trim();
                int flags = data.getInt() & -8417;
                for (int table = data.get(); table > 0; table--) skip(data, data.getShort() * 2);
                int rows = data.get();
                for (int animation = data.get(); animation > 0; animation--) {
                    skip(data, 3); skip(data, data.get() & 255);
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
                                if (external) image = new Frame(0, 0, data.getInt() & 255, data.getInt(), data.getInt());
                                else {
                                    int length = data.getInt();
                                    image = new Frame(data.position(), length, -1, 0, 0);
                                    skip(data, length);
                                }
                                frames.put(new FrameKey(slot, id, format, row, frame, target), image);
                            }
                        }
                    }
                }
                if (!fallback.equals("UNUSED")) result.add(new Entry(slot, id, fallback, flags));
            }
            return List.copyOf(result);
        } catch (Exception error) { throw new IllegalArgumentException("Cannot load appearance catalog " + file.path(), error); }
    }
    private static void skip(ByteBuffer data, int length) {
        if (length < 0 || length > data.remaining()) throw new IllegalArgumentException("Invalid appearance data length");
        data.position(data.position() + length);
    }
}
