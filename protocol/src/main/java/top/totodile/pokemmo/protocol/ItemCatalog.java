package top.totodile.pokemmo.protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 道具目录只含编号、背包分类和堆叠限制，服务端无需读取 ROM 图像。 */
public final class ItemCatalog {
    public record Item(int id, int pocket, int maxStack, int region, int nameId, int iconId, int source) {
        public boolean addable() { return id > 0; }
        public int descriptionId() { return DESCRIPTIONS.getOrDefault(id,130000+id); }
    }
    private static final Map<Integer, Item> ITEMS = load();
    private static final Map<Integer,Integer> DESCRIPTIONS = loadDescriptions();
    private ItemCatalog() { }
    public static Collection<Item> items() { return ITEMS.values(); }
    public static Item get(int id) { return ITEMS.get(id); }
    private static Map<Integer,Integer> loadDescriptions() {
        Map<Integer,Integer> result=new HashMap<>();
        try(var stream=ItemCatalog.class.getResourceAsStream("/catalog/item-descriptions.csv")) {
            if(stream==null)throw new IOException("Missing item descriptions");
            try(var reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
                for(String line;(line=reader.readLine())!=null;) {
                    if(line.startsWith("#")||line.isBlank())continue;
                    String[] fields=line.split(",");
                    if(fields.length!=2)throw new IOException("Invalid description mapping");
                    int id=Integer.parseInt(fields[0]),description=Integer.parseInt(fields[1]);
                    if(!ITEMS.containsKey(id)||description < -1||result.put(id,description)!=null)throw new IOException("Invalid item description");
                }
            }
        }catch(IOException|RuntimeException error){throw new ExceptionInInitializerError(error);}
        return Map.copyOf(result);
    }
    private static Map<Integer, Item> load() {
        var result = new TreeMap<Integer, Item>();
        try (var stream = ItemCatalog.class.getResourceAsStream("/catalog/items.csv")) {
            if (stream == null) throw new IOException("Missing item catalog");
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null;) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String[] fields = line.split(",");
                    if (fields.length != 7) throw new IOException("Invalid item catalog columns");
                    var item = new Item(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Integer.parseInt(fields[2]),
                            Integer.parseInt(fields[3]), Integer.parseInt(fields[4]), Integer.parseInt(fields[5]), Integer.parseInt(fields[6]));
                    if (item.id() < 0 || item.id() > 32767 || item.pocket() < 0 || item.pocket() > 7
                            || item.source() < -1 || item.source() > 4 || item.region() < 0 || item.region() > 10 || item.nameId() < 0
                            || item.maxStack() < 1 || item.maxStack() > 9999 || result.put(item.id(), item) != null)
                        throw new IOException("Invalid item catalog entry");
                }
            }
        } catch (IOException | RuntimeException exception) { throw new ExceptionInInitializerError(exception); }
        return Collections.unmodifiableMap(result);
    }
}
