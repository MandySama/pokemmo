package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 绿宝石卡带内嵌的丰缘区域地图。与火红不同：图块集为 8bpp（233 个 8x8 图块，每字节一个
 * 调色板索引），图块层每条目 1 字节、铺在 512x160 的画布上且只显示左半边 256x160，
 * 调色板为一段未压缩的 256 色 BGR555。玩家落点来自卡带的 15x28 地点网格（单层），
 * 按地图头里的地区编号索引；光标落在地点格右下方的标记中心上，与图上自带的
 * 城市标记和城镇圆点重合。
 */
public final class EmeraldRegionMap extends GbaRegionMap {
    /** 丰缘只有一张大区图。 */
    public static final int REGIONS = 1;
    /** 显示区域：画布左半边，去掉右侧留白。 */
    private static final int CROP_WIDTH = 256, CROP_HEIGHT = 160;
    /** 图块层画布尺寸（像素），图块按 8 像素步进铺放。 */
    private static final int CANVAS_WIDTH = 512, CANVAS_HEIGHT = 160;
    /** 图块集解压后长度：233 个 8bpp 图块。 */
    private static final int TILESET_BYTES = 233 * 64;
    /** 图块层解压后长度（字节，每条目 1 字节）。 */
    private static final int TILEMAP_BYTES = 4096;
    /** 地点网格尺寸（列 x 行）。 */
    private static final int GRID_COLUMNS = 28, GRID_ROWS = 15;
    /** 地点网格里表示空白的哨兵值。 */
    private static final int GRID_EMPTY = 213;

    /** 调色板基址（256 色 BGR555，未压缩，索引 0 强制透明）。 */
    private static final int PALETTE = 0x59F65C;
    /** 图块集基址（LZ77）。 */
    private static final int TILESET = 0x59F77C;
    /** 图块层基址（LZ77）。 */
    private static final int TILEMAP = 0x5A04E0;
    /** 地点网格基址。 */
    private static final int GRID = 0x5A096C;
    /** 光标中心相对地点格左上角的偏移，与图上自带的标记中心对齐。 */
    private static final int CURSOR_X = 14, CURSOR_Y = 21;

    public static EmeraldRegionMap load(byte[] rom) throws IOException {
        EmeraldRegionMap map = new EmeraldRegionMap(rom);
        map.validate();
        return map;
    }

    private EmeraldRegionMap(byte[] rom) {
        super(rom, REGIONS);
    }

    @Override
    protected int gridColumns() {
        return GRID_COLUMNS;
    }

    @Override
    public int pixelX(int column) {
        return column * 8 + CURSOR_X;
    }

    @Override
    public int pixelY(int row) {
        return row * 8 + CURSOR_Y;
    }

    /**
     * 校验卡带结构：图块集与图块层的解压长度、地点网格都必须符合绿宝石 v1.0 的布局，
     * 否则视为不支持的版本（如改版卡带）。
     */
    @Override
    protected void validate() throws IOException {
        if (lz77Size(TILESET) != TILESET_BYTES) throw new IOException("Region map tileset not found");
        if (lz77Size(TILEMAP) != TILEMAP_BYTES) throw new IOException("Region map tilemap not found");
        label(0, readGrid());
        // 网格里的 0..15 对应丰缘的城镇与城市，0 为未白镇，顺带验证了地址与空白哨兵值。
        for (int section = 0; section <= 15; section++) {
            if (position(section) == null) throw new IOException("Region map labels not found");
        }
    }

    /** 读取 15x28 地点网格，哨兵值视为空白。 */
    private int[] readGrid() throws IOException {
        int[] cells = new int[GRID_COLUMNS * GRID_ROWS];
        for (int cell = 0; cell < cells.length; cell++) {
            int value = unsigned(GRID + cell);
            cells[cell] = value == GRID_EMPTY ? -1 : value;
        }
        return cells;
    }

    /** 按图块层把图块集铺到画布左半边。条目 0 也是有效海面图块，调色板索引 0 为透明。 */
    @Override
    protected BufferedImage decode(int region) throws IOException {
        byte[] tiles = decompress(TILESET);
        byte[] tilemap = decompress(TILEMAP);
        int[] palette = new int[256];
        for (int index = 1; index < palette.length; index++) {
            palette[index] = bgr555(shortAt(PALETTE + index * 2));
        }
        BufferedImage image = new BufferedImage(CROP_WIDTH, CROP_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[CROP_WIDTH * CROP_HEIGHT];
        int tileX = 0, tileY = 0;
        for (int index = 0; index < tilemap.length && tileY < CANVAS_HEIGHT; index++) {
            int tile = tilemap[index] & 255;
            if ((tile + 1) * 64 <= tiles.length) {
                for (int pixel = 0; pixel < 64; pixel++) {
                    int color = palette[tiles[tile * 64 + pixel] & 255];
                    if (color == 0) continue;
                    int x = tileX + pixel % 8;
                    int y = tileY + pixel / 8;
                    if (x >= CROP_WIDTH || y >= CROP_HEIGHT) continue;
                    pixels[y * CROP_WIDTH + x] = color;
                }
            }
            tileX += 8;
            if (tileX >= CANVAS_WIDTH) {
                tileX = 0;
                tileY += 8;
            }
        }
        image.setRGB(0, 0, CROP_WIDTH, CROP_HEIGHT, pixels, 0, CROP_WIDTH);
        return image;
    }
}
