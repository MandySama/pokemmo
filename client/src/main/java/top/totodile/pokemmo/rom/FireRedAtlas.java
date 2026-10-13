package top.totodile.pokemmo.rom;

import java.io.IOException;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 一次读取卡带，索引地图尺寸、室外连接及门和楼梯传送事件。 */
public final class FireRedAtlas {
    public record Key(int group, int map) {
        @Override
        public String toString() {
            return group + "/" + map;
        }
    }

    public record Warp(int x, int y, int elevation, int targetWarp, Key target) {
    }

    public record Connection(int direction, int offset, Key target) {
    }

    public record Npc(int id, int graphics, int x, int y, int elevation, int movement, int flag) {
        /** 0xF0 起是剧情变量槽位，不能直接作为 ROM 精灵表下标。 */
        public boolean hasStaticGraphics() {
            return graphics < 0xF0;
        }

        public int direction() {
            return switch (movement) { case 7 -> 1; case 9 -> 2; case 10 -> 3; default -> 0; };
        }
    }

    public record Area(Key key, int columns, int rows, int type, List<Warp> warps, List<Connection> connections) {
    }

    private final byte[] rom;
    private final Map<Key, Area> areas = new LinkedHashMap<>();
    private final Map<Key, List<Npc>> npcs = new LinkedHashMap<>();
    private FireRedNpcSprites npcSprites;
    private HgssNpcSprites replacementSprites;
    private boolean replacementsLoaded;
    private final boolean useHgssSprites = new top.totodile.pokemmo.ClientSettings().hgssSprites();

    public record NpcSprite(BufferedImage image, float scale) {
    }

    public FireRedAtlas(Path file) throws IOException {
        long size = Files.size(file);
        if (size < 256 || size > 32 * 1024 * 1024) throw new IOException("Invalid GBA ROM size");
        rom = Files.readAllBytes(file);
        if (rom[0xAC] != 'B' || rom[0xAD] != 'P' || rom[0xAE] != 'R') throw new IOException("FireRed ROM required");
        byte[] pattern = HexFormat.of().parseHex("80180068890B091808687047");
        int table = pointer(find(pattern) + pattern.length);
        int count = 0;
        while (count < 256 && isPointer(table + count * 4)) count++;
        if (count == 0) throw new IOException("Empty map table");
        for (int group = 0; group < count; group++) {
            int bank = pointer(table + group * 4);
            int end = group + 1 == count ? table : pointer(table + (group + 1) * 4);
            if (end < bank || (end - bank) % 4 != 0 || end - bank > 1024) throw new IOException("Invalid map bank");
            for (int map = 0; map < (end - bank) / 4; map++) {
                Key key = new Key(group, map);
                int header = pointer(bank + map * 4);
                int layout = pointer(header);
                int columns = integer(layout), rows = integer(layout + 4);
                if (columns < 1 || rows < 1 || columns > 256 || rows > 256) throw new IOException("Invalid map dimensions");
                List<Warp> warps = new ArrayList<>();
                List<Npc> objects = new ArrayList<>();
                if (isPointer(header + 4)) {
                    int events = pointer(header + 4);
                    int objectCount = unsigned(events);
                    if (objectCount > 0) {
                        int entries = pointer(events + 4);
                        for (int i = 0; i < objectCount; i++) {
                            int entry = entries + i * 24;
                            objects.add(new Npc(unsigned(entry), unsigned(entry + 1), ushort(entry + 4),
                                    ushort(entry + 6), unsigned(entry + 8), unsigned(entry + 9), ushort(entry + 20)));
                        }
                    }
                    int length = unsigned(events + 1);
                    if (length > 0) {
                        int entries = pointer(events + 8);
                        for (int i = 0; i < length; i++) {
                            int entry = entries + i * 8;
                            warps.add(new Warp(ushort(entry), ushort(entry + 2), unsigned(entry + 4),
                                    unsigned(entry + 5), new Key(unsigned(entry + 7), unsigned(entry + 6))));
                        }
                    }
                }
                List<Connection> connections = new ArrayList<>();
                if (isPointer(header + 12)) {
                    int data = pointer(header + 12);
                    int length = integer(data);
                    if (length < 0 || length > 32) throw new IOException("Invalid connection count");
                    if (length > 0) {
                        int entries = pointer(data + 4);
                        for (int i = 0; i < length; i++) {
                            int entry = entries + i * 12;
                            connections.add(new Connection(integer(entry), integer(entry + 4),
                                    new Key(unsigned(entry + 8), unsigned(entry + 9))));
                        }
                    }
                }
                areas.put(key, new Area(key, columns, rows, unsigned(header + 23), List.copyOf(warps), List.copyOf(connections)));
                npcs.put(key, List.copyOf(objects));
            }
        }
    }

    public List<Area> areas() {
        return List.copyOf(areas.values());
    }

    public Area area(Key key) throws IOException {
        Area area = areas.get(key);
        if (area == null) throw new IOException("Unknown map " + key);
        return area;
    }

    public FireRedMap load(Key key, boolean enhanced) throws IOException {
        area(key);
        return FireRedMap.load(rom, key.group(), key.map(), enhanced);
    }

    public List<Npc> npcs(Key key) throws IOException {
        area(key);
        return npcs.get(key);
    }

    public BufferedImage npcImage(Npc npc) throws IOException {
        return npcSprite(npc, true, 1).image();
    }

    /** 原始模式使用卡带像素；替换图像保留原画布，以独立比例保持脚底锚点。 */
    public NpcSprite npcSprite(Npc npc, boolean enhanced, int season) throws IOException {
        if (enhanced && useHgssSprites && !replacementsLoaded) {
            Path selected = new top.totodile.pokemmo.RomFiles(top.totodile.pokemmo.ClientFiles.data("../roms")).selected(4);
            if (selected != null) replacementSprites = new HgssNpcSprites(selected);
            replacementsLoaded = true;
        }
        if (enhanced && useHgssSprites && replacementSprites != null) {
            BufferedImage image = replacementSprites.image(npc.graphics(), Math.min(npc.direction(), 2));
            if (image != null) return new NpcSprite(image, 0.75f);
        }
        if (npcSprites == null) npcSprites = new FireRedNpcSprites(rom);
        return new NpcSprite(npcSprites.image(npc.graphics(), Math.min(npc.direction(), 2), season, enhanced), 1);
    }

    private int find(byte[] pattern) throws IOException {
        outer: for (int offset = 0; offset <= rom.length - pattern.length - 4; offset++) {
            for (int i = 0; i < pattern.length; i++) if (rom[offset + i] != pattern[i]) continue outer;
            return offset;
        }
        throw new IOException("Map table not found");
    }

    private int unsigned(int offset) throws IOException {
        if (offset < 0 || offset >= rom.length) throw new IOException("ROM offset out of range");
        return rom[offset] & 255;
    }

    private int ushort(int offset) throws IOException {
        return unsigned(offset) | unsigned(offset + 1) << 8;
    }

    private int integer(int offset) throws IOException {
        return ushort(offset) | ushort(offset + 2) << 16;
    }

    private boolean isPointer(int offset) throws IOException {
        int value = integer(offset);
        return value >= 0x08000000 && value < 0x08000000 + rom.length;
    }

    private int pointer(int offset) throws IOException {
        if (!isPointer(offset)) throw new IOException("Invalid ROM pointer");
        return integer(offset) - 0x08000000;
    }
}
