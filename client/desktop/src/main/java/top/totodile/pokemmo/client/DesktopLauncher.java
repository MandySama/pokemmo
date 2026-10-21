package top.totodile.pokemmo.client;

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
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("PokeMMO");
        configuration.setInitialBackgroundColor(Color.BLACK);
        configuration.setWindowIcon(FileType.Absolute, ClientAssets.file("icons/128x128.png"));
        configuration.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override public void created(Lwjgl3Window window) {
                new WindowsWindowIcons().created(window);
            }
            @Override public void focusLost() { Gdx.graphics.setForegroundFPS(UNFOCUSED_FPS); }
            @Override public void focusGained() { Gdx.graphics.setForegroundFPS(FOREGROUND_FPS); }
        });
        configuration.setWindowedMode(1024, 768);
        configuration.setWindowSizeLimits(320, 240, -1, -1);
        configuration.setResizable(true);
        configuration.useVsync(false);
        configuration.setForegroundFPS(FOREGROUND_FPS);
        configuration.setIdleFPS(UNFOCUSED_FPS);
        configuration.disableAudio(true);
        new Lwjgl3Application(new ClientApplication(
                path -> com.badlogic.gdx.Gdx.files.absolute(ClientAssets.file(path)),
                false, System::nanoTime,
                () -> com.badlogic.gdx.Gdx.files.absolute(ClientAssets.configDirectory()),
                top.totodile.pokemmo.server.login.LoginServices.create()).withGameService(
                    new top.totodile.pokemmo.server.game.LocalGameService(() -> new DesktopGameDatabase(
                            java.nio.file.Path.of(ClientAssets.configDirectory()).resolveSibling("save").resolve("game.db").toString()))), configuration);
    }
}
