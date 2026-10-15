package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;

/** NDS 城镇地图的双层图块合成与地图头定位。 */
public final class NdsRegionMap extends GbaRegionMap {
    private final NdsMaps maps;
    private final Map<Integer, int[]> positions = new HashMap<>();
    private final Map<Integer, int[]> names = new HashMap<>();
    private final Map<Integer, int[]> resolved = new HashMap<>();
    private final BufferedImage pixels;
    private final boolean sinnoh;
    private NdsMaps.Matrix matrix;

    public static NdsRegionMap load(Path rom, int region) throws IOException {
        if (rom == null || (region != 2 && region != 3)) throw new IOException("Missing NDS region ROM");
        try { return new NdsRegionMap(rom, region); }
        catch (RuntimeException exception) { throw new IOException("Invalid NDS region map", exception); }
    }

    private NdsRegionMap(Path rom, int region) throws IOException {
        super(new byte[0], 1);
        sinnoh = region == 3;
        NdsArchive archive = new NdsArchive(rom, sinnoh ? "/graphic/tmap_gra.narc" : "/a/0/8/5");
        pixels = new BufferedImage(256, 168, BufferedImage.TYPE_INT_ARGB);
        layer(archive.file(0), archive.file(sinnoh ? 19 : 7), archive.file(sinnoh ? 22 : 16), false);
        layer(archive.file(0), archive.file(sinnoh ? 19 : 5), archive.file(sinnoh ? 24 : 17), true);
        maps = sinnoh ? new PlatinumMaps(rom) : new BlackWhiteMaps(rom);
        if (sinnoh) {
            matrix = maps.matrix(0);
            int[] headers = matrix.headers();
            if (headers != null) for (int i = 0; i < headers.length; i++) {
                int id = headers[i];
                if (id <= 0 || id >= maps.headers().size()) continue;
                put(id, 24 + i % matrix.width() * 7, -36 + i / matrix.width() * 7);
            }
            var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            try {
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                var nodes = factory.newDocumentBuilder().parse(MapFiles.data("maps/sinnoh-town-map.xml").toFile()).getElementsByTagName("location");
                for (int i = 0; i < nodes.getLength(); i++) {
                    var node = (org.w3c.dom.Element) nodes.item(i);
                    put(Integer.parseInt(node.getAttribute("map")), 24 + Integer.parseInt(node.getAttribute("x"))*7,
                            -36 + Integer.parseInt(node.getAttribute("y"))*7);
                }
            } catch (Exception exception) { throw new IOException("Invalid Sinnoh town map locations", exception); }
        } else {
            ByteBuffer entries = new NdsArchive(rom, "/a/0/8/6").file(0);
            if (entries.limit() % 54 != 0) throw new IOException("Invalid Unova town map locations");
            for (int offset = 0; offset < entries.limit(); offset += 54)
                put(u16(entries, offset), u16(entries, offset+4), u16(entries, offset+6));
        }
    }

    private void put(int id, int x, int y) {
        if (id < 0 || id >= maps.headers().size() || x < 0 || y < 0 || x >= 256 || y >= 168) return;
        int[] spot = {0, x, y};
        positions.putIfAbsent(id, spot);
        names.putIfAbsent(maps.header(id).name(), spot);
    }

    public int[] locate(int id, int x, int y) {
        if (sinnoh && matrix.headers() != null && x >= 0 && y >= 0) {
            int column = x / 32, row = y / 32;
            if (column < matrix.width() && row < matrix.height()) {
                int owner = matrix.headers()[row * matrix.width() + column];
                if (owner == id && positions.containsKey(id)) return new int[]{0, 24 + column*7, -36 + row*7};
            }
        }
        if (resolved.containsKey(id)) {
            int[] cached = resolved.get(id);
            return cached == null ? null : cached.clone();
        }
        Set<Integer> visited = new HashSet<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(id);
        while (!queue.isEmpty() && visited.size() < maps.headers().size()) {
            int current = queue.removeFirst();
            if (!visited.add(current) || current < 0 || current >= maps.headers().size()) continue;
            int[] spot = positions.get(current);
            if (spot == null) spot = names.get(maps.header(current).name());
            if (spot != null) {
                resolved.put(id, spot);
                return spot.clone();
            }
            try { for (var warp : maps.warps(current)) if (!warp.special()) queue.add(warp.target()); }
            catch (IOException ignored) { }
        }
        resolved.put(id, null);
        return null;
    }

    private void layer(ByteBuffer palette, ByteBuffer tiles, ByteBuffer screen, boolean overlay) throws IOException {
        int pal = block(palette, "TTLP"), chr = block(tiles, "RAHC"), scr = block(screen, "NRCS");
        int colors = palette.getInt(pal+16)/2, paletteStart = pal+8+palette.getInt(pal+20);
        int depth = tiles.getInt(chr+12) == 4 ? 8 : 4;
        int tileStart = chr+8+tiles.getInt(chr+28), tileLength = tiles.getInt(chr+24);
        int width = u16(screen,scr+8), height = u16(screen,scr+10);
        int size = screen.getInt(scr+16);
        if (width % 8 != 0 || height % 8 != 0 || size != width*height/32) throw new IOException("Invalid Nitro screen dimensions");
        for (int y = 0; y < Math.min(168,height); y++) for (int x = 0; x < Math.min(256,width); x++) {
            int entry = u16(screen,scr+20+((y/8)*(width/8)+x/8)*2);
            int tx = (entry&1024) != 0 ? 7-x%8 : x%8, ty = (entry&2048) != 0 ? 7-y%8 : y%8;
            int index = (entry&1023)*64+ty*8+tx;
            int offset = index*depth/8;
            if (offset >= tileLength) throw new IOException("Nitro screen tile out of range");
            int value = tiles.get(tileStart+offset)&255;
            if (depth == 4) value = value >> ((index&1)*4)&15;
            if (overlay && value == 0) continue;
            int colorIndex = value + (depth == 4 ? (entry>>>12)*16 : 0);
            if (colorIndex >= colors) throw new IOException("Nitro palette index out of range");
            int color = u16(palette,paletteStart+colorIndex*2);
            pixels.setRGB(x,y,0xff000000|(color&31)*255/31<<16|(color>>5&31)*255/31<<8|(color>>10&31)*255/31);
        }
    }

    private static int block(ByteBuffer data, String name) throws IOException {
        for (int offset = u16(data,12); offset+8 <= data.limit();) {
            if (NdsArchive.text(data,offset,4).equals(name)) return offset;
            int size = data.getInt(offset+4);
            if (size < 8) break;
            offset += size;
        }
        throw new IOException("Missing Nitro block " + name);
    }
    private static int u16(ByteBuffer data, int offset) { return Short.toUnsignedInt(data.getShort(offset)); }
    @Override protected int gridColumns() { return 256; }
    @Override public int pixelX(int column) { return column; }
    @Override public int pixelY(int row) { return row; }
    @Override protected BufferedImage decode(int region) { return pixels; }
    @Override protected void validate() { }
}
