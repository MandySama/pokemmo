package top.totodile.pokemmo.tools;

import top.totodile.pokemmo.rom.RomItems;
import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.ClientFiles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** 将 ROM 物品参数导出为客户端与游戏服务共用的小型目录，不包含图像。 */
public final class ExportItemCatalog {
    private ExportItemCatalog() { }
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: ExportItemCatalog <items.csv>");
        var items = RomItems.load(new RomFiles(ClientFiles.data("../roms"))).values();
        var output = new StringBuilder("# id,pocket,max_stack,region,name_id,icon_id,source; merged ROM and custom registry\n");
        for (var item : items) output.append(item.id()).append(',').append(item.pocket()).append(',').append(item.maxStack())
                .append(',').append(item.region()).append(',').append(item.nameId()).append(',').append(item.iconId()).append(',').append(item.source()).append('\n');
        Path destination = Path.of(args[0]);
        if (destination.toAbsolutePath().getParent() != null) Files.createDirectories(destination.toAbsolutePath().getParent());
        Files.writeString(destination, output, StandardCharsets.UTF_8);
        System.out.println("Exported " + items.size() + " item records");
    }
}
