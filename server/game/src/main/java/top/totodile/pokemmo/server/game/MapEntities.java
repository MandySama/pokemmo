package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** MapFile.toNpc / SendAddGameEntityPacket, carried through the local service. */
public final class MapEntities {
    private MapEntities() {}

    public record Npc(int id, int graphics, int x, int y, int elevation, int direction,
                      int movement, int rangeX, int rangeY) {}
    /** Native background index is independent of the map's NPC indices. */
    public record Background(int id, int x, int y, int elevation, int direction) {
        public boolean facing(GameService.Position player, int playerElevation) {
            int facing = player.direction();
            if (facing < 0 || facing > 3 || direction < -1) return false;
            int dx = facing == 2 ? -1 : facing == 3 ? 1 : 0;
            int dy = facing == 1 ? -1 : facing == 0 ? 1 : 0;
            return player.x() + dx == x && player.y() + dy == y
                    && (direction == -1 || direction == facing)
                    && (elevation == 0 || elevation == playerElevation);
        }
    }
    public record Tile(int x,int y,int block) {}
    public record MapView(int bank, int map, List<Npc> npcs, List<Background> backgrounds, List<Tile> tiles) {
        public MapView { npcs = List.copyOf(npcs); backgrounds = List.copyOf(backgrounds); tiles=List.copyOf(tiles); }
        public MapView(int bank,int map,List<Npc> npcs,List<Background> backgrounds){this(bank,map,npcs,backgrounds,List.of());}
        public MapView(int bank, int map, List<Npc> npcs) { this(bank, map, npcs, List.of()); }
    }
    private record Source(int bank, int map, String key, List<Npc> npcs,
                          List<Background> backgrounds, int width, int height, byte[] blocks) {}
    private static final Map<String, String> SCRIPTS = new HashMap<>();
    private static final Map<String, String> BACKGROUND_SCRIPTS = new HashMap<>();
    private static final Set<String> WARP_TILES=new HashSet<>();
    static boolean warpTile(int bank,int map,int x,int y){return WARP_TILES.contains(bank+":"+map+":"+x+":"+y);}
    private static final Map<String,String> COORDINATES=new HashMap<>();
    private static final Map<String,String> HIDE_FLAGS=new HashMap<>();
    private static final List<Source> MAPS = load();
    static boolean coordinate(GameService.Position p,String script){return Arrays.asList(COORDINATES.getOrDefault(p.bank()+":"+p.map()+":"+p.x()+":"+p.y(),"").split("\\|" )).contains(script);}
    static GameService.Position position(String key,int x,int y,int direction) {
        var m=MAPS.stream().filter(v->v.key.equals(key)).findFirst().orElseThrow(()->new IllegalStateException("Missing map "+key));
        return new GameService.Position(m.bank,m.map,x,y,direction);
    }

    static int block(int bank,int map,int x,int y) {
        var source=MAPS.stream().filter(m->m.bank==bank&&m.map==map).findFirst().orElseThrow();
        if(x<0||y<0||x>=source.width||y>=source.height)throw new IllegalArgumentException("Map tile out of bounds");
        int at=(y*source.width+x)*2;return Byte.toUnsignedInt(source.blocks[at])|Byte.toUnsignedInt(source.blocks[at+1])<<8;
    }

    static List<Npc> definitions(int bank,int map) {
        return MAPS.stream().filter(m->m.bank==bank&&m.map==map).findFirst().map(Source::npcs).orElse(List.of());
    }
    static String mapKey(int bank,int map) {
        return MAPS.stream().filter(m->m.bank==bank&&m.map==map).findFirst().map(Source::key).orElse("");
    }

    static String script(int bank, int map, int npc) {
        return SCRIPTS.getOrDefault(bank + ":" + map + ":" + npc, "");
    }

    static String backgroundScript(int bank, int map, int background) {
        return BACKGROUND_SCRIPTS.getOrDefault(bank + ":" + map + ":" + background, "");
    }

    static Background background(GameService.Position position, int id) {
        for (var map : MAPS) if (map.bank == position.bank() && map.map == position.map()) {
            if (position.x() < 0 || position.y() < 0 || position.x() >= map.width
                    || position.y() >= map.height) return null;
            int at = (position.y() * map.width + position.x()) * 2;
            int elevation = (map.blocks[at + 1] & 255) >>> 4;
            return map.backgrounds.stream().filter(b -> b.id() == id && b.facing(position, elevation))
                    .findFirst().orElse(null);
        }
        return null;
    }

    static boolean nurse(int bank, int map, int npc) {
        return MAPS.stream().anyMatch(m -> m.bank == bank && m.map == map
                && m.key.endsWith("PokemonCenter_1F")
                && SCRIPTS.getOrDefault(bank + ":" + map + ":" + npc, "").endsWith("EventScript_Nurse"));
    }

