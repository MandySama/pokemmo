package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Renderer;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 参考 RenderTargetResource.mT() 的登录滚动背景分支。
 * 连续加载 bg_00.png、bg_01.png 等图片，按窗口高度缩放后横向拼接、循环向左滚动。
 * 纹理交给当前主题的缓存管理，此类仅持有图片视图，不单独释放 GPU 资源。
 */
final class ScrollingLoginBackground {
    private static final long NANOS_PER_PIXEL = 22_000_000L;

    private final List<Image> images = new ArrayList<>();

    ScrollingLoginBackground(Renderer renderer) throws IOException {
        // 与参考客户端一致，序号从 00 开始；第一个不存在的文件表示序列结束。
        for (int index = 0; index < 100; index++) {
            Path path = ClientFiles.data(String.format(Locale.ROOT, "sprites/textures/bg_%02d.png", index));
            if (!Files.isRegularFile(path)) {
                break;
            }
            Texture texture = renderer.loadTexture(path.toUri().toURL(), null, "nearest");
            images.add(texture.getImage(0, 0, texture.getWidth(), texture.getHeight(),
                    Color.WHITE, false, Texture.Rotation.NONE));
        }
    }

    void draw(int x, int y, int width, int height, long elapsedNanos) {
        if (width <= 0 || height <= 0 || images.isEmpty()) {
            return;
        }
        if (images.size() == 1) {
            // 参考实现的单图分支直接铺满窗口，不进行横向滚动。
            images.getFirst().draw(null, x, y, width, height);
            return;
        }

        long cycleWidth = 0;
        for (Image image : images) {
            cycleWidth += scaledWidth(image, height);
        }
        // 参考实现每 22 毫秒向左移动一个窗口坐标单位，与帧率无关。
        // 使用实际绘制宽度之和作为周期，避免比例缩放取整造成拼接缝隙。
        long offset = -((Math.max(0, elapsedNanos) / NANOS_PER_PIXEL) % cycleWidth);
        while (offset < width) {
            for (Image image : images) {
                int drawWidth = scaledWidth(image, height);
                if (offset + drawWidth > 0 && offset < width) {
                    image.draw(null, x + (int) offset, y, drawWidth, height);
                }
                offset += drawWidth;
            }
        }
    }

    private static int scaledWidth(Image image, int height) {
        return Math.max(1, (int) ((float) image.getWidth() * height / image.getHeight()));
    }
}
