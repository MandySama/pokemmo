package top.totodile.pokemmo.protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** 只读的 ROM 指令与物件事件，地址保持卡带原偏移。 */
public final class GbaScriptProgram {
    public record Npc(int group, int map, int id, int graphics, int x, int y, int elevation, int facing, int flag, int script) { }
    public record Trigger(int group, int map, int kind, int x, int y, int variable, int value, int script, int elevation) {
        public Trigger(int group,int map,int kind,int x,int y,int variable,int value,int script) { this(group,map,kind,x,y,variable,value,script,0); }
    }
    public record HiddenItem(int group, int map, int x, int y, int elevation, int kind, int data) { }
    final List<HiddenItem> hiddenItems = new ArrayList<>();
    public static final GbaScriptProgram INSTANCE = load(0);
    private static final class Hoenn { static final GbaScriptProgram INSTANCE = load(1); }
    public static GbaScriptProgram forRegion(int region) { if(region==0)return INSTANCE;if(region==1)return Hoenn.INSTANCE;throw new IllegalArgumentException("Unsupported script region"); }
    final int region;
    final NavigableMap<Integer,Integer> dexRatings = new TreeMap<>();
    final Map<Integer,Integer> tmMoves = new HashMap<>();
    final Map<Integer,Integer> species = new HashMap<>();
    final Map<Integer,Integer> standards = new HashMap<>();
    final Map<Integer,CharacterPackets.Position> heals = new HashMap<>();
    final Map<Integer, byte[]> code = new HashMap<>(), movements = new HashMap<>();
    final Map<Integer, String> specials = new HashMap<>(), movementNames = new HashMap<>();
    final List<Npc> npcs = new ArrayList<>();
    final List<Trigger> triggers = new ArrayList<>();
    String hash;
    int initializer;
    int hiddenRoot;
    int finderRoot;
    private GbaScriptProgram(int region) { this.region=region; }
    private static GbaScriptProgram load(int region) {
        var result = new GbaScriptProgram(region);
        try (var stream = GbaScriptProgram.class.getResourceAsStream(region==0?"/maps/gba-opening-program.txt.gz":"/maps/gba-hoenn-program.txt.gz")) {
            if (stream == null) throw new IOException("Missing exported GBA program");
            try (var reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(stream), StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null;) {
                    String[] f = line.split(",");
                    switch (f[0]) {
                        case "sha256" -> result.hash = f[1];
                        case "init" -> result.initializer = Integer.parseInt(f[1]);
                        case "hiddenroot" -> result.hiddenRoot = Integer.parseInt(f[1]);
                        case "finderroot" -> result.finderRoot = Integer.parseInt(f[1]);
                        case "hidden" -> result.hiddenItems.add(new HiddenItem(n(f,1),n(f,2),n(f,3),n(f,4),n(f,5),n(f,6),n(f,7)));
                        case "dexrating" -> result.dexRatings.put(n(f,1),n(f,2));
                        case "tmmove" -> result.tmMoves.put(n(f,1),n(f,2));
                        case "species" -> result.species.put(n(f,1),n(f,2));
                        case "std" -> result.standards.put(n(f,1),n(f,2));
                        case "heal" -> result.heals.put(n(f,1),new CharacterPackets.Position(n(f,2),n(f,3),n(f,4),n(f,5),3,0));
                        case "code", "move" -> (f[0].equals("code") ? result.code : result.movements).put(Integer.parseInt(f[1]), HexFormat.of().parseHex(f[2]));
                        case "special", "movement" -> (f[0].equals("special") ? result.specials : result.movementNames).put(Integer.parseInt(f[1]), f[2]);
                        case "npc" -> result.npcs.add(new Npc(n(f,1),n(f,2),n(f,3),n(f,4),n(f,5),n(f,6),n(f,7),n(f,8),n(f,9),n(f,10)));
                        case "coord" -> result.triggers.add(new Trigger(n(f,1),n(f,2),0,n(f,3),n(f,4),n(f,6),n(f,7),n(f,8)));
                        case "map" -> result.triggers.add(new Trigger(n(f,1),n(f,2),n(f,3),0,0,n(f,4),n(f,5),n(f,6)));
                        case "bg" -> result.triggers.add(new Trigger(n(f,1),n(f,2),100+n(f,6),n(f,3),n(f,4),0,0,n(f,7),n(f,5)));
                        default -> throw new IOException("Invalid GBA program record");
                    }
                }
            }
            return result;
        } catch (IOException | RuntimeException error) { throw new ExceptionInInitializerError(error); }
    }
    private static int n(String[] f,int i) { return Integer.parseInt(f[i]); }
    public boolean supports(int group, int map) { return npcs.stream().anyMatch(n -> n.group == group && n.map == map)
            || triggers.stream().anyMatch(t -> t.group == group && t.map == map) || hiddenItems.stream().anyMatch(t -> t.group == group && t.map == map); }
    public boolean backgroundScript(int script) { return triggers.stream().anyMatch(t -> t.kind >= 100 && t.script == script); }
    public List<Npc> npcs(int group,int map) { return npcs.stream().filter(n -> n.group == group && n.map == map).toList(); }
    public String romHash() { return hash; }
    public int nationalSpecies(int raw) { return region==0?raw:species.getOrDefault(raw,raw); }
    public int textId(int offset) { return offset<=0?0:offset | region<<28; }
}
