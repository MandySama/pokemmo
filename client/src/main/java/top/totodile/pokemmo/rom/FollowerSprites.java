package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import static top.totodile.pokemmo.rom.NdsArchive.*;
import static top.totodile.pokemmo.rom.NitroData.*;

/** 心金魂银的普通跟随精灵，每方向两帧，不复用队伍的小图标。 */
public final class FollowerSprites {
    private FollowerSprites() { }
    public static BufferedImage[] load(Path rom, int species) throws IOException {
        if (species < 1 || species > 493) throw new IOException("Unsupported follower species");
        return decode(new NdsArchive(rom, "/a/0/8/1").file(296 + index(species)));
    }
    /** 仅开放具有专用行走图的前四世代种族。 */
    public static BufferedImage[] load(Path hgss, Path blackWhite, int species) throws IOException {
        if (species < 1 || species > 493) throw new IOException("Unsupported follower species");
        if (hgss == null) throw new IOException("Missing HGSS ROM");
        return load(hgss, species);
        /* 后续种族的正背面回退暂时停用，等待专用行走资源。
        if (blackWhite == null) throw new IOException("Missing Black/White ROM");
        var front = PokemonAnimation.loadBattle(blackWhite, species, false, false).images();
        var back = PokemonAnimation.loadBattle(blackWhite, species, false, true).images();
        BufferedImage[] frames = new BufferedImage[8];
        for (int direction = 0; direction < 4; direction++) for (int frame = 0; frame < 2; frame++) {
            var sequence = direction == 1 ? back : front;
            BufferedImage source = sequence.get(frame == 0 ? 0 : Math.max(1, sequence.size()/4)%sequence.size());
            BufferedImage target = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
            var graphics = target.createGraphics();
            graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            float scale = Math.min(40f/source.getWidth(), 40f/source.getHeight());
            int width = Math.max(1, Math.round(source.getWidth()*scale)), height = Math.max(1, Math.round(source.getHeight()*scale));
            int x = (48-width)/2, y = 46-height;
            if (direction == 3) graphics.drawImage(source,x+width,y,-width,height,null);
            else graphics.drawImage(source,x,y,width,height,null);
            graphics.dispose();
            frames[direction*2+frame] = target;
        }
        return frames;
        */
    }

    private static int index(int species) {
        int result = species;
        int[] boundaries = {3,25,154,172,201,202,208,214,386,412,413,415,422,423,443,444,445,449,450,479,487,492};
        int[] extra = {1,1,1,1,27,1,1,1,3,2,2,1,1,1,1,1,1,1,1,5,1,1};
        for (int i = 0; i < boundaries.length; i++) if (species > boundaries[i]) result += extra[i];
        return result;
    }

    static BufferedImage[] decode(ByteBuffer data) throws IOException {
        try {
            if (!text(data, 0, 4).equals("BTX0")) throw new IOException("Invalid follower texture");
            int base = data.getInt(16);
            var textures = dictionary(data, base + 60, 8);
            var palettes = dictionary(data, base + 60 + ushort(data, base + 62), 4);
            if (textures.size() != 8 || palettes.isEmpty()) throw new IOException("Unsupported follower frames");
            BufferedImage[] images = new BufferedImage[8];
            int[] order = {2,3,0,1,4,5,6,7};
            for (int frame = 0; frame < 8; frame++) {
                var entry = textures.get(order[frame]);
                int params = ushort(data, entry.offset() + 2), format = params >> 10 & 7;
                int width = 8 << (params >> 4 & 7), height = 8 << (params >> 7 & 7);
                if ((format != 3 && format != 4) || width > 128 || height > 128) throw new IOException("Invalid follower dimensions");
                int pixels = base + data.getInt(base + 20) + ushort(data, entry.offset()) * 8;
                int palette = base + data.getInt(base + 56) + (ushort(data, palettes.getFirst().offset()) & 8191) * 8;
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                for (int i = 0; i < width * height; i++) {
                    int packed = data.get(pixels + (format == 3 ? i / 2 : i)) & 255;
                    int index = format == 3 ? packed >> (i % 2 * 4) & 15 : packed;
                    if (index == 0 && (params & 8192) != 0) continue;
                    int color = ushort(data, palette + index * 2);
                    image.setRGB(i % width, i / width, 0xff000000 | (color & 31) * 8 << 16
                            | (color >> 5 & 31) * 8 << 8 | (color >> 10 & 31) * 8);
                }
                images[frame] = image;
            }
            return images;
        } catch (IndexOutOfBoundsException | IllegalArgumentException exception) { throw new IOException("Invalid follower resource", exception); }
    }
}