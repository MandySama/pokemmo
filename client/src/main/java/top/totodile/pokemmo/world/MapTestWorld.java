package top.totodile.pokemmo.world;

import top.totodile.pokemmo.rom.FireRedAtlas;
import top.totodile.pokemmo.rom.FireRedAtlas.Area;
import top.totodile.pokemmo.rom.FireRedAtlas.Key;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;

/** 地图测试的镜头、连接拼接和传送历史，不依赖图形上下文或游戏服务。 */
public final class MapTestWorld {
    public record Position(int x, int y) {
    }

    private record Visit(Key key, double x, double y) {
    }

    private final FireRedAtlas atlas;
    private final ArrayDeque<Visit> history = new ArrayDeque<>();
    private Map<Key, Position> positions;
    private Key current;
    private double centerX;
    private double centerY;

    public MapTestWorld(FireRedAtlas atlas) throws IOException {
        this.atlas = atlas;
        select(new Key(3, 0));
    }

    public Map<Key, Position> positions() {
        return java.util.Collections.unmodifiableMap(positions);
    }

    public Key current() {
        return current;
    }

    public double centerX() {
        return centerX;
    }

    public double centerY() {
        return centerY;
    }

    public void pan(double x, double y) {
        centerX += x;
        centerY += y;
    }

    public void select(Key key) throws IOException {
        Area area = atlas.area(key);
        Map<Key, Position> next = new LinkedHashMap<>();
        ArrayDeque<Key> queue = new ArrayDeque<>();
        next.put(key, new Position(0, 0));
        queue.add(key);
        while (!queue.isEmpty()) {
            Key source = queue.removeFirst();
            Area from = atlas.area(source);
            Position origin = next.get(source);
            for (var connection : from.connections()) {
                if (connection.direction() < 1 || connection.direction() > 4 || next.containsKey(connection.target())) continue;
                Area to = atlas.area(connection.target());
                Position offset = switch (connection.direction()) {
                    case 1 -> new Position(connection.offset(), from.rows());
                    case 2 -> new Position(connection.offset(), -to.rows());
                    case 3 -> new Position(-to.columns(), connection.offset());
                    case 4 -> new Position(from.columns(), connection.offset());
                    default -> throw new IllegalStateException();
                };
                next.put(to.key(), new Position(origin.x() + offset.x(), origin.y() + offset.y()));
                queue.add(to.key());
            }
        }
        current = key;
        positions = next;
        centerX = area.columns() * 8.0;
        centerY = area.rows() * 8.0;
    }

    /** 点击事件坐标而非碰撞格；拖动只改变镜头，不触发步行或传送。 */
    public boolean click(double pixelX, double pixelY) throws IOException {
        for (var entry : positions.entrySet()) {
            Area area = atlas.area(entry.getKey());
            int x = (int) Math.floor(pixelX / 16) - entry.getValue().x();
            int y = (int) Math.floor(pixelY / 16) - entry.getValue().y();
            for (var warp : area.warps()) {
                if (warp.x() != x || warp.y() != y) continue;
                // 动态出口由进入前的位置确定，不能把 127/255 当成实际地图编号。
                if (warp.target().group() == 127 || warp.target().map() == 127
                        || warp.target().group() == 255 || warp.target().map() == 255 || warp.targetWarp() == 255) {
                    return back();
                }
                Area target = atlas.area(warp.target());
                if (warp.targetWarp() >= target.warps().size()) throw new IOException("Invalid destination warp");
                var arrival = target.warps().get(warp.targetWarp());
                Visit previous = new Visit(current, centerX, centerY);
                select(target.key());
                centerX = arrival.x() * 16 + 8;
                centerY = arrival.y() * 16 + 8;
                history.push(previous);
                return true;
            }
        }
        return false;
    }

    public boolean back() throws IOException {
        if (history.isEmpty()) return false;
        Visit visit = history.peek();
        select(visit.key());
        centerX = visit.x();
        centerY = visit.y();
        history.pop();
        return true;
    }

    public void home() throws IOException {
        browse(new Key(3, 0));
    }

    public void browse(Key key) throws IOException {
        select(key);
        history.clear();
    }
}
