package top.totodile.pokemmo.rom;

import top.totodile.pokemmo.protocol.PartyMember;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

/** 小图标无独立闪光调色板，按战斗用普通与闪光调色板的对应色转换图标色。 */
public final class PartyIcons {
    public static final int TILE_SIZE = 42;
    public static final int PADDING = 5;
    private PartyIcons() { }

    public static BufferedImage load(Path rom, List<PartyMember> members, int alphaColor, int shinyAlphaColor) throws IOException {
        return load(rom, members, alphaColor, shinyAlphaColor, 0.0f, 1.25f);
    }

    public static BufferedImage load(Path rom, List<PartyMember> members, int alphaColor, int shinyAlphaColor,
                                     float glowSize, float glowIntensity) throws IOException {
        return render(rom, members, alphaColor, shinyAlphaColor, glowSize, glowIntensity, 6);
    }

    /** 箱子网格可达 60 格，图标条按实际成员数量定位，不再固定六格宽度。 */
    public static BufferedImage loadStrip(Path rom, List<PartyMember> members, int alphaColor, int shinyAlphaColor,
                                          float glowSize, float glowIntensity) throws IOException {
        return render(rom, members, alphaColor, shinyAlphaColor, glowSize, glowIntensity, Math.max(1, members.size()));
    }

    private static BufferedImage render(Path rom, List<PartyMember> members, int alphaColor, int shinyAlphaColor,
                                        float glowSize, float glowIntensity, int tiles) throws IOException {
        BufferedImage source = PokedexIcons.load(rom);
        NdsArchive battle = members.stream().anyMatch(PartyMember::shiny) ? new NdsArchive(rom, "/a/0/0/4") : null;
        BufferedImage result = new BufferedImage(tiles * TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < members.size(); i++) {
            PartyMember member = members.get(i);
            int sx = 1024 + (member.species() - 1) % 32 * 32, sy = (member.species() - 1) / 32 * 32;
            int[] normal = member.shiny() ? palette(battle.file(member.species() * 20 + 18)) : null;
            int[] shiny = member.shiny() ? palette(battle.file(member.species() * 20 + 19)) : null;
            BufferedImage tile = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
                int color = source.getRGB(sx + x, sy + y);
                if ((color >>> 24) == 0) continue;
                if (normal != null) color = recolor(color, normal, shiny);
                if (member.alpha()) {
                    for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx, ny = y + dy;
                        if (nx < 0 || nx >= 32 || ny < 0 || ny >= 32
                                || (source.getRGB(sx + nx, sy + ny) >>> 24) == 0)
                            tile.setRGB(nx + PADDING, ny + PADDING, member.shiny() ? shinyAlphaColor : alphaColor);
                    }
                }
                tile.setRGB(x + PADDING, y + PADDING, color);
            }
            BufferedImage glow = member.alpha() && glowSize > 0 ? glow(tile, member.shiny() ? shinyAlphaColor : alphaColor,
                    glowSize, glowIntensity) : tile;
            result.setRGB(i * TILE_SIZE, 0, TILE_SIZE, TILE_SIZE,
                    glow.getRGB(0, 0, TILE_SIZE, TILE_SIZE, null, 0, TILE_SIZE), 0, TILE_SIZE);
        }
        return result;
    }

    /** 在图标透明区域预计算非 HQ 的 3×5 采样柔光，避免固定管线全局切换着色器。 */
    private static BufferedImage glow(BufferedImage source, int color, float size, float intensity) {
        BufferedImage result = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        double[] offsets = {-3.2308, -1.3883, 0, 1.3883, 3.2308};
        double[] weights = {0.0702, 0.3090, 0.2416, 0.3090, 0.0702};
        double[] vertical = {0.25, 0.50, 0.25};
        for (int y = 0; y < TILE_SIZE; y++) for (int x = 0; x < TILE_SIZE; x++) {
            int pixel = source.getRGB(x, y);
            if ((pixel >>> 24) > 25 || size <= 0) {
                result.setRGB(x, y, pixel);
                continue;
            }
            double alpha = 0;
            for (int row = -1; row <= 1; row++) for (int col = 0; col < offsets.length; col++)
                alpha += vertical[row + 1] * weights[col] * sampleAlpha(source, x + offsets[col] * size, y + row * size);
            int opacity = (int) Math.round(Math.clamp(alpha * intensity, 0, 1) * 255);
            result.setRGB(x, y, opacity == 0 ? 0 : opacity << 24 | color & 0xffffff);
        }
        return result;
    }

    private static double sampleAlpha(BufferedImage source, double x, double y) {
        int left = (int) Math.floor(x), top = (int) Math.floor(y);
        double fx = x - left, fy = y - top;
        return alpha(source, left, top) * (1 - fx) * (1 - fy)
                + alpha(source, left + 1, top) * fx * (1 - fy)
                + alpha(source, left, top + 1) * (1 - fx) * fy
                + alpha(source, left + 1, top + 1) * fx * fy;
    }

    private static double alpha(BufferedImage source, int x, int y) {
        return x < 0 || y < 0 || x >= TILE_SIZE || y >= TILE_SIZE ? 0 : (source.getRGB(x, y) >>> 24) / 255.0;
    }

    private static int[] palette(ByteBuffer data) throws IOException {
        try {
            if (data.getInt(0) != 0x4e434c52) throw new IOException("Invalid Pokemon palette");
            int start = 24 + data.getInt(36);
            int[] colors = new int[16];
            for (int i = 0; i < colors.length; i++) {
                int c = Short.toUnsignedInt(data.getShort(start + i * 2));
                colors[i] = 0xff000000 | ((c & 31) * 255 / 31) << 16
                        | ((c >> 5 & 31) * 255 / 31) << 8 | (c >> 10 & 31) * 255 / 31;
            }
            return colors;
        } catch (IndexOutOfBoundsException exception) { throw new IOException("Invalid Pokemon palette data", exception); }
    }

    private static int recolor(int color, int[] normal, int[] shiny) {
        int best = 1, distance = Integer.MAX_VALUE;
        for (int i = 1; i < normal.length; i++) {
            int dr = (color >> 16 & 255) - (normal[i] >> 16 & 255);
            int dg = (color >> 8 & 255) - (normal[i] >> 8 & 255);
            int db = (color & 255) - (normal[i] & 255);
            int score = dr * dr + dg * dg + db * db;
            if (score < distance) { distance = score; best = i; }
        }
        if (distance > 10000) return color;
        int r = Math.clamp((color >> 16 & 255) + (shiny[best] >> 16 & 255) - (normal[best] >> 16 & 255), 0, 255);
        int g = Math.clamp((color >> 8 & 255) + (shiny[best] >> 8 & 255) - (normal[best] >> 8 & 255), 0, 255);
        int b = Math.clamp((color & 255) + (shiny[best] & 255) - (normal[best] & 255), 0, 255);
        return 0xff000000 | r << 16 | g << 8 | b;
    }
}
