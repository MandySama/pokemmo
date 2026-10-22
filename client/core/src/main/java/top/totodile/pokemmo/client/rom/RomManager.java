package top.totodile.pokemmo.client.rom;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/** ROM 目录扫描、加载状态与导入；不依赖 UI 或客户端配置实现。 */
public final class RomManager {
    /** 由客户端组装层适配配置存储；未配置时 selectedFile 返回空字符串。 */
    public interface Configuration {
        String selectedFile(RomType type);
        void selectFile(RomType type, String filename);
    }

    private final Path directory;
    private final Configuration configuration;
    private Map<RomType, RomInfo> loaded=Map.of();
    private List<String> errors=List.of();
    public RomManager(Path directory, Configuration configuration) {
        this.directory=directory.toAbsolutePath().normalize(); this.configuration=Objects.requireNonNull(configuration); refresh();
    }
    public Path directory() { return directory; }
    public synchronized RomInfo get(RomType type) { return loaded.get(type); }
    public synchronized List<RomInfo> loaded() { return List.copyOf(loaded.values()); }
    public synchronized List<String> errors() { return errors; }
    public synchronized void refresh() {
        Map<RomType, RomInfo> next=new EnumMap<>(RomType.class);
        List<String> failures=new ArrayList<>();
        Set<Path> candidates=new LinkedHashSet<>();
        for (RomType type:RomType.values()) {
            String value=configuration.selectedFile(type);
            if (!value.isBlank()) {
                try { candidates.add(directory.resolve(value).normalize()); }
                catch (InvalidPathException error) { failures.add(value+": 路径无效"); }
            }
        }
        if (Files.isDirectory(directory)) {
            try (var paths=Files.list(directory)) {
                paths.filter(Files::isRegularFile).filter(path -> {
                    String name=path.toString().toLowerCase(Locale.ROOT);
                    return name.endsWith(".gba") || name.endsWith(".nds");
                }).sorted().forEach(candidates::add);
            } catch (IOException error) { failures.add("无法读取 ROM 文件夹: "+error.getMessage()); }
        }
        for (Path path:candidates) {
            try { RomInfo rom=RomReader.inspect(path); next.putIfAbsent(rom.type(), rom); }
            catch (IOException | RuntimeException error) { failures.add(path.getFileName()+": "+error.getMessage()); }
        }
        loaded=next; errors=List.copyOf(failures);
    }
    public void importRom(RomType expected, RomFilePicker.Source source) throws IOException {
        Files.createDirectories(directory);
        Path temporary=Files.createTempFile(directory, "rom-", ".tmp");
        try {
            try (InputStream input=source.open(); OutputStream output=Files.newOutputStream(temporary)) {
                byte[] buffer=new byte[65536]; long size=0;
                for (int count;(count=input.read(buffer))!=-1;) {
                    size+=count;
                    if (size>512L*1024*1024) throw new IOException("ROM 文件过大");
                    output.write(buffer,0,count);
                }
            }
            RomInfo rom=RomReader.inspect(temporary);
            if (rom.type()!=expected) throw new IOException("ROM 类型与所选行不符 ("+rom.code()+")");
            // 使用独立文件名；配置写入失败时不会覆盖当前正在使用的 ROM。
            Path target=directory.resolve(rom.code()+"-"+UUID.randomUUID()+"."+expected.extension);
            Files.move(temporary,target);
            try { configuration.selectFile(expected, target.getFileName().toString()); }
            catch (RuntimeException error) { Files.deleteIfExists(target); throw new IOException("无法保存 ROM 配置", error); }
            refresh();
        } finally { Files.deleteIfExists(temporary); }
    }
}
