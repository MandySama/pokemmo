package top.totodile.pokemmo.client.resources;

import org.w3c.dom.Element;

import top.totodile.pokemmo.client.rom.RomFilePicker;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

/** 参考 ModManagerWindow.Bn/No：校验原包、按原名复制；不修改启用配置。 */
public final class ModImporter {
    public record Mod(
            String filename, String name, String author, String version, String description) {
        public Mod(String filename, String name, String author, String version) {
            this(filename, name, author, version, "");
        }
    }

    public static final class Failure extends IOException {
        public final int stringId;

        Failure(int stringId, Throwable cause) {
            super(cause);
            this.stringId = stringId;
        }
    }

    private final Path directory;

    @FunctionalInterface
    interface BeforeDelete {
        void release(Path path) throws IOException;
    }

    private final BeforeDelete beforeDelete;

    public ModImporter(Path directory) {
        this(directory, path -> {});
    }

    ModImporter(Path directory, BeforeDelete beforeDelete) {
        this.directory = directory.toAbsolutePath().normalize();
        this.beforeDelete = beforeDelete;
    }

    public Path directory() {
        return directory;
    }

    /** 删除前释放已加载归档；只允许删除 MOD 目录中的单个文件，不递归。 */
    public void delete(Mod mod) throws IOException {
        String name = mod.filename();
        if (name == null
                || name.isBlank()
                || name.contains("/")
                || name.contains("\\")
                || name.contains(":")) throw new IOException("Invalid MOD filename");
        Path file = directory.resolve(name).normalize();
        if (!directory.equals(file.getParent())
                || Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)
                || !(name.toLowerCase(Locale.ROOT).endsWith(".zip")
                        || name.toLowerCase(Locale.ROOT).endsWith(".mod")))
            throw new IOException("Invalid MOD file");
        beforeDelete.release(file);
        Files.deleteIfExists(file);
    }

    /** 与参考一致，只读取压缩包根目录的 icon.png；不解压或启用 MOD。 */
    public byte[] icon(Mod mod) throws IOException {
        try (ZipFile archive = new ZipFile(directory.resolve(mod.filename()).toFile())) {
            var entry = archive.getEntry("icon.png");
            if (entry == null || entry.isDirectory()) return null;
            if (entry.getSize() > 4 * 1024 * 1024) throw new IOException("Mod icon too large");
            try (InputStream input = archive.getInputStream(entry);
                    var bytes = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                for (int count; (count = input.read(buffer)) != -1; ) {
                    if (bytes.size() + count > 4 * 1024 * 1024)
                        throw new IOException("Mod icon too large");
                    bytes.write(buffer, 0, count);
                }
                return bytes.toByteArray();
            }
        }
    }

    public Mod importMod(RomFilePicker.Source source, BiConsumer<Integer, Double> progress)
            throws Failure {
        String name = source.name();
        if (name == null
                || name.isBlank()
                || name.equals(".")
                || name.equals("..")
                || name.contains("/")
                || name.contains("\\")
                || !(name.toLowerCase(Locale.ROOT).endsWith(".mod")
                        || name.toLowerCase(Locale.ROOT).endsWith(".zip")))
            throw new Failure(1226, null);
        Path target = directory.resolve(name), temporary = null;
        if (Files.exists(target)) throw new Failure(1232, null);
        try {
            Files.createDirectories(directory);
            temporary = Files.createTempFile(directory, "import-", ".tmp");
            // Android 文档流先落临时文件，PC 可先校验原文件再复制。
            Path original = source.path();
            if (original == null) {
                progress.accept(1139, 0d);
                try (InputStream input = source.open()) {
                    Files.copy(input, temporary, StandardCopyOption.REPLACE_EXISTING);
                }
                original = temporary;
            }
            progress.accept(1095, -1d);
            Mod mod;
            try {
                mod = validateImport(original, name);
            } catch (Exception error) {
                throw new Failure(1226, error);
            }
            if (!original.equals(temporary)) {
                progress.accept(1139, 0d);
                long size = Files.size(original), copied = 0;
                try (InputStream input = Files.newInputStream(original);
                        OutputStream output = Files.newOutputStream(temporary)) {
                    byte[] buffer = new byte[65536];
                    for (int count; (count = input.read(buffer)) != -1; ) {
                        output.write(buffer, 0, count);
                        copied += count;
                        progress.accept(
                                1139, size == 0 ? 0d : Math.min(1d, (double) copied / size));
                    }
                }
            }
            // 不使用 REPLACE_EXISTING；并发导入或同名文件均不得覆盖。
            try {
                Files.move(temporary, target);
            } catch (FileAlreadyExistsException duplicate) {
                throw new Failure(1232, duplicate);
            }
            return mod;
        } catch (Failure error) {
            throw error;
        } catch (Exception error) {
            throw new Failure(1138, error);
        } finally {
            if (temporary != null)
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                }
        }
    }

    /** 仅枚举元信息，不注册主题/资源，也不启用 MOD。 */
    public List<Mod> list() {
        List<Mod> mods = new ArrayList<>();
        if (!Files.isDirectory(directory)) return mods;
        try (var files = Files.list(directory)) {
            files.filter(Files::isRegularFile)
                    .sorted(Comparator.reverseOrder())
                    .forEach(
                            path -> {
                                String name =
                                        path.getFileName().toString().toLowerCase(Locale.ROOT);
                                if (!name.endsWith(".zip") && !name.endsWith(".mod")) return;
                                try {
                                    mods.add(inspect(path, path.getFileName().toString()));
                                } catch (Exception ignored) {
                                    /* 无效包不进入已导入列表。 */
                                }
                            });
        } catch (IOException ignored) {
        }
        return mods;
    }

    static Mod inspect(Path path, String filename) throws Exception {
        try (ZipFile archive = new ZipFile(path.toFile())) {
            var entry = archive.getEntry("info.xml");
            if (entry == null || entry.isDirectory() || entry.getSize() > 1024 * 1024)
                throw new IOException("Missing mod info.xml");
            try (InputStream input = archive.getInputStream(entry)) {
                return readMetadata(input, filename);
            }
        }
    }

    /** ModManagerWindow.No：流式扫描根目录 info.xml；校验通过后才开始复制原包。 */
    private static Mod validateImport(Path path, String filename) throws Exception {
        try (var archive =
                new ZipInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            for (var entry = archive.getNextEntry();
                    entry != null;
                    entry = archive.getNextEntry()) {
                if (!entry.getName().equals("info.xml") || entry.isDirectory()) continue;
                if (entry.getSize() > 1024 * 1024) throw new IOException("Mod metadata too large");
                return readMetadata(archive, filename);
            }
            throw new IOException("Missing mod info.xml");
        }
    }

    private static Mod readMetadata(InputStream input, String filename) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        var xml = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        for (int count; (count = input.read(buffer)) != -1; ) {
            if (xml.size() + count > 1024 * 1024) throw new IOException("Mod metadata too large");
            xml.write(buffer, 0, count);
        }
        Element resource =
                factory.newDocumentBuilder()
                        .parse(new ByteArrayInputStream(xml.toByteArray()))
                        .getDocumentElement();
        if (!resource.getTagName().equals("resource"))
            throw new IOException("Invalid mod metadata");
        validateSection(resource, "themes", "theme", "theme_revision", 3);
        validateSection(resource, "strings", "string", "string_revision", 1);
        var overlays = resource.getElementsByTagName("overlays");
        if (overlays.getLength() > 1) throw new IOException("Duplicate overlays");
        if (overlays.getLength() == 1) {
            var entries = ((Element) overlays.item(0)).getElementsByTagName("overlay");
            for (int i = 0; i < entries.getLength(); i++)
                if (!((Element) entries.item(i)).hasAttribute("path"))
                    throw new IOException("Missing overlay path");
        }
        return new Mod(
                filename,
                resource.getAttribute("name"),
                resource.getAttribute("author"),
                resource.getAttribute("version"),
                resource.getAttribute("description"));
    }

    private static void validateSection(
            Element root, String sectionName, String entryName, String revisionName, int supported)
            throws IOException {
        var sections = root.getElementsByTagName(sectionName);
        if (sections.getLength() > 1) throw new IOException("Duplicate " + sectionName);
        if (sections.getLength() == 0) return;
        Element section = (Element) sections.item(0);
        var entries = section.getElementsByTagName(entryName);
        Set<String> names = new HashSet<>();
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            if (!entry.hasAttribute("path")) throw new IOException("Missing mod path");
            String revision =
                    section.hasAttribute(revisionName)
                            ? section.getAttribute(revisionName)
                            : entry.getAttribute("revision");
            try {
                if (Integer.parseInt(revision) > supported)
                    throw new IOException("Unsupported mod revision");
            } catch (NumberFormatException error) {
                throw new IOException("Invalid mod revision", error);
            }
            if (entryName.equals("theme")) {
                String name = entry.getAttribute("name"), mobile = entry.getAttribute("is_mobile");
                if (!entry.hasAttribute("name")
                        || name.equals("default")
                        || name.equals("android")
                        || !names.add(name)
                        || !(mobile.equals("true") || mobile.equals("false")))
                    throw new IOException("Invalid mod theme");
            }
        }
    }
}
