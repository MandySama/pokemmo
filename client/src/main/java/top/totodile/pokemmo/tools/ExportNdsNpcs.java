package top.totodile.pokemmo.tools;

import top.totodile.pokemmo.protocol.BlackWhiteWorldRules;
import top.totodile.pokemmo.rom.NdsNpcData;
import top.totodile.pokemmo.rom.NdsNpcSprites;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.zip.GZIPOutputStream;

/** 从各地区 ROM 生成服务端可用的普通 NPC 目录。 */
public final class ExportNdsNpcs {
    public static void main(String[] args) throws Exception {
        int region = Integer.parseInt(args[0]);
        var rules = new BlackWhiteWorldRules(region);
        var sprites = new NdsNpcSprites(Path.of(args[1]), region);
        var graphics = new java.util.HashMap<Integer, Boolean>();
        var all = NdsNpcData.read(Path.of(args[1]), region);
        int kept = 0, dialogs = 0, hidden = 0, invalid = 0;
        Path target = Path.of(args[2]);
        Files.createDirectories(target.getParent());
        try (var writer =
                new BufferedWriter(
                        new OutputStreamWriter(
                                new GZIPOutputStream(Files.newOutputStream(target)),
                                StandardCharsets.UTF_8))) {
            for (var npc : all) {
                if (npc.hide() != 0) {
                    hidden++;
                    continue;
                }
                boolean renderable =
                        graphics.computeIfAbsent(
                                npc.graphics(),
                                id -> {
                                    try {
                                        return sprites.image(id, 0, 0) != null;
                                    } catch (IOException error) {
                                        return false;
                                    }
                                });
                if (!renderable) {
                    invalid++;
                    continue;
                }
                var h = rules.header(npc.header());
                var matrix = rules.matrix(npc.header());
                if (npc.x() >= h.tileWidth() * matrix.width()
                        || npc.y() >= h.tileHeight() * matrix.height()) {
                    invalid++;
                    continue;
                }
                int layer = 0;
                float delta = Float.POSITIVE_INFINITY;
                for (int n = 0; n < 8; n++)
                    try {
                        if (rules.walkable(npc.header(), npc.x(), npc.y(), n)) {
                            float d =
                                    Math.abs(
                                            rules.height(npc.header(), npc.x(), npc.y(), n)
                                                    - npc.elevation() / 4096f);
                            if (d < delta) {
                                delta = d;
                                layer = n;
                            }
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                writer.write(
                        npc.header()
                                + ","
                                + npc.id()
                                + ","
                                + npc.graphics()
                                + ","
                                + npc.x()
                                + ","
                                + npc.y()
                                + ","
                                + layer
                                + ","
                                + npc.facing()
                                + ","
                                + npc.movement()
                                + ","
                                + npc.rangeX()
                                + ","
                                + npc.rangeY()
                                + ","
                                + npc.text()
                                + "\n");
                kept++;
                if (npc.text() > 0) dialogs++;
            }
        }
        System.out.println(
                "region="
                        + region
                        + " raw="
                        + all.size()
                        + " visible="
                        + kept
                        + " dialogues="
                        + dialogs
                        + " hidden="
                        + hidden
                        + " invalid="
                        + invalid);
    }
}
