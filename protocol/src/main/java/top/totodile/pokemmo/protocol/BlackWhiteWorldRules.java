package top.totodile.pokemmo.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static top.totodile.pokemmo.protocol.CharacterPackets.*;

/** 黑白／白金场景矩阵、分层碰撞和入口编号；不持有卡带图像。 */
public final class BlackWhiteWorldRules implements WorldRules {
    public record Warp(int target, int entrance, int direction, int type, boolean special,
                       int x, int y, int elevation, int width, int height) {
        boolean contains(int x, int y) {
            return !special && x >= this.x && y >= this.y && x < this.x + width && y < this.y + height;
        }
    }

    public record Header(int matrix, int name, int tileWidth, int tileHeight, Warp[] warps) {
    }

    public record Matrix(int width, int height, int[] chunks, int[] headers, float[] offsets) {
    }

    public record Terrain(int width, int height, boolean[] walkable, float[] heights) {
    }

    private final int region;
    private final Header[] headers;
    private final Map<Integer, Matrix> matrices = new HashMap<>();
    private final Terrain[][] chunks;

    public BlackWhiteWorldRules() throws IOException { this(2); }

    public BlackWhiteWorldRules(int region) throws IOException {
        if (region != 2 && region != 3) throw new IllegalArgumentException("Invalid NDS region");
        this.region = region;
        var raw = BlackWhiteWorldRules.class.getResourceAsStream(region == 2 ? "/maps/unova-world.gz" : "/maps/sinnoh-world.gz");
        if (raw == null) throw new IOException("Missing NDS world rules");
        try (var input = new DataInputStream(new GZIPInputStream(raw))) {
            int version = input.readInt();
            if (version != (region == 2 ? 0x42575731 : 0x42575732)) throw new IOException("Invalid NDS rules");
            int count = input.readInt();
            if (count != (region == 2 ? 427 : 593)) throw new IOException("Incomplete NDS headers");
            headers = new Header[count];
            for (int i = 0; i < count; i++) {
                int matrix = input.readInt(), name = input.readInt(), width = input.readInt(), height = input.readInt();
                Warp[] warps = new Warp[input.readInt()];
                for (int j = 0; j < warps.length; j++) {
                    warps[j] = new Warp(input.readInt(), input.readInt(), input.readInt(), input.readInt(),
                            input.readBoolean(), input.readInt(), input.readInt(), input.readInt(), input.readInt(), input.readInt());
                }
                headers[i] = new Header(matrix, name, width, height, warps);
            }
            count = input.readInt();
            for (int i = 0; i < count; i++) {
                int id = input.readInt(), width = input.readInt(), height = input.readInt();
                boolean linked = input.readBoolean();
                int[] models = new int[width * height], names = linked ? new int[width * height] : null;
                for (int j = 0; j < models.length; j++) models[j] = input.readInt();
                if (names != null) for (int j = 0; j < names.length; j++) names[j] = input.readInt();
                float[] offsets = new float[models.length];
                if (version == 0x42575732) for (int j = 0; j < offsets.length; j++) offsets[j] = input.readFloat();
                matrices.put(id, new Matrix(width, height, models, names, offsets));
            }
            chunks = new Terrain[input.readInt()][];
            for (int i = 0; i < chunks.length; i++) {
                chunks[i] = new Terrain[input.readInt()];
                for (int j = 0; j < chunks[i].length; j++) {
                    int width = input.readInt(), height = input.readInt();
                    boolean[] walkable = new boolean[width * height];
                    float[] heights = new float[walkable.length];
                    for (int k = 0; k < walkable.length; k++) {
                        walkable[k] = input.readBoolean(); heights[k] = input.readFloat();
                    }
                    chunks[i][j] = new Terrain(width, height, walkable, heights);
                }
            }
            if (input.read() != -1) throw new IOException("Trailing NDS rules");
        }
    }

    public Header header(int id) {
        if (id < 0 || id >= headers.length) throw new IllegalArgumentException("Invalid NDS header");
        return headers[id];
    }

    public Matrix matrix(int id) {
        return matrices.get(header(id).matrix());
    }

    public int headerAt(int id, int x, int y) {
        Header header = header(id);
        Matrix matrix = matrix(id);
        int column = x / header.tileWidth(), row = y / header.tileHeight();
        if (x < 0 || y < 0 || column >= matrix.width() || row >= matrix.height()) return -1;
        int cell = row * matrix.width() + column;
        if (matrix.chunks()[cell] < 0) return -1;
        int next = matrix.headers() == null ? id : matrix.headers()[cell];
        return next >= 0 && next < headers.length ? next : id;
    }

