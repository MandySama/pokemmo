package top.totodile.pokemmo.client.resources;

import com.badlogic.gdx.files.FileHandle;
import java.io.BufferedInputStream;
import java.util.*;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/** 对齐参考项目 XmlDocumentDomSerializer / ThemeXmlLayoutLoader 的语言容器加载流程。 */
public final class ClientStrings {
    private static final Logger LOG = Logger.getLogger(ClientStrings.class.getName());
    private static final Pattern REFERENCES = Pattern.compile("\\{STRING_(\\d+)\\}");
    private static final Pattern VARIABLES = Pattern.compile("\\{([A-F0-9]{2,4})\\}");
    private final List<Container> containers = new ArrayList<>();
    private final Map<Integer, String> messages = new LinkedHashMap<>();
    private final Map<String, String> archives = new LinkedHashMap<>();
    private final Map<Integer, String> preloaded = new LinkedHashMap<>();
    private IntFunction<String> variables = id -> "";
    private String language;
    private String typeface;

    public ClientStrings(FileHandle file) {
        containers.add(new Container(file, true));
        select(containers.get(0).language);
    }

    public static ClientStrings load(FileHandle directory, String requestedLanguage) {
        FileHandle[] files = directory.list(".xml");
        Arrays.sort(files, Comparator.comparing(FileHandle::name));
        return new ClientStrings(Arrays.asList(files), requestedLanguage);
    }

    ClientStrings(List<FileHandle> files, String requestedLanguage) {
        this(requestedLanguage, files.stream().map(file -> new Source(file, true)).toArray(Source[]::new));
    }

    static final class Source {
        final FileHandle file;
        final boolean bundled;
        Source(FileHandle file, boolean bundled) { this.file = file; this.bundled = bundled; }
    }

    static void validate(Source source) { new Container(source.file, source.bundled); }

    ClientStrings(String requestedLanguage, Source... sources) {
        for (Source source : sources) {
            try { containers.add(new Container(source.file, source.bundled)); }
            catch (RuntimeException error) { LOG.log(Level.WARNING, "Error parsing string container " + source.file.path(), error); }
        }
        select(requestedLanguage == null ? preferredLanguage(Locale.getDefault()) : requestedLanguage);
    }

