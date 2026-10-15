package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 火红卡带内嵌的区域地图。LZ77 压缩的 4bpp 图块集（320 个 8x8 图块）+ 30x20 的 16 位图块层
 * + 4 组 16 色调色板，关都、七之岛（三组）各一张。玩家落点来自卡带的 15x22 地点网格
 * （oY/Gt 两层），按地图头里的地区编号索引 —— 与游戏本体一致，不需要额外烘焙地标表。
 */
public final class FireRedRegionMap extends GbaRegionMap {
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

    public static FireRedRegionMap load(byte[] rom) throws IOException {
        FireRedRegionMap map = new FireRedRegionMap(rom);
        map.validate();
        return map;
    }

    private FireRedRegionMap(byte[] rom) {
        super(rom, REGIONS);
    }

    @Override
    protected int gridColumns() {
        return GRID_COLUMNS;
    }

    @Override
    public int pixelX(int column) {
        return (column + 4) * 8 + 4 - CROP_X;
    }

    @Override
    public int pixelY(int row) {
        return (row + 4) * 8 + 4 - CROP_Y;
    }

    /**
     * 校验卡带结构：调色板、图块集、四张图块层与地点网格都必须符合火红 v1.0 的布局，
     * 否则视为不支持的版本。
     */
    @Override
    protected void validate() throws IOException {
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
        java.util.Arrays.fill(merged, -1);
        int filled = 0;
        for (int layer = 0; layer < 2; layer++) {
            for (int cell = 0; cell < columns; cell++) {
                int value = unsigned(base + layer * columns + cell);
                if (value == GRID_EMPTY) continue;
                if (merged[cell] == -1) {
                    merged[cell] = value;
                    filled++;
                }
            }
        }
        label(region, merged);
        return filled;
    }

    /** 按图块层把图块集铺成位图，再裁掉四周留白。索引 0 与越界调色板都视为透明。 */
    @Override
    protected BufferedImage decode(int region) throws IOException {
        byte[] tiles = decompress(TILESET);
        byte[] tilemap = decompress(TILEMAPS[region]);
        int[] palette = new int[64];
        for (int index = 0; index < palette.length; index++) {
            palette[index] = bgr555(shortAt(PALETTE + index * 2));
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
                        if (color == 0) continue;
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
