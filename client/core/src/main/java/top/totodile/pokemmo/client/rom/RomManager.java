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

        /** 成功选择 ROM 后通知，包括已加载的同一文件；由平台适配参考缓存失效规则。 */
        default void romImported(RomType type) {}
    }

    private final Path directory;
    private final Configuration configuration;
    private final Object importLock = new Object();
    private Map<RomType, RomInfo> loaded = Map.of();
    private List<String> errors = List.of();

    public RomManager(Path directory, Configuration configuration) {
        this.directory = directory.toAbsolutePath().normalize();
        this.configuration = Objects.requireNonNull(configuration);
        refresh();
    }

    public Path directory() {
        return directory;
    }

    public synchronized RomInfo get(RomType type) {
        return loaded.get(type);
    }

    public synchronized List<RomInfo> loaded() {
        return List.copyOf(loaded.values());
    }

    public synchronized List<String> errors() {
        return errors;
    }

    public synchronized void refresh() {
        Map<RomType, RomInfo> next = new EnumMap<>(RomType.class);
        List<String> failures = new ArrayList<>();
        Set<Path> candidates = new LinkedHashSet<>();
        for (RomType type : RomType.values()) {
            String value = configuration.selectedFile(type);
            if (!value.isBlank()) {
                try {
                    candidates.add(directory.resolve(value).normalize());
                } catch (InvalidPathException error) {
                    failures.add(value + ": 路径无效");
                }
            }
        }
        if (Files.isDirectory(directory)) {
            try (var paths = Files.list(directory)) {
                paths.filter(Files::isRegularFile)
                        .filter(
                                path -> {
                                    String name = path.toString().toLowerCase(Locale.ROOT);
                                    return name.endsWith(".gba") || name.endsWith(".nds");
                                })
                        .sorted()
                        .forEach(candidates::add);
            } catch (IOException error) {
                failures.add("无法读取 ROM 文件夹: " + error.getMessage());
            }
        }
        for (Path path : candidates) {
            try {
                RomInfo rom = RomReader.inspect(path);
                next.putIfAbsent(rom.type(), rom);
            } catch (IOException | RuntimeException error) {
                failures.add(path.getFileName() + ": " + error.getMessage());
            }
        }
        loaded = next;
        errors = List.copyOf(failures);
    }

    public enum ImportResult {
        IMPORTED,
        ALREADY_LOADED
    }

    @FunctionalInterface
    public interface Progress {
        void update(int stringId, double value);
    }

    public ImportResult importRom(RomType expected, RomFilePicker.Source source)
            throws IOException {
        return importRom(expected, source, value -> {});
    }

    public ImportResult importRom(
            RomType expected,
            RomFilePicker.Source source,
            java.util.function.DoubleConsumer progress)
            throws IOException {
        return importRom(expected, source, (stage, value) -> progress.accept(value));
    }

    public ImportResult importRom(RomType expected, RomFilePicker.Source source, Progress progress)
            throws IOException {
        // 复制不能持有 loaded 的监视器，否则渲染线程查询 ROM 时会被阻塞，进度框无法更新。
        synchronized (importLock) {
            ImportResult result = importSelected(expected, source, progress);
            configuration.romImported(expected);
            return result;
        }
    }

    private ImportResult importSelected(
            RomType expected, RomFilePicker.Source source, Progress progress) throws IOException {
        Path local = source.path();
        String localArchive = null;
        // 参考 ClientSettingsWindow.mh0/Le0：PC 先检查原文件，再决定解压或复制。
        if (local != null) {
            byte[] header = new byte[512];
            try (InputStream input = Files.newInputStream(local)) {
                header = Arrays.copyOf(header, readBlock(input, header));
            }
            localArchive = archiveType(header);
            checkArchive(localArchive, header);
            if (Files.size(local) > 512L * 1024 * 1024)
                throw new RomImportFailure(1182, "ROM 文件过大", null);
            if (localArchive == null) {
                validate(expected, local);
            }
        }
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "rom-", ".tmp");
        Path extracted = null;
        try {
            String archive = localArchive;
            // PC 压缩包由 libarchive 直接读取；只有无本地路径的文档流需要先落盘。
            if (local == null || archive == null)
                try (InputStream input = source.open();
                        OutputStream output = Files.newOutputStream(temporary)) {
                    byte[] prefix = new byte[512];
                    prefix = Arrays.copyOf(prefix, readBlock(input, prefix));
                    archive = archiveType(prefix);
                    checkArchive(archive, prefix);
                    progress.update(archive == null ? 1142 : 1091, 0);
                    output.write(prefix);
                    byte[] buffer = new byte[65536];
                    long size = prefix.length;
                    long total = source.path() == null ? 0 : Files.size(source.path());
                    for (int count; (count = input.read(buffer)) != -1; ) {
                        size += count;
                        if (size > 512L * 1024 * 1024)
                            throw new RomImportFailure(1182, "ROM 文件过大", null);
                        output.write(buffer, 0, count);
                        progress.update(
                                archive == null ? 1142 : 1091,
                                total > 0 ? Math.min(1, size / (double) total) : -1);
                    }
                }
            Path candidate = temporary;
            String extractedName = null;
            if (archive != null) {
                extracted = Files.createTempFile(directory, "rom-extracted-", ".tmp");
                progress.update(1091, -1);
                try {
                    extractedName =
                            LibArchiveRomExtractor.extract(
                                    local == null ? temporary : local,
                                    extracted,
                                    expected.extension,
                                    archive.equals("GZIP"),
                                    directory.getParent().resolve("cache/archive"),
                                    value -> progress.update(1091, value));
                } catch (RomImportFailure error) {
                    throw error;
                } catch (IOException | LinkageError error) {
                    throw new RomImportFailure(1090, "1", error);
                }
                candidate = extracted;
            }
            // 本地普通 ROM 已在复制前校验；复制结束仍属于文件保存阶段，不冒充解析进度。
            int finalStage = archive == null && local != null ? 1142 : 1095;
            progress.update(finalStage, finalStage == 1142 ? 1 : .95);
            RomInfo rom = validate(expected, candidate);
            // 重复选择也已完整复制到临时文件；替换原受管文件，不再跳过复制或创建重复副本。
            boolean repeated = alreadyLoaded(expected, candidate);
            Path replacement = repeated ? get(expected).path() : null;
            if (replacement == null
                    && local != null
                    && archive == null
                    && directory.equals(local.toAbsolutePath().normalize().getParent()))
                replacement = local.toAbsolutePath().normalize();
            if (replacement != null) {
                // 源输入流已经关闭；选择受管文件本身时也不会边读边覆盖。
                try {
                    configuration.selectFile(expected, replacement.getFileName().toString());
                } catch (RuntimeException error) {
                    throw new IOException("无法保存 ROM 配置", error);
                }
                try {
                    Files.move(
                            candidate,
                            replacement,
                            StandardCopyOption.ATOMIC_MOVE,
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException unsupported) {
                    Files.move(candidate, replacement, StandardCopyOption.REPLACE_EXISTING);
                }
                refresh();
                progress.update(finalStage, 1);
                return repeated ? ImportResult.ALREADY_LOADED : ImportResult.IMPORTED;
            }
            // 使用独立文件名；配置写入失败时不会覆盖当前正在使用的 ROM。
            Path target;
            if (extractedName != null || source.path() != null) {
                String name =
                        extractedName != null
                                ? extractedName
                                : source.path().getFileName().toString();
                if (extractedName != null
                        && archive.equals("GZIP")
                        && name.equals("imported." + expected.extension)) {
                    String original = source.name();
                    if (original != null && original.toLowerCase(Locale.ROOT).endsWith(".gz")) {
                        original = original.substring(0, original.length() - 3);
                        if (original.toLowerCase(Locale.ROOT).endsWith("." + expected.extension))
                            name = original;
                    }
                }
                try {
                    Path relative = Path.of(name);
                    if (relative.isAbsolute()
                            || relative.getNameCount() != 1
                            || name.contains("\\")
                            || name.contains("/"))
                        throw new InvalidPathException(name, "Not a filename");
                } catch (InvalidPathException error) {
                    name = rom.code() + "-" + UUID.randomUUID() + "." + expected.extension;
                }
                target = directory.resolve(name);
                int dot = name.lastIndexOf('.');
                String stem = dot > 0 ? name.substring(0, dot) : name;
                String suffix = dot > 0 ? name.substring(dot) : "." + expected.extension;
                for (int index = 2; Files.exists(target); index++)
                    target = directory.resolve(stem + "-" + index + suffix);
            } else
                target =
                        directory.resolve(
                                rom.code() + "-" + UUID.randomUUID() + "." + expected.extension);
            Files.move(candidate, target);
            try {
                configuration.selectFile(expected, target.getFileName().toString());
            } catch (RuntimeException error) {
                Files.deleteIfExists(target);
                throw new IOException("无法保存 ROM 配置", error);
            }
            refresh();
            progress.update(finalStage, 1);
            return ImportResult.IMPORTED;
        } finally {
            Files.deleteIfExists(temporary);
            if (extracted != null) Files.deleteIfExists(extracted);
        }
    }

    private boolean alreadyLoaded(RomType type, Path candidate) throws IOException {
        RomInfo existing = get(type);
        return existing != null
                && directory.equals(existing.path().getParent())
                && Files.isRegularFile(existing.path())
                && sameContent(existing.path(), candidate);
    }

    private static boolean sameContent(Path left, Path right) throws IOException {
        if (Files.size(left) != Files.size(right)) return false;
        // 避免 Files.mismatch / InputStream.readNBytes 对较旧 Android API 的依赖。
        try (InputStream a = Files.newInputStream(left);
                InputStream b = Files.newInputStream(right)) {
            byte[] first = new byte[65536], second = new byte[65536];
            for (int count; (count = readBlock(a, first)) > 0; ) {
                if (readBlock(b, second) != count) return false;
                for (int i = 0; i < count; i++) if (first[i] != second[i]) return false;
            }
            return b.read() == -1;
        }
    }

    private static int readBlock(InputStream input, byte[] buffer) throws IOException {
        int size = 0;
        while (size < buffer.length) {
            int count = input.read(buffer, size, buffer.length - size);
            if (count < 0) break;
            size += count;
        }
        return size;
    }

    private static RomInfo validate(RomType expected, Path path) throws IOException {
        RomInfo rom;
        try {
            rom = RomReader.inspect(path);
        } catch (RomReader.UnsupportedGame error) {
            throw new RomImportFailure(1183, error.getMessage(), error);
        } catch (IOException | RuntimeException error) {
            throw new RomImportFailure(1182, error.getMessage(), error);
        }
        if (rom.type() != expected) throw new RomImportFailure(1183, rom.code(), null);
        return rom;
    }

    private static String archiveType(byte[] prefix) {
        if (prefix.length >= 4
                && prefix[0] == 'P'
                && prefix[1] == 'K'
                && ((prefix[2] == 3 && prefix[3] == 4)
                        || (prefix[2] == 5 && prefix[3] == 6)
                        || (prefix[2] == 7 && prefix[3] == 8))) return "ZIP";
        if (prefix.length >= 6
                && prefix[0] == 'R'
                && prefix[1] == 'a'
                && prefix[2] == 'r'
                && prefix[3] == '!'
                && prefix[4] == 0x1a
                && prefix[5] == 7) return "RAR";
        if (prefix.length >= 6
                && prefix[0] == '7'
                && prefix[1] == 'z'
                && prefix[2] == (byte) 0xbc
                && prefix[3] == (byte) 0xaf
                && prefix[4] == 0x27
                && prefix[5] == 0x1c) return "7z";
        if (prefix.length >= 2 && prefix[0] == 0x1f && prefix[1] == (byte) 0x8b) return "GZIP";
        if (prefix.length >= 262
                && new String(prefix, 257, 5, java.nio.charset.StandardCharsets.US_ASCII)
                        .equals("ustar")) return "TAR";
        if (prefix.length >= 3 && prefix[0] == 'B' && prefix[1] == 'Z' && prefix[2] == 'h')
            return "Bzip2";
        if (prefix.length >= 6
                && prefix[0] == (byte) 0xfd
                && prefix[1] == '7'
                && prefix[2] == 'z'
                && prefix[3] == 'X'
                && prefix[4] == 'Z'
                && prefix[5] == 0) return "XZ";
        if (prefix.length >= 4
                && prefix[0] == 'L'
                && prefix[1] == 'Z'
                && prefix[2] == 'I'
                && prefix[3] == 'P') return "lzip";
        if (prefix.length >= 4
                && prefix[0] == 0x28
                && prefix[1] == (byte) 0xb5
                && prefix[2] == 0x2f
                && prefix[3] == (byte) 0xfd) return "Zstandard";
        return null;
    }

    private static void checkArchive(String archive, byte[] prefix) throws RomImportFailure {
        if (archive != null
                && (!List.of("ZIP", "RAR", "7z", "GZIP", "TAR").contains(archive)
                        || archive.equals("ZIP") && prefix.length >= 4 && prefix[2] == 5))
            throw new RomImportFailure(1089, archive, null);
    }
}
