package top.totodile.pokemmo.protocol;

import top.totodile.pokemmo.protocol.CharacterPackets.Position;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** ROM 活动范围约束 NPC；预留起点和终点防止交换穿身，对话锁定目标直至结束。 */
public final class KantoNpcs {
    public record Definition(
            int id, int x, int y, int elevation, int movement, int rangeX, int rangeY) {
        public int radiusX() {
            return ranged(movement) ? Math.max(1, rangeX) : rangeX;
        }

        public int radiusY() {
            return ranged(movement) ? Math.max(1, rangeY) : rangeY;
        }
    }

    private static final Map<Integer, List<Definition>> DEFINITIONS = load("kanto-npcs.csv");
    private static final Map<Integer, List<Definition>> HOENN_DEFINITIONS = load("emerald-npcs.csv");
    private final Map<Integer, List<Definition>> catalogue;
    private final FireRedWorldRules rules;
    private final Map<Integer, List<Actor>> maps = new LinkedHashMap<>();
    private int talking = -1, talkingMap = -1;
    private final Map<Integer, Position> lastPlayers = new HashMap<>();
    private static final int[][] SEQUENCES = {
        {1, 3, 2, 0}, {3, 2, 0, 1}, {0, 1, 3, 2}, {2, 0, 1, 3}, {1, 2, 3, 0}, {2, 3, 0, 1},
        {0, 1, 2, 3}, {3, 0, 1, 2}, {2, 1, 0, 3}, {1, 0, 3, 2}, {3, 2, 1, 0}, {0, 3, 2, 1},
        {3, 1, 0, 2}, {1, 0, 2, 3}, {2, 3, 1, 0}, {0, 2, 3, 1}, {1, 2, 0, 3}, {0, 3, 1, 2},
        {2, 0, 3, 1}, {3, 1, 2, 0}, {1, 3, 0, 2}, {0, 2, 1, 3}, {2, 1, 3, 0}, {3, 0, 2, 1}
    };

    private static final class Actor {
        final Definition definition;
        int x, y, fromX, fromY, facing, sequence;
        long ends, next;
        final Random random;

        Actor(Definition d, int key) {
            definition = d;
            x = fromX = d.x();
            y = fromY = d.y();
            facing = initialFacing(d.movement());
            random = new Random(key * 65537L + d.id());
            next = 0;
        }

        boolean occupies(int px, int py, long now) {
            return x == px && y == py || now < ends && fromX == px && fromY == py;
        }
    }

    public KantoNpcs(FireRedWorldRules rules) {
        this.rules = rules;
        catalogue = rules.region() == 0 ? DEFINITIONS : HOENN_DEFINITIONS;
        if (rules.region() != 0 && rules.region() != 1) throw new IllegalArgumentException("Unsupported NPC region");
    }

    public static List<Definition> definitions(int group, int map) {
        return DEFINITIONS.getOrDefault(group * 256 + map, List.of());
    }

    public static List<Definition> definitions(int region, int group, int map) {
        if (region != 0 && region != 1) throw new IllegalArgumentException("Unsupported NPC region");
        return (region == 0 ? DEFINITIONS : HOENN_DEFINITIONS).getOrDefault(group * 256 + map, List.of());
    }

    private List<Actor> actors(int key) {
        return maps.computeIfAbsent(
                key,
                k ->
                        catalogue.getOrDefault(k, List.of()).stream()
                                .filter(d -> d.x() <= 255 && d.y() <= 255)
                                .map(d -> new Actor(d, k))
                                .toList());
    }

    private static boolean ranged(int movement) {
        return movement >= 2 && movement <= 6
                || movement >= 0x19 && movement <= 0x38
                || movement >= 0x3b && movement <= 0x3e
                || movement == 0x50;
    }

    public static int initialFacing(int movement) {
        if (movement >= 0x1d && movement <= 0x34) return SEQUENCES[movement - 0x1d][0];
        if (movement >= 0x40 && movement <= 0x4b) return (movement - 0x40) % 4;
        return switch (movement) {
            case 3, 7, 0xf, 0x10, 0x15, 0x19, 0x35, 0x3b -> 1;
            case 5, 9, 0xe, 0x1b, 0x37, 0x3d -> 2;
            case 6, 10, 0x1c, 0x38, 0x3e -> 3;
            default -> 0;
        };
    }

