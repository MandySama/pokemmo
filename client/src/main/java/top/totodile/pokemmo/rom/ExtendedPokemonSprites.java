package top.totodile.pokemmo.rom;

import top.totodile.pokemmo.ClientFiles;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import org.w3c.dom.Node;

/** 外部种族图像按全国编号索引，GIF 的帧延时和覆盖方式在 CPU 端合成。 */
public final class ExtendedPokemonSprites {
    private ExtendedPokemonSprites() { }
    public static BufferedImage icon(int species) throws IOException {
        try (var zip = new ZipFile(ClientFiles.data("resources/pokemon-gen9.zip").toFile())) {
            return icon(zip, species);
        }
    }
    static BufferedImage icon(ZipFile zip, int species) throws IOException {
        var entry = zip.getEntry(species + "/icon.png");
        if (entry == null) throw new IOException("Missing extended icon " + species);
        try (var input = zip.getInputStream(entry)) {
            var image = ImageIO.read(input);
            if (image == null) throw new IOException("Invalid extended icon " + species);
            return image;
        }
    }

    private static Node child(Node node, String name) {
        for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) if (n.getNodeName().equals(name)) return n;
        return null;
    }
    private static String attr(Node n, String name, String fallback) {
        return n == null || n.getAttributes().getNamedItem(name) == null ? fallback : n.getAttributes().getNamedItem(name).getNodeValue();
    }
    public static PokemonAnimation.Frames load(int species, boolean shiny, boolean back, int canvas) throws IOException {
        String stem = species + "/" + (back ? "back" : "front") + (shiny ? "-s" : "-n");
        try (var zip = new ZipFile(ClientFiles.data("resources/pokemon-gen9.zip").toFile())) {
            var entry = zip.getEntry(stem + ".gif");
            if (entry == null) entry = zip.getEntry(stem + ".png");
            if (entry == null) throw new IOException("Missing extended sprite " + stem);
            try (var input = zip.getInputStream(entry)) { return load(input, canvas); }
        }
    }

    public static PokemonAnimation.Frames load(java.nio.file.Path file, int canvas) throws IOException {
        try (var input = java.nio.file.Files.newInputStream(file)) { return load(input, canvas); }
    }

    private static PokemonAnimation.Frames load(InputStream input, int canvas) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();
        try (var stream = ImageIO.createImageInputStream(input)) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IOException("Invalid external sprite");
            var reader = readers.next();
            try {
                reader.setInput(stream);
                if (!reader.getFormatName().equalsIgnoreCase("gif")) frames.add(reader.read(0));
                else {
                    Node logical = child(reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0"), "LogicalScreenDescriptor");
                    int w = Integer.parseInt(attr(logical, "logicalScreenWidth", "96")), h = Integer.parseInt(attr(logical, "logicalScreenHeight", "96"));
                    if (w > 1024 || h > 1024) throw new IOException("Extended sprite too large");
                    var image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    for (int i = 0; i < reader.getNumImages(true); i++) {
                        Node root = reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
                        Node desc = child(root, "ImageDescriptor"), control = child(root, "GraphicControlExtension");
                        int x = Integer.parseInt(attr(desc, "imageLeftPosition", "0")), y = Integer.parseInt(attr(desc, "imageTopPosition", "0"));
                        var previous = copy(image);
                        var g = image.createGraphics(); g.drawImage(reader.read(i), x, y, null); g.dispose();
                        var snapshot = copy(image);
                        int ticks = Math.max(1, Math.round(Integer.parseInt(attr(control, "delayTime", "10")) * 0.3f));
                        if (frames.size() + ticks > 4096) throw new IOException("Extended animation too long");
                        for (int j = 0; j < ticks; j++) frames.add(snapshot);
                        String disposal = attr(control, "disposalMethod", "none");
                        if (disposal.equals("restoreToPrevious")) image = previous;
                        else if (disposal.equals("restoreToBackgroundColor")) {
                            g = image.createGraphics(); g.setComposite(java.awt.AlphaComposite.Clear);
                            g.fillRect(x, y, Integer.parseInt(attr(desc, "imageWidth", "0")), Integer.parseInt(attr(desc, "imageHeight", "0"))); g.dispose();
                        }
                    }
                }
            } finally { reader.dispose(); }
        }
        int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, right = 0, bottom = 0;
        for (var image : frames) for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) if ((image.getRGB(x, y) >>> 24) != 0) {
            left = Math.min(left, x); top = Math.min(top, y); right = Math.max(right, x + 1); bottom = Math.max(bottom, y + 1);
        }
        if (right <= left || bottom <= top) throw new IOException("Empty extended sprite");
        if (canvas == 0) return new PokemonAnimation.Frames(List.copyOf(frames), 1);
        final int dx = canvas / 2 - (left + right) / 2, dy = canvas / 2 - bottom;
        Map<BufferedImage, BufferedImage> composed = new IdentityHashMap<>();
        List<BufferedImage> output = new ArrayList<>();
        for (var image : frames) output.add(composed.computeIfAbsent(image, value -> {
            var result = new BufferedImage(canvas, canvas, BufferedImage.TYPE_INT_ARGB);
            var g = result.createGraphics(); g.drawImage(value, dx, dy, null); g.dispose(); return result;
        }));
        return new PokemonAnimation.Frames(List.copyOf(output), 1);
    }
    private static BufferedImage copy(BufferedImage source) {
        var target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        var g = target.createGraphics(); g.drawImage(source, 0, 0, null); g.dispose(); return target;
    }
}
