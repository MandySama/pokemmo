package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 卡带内嵌的区域地图（城镇地图）。区域图不是玩法地图的拼接，而是卡带里的一幅专用手绘位图：
 * LZ77 压缩的 4bpp 图块集（320 个 8x8 图块）+ 30x20 的图块层 + 4 组 16 色 BGR555 调色板，
 * 关都、七之岛（三组）各一张。玩家落点来自卡带的 15x22 地点网格（oY/Gt 两层），
 * 按地图头里的地区编号索引 —— 与游戏本体一致，不需要额外烘焙地标表。
 *
 * <p>数据位置按火红 v1.0（BPRE rev 0）的固定地址定位，并用结构特征逐项校验；
 * 校验失败说明不是该版本卡带，此时调用方应退回其它渲染方式。
 */
public final class FireRedRegionMap {
    /** 大区数量：0 关都，1..3 七之岛。 */
    public static final int REGIONS = 4;
    /** 位图中实际地图区域的裁剪矩形，去掉四周用于对话框的留白。 */
    private static final int CROP_X = 24, CROP_Y = 16, CROP_WIDTH = 192, CROP_HEIGHT = 144;
    /** 图块层尺寸（格）。 */
    private static final int TILES_X = 30, TILES_Y = 20;
    /** 图块集解压后长度：320 个 4bpp 图块。 */
    private static final int TILESET_BYTES = 320 * 32;
    /** 图块层解压后长度：30x20 个 16 位条目。 */
    private static final int TILEMAP_BYTES = TILES_X * TILES_Y * 2;
    /** 地点网格尺寸（列 x 行）。 */
    private static final int GRID_COLUMNS = 22, GRID_ROWS = 15;
    /** 地点网格里表示空白的哨兵值。 */
    private static final int GRID_EMPTY = 197;

    /** 调色板基址（4 组 x 16 色）。 */
    private static final int PALETTE = 0x3EF2DC;
    /** 图块集基址（LZ77）。 */
    private static final int TILESET = 0x3EF61C;
    /** 四张大区图的图块层基址（LZ77），顺序与 REGIONS 一致。 */
    private static final int[] TILEMAPS = {0x3F089C, 0x3F0AFC, 0x3F0C0C, 0x3F0CF0};
    /** 地点网格基址：4 区 x（oY 330 字节 + Gt 330 字节）。 */
    private static final int GRID = 0x3F2490;

    private final byte[] rom;
    private final int[][] labels = new int[REGIONS][];
    private final BufferedImage[] images = new BufferedImage[REGIONS];

    private FireRedRegionMap(byte[] rom) {
        this.rom = rom;
    }

    /**
     * 解析卡带里的区域图；不是火红 v1.0 或数据校验不通过时抛异常，由调用方决定回退策略。
     */
    public static FireRedRegionMap load(byte[] rom) throws IOException {
        FireRedRegionMap map = new FireRedRegionMap(rom);
        map.validate();
        return map;
    }

    /**
     * 取大区图（已裁剪为 192x144），按需解码并缓存。
     *
     * @param region 0 关都，1..3 七之岛
     */
    public BufferedImage image(int region) throws IOException {
        if (region < 0 || region >= REGIONS) throw new IOException("Unknown region map " + region);
        if (images[region] == null) images[region] = decode(region);
        return images[region];
    }

    /**
     * 按地图头的地区编号求落点：返回 {大区, 列, 行}，没有登记返回 null。
     * 先查主网格再查补充网格，与游戏本体的查表顺序一致。
     */
    public int[] position(int section) {
        if (section <= 0) return null;
        for (int region = 0; region < REGIONS; region++) {
            int[] labels = this.labels[region];
            for (int cell = 0; cell < labels.length; cell++) {
                if (labels[cell] == section) {
                    return new int[]{region, cell % GRID_COLUMNS, cell / GRID_COLUMNS};
                }
            }
        }
        return null;
    }

    /** 落点换算成裁剪后位图上的像素（格心），供窗口绘制光标使用。 */
    public static int pixelX(int column) {
        return (column + 4) * 8 + 4 - CROP_X;
    }

    public static int pixelY(int row) {
        return (row + 4) * 8 + 4 - CROP_Y;
    }

    /**
     * 校验卡带结构：调色板、图块集、四张图块层与地点网格都必须符合火红 v1.0 的布局，
     * 否则视为不支持的版本。
     */
    private void validate() throws IOException {
        for (int region = 0; region < REGIONS; region++) {
            if (readGrid(region) < 10) throw new IOException("Region map labels not found");
        }
        // 关都网格里的 88..98 恰好是 11 座城镇，顺带验证了地址与空白哨兵值。
        for (int section = 88; section <= 98; section++) {
            if (position(section) == null) throw new IOException("Region map labels not found");
        }
        if (shortAt(PALETTE) != 0) throw new IOException("Region map palette not found");
        if (lz77Size(TILESET) != TILESET_BYTES) throw new IOException("Region map tileset not found");
        for (int offset : TILEMAPS) {
            if (lz77Size(offset) != TILEMAP_BYTES) throw new IOException("Region map tilemap not found");
        }
    }

