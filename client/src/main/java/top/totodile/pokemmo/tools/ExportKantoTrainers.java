package top.totodile.pokemmo.tools;

import top.totodile.pokemmo.rom.*;
import java.nio.file.*;
import java.util.*;

/** 从实际卡带导出静态NPC单人挑战及队伍，服务端不读取ROM。 */
public final class ExportKantoTrainers {
    public static void main(String[] args) throws Exception {
        if(args.length!=2)throw new IllegalArgumentException("ExportKantoTrainers <ROM> <csv>");
        var atlas=new FireRedAtlas(Path.of(args[0]));var lines=new ArrayList<String>();
        lines.add("# group,map,npc,trainer,name,intro,defeat,after; species:level:iv:item:move/pp+...");
        int count=0, mons=0;
        for(int id=1;id<743;id++){var trainer=atlas.trainer(id);mons+=trainer.party().size();}
        for(var area:atlas.areas())for(var npc:atlas.npcs(area.key())) {
            if(npc.flag()!=0||!npc.hasStaticGraphics())continue;
            var battle=atlas.npcDialogueDetails(npc,0).stream().map(FireRedDialogue::battle).filter(Objects::nonNull).findFirst().orElse(null);
            if(battle==null||!Set.of(0,1,2,5).contains(battle.type()))continue;
            var trainer=atlas.trainer(battle.id());if(trainer.doubles())continue;
            var text=atlas.npcDialogueDetails(npc,0).stream().filter(d->d.battle()!=null).findFirst().orElseThrow();
            StringBuilder row=new StringBuilder(area.key().group()+","+area.key().map()+","+npc.id()+","+battle.id()+","+trainer.name()+","+text.textId()+","+battle.defeatText()+","+battle.afterText());
            for(var mon:trainer.party()) {
                row.append(';').append(mon.species()).append(':').append(mon.level()).append(':').append(mon.iv()).append(':').append(mon.heldItem()).append(':');
                row.append(mon.moves().stream().map(m->m.id()+"/"+m.pp()).collect(java.util.stream.Collectors.joining("+"))).append(':').append(mon.rawHeldItem());
            }
            lines.add(row.toString());count++;
        }
        Files.write(Path.of(args[1]),lines);System.out.println("Decoded 742 trainer parties / "+mons+" Pokemon; exported "+count+" static single NPCs");
    }
}
