package top.totodile.pokemmo.client.startup;

import java.io.IOException;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import top.totodile.pokemmo.client.rom.nds.model.NitroModelRenderer;

/** 参考 NdsNitroModelParser.D7：直接解码 PAT0 指定的 TEX0 纹理与调色板。 */
final class RomTexturePattern implements AutoCloseable {
    private record Track(String material,int[] frames,String[] textures,String[] palettes) {}
    private final List<Track> tracks=new ArrayList<>();
    private final NitroModelRenderer renderer;
    private int duration;
    RomTexturePattern(byte[] bytes,NitroModelRenderer renderer) throws IOException {
        this.renderer=renderer;
        try {
            ByteBuffer b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            int section=b.getInt(16), info=section+8, count=bytes[info+1]&255;
            if (count==0) return;
            int animation=section+b.getInt(info+16+count*4);
            duration=Short.toUnsignedInt(b.getShort(animation+4));
            int names=animation+Short.toUnsignedInt(b.getShort(animation+8));
            int textureCount=bytes[animation+6]&255;
            int paletteCount=bytes[animation+7]&255;
            String[] filenames=new String[textureCount];
            String[] palettes=new String[paletteCount];
            for (int i=0;i<textureCount;i++) filenames[i]=name(bytes,names+i*16);
            int paletteNames=animation+Short.toUnsignedInt(b.getShort(animation+10));
            for(int i=0;i<paletteCount;i++)palettes[i]=name(bytes,paletteNames+i*16);
            info=animation+12; count=bytes[info+1]&255;
            int records=info+16+count*4;
            for (int i=0;i<count;i++) {
                String materialName=name(bytes,records+count*8+i*16);
                var material=renderer.instance.getMaterial(materialName);
                if (material==null) continue;
                int keys=b.getInt(records+i*8), offset=animation+Short.toUnsignedInt(b.getShort(records+i*8+6));
                if (keys<1 || keys>65535) throw new IOException("Invalid PAT0 track");
                int[] frames=new int[keys]; String[] images=new String[keys],colors=new String[keys];
                for (int key=0;key<keys;key++) {
                    frames[key]=Short.toUnsignedInt(b.getShort(offset+key*4));
                    int texture=bytes[offset+key*4+2]&255;
                    int palette=bytes[offset+key*4+3]&255;
                    if (texture>=textureCount) throw new IOException("Invalid PAT0 texture index");
                    if(palette>=paletteCount)throw new IOException("Invalid PAT0 palette index");
                    images[key]=filenames[texture]; colors[key]=palettes[palette];
                }
                tracks.add(new Track(materialName,frames,images,colors));
            }
        } catch (IOException | RuntimeException error) { close(); throw new IOException("Invalid title texture animation",error); }
    }
    private static String name(byte[] bytes,int offset) {
        int size=0; while(size<16 && bytes[offset+size]!=0) size++;
        return new String(bytes,offset,size,StandardCharsets.US_ASCII);
    }
    void update(float time) throws IOException {
        int frame=Math.min(Math.max(0,(int)(time*30)),Math.max(0,duration-1));
        for (Track track:tracks) {
            int key=0; while(key+1<track.frames.length && track.frames[key+1]<=frame) key++;
            renderer.setTexture(track.material,track.textures[key],track.palettes[key]);
        }
    }
    @Override public void close() { tracks.clear(); }
}
