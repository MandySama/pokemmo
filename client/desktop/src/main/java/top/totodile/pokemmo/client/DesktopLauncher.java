package top.totodile.pokemmo.client;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;

public final class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("PokeMMO");
        configuration.setInitialBackgroundColor(Color.BLACK);
        configuration.setWindowIcon(FileType.Absolute, ClientAssets.file("icons/128x128.png"));
        configuration.setWindowListener(new WindowsWindowIcons());
        configuration.setWindowedMode(1024, 768);
        configuration.setWindowSizeLimits(320, 240, -1, -1);
        configuration.setResizable(true);
        configuration.useVsync(true);
        configuration.setForegroundFPS(60);
        configuration.setIdleFPS(30);
        configuration.disableAudio(true);
        new Lwjgl3Application(new ClientApplication(
                path -> com.badlogic.gdx.Gdx.files.absolute(ClientAssets.file(path)),
                false, System::nanoTime,
                () -> com.badlogic.gdx.Gdx.files.absolute(ClientAssets.configDirectory())), configuration);
    }
}
