package top.totodile.pokemmo.avatar;

import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;

/** 外观包保留原始 PNG 与帧引用；按需解码，不为未选择的服饰分配纹理。 */
public final class AvatarAssets {
    public record Color(int id, int red, int green, int blue, int alpha) {
        public int swatch() {
            float amount = (alpha / 2) / 255f;
            return 0xff000000 | ((int) (red * amount + 255 * (1 - amount)) << 16)
                    | ((int) (green * amount + 255 * (1 - amount)) << 8)
                    | (int) (blue * amount + 255 * (1 - amount));
        }
    }

    private record Layer(int id, int offset, int length, int source, int dx, int dy) {
    }

    public static final class Item {
        public final int category;
        public final int id;
        public final int flags;
        private final String fallback;
        private final short[][] hiddenHair;
        private final List<List<Layer>>[] views;

        private Item(int category, int id, String fallback, int flags, short[][] hiddenHair,
                     List<List<Layer>>[] views) {
            this.category = category;
            this.id = id;
            this.fallback = fallback;
            this.flags = flags;
            this.hiddenHair = hiddenHair;
            this.views = views;
        }

        public boolean colorable() {
            return (flags & 16) != 0;
        }

        @Override
        public String toString() {
            if (id == -1) return ClientStrings.get(2803);
            try {
                return ClientStrings.get(20000 + category * 1000 + id);
            } catch (IllegalArgumentException exception) {
                return fallback;
            }
        }
    }