    private static JsonElement read(String name) throws IOException {
        try (var stream = MapEntities.class.getResourceAsStream("/map/kanto/" + name)) {
            if (stream == null) throw new IOException("Missing reference map: " + name);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }

    private static List<Source> load() {
        try {
            var maps = new ArrayList<Source>();
            for (var entry : read("index.json").getAsJsonArray()) {
                String path = entry.getAsJsonObject().get("path").getAsString();
                var map = read(path).getAsJsonObject();
                if(map.has("coordScripts"))for(var value:map.getAsJsonArray("coordScripts")) {
                    var c=value.getAsJsonObject();String key=map.get("bankId").getAsInt()+":"+map.get("mapId").getAsInt()+":"+c.get("x").getAsInt()+":"+c.get("y").getAsInt();
                    COORDINATES.merge(key,c.get("script").getAsString(),(a,b)->a+"|"+b);
                }
                for(var value:map.getAsJsonArray("warps")){var w=value.getAsJsonObject();WARP_TILES.add(map.get("bankId").getAsInt()+":"+map.get("mapId").getAsInt()+":"+w.get("x").getAsInt()+":"+w.get("y").getAsInt());}
                var npcs = new ArrayList<Npc>();
                for (var value : map.getAsJsonArray("npcs")) {
                    var n = value.getAsJsonObject();
                    SCRIPTS.put(map.get("bankId").getAsInt() + ":" + map.get("mapId").getAsInt()
                            + ":" + n.get("entityIdx").getAsInt(), n.get("script").getAsString());
                    HIDE_FLAGS.put(map.get("bankId").getAsInt()+":"+map.get("mapId").getAsInt()+":"+n.get("entityIdx").getAsInt(),n.has("hideFlag")?n.get("hideFlag").getAsString():"");
                    // Off-map resource placeholders (Route21_North npc_5 is at y=-3)
                    // must not appear over the connected map's real resident.
                    int x = n.get("x").getAsInt(), y = n.get("y").getAsInt();
                    if (x < 0 || y < 0 || x >= map.get("width").getAsInt()
                            || y >= map.get("height").getAsInt()) continue;
                    String facing = n.get("facing").getAsString();
                    int direction = switch (facing.substring(facing.lastIndexOf('.') + 1)) {
                        case "UP", "NORTH" -> 1;
                        case "LEFT", "WEST" -> 2;
                        case "RIGHT", "EAST" -> 3;
                        default -> 0;
                    };
                    // All bundled GBA resources declare rawMovementType (verified on import).
                    npcs.add(new Npc(n.get("entityIdx").getAsInt(), n.get("graphicsId").getAsInt(),
                            n.get("x").getAsInt(), n.get("y").getAsInt(), n.get("elevation").getAsInt(),
                            direction, n.get("rawMovementType").getAsInt(),
                            n.get("movementRangeX").getAsInt(), n.get("movementRangeY").getAsInt()));
                }
                var backgrounds = new ArrayList<Background>();
                int bank = map.get("bankId").getAsInt(), mapId = map.get("mapId").getAsInt();
                for (var value : map.getAsJsonArray("bgEvents")) {
                    var bg = value.getAsJsonObject();
                    int id = backgrounds.size();
                    BACKGROUND_SCRIPTS.put(bank + ":" + mapId + ":" + id, bg.get("script").getAsString());
                    int direction = switch (bg.get("facingDir").getAsString()) {
                        case "ANY" -> -1;
                        case "NORTH" -> 1;
                        case "SOUTH" -> 0;
                        case "WEST" -> 2;
                        case "EAST" -> 3;
                        default -> -2;
                    };
                    backgrounds.add(new Background(id, bg.get("x").getAsInt(), bg.get("y").getAsInt(),
                            bg.get("elevation").getAsInt(), direction));
                }
                maps.add(new Source(bank, mapId,
                        path.substring(path.lastIndexOf('/') + 1, path.length() - 5), List.copyOf(npcs),
                        List.copyOf(backgrounds), map.get("width").getAsInt(), map.get("height").getAsInt(),
                        Base64.getDecoder().decode(map.get("blockData").getAsString())));
            }
            return List.copyOf(maps);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    /** PalletStoryNpcs.project: immutable per-character views, never mutate shared definitions. */
    static List<MapView> project(StoryData.State state) {
        return MAPS.stream().map(map -> new MapView(map.bank, map.map,
                map.npcs.stream().filter(n -> visible(map.key, n.id, state)).toList(), map.backgrounds)).toList();
    }

    static boolean inSight(GameService.Position p,Npc n,int range) {
        int dx=n.direction==2?-1:n.direction==3?1:0,dy=n.direction==1?-1:n.direction==0?1:0;
        int distance=Math.abs(p.x()-n.x)+Math.abs(p.y()-n.y);
        if(distance<1||distance>range||p.x()!=n.x+dx*distance||p.y()!=n.y+dy*distance)return false;
        var m=MAPS.stream().filter(v->v.bank==p.bank()&&v.map==p.map()).findFirst().orElseThrow();
        for(int step=1;step<distance;step++){
            int x=n.x+dx*step,y=n.y+dy*step;
            int at=(y*m.width+x)*2;
            if(at<0||at+1>=m.blocks.length)return false;
            int block=Byte.toUnsignedInt(m.blocks[at])|Byte.toUnsignedInt(m.blocks[at+1])<<8;
            if((block&0xc00)!=0)return false;
        }
        return true;
    }

    static List<MapView> projectStory(top.totodile.pokemmo.db.Database db,String owner,StoryData.State state) {
        if(!state.enabled())return project(state);
        var p=KantoMainline.progress(db,owner);
        var events=new HashSet<String>();for(var row:db.query("SELECT flag FROM character_story_event WHERE character_id=?",owner))events.add(row[0]);
        return KantoMechanisms.projectSaved(db,owner,project(state).stream().map(m->new MapView(m.bank,m.map,m.npcs.stream().filter(n->{
            String f=HIDE_FLAGS.getOrDefault(m.bank+":"+m.map+":"+n.id,"");
            if(events.contains(f))return false;
            return switch(f) {
                case "kanto/FLAG_HIDE_DOME_FOSSIL","kanto/FLAG_HIDE_HELIX_FOSSIL" -> !p.bit("story",6);
                case "kanto/FLAG_HIDE_TOWER_FUJI","kanto/FLAG_HIDE_TOWER_ROCKET_1","kanto/FLAG_HIDE_TOWER_ROCKET_2","kanto/FLAG_HIDE_TOWER_ROCKET_3" -> !p.bit("story",0);
                case "kanto/FLAG_HIDE_TOWER_RIVAL" -> !p.bit("extra",4);
                case "kanto/FLAG_HIDE_POKEHOUSE_FUJI" -> p.bit("story",0);
                case "kanto/FLAG_HIDE_HIDEOUT_GIOVANNI","kanto/FLAG_HIDE_SILPH_SCOPE","kanto/FLAG_HIDE_MISC_KANTO_ROCKETS","kanto/FLAG_HIDE_CELADON_ROCKETS" -> !p.bit("story",1);
                case "kanto/FLAG_HIDE_LIFT_KEY" -> events.contains("kanto/trainer/RocketHideout_B4F/RocketHideout_B4F_EventScript_Grunt1")&&!p.bit("extra",2);
                case "kanto/FLAG_HIDE_SILPH_ROCKETS","kanto/FLAG_HIDE_SAFFRON_ROCKETS" -> !p.bit("story",3);
                case "kanto/FLAG_HIDE_SAFARI_ZONE_WEST_GOLD_TEETH" -> !p.bit("story",15);
                case "kanto/FLAG_HIDE_POKEMON_MANSION_B1F_SECRET_KEY" -> !p.bit("story",4);
                case "kanto/FLAG_HIDE_CERULEAN_ROCKET" -> !p.bit("extra",5);
                case "kanto/FLAG_HIDE_NUGGET_BRIDGE_ROCKET" -> !p.bit("story",8);
                case "kanto/FLAG_HIDE_CERULEAN_RIVAL" -> !p.bit("story",7);
                case "kanto/FLAG_HIDE_SS_ANNE_RIVAL" -> !p.bit("story",12);
                case "kanto/FLAG_HIDE_SAFFRON_CIVILIANS" -> p.bit("story",3);
                case "kanto/FLAG_HIDE_VIRIDIAN_GIOVANNI" -> !p.bit("badges",7);
                case "kanto/FLAG_HIDE_ROUTE_22_RIVAL" -> (!p.bit("extra",6)||p.bit("badges",7))&&!p.bit("extra",0);
                case "kanto/FLAG_HIDE_SILPH_RIVAL" -> !p.bit("extra",1);
                default -> true;
            };
        }).toList(),m.backgrounds,KantoMiddle.tiles(db,owner,m.bank,m.map,state))).toList());
    }

    private static boolean visible(String key, int id, StoryData.State s) {
        // Reference map FLAG_HIDE_CREDITS_* actors are added only by the native credits scene.
        // They share the League doorway; ordinary field loading must never spawn them there.
        if (key.equals("IndigoPlateau_Exterior") && (id == 0 || id == 1)) return false;
        if (key.equals("PokemonLeague_ChampionsRoom") && id == 1) return false;
        // Old saves have no opening checkpoint; they must not expose the temporary
        // town Oak actor. Never manufacture a checkpoint or replay their opening.
        if (key.equals("PalletTown") && id == 2) return s.enabled() && s.stage() == 1;
        if (!s.enabled()) return true;
        if (!key.equals("PalletTown_ProfessorOaksLab")) return true;
        if (id == 3) return s.stage() >= 1;
        if (id == 7) return s.stage() < 4 || s.stage() == 5;
        if (id == 8 || id == 9) return s.stage() < 6;
        int choice = id == 4 ? 0 : id == 5 ? 2 : id == 6 ? 1 : -1;
        return choice < 0 || s.starter() == 3
                || choice != s.starter() && choice != (s.starter() + 1) % 3;
    }
}
