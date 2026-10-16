package top.totodile.pokemmo.rom;

import top.totodile.pokemmo.ClientFiles;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.ArrayDeque;
import javax.imageio.ImageIO;

/** 官方静态图表按需落盘；同一图表的下载合并，网络及图像解码仅由后台调用。 */
final class FusionSpriteSheets {
    private static final String SOURCE = "https://infinitefusion.net/customsprites/spritesheets/spritesheets_custom/";
    private static final HttpClient HTTP = httpClient();
    private static final IconImageCache<IconImageCache.Source> SHEETS = new IconImageCache<>(2, 48L * 1024 * 1024);
    private static final ArrayDeque<Long> REQUEST_TIMES = new ArrayDeque<>();
    private static final int CELL = 96, COLUMNS = 20;

    private FusionSpriteSheets() { }

    private static HttpClient httpClient() {
        var builder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).followRedirects(HttpClient.Redirect.NORMAL);
        String proxy = System.getenv("HTTPS_PROXY");
        if (proxy == null || proxy.isBlank()) proxy = System.getenv("HTTP_PROXY");
        if (proxy != null && !proxy.isBlank()) {
            try {
                var uri = URI.create(proxy);
                if ("http".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null)
                    builder.proxy(java.net.ProxySelector.of(new java.net.InetSocketAddress(uri.getHost(), uri.getPort() < 0 ? 80 : uri.getPort())));
            } catch (IllegalArgumentException invalid) {
                System.getLogger(FusionSpriteSheets.class.getName()).log(System.Logger.Level.WARNING, "Invalid HTTPS proxy configuration");
            }
        }
        return builder.build();
    }

    static Path path(String id) {
        int dot = id.indexOf('.');
        String head = id.substring(0, dot), suffix = id.substring(dot + 1).replaceAll("[0-9]", "");
        return ClientFiles.data("cosmetics/pokemon/sheets/" + head + "/" + head + suffix + ".png");
    }

    static BufferedImage image(String id) throws IOException {
        var path = path(id);
        if (!Files.exists(path)) path = ensure(id);
        final Path pixels = path;
        var sheet = SHEETS.get(IconImageCache.Source.of(path), () -> decode(pixels));
        int dot = id.indexOf('.');
        int body = Integer.parseInt(id.substring(dot + 1).replaceAll("[a-z]", ""));
        int x = body % COLUMNS * CELL, y = body / COLUMNS * CELL;
        if (x + CELL > sheet.getWidth() || y + CELL > sheet.getHeight()) throw new IOException("Fusion outside sprite sheet: " + id);
        return sheet.getSubimage(x, y, CELL, CELL);
    }

    private static BufferedImage decode(Path path) throws IOException {
        var image = ImageIO.read(path.toFile());
        if (image == null || image.getWidth() != COLUMNS * CELL || image.getHeight() < CELL || image.getHeight() > 8192)
            throw new IOException("Invalid fusion sprite sheet");
        return image;
    }

    private static synchronized Path ensure(String id) throws IOException {
        Path target = path(id);
        if (Files.exists(target)) return target;
        // 沿用源项目的每分钟最多十五次图表请求限制，等待不会占用 GUI 线程。
        while (true) {
            long now = System.nanoTime();
            while (!REQUEST_TIMES.isEmpty() && now - REQUEST_TIMES.getFirst() >= 60_000_000_000L) REQUEST_TIMES.removeFirst();
            if (REQUEST_TIMES.size() < 15) { REQUEST_TIMES.addLast(now); break; }
            try { FusionSpriteSheets.class.wait(Math.max(1, (60_000_000_000L - (now - REQUEST_TIMES.getFirst())) / 1_000_000L)); }
            catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new IOException("Fusion download interrupted", error); }
        }
        String head = target.getParent().getFileName().toString(), file = target.getFileName().toString();
        var request = HttpRequest.newBuilder(URI.create(SOURCE + head + "/" + file)).timeout(Duration.ofSeconds(45))
                .header("User-Agent", "Pokemon-cosmetics-cache/1.0").GET().build();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "fusion-", ".tmp");
        try {
            HttpResponse<byte[]> response;
            try { response = HTTP.send(request, HttpResponse.BodyHandlers.ofByteArray()); }
            catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new IOException("Fusion download interrupted", error); }
            if (response.statusCode() != 200) throw new IOException("Fusion HTTP status " + response.statusCode());
            if (response.body().length > 16 * 1024 * 1024) throw new IOException("Fusion sprite sheet too large");
            Files.write(temporary, response.body());
            decode(temporary);
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException unsupported) { Files.move(temporary, target); }
            return target;
        } finally { Files.deleteIfExists(temporary); }
    }
}
