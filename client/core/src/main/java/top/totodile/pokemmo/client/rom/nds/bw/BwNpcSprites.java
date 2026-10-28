package top.totodile.pokemmo.client.rom.nds.bw;

import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;
import top.totodile.pokemmo.client.rom.nds.model.NitroTextureDictionary;
import top.totodile.pokemmo.client.rom.gba.GbaNpcSprites.Frame;
import java.io.IOException;
import java.nio.*;
import java.util.*;

/** BlackWhiteRom.Z10 + BattleTurnStatusRecord.gs0: native sprite mapping and frame order. */
public final class BwNpcSprites {
    private record Entry(int model,int animation,int type,int size) {}
    private final Map<Integer,Entry> entries=new HashMap<>();
    private final Map<Integer,List<Frame>> cache=new HashMap<>();
    private final NdsArchive models;
    public BwNpcSprites(RomInfo rom)throws IOException {
        models=new NdsArchive(RomReader.readFile(rom,"a/0/4/9"));
        var bytes=new NdsArchive(RomReader.readFile(rom,"a/0/4/8")).readRaw(0);
        var b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);int count=b.getInt();
        if(count<0||4L+count*28L>bytes.length)throw new IOException("Invalid BW NPC mapping");
        for(int i=0;i<count;i++){int p=4+i*28;entries.put(Short.toUnsignedInt(b.getShort(p)),new Entry(Short.toUnsignedInt(b.getShort(p+16)),b.get(p+9)&255,b.get(p+2)&255,b.get(p+7)&255));}
    }
    /** GdxBoneWeightsManager.jq0: 32x32 billboard, scale .011 (.022 for size 2); tile=.25. */
    public float billboardSize(int graphics){var e=entries.get(graphics);return 32*(e!=null&&e.size()==2?.022f:.011f)*64;}
    public Frame frame(int graphics,int direction,int phase)throws IOException {
        // BlackWhiteRom.Z10 starts textured NPC models at 6; lower entries are not sprite models.
        var e=entries.get(graphics);if(e==null||e.type()!=1||e.model()<6)return null;
        var frames=cache.get(e.model());
        if(frames==null) {
            var dictionary=new NitroTextureDictionary(models.read(e.model()),true);var decoded=new ArrayList<Frame>();
            if(dictionary.paletteNames().isEmpty())return null;
            String palette=dictionary.paletteNames().iterator().next();
            for(var name:dictionary.names()) {
                var pixels=dictionary.pixels(name,palette,false,false);
                try {int[] rgba=new int[pixels.getWidth()*pixels.getHeight()];for(int y=0;y<pixels.getHeight();y++)for(int x=0;x<pixels.getWidth();x++)rgba[y*pixels.getWidth()+x]=pixels.getPixel(x,y);decoded.add(new Frame(pixels.getWidth(),pixels.getHeight(),rgba));}
                finally {pixels.dispose();}
            }
            frames=List.copyOf(decoded);cache.put(e.model(),frames);
        }
        if(frames.isEmpty())return null;
        int index;
        switch(e.animation()) {
            case 8,19 -> index=0;
            case 5,12 -> index=direction==0?phase%2+2:direction==2?phase%2+4:direction==3?(phase%2)*2+4:phase%2;
            case 13,14 -> index=direction==0?2:direction==2?4:direction==3?(e.animation()==13?4:6):0;
            default -> index=(direction==0?3:direction==2?6:direction==3?9:0)+new int[]{0,1,0,2}[phase%4];
        }
        return frames.get(index<frames.size()?index:0);
    }
}
