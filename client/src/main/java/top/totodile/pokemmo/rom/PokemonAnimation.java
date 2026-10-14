package top.totodile.pokemmo.rom;

import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;

/** 按 NANR/NMAR 时间轴合成多部件精灵，色板由个体闪光状态选择。 */
public final class PokemonAnimation {
    public record Frames(List<BufferedImage> images,int ticksPerFrame) { }
    private record Frame(int duration,int cell,double angle,double sx,double sy,int x,int y) { }
    private static int u(ByteBuffer b,int o){return b.getShort(o)&65535;}
    private static List<List<Frame>> sequences(ByteBuffer b) throws IOException {
        List<List<Frame>> result=new ArrayList<>();int n=u(b,24),table=24+b.getInt(32),values=24+b.getInt(36);
        if(n>256)throw new IOException("Invalid sprite sequences");
        for(int i=0;i<n;i++){
            int at=48+i*16,count=u(b,at),kind=u(b,at+2),base=table+b.getInt(at+12);
            // Nitro stores the frame count as a 16-bit value followed by the loop start.
            kind=u(b,at+4);List<Frame> frames=new ArrayList<>();
            if(count>2048)throw new IOException("Invalid sprite frames");
            for(int j=0;j<count;j++){
                int entry=base+j*8,v=values+b.getInt(entry),duration=u(b,entry+4);
                double angle=0,sx=1,sy=1;int x=0,y=0;
                if(kind==1){angle=u(b,v+2)*Math.PI*2/65536;sx=b.getInt(v+4)/4096.0;sy=b.getInt(v+8)/4096.0;x=b.getShort(v+12);y=b.getShort(v+14);}
                if(kind==2){x=b.getShort(v+4);y=b.getShort(v+6);}
                frames.add(new Frame(Math.max(1,duration),u(b,v),angle,sx,sy,x,y));
            }
            result.add(frames);
        }
        return result;
    }
    private static Frame at(List<Frame> frames,int time){int length=frames.stream().mapToInt(Frame::duration).sum();time=Math.floorMod(time,length);for(Frame f:frames){if(time<f.duration())return f;time-=f.duration();}return frames.getLast();}

    public static Frames load(Path rom,int species,boolean shiny) throws IOException {
        return load(rom, species, shiny, false);
    }

    public static Frames load(Path rom, int species, boolean shiny, boolean back) throws IOException {
        var archive=new NdsArchive(rom,"/a/0/0/4");int paletteBase=species*20,base=paletteBase+(back?9:0);
        ByteBuffer graphics=NdsCompression.decode(archive.file(base+2)),cells=NdsCompression.decode(archive.file(base+4));
        ByteBuffer palette=archive.file(paletteBase+(shiny?19:18));
        var parts=sequences(NdsCompression.decode(archive.file(base+5)));
        var outer=sequences(NdsCompression.decode(archive.file(base+7))).getFirst();
        ByteBuffer multi=archive.file(base+6);int count=u(multi,24),offset=44+count*8;
        List<List<int[]>> layouts=new ArrayList<>();
        for(int i=0;i<count;i++){
            int entries=u(multi,44+i*8);List<int[]> layout=new ArrayList<>();
            for(int j=0;j<entries;j++){layout.add(new int[]{u(multi,offset),multi.getShort(offset+2),multi.getShort(offset+4)});offset+=8;}
            layouts.add(layout);
        }
        Map<Integer,BufferedImage> tiles=new HashMap<>();int length=outer.stream().mapToInt(Frame::duration).sum();
        if(length>1200)throw new IOException("Sprite timeline too long");
        List<BufferedImage> output=new ArrayList<>();
        for(int time=0;time<length;time+=2){
            Frame root=at(outer,time);var image=new BufferedImage(96,96,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();
            var layout=layouts.get(root.cell());
            int elapsed=0,sectionStart=0,lastCell=-1;
            for(Frame section:outer){if(section.cell()!=lastCell)sectionStart=elapsed;if(time<elapsed+section.duration())break;elapsed+=section.duration();lastCell=section.cell();}
            for(int p=layout.size()-1;p>=0;p--){var bone=layout.get(p);Frame f=at(parts.get(bone[0]),time-sectionStart);
                var tile=tiles.get(f.cell());if(tile==null){tile=PokedexData.renderCell(graphics,cells,palette,f.cell());tiles.put(f.cell(),tile);}
                var transform=new AffineTransform();transform.translate(48+root.x()+bone[1]+f.x(),48+root.y()+bone[2]+f.y());transform.scale(f.sx(),f.sy());transform.rotate(f.angle());transform.translate(-48,-48);g.drawImage(tile,transform,null);
            }
            g.dispose();output.add(image);
        }
        return new Frames(List.copyOf(output),2);
    }
}
