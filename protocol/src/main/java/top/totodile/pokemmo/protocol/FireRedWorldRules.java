package top.totodile.pokemmo.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static top.totodile.pokemmo.protocol.CharacterPackets.*;

/** 不含图像的地图通行、连接、静态物件和传送事件，只由服务端裁决使用。 */
public final class FireRedWorldRules implements WorldRules {
    private record Warp(int x, int y, int index, int group, int map) {
        boolean dynamic() {
            return group == 127 || group == 255 || map == 127 || map == 255 || index == 255;
        }
    }

    private record Connection(int direction, int offset, int group, int map) {
    }

    private record Area(int width, int height, boolean[] walkable, byte[] heights, byte[] behaviors,
                        Set<Integer> occupied, List<Connection> connections, List<Warp> warps) {
        boolean contains(int x, int y) {
            return x >= 0 && y >= 0 && x < width && y < height;
        }

        boolean walkable(int x, int y) {
            return contains(x, y) && walkable[y * width + x];
        }

        int height(int x, int y) {
            return heights[y * width + x];
        }

        int behavior(int x, int y) {
            return behaviors[y * width + x] & 255;
        }

        int standing(int x, int y) {
            return switch (behavior(x, y)) {
                case 0x62, 0x6C, 0x6E -> 3;
                case 0x63, 0x6D, 0x6F -> 2;
                case 0x64 -> 1;
                case 0x65 -> 0;
                default -> -1;
            };
        }
    }

    public record Decision(Position position, boolean moved, boolean warp, Position approach) {
        public Decision(Position position, boolean moved, boolean warp) {
            this(position, moved, warp, null);
        }
    }

    private final int region;
    private final Map<Integer, Area> areas = new HashMap<>();

    public FireRedWorldRules() throws IOException { this(0); }

    public FireRedWorldRules(int region) throws IOException {
        if (region < 0 || region != 0 && region != 1 && region != 4) throw new IllegalArgumentException("Invalid world region");
        this.region = region;
        var stream = FireRedWorldRules.class.getResourceAsStream(region == 0 ? "/maps/fire-red-world.gz" : region == 1 ? "/maps/emerald-world.gz" : "/maps/emerald2-world.gz");
        if (stream == null) throw new IOException("Missing world rules");
        try (var data = new DataInputStream(new GZIPInputStream(stream))) {
            if (data.readInt() != 0x46525731) throw new IOException("Invalid world rules version");
            int count = data.readInt();
            if (count <= 0 || count > 65536 || region != 4 && count != (region == 0 ? 425 : 518)) throw new IOException("Incomplete world rules");
            for (int i = 0; i < count; i++) {
                int key = data.readUnsignedByte() * 256 + data.readUnsignedByte();
                int width = data.readUnsignedShort(), height = data.readUnsignedShort();
                if (width < 1 || height < 1 || width > 256 || height > 256)
                    throw new IOException("Invalid world dimensions");
                boolean[] walkable = new boolean[width * height];
                byte[] heights = new byte[width * height], behaviors = new byte[width * height];
                for (int j = 0; j < walkable.length; j++) {
                    walkable[j] = data.readBoolean();
                    heights[j] = data.readByte();
                    behaviors[j] = data.readByte();
                }
                Set<Integer> occupied = new HashSet<>();
                int length = data.readUnsignedShort();
                for (int j = 0; j < length; j++) {
                    int x = data.readUnsignedShort(), y = data.readUnsignedShort();
                    occupied.add(y * 65536 + x);
                }
                List<Connection> connections = new ArrayList<>();
                length = data.readUnsignedShort();
                for (int j = 0; j < length; j++) {
                    connections.add(new Connection(data.readUnsignedByte(), data.readInt(),
                            data.readUnsignedByte(), data.readUnsignedByte()));
                }
                List<Warp> warps = new ArrayList<>();
                length = data.readUnsignedShort();
                for (int j = 0; j < length; j++) {
                    warps.add(new Warp(data.readUnsignedShort(), data.readUnsignedShort(), data.readUnsignedByte(),
                            data.readUnsignedByte(), data.readUnsignedByte()));
                }
                Area area = new Area(width, height, walkable, heights, behaviors, Set.copyOf(occupied),
                        List.copyOf(connections), List.copyOf(warps));
                if (areas.put(key, area) != null) throw new IOException("Duplicate world map");
            }
            if (data.read() != -1) throw new IOException("Trailing world rules");
        }
    }

