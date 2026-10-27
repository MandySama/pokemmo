package top.totodile.pokemmo.client.rom.gba;

import top.totodile.pokemmo.client.rom.RomInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/** GbaSpritePaletteLoader, GbaSpriteAnimationData and CharacterRawTextureProvider. */
public final class GbaNpcSprites {
    public record Frame(int width, int height, int[] rgba) {}

    private final byte[] data;
    private final int table;
    private final Map<Integer, Integer> palettes = new HashMap<>();
    private final TreeSet<Integer> frameTables = new TreeSet<>();

    public GbaNpcSprites(RomInfo rom) throws IOException {
        var tables = GbaSurfSprites.tables(rom);
        data = Files.readAllBytes(rom.path());
        table = tables[0];
        for (int section = 1; section <= 2; section++) {
            for (int i = 0; i < 46; i++) {
                int row = tables[section] + i * 8;
                if (!validPointer(row)) break;
                int id = (byte) u8(row + 4) + (section == 2 ? 26 : 0);
                if (id >= 0 && id < 46) palettes.put(id, pointer(row));
            }
        }
        if (!palettes.containsKey(0)) throw new IOException("Missing GBA NPC palette zero");
        // The reference counts frame records up to the next descriptor's frame table.
        // Static objects often have one frame; an adjacent sprite pointer is not their frame 1.
        int separators = 0;
        for (int i = 0; i < 1024 && separators < 2; i++) {
            int row = table + i * 4;
            if (!validPointer(row)) { separators++; continue; }
            int descriptor = pointer(row), kind = (byte) u8(descriptor + 3);
            int at = kind == 17 ? descriptor + 28
                    : kind == 16 || kind == -1 ? descriptor + 12 : -1;
            if (at >= 0 && validPointer(at)) frameTables.add(pointer(at));
        }
    }

    public Frame frame(int graphics, int direction) throws IOException {
        return frame(graphics, direction, 0);
    }

    /** GdxParticleController.F7: stand, left step, stand, right step at 160ms intervals. */
    public Frame frame(int graphics, int direction, int phase) throws IOException {
        // N30 hides model 0; it is a script-controlled player placeholder, not an NPC sprite.
        if (graphics == 0) return null;
        int descriptor = pointer(table + graphics * 4);
        if (u16(descriptor) != 65535) return null;
        int kind = (byte) u8(descriptor + 3);
        int paletteId = (byte) u8(descriptor + 2), width, height, frames;
        if (kind == 17) {
            width = u16(descriptor + 8);
            height = u16(descriptor + 10);
            frames = pointer(descriptor + 28);
        } else if (kind == 16) {
            paletteId += 26;
            width = height = 16;
            frames = pointer(descriptor + 12);
        } else {
            // T4=-1 is a field-effect sprite, omitted by reference NPC N30 as well.
            return null;
        }
        int frame = direction == 3 ? 2 : direction;
        if (phase == 1) frame = frame * 2 + 3;
        else if (phase == 3) frame = frame * 2 + 4;
        Integer boundary = frameTables.higher(frames);
        if (frame < 0 || frame > 8 || boundary != null && frames + (frame + 1) * 8 > boundary
                || !validPointer(frames + frame * 8)) frame = 0;
        int pixels = pointer(frames + frame * 8);
        // Dedicated reference providers halve packed furniture/field sprite dimensions.
        if (graphics == 93) width /= 2;
        if (graphics == 153 || graphics == 161 || graphics == 165) height /= 2;
        if (width < 8 || height < 8 || width % 8 != 0 || height % 8 != 0
                || width > 256 || height > 256) throw new IOException("Invalid NPC dimensions");
        int palette = palettes.getOrDefault(paletteId, palettes.get(0));
        int rawWidth = graphics == 151 ? width / 2 : width;
        int rawHeight = graphics == 151 ? height * 2 : height;
        int[] rgba = new int[rawWidth * rawHeight];
        for (int y = 0; y < rawHeight; y++)
            for (int x = 0; x < rawWidth; x++) {
                int tile = (y / 8) * (rawWidth / 8) + x / 8;
                int index = u8(pixels + tile * 32 + (y & 7) * 4 + (x & 7) / 2)
                        >> ((x & 1) * 4) & 15;
                if (index == 0) continue;
                int color = u16(palette + index * 2);
                rgba[y * rawWidth + x] = ((color & 31) * 255 / 31) << 24
                        | ((color >> 5 & 31) * 255 / 31) << 16
                        | ((color >> 10 & 31) * 255 / 31) << 8 | 255;
            }
        if (graphics == 151) {
            // MapLampGlowSpriteProvider: four vertical 64x32 chunks form a 2x2 ship.
            int[] arranged = new int[width * height];
            for (int part = 0; part < 4; part++)
                for (int y = 0; y < 32; y++)
                    System.arraycopy(rgba, (part * 32 + y) * rawWidth, arranged,
                            (part / 2 * 32 + y) * width + part % 2 * 64, 64);
            rgba = arranged;
        }
        return new Frame(width, height, rgba);
    }

    /** BadgeIconButton state 6 -> GbaRom.o00_0's original emote strip, slice 2. */
    public Frame exclamation() throws IOException {
        int graphics=signaturePointer("08080000004000040000",0);
        int palette=signaturePointer("FF110010800040001A000000",84);
        int[] rgba=new int[256];
        for(int y=0;y<16;y++)for(int x=0;x<16;x++){
            int tile=8+(y/8)*2+x/8;
            int index=u8(graphics+tile*32+(y&7)*4+(x&7)/2)>>>((x&1)*4)&15;
            if(index==0)continue;
            int c=u16(palette+index*2);
            rgba[y*16+x]=((c&31)*255/31)<<24|((c>>5&31)*255/31)<<16|((c>>10&31)*255/31)<<8|255;
        }
        return new Frame(16,16,rgba);
    }
    private int signaturePointer(String hex,int offset) throws IOException {
        byte[] pattern=java.util.HexFormat.of().parseHex(hex);
        search:for(int p=0;p<data.length-pattern.length-4-offset;p++){
            for(int i=0;i<pattern.length;i++)if(data[p+i]!=pattern[i])continue search;
            return pointer(p+pattern.length+offset);
        }
        throw new IOException("Missing reference NPC reaction resource");
    }

    private int u8(int p) throws IOException {
        if (p < 0 || p >= data.length) throw new IOException("GBA NPC read outside ROM");
        return data[p] & 255;
    }

    private int u16(int p) throws IOException { return u8(p) | u8(p + 1) << 8; }
    private int u32(int p) throws IOException { return u16(p) | u16(p + 2) << 16; }
    private boolean validPointer(int p) throws IOException {
        int value = u32(p);
        return (value & 0xfe000000) == 0x08000000 && (value & 0x1ffffff) < data.length;
    }
    private int pointer(int p) throws IOException {
        if (!validPointer(p)) throw new IOException("Invalid GBA NPC pointer at " + Integer.toHexString(p));
        return u32(p) & 0x1ffffff;
    }
}