    private static int[] directions(int type) {
        return switch (type) {
            case 3, 4, 0xd, 0x19, 0x1a -> new int[] {0, 1};
            case 5, 6, 0xe, 0x1b, 0x1c -> new int[] {2, 3};
            case 0xf -> new int[] {1, 2};
            case 0x10 -> new int[] {1, 3};
            case 0x11 -> new int[] {0, 2};
            case 0x12 -> new int[] {0, 3};
            case 0x13 -> new int[] {0, 1, 2};
            case 0x14 -> new int[] {0, 1, 3};
            case 0x15 -> new int[] {1, 2, 3};
            case 0x16 -> new int[] {0, 2, 3};
            default -> new int[] {0, 1, 2, 3};
        };
    }

    public boolean occupied(int group, int map, int x, int y, long now) {
        for (Actor actor : actors(group * 256 + map)) if (actor.occupies(x, y, now)) return true;
        return false;
    }
    public boolean occupiedExcept(int group,int map,int x,int y,long now,Set<Integer> controlled) {
        for(Actor actor:actors(group*256+map))if(!controlled.contains(actor.definition.id())&&actor.occupies(x,y,now))return true;
        return false;
    }

    public void update(Position player, long now) {
        update(player, null, now);
    }

    public void update(Position player, Position reserved, long now) { update(player,reserved,now,Set.of()); }

    public void update(Position player, Position reserved, long now,Set<Integer> controlled) {
        int key = player.group() * 256 + player.map();
        Position lastPlayer = lastPlayers.put(key, player);
        boolean playerMoved =
                lastPlayer != null
                        && (lastPlayer.x() != player.x() || lastPlayer.y() != player.y());
        for (Actor actor : actors(key)) {
            if (controlled.contains(actor.definition.id()) || now < actor.ends || actor.definition.id() == talking && key == talkingMap) continue;
            actor.fromX = actor.x;
            actor.fromY = actor.y;
            int type = actor.definition.movement();
            boolean copying = type >= 0x35 && type <= 0x38 || type >= 0x3b && type <= 0x3e;
            if (copying ? !playerMoved : now < actor.next) continue;
            actor.next = now + (800 + actor.random.nextInt(1400)) * 1_000_000L;
            if (type >= 0x40 && type <= 0x4b) {
                actor.ends = now + 275_000_000L;
                continue;
            }
            if (type == 0 || type >= 7 && type <= 12 || type >= 0x35 && type != 0x50 && !copying)
                continue;
            int[] choices = directions(type);
            if (type == 0x17 || type == 0x18) {
                int[] rotation = type == 0x18 ? new int[] {0, 2, 1, 3} : new int[] {0, 3, 1, 2};
                actor.facing = rotation[(++actor.sequence) % 4];
                continue;
            }
            boolean walking =
                    type >= 2 && type <= 6
                            || type >= 0x19 && type <= 0x34
                            || type == 0x50
                            || copying;
            if (copying) {
                int rotation = (type >= 0x3b ? type - 0x3b : type - 0x35);
                actor.facing =
                        switch (rotation) {
                            case 1 -> player.facing() ^ 1;
                            case 2 -> new int[] {3, 2, 0, 1}[player.facing()];
                            case 3 -> new int[] {2, 3, 1, 0}[player.facing()];
                            default -> player.facing();
                        };
            } else if (type >= 0x1d && type <= 0x34) {
                int[] route = SEQUENCES[type - 0x1d];
                int index = actor.sequence % 4;
                boolean atAxisOrigin =
                        route[index] < 2
                                ? actor.y == actor.definition.y()
                                : actor.x == actor.definition.x();
                if (index == 3
                        && actor.x == actor.definition.x()
                        && actor.y == actor.definition.y()) actor.sequence = 0;
                else if ((index == 2 || index == 1 && route[1] == (route[0] ^ 1)) && atAxisOrigin)
                    actor.sequence++;
                actor.facing = route[actor.sequence % 4];
            } else if (type >= 0x19 && type <= 0x1c) {
                if (actor.sequence != 0
                        && actor.x == actor.definition.x()
                        && actor.y == actor.definition.y()) actor.sequence = 0;
                actor.facing = initialFacing(type) ^ actor.sequence;
            } else actor.facing = choices[actor.random.nextInt(choices.length)];
            if (!walking) continue;
            int x = actor.x + (actor.facing == 2 ? -1 : actor.facing == 3 ? 1 : 0);
            int y = actor.y + (actor.facing == 1 ? -1 : actor.facing == 0 ? 1 : 0);
            Definition d = actor.definition;
            if ((type >= 0x19 && type <= 0x34)
                    && (Math.abs(x - d.x()) > d.radiusX() || Math.abs(y - d.y()) > d.radiusY())) {
                if (type <= 0x1c) {
                    actor.sequence ^= 1;
                    actor.facing ^= 1;
                } else {
                    actor.sequence = (actor.sequence + 1) % 4;
                    actor.facing = SEQUENCES[type - 0x1d][actor.sequence];
                }
                x = actor.x + (actor.facing == 2 ? -1 : actor.facing == 3 ? 1 : 0);
                y = actor.y + (actor.facing == 1 ? -1 : actor.facing == 0 ? 1 : 0);
            }
            boolean can =
                    Math.abs(x - d.x()) <= d.radiusX()
                            && Math.abs(y - d.y()) <= d.radiusY()
                            && rules.npcCanMove(
                                    player.group(), player.map(), actor.x, actor.y, x, y)
                            && !(player.x() == x && player.y() == y)
                            && !(reserved != null
                                    && reserved.group() == player.group()
                                    && reserved.map() == player.map()
                                    && reserved.x() == x
                                    && reserved.y() == y)
                            && !occupiedExcept(player.group(), player.map(), x, y, now,controlled);
            if (can) {
                actor.x = x;
                actor.y = y;
                actor.ends = now + 275_000_000L;
            }
            if (type >= 0x19 && type <= 0x34) actor.next = now + 275_000_000L;
        }
        var cached = maps.keySet().iterator();
        while (maps.size() > 16 && cached.hasNext()) {
            int oldest = cached.next();
            if (oldest == key) continue;
            cached.remove();
            lastPlayers.remove(oldest);
        }
    }

