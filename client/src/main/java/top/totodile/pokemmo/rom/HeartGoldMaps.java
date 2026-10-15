package top.totodile.pokemmo.rom;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.*;

/** 心金／魂银地图头、矩阵、地形与传送事件的独立解码。 */
public final class HeartGoldMaps implements NdsMaps {
    private final List<Header> headers = new ArrayList<>();
    private final List<Boolean> interiors = new ArrayList<>();
    private final NdsArchive matrices, models, textures, areas, buildings, indoorBuildings, events;
    private final Map<Integer, Chunk> decoded = new HashMap<>();

    public HeartGoldMaps(Path rom) throws IOException {
        ByteBuffer arm9 = NdsArchive.arm9(rom, true);
        int start = -1;
        for (int i = 0; i <= arm9.limit() - 8; i += 4) {
            if (arm9.getInt(i) == 33489410 && arm9.getInt(i + 4) == 11076050) {
                start = i + 16;
                break;
            }
        }
        if (start < 0) throw new IOException("Missing HGSS map headers");
        int count = NdsArchive.resource(rom, "/fielddata/maptable/mapname.bin").remaining() / 16;
        for (int i = 0; i < count; i++) {
            int offset = start + i * 24;
            interiors.add((arm9.get(offset + 21) & 7) == 4);
            headers.add(
                    new Header(
                            i,
                            arm9.get(offset + 1) & 255,
                            u16(arm9, offset + 4),
                            u16(arm9, offset + 16),
                            arm9.get(offset + 18) & 255));
        }
        matrices = new NdsArchive(rom, "/a/0/4/1");
        models = new NdsArchive(rom, "/a/0/6/5");
        textures = new NdsArchive(rom, "/a/0/4/4");
        areas = new NdsArchive(rom, "/a/0/4/2");
        buildings = new NdsArchive(rom, "/a/0/4/0");
        indoorBuildings = new NdsArchive(rom, "/a/1/4/8");
        events = new NdsArchive(rom, "/a/0/3/2");
    }

    @Override
    public List<Header> headers() {
        return List.copyOf(headers);
    }

    @Override
    public Header header(int id) {
        return headers.get(id);
    }

    @Override
    public int chunkCount() {
        return models.size();
    }

    @Override
    public boolean indoor(int headerId) {
        return interiors.get(headerId);
    }

    @Override
    public Matrix matrix(int id) throws IOException {
        try {
            ByteBuffer data = matrices.file(header(id).matrix());
            int width = data.get() & 255, height = data.get() & 255;
            boolean indexed = data.get() == 1, elevated = data.get() == 1;
            int length = data.get() & 255;
            data.position(data.position() + length);
            int count = width * height;
            int[] names = indexed ? new int[count] : null, chunks = new int[count];
            float[] offsets = new float[count];
            if (indexed) for (int i = 0; i < count; i++) names[i] = data.getShort();
            if (elevated) for (int i = 0; i < count; i++) offsets[i] = data.get() / 8f;
            for (int i = 0; i < count; i++) chunks[i] = data.getShort();
            if (data.hasRemaining()) throw new IOException("Trailing HGSS matrix data");
            return new Matrix(width, height, chunks, names, offsets);
        } catch (RuntimeException error) {
            throw new IOException("Invalid HGSS matrix: " + id, error);
        }
    }

    @Override
    public ByteBuffer textures(int headerId, int season) {
        return textures.file(u16(areas.file(header(headerId).area()), 2));
    }

    @Override
    public ByteBuffer buildingTextures(int headerId, int season) {
        return null;
    }

    @Override
    public List<Building> buildings(int headerId, int season) {
        List<Building> result = new ArrayList<>();
        NdsArchive selected = indoor(headerId) ? indoorBuildings : buildings;
        for (int id = 0; id < selected.size(); id++)
            result.add(new Building(id, selected.file(id)));
        return List.copyOf(result);
    }

