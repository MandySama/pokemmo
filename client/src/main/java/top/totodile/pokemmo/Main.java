package top.totodile.pokemmo;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxNativesLoader;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import top.totodile.pokemmo.rom.FireRedMap;
import top.totodile.pokemmo.rom.GbaMapPreview;
import top.totodile.pokemmo.ui.LoginScreen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 桌面客户端入口：负责 GLFW 窗口、图标、OpenGL 上下文与帧循环。
 * 初始化窗口与图标，界面内容交给 TWL 绘制。
 */
public class Main {
    public static void main(String[] args) throws IOException {
        ClientRestart.initialize(args);
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }
        GLFW.glfwDefaultWindowHints();
        // 首帧绘制完成后再显示窗口，避免显示尚未初始化的画面。
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        long window = GLFW.glfwCreateWindow(1024, 768, "PokeMMO", 0L, 0L);
        if (window == 0L) {
            GLFW.glfwTerminate();
            throw new RuntimeException("Couldn't create window");
        }
        LoginScreen screen = null;
        GbaMapPreview mapPreview = null;
        try {
            centerWindow(window);
            setWindowIcon(window, "icons/128x128.png", "icons/32x32.png", "icons/16x16.png");
            // 必须先绑定当前上下文，再加载 OpenGL 函数并创建 UI 纹理。
            GLFW.glfwMakeContextCurrent(window);
            GL.createCapabilities();
            GLFW.glfwSwapInterval(1);
            if ("kanto".equals(System.getProperty("pokemmo.preview.map"))) {
                Path rom = new RomFiles(ClientFiles.data("../roms")).selected(1);
                if (rom == null) throw new IOException("请先在客户端管理中导入火红 ROM。");
                int group = Integer.getInteger("pokemmo.preview.group", 4);
                int map = Integer.getInteger("pokemmo.preview.id", 1);
                boolean enhanced = Boolean.parseBoolean(System.getProperty("pokemmo.preview.enhanced", "true"));
                mapPreview = new GbaMapPreview(FireRedMap.load(rom, group, map, enhanced));
                GLFW.glfwSetWindowTitle(window, "PokeMMO - 关都地图 " + group + "/" + map
                        + (enhanced ? "（增强）" : "（原始）"));
            } else {
                screen = new LoginScreen();
                screen.attachInput(window);
            }
            GL11.glClearColor(0f, 0f, 0f, 1f);
            // 帧缓冲尺寸用于像素绘制，窗口尺寸用于 UI 布局；高 DPI 下两者可能不同。
            int[] width = new int[1];
            int[] height = new int[1];
            int[] windowWidth = new int[1];
            int[] windowHeight = new int[1];
            boolean shown = false;
            ClientSettings previewSettings = mapPreview == null ? null : new ClientSettings();
            while (!GLFW.glfwWindowShouldClose(window)) {
                long frameStartedAt = System.nanoTime();
                GLFW.glfwPollEvents();
                GLFW.glfwGetFramebufferSize(window, width, height);
                if (width[0] == 0 || height[0] == 0) {
                    // 最小化时停止绘制，等待窗口事件，避免空转占用 CPU。
                    GLFW.glfwWaitEvents();
                    continue;
                }
                GL11.glViewport(0, 0, width[0], height[0]);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
                if (mapPreview != null) {
                    mapPreview.render(width[0], height[0]);
                } else {
                    screen.render(windowWidth[0], windowHeight[0], width[0], height[0]);
                }
                GLFW.glfwSwapBuffers(window);
                FrameLimiter.await(frameStartedAt, screen == null ? previewSettings.fpsLimit() : screen.fpsLimit());
                if (!shown) {
                    GLFW.glfwShowWindow(window);
                    shown = true;
                }
            }
        } finally {
            // 先释放主题持有的 GPU 纹理；窗口销毁后其 OpenGL 上下文不再可用。
            if (screen != null) screen.close();
            if (mapPreview != null) mapPreview.close();
            org.lwjgl.glfw.Callbacks.glfwFreeCallbacks(window);
            GLFW.glfwDestroyWindow(window);
            GLFW.glfwTerminate();
        }
        if (ClientRestart.pending()) System.exit(0);
    }

    /** 在主屏工作区内居中窗口并避开任务栏。 */
    private static void centerWindow(long window) {
        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor == 0L) return;
        int[] x = new int[1], y = new int[1], w = new int[1], h = new int[1];
        int[] left = new int[1], top = new int[1], right = new int[1], bottom = new int[1];
        int[] width = new int[1], height = new int[1];
        GLFW.glfwGetMonitorWorkarea(monitor, x, y, w, h);
        GLFW.glfwGetWindowFrameSize(window, left, top, right, bottom);
        GLFW.glfwGetWindowSize(window, width, height);
        if (w[0] <= 0 || h[0] <= 0) return;
        // 扣除标题栏和边框，再限制客户区大小，确保完整窗口位于工作区内。
        int usableWidth = Math.max(1, w[0] - left[0] - right[0]);
        int usableHeight = Math.max(1, h[0] - top[0] - bottom[0]);
        int clientWidth = Math.min(width[0], usableWidth);
        int clientHeight = Math.min(height[0], usableHeight);
        GLFW.glfwSetWindowSize(window, clientWidth, clientHeight);
        GLFW.glfwSetWindowPos(window,
                x[0] + left[0] + (usableWidth - clientWidth) / 2,
                y[0] + top[0] + (usableHeight - clientHeight) / 2);
    }

    /**
     * 同时提交多种尺寸的图标，由操作系统选择合适的尺寸。
     * GLFW 调用时会复制像素，因此调用结束后即可释放 Pixmap 和临时图片结构。
     */
    public static void setWindowIcon(long window, String... resourcePaths) throws IOException {
        Pixmap[] icons = new Pixmap[resourcePaths.length];
        try (GLFWImage.Buffer images = GLFWImage.malloc(resourcePaths.length)) {
            for (int i = 0; i < resourcePaths.length; i++) {
                icons[i] = loadRgba(resourcePaths[i]);
                images.get(i).set(icons[i].getWidth(), icons[i].getHeight(), icons[i].getPixels());
            }
            GLFW.glfwSetWindowIcon(window, images);
        } finally {
            for (Pixmap icon : icons) {
                if (icon != null) icon.dispose();
            }
        }
    }

    /** 从外部 data 目录解码图片并统一为 RGBA8888；返回的 Pixmap 由调用方负责 dispose。 */
    private static Pixmap loadRgba(String resourcePath) throws IOException {
        GdxNativesLoader.load();
        byte[] bytes;
        try (InputStream input = Files.newInputStream(ClientFiles.data(resourcePath))) {
            bytes = input.readAllBytes();
        }
        Pixmap source = new Pixmap(bytes, 0, bytes.length);
        try {
            Pixmap converted = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);
            // 格式转换时直接复制像素，避免透明像素与默认背景混合。
            converted.setBlending(Pixmap.Blending.None);
            converted.drawPixmap(source, 0, 0);
            return converted;
        } finally {
            source.dispose();
        }
    }
}
