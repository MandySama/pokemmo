package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.LinkedHashMap;

/** 只保存 CPU 像素；返回的共享图像不可修改，缓存不持有图形上下文或打开的文件。 */
final class IconImageCache<K> {
    record Source(Path path, long size, FileTime modified) {
        static Source of(Path path) throws IOException {
            var normalized = path.toAbsolutePath().normalize();
            var attributes = Files.readAttributes(normalized, java.nio.file.attribute.BasicFileAttributes.class);
            return new Source(normalized, attributes.size(), attributes.lastModifiedTime());
        }
    }
    @FunctionalInterface interface Loader { BufferedImage load() throws IOException; }
    private final int capacity;
    private final long maxBytes;
    private long bytes;
    private final LinkedHashMap<K, BufferedImage> images = new LinkedHashMap<>(16, 0.75f, true);
    IconImageCache(int capacity) { this(capacity, 24L * 1024 * 1024); }
    IconImageCache(int capacity, long maxBytes) { this.capacity = capacity; this.maxBytes = maxBytes; }

    // 同一资源的并发首载在此合并；失败不缓存，之后可重新尝试。
    synchronized BufferedImage get(K key, Loader loader) throws IOException {
        var image = images.get(key);
        if (image != null) return image;
        image = loader.load();
        if (image == null) throw new IOException("Missing icon image");
        long size = (long) image.getWidth() * image.getHeight() * 4;
        if (size > maxBytes) return image;
        images.put(key, image);
        bytes += size;
        while (images.size() > capacity || bytes > maxBytes) {
            var oldest = images.remove(images.keySet().iterator().next());
            bytes -= (long) oldest.getWidth() * oldest.getHeight() * 4;
        }
        return image;
    }
}
