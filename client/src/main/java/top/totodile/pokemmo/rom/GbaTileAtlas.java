package top.totodile.pokemmo.rom;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

/** 同一 OpenGL 上下文中的图块集共享静态与动画帧，最后一个使用者退出时释放图集。 */
final class GbaTileAtlas {
    private static final int SIZE = 512;
    private static final int TILES_PER_ROW = SIZE / 16;
    private static final int PAGE_TILES = TILES_PER_ROW * TILES_PER_ROW;
    private record Key(long context, FireRedMap.GraphicsKey graphics) { }
    private record Pixels(int[] values) {
        @Override public int hashCode() { return Arrays.hashCode(values); }
        @Override public boolean equals(Object other) {
            return other instanceof Pixels pixels && Arrays.equals(values, pixels.values);
        }
    }
    private static final Map<Key, GbaTileAtlas> SHARED = new HashMap<>();
    private final Key key;
    private final ArrayList<Integer> textures = new ArrayList<>();
    private final int[][][] frames;
    private int references;

    static GbaTileAtlas acquire(FireRedMap map, int season) throws IOException {
        Key key = new Key(GLFW.glfwGetCurrentContext(), map.graphicsKey(season));
        GbaTileAtlas atlas = SHARED.get(key);
        if (atlas == null) {
            atlas = new GbaTileAtlas(key, map, season);
            SHARED.put(key, atlas);
        }
        atlas.references++;
        return atlas;
    }

    private GbaTileAtlas(Key key, FireRedMap map, int season) throws IOException {
        this.key = key;
        frames = new int[Math.min(1024, map.blockCount())][][];
        int[] page = new int[SIZE * SIZE];
        Map<Pixels, Integer> slots = new HashMap<>();
        int next = 0;
        try {
            for (int block = 0; block < frames.length; block++) {
                frames[block] = new int[map.blockFrames(block)][5];
                for (int frame = 0; frame < frames[block].length; frame++) {
                    int[][] layers = map.blockLayers(block, season, frame);
                    for (int layer = 0; layer < 5; layer++) {
                        int[] pixels = layers[layer];
                        boolean empty = true;
                        for (int pixel : pixels) if ((pixel & 255) != 0) { empty = false; break; }
                        if (empty) { frames[block][frame][layer] = -1; continue; }
                        Pixels image = new Pixels(pixels);
                        Integer existing = slots.get(image);
                        if (existing != null) { frames[block][frame][layer] = existing; continue; }
                        int slot = next++;
                        slots.put(image, slot);
                        frames[block][frame][layer] = slot;
                        int x = slot % TILES_PER_ROW * 16;
                        int y = slot % PAGE_TILES / TILES_PER_ROW * 16;
                        for (int row = 0; row < 16; row++) System.arraycopy(pixels, row * 16, page, (y + row) * SIZE + x, 16);
                        if (next % PAGE_TILES == 0) { upload(page); Arrays.fill(page, 0); }
                    }
                }
            }
            if (next % PAGE_TILES != 0) upload(page);
        } catch (IOException | RuntimeException | Error exception) {
            dispose();
            throw exception;
        } finally { glBindTexture(GL_TEXTURE_2D, 0); }
    }

    private void upload(int[] pixels) throws IOException {
        ByteBuffer bytes = MemoryUtil.memAlloc(SIZE * SIZE * 4);
        try {
            for (int pixel : pixels) bytes.put((byte) (pixel >>> 24)).put((byte) (pixel >>> 16))
                    .put((byte) (pixel >>> 8)).put((byte) pixel);
            bytes.flip();
            int texture = glGenTextures();
            textures.add(texture);
            glBindTexture(GL_TEXTURE_2D, texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, SIZE, SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, bytes);
            if (glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH) != SIZE)
                throw new IOException("Cannot allocate map tile atlas");
        } finally { MemoryUtil.memFree(bytes); }
    }

    int slot(int block, int layer, long tick) {
        int[][] animation = frames[block];
        return animation[(int) (tick % animation.length)][layer];
    }

    int pages() { return textures.size(); }
    void bind(int page) { glBindTexture(GL_TEXTURE_2D, textures.get(page)); }
    static int page(int slot) { return slot / PAGE_TILES; }

    static void quad(int slot, int x, int y, int scale) {
        float u = slot % TILES_PER_ROW * 16f / SIZE;
        float v = slot % PAGE_TILES / TILES_PER_ROW * 16f / SIZE;
        float edge = 16f / SIZE;
        int size = 16 * scale;
        glTexCoord2f(u, v); glVertex2i(x, y);
        glTexCoord2f(u + edge, v); glVertex2i(x + size, y);
        glTexCoord2f(u + edge, v + edge); glVertex2i(x + size, y + size);
        glTexCoord2f(u, v + edge); glVertex2i(x, y + size);
    }

    void release() {
        if (--references != 0) return;
        SHARED.remove(key);
        dispose();
    }

    private void dispose() {
        for (int texture : textures) glDeleteTextures(texture);
        textures.clear();
    }
}