    private final byte[] bytes;
    private final List<Item> items = new ArrayList<>();
    private final List<Item> bodies = new ArrayList<>();
    private final List<Color> colors = new ArrayList<>();
    private final Map<Integer, BufferedImage> decoded = new LinkedHashMap<>(128, .75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, BufferedImage> entry) {
            return size() > 256;
        }
    };

    public AvatarAssets(Path path) throws IOException {
        bytes = Files.readAllBytes(path);
        try {
            if (bytes.length < 32 || bytes.length > 64 * 1024 * 1024) throw new IOException("Invalid avatar pack size");
            byte[] expected = Arrays.copyOfRange(bytes, bytes.length - 20, bytes.length);
            byte[] actual = MessageDigest.getInstance("SHA-1").digest(Arrays.copyOf(bytes, bytes.length - 20));
            if (!Arrays.equals(expected, actual)) throw new IOException("Avatar pack checksum mismatch");
            ByteBuffer input = ByteBuffer.wrap(bytes, 0, bytes.length - 20).order(ByteOrder.LITTLE_ENDIAN);
            if (input.get() != 7) throw new IOException("Invalid avatar pack header");
            byte[] signature = new byte[7];
            input.get(signature);
            if (!Arrays.equals(signature, "POKEMMO".getBytes(StandardCharsets.US_ASCII))) {
                throw new IOException("Invalid avatar pack signature");
            }
            input.position(10);
            int bodyCount = u8(input);
            for (int i = 0; i < bodyCount; i++) {
                List<List<Layer>>[] views = views();
                for (int view = 0; view < 3; view++) {
                    int frames = u8(input);
                    for (int frame = 0; frame < frames; frame++) {
                        int length = input.getInt();
                        int offset = input.position();
                        skip(input, length);
                        views[view].add(List.of(new Layer(0, offset, length, -1, 0, 0)));
                    }
                }
                bodies.add(new Item(-1, i, "", 0, new short[0][], views));
            }
            int count = input.getInt();
            if (count < 0 || count > 10000) throw new IOException("Invalid avatar item count");
            for (int n = 0; n < count; n++) {
                int id = input.getShort() & 0xffff;
                int category = u8(input);
                byte[] text = new byte[u8(input)];
                input.get(text);
                String name = new String(text, StandardCharsets.UTF_8).trim();
                int flags = input.getInt() & -8417;
                short[][] masks = new short[u8(input)][];
                for (int i = 0; i < masks.length; i++) {
                    masks[i] = new short[input.getShort() & 0xffff];
                    for (int j = 0; j < masks[i].length; j++) masks[i][j] = input.getShort();
                }
                int variants = u8(input);
                int tracks = u8(input);
                for (int i = 0; i < tracks; i++) {
                    skip(input, 3);
                    skip(input, u8(input));
                }
                List<List<Layer>>[] views = views();
                for (int variant = 0; variant < variants; variant++) {
                    for (int view = 0; view < 3; view++) {
                        int frames = u8(input);
                        for (int frame = 0; frame < frames; frame++) {
                            int layers = u8(input);
                            List<Layer> entries = new ArrayList<>();
                            for (int layer = 0; layer < layers; layer++) {
                                boolean reference = view == 0 && u8(input) != 0;
                                int layerId = u8(input);
                                if (layerId > 8) throw new IOException("Invalid avatar layer");
                                if (reference) {
                                    int source = input.getInt();
                                    entries.add(new Layer(layerId, 0, 0, source, input.getInt(), input.getInt()));
                                } else {
                                    int length = input.getInt();
                                    int offset = input.position();
                                    skip(input, length);
                                    entries.add(new Layer(layerId, offset, length, -1, 0, 0));
                                }
                            }
                            if (variant == 0) views[view].add(List.copyOf(entries));
                        }
                    }
                }
                if (category >= 12) throw new IOException("Invalid avatar category");
                if (!name.equals("UNUSED")) items.add(new Item(category, id, name, flags, masks, views));
            }
            if (input.hasRemaining()) throw new IOException("Trailing avatar pack data");
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            try (var stream = Files.newInputStream(ClientFiles.data("sprites/avatar-palette.xml"))) {
                var entries = factory.newDocumentBuilder().parse(stream).getElementsByTagName("color");
                for (int i = 0; i < entries.getLength(); i++) {
                    var entry = (org.w3c.dom.Element) entries.item(i);
                    colors.add(new Color(Integer.parseInt(entry.getAttribute("id")),
                            Integer.parseInt(entry.getAttribute("r")), Integer.parseInt(entry.getAttribute("g")),
                            Integer.parseInt(entry.getAttribute("b")), Integer.parseInt(entry.getAttribute("alpha"))));
                }
            }
            if (bodies.size() != 5 || colors.size() != 48) throw new IOException("Incomplete avatar resources");
        } catch (IOException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IOException("Invalid avatar resource", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<List<Layer>>[] views() {
        return new List[]{new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
    }

    private static int u8(ByteBuffer input) {
        return input.get() & 0xff;
    }

    private static void skip(ByteBuffer input, int length) throws IOException {
        if (length < 0 || length > input.remaining()) throw new IOException("Avatar section outside pack");
        input.position(input.position() + length);
    }

    public List<Color> colors() {
        return List.copyOf(colors);
    }

    public List<Item> choices(int category, int gender) {
        return choices(category, gender, false);
    }

    public List<Item> choices(int category, int gender, boolean wardrobe) {
        List<Item> result = new ArrayList<>();
        if (category == 1 || category == 4 || category == 5 || category == 7) {
            result.add(new Item(category, -1, "", 0, new short[0][], views()));
        }
        for (Item item : items) {
            int flags = item.flags;
            int requiredGender = (flags & 1) != 0 ? 0 : (flags & 2) != 0 ? 1 : -1;
            if (item.category == category && (requiredGender == -1 || requiredGender == gender)
                    && (wardrobe || (flags & 4) == 0) && (flags & 1024) == 0 && (category != 3 || item.id >= 2)) {
                result.add(item);
            }
        }
        result.sort(Comparator.comparing((Item item) -> item.id != -1).thenComparing(Item::toString));
        return List.copyOf(result);
    }

    public BufferedImage render(int skin, Item[] selected, int[] palette, int view, int frame) throws IOException {
        return render(skin, selected, palette, view, frame, -1);
    }

    /** 大世界服饰按朝向和上衣遮蔽标记调整顺序。 */
    public BufferedImage renderOverworld(int skin, Item[] selected, int[] palette, int frame, int direction) throws IOException {
        return render(skin, selected, palette, 0, frame, direction);
    }

    private BufferedImage render(int skin, Item[] selected, int[] palette, int view, int frame, int direction) throws IOException {
        BufferedImage body = layer(bodies.get(skin), view, frame, 0, 0);
        int width = body == null ? 57 : body.getWidth();
        int height = body == null ? 56 : body.getHeight();
        for (Item item : selected) {
            if (item == null) continue;
            for (int id = 0; id < 9; id++) {
                BufferedImage image = layer(item, view, frame, id, 0);
                if (image != null) {
                    width = Math.max(width, image.getWidth());
                    height = Math.max(height, image.getHeight());
                }
            }
        }
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        int[] categories = {10, 11, 8, 9, 7, 6, 5, 4, 3, 2, 1};
        if (direction >= 0) {
            boolean glovesBehind = selected[7] != null && selected[7].id == 1 || direction == 1;
            boolean backInFront = direction == 1 && selected[5] != null && (selected[5].flags & 256) != 0;
            boolean effectInFront = direction == 1 && selected[11] != null && (selected[11].flags & 256) != 0;
            List<Integer> order = new ArrayList<>();
            order.add(10);
            if (!effectInFront) order.add(11);
            Item shirt = selected[6];
            if (shirt == null || (shirt.flags & 4096) == 0) order.add(8);
            if (shirt == null || (shirt.flags & 2048) == 0) order.add(9);
            if (glovesBehind) order.add(7);
            order.add(6);
            if (!backInFront) order.add(5);
            order.addAll(List.of(4, 3, 2, 1));
            if (!glovesBehind) order.add(7);
            if (backInFront) order.add(5);
            if (effectInFront) order.add(11);
            categories = order.stream().mapToInt(Integer::intValue).toArray();
        }
        int[] groups = {3, 0, 6};
        Item hat = selected[1];
        Item hair = selected[2];
        if (hair != null && (hair.flags & 65536) != 0) hat = null;
        for (int group : groups) {
            if (group == 0 && body != null) graphics.drawImage(body, 0, 0, null);
            for (int category : categories) {
                Item item = category == 1 ? hat : selected[category];
                if (item == null) continue;
                if (category == 2 && hat != null && view < hat.hiddenHair.length && hair != null) {
                    for (short id : hat.hiddenHair[view]) {
                        if (id == hair.id) {
                            item = find(2, 0);
                            break;
                        }
                    }
                }
                for (int id : new int[]{group, group + 2, group + 1}) {
                    BufferedImage source = layer(item, view, frame, id, 0);
                    if (source == null) continue;
                    BufferedImage mask = category == 2 && hat != null ? layer(hat, view, frame, 0, 0) : null;
                    Color color = item.colorable() && id % 3 != 2 ? colors.get(palette[category]) : null;
                    graphics.drawImage(tint(source, color, mask), 0, 0, null);
                }
            }
        }
        graphics.dispose();
        return result;
    }

    private Item find(int category, int id) {
        return items.stream().filter(item -> item.category == category && item.id == id).findFirst().orElse(null);
    }

    private BufferedImage layer(Item item, int view, int frame, int id, int depth) throws IOException {
        if (item == null || frame < 0 || frame >= item.views[view].size()) return null;
        if (depth > 32) throw new IOException("Cyclic avatar frame reference");
        for (Layer entry : item.views[view].get(frame)) {
            if (entry.id != id) continue;
            if (entry.source >= 0) {
                BufferedImage source = layer(item, view, entry.source, id, depth + 1);
                if (source == null) return null;
                BufferedImage shifted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = shifted.createGraphics();
                graphics.drawImage(source, entry.dx, entry.dy, null);
                graphics.dispose();
                return shifted;
            }
            if (entry.length == 0) return null;
            BufferedImage image = decoded.get(entry.offset);
            if (image == null) {
                image = ImageIO.read(new ByteArrayInputStream(bytes, entry.offset, entry.length));
                if (image == null) throw new IOException("Invalid avatar PNG");
                decoded.put(entry.offset, image);
            }
            return image;
        }
        return null;
    }

    private static BufferedImage tint(BufferedImage source, Color color, BufferedImage mask) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                if (argb == 0xffff1493 || mask != null && x < mask.getWidth() && y < mask.getHeight()
                        && mask.getRGB(x, y) == 0xffff1493) continue;
                if (color != null) {
                    int red = (argb >>> 16) & 255;
                    int green = (argb >>> 8) & 255;
                    int blue = argb & 255;
                    red = shade(red, color.red, color.alpha);
                    green = shade(green, color.green, color.alpha);
                    blue = shade(blue, color.blue, color.alpha);
                    argb = argb & 0xff000000 | red << 16 | green << 8 | blue;
                }
                result.setRGB(x, y, argb);
            }
        }
        return result;
    }

    private static int shade(int channel, int tint, int alpha) {
        return Math.round(channel * ((255 - alpha) / 255f + tint / 255f * alpha / 255f));
    }
}
