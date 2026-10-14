package top.totodile.pokemmo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/** 管理导入文件及路径配置，不解析游戏资源。 */
public final class RomFiles {
    private static final int[] TYPES = {2, 0, 1, 3, 4, 5};
    private static final String[] KEYS = {"client.roms.nds2", "client.roms.fr", "client.roms.em",
            "client.roms.nds3", "client.roms.nds", "client.roms.em2"};
    private final Path directory;
    private final Path configuration;
    private final Properties entries = new Properties();

    public RomFiles(Path directory) throws IOException {
        this.directory = directory.toAbsolutePath().normalize();
        configuration = this.directory.getParent().resolve("config/main.properties");
        if (Files.isRegularFile(configuration)) {
            try (InputStream input = Files.newInputStream(configuration)) {
                entries.load(input);
            }
        }
        // 将旧版选择记录迁移到统一配置，保留原始文件供恢复使用。
        Path manifest = this.directory.resolve("selected.properties");
        if (Files.isRegularFile(manifest)) {
            Properties legacy = new Properties();
            try (InputStream input = Files.newInputStream(manifest)) {
                legacy.load(input);
            }
            for (int index = 0; index < TYPES.length; index++) {
                String name = legacy.getProperty(Integer.toString(TYPES[index]));
                if (entries.containsKey(KEYS[index]) || name == null) continue;
                Path file = this.directory.resolve(name).normalize();
                if (!this.directory.equals(file.getParent())) continue;
                try {
                    RomHeader.validate(index, file);
                } catch (IOException exception) {
                    continue;
                }
                importFile(index, file);
            }
        }
    }

    public synchronized Path selected(int index) {
        String name = entries.getProperty(KEYS[index]);
        if (name == null || name.isBlank()) return null;
        try {
            Path file = Path.of(name);
            RomHeader.validate(index, file);
            return file;
        } catch (IOException | java.nio.file.InvalidPathException exception) {
            return null;
        }
    }

    public Path directory() {
        return directory;
    }

    /** 复制到临时文件，校验通过后移除导入标记，最后原子保存路径配置。 */
    public synchronized Path importFile(int index, Path source) throws IOException {
        RomHeader.validate(index, source);
        Files.createDirectories(directory);
        Files.createDirectories(configuration.getParent());
        String originalName = source.getFileName().toString();
        int dot = originalName.lastIndexOf('.');
        String stem = dot > 0 ? originalName.substring(0, dot) : originalName;
        String extension = dot > 0 ? originalName.substring(dot) : "";
        String name = originalName;
        Path imported = directory.resolve(name);
        // 同名文件保留原有内容，仅在新副本名称末尾追加序号。
        int suffix = 2;
        while (name.equalsIgnoreCase("importing.tmp") || Files.exists(imported) || Files.exists(directory.resolve(name + "_import"))) {
            name = stem + " (" + suffix++ + ")" + extension;
            imported = directory.resolve(name);
        }
        Path temporary = directory.resolve("importing.tmp");
        Path pending = directory.resolve(name + "_import");
        Properties next = new Properties();
        next.putAll(entries);
        // 其他设置可能在本次运行中更新，导入 ROM 时保留磁盘上的最新值。
        if (Files.isRegularFile(configuration)) {
            try (InputStream input = Files.newInputStream(configuration)) {
                next.load(input);
            }
        }
        next.setProperty(KEYS[index], imported.toString());
        Path settingsTemporary = Files.createTempFile(configuration.getParent(), "main-", ".tmp");
        boolean saved = false;
        try {
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
            RomHeader.validate(index, temporary);
            Files.move(temporary, pending, StandardCopyOption.ATOMIC_MOVE);
            RomHeader.validate(index, pending);
            Files.move(pending, imported, StandardCopyOption.ATOMIC_MOVE);
            try (OutputStream output = Files.newOutputStream(settingsTemporary)) {
                next.store(output, null);
            }
            Files.move(settingsTemporary, configuration,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            entries.clear();
            entries.putAll(next);
            saved = true;
            return imported;
        } finally {
            Files.deleteIfExists(temporary);
            Files.deleteIfExists(pending);
            Files.deleteIfExists(settingsTemporary);
            if (!saved) Files.deleteIfExists(imported);
        }
    }
}
