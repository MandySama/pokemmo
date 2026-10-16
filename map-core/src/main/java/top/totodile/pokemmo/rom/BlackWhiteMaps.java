package top.totodile.pokemmo.rom;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 黑白地图头、场景矩阵及四通道地形；CPU 数据不依赖 OpenGL。 */
public final class BlackWhiteMaps implements NdsMaps {
    private final Path rom;
    @Override public Path rom() { return rom; }
    private final List<Header> headers = new ArrayList<>();
    private final NdsArchive matrices;
    private final NdsArchive models;
    private final NdsArchive textures;
    private final ByteBuffer areas;
    private final Map<Integer, Chunk> decoded = new HashMap<>();
    private final NdsArchive outdoorBuildings, indoorBuildings, outdoorTextures, indoorTextures;
    private final NdsArchive events;
    public BlackWhiteMaps(Path rom) throws IOException {
        this.rom = rom;
        ByteBuffer table = new NdsArchive(rom, "/a/0/1/2").file(0);
        if (table.remaining() % 48 != 0) throw new IOException("Invalid BW map headers");
        for (int offset = 0; offset < table.limit(); offset += 48) {
            headers.add(new Header(offset / 48, u16(table, offset + 2), u16(table, offset + 4),
                    u16(table, offset + 22), table.get(offset + 26) & 255));
        }
        matrices = new NdsArchive(rom, "/a/0/0/9");
        models = new NdsArchive(rom, "/a/0/0/8");
        textures = new NdsArchive(rom, "/a/0/1/4");
        areas = NdsArchive.resource(rom, "/a/0/1/3");
        outdoorBuildings = new NdsArchive(rom, "/a/2/2/9");
        indoorBuildings = new NdsArchive(rom, "/a/2/3/0");
        outdoorTextures = new NdsArchive(rom, "/a/1/7/6");
        indoorTextures = new NdsArchive(rom, "/a/1/7/7");
        events = new NdsArchive(rom, "/a/1/2/5");
    }

    public List<Header> headers() {
        return List.copyOf(headers);
    }

    public Header header(int id) {
        return headers.get(id);
    }

    public int chunkCount() {
        return models.size();
    }

    public Matrix matrix(int id) throws IOException {
        try {
            ByteBuffer data = matrices.file(header(id).matrix());
            int flags = data.getInt();
            int width = Short.toUnsignedInt(data.getShort());
            int height = Short.toUnsignedInt(data.getShort());
            int count = Math.multiplyExact(width, height);
            if (count == 0 || count > 65536 || data.remaining() != count * (flags == 1 ? 8 : 4)) {
                throw new IOException("Invalid BW map matrix: " + id);
            }
            int[] modelIds = new int[count];
            int[] headerIds = flags == 1 ? new int[count] : null;
            for (int i = 0; i < count; i++) modelIds[i] = data.getInt();
            if (headerIds != null) for (int i = 0; i < count; i++) headerIds[i] = data.getInt();
            return new Matrix(width, height, modelIds, headerIds);
        } catch (IndexOutOfBoundsException | ArithmeticException exception) {
            throw new IOException("Invalid BW map matrix: " + id, exception);
        }
    }

    public ByteBuffer textures(int headerId, int season) throws IOException {
        int area = header(headerId).area();
        if (area >= 2 && area <= 206) area += Math.floorMod(season, 4);
        return textures.file(u16(areas, area * 10 + 2));
    }

    private int area(int headerId, int season) {
        int area = header(headerId).area();
        return area >= 2 && area <= 206 ? area + Math.floorMod(season, 4) : area;
    }

    public boolean indoor(int headerId) {
        return areas.get(area(headerId, 0) * 10 + 6) == 0;
    }

    public int buildingSet(int headerId, int season) {
        int texture = u16(areas, area(headerId, season) * 10 + 2);
        return texture < 210 ? texture < 2 ? 0 : (texture - 2) / 4 : texture - 210;
    }

    public ByteBuffer buildingTextures(int headerId, int season) throws IOException {
        return (indoor(headerId) ? indoorTextures : outdoorTextures).file(buildingSet(headerId, season));
    }