    public String language() { return language; }
    public String typeface() { return typeface; }
    public List<String> languages() {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Container container : containers) result.add(container.language);
        return new ArrayList<>(result);
    }
    public String languageName(String code) {
        for (Container container : containers)
            if (container.language.equalsIgnoreCase(code) && container.languageName.length() > code.length()) return container.languageName;
        return code;
    }
    public void setVariableResolver(IntFunction<String> resolver) { variables = Objects.requireNonNull(resolver); }

    public void select(String code) {
        messages.clear();
        archives.clear();
        preloaded.clear();
        typeface = null;
        Container primary = containers.stream().filter(value -> value.primary).findFirst().orElse(null);
        if (primary == null) LOG.warning("Unable to find a primary string container.");
        language = code.toLowerCase(Locale.ENGLISH);
        List<Container> selected = new ArrayList<>();
        for (Container value : containers) if (value.language.equals(language) && value != primary) selected.add(value);
        // 参考 qj：归档先于普通字符串；覆盖包最后应用。同一资源来源按扫描顺序稳定排序。
        selected.sort(Comparator.comparingInt((Container value) -> value.archive ? 0 : 1)
                .thenComparing(value -> value.override).thenComparing(value -> !value.bundled));
        if (primary != null && !primary.override) typeface = primary.typeface;
        for (Container value : selected)
            if (value.typeface != null && !value.override) typeface = value.typeface;
        // 对齐 t2(false)：先主语言，再按优先级应用所选语言容器。
        if (primary != null) apply(primary, false);
        for (Container value : selected) apply(value, false);
    }

    String preferredLanguage(Locale locale) {
        String code = locale.getLanguage();
        if (!locale.getCountry().isEmpty()) code += "-" + locale.getCountry();
        if (locale.getScript().equalsIgnoreCase("hant") || code.regionMatches(true, 0, "zh-TW", 0, 5)
                || code.regionMatches(true, 0, "zh-HK", 0, 5)) code = "zh-Hant";
        while (code.indexOf('-') != code.lastIndexOf('-')) code = code.substring(0, code.lastIndexOf('-'));
        String selected = "";
        for (Container container : containers) {
            if (container.language.equalsIgnoreCase(code)) return container.language;
            String prefix = container.language.substring(0, Math.min(2, container.language.length()));
            if (code.regionMatches(true, 0, prefix, 0, prefix.length())
                    && (selected.isEmpty() || container.language.length() < selected.length())) selected = container.language;
        }
        if (!selected.isEmpty()) return selected;
        return containers.stream().filter(value -> value.primary).findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to find a primary string container.")).language;
    }

    private void apply(Container container, boolean raw) {
        if (raw && container.typeface != null && !container.override) typeface = container.typeface;
        if (container.archive) {
            if (!raw) archives.putAll(container.archiveMessages);
            return;
        }
        preloaded.putAll(container.preloaded);
        if (raw) messages.putAll(container.messages);
        else {
            // 对齐 sE0(false)：遍历容器两次解析 STRING 引用，后加载容器可以覆盖已有 ID。
            for (int pass = 0; pass < 2; pass++)
                for (Map.Entry<Integer, String> entry : container.messages.entrySet())
                    messages.put(entry.getKey(), expandReferences(entry.getValue()));
        }
    }

    private String expandReferences(String value) {
        Matcher matcher = REFERENCES.matcher(value);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement;
            try { replacement = messages.get(Integer.parseInt(matcher.group(1))); }
            catch (NumberFormatException ignored) { replacement = null; }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement == null ? matcher.group() : replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public String get(int id) {
        String value = messages.get(id);
        if (value == null) return "STRING_" + id;
        return substituteVariables(value);
    }

    public String format(int id, String... arguments) {
        String value = messages.get(id);
        if (value == null) return "STRING_" + id;
        for (int index = 0; index < arguments.length; index++)
            value = value.replace("{" + String.format(Locale.ROOT, "%02X", index) + "}", arguments[index]);
        Matcher remainder = Pattern.compile("\\{([A-F0-9]{2,4})\\+\\}").matcher(value);
        if (arguments.length > 0 && remainder.find()) {
            int first = Integer.parseInt(remainder.group(1),16)&255;
            String joined = first >= arguments.length ? "" : String.join("/",Arrays.copyOfRange(arguments,first,arguments.length));
            value = value.replace(remainder.group(), joined);
        }
        return substituteVariables(value);
    }

    public String getOrDefault(int id, String fallback) {
        String value = messages.get(id);
        return value == null ? fallback : substituteVariables(value);
    }

    public String preloaded(int id) { return preloaded.get(id); }

    private String substituteVariables(String value) {
        Matcher matcher = VARIABLES.matcher(value);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement = variables.apply(Integer.parseInt(matcher.group(1), 16) & 255);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement == null ? "" : replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public String archive(int region, int type, int table, int block, int entry) {
        return archives.get(archiveKey(region, type, table, block, entry));
    }
    private static String archiveKey(int region, int type, int table, int block, int entry) {
        return region + ":" + type + ":" + table + ":" + block + ":" + entry;
    }
    private static String text(String value, boolean archive) {
        if (!archive) value = value.replace("|br|", "\n").replace("|nb2|", "\n\n").replaceAll("\\|nb[0-9]?\\|", "\n");
        return value.replace("\\n", "\n");
    }

    private static final class Container {
        final Map<Integer, String> messages = new LinkedHashMap<>();
        final Map<Integer, String> preloaded = new LinkedHashMap<>();
        final Map<String, String> archiveMessages = new LinkedHashMap<>();
        final boolean bundled;
        String language;
        String languageName;
        String typeface;
        boolean primary;
        boolean override;
        boolean archive;
        int region = -1;
        int archiveType;

        Container(FileHandle file, boolean bundled) {
            this.bundled = bundled;
            try (BufferedInputStream input = new BufferedInputStream(file.read(), 2048)) {
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(input, null);
                parser.nextTag();
                archive = "ds_strings_archive".equals(parser.getName());
                if (!archive && !"strings".equals(parser.getName())) throw new IllegalArgumentException("Invalid string container root");
                language = parser.getAttributeValue(null, "lang");
                if (language == null || !language.matches("^[a-zA-Z\\-_0-9]+$")) throw new IllegalArgumentException("Invalid language code");
                language = language.toLowerCase(Locale.ENGLISH);
                languageName = parser.getAttributeValue(null, "lang_full");
                if (languageName == null || languageName.isEmpty()) languageName = language;
                typeface = parser.getAttributeValue(null, "typeface");
                primary = flag(parser, "is_primary");
                override = flag(parser, "is_override");
                String regionValue = parser.getAttributeValue(null, "region_id");
                if (regionValue != null) region = Integer.parseInt(regionValue);
                if (archive) {
                    archiveType = Integer.parseInt(parser.getAttributeValue(null, "archive_type"));
                    if (region < 0 || region >= 5 || archiveType < 0 || archiveType > 1) throw new IllegalArgumentException("Invalid string archive region/type");
                }
                int event;
                while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                    if (event != XmlPullParser.START_TAG || !"string".equals(parser.getName())) continue;
                    String idText = parser.getAttributeValue(null, "id");
                    String tableText = parser.getAttributeValue(null, "table_id");
                    String blockText = parser.getAttributeValue(null, "block_id");
                    String entryText = parser.getAttributeValue(null, "entry_id");
                    boolean preload = "1".equals(parser.getAttributeValue(null, "preload"));
                    Integer id = null, table = null, block = null, entry = null;
                    try {
                        if (archive) {
                            table = Integer.valueOf(tableText); block = Integer.valueOf(blockText); entry = Integer.valueOf(entryText);
                        } else id = Integer.valueOf(idText);
                    } catch (NumberFormatException ignored) { }
                    String value = parser.nextText();
                    if (!archive && id != null && !value.isEmpty()) {
                        String converted = text(value, false);
                        messages.put(id, converted);
                        if (preload) preloaded.put(id, converted);
                    }
                    else if (archive && table != null && block != null && entry != null && table >= 0 && block >= 0 && entry >= 0)
                        archiveMessages.put(archiveKey(region, archiveType, table, block, entry), text(value, true));
                }
            } catch (Exception error) { throw new IllegalArgumentException("Error parsing string container " + file.path(), error); }
        }
        private static boolean flag(XmlPullParser parser, String name) {
            String value = parser.getAttributeValue(null, name);
            return "1".equals(value) || "true".equals(value);
        }
    }
}
