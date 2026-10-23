package top.totodile.pokemmo.client.startup;

import java.io.IOException;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import top.totodile.pokemmo.client.rom.nds.model.NitroModelRenderer;

/** Black/White 2 title SRT0 tracks, following EvolutionChainRomChunk and D7. */
final class RomTextureTransform {
    private record Track(RomTitleShaderProvider.Transform target,float[][] values) {}
    private final List<Track> tracks=new ArrayList<>();
    private final int frames;
    RomTextureTransform(byte[] bytes,NitroModelRenderer renderer) throws IOException {
        try {
            ByteBuffer b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            int section=b.getInt(16), dictionary=section+8;
            if(b.getInt(section)!=0x30545253) throw new IOException("Missing SRT0 section");
            int count=bytes[dictionary+1]&255;
            int base=section+b.getInt(dictionary+16+count*4);
            frames=Short.toUnsignedInt(b.getShort(base+4));
            dictionary=base+8; count=bytes[dictionary+1]&255;
            int records=dictionary+16+count*4;
            for(int i=0;i<count;i++) {
                int nameStart=records+count*40+i*16, length=0;
                while(length<16 && bytes[nameStart+length]!=0) length++;
                String name=new String(bytes,nameStart,length,StandardCharsets.US_ASCII);
                var material=renderer.instance.getMaterial(name);
                if(material==null) continue;
                float[][] values=new float[5][];
                for(int c=0;c<5;c++) values[c]=channel(b,base,records+i*40+c*8,frames,c==2);
                var target=new RomTitleShaderProvider.Transform(); material.set(target);
                tracks.add(new Track(target,values));
            }
        } catch(RuntimeException error) { throw new IOException("Invalid title SRT0 animation",error); }
    }
    private static float[] channel(ByteBuffer b,int base,int record,int frames,boolean rotation) {
        int flags=b.getInt(record), value=b.getInt(record+4);
        int step=1<<(flags>>>30), last=flags&65535;
        float[] result=new float[rotation?frames*2:frames];
        for(int frame=0;frame<frames;frame++) {
            int a=frame/step, boundary=last/step;
            if(frame>=last) a=boundary+frame-last;
            float fraction=frame<last?(frame%step)/(float)step:0;
            int raw=sample(b,base,flags,value,a,rotation);
            int next=fraction==0?raw:sample(b,base,flags,value,a+1,rotation);
            if(rotation) {
                result[frame*2]=(short)raw/4096f+((short)next-(short)raw)/4096f*fraction;
                result[frame*2+1]=(short)(raw>>>16)/4096f+((short)(next>>>16)-(short)(raw>>>16))/4096f*fraction;
            } else result[frame]=raw/4096f+(next-raw)/4096f*fraction;
        }
        return result;
    }
    private static int sample(ByteBuffer b,int base,int flags,int value,int index,boolean rotation) {
        if((flags&0x20000000)!=0)return value;
        return !rotation && (flags&0x10000000)!=0?b.getShort(base+value+index*2):b.getInt(base+value+index*4);
    }
    void update(float time) {
        int frame=Math.min(Math.max(0,(int)(time*30)),frames-1);
        for(var track:tracks) {
            float[][] v=track.values;
            float sin=v[2][frame*2],cos=v[2][frame*2+1];
            float[] m=track.target.matrix.val;
            m[0]=cos*v[0][frame]; m[3]=-sin*v[1][frame]; m[6]=v[3][frame];
            m[1]=sin*v[0][frame]; m[4]=cos*v[1][frame]; m[7]=-v[4][frame];
        }
    }
}