    public List<Building> buildings(int headerId, int season) throws IOException {
        ByteBuffer data = (indoor(headerId) ? indoorBuildings : outdoorBuildings).file(buildingSet(headerId, season));
        if (u16(data, 0) != 16961) throw new IOException("Invalid BW building layout");
        int count = u16(data, 2) / 2;
        List<Building> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int entry = data.getInt(4 + i * 4);
            int model = data.getInt(4 + count * 4 + i * 4);
            int length = data.getInt(model + 8);
            result.add(new Building(u16(data, entry), data.slice(model, length).order(ByteOrder.LITTLE_ENDIAN)));
        }
        return List.copyOf(result);
    }

    public List<Placement> placements(Chunk chunk) throws IOException {
        ByteBuffer data = chunk.objects().duplicate().order(ByteOrder.LITTLE_ENDIAN);
        int count = data.getInt();
        if (count < 0 || count * 16L > data.remaining()) throw new IOException("Invalid BW placements");
        List<Placement> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float x = Short.toUnsignedInt(data.getShort()) / 65536f + data.getShort();
            float y = Short.toUnsignedInt(data.getShort()) / 65536f + data.getShort();
            float z = -(Short.toUnsignedInt(data.getShort()) / 65536f + data.getShort());
            data.get();
            int rotation = data.get() & 255;
            int model = (data.get() & 255) << 8 | data.get() & 255;
            result.add(new Placement(model, x / 4, y / 4, z / 4, rotation));
        }
        return List.copyOf(result);
    }

    public List<Warp> warps(int headerId) throws IOException {
        ByteBuffer data = events.file(header(headerId).events());
        if (data.getInt() < 1) return List.of();
        int backgrounds = data.get() & 255, objects = data.get() & 255, count = data.get() & 255;
        data.get();
        data.position(8 + backgrounds * 20 + objects * 36);
        List<Warp> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int target = Short.toUnsignedInt(data.getShort()), entrance = Short.toUnsignedInt(data.getShort());
            int direction = switch (data.get() & 255) { case 1 -> 0; case 2 -> 1; case 3 -> 3; case 4 -> 2; default -> -1; };
            int type = data.get() & 255;
            boolean special = data.getShort() == 1;
            int x = data.getShort(), elevation = data.getShort(), y = data.getShort();
            int width = Short.toUnsignedInt(data.getShort()), height = Short.toUnsignedInt(data.getShort());
            data.getShort();
            result.add(new Warp(target, entrance, direction, type, special, x >> 4, elevation >> 4,
                    y >> 4, Math.max(1, width), Math.max(1, height)));
        }
        return List.copyOf(result);
    }

    public Chunk chunk(int id) throws IOException {
        Chunk cached = decoded.get(id);
        if (cached != null) return cached;
        try {
            ByteBuffer data = models.file(id);
            int magic = data.getInt();
            int modelOffset = data.getInt();
            int layerCount = magic == 149326 ? 0 : magic == 214098 || magic == 213591 ? 1 : 2;
            int[] offsets = new int[layerCount];
            for (int i = 0; i < layerCount; i++) offsets[i] = data.getInt();
            int objectOffset = data.getInt();
            data.getInt();
            int modelSize = data.getInt(modelOffset + 8);
            if (!NdsArchive.text(data, modelOffset, 4).equals("BMD0")) {
                throw new IOException("Missing BW NSBMD: " + id);
            }
            List<Terrain> layers = new ArrayList<>();
            for (int i = 0; i < layerCount; i++) {
                data.position(offsets[i]);
                int width = Short.toUnsignedInt(data.getShort());
                int height = Short.toUnsignedInt(data.getShort());
                int count = Math.multiplyExact(width, height);
                if (count == 0 || count > 1048576) throw new IOException("Invalid BW terrain size");
                short[][] channels = new short[4][count];
                for (int cell = 0; cell < count; cell++) {
                    for (int channel = 0; channel < 4; channel++) {
                        if (magic == 214098) data.position(data.position() + 4);
                        channels[channel][cell] = data.getShort();
                    }
                }
                int end = i + 1 < layerCount ? offsets[i + 1] : objectOffset;
                int remaining = end - data.position();
                if (remaining < 0 || remaining % 8 != 0) throw new IOException("Invalid BW terrain planes");
                short[][] planes = new short[remaining / 8][4];
                for (short[] plane : planes) for (int j = 0; j < 4; j++) plane[j] = data.getShort();
                layers.add(new Terrain(width, height, channels, planes));
            }
            Chunk result = new Chunk(id, data.slice(modelOffset, modelSize).order(ByteOrder.LITTLE_ENDIAN),
                    List.copyOf(layers), data.slice(objectOffset, data.limit() - objectOffset).order(ByteOrder.LITTLE_ENDIAN));
            decoded.put(id, result);
            return result;
        } catch (IndexOutOfBoundsException | IllegalArgumentException | ArithmeticException exception) {
            throw new IOException("Invalid BW map chunk: " + id, exception);
        }
    }

    private static int u16(ByteBuffer data, int offset) {
        return Short.toUnsignedInt(data.getShort(offset));
    }
}
