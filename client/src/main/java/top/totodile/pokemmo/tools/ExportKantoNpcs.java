package top.totodile.pokemmo.tools;

import top.totodile.pokemmo.rom.FireRedAtlas;

import java.nio.file.*;
import java.util.ArrayList;

/** 导出 NPC 移动类型与活动范围，不需要服务端读取卡带或图像。 */
public final class ExportKantoNpcs {
    public static void main(String[] args) throws Exception {
        if (args.length != 2)
            throw new IllegalArgumentException("Usage: ExportKantoNpcs <FireRed ROM> <output.csv>");
        var atlas = new FireRedAtlas(Path.of(args[0]));
        if (atlas.region() != 0) throw new IllegalArgumentException("Kanto cartridge required");
        var rows = new ArrayList<String>();
        rows.add("# group,map,id,x,y,elevation,movement,rangeX,rangeY");
        for (var area : atlas.areas())
            for (var npc : atlas.npcs(area.key())) {
                if (npc.flag() != 0 || !npc.hasStaticGraphics()) continue;
                rows.add(
                        area.key().group()
                                + ","
                                + area.key().map()
                                + ","
                                + npc.id()
                                + ","
                                + npc.x()
                                + ","
                                + npc.y()
                                + ","
                                + npc.elevation()
                                + ","
                                + npc.movement()
                                + ","
                                + npc.rangeX()
                                + ","
                                + npc.rangeY());
            }
        Files.write(Path.of(args[1]), rows);
        System.out.println("Exported " + (rows.size() - 1) + " NPC definitions");
    }
}
