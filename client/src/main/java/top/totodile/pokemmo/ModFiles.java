package top.totodile.pokemmo;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.zip.ZipFile;

/** 只管理 MOD 包及元信息，不将包中的资源应用到游戏。 */
public final class ModFiles {
    private static final String ENABLED = "client.mods.enabled_mods";
    private static final String VERBOSE = "client.mods.debugs.verbose.enabled";
    private final Path directory;
    private final Path config;
    private final List<String> enabled = new ArrayList<>();
    private List<Entry> entries = List.of();
    private boolean verbose;

    public record Entry(Path file, String name, String author, String version, String description,
                        String website, BufferedImage icon) {
        public String filename() {
            return file.getFileName().toString();
        }
    }

    public ModFiles(Path directory, Path config) throws IOException {
        this.directory = directory.toAbsolutePath().normalize();
        this.config = config.toAbsolutePath().normalize();
        Properties values = readConfig();
        for (String name : values.getProperty(ENABLED, "").split("/")) {
            if (!name.isBlank() && !enabled.contains(name)) enabled.add(name);
        }
        verbose = Boolean.parseBoolean(values.getProperty(VERBOSE, "false"));
    }

    public Path directory() {
        return directory;
    }

    public boolean verbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean enabled(Entry entry) {
        return enabled.contains(entry.filename());
    }

    public void setEnabled(Entry entry, boolean active) {
        enabled.remove(entry.filename());
        if (active) enabled.add(0, entry.filename());
    }

    public int order(Entry entry) {
        return enabled.indexOf(entry.filename());
    }

    public int enabledCount() {
        return enabled.size();
    }

    public void move(Entry entry, int delta) {
        int index = order(entry);
        int next = index + delta;
        if (index >= 0 && next >= 0 && next < enabled.size()) java.util.Collections.swap(enabled, index, next);
    }

    public List<Entry> entries() {
        return entries.stream().sorted(Comparator.comparingInt((Entry entry) -> enabled(entry) ? 0 : 1)
                .thenComparingInt(entry -> enabled(entry) ? order(entry) : 0)
                .thenComparing(Entry::filename, Comparator.reverseOrder())).toList();
    }

    public void scan() throws IOException {
        List<Entry> found = new ArrayList<>();
        if (Files.isDirectory(directory)) {
            try (var files = Files.list(directory)) {
                for (Path file : files.filter(Files::isRegularFile).filter(ModFiles::supported).sorted().toList()) {
                    try {
                        found.add(inspect(file));
                    } catch (IOException exception) {
                        log("Invalid MOD " + file.getFileName() + ": " + exception.getMessage());
                    }
                }
            }
        }
        entries = List.copyOf(found);
        enabled.removeIf(name -> entries.stream().noneMatch(entry -> entry.filename().equals(name)));
        if (verbose) log("Scanned " + entries.size() + " MOD packages");
    }