    @Override
    public Chunk chunk(int id) throws IOException {
        Chunk cached = decoded.get(id);
        if (cached != null) return cached;
        try {
            ByteBuffer data = models.file(id);
            int terrainSize = data.getInt(), objectSize = data.getInt(), modelSize = data.getInt();
            data.getInt();
            int attributesOffset = 20 + u16(data, 18);
            int objectOffset = attributesOffset + terrainSize,
                    modelOffset = objectOffset + objectSize;
            int elevationOffset = modelOffset + modelSize;
            short[] attributes = new short[1024];
            data.position(attributesOffset);
            for (int i = 0; i < attributes.length; i++) attributes[i] = data.getShort();
            List<Terrain> layers =
                    PlatinumTerrain.decode(
                            data.duplicate()
                                    .order(ByteOrder.LITTLE_ENDIAN)
                                    .position(elevationOffset)
                                    .slice()
                                    .order(ByteOrder.LITTLE_ENDIAN),
                            attributes);
            var result =
                    new Chunk(
                            id,
                            data.slice(modelOffset, modelSize).order(ByteOrder.LITTLE_ENDIAN),
                            layers,
                            data.slice(objectOffset, objectSize).order(ByteOrder.LITTLE_ENDIAN));
            decoded.put(id, result);
            return result;
        } catch (RuntimeException error) {
            throw new IOException("Invalid HGSS land: " + id, error);
        }
    }

    @Override
    public List<Placement> placements(Chunk chunk) throws IOException {
        ByteBuffer data = chunk.objects().duplicate().order(ByteOrder.LITTLE_ENDIAN);
        List<Placement> result = new ArrayList<>();
        for (int offset = 0; offset + 48 <= data.limit(); offset += 48) {
            int model = data.getInt(offset);
            float x = fixed(data, offset + 4),
                    y = fixed(data, offset + 8),
                    z = fixed(data, offset + 12);
            result.add(new Placement(model, x / 4, y / 4, z / 4, 0));
        }
        return List.copyOf(result);
    }

    @Override
    public List<Warp> warps(int headerId) throws IOException {
        ByteBuffer data = events.file(header(headerId).events());
        try {
            int backgrounds = data.getInt();
            data.position(data.position() + backgrounds * 20);
            int objects = data.getInt();
            data.position(data.position() + objects * 32);
            int count = data.getInt();
            List<Warp> result = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int x = u16(data, data.position()), y = u16(data, data.position() + 2);
                int target = u16(data, data.position() + 4),
                        entrance = u16(data, data.position() + 6);
                data.position(data.position() + 12);
                Matrix matrix = matrix(headerId);
                int cell = y / 32 * matrix.width() + x / 32;
                int behavior =
                        cell >= 0 && cell < matrix.models().length && matrix.models()[cell] >= 0
                                ? chunk(matrix.models()[cell])
                                        .layers()
                                        .getFirst()
                                        .value(2, x % 32, y % 32)
                                : 0;
                int direction =
                        switch (behavior) {
                            case 94, 98, 108 -> 3;
                            case 95, 99, 109 -> 2;
                            case 101, 111 -> 0;
                            case 105, 110 -> 1;
                            default -> -1;
                        };
                int type =
                        switch (behavior) {
                            case 94, 95, 98, 99, 101, 108, 109, 111 -> 1;
                            default -> 2;
                        };
                result.add(new Warp(target, entrance, direction, type, false, x, 0, y, 1, 1));
            }
            return List.copyOf(result);
        } catch (RuntimeException error) {
            throw new IOException("Invalid HGSS events: " + headerId, error);
        }
    }

    private static int u16(ByteBuffer data, int offset) {
        return Short.toUnsignedInt(data.getShort(offset));
    }

    private static float fixed(ByteBuffer data, int offset) {
        return u16(data, offset) / 65536f + data.getShort(offset + 2);
    }
}
