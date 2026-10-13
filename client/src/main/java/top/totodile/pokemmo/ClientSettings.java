package top.totodile.pokemmo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** 界面和音乐设置共用配置文件，写入前重新读取以保留其他配置。 */
public final class ClientSettings {
    private static final String VOLUME = "client.sound_engine.music_volume";
    private static final String MUTE = "client.sound_engine.music_mute_when_unfocused";
    private static final String SHOW_FPS = "client.graphics.show_fps";
    private static final String FPS_LIMIT = "client.graphics.max_fpx";
    private static final String HGSS_SPRITES = "client.graphics.nds.hgss_sprites_enabled_in_gba";
    private final Path file;
    private int volume = 50;
    private boolean muteWhenUnfocused = true;
    private boolean showFps;
    private int fpsLimit = 60;
    private boolean hgssSprites = true;

    public ClientSettings() {
        this(ClientFiles.data("../config/main.properties").normalize());
    }

    public ClientSettings(Path file) {
        this.file = file;
        try {
            Properties values = read();
            setVolume(Integer.parseInt(values.getProperty(VOLUME, "50")));
            muteWhenUnfocused = Boolean.parseBoolean(values.getProperty(MUTE, "true"));
            showFps = Boolean.parseBoolean(values.getProperty(SHOW_FPS, "false"));
            hgssSprites = Boolean.parseBoolean(values.getProperty(HGSS_SPRITES, "true"));
            setFpsLimit(Integer.parseInt(values.getProperty(FPS_LIMIT, "60")));
        } catch (IOException | IllegalArgumentException exception) {
            System.getLogger(ClientSettings.class.getName()).log(System.Logger.Level.WARNING,
                    "Unable to load client settings: {0}", exception.getClass().getSimpleName());
        }
    }

    public int volume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
    }

    public boolean muteWhenUnfocused() {
        return muteWhenUnfocused;
    }

    public void setMuteWhenUnfocused(boolean mute) {
        muteWhenUnfocused = mute;
    }

    public boolean showFps() {
        return showFps;
    }

    public void setShowFps(boolean showFps) {
        this.showFps = showFps;
    }

    public int fpsLimit() {
        return fpsLimit;
    }

    public void setFpsLimit(int fpsLimit) {
        this.fpsLimit = Math.max(10, Math.min(1000, fpsLimit));
    }

    public void save() throws IOException {
        Properties values = read();
        values.setProperty(VOLUME, Integer.toString(volume));
        values.setProperty(MUTE, Boolean.toString(muteWhenUnfocused));
        values.setProperty(SHOW_FPS, Boolean.toString(showFps));
        values.setProperty(FPS_LIMIT, Integer.toString(fpsLimit));
        values.setProperty(HGSS_SPRITES, Boolean.toString(hgssSprites));
        Path directory = file.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "settings-", ".tmp");
        try {
            try (var output = Files.newOutputStream(temporary)) {
                values.store(output, null);
            }
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public boolean hgssSprites() {
        return hgssSprites;
    }

    public void setHgssSprites(boolean enabled) {
        hgssSprites = enabled;
    }

    private Properties read() throws IOException {
        Properties values = new Properties();
        if (Files.isRegularFile(file)) {
            try (var input = Files.newInputStream(file)) {
                values.load(input);
            }
        }
        return values;
    }
}
