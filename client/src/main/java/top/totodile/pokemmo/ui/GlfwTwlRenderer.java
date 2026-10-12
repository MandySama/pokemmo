package top.totodile.pokemmo.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxNativesLoader;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.input.Input;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.ClipStack;
import de.matthiasmann.twl.utils.StateSelect;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * 将官方 TWL 的 Renderer/Texture/Image 接口适配到现有 GLFW + LWJGL3 上下文。
 * 提供当前界面所需的最小渲染适配功能。
 * 支持主题图片、字体、颜色叠乘、裁剪和纹理释放；其他能力按接口约定返回空或显式报错。
 * 使用固定管线和立即模式，需要兼容这些功能的 OpenGL 上下文。
 */
final class GlfwTwlRenderer implements Renderer {
    // UI 使用窗口坐标；OpenGL 裁剪使用帧缓冲像素坐标。
    private int width = 1024, height = 768, framebufferWidth = 1024, framebufferHeight = 768;
    private final ClipStack clips = new ClipStack();
    private final Rect clip = new Rect();
    private final Deque<float[]> tints = new ArrayDeque<>();
    private ImageCache cache;

    GlfwTwlRenderer() {
        TwlFont.initialize();
        tints.push(new float[]{1, 1, 1, 1});
    }

    void resize(int width, int height, int framebufferWidth, int framebufferHeight) {
        this.width = width;
        this.height = height;
        this.framebufferWidth = framebufferWidth;
        this.framebufferHeight = framebufferHeight;
    }

    @Override
    public long getTimeMillis() {
        return System.nanoTime() / 1_000_000;
    }

    @Override
    public Input getInput() {
        return null;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean startRendering() {
        if (width <= 0 || height <= 0) return false;
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        // TWL 坐标原点在左上角，Y 轴向下；正交投影将窗口坐标直接映射到画面。
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glDisable(GL_LIGHTING);
        glDisable(GL_SCISSOR_TEST);
        glEnable(GL_BLEND);
        // PNG 使用普通透明度混合，保留 Logo 周围的透明区域。
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);
        clips.clearStack();
        return true;
    }

    @Override
    public void endRendering() {
        // 与 startRendering 成对恢复矩阵和属性，避免 UI 状态影响后续绘制。
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glPopAttrib();
        glMatrixMode(GL_MODELVIEW);
    }

    @Override
    public CacheContext createNewCacheContext() {
        return new ImageCache();
    }

    @Override
    public void setActiveCacheContext(CacheContext context) {
        if (!(context instanceof ImageCache images) || !images.isValid())
            throw new IllegalArgumentException("Invalid TWL image cache");
        cache = images;
    }

    @Override
    public CacheContext getActiveCacheContext() {
        if (cache == null || !cache.isValid()) cache = new ImageCache();
        return cache;
    }