    public Entry importFile(Path source) throws IOException {
        if (!supported(source) || !Files.isRegularFile(source)) throw new IOException(ClientStrings.get(1226));
        Files.createDirectories(directory);
        Path destination = directory.resolve(source.getFileName()).normalize();
        if (!destination.getParent().equals(directory)) throw new IOException(ClientStrings.get(1226));
        if (Files.exists(destination)) throw new IOException(ClientStrings.get(1232));
        Path temporary = Files.createTempFile(directory, ".import-", ".tmp");
        try {
            Files.copy(source, temporary, StandardCopyOption.REPLACE_EXISTING);
            Entry parsed = inspect(temporary);
            // 不覆盖已有文件；验证通过后才发布完整包。
            Files.move(temporary, destination);
            Entry imported = new Entry(destination, parsed.name(), parsed.author(), parsed.version(),
                    parsed.description(), parsed.website(), parsed.icon());
            List<Entry> updated = new ArrayList<>(entries);
            updated.add(imported);
            entries = List.copyOf(updated);
            enabled.add(0, imported.filename());
            log("Imported " + imported.filename());
            return imported;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public void delete(Entry entry) throws IOException {
        Path file = entry.file().toAbsolutePath().normalize();
        if (!file.getParent().equals(directory) || !entries.contains(entry)) throw new IOException("Invalid MOD path");
        Files.delete(file);
        entries = entries.stream().filter(item -> !item.equals(entry)).toList();
        enabled.remove(entry.filename());
        log("Deleted " + entry.filename());
    }

    /** 写入前重新读取磁盘，保留 ROM、音量等其他设置。 */
    public boolean save() throws IOException {
        Properties values = readConfig();
        String names = String.join("/", enabled);
        boolean changed = !names.equals(values.getProperty(ENABLED, ""));
        values.setProperty(ENABLED, names);
        values.setProperty(VERBOSE, Boolean.toString(verbose));
        Files.createDirectories(config.getParent());
        Path temporary = Files.createTempFile(config.getParent(), "mods-", ".tmp");
        try {
            try (var output = Files.newOutputStream(temporary)) {
                values.store(output, null);
            }
            Files.move(temporary, config, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
        return changed;
    }

    private Properties readConfig() throws IOException {
        Properties values = new Properties();
        if (Files.isRegularFile(config)) {
            try (var input = Files.newInputStream(config)) {
                values.load(input);
            }
        }
        return values;
    }

    private static boolean supported(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".mod") || name.endsWith(".zip");
    }

    private static Entry inspect(Path file) throws IOException {
        try (var input = Files.newInputStream(file)) {
            if (!java.util.Arrays.equals(input.readNBytes(4), new byte[]{80, 75, 3, 4})) {
                throw new IOException(ClientStrings.get(1226));
            }
        }
        try (ZipFile zip = new ZipFile(file.toFile())) {
            var info = zip.getEntry("info.xml");
            if (info == null || info.isDirectory()) throw new IOException("Missing root info.xml");
            Element resource;
            try (var input = zip.getInputStream(info)) {
                var factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                var builder = factory.newDocumentBuilder();
                builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
                NodeList resources = builder.parse(new ByteArrayInputStream(readLimited(input, 1024 * 1024)))
                        .getElementsByTagName("resource");
                if (resources.getLength() == 0) throw new IOException("Missing resource element");
                resource = (Element) resources.item(0);
                validateSection(resource, "strings", "string", "string_revision", 1);
                validateSection(resource, "themes", "theme", "theme_revision", 3);
                if (resource.getElementsByTagName("overlays").getLength() > 1) throw new IOException("Duplicate overlays");
                NodeList overlays = resource.getElementsByTagName("overlay");
                for (int i = 0; i < overlays.getLength(); i++) {
                    if (!((Element) overlays.item(i)).hasAttribute("path")) throw new IOException("Missing overlay path");
                }
            }
            BufferedImage icon = null;
            var image = zip.getEntry("icon.png");
            if (image != null && !image.isDirectory()) {
                try (var input = zip.getInputStream(image)) {
                    icon = readIcon(readLimited(input, 8 * 1024 * 1024));
                }
            }
            return new Entry(file.toAbsolutePath().normalize(), resource.getAttribute("name"),
                    resource.getAttribute("author"), resource.getAttribute("version"),
                    resource.getAttribute("description"), validWebsite(resource.getAttribute("weblink")), icon);
        } catch (Exception exception) {
            throw new IOException(ClientStrings.get(1226), exception);
        }
    }

    private static void validateSection(Element resource, String sectionName, String entryName,
                                        String revisionName, int maximum) throws IOException {
        NodeList sections = resource.getElementsByTagName(sectionName);
        if (sections.getLength() > 1) throw new IOException("Duplicate " + sectionName);
        if (sections.getLength() == 0) return;
        Element section = (Element) sections.item(0);
        NodeList items = section.getElementsByTagName(entryName);
        List<String> names = new ArrayList<>();
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            if (!item.hasAttribute("path")) throw new IOException("Missing path");
            String revision = section.hasAttribute(revisionName) ? section.getAttribute(revisionName) : item.getAttribute("revision");
            if (Integer.parseInt(revision) > maximum) throw new IOException("Unsupported revision");
            if (entryName.equals("theme")) {
                String name = item.getAttribute("name");
                if (!item.hasAttribute("name") || !item.hasAttribute("is_mobile")
                        || name.equals("default") || name.equals("android") || names.contains(name)) {
                    throw new IOException("Invalid theme metadata");
                }
                names.add(name);
            }
        }
    }

    private static byte[] readLimited(InputStream input, int maximum) throws IOException {
        byte[] bytes = input.readNBytes(maximum + 1);
        if (bytes.length > maximum) throw new IOException("MOD metadata too large");
        return bytes;
    }

    private static BufferedImage readIcon(byte[] bytes) throws IOException {
        try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IOException("Invalid MOD icon");
            var reader = readers.next();
            try {
                reader.setInput(stream);
                if (reader.getWidth(0) > 4096 || reader.getHeight(0) > 4096) throw new IOException("MOD icon too large");
                BufferedImage original = reader.read(0);
                BufferedImage thumbnail = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                var graphics = thumbnail.createGraphics();
                try {
                    graphics.drawImage(original, 0, 0, 64, 64, null);
                } finally {
                    graphics.dispose();
                }
                return thumbnail;
            } finally {
                reader.dispose();
            }
        }
    }

    private static String validWebsite(String value) {
        try {
            URI uri = URI.create(value);
            if (!"https".equalsIgnoreCase(uri.getScheme())) return "";
            String host = uri.getHost();
            return host != null && List.of("pokemmo.com", "forums.pokemmo.com", "pokemmo.eu", "forums.pokemmo.eu")
                    .contains(host.toLowerCase(Locale.ROOT)) ? value : "";
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }

    private void log(String message) {
        try {
            Path log = config.getParent().resolve("../log/mods.log").normalize();
            Files.createDirectories(log.getParent());
            Files.writeString(log, message + System.lineSeparator(), java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // 日志写入失败不影响包管理和配置保存。
        }
    }
}
