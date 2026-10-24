package top.totodile.pokemmo.client;

import top.totodile.pokemmo.db.desktop.JdbcDatabase;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.badlogic.gdx.graphics.Color;

public final class DesktopLauncher {
    private static final int FOREGROUND_FPS = 60;
    private static final int UNFOCUSED_FPS = 20;
    public static void main(String[] args) {
        configureConsoleLogging();
        ClientApplication application=new ClientApplication(
                path -> com.badlogic.gdx.Gdx.files.absolute(ClientAssets.file(path)),
                false, System::nanoTime,
                () -> com.badlogic.gdx.Gdx.files.absolute(ClientAssets.configDirectory()),
                top.totodile.pokemmo.server.login.LoginServices.create()).withRomFilePicker(new DesktopRomFilePicker())
                .withRestart(DesktopRestart::restart)
                .withLoginScene(resources -> new top.totodile.pokemmo.client.startup.DesktopLoginScene(resources,
                        java.nio.file.Path.of(ClientAssets.configDirectory()).resolveSibling("cache").resolve("login")))
                .withGameService(new top.totodile.pokemmo.server.game.LocalGameService(() -> new JdbcDatabase(
                        java.nio.file.Path.of(ClientAssets.configDirectory()).resolveSibling("save").resolve("game.db").toString())));
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("PokeMMO");
        configuration.setInitialBackgroundColor(Color.BLACK);
        configuration.setBackBufferConfig(8,8,8,8,24,0,0);
        configuration.setWindowIcon(FileType.Absolute, ClientAssets.file("icons/128x128.png"));
        configuration.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override public void created(Lwjgl3Window window) {
                new WindowsWindowIcons().created(window);
            }
            @Override public void focusLost() { application.setFocused(false); }
            @Override public void focusGained() { application.setFocused(true); }
        });
        configureScreenMode(configuration);
        configuration.setWindowSizeLimits(320, 240, -1, -1);
        configuration.setResizable(true);
        configuration.useVsync(false);
        configuration.setForegroundFPS(FOREGROUND_FPS);
        configuration.setIdleFPS(UNFOCUSED_FPS);
        configuration.disableAudio(false);
        // 参考 Client.main 的 display.aB(AA0.bI * 2)：16 sources、1920 bytes、6 buffers。
        // 默认 512-byte 缓冲在 Windows 上容易断流，导致杂音和播放变慢。
        configuration.setAudioConfig(16,1920,6);
        new Lwjgl3Application(application, configuration);
    }

    /** 对齐参考 Client.main：启动时读取已确认的屏幕模式和分辨率。 */
    private static void configureScreenMode(Lwjgl3ApplicationConfiguration configuration) {
        var settings = new java.util.Properties();
        var file = java.nio.file.Path.of(ClientAssets.configDirectory(), "main.properties");
        if (java.nio.file.Files.exists(file)) {
            try (var input = java.nio.file.Files.newInputStream(file)) { settings.load(input); }
            catch (java.io.IOException error) { throw new IllegalStateException("Cannot read client settings", error); }
        }
        int mode = screenValue(settings, "client.graphics.display_mode", 0);
        int width = Math.max(320, screenValue(settings, "client.graphics.width", 1024));
        int height = Math.max(240, screenValue(settings, "client.graphics.height", 768));
        configuration.setWindowedMode(width, height);
        configuration.setDecorated(mode != 2);
        if (mode == 1) {
            var selected = java.util.Arrays.stream(Lwjgl3ApplicationConfiguration.getDisplayModes())
                    .filter(value -> value.width == width && value.height == height)
                    .max(java.util.Comparator.comparingInt((com.badlogic.gdx.Graphics.DisplayMode value) -> value.bitsPerPixel)
                            .thenComparingInt(value -> value.refreshRate))
                    .orElse(Lwjgl3ApplicationConfiguration.getDisplayMode());
            configuration.setFullscreenMode(selected);
        } else {
            configuration.setMaximized(Boolean.parseBoolean(settings.getProperty("client.graphics.maximized", "true")));
        }
    }

    private static int screenValue(java.util.Properties settings, String key, int fallback) {
        try { return Integer.parseInt(settings.getProperty(key, Integer.toString(fallback))); }
        catch (NumberFormatException invalid) { return fallback; }
    }

    static void configureConsoleLogging() {
        // 参考 logback.xml 的 CONSOLE：标准输出、UTF-8，避免 INFO 被 IDE 当作错误标红。
        var root=java.util.logging.Logger.getLogger("");
        for(var handler:root.getHandlers()) {
            if(handler instanceof java.util.logging.ConsoleHandler)try {
                var console=new java.util.logging.StreamHandler(System.out,handler.getFormatter()) {
                    @Override public void publish(java.util.logging.LogRecord record) { super.publish(record);flush(); }
                    @Override public void close() { flush(); }
                };
                console.setEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
                console.setLevel(handler.getLevel());
                console.setFilter(handler.getFilter());
                root.removeHandler(handler);handler.close();root.addHandler(console);
            }catch(java.io.UnsupportedEncodingException impossible) {
                throw new AssertionError(impossible);
            }
        }
    }
}