    @Override
    public Texture loadTexture(URL url, String format, String filter) throws IOException {
        // ThemeManager 传入相对主题文件解析后的 URL，支持类路径和 JAR 内资源。
        GdxNativesLoader.load();
        byte[] bytes;
        try (InputStream stream = url.openStream()) {
            bytes = stream.readAllBytes();
        }
        Pixmap source = new Pixmap(bytes, 0, bytes.length);
        Pixmap rgba = null;
        try {
            rgba = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
            rgba.setBlending(Pixmap.Blending.None);
            rgba.drawPixmap(source, 0, 0);
            int id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, id);
            int filtering = "nearest".equals(filter) ? GL_NEAREST : GL_LINEAR;
            // Logo 使用 nearest 采样，保留原图像素边缘。
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filtering);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filtering);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, rgba.getWidth(), rgba.getHeight(),
                    0, GL_RGBA, GL_UNSIGNED_BYTE, rgba.getPixels());
            ImageTexture texture = new ImageTexture(id, rgba.getWidth(), rgba.getHeight());
            // GPU 纹理由主题缓存持有；上传结束后 CPU 侧 Pixmap 可立即释放。
            ((ImageCache) getActiveCacheContext()).textures.add(texture);
            return texture;
        } finally {
            if (rgba != null) rgba.dispose();
            source.dispose();
        }
    }

    @Override
    public void clipEnter(int x, int y, int w, int h) {
        clips.push(x, y, w, h);
        applyClip();
    }

    @Override
    public void clipEnter(Rect rect) {
        clips.push(rect);
        applyClip();
    }

    @Override
    public boolean clipIsEmpty() {
        return clips.getStackSize() > 0 && clips.isClipEmpty();
    }

    @Override
    public void clipLeave() {
        clips.pop();
        applyClip();
    }

    private void applyClip() {
        if (clips.getClipRect(clip)) {
            // ClipStack 已求出父子裁剪区域的交集；转换 DPI 比例并翻转到 OpenGL 左下原点。
            // 左/下向外取整，右/上向外取整，避免缩放后丢失边缘像素。
            glEnable(GL_SCISSOR_TEST);
            int x = (int) Math.floor((double) clip.getX() * framebufferWidth / width);
            int right = (int) Math.ceil((double) clip.getRight() * framebufferWidth / width);
            int y = (int) Math.floor((double) (height - clip.getBottom()) * framebufferHeight / height);
            int top = (int) Math.ceil((double) (height - clip.getY()) * framebufferHeight / height);
            glScissor(x, y, Math.max(0, right - x), Math.max(0, top - y));
        } else glDisable(GL_SCISSOR_TEST);
    }

    @Override
    public void pushGlobalTintColor(float r, float g, float b, float a) {
        // 父子控件颜色逐层相乘，pop 时恢复父控件颜色；栈底白色表示不改变原色。
        float[] previous = tints.peek();
        tints.push(new float[]{r * previous[0], g * previous[1], b * previous[2], a * previous[3]});
    }

    @Override
    public void popGlobalTintColor() {
        if (tints.size() == 1) throw new IllegalStateException("TWL tint stack underflow");
        tints.pop();
    }

    // 鼠标和键盘由 GLFW 回调接入；以下保留尚未使用的可选渲染接口。

    @Override
    public void setCursor(MouseCursor cursor) {
    }

    @Override
    public void setMousePosition(int x, int y) {
    }

    @Override
    public void setMouseButton(int button, boolean state) {
    }

    @Override
    public LineRenderer getLineRenderer() {
        return null;
    }

    @Override
    public OffscreenRenderer getOffscreenRenderer() {
        return null;
    }

    @Override
    public FontMapper getFontMapper() {
        return null;
    }

    @Override
    public DynamicImage createDynamicImage(int w, int h) {
        return null;
    }

    @Override
    public Image createGradient(Gradient gradient) {
        throw new UnsupportedOperationException("TWL gradients");
    }

    @Override
    public Font loadFont(URL url, StateSelect select, FontParameter... parameters) throws IOException {
        return new TwlFont(this, url, select, parameters);
    }

    Image uploadText(java.awt.image.BufferedImage image) {
        Texture texture = uploadImage(image);
        return texture.getImage(0, 0, image.getWidth(), image.getHeight(), Color.WHITE, false, Texture.Rotation.NONE);
    }

    /** 调用方可提前销毁纹理，主题缓存同时保证退出时释放仍存活的纹理。 */
    Texture uploadImage(java.awt.image.BufferedImage image) {
        var pixels = org.lwjgl.BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                pixels.put((byte) (argb >>> 16)).put((byte) (argb >>> 8))
                        .put((byte) argb).put((byte) (argb >>> 24));
            }
        }
        pixels.flip();
        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        ImageTexture texture = new ImageTexture(id, image.getWidth(), image.getHeight());
        ((ImageCache) getActiveCacheContext()).textures.add(texture);
        return texture;
    }

    /** 记录当前主题加载的纹理，在 ThemeManager.destroy() 时统一释放。 */
    private static final class ImageCache implements CacheContext {
        private boolean valid = true;
        private final List<ImageTexture> textures = new ArrayList<>();

        @Override
        public boolean isValid() {
            return valid;
        }

        @Override
        public void destroy() {
            textures.forEach(ImageTexture::destroy);
            textures.clear();
            valid = false;
        }
    }

    /** 一张已上传到 GPU 的完整图片；通过 getImage 创建共享同一纹理的区域视图。 */
    private final class ImageTexture implements Texture {
        private int id;
        private final int width, height;
        ImageTexture(int id, int width, int height) {
            this.id = id;
            this.width = width;
            this.height = height;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void destroy() {
            if (id != 0) {
                glDeleteTextures(id);
                id = 0;
            }
        }

        @Override
        public void themeLoadingDone() {
        }

        @Override
        public MouseCursor createCursor(int x, int y, int w, int h, int hotX, int hotY, Image ref) {
            throw new UnsupportedOperationException("TWL custom cursors");
        }

        @Override
        public Image getImage(int x, int y, int w, int h, Color tint, boolean tiled, Rotation rotation) {
            if (rotation != Rotation.NONE) throw new UnsupportedOperationException("TWL rotated images");
            return new TextureImage(this, x, y, w, h, tint == null ? Color.WHITE : tint, tiled);
        }
    }

    /** 纹理中的区域和颜色信息，不单独拥有 GPU 资源。 */
    private final class TextureImage implements Image {
        private final ImageTexture texture;
        private final int x, y, width, height;
        private final Color tint;
        private final boolean tiled;
        TextureImage(ImageTexture texture, int x, int y, int width, int height, Color tint, boolean tiled) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.tint = tint;
            this.tiled = tiled;
        }

        @Override
        public int getWidth() {
            return Math.abs(width);
        }

        @Override
        public int getHeight() {
            return Math.abs(height);
        }

        @Override
        public Image createTintedVersion(Color color) {
            // 创建轻量视图并叠乘颜色，不重复上传图片。
            return new TextureImage(texture, x, y, width, height, tint.multiply(color), tiled);
        }

        @Override
        public void draw(AnimationState state, int x, int y) {
            draw(state, x, y, getWidth(), getHeight());
        }

        @Override
        public void draw(AnimationState state, int dx, int dy, int dw, int dh) {
            if (tiled) {
                for (int offsetY = 0; offsetY < dh; offsetY += getHeight()) {
                    for (int offsetX = 0; offsetX < dw; offsetX += getWidth()) {
                        int tileWidth = Math.min(getWidth(), dw - offsetX);
                        int tileHeight = Math.min(getHeight(), dh - offsetY);
                        drawRegion(dx + offsetX, dy + offsetY, tileWidth, tileHeight,
                                tileWidth, tileHeight);
                    }
                }
            } else {
                drawRegion(dx, dy, dw, dh, width, height);
            }
        }

        private void drawRegion(int dx, int dy, int dw, int dh, int sourceWidth, int sourceHeight) {
            if (texture.id == 0) throw new IllegalStateException("TWL texture has been destroyed");
            float u0 = (float) x / texture.width, v0 = (float) y / texture.height;
            // 源图区域换算成归一化纹理坐标，目标矩形的位置和大小由 TWL 布局决定。
            float u1 = (float) (x + sourceWidth) / texture.width, v1 = (float) (y + sourceHeight) / texture.height;
            float[] global = tints.peek();
            glBindTexture(GL_TEXTURE_2D, texture.id);
            glColor4f(tint.getRedFloat() * global[0], tint.getGreenFloat() * global[1],
                    tint.getBlueFloat() * global[2], tint.getAlphaFloat() * global[3]);
            glBegin(GL_QUADS);
            glTexCoord2f(u0, v0);
            glVertex2i(dx, dy);
            glTexCoord2f(u1, v0);
            glVertex2i(dx + dw, dy);
            glTexCoord2f(u1, v1);
            glVertex2i(dx + dw, dy + dh);
            glTexCoord2f(u0, v1);
            glVertex2i(dx, dy + dh);
            glEnd();
        }
    }
}
