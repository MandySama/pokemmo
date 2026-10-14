package top.totodile.pokemmo.rom;

import org.w3c.dom.Element;
import top.totodile.pokemmo.ClientFiles;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 火红地图修正、季节颜色、动画描述与夜间贴图的公共规则。 */
final class FireRedRules {
    final Map<Integer, Map<Integer, Integer>> patches = new HashMap<>();
    final Map<ColorKey, Integer> colors = new HashMap<>();
    final List<Animation> animations = new ArrayList<>();
    final Map<Integer, Overlay> overlays = new HashMap<>();
    final int intervalMillis;
    final float redGreenDivisor;
    final float blueDivisor;

    FireRedRules() throws IOException { this(false); }

    FireRedRules(boolean emerald) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setExpandEntityReferences(false);
            Element root;
            try (var input = Files.newInputStream(ClientFiles.data(emerald ? "maps/emerald.xml" : "maps/fire-red.xml"))) {
                root = factory.newDocumentBuilder().parse(input).getDocumentElement();
            }
            var cells = root.getElementsByTagName("cell");
            for (int i = 0; i < cells.getLength(); i++) {
                Element cell = (Element) cells.item(i);
                patches.computeIfAbsent(number(cell, "layout"), ignored -> new HashMap<>())
                        .put(number(cell, "index"), number(cell, "value"));
            }
            var entries = root.getElementsByTagName("color");
            for (int i = 0; i < entries.getLength(); i++) {
                Element color = (Element) entries.item(i);
                colors.put(new ColorKey(number(color, "season"), number(color, "tileset"),
                                number(color, "palette"), number(color, "index")),
                        Integer.parseUnsignedInt(color.getAttribute("rgba"), 16));
            }
            var tiles = root.getElementsByTagName("tiles");
            for (int i = 0; i < tiles.getLength(); i++) {
                Element tile = (Element) tiles.item(i);
                animations.add(new Animation(number(tile, "tileset"), number(tile, "frames"),
                        number(tile, "first"), number(tile, "count"), number(tile, "pointerOffset"), tile.hasAttribute("rotation") ? number(tile, "rotation") : 0));
            }
            intervalMillis = number((Element) root.getElementsByTagName("animations").item(0), "intervalMillis");
            Element lighting = (Element) root.getElementsByTagName("lighting").item(0);
            redGreenDivisor = Float.parseFloat(lighting.getAttribute("redGreenDivisor"));
            blueDivisor = Float.parseFloat(lighting.getAttribute("blueDivisor"));
            if (intervalMillis < 1 || redGreenDivisor < 1 || blueDivisor < 1) {
                throw new IOException("无效的地图动画或光照规则。");
            }
            if (!emerald) loadOverlays();
        } catch (IOException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IOException("无法读取火红地图绘制规则。", exception);
        }
    }

    private static int number(Element element, String name) {
        return Integer.parseInt(element.getAttribute(name));
    }

    private void loadOverlays() throws IOException {
        ByteBuffer data = ByteBuffer.wrap(Files.readAllBytes(ClientFiles.data("sprites/overlays-data.pak")))
                .order(ByteOrder.LITTLE_ENDIAN);
        if (data.getInt() != 21709136) throw new IOException("无效的夜间贴图索引。");
        int count = Short.toUnsignedInt(data.getShort());
        data.getShort();
        var image = ImageIO.read(ClientFiles.data("sprites/overlays.pak").toFile());
        if (image == null || data.remaining() < count * 9) throw new IOException("夜间贴图资源被截断。");
        for (int i = 0; i < count; i++) {
            int tileset = Short.toUnsignedInt(data.getShort());
            int tile = Short.toUnsignedInt(data.getShort());
            int layer = Byte.toUnsignedInt(data.get());
            int x = Short.toUnsignedInt(data.getShort());
            int y = Short.toUnsignedInt(data.getShort());
            if (x + 8 > image.getWidth() || y + 8 > image.getHeight()) {
                throw new IOException("夜间贴图坐标超出图集范围。");
            }
            int[] pixels = image.getRGB(x, y, 8, 8, null, 0, 8);
            for (int p = 0; p < pixels.length; p++) {
                int argb = pixels[p];
                pixels[p] = argb << 8 | argb >>> 24;
            }
            overlays.put(tile * 65536 + tileset, new Overlay(layer, pixels));
        }
    }

    /** 室内、地下和秘密基地不随时间染色；水下使用独立的蓝色乘数。 */
    int darkness(int locationType, int hour, int minute) {
        if (locationType == 4 || locationType == 5 || locationType == 8 || locationType == 9) return 0;
        int time = hour * 100 + (int) (minute * 1.65);
        if (time >= 700 && time <= 2000) return 0;
        if (time > 2000 && time < 2255) return time - 2000;
        if (time > 445 && time < 700) return 255 - (time - 445);
        return 255;
    }

    record ColorKey(int season, int tileset, int palette, int index) {
    }

    record Animation(int tileset, int frames, int first, int count, int pointerOffset, int rotation) {
    }

    record Overlay(int layer, int[] pixels) {
    }
}
