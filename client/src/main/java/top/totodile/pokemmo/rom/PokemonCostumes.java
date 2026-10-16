package top.totodile.pokemmo.rom;

import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.protocol.PartyMember;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

/** 本机时装绑定个体编号，只替换已有的图像方向，不改变宝可梦数据。 */
public final class PokemonCostumes {
    public record Costume(String id, int species, int donor, int scale, String artist) {
        public Costume(String id, int species, int donor, int scale) { this(id, species, donor, scale, ""); }
    }
    private record Catalog(Map<String, Costume> ids, Map<Integer, List<Costume>> species) { }
    private static java.util.concurrent.CompletableFuture<Catalog> catalogFuture;
    private static final IconImageCache<Object> IMAGES = new IconImageCache<>(64, 8L * 1024 * 1024);
    private static final Properties SELECTED = loadSelections();
    private PokemonCostumes() { }

    private static synchronized java.util.concurrent.CompletableFuture<Catalog> catalog() {
        if (catalogFuture == null) catalogFuture = java.util.concurrent.CompletableFuture.supplyAsync(PokemonCostumes::loadCatalog);
        return catalogFuture;
    }

    public static void preloadCatalog() { catalog(); }
    public static boolean catalogReady() { return catalog().isDone() && !catalog().isCompletedExceptionally(); }
    public static boolean catalogFailed() { return catalog().isCompletedExceptionally(); }

    public static List<Costume> choices(int species) {
        return catalog().join().species().getOrDefault(species, List.of());
    }

    public static void prepare(String id) throws IOException {
        if (id.isEmpty()) return;
        var costume = catalog().join().ids().get(id);
        if (costume == null) throw new IOException("Unknown fusion costume");
        loadImage(costume, false, false);
    }

    public static synchronized String selected(PartyMember member) {
        String id = member.individual() == null ? "" : SELECTED.getProperty(Long.toString(member.individual().id()), "");
        return id.matches("[0-9]+[.][0-9]+[a-z]*") ? id : "";
    }

