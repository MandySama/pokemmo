package top.totodile.pokemmo.client.rom.nds.gen4;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.gba.GbaNpcSprites.Frame;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;
import top.totodile.pokemmo.client.rom.nds.model.NitroTextureDictionary;
import java.io.IOException;
import java.util.*;
/** Native Platinum/HGSS sprites, following PlatinumRom overlay-5 NG and HGSS GY. */
public final class Gen4NpcSprites {
    private static final int[] FRAMES_16={11,0,2,12,14,8,10,1,3,11};
    private static final int[] FRAMES_17={12,0,2,13,15,9,11,1,3,12};
    private static final int[] FRAMES_8={2,0,4,2,3,0,1,4,5};
    private final NdsArchive models;
    private final Map<Integer,List<Frame>> cache=new HashMap<>();
    public Gen4NpcSprites(RomInfo rom) throws IOException {
        models=new NdsArchive(RomReader.readFile(rom,rom.type()==RomType.PLATINUM?"data/mmodel/mmodel.narc":"a/0/8/1"));
        platinum=rom.type()==RomType.PLATINUM;
        if(platinum) {
            byte[] h=RomReader.read(rom,0,0x60);
            byte[] table=RomReader.read(rom,i32(h,0x50),i32(h,0x54));
            int p=5*32,file=i32(table,p+24);
            byte[] fat=RomReader.read(rom,i32(h,0x48)+file*8L,8);
            byte[] overlay=RomReader.read(rom,i32(fat,0),i32(fat,4)-i32(fat,0));
            if((i32(table,p+28)&0x1000000)!=0)overlay=inflate(overlay);
            int start=-1;
            for(int i=0;i+16<=overlay.length;i+=4)if(i32(overlay,i)==275&&i32(overlay,i+4)==1089&&i32(overlay,i+8)==65535){start=i+16;break;}
            if(start<0)throw new IOException("Platinum NPC mapping signature missing");
            for(int i=start;i+8<=overlay.length;i+=8){int id=i32(overlay,i),model=i32(overlay,i+4);if(id==65535)break;mapping.put(id,model);}
        }
    }
    private final boolean platinum;
    private final Map<Integer,Integer> mapping=new HashMap<>();
    public int model(int graphics){return platinum?mapping.getOrDefault(graphics,-1):graphics;}
    public Frame frame(int graphics,int direction,int phase) throws IOException {
        int model=model(graphics);
        if(model<0) return null;
        var frames=cache.get(model);
        if(frames==null) {
            // Native region graphics resolve to zero-based archive indices.
            int archiveIndex=model;
            if(archiveIndex<0 || archiveIndex>=models.size()) return null;
            byte[] bytes=models.read(archiveIndex);
            // Both native archives expose BTX0 texture bundles.
            if(bytes.length<4 || bytes[0]!='B'||bytes[1]!='T'||bytes[2]!='X'||bytes[3]!='0')return null;
            var dictionary=new NitroTextureDictionary(bytes,platinum);
            if(dictionary.paletteNames().isEmpty())return null;
            String palette=dictionary.paletteNames().iterator().next();
            var decoded=new ArrayList<Frame>();
            for(String texture:dictionary.names()) {
                var pixels=dictionary.pixels(texture,palette,false,false);
                try {
                    int[] rgba=new int[pixels.getWidth()*pixels.getHeight()];
                    for(int y=0;y<pixels.getHeight();y++)for(int x=0;x<pixels.getWidth();x++)
                        rgba[y*pixels.getWidth()+x]=pixels.getPixel(x,y);
                    decoded.add(new Frame(pixels.getWidth(),pixels.getHeight(),rgba));
                } finally {pixels.dispose();}
            }
            frames=List.copyOf(decoded);cache.put(model,frames);
        }
        if(frames.isEmpty())return null;
        int index=direction==3?2:direction;
        if(phase==1)index=index*2+3;else if(phase==3)index=index*2+4;
        int[] mapping=frames.size()==16?FRAMES_16:frames.size()==17?FRAMES_17:frames.size()==8?FRAMES_8:null;
        if(mapping!=null) {if(index>=mapping.length)return null;index=mapping[index];}
        return frames.get(index<frames.size()?index:0);
    }
    private static byte[] inflate(byte[] input) throws IOException {
        try {
            int n = input.length, extra = i32(input, n - 4);
            if (extra == 0) return input;
            int header = input[n - 5] & 255, packed = i32(input, n - 8) & 0xffffff;
            if (header < 8
                    || header > 11
                    || packed < header
                    || packed > n
                    || extra < 0
                    || extra > 16 * 1024 * 1024 - n)
                throw new IOException("Invalid ARM9 compression footer");
            int start = n - packed, source = n - header, destination = n + extra;
            byte[] out = Arrays.copyOf(input, destination);
            while (destination > start) {
                int flags = out[--source] & 255;
                for (int bit = 7; bit >= 0 && destination > start; bit--) {
                    if ((flags & (1 << bit)) == 0) out[--destination] = out[--source];
                    else {
                        int token = (out[--source] & 255) << 8;
                        token |= out[--source] & 255;
                        int length = (token >>> 12) + 3, distance = (token & 4095) + 3;
                        while (length-- > 0 && destination > start) {
                            destination--;
                            out[destination] = out[destination + distance];
                        }
                    }
                }
            }
            return out;
        } catch (IndexOutOfBoundsException error) {
            throw new IOException("Truncated ARM9", error);
        }
    }

    private static int i32(byte[] b,int p){return java.nio.ByteBuffer.wrap(b).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt(p);}
}
