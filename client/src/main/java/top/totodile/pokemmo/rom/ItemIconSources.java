package top.totodile.pokemmo.rom;

import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.protocol.ItemCatalog;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.*;

/** 按公共道具编号解析图标别名；外部 PNG 优先，缺失时使用所属 ROM 的图块。 */
public final class ItemIconSources {
    private final RomFiles roms;
    private final Map<Integer, BufferedImage> cache = new HashMap<>();
    private final Map<Integer, NdsIcons> nds = new HashMap<>();
    private final Map<Integer, GbaIcons> gba = new HashMap<>();
    private BlackWhiteItemIcons bw;

    public ItemIconSources(RomFiles roms) { this.roms = roms; }

    public BufferedImage image(ItemCatalog.Item item) throws IOException {
        int icon = item.iconId();
        if (cache.containsKey(icon)) return cache.get(icon);
        BufferedImage pixels = png(icon);
        if (pixels == null) {
            var base = ItemCatalog.get(icon);
            if (base == null && icon == item.id()) base = item;
            if (base != null && base.source() >= 0) {
                Path rom = roms.selected(base.source());
                if (rom != null) {
                    if (base.source() == 0 && icon >= 5001 && icon <= 5626) {
                        if (bw == null) bw = new BlackWhiteItemIcons(rom);
                        pixels = bw.image(icon);
                    } else if (base.source() == 1 || base.source() == 2) {
                        GbaIcons source = gba.get(base.source());
                        if (source == null) { source = new GbaIcons(rom, base.source() == 2); gba.put(base.source(), source); }
                        pixels = source.image(icon);
                    } else if (base.source() == 3 || base.source() == 4) {
                        NdsIcons source = nds.get(base.source());
                        if (source == null) { source = new NdsIcons(rom, base.source() == 4); nds.put(base.source(), source); }
                        pixels = source.image(icon);
                    }
                }
            }
        }
        if (pixels == null) pixels = png(-1);
        if (pixels == null) pixels = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        cache.put(icon, pixels);
        return pixels;
    }

    private static BufferedImage png(int id) throws IOException {
        Path path = ClientFiles.data("sprites/itemicons/" + id + ".png");
        if (!Files.isRegularFile(path)) return null;
        var image = ImageIO.read(path.toFile());
        if (image == null || image.getWidth() > 512 || image.getHeight() > 512) throw new IOException("Invalid item icon PNG");
        return image;
    }

    private static final class NdsIcons {
        private final NdsArchive archive;
        private final ByteBuffer table;
        private final int base, count;
        NdsIcons(Path rom, boolean hgss) throws IOException {
            archive = new NdsArchive(rom, hgss ? "/a/0/1/8" : "/itemtool/itemdata/item_icon.narc");
            ByteBuffer arm9 = NdsArchive.arm9(rom,hgss);
            int start = -1, marker = hgss ? 16318714 : 16318896;
            for (int i = 0; i <= arm9.limit() - 12; i += 4) {
                if (arm9.getInt(i) == 4587577 && arm9.getInt(i + 4) == marker && arm9.getInt(i + 8) == 28246143) { start = i + 12; break; }
            }
            count = hgss ? 537 : 468; base = hgss ? 9000 : 8000;
            if (start < 0 || (long) start + count * 8 > arm9.limit()) throw new IOException("Missing NDS item icon table");
            table = arm9.slice(start, count * 8).order(ByteOrder.LITTLE_ENDIAN);
        }
        BufferedImage image(int id) throws IOException {
            int index = id - base;
            if (index < 0 || index >= count) return null;
            int tile = Short.toUnsignedInt(table.getShort(index * 8 + 2)), palette = Short.toUnsignedInt(table.getShort(index * 8 + 4));
            ByteBuffer tiles = NdsCompression.decode(archive.file(tile)), colors = NdsCompression.decode(archive.file(palette));
            if (tiles.getInt(0) != 0x4e434752 || colors.getInt(0) != 0x4e434c52) throw new IOException("Invalid NDS item icon");
            return decode(tiles, 48, colors, 24 + colors.getInt(36), 4);
        }
    }

    private static final class GbaIcons {
        private final ByteBuffer rom;
        private final int table;
        GbaIcons(Path path, boolean emerald) throws IOException {
            byte[] bytes = Files.readAllBytes(path);
            rom = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            byte[] pattern = HexFormat.of().parseHex("40188018006802BC08470000");
            int found = -1;
            outer: for (int i = 0; i <= bytes.length - pattern.length; i++) {
                for (int j = 0; j < pattern.length; j++) if (bytes[i+j] != pattern[j]) continue outer;
                found = i; break;
            }
            if (found < 0) throw new IOException("Missing GBA item icon table");
            table = pointer(found + pattern.length + (emerald ? 0 : 4));
        }
        private int pointer(int offset) throws IOException {
            if (offset < 0 || offset > rom.limit() - 4) throw new IOException("Invalid GBA icon pointer");
            int value = rom.getInt(offset) - 0x08000000;
            if (value < 0 || value >= rom.limit()) throw new IOException("Invalid GBA icon address");
            return value;
        }
        BufferedImage image(int id) throws IOException {
            if (id < 0 || id > 376) return null;
            int offset = table + id * 8;
            int tile = pointer(offset), palette = pointer(offset + 4);
            ByteBuffer tiles = NdsCompression.decode(rom.slice(tile, rom.limit() - tile).order(ByteOrder.LITTLE_ENDIAN));
            ByteBuffer colors = NdsCompression.decode(rom.slice(palette, rom.limit() - palette).order(ByteOrder.LITTLE_ENDIAN));
            return decode(tiles, 0, colors, 0, 3);
        }
    }

    private static BufferedImage decode(ByteBuffer tiles, int tileStart, ByteBuffer palette, int paletteStart, int columns) throws IOException {
        try {
            int[] colors = new int[16];
            for (int i = 1; i < 16; i++) {
                int c = Short.toUnsignedInt(palette.getShort(paletteStart + i * 2));
                colors[i] = 0xff000000 | (c & 31) * 255 / 31 << 16 | (c >> 5 & 31) * 255 / 31 << 8 | (c >> 10 & 31) * 255 / 31;
            }
            var image = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
            for (int y=0;y<24;y++) for (int x=0;x<24;x++) {
                int pixel=((y/8)*columns+x/8)*64+(y%8)*8+x%8;
                image.setRGB(x,y,colors[(tiles.get(tileStart+pixel/2)>>>((pixel&1)*4))&15]);
            }
            return image;
        } catch (IndexOutOfBoundsException exception) { throw new IOException("Truncated item icon",exception); }
    }
}
