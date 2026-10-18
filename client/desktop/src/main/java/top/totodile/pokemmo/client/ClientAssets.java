package top.totodile.pokemmo.client;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/** 定位外部资源，打包后不依赖程序启动时的工作目录。 */
public final class ClientAssets {
    private static final Path DATA_DIRECTORY = locateDataDirectory();

    private ClientAssets() {
    }

    public static String file(String relativePath) {
        Path path = DATA_DIRECTORY.resolve(relativePath).normalize();
        if (!path.startsWith(DATA_DIRECTORY) || !Files.isRegularFile(path)) {
            throw new IllegalStateException("Client asset not found: " + path);
        }
        return path.toString();
    }

    private static Path locateDataDirectory() {
        try {
            Path location = Path.of(ClientAssets.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath().normalize();
            if (Files.isRegularFile(location)) {
                // 可执行 JAR 包的 data 与 JAR 位于同一目录。
                Path siblingData = location.getParent().resolve("data");
                if (Files.isDirectory(siblingData)) {
                    return siblingData;
                }
                // EXE 包的 JAR 位于 app 目录，Gradle 发行包的 JAR 位于 lib 目录。
                return location.getParent().getParent().resolve("data");
            }
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("Cannot locate client installation", exception);
        }
        // 兼容 Gradle 的 client 工作目录，以及 IDE 的项目根目录和 desktop 目录。
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        for (String relative : new String[]{"data", "client/data", "../data"}) {
            Path data = workingDirectory.resolve(relative).normalize();
            if (Files.isDirectory(data)) {
                return data;
            }
        }
        throw new IllegalStateException("Cannot locate client data directory from " + workingDirectory);
    }
}
