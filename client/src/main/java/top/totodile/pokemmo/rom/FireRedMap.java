package top.totodile.pokemmo.rom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/** 火红地图资源、公共修正规则及分层像素，像素采用 RGBA8888。 */
public final class FireRedMap {
    private static final byte[] MAP_TABLE_PATTERN = HexFormat.of().parseHex("80180068890B091808687047");
    private final byte[] rom;
    private final int width;
    private final int height;
    private final int[] cells;
    private final int columns;
    private final int rows;
    private final int layoutId;
    private final int locationType;
    private final Tileset primary;
    private final Tileset secondary;
    private final FireRedRules rules;
    private final boolean enhanced;

    private FireRedMap(byte[] rom, int group, int map, boolean enhanced) throws IOException {
        this.rom = rom;
        this.enhanced = enhanced;
        rules = new FireRedRules();
        if (rom.length < 256 || rom[172] != 'B' || rom[173] != 'P' || rom[174] != 'R') {
            throw new IOException("地图预览需要火红 ROM。");
        }
        int table = pointer(findMapTable());
        int groups = 0;
        while (groups < 256 && isPointer(table + groups * 4)) groups++;
        if (group < 0 || group >= groups) throw new IOException("地图组编号超出范围。");
        int bank = pointer(table + group * 4);
        int bankEnd = group + 1 == groups ? table : pointer(table + (group + 1) * 4);
        if (map < 0 || map >= (bankEnd - bank) / 4) throw new IOException("地图编号超出范围。");
        int header = pointer(pointer(table + group * 4) + map * 4);
        int layout = pointer(header);
        int footers = pointer(findPattern(HexFormat.of().parseHex("89000918086802BC08470000")) + 12);
        int id = 1;
        while (id < 1024) {
            int entry = footers + (id - 1) * 4;
            // 空布局仍占据编号，只有非零的无效指针才标记表尾。
            if (integer(entry) != 0) {
                if (!isPointer(entry)) throw new IOException("无法定位地图布局编号。");
                if (pointer(entry) == layout) break;
            }
            id++;
        }
        if (id == 1024) throw new IOException("无法定位地图布局编号。");
        layoutId = id;
        locationType = unsigned(header + 23);
        columns = integer(layout);
        rows = integer(layout + 4);
        if (columns < 1 || rows < 1 || columns > 256 || rows > 256) {
            throw new IOException("无效的地图尺寸。");
        }
        width = columns * 16;
        height = rows * 16;
        cells = new int[columns * rows];
        int cellData = pointer(layout + 12);
        for (int i = 0; i < cells.length; i++) cells[i] = ushort(cellData + i * 2);
        if (enhanced) {
            for (var patch : rules.patches.getOrDefault(layoutId, Map.of()).entrySet()) {
                if (patch.getKey() < 0 || patch.getKey() >= cells.length) throw new IOException("地图修正坐标超出范围。");
                cells[patch.getKey()] = patch.getValue();
            }
        }
        int tilesetTable = pointer(findPattern(HexFormat.of().parseHex("DD0572042800000028000000")) + 20);
        primary = new Tileset(pointer(layout + 16), tilesetTable);
        secondary = new Tileset(pointer(layout + 20), tilesetTable);
    }

