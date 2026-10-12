package top.totodile.pokemmo.rom;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

/** 本地分层地图预览，按整数倍放大，季节、动画和昼夜规则独立应用。 */
public final class GbaMapPreview implements AutoCloseable {
    private final int[] textures = new int[5];
    private final FireRedMap map;
    private final int width;
    private final int height;
    private final ByteBuffer rgba;
    private final long started = System.nanoTime();
    private long lastFrame = -1;
    private int lastSeason = -1;
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
        rgba = BufferUtils.createByteBuffer(width * height * 4);
        try {
            for (int i = 0; i < textures.length; i++) {
                textures[i] = glGenTextures();
                glBindTexture(GL_TEXTURE_2D, textures[i]);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
            }
            update(season, 0);
        } catch (IOException | RuntimeException exception) {
            close();
            throw exception;
        } finally {
            glBindTexture(GL_TEXTURE_2D, 0);
        }
    }

    private void update(int season, long elapsedMillis) throws IOException {
        long frame = map.animated() ? elapsedMillis / map.animationIntervalMillis() : 0;
        if (lastFrame == frame && lastSeason == season) return;
        int[][] layers = map.layers(season, elapsedMillis);
        for (int layer = 0; layer < textures.length; layer++) {
            rgba.clear();
            for (int pixel : layers[layer]) {
                rgba.put((byte) (pixel >>> 24)).put((byte) (pixel >>> 16)).put((byte) (pixel >>> 8)).put((byte) pixel);
            }
            rgba.flip();
            glBindTexture(GL_TEXTURE_2D, textures[layer]);
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, rgba);
        }
        lastFrame = frame;
        lastSeason = season;
    }

    public void render(int viewportWidth, int viewportHeight) throws IOException {
        render(viewportWidth, viewportHeight, season, hour, minute, (System.nanoTime() - started) / 1_000_000);
    }

    public void render(int viewportWidth, int viewportHeight, int season, int hour, int minute, long elapsedMillis) throws IOException {
        update(season, elapsedMillis);
        float[] tint = map.tint(hour, minute);
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
        int scale = Math.max(1, Math.min(4, Math.min(viewportWidth / width, viewportHeight / height)));
        int left = (viewportWidth - width * scale) / 2;
        int top = (viewportHeight - height * scale) / 2;
        for (int layer = 0; layer < textures.length; layer++) {
            if (layer >= 3 && tint[3] == 0) continue;
            if (layer < 3) glColor4f(tint[0], tint[1], tint[2], 1);
            else glColor4f(1, 1, 1, layer == 3 ? tint[3] : 1);
            glBindTexture(GL_TEXTURE_2D, textures[layer]);
            glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
            glVertex2i(left, top);
            glTexCoord2f(1, 0);
            glVertex2i(left + width * scale, top);
            glTexCoord2f(1, 1);
            glVertex2i(left + width * scale, top + height * scale);
            glTexCoord2f(0, 1);
            glVertex2i(left, top + height * scale);
            glEnd();
        }
        glBindTexture(GL_TEXTURE_2D, 0);
        glColor4f(1, 1, 1, 1);
    }

    @Override
    public void close() {
        for (int texture : textures) if (texture != 0) glDeleteTextures(texture);
    }
}
