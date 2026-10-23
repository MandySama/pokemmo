package top.totodile.pokemmo.client.rom.nds.bw;

import java.io.IOException;
import java.nio.*;
import java.util.*;

/** Adapted from reference NitroAnimationResource / NdsMeshDescriptorTable: ROM poses only, no pixel resampling. */
public final class BwPokemonAnimation {
    private record Frame(int cell,int duration,int angle,double scaleX,double scaleY,int x,int y) {}
    private record Sequence(List<Frame> frames,int duration) {
        Frame at(int tick){tick=Math.floorMod(tick,duration);for(var frame:frames){if(tick<frame.duration)return frame;tick-=frame.duration;}return frames.get(0);}
        int partTime(int tick){
            tick=Math.floorMod(tick,duration);int elapsed=0,previous=-1;
            for(var frame:frames){if(previous!=frame.cell)elapsed=0;if(tick<frame.duration)return elapsed+tick;elapsed+=frame.duration;tick-=frame.duration;previous=frame.cell;}
            return 0;
        }
    }
    private record Part(int sequence,int x,int y) {}
    public record Cell(BwPokemonSprites.Pixels pixels,int originX,int originY) {}
    public record Draw(int cell,int x,int y,float scaleX,float scaleY,float rotation,boolean flipX,boolean flipY) {}
    private final List<Sequence> cellsAnimation,multiAnimation;
    private final List<List<Part>> groups;
    private final byte[] tiles,palette,cells;
    private final Map<Integer,Cell> cache=new HashMap<>();

    BwPokemonAnimation(byte[] tiles,byte[] palette,byte[] cells,byte[] nanr,byte[] nmcr,byte[] nmar)throws IOException {
        this.tiles=tiles;this.palette=palette;this.cells=cells;
        cellsAnimation=sequences(nanr,0x4e414e52);multiAnimation=sequences(nmar,0x4e4d4152);
        try{
            ByteBuffer b=buffer(nmcr,0x4e4d4352);int count=u16(b,24),position=44+count*8;
            groups=new ArrayList<>();
            for(int i=0;i<count;i++){
                int parts=u16(b,44+i*8);var group=new ArrayList<Part>();
                for(int p=0;p<parts;p++,position+=8)group.add(new Part(u16(b,position),b.getShort(position+2),b.getShort(position+4)));
                groups.add(List.copyOf(group));
            }
            if(groups.isEmpty()||cellsAnimation.isEmpty()||multiAnimation.isEmpty())throw new IOException("Empty Pokémon animation");
        }catch(IndexOutOfBoundsException error){throw new IOException("Truncated Pokémon multicell resource",error);}
    }
    public int durationTicks(){return multiAnimation.get(0).duration;}
    public int cellCount(){return BwPokedex.u16(cells,24);}
    public List<Draw> pose(int ticks)throws IOException {
        var animation=multiAnimation.get(0);var parent=animation.at(ticks);int time=animation.partTime(ticks);
        if(parent.cell<0||parent.cell>=groups.size())throw new IOException("Invalid Pokémon multicell index");
        var draws=new ArrayList<Draw>();var group=groups.get(parent.cell);
        // The reference draws multicell parts back to front and keeps their shared ROM anchor fixed.
        for(int i=group.size()-1;i>=0;i--){
            var part=group.get(i);if(part.sequence>=cellsAnimation.size())throw new IOException("Invalid Pokémon animation index");
            var frame=cellsAnimation.get(part.sequence).at(time);
            draws.add(new Draw(frame.cell,parent.x+part.x+frame.x,parent.y+part.y+frame.y,
                    (float)Math.abs(frame.scaleX),(float)Math.abs(frame.scaleY),frame.angle/65536.0f*-360,
                    frame.scaleX<0,frame.scaleY<0));
        }
        return List.copyOf(draws);
    }
    public Cell cell(int index)throws IOException {
        Cell cell=cache.get(index);if(cell!=null)return cell;
        try{
            var b=buffer(cells,0x4e434552);int count=u16(b,24),size=u16(b,26)==1?16:8,start=24+b.getInt(28);
            if(index<0||index>=count)throw new IOException("Invalid Pokémon cell index");
            int entry=start+index*size,objects=u16(b,entry),oam=start+count*size+b.getInt(entry+4);
            int left=512,top=256,right=-512,bottom=-256;
            int[][] widths={{8,16,32,64},{16,32,32,64},{8,8,16,32}},heights={{8,16,32,64},{8,8,16,32},{16,32,32,64}};
            for(int i=0;i<objects;i++){
                int a=u16(b,oam+i*6),attr=u16(b,oam+i*6+2),shape=a>>14,dimension=attr>>14;if(shape>=3)continue;
                int x=attr&511;if(x>=256)x-=512;int y=(byte)a,multiplier=(a&0x300)==0x300?2:1;
                left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x+widths[shape][dimension]*multiplier);bottom=Math.max(bottom,y+heights[shape][dimension]*multiplier);
            }
            if(objects==0){left=top=0;right=bottom=1;}
            var pixels=BwPokemonSprites.composeCell(tiles,palette,cells,index,right-left,bottom-top,-left,-top);
            cell=new Cell(pixels,-left,-top);cache.put(index,cell);return cell;
        }catch(IndexOutOfBoundsException error){throw new IOException("Truncated Pokémon cell resource",error);}
    }
    private static List<Sequence> sequences(byte[] bytes,int magic)throws IOException {
        try{
            var b=buffer(bytes,magic);int count=u16(b,24),frames=b.getInt(32)+24,data=b.getInt(36)+24;
            var sequences=new ArrayList<Sequence>();
            for(int i=0;i<count;i++){
                int start=48+i*16,n=b.getInt(start),type=u16(b,start+4),offset=b.getInt(start+12),duration=0;
                if(n<=0||n>65535||type>2)throw new IOException("Invalid Pokémon animation sequence");
                var values=new ArrayList<Frame>();
                for(int j=0;j<n;j++){
                    int f=frames+offset+j*8,p=data+b.getInt(f),length=u16(b,f+4),cell=u16(b,p),angle=0,x=0,y=0;
                    double scaleX=1,scaleY=1;
                    if(type==1){angle=u16(b,p+2);scaleX=b.getInt(p+4)/4096.0;scaleY=b.getInt(p+8)/4096.0;x=b.getShort(p+12);y=b.getShort(p+14);}
                    else if(type==2){x=b.getShort(p+4);y=b.getShort(p+6);}
                    values.add(new Frame(cell,length,angle,scaleX,scaleY,x,y));duration+=length;
                }
                if(duration<=0)throw new IOException("Empty Pokémon animation duration");
                sequences.add(new Sequence(List.copyOf(values),duration));
            }
            return List.copyOf(sequences);
        }catch(IndexOutOfBoundsException error){throw new IOException("Truncated Pokémon animation resource",error);}
    }
    private static ByteBuffer buffer(byte[] bytes,int magic)throws IOException {
        if(bytes.length<44)throw new IOException("Missing Pokémon animation resource");
        var b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);if(b.getInt(0)!=magic)throw new IOException("Invalid Pokémon animation resource");return b;
    }
    private static int u16(ByteBuffer b,int p){return Short.toUnsignedInt(b.getShort(p));}
}
