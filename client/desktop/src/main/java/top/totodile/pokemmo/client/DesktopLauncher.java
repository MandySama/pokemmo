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
    private static final int FOREGROUND_FPS = 144;
    private static final int UNFOCUSED_FPS = 144;
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
            @Override public void focusLost() { Gdx.graphics.setForegroundFPS(UNFOCUSED_FPS); application.setFocused(false); }
            @Override public void focusGained() { Gdx.graphics.setForegroundFPS(FOREGROUND_FPS); application.setFocused(true); }
        });
        configuration.setWindowedMode(1024, 768);
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