    /** 读取一区的 oY/Gt 两层网格合并成一张，返回非空格数量。 */
    private int readGrid(int region) throws IOException {
        int columns = GRID_COLUMNS * GRID_ROWS;
        int base = GRID + region * columns * 2;
        int[] merged = new int[columns];
        int filled = 0;
        for (int layer = 0; layer < 2; layer++) {
            for (int cell = 0; cell < columns; cell++) {
                int value = unsigned(base + layer * columns + cell);
                if (value == GRID_EMPTY) continue;
                if (merged[cell] == 0) {
                    merged[cell] = value;
                    filled++;
                }
            }
        }
        labels[region] = merged;
        return filled;
    }

    private int unsigned(int offset) throws IOException {
        if (offset < 0 || offset >= rom.length) throw new IOException("ROM offset out of range");
        return rom[offset] & 255;
    }

    private int shortAt(int offset) throws IOException {
        return unsigned(offset) | unsigned(offset + 1) << 8;
    }

    private int lz77Size(int offset) {
        if (offset < 0 || offset + 4 > rom.length || rom[offset] != 0x10) return -1;
        return (rom[offset + 1] & 255) | (rom[offset + 2] & 255) << 8 | (rom[offset + 3] & 255) << 16;
    }

    /** 解压 LZ77 类型 0x10：标志位按高位先读，回引长度 3..18、位移 1..4096。 */
    private byte[] decompress(int offset) throws IOException {
        int size = lz77Size(offset);
        if (size < 0) throw new IOException("Not a LZ77 block");
        byte[] output = new byte[size];
        int source = offset + 4;
        int written = 0;
        while (written < size) {
            int flags = unsigned(source++);
            for (int bit = 0; bit < 8 && written < size; bit++) {
                if ((flags >> (7 - bit) & 1) == 1) {
                    int first = unsigned(source);
                    int second = unsigned(source + 1);
                    source += 2;
                    int length = (first >> 4) + 3;
                    int start = written - (((first & 15) << 8 | second) + 1);
                    if (start < 0) throw new IOException("Corrupt LZ77 block");
                    for (int index = 0; index < length && written < size; index++) {
                        output[written++] = output[start + index];
                    }
                } else {
                    output[written++] = rom[source++];
                }
            }
        }
        return output;
    }

    /** 按图块层把图块集铺成位图，再裁掉四周留白。索引 0 与越界调色板都视为透明。 */
    private BufferedImage decode(int region) throws IOException {
        byte[] tiles = decompress(TILESET);
        byte[] tilemap = decompress(TILEMAPS[region]);
        int[] palette = new int[64];
        for (int index = 0; index < palette.length; index++) {
            int color = shortAt(PALETTE + index * 2);
            if ((color & 0x7FFF) == 0) continue;
            palette[index] = 0xFF000000 | (color & 31) * 255 / 31 << 16
                    | (color >> 5 & 31) * 255 / 31 << 8 | (color >> 10 & 31) * 255 / 31;
        }
        BufferedImage image = new BufferedImage(CROP_WIDTH, CROP_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[CROP_WIDTH * CROP_HEIGHT];
        for (int row = 0; row < TILES_Y; row++) {
            for (int column = 0; column < TILES_X; column++) {
                int entry = (tilemap[(row * TILES_X + column) * 2] & 255)
                        | (tilemap[(row * TILES_X + column) * 2 + 1] & 255) << 8;
                if (entry == 0 || (entry >> 12 & 15) > 3) continue;
                int tile = (entry & 1023) * 32;
                boolean flipX = (entry & 1024) != 0, flipY = (entry & 2048) != 0;
                int bank = (entry >> 12 & 15) * 16;
                for (int line = 0; line < 8; line++) {
                    for (int pixel = 0; pixel < 8; pixel++) {
                        // 每个字节两个像素，低半字节在左；第 line 行落在字节偏移 line*4。
                        int packed = tiles[tile + line * 4 + (pixel >> 1)] & 255;
                        int color = palette[bank + ((pixel & 1) == 0 ? packed & 15 : packed >> 4)];
                        if ((color >>> 24) == 0) continue;
                        int x = column * 8 + (flipX ? 7 - pixel : pixel) - CROP_X;
                        int y = row * 8 + (flipY ? 7 - line : line) - CROP_Y;
                        if (x < 0 || y < 0 || x >= CROP_WIDTH || y >= CROP_HEIGHT) continue;
                        pixels[y * CROP_WIDTH + x] = color;
                    }
                }
            }
        }
        image.setRGB(0, 0, CROP_WIDTH, CROP_HEIGHT, pixels, 0, CROP_WIDTH);
        return image;
    }
}
