package top.totodile.pokemmo.rom;

import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.protocol.PartyMember;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** 外部形态动画保留帧延时，正背面共用缩放和脚底锚点。 */
public final class PokemonForms {
    private static final String[] DIRECTORIES = {"", "megax", "megay", "gmax"};

    private PokemonForms() { }

    public static boolean active(PartyMember member) {
        return member.individual() != null && member.individual().form() != 0
                && top.totodile.pokemmo.protocol.PokemonFormsCatalog.supports(member.species(), member.individual().form());
    }

    public static PokemonAnimation.SummaryFrames load(PartyMember member) throws IOException {
        if (!active(member)) throw new IllegalArgumentException("No external Pokemon form");
        String root = member.species() == 6 && member.individual().form() == 3
                ? "resources/pokemon-forms/" : "resources/pokemon-forms-mod/";
        String path = root + member.species() + "/" + DIRECTORIES[member.individual().form()] + "/";
        var front = images(path, "front", member.shiny());
        var back = images(path, "back", member.shiny());
        int[] f = bounds(front), b = bounds(back);
        int width = Math.max(f[2] - f[0], b[2] - b[0]);
        int height = Math.max(f[3] - f[1], b[3] - b[1]);
        double scale = Math.min(1, Math.min(120.0 / width, 96.0 / height));
        int w = (int) Math.ceil(width * scale) + 4, h = (int) Math.ceil(height * scale) + 4;
        return new PokemonAnimation.SummaryFrames(compose(front, f, scale, w, h), compose(back, b, scale, w, h),
                w, h, w / 2, h - 2);
    }

    private static List<BufferedImage> images(String path, String side, boolean shiny) throws IOException {
        for (String color : shiny ? new String[]{"s", "n"} : new String[]{"n"}) {
            for (String extension : new String[]{"gif", "png"}) {
                var file = ClientFiles.data(path + side + "-" + color + "." + extension);
                if (java.nio.file.Files.isRegularFile(file)) return ExtendedPokemonSprites.load(file, 0).images();
            }
        }
        throw new IOException("Missing Pokemon form sprite " + path + side);
    }

    private static int[] bounds(List<BufferedImage> frames) {
        int[] box = {Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0};
        for (var image : frames) for (int y = 0; y < image.getHeight(); y++)
            for (int x = 0; x < image.getWidth(); x++) if ((image.getRGB(x, y) >>> 24) != 0) {
                box[0] = Math.min(box[0], x); box[1] = Math.min(box[1], y);
                box[2] = Math.max(box[2], x + 1); box[3] = Math.max(box[3], y + 1);
            }
        return box;
    }

    private static List<BufferedImage> compose(List<BufferedImage> frames, int[] box, double scale, int w, int h) {
        Map<BufferedImage, BufferedImage> composed = new IdentityHashMap<>();
        List<BufferedImage> result = new ArrayList<>();
        int width = Math.max(1, (int) Math.round((box[2] - box[0]) * scale));
        int height = Math.max(1, (int) Math.round((box[3] - box[1]) * scale));
        for (var frame : frames) result.add(composed.computeIfAbsent(frame, image -> {
            var target = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            var graphics = target.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(image, (w - width) / 2, h - 2 - height, (w - width) / 2 + width, h - 2,
                    box[0], box[1], box[2], box[3], null);
            graphics.dispose();
            return target;
        }));
        return List.copyOf(result);
    }
}
