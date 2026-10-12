package top.totodile.pokemmo;

import java.nio.file.Files;
import java.nio.file.Path;

/** 从外部 data 目录读取可替换资源。 */
public final class ClientFiles {
    private ClientFiles() {
    }

    /** 默认从启动工作目录读取；也支持在 Maven client 模块目录内启动。 */
    public static Path data(String relativePath) {
        Path root = Path.of("data");
        if (!Files.isDirectory(root) && Path.of("").toAbsolutePath().getFileName().toString().equals("client")) {
            root = Path.of("..", "data");
        }
        return root.resolve(relativePath);
    }
}
