package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/** 黑白基础种族的小图标；ARM9 指定调色板，图集同时保存彩色与未遇见剪影。 */
public final class PokedexIcons {
    private record Key(IconImageCache.Source rom, IconImageCache.Source extended) { }
    private static final IconImageCache<Key> CACHE = new IconImageCache<>(2);
    public static final int SPECIES = top.totodile.pokemmo.protocol.PokemonCatalog.MAX_SPECIES;
    public static BufferedImage loadUnknown(Path rom) throws IOException {
        return load(rom).getSubimage(0, 0, 1024, ((SPECIES + 31) / 32) * 32);
    }

    public static BufferedImage load(Path rom) throws IOException {
        var key = new Key(IconImageCache.Source.of(rom), IconImageCache.Source.of(
                top.totodile.pokemmo.ClientFiles.data("resources/pokemon-gen9.zip")));
        return CACHE.get(key, () -> decode(rom));
    }

    private static BufferedImage decode(Path rom) throws IOException {
        NdsArchive archive = new NdsArchive(rom, "/a/0/0/7");
        BufferedImage atlas = new BufferedImage(64 * 32, 32 * ((SPECIES + 31) / 32), BufferedImage.TYPE_INT_ARGB);
        try {
            ByteBuffer arm9 = NdsArchive.arm9(rom);
            byte[] signature = {0,17,17,17,0,0,0,0,34,34,17,17,0,17,34,34};
            int directory = -1;
            for (int i = 0; i <= arm9.limit() - 712; i++) {
                int j = 0;
                while (j < signature.length && arm9.get(i+j) == signature[j]) j++;
                if (j == signature.length) { directory = i; break; }
            }
            if (directory < 0) throw new IOException("Missing Pokemon icon palette directory");
            ByteBuffer palettes = archive.file(0);
            int paletteStart = 24 + palettes.getInt(36);
            for (int species = 1; species <= 649; species++) {
                ByteBuffer tiles = archive.file(species * 2 + 7);
                if (tiles.getInt(0) != 0x4e434752 || tiles.getInt(28) != 3 || tiles.limit() < 48 + 512)
                    throw new IOException("Invalid Pokedex icon tiles");
                int originX = (species - 1) % 32 * 32, originY = (species - 1) / 32 * 32;
                for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
                    int pixel = (y / 8 * 4 + x / 8) * 64 + y % 8 * 8 + x % 8;
                    int index = tiles.get(48 + pixel / 2) >>> ((pixel & 1) * 4) & 15;
                    atlas.setRGB(originX + x, originY + y, index == 0 ? 0 : 0xb4000000);
                    int bank = arm9.get(directory + species) & 15;
                    int c = Short.toUnsignedInt(palettes.getShort(paletteStart + bank * 32 + index * 2));
                    int color = index == 0 ? 0 : 0xff000000 | ((c & 31) * 255 / 31) << 16
                            | ((c >> 5 & 31) * 255 / 31) << 8 | (c >> 10 & 31) * 255 / 31;
                    atlas.setRGB(originX + x + 1024, originY + y, color);
                }
            }
        } catch (IndexOutOfBoundsException exception) { throw new IOException("Invalid Pokedex archive", exception); }
        try (var zip = new java.util.zip.ZipFile(top.totodile.pokemmo.ClientFiles.data("resources/pokemon-gen9.zip").toFile())) {
            for (int id = 650; id <= SPECIES; id++) {
                var icon = ExtendedPokemonSprites.icon(zip, id);
                var tile = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                var g = tile.createGraphics();
                int left=icon.getWidth(),top=icon.getHeight(),right=0,bottom=0;
                for(int y=0;y<icon.getHeight();y++)for(int x=0;x<icon.getWidth();x++)if((icon.getRGB(x,y)>>>24)!=0){
                    left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x+1);bottom=Math.max(bottom,y+1);
                }
                if(right<=left||bottom<=top)throw new IOException("Empty extended icon "+id);
                // 仅对超出槽位的内容缩小，其余像素保持原比例并移入可见范围。
                double scale=Math.min(1,Math.min(32.0/(right-left),32.0/(bottom-top)));
                int width=Math.max(1,(int)Math.round((right-left)*scale)),height=Math.max(1,(int)Math.round((bottom-top)*scale));
                g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(icon,(32-width)/2,(32-height)/2,(32+width)/2,(32+height)/2,left,top,right,bottom,null);g.dispose();
                int ox = (id-1)%32*32, oy = (id-1)/32*32;
                for (int y=0;y<32;y++) for (int x=0;x<32;x++) {
                    int color = tile.getRGB(x,y);
                    atlas.setRGB(ox+1024+x,oy+y,color);
                    atlas.setRGB(ox+x,oy+y,(color>>>24)==0?0:0xb4000000);
                }
            }
        }
        return atlas;
    }
}
