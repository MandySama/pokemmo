package top.totodile.pokemmo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * GM 调试开关只管理自身的键，写入前重新读取以保留音量、FPS、ROM 路径等其他配置。
 * 与 ClientSettings 分开，避免两个实例互相覆盖对方的键。
 */
public final class DebugSettings {
    private static final String NOCLIP = "client.debug.noclip";
    private static final String SPEED_MULTIPLIER = "client.debug.speed_multiplier";
    private final Path file;
    private boolean noclip;
    private int speedMultiplier = 1;

    public DebugSettings() {
        this(ClientFiles.data("../config/main.properties").normalize());
    }

    public DebugSettings(Path file) {
        this.file = file;
        try {
            Properties values = read();
            noclip = Boolean.parseBoolean(values.getProperty(NOCLIP, "false").trim());
            speedMultiplier = normalizeSpeed(values.getProperty(SPEED_MULTIPLIER, "1"));
        } catch (IOException exception) {
            System.getLogger(DebugSettings.class.getName()).log(System.Logger.Level.WARNING,
                    "Unable to load debug settings: {0}", exception.getClass().getSimpleName());
        }
    }

    public boolean noclip() {
        return noclip;
    }

    public void setNoclip(boolean enabled) {
        noclip = enabled;
    }

    public int speedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(int multiplier) {
        speedMultiplier = normalize(multiplier);
    }

    public void save() throws IOException {
        Properties values = read();
        values.setProperty(NOCLIP, Boolean.toString(noclip));
        values.setProperty(SPEED_MULTIPLIER, Integer.toString(speedMultiplier));
        Path directory = file.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "debug-", ".tmp");
        try {
            try (var output = Files.newOutputStream(temporary)) {
                values.store(output, null);
            }
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static int normalizeSpeed(String value) {
        try {
            return normalize(Integer.parseInt(value.trim()));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    /** 倍速只接受 1／2／4／8，非法值回落到 1，避免异常配置进入世界。 */
    private static int normalize(int multiplier) {
        return multiplier == 2 || multiplier == 4 || multiplier == 8 ? multiplier : 1;
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