    public int talk(Position player, int id, long now) {
        if (id == 255) {
            if (talking >= 0)
                for (Actor actor : actors(talkingMap))
                    if (actor.definition.id() == talking) {
                        if (actor.definition.movement() >= 7 && actor.definition.movement() <= 10)
                            actor.facing = initialFacing(actor.definition.movement());
                        actor.next = now + 800_000_000L;
                    }
            talking = -1;
            talkingMap = -1;
            return -1;
        }
        if (talking >= 0) return talking;
        int x = player.x() + (player.facing() == 2 ? -1 : player.facing() == 3 ? 1 : 0);
        int y = player.y() + (player.facing() == 1 ? -1 : player.facing() == 0 ? 1 : 0);
        for (Actor actor : actors(player.group() * 256 + player.map())) {
            if (actor.definition.id() != id || now < actor.ends || actor.x != x || actor.y != y)
                continue;
            if (Math.abs(actor.definition.elevation() - player.elevation()) > 1) return -1;
            talking = id;
            talkingMap = player.group() * 256 + player.map();
            actor.facing = player.facing() ^ 1;
            return id;
        }
        return -1;
    }

    public boolean talking() {
        return talking >= 0;
    }

    /** 剧情结束后从最终落点恢复日常活动，保留一段停顿供玩家继续交互。 */
    public void resume(int group, int map, NpcPackets.State state, long now) {
        for (Actor actor : actors(group * 256 + map)) {
            if (actor.definition.id() != state.id()) continue;
            actor.x = actor.fromX = state.x();
            actor.y = actor.fromY = state.y();
            actor.facing = state.facing();
            actor.ends = now;
            actor.next = now + 800_000_000L;
        }
    }

    public NpcPackets.Snapshot snapshot(Position player, long now) {
        int key = player.group() * 256 + player.map();
        return new NpcPackets.Snapshot(
                player.group(),
                player.map(),
                key == talkingMap ? talking : -1,
                actors(key).stream()
                        .map(
                                a ->
                                        new NpcPackets.State(
                                                a.definition.id(),
                                                a.x,
                                                a.y,
                                                a.fromX,
                                                a.fromY,
                                                a.facing,
                                                (int)
                                                        Math.max(
                                                                0,
                                                                (a.ends - now + 999999)
                                                                        / 1_000_000)))
                        .toList());
    }

    private static Map<Integer, List<Definition>> load(String resource) {
        Map<Integer, List<Definition>> result = new HashMap<>();
        try (var stream = KantoNpcs.class.getResourceAsStream("/maps/" + resource)) {
            if (stream == null) throw new IOException("Missing Kanto NPC catalogue");
            try (var reader =
                    new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    int[] v = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
                    if (v.length != 9) throw new IOException("Invalid NPC definition");
                    result.computeIfAbsent(v[0] * 256 + v[1], k -> new ArrayList<>())
                            .add(new Definition(v[2], v[3], v[4], v[5], v[6], v[7], v[8]));
                }
            }
        } catch (IOException | RuntimeException e) {
            throw new ExceptionInInitializerError(e);
        }
        result.replaceAll((k, v) -> List.copyOf(v));
        return Map.copyOf(result);
    }
}
