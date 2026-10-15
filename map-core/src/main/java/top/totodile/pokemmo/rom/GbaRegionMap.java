package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * GBA 卡带内嵌的区域地图（城镇地图）公共解码。区域图不是玩法地图的拼接，而是卡带里的
 * 一幅专用手绘位图：LZ77（类型 0x10，标志位按高位先读）压缩的图块集与图块层，加一段
 * BGR555 调色板；玩家落点来自卡带的地点网格，按地图头里的地区编号反查，不需要烘焙地标表。
 *
 * <p>数据位置按各版本卡带的固定地址定位，并用结构特征逐项校验；校验失败说明不是该版本
 * 卡带，此时调用方应退回其它渲染方式。
 */
public abstract class GbaRegionMap {
    protected final byte[] rom;
    private final BufferedImage[] images;
    private int[][] labels;

    protected GbaRegionMap(byte[] rom, int regions) {
        this.rom = rom;
        this.images = new BufferedImage[regions];
        this.labels = new int[regions][];
    }

    /** 大区数量（关都为关都 + 三组七之岛，丰缘只有一张）。 */
    public final int regions() {
        return images.length;
    }

    /**
     * 取大区图（已裁剪到显示区域），按需解码并缓存。
     */
    public final BufferedImage image(int region) throws IOException {
        if (region < 0 || region >= images.length) throw new IOException("Unknown region map " + region);
        if (images[region] == null) images[region] = decode(region);
        return images[region];
    }

    /**
     * 按地图头的地区编号求落点：返回 {大区, 列, 行}，没有登记返回 null。
     * 每个大区按列优先展开的网格顺序取第一个命中格。
     */
    public final int[] position(int section) {
        if (section < 0) return null;
        for (int region = 0; region < labels.length; region++) {
            int[] cells = labels[region];
            if (cells == null) continue;
            for (int cell = 0; cell < cells.length; cell++) {
                if (cells[cell] == section) {
                    return new int[]{region, cell % gridColumns(), cell / gridColumns()};
                }
            }
        }
        return null;
    }

    /** 落点换算成裁剪后位图上的像素（标记中心），供窗口绘制光标使用。 */
    public abstract int pixelX(int column);

    public abstract int pixelY(int row);

    /** 登记某个大区的地点网格（按列优先展开，-1 表示空白格）。 */
    protected final void label(int region, int[] cells) {
        labels[region] = cells;
    }

    /** 网格每行列数，供落点反展开使用。 */
    protected abstract int gridColumns();

    protected abstract BufferedImage decode(int region) throws IOException;

    protected abstract void validate() throws IOException;

    protected final int unsigned(int offset) throws IOException {
        if (offset < 0 || offset >= rom.length) throw new IOException("ROM offset out of range");
        return rom[offset] & 255;
    }

    protected final int shortAt(int offset) throws IOException {
        return unsigned(offset) | unsigned(offset + 1) << 8;
    }

    protected final int lz77Size(int offset) {
        if (offset < 0 || offset + 4 > rom.length || rom[offset] != 0x10) return -1;
        return (rom[offset + 1] & 255) | (rom[offset + 2] & 255) << 8 | (rom[offset + 3] & 255) << 16;
    }

    /** 解压 LZ77 类型 0x10：标志位按高位先读，回引长度 3..18、位移 1..4096。 */
    protected final byte[] decompress(int offset) throws IOException {
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

    /** BGR555 转 ARGB；颜色 0（含通配的半透明位）视为透明。 */
    protected static int bgr555(int color) {
        if ((color & 0x7FFF) == 0) return 0;
        return 0xFF000000 | (color & 31) * 255 / 31 << 16
                | (color >> 5 & 31) * 255 / 31 << 8 | (color >> 10 & 31) * 255 / 31;
    }
}
