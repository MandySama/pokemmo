package top.totodile.pokemmo.rom;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

/** 图块图集绘制：按镜头裁剪格子，动画只切换已有纹理区域。 */
public final class GbaMapPreview implements AutoCloseable {
    private final FireRedMap map;
    private final int width;
    private final int height;
    private final long started = System.nanoTime();
    private GbaTileAtlas atlas;
    private int atlasSeason;
    private boolean borderVisible;
    private boolean closed;
    // 预览环境显式指定，不用本机时间代替游戏服务器时钟。
    private final int season = Integer.getInteger("pokemmo.preview.season", 1);
    private final int hour = Integer.getInteger("pokemmo.preview.hour", 12);
    private final int minute = Integer.getInteger("pokemmo.preview.minute", 0);

    public GbaMapPreview(FireRedMap map) throws IOException {
        this.map = map;
        if (season < 0 || season > 3) throw new IllegalArgumentException("季节编号必须在 0～3 之间。");
        map.tint(hour, minute);
        width = map.width();
        height = map.height();
        atlas = GbaTileAtlas.acquire(map, season);
        atlasSeason = season;
    }

    public void setBorderVisible(boolean visible) { borderVisible = visible; }

    public void render(int viewportWidth, int viewportHeight) throws IOException {
        render(viewportWidth, viewportHeight, season, hour, minute, (System.nanoTime() - started) / 1_000_000);
    }

    public void render(int viewportWidth, int viewportHeight, int season, int hour, int minute, long elapsedMillis) throws IOException {
        int scale = Math.max(1, Math.min(4, Math.min(viewportWidth / width, viewportHeight / height)));
        renderAt(viewportWidth, viewportHeight, season, hour, minute, elapsedMillis,
                (viewportWidth - width * scale) / 2, (viewportHeight - height * scale) / 2, scale, null);
    }

    /** 按人物高度层插入绘制，普通上层和高处上层各自保持遮挡关系。 */
    public void renderAt(int viewportWidth, int viewportHeight, int season, int hour, int minute,
                         long elapsedMillis, int left, int top, int scale, Runnable actor) throws IOException {
        renderAt(viewportWidth, viewportHeight, season, hour, minute, elapsedMillis, left, top, scale, 2, actor);
    }

    public void renderAt(int viewportWidth, int viewportHeight, int season, int hour, int minute,
                         long elapsedMillis, int left, int top, int scale, int actorLayer, Runnable actor) throws IOException {
        renderWithActors(viewportWidth, viewportHeight, season, hour, minute, elapsedMillis, left, top, scale,
                actor == null ? null : layer -> {
                    if (layer == actorLayer) actor.run();
                });
    }

    /** 多个实体按各自高度插入，图块上层沿用地图格子的高度分类。 */
    public void renderWithActors(int viewportWidth, int viewportHeight, int season, int hour, int minute,
                                 long elapsedMillis, int left, int top, int scale,
                                 java.util.function.IntConsumer actors) throws IOException {
        if (closed) throw new IllegalStateException("Map renderer is closed");
        if (season < 0 || season > 3 || elapsedMillis < 0 || scale < 1)
            throw new IllegalArgumentException("Invalid map rendering parameters: season=" + season
                    + ", elapsedMillis=" + elapsedMillis + ", scale=" + scale);
        if (season != atlasSeason) {
            GbaTileAtlas replacement = GbaTileAtlas.acquire(map, season);
            atlas.release();
            atlas = replacement;
            atlasSeason = season;
        }
        float[] tint = map.tint(hour, minute);
        long tick = elapsedMillis / map.animationIntervalMillis();
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, viewportWidth, viewportHeight, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        int size = 16 * scale;
        int startX = Math.floorDiv(-left, size), startY = Math.floorDiv(-top, size);
        int endX = Math.floorDiv(viewportWidth - 1 - left, size) + 1;
        int endY = Math.floorDiv(viewportHeight - 1 - top, size) + 1;
        if (borderVisible && map.outdoor()) {
            glColor4f(tint[0], tint[1], tint[2], 1);
            for (int layer = 0; layer < 2; layer++) {
                drawCells(layer, tick, left, top, scale, startX, startY, endX, endY, true);
            }
        }
        startX = Math.max(0, startX); startY = Math.max(0, startY);
        endX = Math.min(map.columns(), endX); endY = Math.min(map.rows(), endY);
        for (int layer = 0; layer < 5; layer++) {
            if (actors != null && (layer == 1 || layer == 2)) actors.accept(layer);
            if (layer >= 3 && tint[3] == 0) continue;
            if (layer < 3) glColor4f(tint[0], tint[1], tint[2], 1);
            else glColor4f(1, 1, 1, layer == 3 ? tint[3] : 1);
            drawCells(layer, tick, left, top, scale, startX, startY, endX, endY, false);
        }
        glBindTexture(GL_TEXTURE_2D, 0);
        glColor4f(1, 1, 1, 1);
    }

    private void drawCells(int layer, long tick, int left, int top, int scale,
                           int startX, int startY, int endX, int endY, boolean border) {
        int size = 16 * scale;
        for (int page = 0; page < atlas.pages(); page++) {
            atlas.bind(page);
            glBegin(GL_QUADS);
            for (int y = startY; y < endY; y++) {
                for (int x = startX; x < endX; x++) {
                    int cell = border ? map.borderCell(Math.floorMod(x, map.borderColumns()), Math.floorMod(y, map.borderRows()))
                            : map.cell(x, y);
                    int sourceLayer = layer;
                    if (!border && (layer == 1 || layer == 2)) {
                        if (map.upperLayer(cell) != layer) continue;
                        sourceLayer = 1;
                    }
                    int slot = atlas.slot(cell & 1023, sourceLayer, tick);
                    if (slot >= 0 && GbaTileAtlas.page(slot) == page)
                        GbaTileAtlas.quad(slot, left + x * size, top + y * size, scale);
                }
            }
            glEnd();
        }
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        atlas.release();
    }
}
