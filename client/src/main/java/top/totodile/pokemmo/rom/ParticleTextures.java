package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;

/** 读取战斗粒子归档内的线性索引纹理与透明度。 */
public final class ParticleTextures {
    private ParticleTextures() { }
    public static List<BufferedImage> load(Path rom,int id) throws IOException {
        ByteBuffer b=new NdsArchive(rom,"/a/0/0/6").file(id);if(b.getInt(0)!=1397768480)throw new IOException("Invalid particle archive");
        int count=b.getShort(10)&65535,offset=b.getInt(24);List<BufferedImage> result=new ArrayList<>();
        for(int i=0;i<count;i++){
            int flags=b.getShort(offset+4)&65535,format=flags&7,w=8<<((flags>>4)&7),h=8<<((flags>>8)&7);
            int length=b.getInt(offset+8),paletteLength=b.getShort(offset+16)&65535,pixels=offset+32,palette=pixels+length;
            var image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<h;y++)for(int x=0;x<w;x++){
                int p=y*w+x,index,alpha=255,value=b.get(pixels+(format==3?p/2:p))&255;
                if(format==3){index=(value>>((p&1)*4))&15;if(index==0)alpha=0;}
                else if(format==1){index=value&31;alpha=(value>>5)*255/7;}
                else if(format==6){index=value&7;alpha=(value>>3)*255/31;}
                else continue;
                if(index*2>=paletteLength)continue;int c=b.getShort(palette+index*2)&65535;
                image.setRGB(x,y,alpha<<24|(c&31)*255/31<<16|(c>>5&31)*255/31<<8|(c>>10&31)*255/31);
            }
            result.add(image);offset=palette+paletteLength;
        }
        return List.copyOf(result);
    }
}
