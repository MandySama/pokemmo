package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.*;

/** 正式角色的场景完成标记；重置与播放使用同一场景标识。 */
public final class StoryProgressPackets {
    public static final int OPCODE = 0x88, QUERY = 0, RESET = 1, BEGIN = 2, COMPLETE = 3, CANCEL = 4;
    public record Scene(String id, int group, int map, int x, int y) { public int region(){return id.startsWith("hoenn.")?1:0;} }
    public record Request(int action, String scene, long token) { }
    public record Result(boolean accepted,long token,Set<String> completed,CharacterPackets.Position position,boolean nativeScript,Set<String> available) {
        public Result(boolean accepted,long token,Set<String> completed,CharacterPackets.Position position){this(accepted,token,completed,position,false,Set.of());}
    }
    private static final Map<String, Scene> SCENES = load();
    public static final String OPENING = "kanto.pallet_town.opening/PalletTown/town_stop";
    public static final String ESCORT = "kanto.pallet_town.opening/PalletTown/escort";
    public static final String LAB_INTRODUCTION = "kanto.pallet_town.opening/PalletTown_ProfessorOaksLab/lab_introduction";
    public static final String HOENN_OPENING="hoenn.opening/truck", HOENN_RESCUE="hoenn.opening/rescue", HOENN_DEX="hoenn.opening/pokedex";
    private StoryProgressPackets() { }
    private static Map<String, Scene> load() {
        var scenes = new LinkedHashMap<String, Scene>();
        try (var stream = StoryProgressPackets.class.getResourceAsStream("/maps/kanto-story-scenes.csv")) {
            if (stream == null) throw new IOException("Missing story scenes");
            for (String line : new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).split("\\R")) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] f = line.split(",");
                var scene = new Scene(f[0], Integer.parseInt(f[1]), Integer.parseInt(f[2]), Integer.parseInt(f[3]), Integer.parseInt(f[4]));
                if (!FireRedMaps.contains(0, scene.group(), scene.map(), scene.x(), scene.y())
                        || scenes.put(scene.id(), scene) != null) throw new IOException("Invalid story scene");
            }
            try(var hoenn=StoryProgressPackets.class.getResourceAsStream("/maps/hoenn-story-scenes.csv")){
                if(hoenn==null)throw new IOException("Missing Hoenn story scenes");
                for(String line:new String(hoenn.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).split("\\R")){
                    if(line.isBlank()||line.startsWith("#"))continue;var f=line.split(",");var scene=new Scene(f[0],Integer.parseInt(f[1]),Integer.parseInt(f[2]),Integer.parseInt(f[3]),Integer.parseInt(f[4]));
                    if(!FireRedMaps.contains(1,scene.group(),scene.map(),scene.x(),scene.y())||scenes.put(scene.id(),scene)!=null)throw new IOException("Invalid Hoenn scene");
                }
            }
            return Collections.unmodifiableMap(scenes);
        } catch (IOException | RuntimeException error) { throw new ExceptionInInitializerError(error); }
    }
    public static Scene scene(String id) { return SCENES.get(id); }
    public static Set<String> scenes() { return SCENES.keySet(); }
    /** 完整开场包含带路片段，单独播放带路不会反向完成整段开场。 */
    public static Set<String> completionScenes(String scene) {
        if (scene.equals(OPENING)) return Set.of(OPENING, ESCORT, LAB_INTRODUCTION);
        return scene.equals(ESCORT) ? Set.of(ESCORT, LAB_INTRODUCTION) : Set.of(scene);
    }
    /** 重置片段也使包含它的完整场景失效，避免完整场景覆盖未完成的片段。 */
    public static Set<String> resetScenes(String scene) {
        return scene.equals(OPENING) || scene.equals(ESCORT) || scene.equals(LAB_INTRODUCTION)
                ? Set.of(OPENING, ESCORT, LAB_INTRODUCTION) : Set.of(scene);
    }
    public static byte[] request(int action, String scene, long token) {
        if (action < QUERY || action > CANCEL || scene.length() > 200) throw new IllegalArgumentException("Invalid story action");
        return new PacketData(420).putByte(OPCODE).putByte(action).putString(scene).putLong(token).written();
    }
    public static Request readRequest(byte[] packet) throws IOException {
        var data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Invalid story opcode");
        var request = new Request(data.byteValue(), data.string(200), data.longValue()); data.end();
        if (request.action() > CANCEL || request.action() != QUERY && scene(request.scene()) == null)
            throw new IOException("Unknown story scene/action");
        return request;
    }
    public static byte[] response(Result result) {
        var data = new PacketData(40000).putByte(OPCODE).putByte(result.accepted() ? 1 : 0).putLong(result.token()).putShort(result.completed().size());
        for (String scene : new TreeSet<>(result.completed())) data.putString(scene);
        var p = result.position(); data.putByte(p == null ? 0 : 1);
        if (p != null) data.putByte(p.group()).putByte(p.map()).putShort(p.x()).putShort(p.y()).putByte(p.elevation()).putByte(p.facing());
        data.putByte(result.nativeScript()?1:0).putShort(result.available().size());for(String id:result.available())data.putString(id);
        return data.written();
    }
    public static Result decode(byte[] packet) throws IOException {
        var data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Invalid story response");
        boolean accepted = data.booleanValue(); long token = data.longValue(); int count = data.shortValue();
        if (count > SCENES.size()) throw new IOException("Invalid story count");
        Set<String> completed = new HashSet<>();
        for (int i = 0; i < count; i++) {
            String scene = data.string(200);
            if (scene(scene) == null || !completed.add(scene)) throw new IOException("Invalid completed scene");
        }
        CharacterPackets.Position p = data.booleanValue() ? new CharacterPackets.Position(data.byteValue(), data.byteValue(), data.shortValue(), data.shortValue(), data.byteValue(), data.byteValue()) : null;
        boolean nativeScript=data.booleanValue();int allowed=data.shortValue();if(allowed>SCENES.size())throw new IOException("Invalid scene availability");var available=new HashSet<String>();for(int i=0;i<allowed;i++){String id=data.string(200);if(scene(id)==null)throw new IOException("Invalid available scene");available.add(id);}data.end();
        if (p != null && !p.valid(0) && !p.valid(1)) throw new IOException("Invalid story destination");
        return new Result(accepted,token,Set.copyOf(completed),p,nativeScript,Set.copyOf(available));
    }
}
