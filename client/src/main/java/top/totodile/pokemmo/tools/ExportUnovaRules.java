package top.totodile.pokemmo.tools;

import top.totodile.pokemmo.rom.BlackWhiteMaps;
import top.totodile.pokemmo.protocol.NdsTerrainHeights;

import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

/** 输出服务端所需地形及事件，不包含模型、纹理和脚本。 */
public final class ExportUnovaRules {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("ROM output-directory");
        export(Path.of(args[0]), Path.of(args[1]), 2);
    }

    static void export(Path rom, Path output, int region) throws Exception {
        top.totodile.pokemmo.rom.NdsMaps maps = region == 2 ? new BlackWhiteMaps(rom) : new top.totodile.pokemmo.rom.PlatinumMaps(rom);
        String name = region == 2 ? "unova" : "sinnoh";
        Files.createDirectories(output);
        var matrices = new HashMap<Integer, top.totodile.pokemmo.rom.NdsMaps.Matrix>();
        StringBuilder sizes = new StringBuilder();
        try (var data = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(output.resolve(name + "-world.gz"))))) {
            data.writeInt(region == 2 ? 0x42575731 : 0x42575732);
            data.writeInt(maps.headers().size());
            for (var header : maps.headers()) {
                var matrix = maps.matrix(header.id());
                matrices.putIfAbsent(header.matrix(), matrix);
                int tileWidth = 32, tileHeight = 32;
                for (int id : matrix.models()) {
                    if (id >= 0 && !maps.chunk(id).layers().isEmpty()) {
                        var terrain = maps.chunk(id).layers().getFirst();
                        tileWidth = terrain.width(); tileHeight = terrain.height(); break;
                    }
                }
                data.writeInt(header.matrix()); data.writeInt(header.name());
                data.writeInt(tileWidth); data.writeInt(tileHeight);
                sizes.append(header.id() >>> 8).append(',').append(header.id() & 255).append(',')
                        .append(matrix.width() * tileWidth).append(',').append(matrix.height() * tileHeight)
                        .append(',').append(140000 + region * 1000 + header.name()).append('\n');
                var warps = maps.warps(header.id());
                data.writeInt(warps.size());
                for (var warp : warps) {
                    data.writeInt(warp.target()); data.writeInt(warp.entrance()); data.writeInt(warp.direction());
                    data.writeInt(warp.type()); data.writeBoolean(warp.special());
                    data.writeInt(warp.x()); data.writeInt(warp.y()); data.writeInt(warp.elevation());
                    data.writeInt(warp.width()); data.writeInt(warp.height());
                }
            }
            data.writeInt(matrices.size());
            for (var entry : matrices.entrySet()) {
                var matrix = entry.getValue();
                data.writeInt(entry.getKey()); data.writeInt(matrix.width()); data.writeInt(matrix.height());
                data.writeBoolean(matrix.headers() != null);
                for (int id : matrix.models()) data.writeInt(id);
                if (matrix.headers() != null) for (int id : matrix.headers()) data.writeInt(id);
                if (region == 3) for (float value : matrix.offsets()) data.writeFloat(value);
            }
            data.writeInt(maps.chunkCount());
            for (int id = 0; id < maps.chunkCount(); id++) {
                var chunk = maps.chunk(id);
                data.writeInt(chunk.layers().size());
                for (var terrain : chunk.layers()) {
                    data.writeInt(terrain.width()); data.writeInt(terrain.height());
                    for (int y = 0; y < terrain.height(); y++) for (int x = 0; x < terrain.width(); x++) {
                        data.writeBoolean(!terrain.blocked(x, y));
                        data.writeFloat(terrain.height(x, y));
                    }
                }
            }
        }
        Files.writeString(output.resolve(name + ".csv"), sizes);
        System.out.println(name + " headers=" + maps.headers().size() + " chunks=" + maps.chunkCount());
    }
}