    public static synchronized void select(PartyMember member, String id) throws IOException {
        if (member.individual() == null || !id.isEmpty() && choices(member.species()).stream().noneMatch(value -> value.id().equals(id)))
            throw new IllegalArgumentException("Invalid Pokemon costume");
        Path path = selectionFile();
        Properties next = new Properties();
        if (Files.exists(path)) try (var input = Files.newInputStream(path)) { next.load(input); }
        String key = Long.toString(member.individual().id());
        if (id.isEmpty()) next.remove(key); else next.setProperty(key, id);
        Files.createDirectories(path.getParent());
        Path temporary = Files.createTempFile(path.getParent(), "pokemon-costumes-", ".tmp");
        try {
            try (var output = Files.newOutputStream(temporary)) { next.store(output, "Local Pokemon cosmetics"); }
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException unsupported) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
            SELECTED.clear(); SELECTED.putAll(next);
        } finally { Files.deleteIfExists(temporary); }
    }

    private static Path selectionFile() { return ClientFiles.data("../config/pokemon-costumes.properties"); }

    private static Properties loadSelections() {
        Properties result = new Properties();
        try { if (Files.exists(selectionFile())) try (var input = Files.newInputStream(selectionFile())) { result.load(input); } }
        catch (IOException error) { System.getLogger(PokemonCostumes.class.getName()).log(System.Logger.Level.WARNING, "Unable to load Pokemon costumes", error); }
        return result;
    }

    private static Catalog loadCatalog() {
        Map<String, Costume> result = new LinkedHashMap<>();
        Path fusion = ClientFiles.data("cosmetics/pokemon/fusion-catalog.tsv.gz");
        try {
            if (Files.exists(fusion)) try (var input = new java.util.zip.GZIPInputStream(Files.newInputStream(fusion));
                    var reader = new BufferedReader(new InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null;) {
                    if (line.startsWith("#") || line.isBlank()) continue;
                    var fields = line.split("\t", -1);
                    if (fields.length != 5 || result.size() >= 400000) throw new IOException("Invalid fusion catalog row");
                    var value = new Costume(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2]),
                            Integer.parseInt(fields[3]), fields[4]);
                    validate(value);
                    if (result.put(value.id(), value) != null) throw new IOException("Duplicate fusion costume");
                }
            }
            Path local = ClientFiles.data("cosmetics/pokemon/catalog.xml");
            if (Files.exists(local)) try (var input = Files.newInputStream(local)) {
                var factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                var nodes = factory.newDocumentBuilder().parse(input).getElementsByTagName("costume");
                for (int i = 0; i < nodes.getLength(); i++) {
                    var node = (Element) nodes.item(i);
                    String id = node.getAttribute("id");
                    String artist = result.containsKey(id) ? result.get(id).artist() : "";
                    var value = new Costume(id, Integer.parseInt(node.getAttribute("species")),
                            Integer.parseInt(node.getAttribute("donor")), Integer.parseInt(node.getAttribute("scale")), artist);
                    validate(value);
                    result.put(id, value);
                }
            }
            Map<Integer, List<Costume>> grouped = new HashMap<>();
            for (var value : result.values()) {
                grouped.computeIfAbsent(value.species(), ignored -> new ArrayList<>()).add(value);
                if (value.donor() != value.species()) grouped.computeIfAbsent(value.donor(), ignored -> new ArrayList<>()).add(value);
            }
            grouped.replaceAll((species, values) -> List.copyOf(values));
            return new Catalog(Map.copyOf(result), Map.copyOf(grouped));
        } catch (Exception error) { throw new IllegalStateException("Unable to load Pokemon costume catalog", error); }
    }

    private static void validate(Costume value) throws IOException {
        if (!value.id().matches("[0-9]+[.][0-9]+[a-z]*") || value.species() < 1 || value.donor() < 1
                || value.species() > top.totodile.pokemmo.protocol.PokemonCatalog.MAX_SPECIES
                || value.donor() > top.totodile.pokemmo.protocol.PokemonCatalog.MAX_SPECIES || value.scale() < 1 || value.scale() > 8)
            throw new IOException("Invalid costume catalog");
    }

    /** 资料背面仍使用独立背面图；战斗由 battleImage 统一取时装正面。 */
    public static BufferedImage image(PartyMember member, boolean back) throws IOException {
        String id = selected(member);
        if (id.isEmpty()) return null;
        var costume = catalog().join().ids().get(id);
        if (costume == null || costume.species() != member.species() && costume.donor() != member.species()) return null;
        return loadImage(costume, member.shiny(), back);
    }

    public static BufferedImage battleImage(PartyMember member) throws IOException { return image(member, false); }

    private static BufferedImage loadImage(Costume costume, boolean shiny, boolean back) throws IOException {
        Path file = ClientFiles.data("cosmetics/pokemon/" + costume.id() + "/" + (back ? "back" : "front") + (shiny ? "-s" : "") + ".png");
        if (Files.exists(file)) return IMAGES.get(IconImageCache.Source.of(file), () -> decodeImage(file, costume.scale()));
        if (back) return null;
        if (shiny) return loadImage(costume, false, false);
        Path sheet = FusionSpriteSheets.path(costume.id());
        if (!Files.exists(sheet)) FusionSpriteSheets.image(costume.id());
        return IMAGES.get(IconImageCache.Source.of(sheet).toString() + ":" + costume.id(),
                () -> crop(FusionSpriteSheets.image(costume.id()), 1));
    }

    static String iconVersion(PartyMember member) throws IOException {
        String id = selected(member);
        if (id.isEmpty()) return "";
        Path file = ClientFiles.data("cosmetics/pokemon/" + id + "/front" + (member.shiny() ? "-s" : "") + ".png");
        if (!Files.exists(file)) file = ClientFiles.data("cosmetics/pokemon/" + id + "/front.png");
        if (!Files.exists(file)) file = FusionSpriteSheets.path(id);
        return id + (Files.exists(file) ? IconImageCache.Source.of(file).toString() : ":missing");
    }

    private static BufferedImage decodeImage(Path file, int scale) throws IOException {
        BufferedImage source = ImageIO.read(file.toFile());
        if (source == null || source.getWidth() > 1024 || source.getHeight() > 1024) throw new IOException("Invalid costume image");
        return crop(source, scale);
    }

    private static BufferedImage crop(BufferedImage source, int scale) throws IOException {
        int l = source.getWidth(), t = source.getHeight(), r = 0, b = 0;
        for (int y = 0; y < source.getHeight(); y++) for (int x = 0; x < source.getWidth(); x++) if ((source.getRGB(x, y) >>> 24) != 0) {
            l = Math.min(l, x); t = Math.min(t, y); r = Math.max(r, x + 1); b = Math.max(b, y + 1);
        }
        if (r <= l || b <= t) throw new IOException("Empty costume image");
        var result = new BufferedImage(Math.max(1, (r-l+scale-1)/scale), Math.max(1, (b-t+scale-1)/scale), BufferedImage.TYPE_INT_ARGB);
        var graphics = result.createGraphics();
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(source, 0, 0, result.getWidth(), result.getHeight(), l, t, r, b, null); graphics.dispose();
        return result;
    }

    public static PokemonAnimation.SummaryFrames summary(PokemonAnimation.SummaryFrames original, PartyMember member) throws IOException {
        var front = image(member, false); var back = image(member, true);
        if (front == null && back == null) return original;
        int l = -original.anchorX(), t = -original.anchorY();
        int r = original.cellWidth() - original.anchorX(), b = original.cellHeight() - original.anchorY();
        for (var image : new BufferedImage[]{front, back}) if (image != null) {
            l = Math.min(l, -image.getWidth()/2); t = Math.min(t, -image.getHeight());
            r = Math.max(r, (image.getWidth()+1)/2);
        }
        return new PokemonAnimation.SummaryFrames(compose(original.front(), front, original, l,t,r,b),
                compose(original.back(), back, original, l,t,r,b), r-l,b-t,-l,-t);
    }

    private static List<BufferedImage> compose(List<BufferedImage> original, BufferedImage replacement,
            PokemonAnimation.SummaryFrames layout, int l, int t, int r, int b) {
        List<BufferedImage> result = new ArrayList<>();
        for (var image : replacement == null ? original : List.of(replacement)) {
            var canvas = new BufferedImage(r-l,b-t,BufferedImage.TYPE_INT_ARGB);var graphics = canvas.createGraphics();
            graphics.drawImage(image, replacement == null ? -layout.anchorX()-l : -image.getWidth()/2-l,
                    replacement == null ? -layout.anchorY()-t : -image.getHeight()-t,null);graphics.dispose();result.add(canvas);
        }
        return List.copyOf(result);
    }
}
