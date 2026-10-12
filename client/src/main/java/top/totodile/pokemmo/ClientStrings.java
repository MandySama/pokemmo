package top.totodile.pokemmo;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** 按字符串 ID 读取语言 XML，语言名称取自根元素的 lang_full 属性。 */
public final class ClientStrings {
    private static final Map<String, Map<Integer, String>> STRINGS = new LinkedHashMap<>();
    private static final Map<String, String> LANGUAGE_NAMES = new LinkedHashMap<>();
    private static String primaryLanguage;
    private static String selectedLanguage;

    static {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var builder = factory.newDocumentBuilder();
            try (var files = Files.list(ClientFiles.data("strings"))) {
                for (var file : files.filter(path -> path.getFileName().toString().endsWith(".xml"))
                        .sorted().toList()) {
                    try (var input = Files.newInputStream(file)) {
                        Element root = builder.parse(input).getDocumentElement();
                        String language = root.getAttribute("lang");
                        if (!root.getTagName().equals("strings") || language.isBlank()) {
                            throw new IllegalStateException("Invalid language XML: " + file);
                        }
                        Map<Integer, String> strings = STRINGS.computeIfAbsent(language, key -> new LinkedHashMap<>());
                        var entries = root.getElementsByTagName("string");
                        for (int i = 0; i < entries.getLength(); i++) {
                            Element entry = (Element) entries.item(i);
                            strings.put(Integer.parseInt(entry.getAttribute("id")),
                                    entry.getTextContent().replace("\\n", "\n"));
                        }
                        String name = root.getAttribute("lang_full");
                        LANGUAGE_NAMES.put(language, name.isBlank() ? language : name);
                        if (root.getAttribute("is_primary").equals("1")) primaryLanguage = language;
                    }
                }
            }
            if (STRINGS.isEmpty()) throw new IllegalStateException("No client languages found");
            if (primaryLanguage == null) primaryLanguage = STRINGS.keySet().iterator().next();
            selectedLanguage = STRINGS.containsKey("zh") ? "zh" : primaryLanguage;
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to load client strings", exception);
        } catch (ParserConfigurationException | SAXException exception) {
            throw new IllegalStateException("Unable to parse client strings", exception);
        }
    }

    private ClientStrings() {
    }

    /** 缺失条目回退到 is_primary 指定的主语言表。 */
    public static String get(int id) {
        String text = STRINGS.get(selectedLanguage).get(id);
        if (text == null) text = STRINGS.get(primaryLanguage).get(id);
        if (text == null) throw new IllegalArgumentException("Unknown client string ID: " + id);
        return text.contains("{STRING_0}") ? text.replace("{STRING_0}", get(0)) : text;
    }

    public static String[] languageNames() {
        return LANGUAGE_NAMES.values().toArray(String[]::new);
    }

    public static int selectedIndex() {
        return new ArrayList<>(LANGUAGE_NAMES.keySet()).indexOf(selectedLanguage);
    }

    public static void select(int index) {
        selectedLanguage = new ArrayList<>(LANGUAGE_NAMES.keySet()).get(index);
    }
}
