package top.totodile.pokemmo.protocol;

import top.totodile.pokemmo.protocol.CharacterPackets.Position;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** NDS 普通对象的会话内运动，起点和终点共同参与碰撞。 */
public final class NdsNpcs {
    public record Definition(
            int header,
            int id,
            int graphics,
            int x,
            int y,
            int elevation,
            int facing,
            int movement,
            int rangeX,
            int rangeY,
            int text) {}

    private static final Map<Integer, Map<Integer, List<Definition>>> CATALOG = load();
    private final int region;
    private final BlackWhiteWorldRules rules;
    private final Map<Integer, List<Actor>> actors = new HashMap<>();
    private int talking = -1, talkingMap = -1;
    private static final int[] DX = {0, 0, -1, 1}, DY = {1, -1, 0, 0};

    private static final class Actor {
        final Definition definition;
        final Random random;
        int x, y, fromX, fromY, facing, sequence;
        long ends, next;

        Actor(Definition d) {
            definition = d;
            x = fromX = d.x();
            y = fromY = d.y();
            facing = d.facing();
            random = new Random(d.header() * 65537L + d.id());
        }

        boolean occupies(int px, int py, long now) {
            return x == px && y == py || now < ends && fromX == px && fromY == py;
        }
    }

    public NdsNpcs(int region, BlackWhiteWorldRules rules) {
        this.region = region;
        this.rules = rules;
    }

    public static List<Definition> definitions(int region, int header) {
        return CATALOG.getOrDefault(region, Map.of()).getOrDefault(header, List.of());
    }

    private List<Actor> actors(int header) {
        return actors.computeIfAbsent(
                header, k -> definitions(region, k).stream().map(Actor::new).toList());
    }

    public boolean talking() {
        return talking >= 0;
    }

    public boolean occupied(Position p, long now) {
        for (var a : actors(p.group() * 256 + p.map()))
            if (a.definition.elevation() == p.elevation() && a.occupies(p.x(), p.y(), now))
                return true;
        return false;
    }

    public void update(Position player, Position from, long now) {
        int header = player.group() * 256 + player.map();
        if (talkingMap >= 0 && talkingMap != header) {
            talking = talkingMap = -1;
        }
        for (var a : actors(header)) {
            if (now < a.ends || now < a.next || a.definition.id() == talking) continue;
            a.fromX = a.x;
            a.fromY = a.y;
            a.next = now + (900 + a.random.nextInt(1400)) * 1_000_000L;
            int kind = a.definition.movement();
            int direction = a.facing;
            if (kind == 2) direction = a.random.nextInt(4);
            else if (kind == 3) direction = a.random.nextInt(4);
            else if (kind == 4 || kind == 45) direction = a.random.nextBoolean() ? 0 : 1;
            else if (kind == 5 || kind == 46) direction = a.random.nextBoolean() ? 2 : 3;
            else if (kind >= 6 && kind <= 13) {
                int[][] choices = {
                    {1, 2}, {1, 3}, {0, 2}, {0, 3}, {1, 0, 2}, {1, 0, 3}, {1, 2, 3}, {0, 2, 3}
                };
                int[] c = choices[kind - 6];
                direction = c[a.random.nextInt(c.length)];
            } else if (kind == 18 || kind == 19) {
                int[] clockwise = {2, 3, 1, 0}, counter = {3, 2, 0, 1};
                direction = (kind == 19 ? clockwise : counter)[a.facing];
            } else if (kind == 20)
                direction = a.sequence % 2 == 0 ? a.definition.facing() : a.definition.facing() ^ 1;
            else if (kind >= 21 && kind <= 44) {
                direction = SEQUENCES[kind - 21][a.sequence % 4];
            } else continue;
            a.facing = direction;
            if (kind != 3 && kind != 4 && kind != 5 && kind != 20 && (kind < 21 || kind > 44))
                continue;
            int x = a.x + DX[direction], y = a.y + DY[direction];
            if (Math.abs(x - a.definition.x()) > a.definition.rangeX()
                    || Math.abs(y - a.definition.y()) > a.definition.rangeY()) {
                blocked(a, kind);
                continue;
            }
            if (x == player.x() && y == player.y()
                    || from != null && x == from.x() && y == from.y()) {
                blocked(a, kind);
                continue;
            }
            Position origin =
                    new Position(
                            header >>> 8,
                            header & 255,
                            a.x,
                            a.y,
                            a.definition.elevation(),
                            direction);
            var move = rules.move(origin, direction, false);
            if (!move.moved()
                    || move.warp()
                    || move.position().group() != origin.group()
                    || move.position().map() != origin.map()
                    || move.position().elevation() != origin.elevation()
                    || occupied(move.position(), now)) {
                blocked(a, kind);
                continue;
            }
            a.x = x;
            a.y = y;
            a.ends = now + 275_000_000L;
            a.next = a.ends + 600_000_000L;
        }
    }