    private Area area(int group, int map) {
        return areas.get(group * 256 + map);
    }

    public Position at(int group, int map, int x, int y, int facing, ReturnPoint returnPoint) {
        Area area = area(group, map);
        if (area == null || !area.contains(x, y)) throw new IllegalArgumentException("Invalid world destination");
        return new Position(group, map, x, y, area.height(x, y) + 1, facing, returnPoint);
    }

    private boolean available(Warp warp, ReturnPoint returnPoint) {
        if (warp.dynamic()) return returnPoint != null && returnPoint.valid(region);
        Area target = area(warp.group, warp.map);
        return target != null && warp.index < target.warps.size();
    }

    private Decision warp(Position position, int direction, boolean entered) {
        Area source = area(position.group(), position.map());
        int standing = source.standing(position.x(), position.y());
        if (entered ? standing >= 0 || source.behavior(position.x(), position.y()) == 0x69 && direction != 1
                : standing != direction) return null;
        for (Warp warp : source.warps) {
            if (warp.x != position.x() || warp.y != position.y() || !available(warp, position.returnPoint())) continue;
            int group, map, x, y, facing = direction;
            ReturnPoint returnPoint = position.returnPoint();
            if (warp.dynamic()) {
                group = returnPoint.group();
                map = returnPoint.map();
                x = returnPoint.x();
                y = returnPoint.y();
                facing = returnPoint.facing();
            } else {
                group = warp.group;
                map = warp.map;
                Warp arrival = area(group, map).warps.get(warp.index);
                x = arrival.x;
                y = arrival.y;
                if (area(group, map).warps.stream().anyMatch(Warp::dynamic)) {
                    returnPoint = new ReturnPoint(position.group(), position.map(), position.x(), position.y(), direction);
                }
            }
            Area target = area(group, map);
            if (target.behavior(x, y) == 0x69 && target.walkable(x, y + 1)) {
                y++;
                facing = 0;
            }
            return new Decision(at(group, map, x, y, facing, returnPoint), true, true);
        }
        return null;
    }

    public Decision move(Position position, int direction, boolean noclip) {
        if (direction < 0 || direction > 3 || !position.valid(region)) throw new IllegalArgumentException("Invalid movement");
        position = at(position.group(), position.map(), position.x(), position.y(), direction, position.returnPoint());
        Decision standing = warp(position, direction, false);
        if (standing != null) return standing;
        Area source = area(position.group(), position.map());
        int x = position.x() + (direction == 2 ? -1 : direction == 3 ? 1 : 0);
        int y = position.y() + (direction == 1 ? -1 : direction == 0 ? 1 : 0);
        int group = position.group(), map = position.map();
        boolean crossing = !source.contains(x, y);
        if (crossing) {
            int connectionDirection = direction + 1;
            boolean found = false;
            for (Connection connection : source.connections) {
                if (connection.direction != connectionDirection) continue;
                Area target = area(connection.group, connection.map);
                if (target == null) continue;
                int originX = switch (connectionDirection) {
                    case 3 -> -target.width;
                    case 4 -> source.width;
                    default -> connection.offset;
                };
                int originY = switch (connectionDirection) {
                    case 1 -> source.height;
                    case 2 -> -target.height;
                    default -> connection.offset;
                };
                if (!target.contains(x - originX, y - originY)) continue;
                x -= originX;
                y -= originY;
                group = connection.group;
                map = connection.map;
                found = true;
                break;
            }
            if (!found) return new Decision(position, false, false);
        }
        Area target = area(group, map);
        boolean door = false;
        if (direction == 1 && target.behavior(x, y) == 0x69) {
            for (Warp warp : target.warps) {
                if (warp.x == x && warp.y == y && available(warp, position.returnPoint())) {
                    door = true;
                    break;
                }
            }
        }
        int fromHeight = source.height(position.x(), position.y()), toHeight = target.height(x, y);
        boolean matchingHeight = fromHeight == toHeight || fromHeight == -1 || toHeight == -1
                || toHeight == 14 || toHeight == -2 || fromHeight == 0 && toHeight == 2;
        boolean blocked = (!target.walkable(x, y) || !crossing && !matchingHeight) && !door
                || target.occupied.contains(y * 65536 + x);
        if (!noclip && blocked) return new Decision(position, false, false);
        Position next = at(group, map, x, y, direction, position.returnPoint());
        Decision entered = warp(next, direction, true);
        return entered == null ? new Decision(next, true, false)
                : new Decision(entered.position(), true, true, next);
    }
}
