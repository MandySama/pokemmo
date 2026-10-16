package top.totodile.pokemmo.tools;

import top.totodile.pokemmo.rom.FireRedAtlas;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/** 导出开场地图事件，及执行目录中 trainerbattle 指令所引用的队伍。 */
public final class ExportGbaScriptData {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            throw new IllegalArgumentException("Usage: ExportGbaScriptData <ROM> <events|trainers> <output>");
        }
        var atlas = new FireRedAtlas(Path.of(args[0]));
        var lines = new ArrayList<String>();
        if (args[1].equals("events")) {
            for (var key : List.of(new FireRedAtlas.Key(3, 0), new FireRedAtlas.Key(4, 3))) {
                for (var n : atlas.npcs(key)) {
                    lines.add("npc," + key.group() + "," + key.map() + "," + n.id() + "," + n.graphics()
                            + "," + n.x() + "," + n.y() + "," + n.elevation() + "," + n.direction()
                            + "," + n.flag() + "," + n.script());
                }
                var events = atlas.storyEvents(key);
                for (var c : events.coordinates()) {
                    lines.add("coord," + key.group() + "," + key.map() + "," + c.x() + "," + c.y()
                            + "," + c.elevation() + "," + c.variable() + "," + c.value() + "," + c.script());
                }
                for (var t : events.scripts()) {
                    lines.add("map," + key.group() + "," + key.map() + "," + t.type()
                            + "," + t.variable() + "," + t.value() + "," + t.script());
                }
            }
        } else if (args[1].equals("trainers")) {
            var ids = new TreeSet<Integer>();
            try (var stream = new GZIPInputStream(Files.newInputStream(
                    Path.of("protocol/src/main/resources/maps/gba-opening-program.txt.gz")))) {
                for (var line : new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).split("\\R")) {
                    if (!line.startsWith("code,")) continue;
                    var bytes = HexFormat.of().parseHex(line.split(",")[2]);
                    if ((bytes[0] & 255) == 0x5c) ids.add((bytes[2] & 255) | (bytes[3] & 255) << 8);
                }
            }
            for (int id : ids) {
                var trainer = atlas.trainer(id);
                var row = new StringBuilder("4,3,8," + id + "," + trainer.name() + ",0,0,0");
                for (var m : trainer.party()) {
                    row.append(';').append(m.species()).append(':').append(m.level()).append(':')
                            .append(m.iv()).append(':').append(m.heldItem()).append(':')
                            .append(m.moves().stream().map(v -> v.id() + "/" + v.pp())
                                    .collect(java.util.stream.Collectors.joining("+")))
                            .append(':').append(m.rawHeldItem());
                }
                lines.add(row.toString());
            }
        } else {
            throw new IllegalArgumentException("Unknown export mode");
        }
        Files.write(Path.of(args[2]), lines);
    }
}