    private static void blocked(Actor actor, int kind) {
        if (kind == 20) actor.sequence ^= 1;
        else if (kind >= 21 && kind <= 44) actor.sequence = (actor.sequence + 1) % 4;
    }

    public void end(long now) {
        for (var a : actors.getOrDefault(talkingMap, List.of()))
            if (a.definition.id() == talking) {
                a.next = now + 800_000_000L;
                if (a.definition.movement() == 0
                        || a.definition.movement() >= 14 && a.definition.movement() <= 17)
                    a.facing = a.definition.facing();
            }
        talking = talkingMap = -1;
    }

    public void talk(Position p, int id, long now) {
        if (talking >= 0) return;
        for (var a : actors(p.group() * 256 + p.map()))
            if (a.definition.id() == id
                    && now >= a.ends
                    && a.x == p.x() + DX[p.facing()]
                    && a.y == p.y() + DY[p.facing()]
                    && a.definition.elevation() == p.elevation()) {
                talking = id;
                talkingMap = p.group() * 256 + p.map();
                a.facing = p.facing() ^ 1;
                break;
            }
    }

    public NpcPackets.Snapshot snapshot(Position p, long now) {
        int header = p.group() * 256 + p.map(), text = 0;
        List<NpcPackets.State> states = new ArrayList<>();
        for (var a : actors(header)) {
            int remaining = (int) Math.max(0, (a.ends - now + 999999) / 1_000_000);
            states.add(
                    new NpcPackets.State(
                            a.definition.id(),
                            a.x,
                            a.y,
                            remaining > 0 ? a.fromX : a.x,
                            remaining > 0 ? a.fromY : a.y,
                            a.facing,
                            remaining));
            if (a.definition.id() == talking) text = a.definition.text();
        }
        return new NpcPackets.Snapshot(
                p.group(), p.map(), talkingMap == header ? talking : -1, states, text);
    }

    private static final int[][] SEQUENCES = {
        {1, 3, 2, 0}, {3, 2, 0, 1}, {0, 1, 3, 2}, {2, 0, 1, 3}, {2, 3, 0, 1}, {1, 2, 3, 0},
        {0, 1, 2, 3}, {3, 0, 1, 2}, {2, 1, 0, 3}, {1, 0, 3, 2}, {3, 2, 1, 0}, {0, 3, 2, 1},
        {3, 1, 0, 2}, {1, 0, 2, 3}, {2, 3, 1, 0}, {0, 2, 3, 1}, {1, 2, 0, 3}, {0, 3, 1, 2},
        {2, 0, 3, 1}, {3, 1, 2, 0}, {1, 3, 0, 2}, {0, 2, 1, 3}, {2, 1, 3, 0}, {3, 0, 2, 1}
    };

    private static Map<Integer, Map<Integer, List<Definition>>> load() {
        Map<Integer, Map<Integer, List<Definition>>> result = new HashMap<>();
        for (int region : new int[] {2, 3, 5}) {
            Map<Integer, List<Definition>> maps = new HashMap<>();
            try (var input =
                    NdsNpcs.class.getResourceAsStream("/maps/nds-npcs-" + region + ".csv.gz")) {
                if (input == null) throw new IOException("Missing NDS NPC catalog " + region);
                try (var reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        new GZIPInputStream(input), StandardCharsets.UTF_8))) {
                    for (String line; (line = reader.readLine()) != null; ) {
                        int[] f =
                                Arrays.stream(line.split(","))
                                        .mapToInt(Integer::parseInt)
                                        .toArray();
                        var d =
                                new Definition(
                                        f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7], f[8], f[9],
                                        f[10]);
                        if (d.id() < 0 || d.id() >= 255 || d.facing() < 0 || d.facing() > 3)
                            throw new IOException("Invalid NDS NPC");
                        maps.computeIfAbsent(d.header(), k -> new ArrayList<>()).add(d);
                    }
                }
            } catch (IOException e) {
                throw new ExceptionInInitializerError(e);
            }
            maps.replaceAll((k, v) -> List.copyOf(v));
            result.put(region, Map.copyOf(maps));
        }
        return Map.copyOf(result);
    }
}
