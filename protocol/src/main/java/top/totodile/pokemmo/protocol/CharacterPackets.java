package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 角色请求只携带外观，所属账号由已认证的游戏会话确定。 */
public final class CharacterPackets {
    public static final int LIMIT = 3;
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
                    || skin < 0 || skin > 4 || region != 0) return false;
            for (int i = 0; i < 12; i++) {
                if (items[i] < -1 || items[i] > 1022 || colors[i] < 0 || colors[i] >= 48) return false;
            }
            return true;
        }
    }
    public record CharacterInfo(long id, Appearance appearance) { }
    public record CreationResult(Status status, List<CharacterInfo> characters) {
        public CreationResult { characters = List.copyOf(characters); }
    }
    private CharacterPackets() { }

    public static byte[] listRequest() { return new byte[]{2}; }
    public static byte[] createRequest(Appearance appearance) {
        PacketData data = new PacketData(256).putByte(3);
        writeAppearance(data, appearance);
        return data.written();
    }
    public static Appearance decodeCreate(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 3) throw new IOException("Unexpected character request");
        Appearance appearance = readAppearance(data);
        data.end();
        return appearance;
    }
    public static byte[] listResponse(List<CharacterInfo> characters) {
        PacketData data = new PacketData(2048).putByte(2);
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
        PacketData data = new PacketData(2048).putByte(3).putByte(result.status().ordinal());
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
        return new PacketData(9).putByte(4).putLong(id).written();
    }
    public static long decodeDelete(byte[] packet) throws IOException {
        PacketData data = new PacketData(packet);
        if (data.byteValue() != 4) throw new IOException("Unexpected deletion request");
        long id = data.longValue();
        data.end();
        if (id <= 0) throw new IOException("Invalid character ID");
        return id;
    }
    public static byte[] deleteResponse(CreationResult result) {
        byte[] packet = createResponse(result);
        packet[0] = 4;
        return packet;
    }
    public static CreationResult decodeDeleteResult(byte[] packet) throws IOException {
        if (packet.length == 0 || packet[0] != 4) throw new IOException("Unexpected deletion result");
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
            result.add(new CharacterInfo(id, appearance));
        }
        return List.copyOf(result);
    }
}
