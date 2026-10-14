package top.totodile.pokemmo.tools;

import java.nio.file.Path;

/** 导出白金地图的无图像通行目录。 */
public final class ExportSinnohRules {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("ROM output-directory");
        ExportUnovaRules.export(Path.of(args[0]), Path.of(args[1]), 3);
    }
}
