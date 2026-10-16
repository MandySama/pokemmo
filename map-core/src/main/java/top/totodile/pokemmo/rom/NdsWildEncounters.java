package top.totodile.pokemmo.rom;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;

/** 解码黑白季节表、心金魂银时段表并导出无图像遭遇数据。 */
public final class NdsWildEncounters {
    private static final int[] LAND_WEIGHTS = {20,20,10,10,10,10,5,5,4,4,1,1};
    private static final int[] WATER_WEIGHTS = {60,30,5,4,1};
    private static final int[] HGSS_FISHING_WEIGHTS = {40,30,15,10,5};
    private NdsWildEncounters() { }

    public static void export(Path rom, Path output, int region) throws IOException {
        if (region != 2 && region != 3 && region != 5) throw new IllegalArgumentException("Invalid encounter ROM region");
        NdsMaps maps = region == 2 ? new BlackWhiteMaps(rom) : region == 5 ? new HeartGoldMaps(rom) : new PlatinumMaps(rom);
        String name = region == 2 ? "unova" : region == 5 ? "johto" : "sinnoh";
        Files.createDirectories(output);
        // 地形行为按模型／层保存，位置映射复用世界通行目录，不重复展开室外矩阵。
        try (var out = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(output.resolve(name + "-encounter-tiles.gz"))))) {
            out.writeInt(0x454E5431); out.writeInt(maps.chunkCount());
            for (int id = 0; id < maps.chunkCount(); id++) {
                var layers = maps.chunk(id).layers(); out.writeInt(layers.size());
                for (var layer : layers) {
                    out.writeInt(layer.width()); out.writeInt(layer.height());
                    for (int y = 0; y < layer.height(); y++) for (int x = 0; x < layer.width(); x++) {
                        out.writeShort(layer.blocked(x, y) ? -1 : layer.value(2, x, y));
                    }
                }
            }
        }
        if (region == 3) return;
        ByteBuffer headers = region == 2 ? new NdsArchive(rom, "/a/0/1/2").file(0) : NdsArchive.arm9(rom, true);
        int start = 0;
        if (region == 5) {
            start = -1;
            for (int i = 0; i <= headers.limit() - 8; i += 4) if (headers.getInt(i) == 33489410 && headers.getInt(i + 4) == 11076050) { start = i + 16; break; }
            if (start < 0) throw new IOException("Missing HGSS encounter header table");
        }
        byte[] code = new byte[4];
        try (var file = new RandomAccessFile(rom.toFile(), "r")) { file.seek(12); file.readFully(code); }
        boolean soulSilver = new String(code, java.nio.charset.StandardCharsets.US_ASCII).startsWith("IPG");
        var archive = new NdsArchive(rom, region == 2 ? "/a/1/2/6" : soulSilver ? "/a/1/3/6" : "/a/0/3/7");
        StringBuilder csv = new StringBuilder("# region,group,map,name,kind,season,time,rate,cave,species,min,max,weight\n");
        int count = 0;
        for (var h : maps.headers()) {
            int offset = start + h.id() * (region == 2 ? 48 : 24);
            int wild = region == 2 ? headers.getShort(offset + 20) & 65535 : headers.get(offset) & 255;
            if (wild == (region == 2 ? 65535 : 255)) continue;
            if (wild >= archive.size()) throw new IOException("Invalid encounter table index: " + wild);
            ByteBuffer data = archive.file(wild);
            boolean cave = region == 2 ? maps.indoor(h.id()) : (headers.get(offset + 21) & 7) == 3;
            if (region == 2) {
                if (data.remaining() != 232 && data.remaining() != 928) throw new IOException("Invalid BW encounter block size");
                int seasons = data.remaining() / 232;
                for (int season = 0; season < seasons; season++) {
                    int base = season * 232, slotOffset = base + 8;
                    int[] amounts = {12,12,12,5,5,5,5};
                    for (int kind = 0; kind < amounts.length; kind++) {
                        int rate = data.get(base + kind) & 255;
                        for (int i = 0; i < amounts[kind]; i++) if (rate > 0) {
                            int slot = slotOffset + i * 4;
                            write(csv, region, h, kind, seasons == 1 ? -1 : season, -1, rate, cave,
                                    data.getShort(slot) & 2047, data.get(slot + 2) & 255, data.get(slot + 3) & 255,
                                    kind < 3 ? LAND_WEIGHTS[i] : WATER_WEIGHTS[i]);
                        }
                        slotOffset += amounts[kind] * 4;
                    }
                }
            } else {
                int rate = data.get(0) & 255;
                for (int time = 0; time < 3; time++) for (int i = 0; i < 12; i++) if (rate > 0) {
                    int level = data.get(8 + i) & 255;
                    write(csv, region, h, 0, -1, time, rate, cave, data.getShort(20 + time * 24 + i * 2) & 65535, level, level, LAND_WEIGHTS[i]);
                }
                int slotOffset = 100;
                int[] amounts = {5,2,5,5,5}, kinds = {3,7,8,9,10};
                for (int mode = 0; mode < amounts.length; mode++) {
                    rate = data.get(1 + mode) & 255;
                    for (int i = 0; i < amounts[mode]; i++) if (rate > 0) {
                        int slot = slotOffset + i * 4;
                        int min = data.get(slot) & 255, max = data.get(slot + 1) & 255;
                        write(csv, region, h, kinds[mode], -1, -1, rate, cave,
                                data.getShort(slot + 2) & 65535, min, max, amounts[mode] == 2 ? (i == 0 ? 80 : 20) : mode >= 2 ? HGSS_FISHING_WEIGHTS[i] : WATER_WEIGHTS[i]);
                    }
                    slotOffset += amounts[mode] * 4;
                }
            }
            count++;
        }
        try (var out = new GZIPOutputStream(Files.newOutputStream(output.resolve(name + "-encounters.csv.gz")))) {
            out.write(csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        System.out.println(name + " encounter headers=" + count + " chunks=" + maps.chunkCount());
    }

    private static void write(StringBuilder csv, int region, NdsMaps.Header h, int kind, int season, int time,
                              int rate, boolean cave, int species, int min, int max, int weight) throws IOException {
        if (species < 1 || species > 649 || min < 1 || min > max || max > 100) throw new IOException("Invalid ROM encounter slot: " + species + "/" + min + "/" + max);
        csv.append(region).append(',').append(h.id() >>> 8).append(',').append(h.id() & 255).append(',')
                .append((region == 2 ? 142000 : 144000) + h.name()).append(',').append(kind).append(',')
                .append(season).append(',').append(time).append(',').append(rate).append(',').append(cave ? 1 : 0).append(',')
                .append(species).append(',').append(min).append(',').append(max).append(',').append(weight).append('\n');
    }
}
