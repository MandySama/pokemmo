package top.totodile.pokemmo.client.ui.startup;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.function.LongSupplier;

/** 按屏幕高度缩放背景分片，向左连续循环滚动。 */
public final class ScrollingBackground {
    // 参考客户端每 22 毫秒移动一个屏幕像素，后台经过的时间也计入滚动。
    private static final float PIXELS_PER_SECOND = 1000f / 22f;
    private final Texture[] tiles;
    private final LongSupplier clock;
    private final long startedAt;

    /** 纹理由调用方持有和释放。 */
    public ScrollingBackground(Texture[] tiles, LongSupplier clock) {
        this.tiles = tiles;
        this.clock = clock;
        this.startedAt = clock.getAsLong();
        for (Texture tile : tiles) {
            tile.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
    }

    public void draw(SpriteBatch batch, float width, float height, int screenWidth) {
        if (width <= 0 || height <= 0 || screenWidth <= 0) return;
        float cycleWidth = 0;
        for (Texture tile : tiles) {
            cycleWidth += height * tile.getWidth() / tile.getHeight();
        }
        float unitsPerPixel = width / screenWidth;
        double elapsedSeconds = (clock.getAsLong() - startedAt) / 1_000_000_000.0;
        float offsetPixels =
                (float) ((elapsedSeconds * PIXELS_PER_SECOND) % (cycleWidth / unitsPerPixel));
        float x = -offsetPixels * unitsPerPixel;
        // 最后一片之后立即接第一片，宽屏和窗口缩放时也不会露出空白。
        while (x < width) {
            for (Texture tile : tiles) {
                float tileWidth = height * tile.getWidth() / tile.getHeight();
                batch.draw(tile, x, 0, tileWidth, height);
                x += tileWidth;
            }
        }
    }
}
