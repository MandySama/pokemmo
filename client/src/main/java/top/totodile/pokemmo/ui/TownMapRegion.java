package top.totodile.pokemmo.ui;

import top.totodile.pokemmo.rom.FireRedAtlas;
import top.totodile.pokemmo.rom.FireRedAtlas.Area;
import top.totodile.pokemmo.rom.FireRedAtlas.Key;
import top.totodile.pokemmo.rom.FireRedMap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 城镇地图的室外地图拼接与像素合成：按 ROM 连接方向/偏移求出每张室外地图的格坐标，
 * 再按连接关系把整幅地图降采样成一张区域图。纯 CPU 逻辑，不含控件和 OpenGL 状态。
 */
final class TownMapRegion {
    record Tile(int x, int y) {
    }

    /** 区域拼接结果：地图编号到格坐标的映射（已平移到原点）以及整体格范围。 */
    record Layout(Map<Key, Tile> tiles, Key anchor, int maxX, int maxY) {
        boolean contains(Key key) {
            return tiles.containsKey(key);
        }
    }

    /**
     * 取包含当前地图的室外连通分量；当前地图在室内时改用通往它的室外地图，
     * 都找不到时退回格数最多的室外分量。
     */
    static Layout layout(FireRedAtlas atlas, Key current) throws IOException {
        Key anchor = anchorFor(atlas, current);
        if (anchor != null) return layoutFrom(atlas, anchor);
        Layout largest = null;
        long largestTiles = -1;
        Set<Key> visited = new HashSet<>();
        for (Area area : atlas.areas()) {
            if (!outdoor(area) || !visited.add(area.key())) continue;
            Layout candidate = layoutFrom(atlas, area.key());
            visited.addAll(candidate.tiles().keySet());
            long tiles = candidate.tiles().keySet().stream()
                    .mapToLong(key -> areaSize(atlas, key)).sum();
            if (tiles > largestTiles) {
                largestTiles = tiles;
                largest = candidate;
            }
        }
        if (largest == null) throw new IOException("没有可拼接的室外地图。");
        return largest;
    }

    /**
     * 当前地图本身是室外时直接返回；室内地图沿传送事件逐层向外查找室外地图，
     * 动态出口（127/255）不作为路径，多层建筑因此也能定位到所在城镇。
     */
    private static Key anchorFor(FireRedAtlas atlas, Key current) throws IOException {
        if (outdoor(atlas.area(current))) return current;
        Set<Key> visited = new HashSet<>();
        ArrayDeque<Key> queue = new ArrayDeque<>();
        visited.add(current);
        queue.add(current);
        for (int hop = 0; hop < 16 && !queue.isEmpty(); hop++) {
            for (int level = queue.size(); level > 0; level--) {
                Area area = atlas.area(queue.removeFirst());
                for (var warp : area.warps()) {
                    Key target = warp.target();
                    if (target.group() >= 127 || target.map() >= 127) continue;
                    if (outdoor(atlas.area(target))) return target;
                    if (visited.add(target)) queue.add(target);
                }
            }
        }
        return null;
    }

    /** 从锚点按连接方向与偏移拼接地块坐标，室内地图不参与区域图。 */
    private static Layout layoutFrom(FireRedAtlas atlas, Key anchor) throws IOException {
        Map<Key, Tile> tiles = new LinkedHashMap<>();
        ArrayDeque<Key> queue = new ArrayDeque<>();
        tiles.put(anchor, new Tile(0, 0));
        queue.add(anchor);
        while (!queue.isEmpty()) {
            Key source = queue.removeFirst();
            Area from = atlas.area(source);
            Tile origin = tiles.get(source);
            for (var connection : from.connections()) {
                if (connection.direction() < 1 || connection.direction() > 4 || tiles.containsKey(connection.target())) continue;
                Area to = atlas.area(connection.target());
                if (!outdoor(to)) continue;
                Tile offset = switch (connection.direction()) {
                    case 1 -> new Tile(connection.offset(), from.rows());
                    case 2 -> new Tile(connection.offset(), -to.rows());
                    case 3 -> new Tile(-to.columns(), connection.offset());
                    case 4 -> new Tile(from.columns(), connection.offset());
                    default -> throw new IllegalStateException();
                };
                tiles.put(to.key(), new Tile(origin.x() + offset.x(), origin.y() + offset.y()));
                queue.add(to.key());
            }
        }
        // 平移到位图原点，使同一连通分量不因起始地图不同而产生不同坐标。
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        for (Tile tile : tiles.values()) {
            minX = Math.min(minX, tile.x());
            minY = Math.min(minY, tile.y());
        }
        Map<Key, Tile> normalized = new LinkedHashMap<>();
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (var entry : tiles.entrySet()) {
            Tile tile = new Tile(entry.getValue().x() - minX, entry.getValue().y() - minY);
            normalized.put(entry.getKey(), tile);
            Area area = atlas.area(entry.getKey());
            maxX = Math.max(maxX, tile.x() + area.columns());
            maxY = Math.max(maxY, tile.y() + area.rows());
        }
        return new Layout(Map.copyOf(normalized), anchor, maxX, maxY);
    }

    /**
     * 把区域每 divisor 格合并成一个像素；取整块的算术平均，避免降采样后出现噪点，
     * 空白处保持透明由窗口背景补齐。
     */
    static BufferedImage compose(FireRedAtlas atlas, Layout layout, int divisor) throws IOException {
        if (divisor < 1) throw new IllegalArgumentException("无效的城镇地图缩放。");
        int width = layout.maxX() * 16 / divisor;
        int height = layout.maxY() * 16 / divisor;
        if (width < 1 || height < 1) throw new IOException("城镇地图尺寸无效。");
        int[] pixels = new int[width * height];
        for (var entry : layout.tiles().entrySet()) {
            FireRedMap map = atlas.load(entry.getKey(), true);
            int[] source = map.pixels();
            int columns = map.width(), rows = map.height();
            int originX = entry.getValue().x() * 16;
            int originY = entry.getValue().y() * 16;
            for (int top = 0; top < rows; top += divisor) {
                int targetY = (originY + top) / divisor;
                if (targetY < 0 || targetY >= height) continue;
                for (int left = 0; left < columns; left += divisor) {
                    int targetX = (originX + left) / divisor;
                    if (targetX < 0 || targetX >= width) continue;
                    int endY = Math.min(top + divisor, rows), endX = Math.min(left + divisor, columns);
                    long alpha = 0, red = 0, green = 0, blue = 0;
                    int count = 0;
                    for (int y = top; y < endY; y++) {
                        for (int x = left; x < endX; x++) {
                            int pixel = source[y * columns + x];
                            alpha += pixel & 255;
                            blue += pixel >>> 8 & 255;
                            green += pixel >>> 16 & 255;
                            red += pixel >>> 24 & 255;
                            count++;
                        }
                    }
                    if (count == 0) continue;
                    pixels[targetY * width + targetX] = (int) (alpha / count) << 24 | (int) (red / count) << 16
                            | (int) (green / count) << 8 | (int) (blue / count);
                }
            }
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    private static boolean outdoor(Area area) {
        return area.type() >= 1 && area.type() <= 3;
    }

    private static long areaSize(FireRedAtlas atlas, Key key) {
        try {
            Area area = atlas.area(key);
            return (long) area.columns() * area.rows();
        } catch (IOException exception) {
            return 0;
        }
    }

    private TownMapRegion() {
    }
}
