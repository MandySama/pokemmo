package top.totodile.pokemmo.protocol;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

/** 与通行目录按模型和高度层对应的 NDS 地块行为，不含图像。 */
final class NdsEncounterTiles {
    private record Layer(int width, int height, short[] values) { }
    private static final ConcurrentHashMap<Integer, NdsEncounterTiles> CACHE = new ConcurrentHashMap<>();
    private final Layer[][] chunks;
    static NdsEncounterTiles get(int region) { return CACHE.computeIfAbsent(region, NdsEncounterTiles::new); }
    private NdsEncounterTiles(int region) {
        String name = region == 2 ? "unova" : region == 3 ? "sinnoh" : "johto";
        try (var raw = NdsEncounterTiles.class.getResourceAsStream("/maps/" + name + "-encounter-tiles.gz")) {
            if (raw == null) throw new IOException("Missing encounter terrain: " + name);
            try (var input = new DataInputStream(new GZIPInputStream(raw))) {
                if (input.readInt() != 0x454E5431) throw new IOException("Invalid encounter terrain version");
                int count = input.readInt();
                if (count < 1 || count > 10000) throw new IOException("Invalid encounter chunk count");
                chunks = new Layer[count][];
                for (int id = 0; id < count; id++) {
                    int layers = input.readInt();
                    if (layers < 0 || layers > 16) throw new IOException("Invalid encounter layer count");
                    chunks[id] = new Layer[layers];
                    for (int layer = 0; layer < layers; layer++) {
                        int width = input.readInt(), height = input.readInt();
                        if (width < 1 || height < 1 || width > 1024 || height > 1024) throw new IOException("Invalid encounter terrain size");
                        short[] values = new short[width * height];
                        for (int i = 0; i < values.length; i++) values[i] = input.readShort();
                        chunks[id][layer] = new Layer(width, height, values);
                    }
                }
                if (input.read() != -1) throw new IOException("Trailing encounter terrain");
            }
        } catch (IOException error) { throw new UncheckedIOException(error); }
    }
    int behavior(int chunk, int layer, int x, int y) {
        if (chunk < 0 || chunk >= chunks.length || layer < 0 || layer >= chunks[chunk].length) return -1;
        var terrain = chunks[chunk][layer];
        return terrain.values()[Math.floorMod(y, terrain.height()) * terrain.width() + Math.floorMod(x, terrain.width())];
    }
}
