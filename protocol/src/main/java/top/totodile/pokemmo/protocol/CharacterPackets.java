package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 角色请求只携带外观，所属账号由已认证的游戏会话确定。 */
public final class CharacterPackets {
    public static final int LIMIT = 3;
    public static final int OUTFIT = 0x84;
    public enum Status { SUCCESS, LIMIT_REACHED, NAME_TAKEN, INVALID, SYSTEM_ERROR }
    public record Appearance(String name, int gender, int skin, int region, int[] items, int[] colors) {
        public Appearance {
            items = items.clone();
            colors = colors.clone();
            if (items.length != 12 || colors.length != 12) throw new IllegalArgumentException("Invalid appearance length");
        }
        @Override public int[] items() { return items.clone(); }
        @Override public int[] colors() { return colors.clone(); }
        public boolean valid() {
            if (name == null || name.length() > 16 || !name.matches("[\\p{L}0-9_]{2,16}") || gender < 0 || gender > 1
                    || skin < 0 || skin > 4 || region < 0 || region > 5) return false;
            for (int i = 0; i < 12; i++) {
                if (items[i] < -1 || items[i] > 1022 || colors[i] < 0 || colors[i] >= 48) return false;
            }
            return true;
        }
    }
    public record ReturnPoint(int group, int map, int x, int y, int facing) {
        public boolean valid() { return valid(0) || valid(1) || valid(2) || valid(3) || valid(4) || valid(5); }
        public boolean valid(int region) { return FireRedMaps.contains(region, group, map, x, y) && facing >= 0 && facing < 4; }
    }
    public record Position(int group, int map, int x, int y, int elevation, int facing, ReturnPoint returnPoint) {
        public Position(int group, int map, int x, int y, int elevation, int facing) {
            this(group, map, x, y, elevation, facing, null);
        }
        public static Position spawn() { return new Position(4, 1, 6, 6, 3, 0); }
        public static Position spawn(int region, int gender) {
            if (region == 0) return spawn();
            if (region == 5) return new Position(0, 64, 6, 6, 0, 0);
            if (region == 3) return new Position(1, 157, 6, 6, 0, 0);
            if (region == 2) return new Position(1, 135, 6, 6, 0, 0);
            if (region != 1 && region != 4) throw new IllegalArgumentException("Invalid starting region");
            return new Position(1, gender == 0 ? 1 : 3, 4, 4, 3, 0);
        }
        public boolean valid() { return valid(0) || valid(1) || valid(2) || valid(3) || valid(4) || valid(5); }
        public boolean valid(int region) {
            return FireRedMaps.contains(region, group, map, x, y) && elevation >= -2 && elevation <= 15
                    && facing >= 0 && facing < 4 && (returnPoint == null || returnPoint.valid(region));
        }
    }
    public record CharacterInfo(long id, Appearance appearance, Position position, long createdAt, boolean dexUnlocked, TrainerStats stats, List<PartyMember> party, boolean cycling, boolean noclip, int speedMultiplier, boolean wildEncounters) {
        public CharacterInfo(long id, Appearance appearance, Position position, long createdAt, boolean dexUnlocked,
                             TrainerStats stats, List<PartyMember> party, boolean cycling, boolean noclip, int speedMultiplier) {
            this(id, appearance, position, createdAt, dexUnlocked, stats, party, cycling, noclip, speedMultiplier, true);
        }
        public CharacterInfo(long id, Appearance appearance, Position position, long createdAt, boolean dexUnlocked, TrainerStats stats, List<PartyMember> party, boolean cycling) {
            this(id, appearance, position, createdAt, dexUnlocked, stats, party, cycling, false, 1);
        }
        public CharacterInfo(long id, Appearance appearance, Position position, long createdAt, boolean dexUnlocked, TrainerStats stats, List<PartyMember> party) {
            this(id, appearance, position, createdAt, dexUnlocked, stats, party, false);
        }
        public CharacterInfo {
            party = PartyMember.validate(party);
            if (speedMultiplier != 1 && speedMultiplier != 2 && speedMultiplier != 4 && speedMultiplier != 8) throw new IllegalArgumentException("Invalid character speed");
        }
        public CharacterInfo(long id, Appearance appearance, Position position, long createdAt, boolean dexUnlocked, TrainerStats stats) {
            this(id, appearance, position, createdAt, dexUnlocked, stats, List.of());
        }
        public CharacterInfo(long id, Appearance appearance, Position position, long createdAt, boolean dexUnlocked) {
            this(id, appearance, position, createdAt, dexUnlocked, TrainerStats.initial(appearance.region(), dexUnlocked));
        }
        public CharacterInfo(long id, Appearance appearance, Position position, long createdAt) { this(id, appearance, position, createdAt, false); }
        public CharacterInfo(long id, Appearance appearance, Position position) { this(id, appearance, position, 0); }
        public CharacterInfo(long id, Appearance appearance) { this(id, appearance, Position.spawn(appearance.region(), appearance.gender())); }
    }
    public static byte[] positionRequest(Position p) {
        PacketData data = new PacketData(32).putByte(WorldPackets.SAVE);
        writePosition(data, p);
        return data.written();
    }
    public static Position decodePosition(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != WorldPackets.SAVE) throw new IOException("Unexpected position packet");
        Position p = readPosition(data);
        data.end();
        return p;
    }
    static void writePosition(PacketData d, Position p) {
        d.putByte(p.group()).putByte(p.map()).putShort(p.x()).putShort(p.y()).putByte(p.elevation()).putByte(p.facing());
        ReturnPoint r = p.returnPoint();
        d.putByte(r == null ? 0 : 1);
        if (r != null) d.putByte(r.group()).putByte(r.map()).putShort(r.x()).putShort(r.y()).putByte(r.facing());
    }
    static Position readPosition(PacketData d) throws IOException {
        Position p = new Position(d.byteValue(), d.byteValue(), d.shortValue(), d.shortValue(), (byte)d.byteValue(), d.byteValue());
        int hasReturn = d.byteValue();
        if (hasReturn > 1) throw new IOException("Invalid return point flag");
        if (hasReturn == 1) p = new Position(p.group(), p.map(), p.x(), p.y(), p.elevation(), p.facing(),
                new ReturnPoint(d.byteValue(), d.byteValue(), d.shortValue(), d.shortValue(), d.byteValue()));
        if (!p.valid()) throw new IOException("Invalid world position");
        return p;
    }
    public record CreationResult(Status status, List<CharacterInfo> characters) {
        public CreationResult { characters = List.copyOf(characters); }
    }
    private CharacterPackets() { }

    public static byte[] leaveResponse(List<CharacterInfo> characters) {
        byte[] packet = listResponse(characters);
        packet[0] = 0x78;
        return packet;
    }

    public static List<CharacterInfo> decodeLeft(byte[] packet) throws IOException {
        if (packet.length == 0 || packet[0] != 0x78) throw new IOException("Unexpected world logout response");
        byte[] list = packet.clone();
        list[0] = 2;
        return decodeList(list);
    }

    public static byte[] enterRequest(long id) { return new PacketData(17).putByte(4).putLong(id).putLong(CharacterSignature.sigHash(Long.hashCode(id))).written(); }
    public static long decodeEnter(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 4) throw new IOException("Unexpected enter request");
        long id = data.longValue();
        if (data.longValue() != CharacterSignature.sigHash(Long.hashCode(id))) throw new IOException("Invalid character signature");
        data.end();
        return id;
    }
    public static byte[] enterResponse(List<CharacterInfo> characters) {
        byte[] packet = listResponse(characters);
        packet[0] = 5;
        return packet;
    }
    public static CharacterInfo decodeEntered(byte[] packet) throws IOException {
        if (packet.length == 0 || packet[0] != 5) throw new IOException("Unexpected enter response");
        byte[] copy = packet.clone();
        copy[0] = 2;
        List<CharacterInfo> characters = decodeList(copy);
        if (characters.size() != 1) throw new IOException("Character entry rejected");
        return characters.getFirst();
    }

    public static byte[] outfitRequest(Appearance appearance) {
        byte[] packet = createRequest(appearance);
        packet[0] = (byte) OUTFIT;
        return packet;
    }
    public static Appearance decodeOutfit(byte[] packet) throws IOException {
        if (packet.length == 0 || (packet[0] & 255) != OUTFIT) throw new IOException("Unexpected outfit request");
        byte[] copy = packet.clone();
        copy[0] = 3;
        return decodeCreate(copy);
    }
    public static byte[] outfitResponse(CreationResult result) {
        byte[] packet = createResponse(result);
        packet[0] = (byte) OUTFIT;
        return packet;
    }
    public static CreationResult decodeOutfitResult(byte[] packet) throws IOException {
        if (packet.length == 0 || (packet[0] & 255) != OUTFIT) throw new IOException("Unexpected outfit result");
        byte[] copy = packet.clone();
        copy[0] = 3;
        return decodeResult(copy);
    }

    public static byte[] listRequest() { return new byte[]{2}; }
    public static byte[] createRequest(Appearance appearance) {
        PacketData data = new PacketData(256).putByte(3);
        data.putString(appearance.name()).putByte(appearance.gender()).putByte(appearance.region()).putByte(appearance.skin()).putShort(0xFFF);
        for (int i = 0; i < 12; i++) data.putShort((appearance.items[i] & 1023) | appearance.colors[i] << 10);
        return data.written();
    }
    public static Appearance decodeCreate(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 3) throw new IOException("Unexpected character request");
        String name = data.string(16);
        int gender = data.byteValue(), region = data.byteValue(), skin = data.byteValue(), mask = data.shortValue();
        if ((mask & ~0xFFF) != 0) throw new IOException("Invalid appearance mask");
        int[] items = {-1,-1,0,0,-1,-1,0,-1,0,0,0,0}, colors = new int[12];
        for (int i = 0; i < 12; i++) if ((mask & 1 << i) != 0) {
            int packed = data.shortValue();
            items[i] = (packed & 1023) == 1023 ? -1 : packed & 1023;
            colors[i] = packed >>> 10;
        }
        Appearance appearance = new Appearance(name, gender, skin, region, items, colors);
        data.end();
        return appearance;
    }
    public static byte[] listResponse(List<CharacterInfo> characters) {
        PacketData data = new PacketData(24576).putByte(2);
        writeList(data, characters);
        return data.written();
    }
    public static List<CharacterInfo> decodeList(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 2) throw new IOException("Unexpected character list");
        List<CharacterInfo> result = readList(data);
        data.end();
        return result;
    }
    public static byte[] createResponse(CreationResult result) {
        PacketData data = new PacketData(24576).putByte(3).putByte(result.status().ordinal());
        writeList(data, result.characters());
        return data.written();
    }
    public static CreationResult decodeResult(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 3) throw new IOException("Unexpected creation result");
        int status = data.byteValue();
        if (status >= Status.values().length) throw new IOException("Invalid creation status");
        CreationResult result = new CreationResult(Status.values()[status], readList(data));
        data.end();
        return result;
    }
    public static byte[] deleteRequest(long id) {
        return new PacketData(9).putByte(0x71).putLong(id).written();
    }
    public static long decodeDelete(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 0x71) throw new IOException("Unexpected deletion request");
        long id = data.longValue();
        data.end();
        if (id <= 0) throw new IOException("Invalid character ID");
        return id;
    }
    public static byte[] deleteResponse(CreationResult result) {
        byte[] packet = createResponse(result);
        packet[0] = 0x71;
        return packet;
    }
    public static CreationResult decodeDeleteResult(byte[] packet) throws IOException {
        if (packet.length == 0 || packet[0] != 0x71) throw new IOException("Unexpected deletion result");
        byte[] copy = packet.clone();
        copy[0] = 3;
        return decodeResult(copy);
    }
    private static void writeAppearance(PacketData data, Appearance appearance) {
        data.putString(appearance.name()).putByte(appearance.gender()).putByte(appearance.skin()).putByte(appearance.region());
        for (int i : appearance.items) data.putShort(i);
        for (int i : appearance.colors) data.putByte(i);
    }
    private static Appearance readAppearance(PacketData data) throws IOException {
        String name = data.string(16);
        int gender = data.byteValue(), skin = data.byteValue(), region = data.byteValue();
        int[] items = new int[12], colors = new int[12];
        for (int i = 0; i < 12; i++) items[i] = (short) data.shortValue();
        for (int i = 0; i < 12; i++) colors[i] = data.byteValue();
        return new Appearance(name, gender, skin, region, items, colors);
    }
    private static void writeList(PacketData data, List<CharacterInfo> characters) {
        if (characters.size() > LIMIT) throw new IllegalArgumentException("Too many characters");
        data.putByte(characters.size());
        for (CharacterInfo character : characters) {
            data.putLong(character.id());
            writeAppearance(data, character.appearance());
            writePosition(data, character.position());
            data.putLong(character.createdAt());
            data.putByte(character.dexUnlocked() ? 1 : 0);
            TrainerStats.write(data, character.stats());
            PartyMember.write(data, character.party());
            data.putByte(character.cycling() ? 1 : 0);
            data.putByte(character.noclip() ? 1 : 0).putByte(character.speedMultiplier()).putByte(character.wildEncounters() ? 1 : 0);
        }
    }
    private static List<CharacterInfo> readList(PacketData data) throws IOException {
        int count = data.byteValue();
        if (count > LIMIT) throw new IOException("Too many characters");
        List<CharacterInfo> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long id = data.longValue();
            Appearance appearance = readAppearance(data);
            if (id <= 0 || !appearance.valid()) throw new IOException("Invalid character record");
            Position position = readPosition(data);
            if (!position.valid(appearance.region())) throw new IOException("Position outside character region");
            long createdAt = data.longValue();
            if (createdAt < 0) throw new IOException("Invalid character creation time");
            int dexUnlocked = data.byteValue();
            if (dexUnlocked > 1) throw new IOException("Invalid Pokedex state");
            result.add(new CharacterInfo(id, appearance, position, createdAt, dexUnlocked == 1, TrainerStats.read(data), PartyMember.read(data), data.booleanValue(), data.booleanValue(), data.byteValue(), data.booleanValue()));
        }
        return List.copyOf(result);
    }
}