    /** 返回底层、普通上层、高处上层、渐亮夜间贴图与固定亮度夜间贴图。 */
    public int[][] layers(int season, long elapsedMillis) throws IOException {
        if (season < 0 || season > 3 || elapsedMillis < 0) throw new IllegalArgumentException("无效的季节或动画时间。");
        // 原始模式沿用静态 ROM 绘制，不应用季节、动画、地图修正或夜间贴图。
        if (!enhanced) {
            season = 1;
            elapsedMillis = 0;
        }
        int[][] pixels = new int[5][width * height];
        long frame = elapsedMillis / rules.intervalMillis;
        int[] primaryColors = primary.palette(season);
        int[] secondaryColors = secondary.palette(season);
        // 一个地图块由两层各四个 8×8 图块组成；高六位保留碰撞和高度信息。
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                int packed = cells[y * columns + x];
                int block = packed & 1023;
                Tileset source = block < primary.count ? primary : secondary;
                int index = source == primary ? block : block - primary.count;
                if (index >= source.count) throw new IOException("地图块编号超出图块集范围。");
                for (int part = 0; part < 8; part++) {
                    int tile = ushort(source.blocks + index * 16 + part * 2);
                    int tileId = tile & 1023;
                    Tileset graphics = tileId < primary.count ? primary : secondary;
                    int localTile = graphics == primary ? tileId : tileId - primary.count;
                    int palette = tile >>> 12;
                    Tileset colors = source == secondary ? (palette >= 6 ? secondary : primary) : graphics;
                    int layer = 0;
                    if (enhanced && part >= 4 && (unsigned(source.attributes + index * 4 + 3) & 32) == 0) {
                        int elevation = (byte) ((byte) ((short) packed >> 10) >> 2) - 1;
                        if (elevation == -8) elevation = 8;
                        layer = elevation >= 4 ? 2 : 1;
                    }
                    Tileset animationSource = source == secondary && tileId <= primary.count ? primary : source;
                    byte[] data = graphics.graphics;
                    int graphicIndex = localTile;
                    for (AnimatedTiles animation : animationSource.animations.values()) {
                        if (tileId >= animation.first && tileId < animation.first + animation.count) {
                            data = animation.frames[(int) (frame % animation.frames.length)];
                            graphicIndex = tileId - animation.first;
                            break;
                        }
                    }
                    drawTile(x * 16 + (part % 2) * 8, y * 16 + (part % 4 / 2) * 8,
                            data, graphicIndex, colors == primary ? primaryColors : secondaryColors, tile, pixels[layer]);
                    int overlayTile = tileId > primary.count ? tileId - primary.count : tileId;
                    int overlayTileset = tileId > primary.count ? secondary.id : primary.id;
                    FireRedRules.Overlay overlay = enhanced ? rules.overlays.get(overlayTile * 65536 + overlayTileset) : null;
                    if (overlay != null) drawOverlay(x * 16 + (part % 2) * 8,
                            y * 16 + (part % 4 / 2) * 8, tile, overlay, pixels[overlay.layer() == 0 ? 3 : 4]);
                }
            }
        }
        return pixels;
    }

    /** 关都初始房间：地图组 4、地图 1，出生格为 (6, 6)。 */
    public static FireRedMap loadBirthRoom(Path file) throws IOException {
        return load(file, 4, 1);
    }

    public static FireRedMap load(Path file, int group, int map) throws IOException {
        return load(file, group, map, true);
    }

    public static FireRedMap load(Path file, int group, int map, boolean enhanced) throws IOException {
        long size = Files.size(file);
        if (size < 256 || size > 32 * 1024 * 1024) throw new IOException("无效的 GBA ROM 大小。");
        return new FireRedMap(Files.readAllBytes(file), group, map, enhanced);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int[] pixels() throws IOException {
        int[][] layers = layers(1, 0);
        int[] result = new int[width * height];
        Arrays.fill(result, 255);
        for (int layer = 0; layer < 3; layer++) {
            for (int i = 0; i < result.length; i++) if ((layers[layer][i] & 255) != 0) result[i] = layers[layer][i];
        }
        return result;
    }

    public int layoutId() {
        return layoutId;
    }

    public float[] tint(int hour, int minute) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) throw new IllegalArgumentException("无效的地图时间。");
        if (!enhanced) return new float[]{1, 1, 1, 0};
        if (locationType == 5) return new float[]{0.7f, 0.7f, 0.85f, 0};
        int darkness = rules.darkness(locationType, hour, minute);
        return new float[]{1 - darkness / rules.redGreenDivisor / 255f,
                1 - darkness / rules.redGreenDivisor / 255f,
                1 - darkness / rules.blueDivisor / 255f, darkness / 255f};
    }

    public int animationIntervalMillis() {
        return rules.intervalMillis;
    }

    public boolean animated() {
        return enhanced && (!primary.animations.isEmpty() || !secondary.animations.isEmpty());
    }

    private void drawTile(int left, int top, byte[] graphics, int tile, int[] palette, int flags, int[] pixels) throws IOException {
        int start = tile * 32;
        if (start < 0) throw new IOException("无效的图块像素编号。");
        // 图块集允许引用未存储的空白图块，读取时补零而不是跨越像素资源。
        if (start >= graphics.length) return;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int sourceX = (flags & 1024) == 0 ? x : 7 - x;
                int sourceY = (flags & 2048) == 0 ? y : 7 - y;
                int packed = graphics[start + sourceY * 4 + sourceX / 2] & 255;
                int colorIndex = packed >>> ((sourceX & 1) * 4) & 15;
                // 每个调色板的第零色透明，让上层图块露出底层。
                if (colorIndex == 0) continue;
                pixels[(top + y) * width + left + x] = palette[(flags >>> 12) * 16 + colorIndex];
            }
        }
    }

    private int findMapTable() throws IOException {
        // 通过 ARM 指令特征定位地图表指针，避免把特定语言或修订版的地址写死。
        return findPattern(MAP_TABLE_PATTERN) + MAP_TABLE_PATTERN.length;
    }

    private int findPattern(byte[] pattern) throws IOException {
        for (int offset = 0; offset <= rom.length - pattern.length - 4; offset++) {
            int index = 0;
            while (index < pattern.length && rom[offset + index] == pattern[index]) index++;
            if (index == pattern.length) return offset;
        }
        throw new IOException("无法定位火红地图数据表。");
    }

    private boolean isPointer(int offset) throws IOException {
        long physical = Integer.toUnsignedLong(integer(offset)) - 0x08000000L;
        return physical >= 0 && physical < rom.length;
    }

    private void drawOverlay(int left, int top, int flags, FireRedRules.Overlay overlay, int[] pixels) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int sx = (flags & 1024) == 0 ? x : 7 - x;
                int sy = (flags & 2048) == 0 ? y : 7 - y;
                int color = overlay.pixels()[sy * 8 + sx];
                if ((color & 255) != 0) pixels[(top + y) * width + left + x] = color;
            }
        }
    }

    private int pointer(int offset) throws IOException {
        long address = Integer.toUnsignedLong(integer(offset));
        long physical = address - 0x08000000L;
        if (physical < 0 || physical >= rom.length) throw new IOException("无效的 GBA 资源指针。");
        return (int) physical;
    }

    private int integer(int offset) throws IOException {
        return ushort(offset) | ushort(offset + 2) << 16;
    }

    private int ushort(int offset) throws IOException {
        return unsigned(offset) | unsigned(offset + 1) << 8;
    }

    private int unsigned(int offset) throws IOException {
        if (offset < 0 || offset >= rom.length) throw new IOException("GBA 资源被截断。");
        return rom[offset] & 255;
    }

    private byte[] graphics(int offset, boolean compressed) throws IOException {
        if (!compressed) {
            if (offset > rom.length - 1024 * 32) throw new IOException("图块资源被截断。");
            return Arrays.copyOfRange(rom, offset, offset + 1024 * 32);
        }
        if (unsigned(offset) != 0x10) throw new IOException("无效的 LZ77 图块数据。");
        int length = integer(offset) >>> 8;
        if (length < 32 || length > 1024 * 32 || length % 32 != 0) {
            throw new IOException("无效的图块解压长度。");
        }
        byte[] result = new byte[length];
        int input = offset + 4;
        int output = 0;
        while (output < length) {
            int flags = unsigned(input++);
            for (int bit = 7; bit >= 0 && output < length; bit--) {
                if ((flags & 1 << bit) == 0) {
                    result[output++] = (byte) unsigned(input++);
                } else {
                    int first = unsigned(input++);
                    int second = unsigned(input++);
                    int count = (first >>> 4) + 3;
                    int distance = ((first & 15) << 8 | second) + 1;
                    if (distance > output || count > length - output) throw new IOException("无效的 LZ77 回溯段。");
                    for (int i = 0; i < count; i++) {
                        result[output] = result[output - distance];
                        output++;
                    }
                }
            }
        }
        return result;
    }

    private final class Tileset {
        private final int id;
        private final int count;
        private final int blocks;
        private final int attributes;
        private final int palette;
        private final byte[] graphics;
        private final Map<Integer, int[]> palettes = new HashMap<>();
        private final Map<Integer, AnimatedTiles> animations = new HashMap<>();

        private Tileset(int header, int table) throws IOException {
            id = (header - table) / 24;
            if (id < 0 || id > 99 || (header - table) % 24 != 0) throw new IOException("无效的图块集编号。");
            blocks = pointer(header + 12);
            attributes = pointer(header + 20);
            count = (attributes - blocks) / 16;
            if (count < 1 || count > 640 || (attributes - blocks) % 16 != 0) {
                throw new IOException("无效的火红图块集。");
            }
            palette = pointer(header + 8);
            graphics = graphics(pointer(header + 4), unsigned(header) != 0);
            for (FireRedRules.Animation definition : rules.animations) {
                if (!enhanced || definition.tileset() != id) continue;
                int tableOffset = pointer(pointer(header + 16) + definition.pointerOffset());
                byte[][] frames = new byte[definition.frames()][definition.count() * 32];
                for (int frame = 0; frame < frames.length; frame++) {
                    int start = pointer(tableOffset + frame * 4);
                    if (start > rom.length - frames[frame].length) throw new IOException("地图动画资源被截断。");
                    System.arraycopy(rom, start, frames[frame], 0, frames[frame].length);
                }
                animations.put(definition.first(), new AnimatedTiles(definition.first(), definition.count(), frames));
            }
        }

        private int[] palette(int season) throws IOException {
            int[] cached = palettes.get(season);
            if (cached != null) return cached;
            int[] result = new int[256];
            for (int i = 0; i < result.length; i++) {
                int color = ushort(palette + i * 2);
                int rgba = (color & 31) * 8 << 24 | (color >>> 5 & 31) * 8 << 16
                        | (color >>> 10 & 31) * 8 << 8 | 255;
                result[i] = i % 16 == 0 ? 0 : season == 1 ? rgba
                        : rules.colors.getOrDefault(new FireRedRules.ColorKey(season, id, i / 16, i % 16), rgba);
            }
            palettes.put(season, result);
            return result;
        }
    }

    private record AnimatedTiles(int first, int count, byte[][] frames) {
    }
}
