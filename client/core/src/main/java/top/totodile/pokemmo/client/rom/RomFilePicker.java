package top.totodile.pokemmo.client.rom;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

/** 平台文件选择器；流由调用方关闭，取消选择时返回 null。 */
public interface RomFilePicker {
    interface Source {
        InputStream open() throws IOException;

        /** 可用的本地源路径，用于保留导入文件名；Android 文档流通常没有本地路径。 */
        default Path path() {
            return null;
        }

        default String name() {
            return path() == null ? null : path().getFileName().toString();
        }
    }

    void choose(String extension, Consumer<Source> selected, Consumer<String> failed);

    default void chooseMod(Consumer<Source> selected, Consumer<String> failed) {
        choose("mod,zip", selected, failed);
    }

    default void openDirectory(String path, Consumer<String> failed) {
        failed.accept("无法打开 ROM 文件夹");
    }
}
