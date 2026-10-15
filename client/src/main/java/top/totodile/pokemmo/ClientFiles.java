package top.totodile.pokemmo;

import java.nio.file.Path;

/** 从外部 data 目录读取可替换资源。 */
public final class ClientFiles {
    private ClientFiles() {
    }

    /** 默认从启动工作目录读取；也支持在 Maven client 模块目录内启动。 */
    public static Path data(String relativePath) {
        return top.totodile.pokemmo.rom.MapFiles.data(relativePath);
    }
}
