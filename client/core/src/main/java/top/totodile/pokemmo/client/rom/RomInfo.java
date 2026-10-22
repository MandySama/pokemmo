package top.totodile.pokemmo.client.rom;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 已校验的 ROM 元信息与资源索引；不持有文件句柄。 */
public record RomInfo(Path path, RomType type, String title, String code,
                      int revision, long size, Map<String, RomEntry> files) {
    public RomInfo {
        files = Collections.unmodifiableMap(new LinkedHashMap<>(files));
    }
}
