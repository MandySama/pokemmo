package top.totodile.pokemmo.tools;

import top.totodile.pokemmo.rom.NdsWildEncounters;
import java.nio.file.Path;

/** 导出 NDS 卡带的遭遇槽和地形行为，服务端无需访问 ROM。 */
public final class ExportWildEncounters {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("region ROM output-directory");
        NdsWildEncounters.export(Path.of(args[1]), Path.of(args[2]), Integer.parseInt(args[0]));
    }
}
