package top.totodile.pokemmo.protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** 只读的 ROM 指令与物件事件，地址保持卡带原偏移。 */
public final class GbaScriptProgram {
    public record Npc(int group, int map, int id, int graphics, int x, int y, int elevation, int facing, int flag, int script) { }
    public record Trigger(int group, int map, int kind, int x, int y, int variable, int value, int script) { }
    public static final GbaScriptProgram INSTANCE = load();
    final Map<Integer, byte[]> code = new HashMap<>(), movements = new HashMap<>();
    final Map<Integer, String> specials = new HashMap<>(), movementNames = new HashMap<>();
    final List<Npc> npcs = new ArrayList<>();
    final List<Trigger> triggers = new ArrayList<>();
    String hash;
    int initializer;
    private GbaScriptProgram() { }
    private static GbaScriptProgram load() {
        var result = new GbaScriptProgram();
        try (var stream = GbaScriptProgram.class.getResourceAsStream("/maps/gba-opening-program.txt.gz")) {
            if (stream == null) throw new IOException("Missing exported GBA program");
            try (var reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(stream), StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null;) {
                    String[] f = line.split(",");
                    switch (f[0]) {
                        case "sha256" -> result.hash = f[1];
                        case "init" -> result.initializer = Integer.parseInt(f[1]);
                        case "code", "move" -> (f[0].equals("code") ? result.code : result.movements).put(Integer.parseInt(f[1]), HexFormat.of().parseHex(f[2]));
                        case "special", "movement" -> (f[0].equals("special") ? result.specials : result.movementNames).put(Integer.parseInt(f[1]), f[2]);
                        case "npc" -> result.npcs.add(new Npc(n(f,1),n(f,2),n(f,3),n(f,4),n(f,5),n(f,6),n(f,7),n(f,8),n(f,9),n(f,10)));
                        case "coord" -> result.triggers.add(new Trigger(n(f,1),n(f,2),0,n(f,3),n(f,4),n(f,6),n(f,7),n(f,8)));
                        case "map" -> result.triggers.add(new Trigger(n(f,1),n(f,2),n(f,3),0,0,n(f,4),n(f,5),n(f,6)));
                        default -> throw new IOException("Invalid GBA program record");
                    }
                }
            }
            return result;
        } catch (IOException | RuntimeException error) { throw new ExceptionInInitializerError(error); }
    }
    private static int n(String[] f,int i) { return Integer.parseInt(f[i]); }
    public boolean supports(int group, int map) { return npcs.stream().anyMatch(n -> n.group == group && n.map == map); }
    public List<Npc> npcs(int group,int map) { return npcs.stream().filter(n -> n.group == group && n.map == map).toList(); }
    public String romHash() { return hash; }
}
