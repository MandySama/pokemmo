package top.totodile.pokemmo.world;

import org.w3c.dom.Element;
import top.totodile.pokemmo.ClientFiles;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.util.*;

/** 章节、地图与可重播场景共享一份只读目录。 */
public final class StoryReplayCatalog {
    public record Data(Map<String, String> values, Map<String, List<Data>> children) {
        public String text(String key) { return values.getOrDefault(key, ""); }
        public int number(String key) { return number(key, 0); }
        public int number(String key, int fallback) { return values.containsKey(key) ? Integer.parseInt(text(key)) : fallback; }
        public List<Data> list(String key) { return children.getOrDefault(key, List.of()); }
        public Data child(String key) { return list(key).isEmpty() ? EMPTY : list(key).getFirst(); }
        public List<Data> entries(String key) { return child(key).list("entry"); }
    }
    private static final Data EMPTY = new Data(Map.of(), Map.of());
    private final Map<String, Data> maps = new LinkedHashMap<>(), chapters = new LinkedHashMap<>(), scenes = new LinkedHashMap<>();

    public StoryReplayCatalog() throws IOException {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            var root = read(factory.newDocumentBuilder().parse(ClientFiles.data("story/kanto-replay.xml").toFile()).getDocumentElement());
            index(root.list("map"), "key", maps);
            index(root.list("chapter"), "id", chapters);
            index(root.list("scene"), "id", scenes);
            for (var scene : scenes.values()) {
                var chapter = chapter(scene.text("chapter"));
                map(scene.text("map"));
                if (!nodes(chapter).containsKey(scene.text("node"))) throw new IOException("Unknown replay entry node");
            }
        } catch (Exception error) { throw new IOException("Unable to load replay catalog", error); }
    }
    private static void index(List<Data> data, String key, Map<String, Data> target) {
        for (var value : data) if (target.put(value.text(key), value) != null) throw new IllegalArgumentException("Duplicate replay key");
    }
    private static Data read(Element element) {
        Map<String, String> values = new LinkedHashMap<>();
        var attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) values.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
        Map<String, List<Data>> children = new LinkedHashMap<>();
        for (var node = element.getFirstChild(); node != null; node = node.getNextSibling())
            if (node instanceof Element child) children.computeIfAbsent(child.getTagName(), ignored -> new ArrayList<>()).add(read(child));
        children.replaceAll((key, list) -> List.copyOf(list));
        return new Data(Map.copyOf(values), Map.copyOf(children));
    }
    public List<Data> scenes() { return List.copyOf(scenes.values()); }
    public Data scene(String id) { return required(scenes, id); }
    public Data map(String id) { return required(maps, id); }
    public Data chapter(String id) { return required(chapters, id); }
    public Map<String, Data> nodes(Data chapter) {
        Map<String, Data> result = new LinkedHashMap<>();
        index(chapter.child("nodes").list("node"), "id", result);
        return result;
    }
    private static Data required(Map<String, Data> values, String key) {
        var value = values.get(key);
        if (value == null) throw new IllegalArgumentException("Unknown replay key " + key);
        return value;
    }
}
