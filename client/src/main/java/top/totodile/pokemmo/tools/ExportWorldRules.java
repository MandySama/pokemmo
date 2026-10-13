package top.totodile.pokemmo.tools;

import top.totodile.pokemmo.rom.FireRedAtlas;

import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/** 将卡带中的通行与事件数据导出为独立服务端资源，不写入图像或调色板。 */
public final class ExportWorldRules {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Usage: ExportWorldRules <FireRed ROM> <output.gz>");
        var atlas = new FireRedAtlas(Path.of(args[0]));
        try (var data = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(Path.of(args[1]))))) {
            data.writeInt(0x46525731);
            data.writeInt(atlas.areas().size());
            for (var area : atlas.areas()) {
                var map = atlas.load(area.key(), true);
                data.writeByte(area.key().group());
                data.writeByte(area.key().map());
                data.writeShort(area.columns());
                data.writeShort(area.rows());
                for (int y = 0; y < area.rows(); y++) {
                    for (int x = 0; x < area.columns(); x++) {
                        data.writeBoolean(map.walkable(x, y));
                        data.writeByte(map.elevation(x, y));
                        data.writeByte(map.behavior(x, y));
                    }
                }
                var npcs = atlas.npcs(area.key()).stream().filter(npc -> npc.flag() == 0 && npc.hasStaticGraphics()).toList();
                data.writeShort(npcs.size());
                for (var npc : npcs) {
                    data.writeShort(npc.x());
                    data.writeShort(npc.y());
                }
                data.writeShort(area.connections().size());
                for (var connection : area.connections()) {
                    data.writeByte(connection.direction());
                    data.writeInt(connection.offset());
                    data.writeByte(connection.target().group());
                    data.writeByte(connection.target().map());
                }
                data.writeShort(area.warps().size());
                for (var warp : area.warps()) {
                    data.writeShort(warp.x());
                    data.writeShort(warp.y());
                    data.writeByte(warp.targetWarp());
                    data.writeByte(warp.target().group());
                    data.writeByte(warp.target().map());
                }
            }
        }
        System.out.println("Exported " + atlas.areas().size() + " maps");
    }
}
