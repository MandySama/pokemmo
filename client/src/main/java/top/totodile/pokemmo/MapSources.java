package top.totodile.pokemmo;

import top.totodile.pokemmo.rom.FireRedAtlas;
import java.io.IOException;
import java.nio.file.Path;

/** 将客户端配置转换为公共地图解析器的输入。 */
public final class MapSources {
    private MapSources() {
    }

    public static FireRedAtlas gba(Path rom, boolean modified) throws IOException {
        boolean replacements = new ClientSettings().hgssSprites();
        Path replacementRom = replacements && !modified
                ? new RomFiles(ClientFiles.data("../roms")).selected(4) : null;
        return new FireRedAtlas(rom, modified, replacementRom, replacements);
    }
}