    private Terrain terrain(int id, int x, int y, int layer) {
        Header header = header(id);
        Matrix matrix = matrix(id);
        if (headerAt(id, x, y) < 0) return null;
        int chunk = matrix.chunks()[y / header.tileHeight() * matrix.width() + x / header.tileWidth()];
        return chunk >= 0 && chunk < chunks.length && layer >= 0 && layer < chunks[chunk].length
                ? chunks[chunk][layer] : null;
    }

    public float height(int id, int x, int y, int layer) {
        Terrain terrain = terrain(id, x, y, layer);
        if (terrain == null) return 0;
        Header header = header(id);
        Matrix matrix = matrix(id);
        return terrain.heights()[y % terrain.height() * terrain.width() + x % terrain.width()]
                + matrix.offsets()[y / header.tileHeight() * matrix.width() + x / header.tileWidth()] * 4;
    }

    public boolean walkable(int id, int x, int y, int layer) {
        Terrain terrain = terrain(id, x, y, layer);
        return terrain != null && terrain.walkable()[y % terrain.height() * terrain.width() + x % terrain.width()];
    }

    @Override
    public Position at(int group, int map, int x, int y, int facing, ReturnPoint returnPoint) {
        int id = headerAt(group * 256 + map, x, y);
        if (id < 0) throw new IllegalArgumentException("Invalid NDS coordinates");
        return new Position(id >>> 8, id & 255, x, y, 0, facing, null);
    }

    private FireRedWorldRules.Decision warp(Position source, Warp warp) {
        if (warp.target() < 0 || warp.target() >= headers.length || warp.special()) return null;
        Warp[] destinations = header(warp.target()).warps();
        if (warp.entrance() >= destinations.length) return null;
        Warp arrival = destinations[warp.entrance()];
        if (arrival.special()) return null;
        int facing = arrival.direction() < 0 ? source.facing() : arrival.direction() ^ 1;
        int x = arrival.x() + (arrival.width() - 1) / 2;
        int y = arrival.y() + (arrival.height() - 1) / 2;
        int dx = facing == 2 ? -1 : facing == 3 ? 1 : 0;
        int dy = facing == 1 ? -1 : facing == 0 ? 1 : 0;
        if (walkable(warp.target(), x + dx, y + dy, 0)) { x += dx; y += dy; }
        if (headerAt(warp.target(), x, y) < 0) return null;
        Position target = at(warp.target() >>> 8, warp.target() & 255, x, y, facing, null);
        return new FireRedWorldRules.Decision(target, true, true);
    }

    @Override
    public FireRedWorldRules.Decision move(Position source, int direction, boolean noclip) {
        if (direction < 0 || direction > 3 || !source.valid(region)) throw new IllegalArgumentException("Invalid NDS move");
        int id = source.group() * 256 + source.map();
        Position turned = new Position(source.group(), source.map(), source.x(), source.y(), source.elevation(), direction);
        int x = source.x() + (direction == 2 ? -1 : direction == 3 ? 1 : 0);
        int y = source.y() + (direction == 1 ? -1 : direction == 0 ? 1 : 0);
        for (Warp warp : header(id).warps()) {
            if ((warp.direction() == -1 || warp.direction() == direction)
                    && (region == 3 ? warp.type() == 1 ? warp.contains(source.x(), source.y()) : warp.contains(x, y)
                    : warp.contains(x, y) || warp.type() == 1 && warp.contains(source.x(), source.y()))) {
                var result = warp(turned, warp);
                if (result != null) return result;
            }
        }
        int next = headerAt(id, x, y);
        int layer = -1;
        float difference = Float.MAX_VALUE;
        boolean standingEntrance = false;
        if (region == 3) for (Warp warp : header(id).warps()) {
            if (warp.type() == 1 && warp.contains(x, y)) { standingEntrance = true; break; }
        }
        if (next >= 0) {
            float fromHeight = height(id, source.x(), source.y(), source.elevation());
            for (int candidate = 0; candidate < 16; candidate++) {
                if (!walkable(id, x, y, candidate)) continue;
                float distance = Math.abs(height(id, x, y, candidate) - fromHeight);
                if (distance < difference) { layer = candidate; difference = distance; }
            }
        }
        if (standingEntrance && next >= 0) { layer = 0; difference = 0; }
        if (next < 0 || !noclip && (layer < 0 || difference >= 1.27f)) {
            return new FireRedWorldRules.Decision(turned, false, false);
        }
        if (noclip) layer = terrain(id, x, y, source.elevation()) == null ? 0 : source.elevation();
        return new FireRedWorldRules.Decision(new Position(next >>> 8, next & 255, x, y, layer, direction), true, false);
    }
}
