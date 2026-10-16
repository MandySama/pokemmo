package top.totodile.pokemmo.tools;

import top.totodile.pokemmo.rom.FireRedAtlas;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeSet;

/** 从卡带导出当前无隐藏标记的固定图形 NPC，及只读对白引用清单。 */
public final class ExportGbaNpcs {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("ExportGbaNpcs <ROM> <actors.csv> <texts.txt>");
        var atlas = new FireRedAtlas(Path.of(args[0]));
        var actors = new ArrayList<String>();
        var texts = new TreeSet<Integer>();
        actors.add("# group,map,id,x,y,elevation,movement,rangeX,rangeY");
        int talking = 0;
        for (var area : atlas.areas()) for (var npc : atlas.npcs(area.key())) {
            if (npc.flag() != 0 || !npc.hasStaticGraphics()) continue;
            actors.add(area.key().group() + "," + area.key().map() + "," + npc.id() + "," + npc.x()
                    + "," + npc.y() + "," + npc.elevation() + "," + npc.movement()
                    + "," + npc.rangeX() + "," + npc.rangeY());
            boolean found = false;
            for (int gender = 0; gender < 2; gender++) for (var text : atlas.npcDialogueDetails(npc, gender)) {
                texts.add(text.textId());
                found = true;
            }
            if (found) talking++;
        }
        Files.write(Path.of(args[1]), actors);
        Files.write(Path.of(args[2]), texts.stream().map(String::valueOf).toList());
        System.out.println("Exported " + (actors.size() - 1) + " NPCs; " + talking
                + " with readable dialogue; " + texts.size() + " text IDs");
    }
}
