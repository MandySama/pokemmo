package top.totodile.pokemmo.tools;

import top.totodile.pokemmo.rom.*;
import java.nio.file.*;
import java.util.*;

/** 导出坐标、地图脚本索引和首段剧情的无图像执行数据。 */
public final class ExportKantoStory {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("ExportKantoStory <ROM> <events.csv> <opening.csv>");
        var atlas = new FireRedAtlas(Path.of(args[0]));
        if (atlas.region() != 0) throw new IllegalArgumentException("Kanto ROM required");
        List<String> events = new ArrayList<>(), opening = new ArrayList<>();
        events.add("# group,map,kind,type,x,y,elevation,variable,value,scriptOffset");
        opening.add("# x,y,elevation,variable,value,script,actor,graphics,actorX,actorY,actorElevation,stopText,warningText,directions");
        for (var area : atlas.areas()) {
            var data = atlas.storyEvents(area.key());
            for (var event : data.coordinates()) {
                events.add(area.key().group() + "," + area.key().map() + ",coordinate,0," + event.x() + "," + event.y() + "," + event.elevation()
                        + "," + event.variable() + "," + event.value() + "," + event.script());
                if (area.key().equals(new FireRedAtlas.Key(3, 0)) && event.variable() == 0x4050 && event.value() == 0) {
                    var scene = atlas.opening(event);
                    var actor = atlas.npcs(area.key()).stream().filter(n -> n.id() == scene.actor()).findFirst().orElseThrow();
                    opening.add(event.x() + "," + event.y() + "," + event.elevation() + "," + event.variable() + "," + event.value()
                            + "," + event.script() + "," + actor.id() + "," + actor.graphics() + "," + actor.x() + "," + actor.y()
                            + "," + actor.elevation() + "," + scene.stopText() + "," + scene.warningText() + ","
                            + String.join("/", scene.approach().stream().map(String::valueOf).toList()));
                }
            }
            for (var script : data.scripts()) events.add(area.key().group() + "," + area.key().map() + ",map," + script.type() + ",0,0,0," + script.variable() + "," + script.value() + "," + script.script());
        }
        Files.write(Path.of(args[1]), events);
        Files.write(Path.of(args[2]), opening);
        System.out.println("Story events=" + (events.size() - 1) + ", opening triggers=" + (opening.size() - 1));
    }
}
