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

    /** 战斗图集保留所有动作的实际像素边界，并移除四周无用透明画布。 */
    public static Frames loadBattle(Path rom, int species, boolean shiny, boolean back) throws IOException {
        for (int canvas : new int[]{192, 256, 384, 512}) {
            Frames frames = load(rom, species, shiny, back, canvas);
            int left = canvas, top = canvas, right = 0, bottom = 0;
            for (BufferedImage image : frames.images()) for (int y = 0; y < canvas; y++)
                for (int x = 0; x < canvas; x++) if ((image.getRGB(x, y) >>> 24) != 0) {
                    left = Math.min(left, x); top = Math.min(top, y);
                    right = Math.max(right, x + 1); bottom = Math.max(bottom, y + 1);
                }
            if (left <= 1 || top <= 1 || right >= canvas - 1 || bottom >= canvas - 1) continue;
            if (right <= left || bottom <= top) throw new IOException("Empty battle animation");
            int width = right - left + 4, height = bottom - top + 4;
            List<BufferedImage> cropped = new ArrayList<>();
            for (BufferedImage image : frames.images()) {
                var target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                var graphics = target.createGraphics();
                graphics.drawImage(image, 0, 0, width, height, left-2, top-2, right+2, bottom+2, null);
                graphics.dispose(); cropped.add(target);
            }
            return new Frames(List.copyOf(cropped), frames.ticksPerFrame());
        }
        throw new IOException("Battle animation exceeds supported canvas");
    }

    public static Frames load(Path rom, int species, boolean shiny, boolean back) throws IOException {
        return load(rom, species, shiny, back, 96);
    }

    /** 资料窗口动画：正／背面帧、共用图集格尺寸与原 96 画布中心在格内的锚点。 */
    public record SummaryFrames(List<BufferedImage> front, List<BufferedImage> back,
                                int cellWidth, int cellHeight, int anchorX, int anchorY) { }

    private static void accumulate(BufferedImage image,int[] box) {
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)
            if((image.getRGB(x,y)>>>24)!=0){
                box[0]=Math.min(box[0],x);box[1]=Math.min(box[1],y);
                box[2]=Math.max(box[2],x+1);box[3]=Math.max(box[3],y+1);
            }
    }

    private static List<BufferedImage> crop(List<BufferedImage> frames,int l,int t,int w,int h) {
        List<BufferedImage> result=new ArrayList<>(frames.size());
        for(var image : frames){
            var target=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            var graphics=target.createGraphics();
            graphics.drawImage(image,0,0,w,h,l,t,l+w,t+h,null);
            graphics.dispose();
            result.add(target);
        }
        return List.copyOf(result);
    }

    /**
     * 按内容边界合成资料窗口用的正／背面动画。BW 战斗动画把脚底锚在画布中心线上、身体向上
     * 延伸，固定 96 画布会截掉高个体的头部；这里从 96 起步、内容触边时换更大画布重渲染，
     * 正／背面帧按并集边界裁剪成同尺寸同锚点（翻转切换时才不会跳动），并保留原画布中心在
     * 裁剪结果内的位置，供显示端把脚底摆回原位。
     */
    public static SummaryFrames loadSummary(Path rom,int species,boolean shiny) throws IOException {
        for(int canvas : new int[]{96,192,256,384,512}){
            List<BufferedImage> front=load(rom,species,shiny,false,canvas).images();
            List<BufferedImage> back=load(rom,species,shiny,true,canvas).images();
            int[] box={canvas,canvas,0,0};
            for(var image : front)accumulate(image,box);
            for(var image : back)accumulate(image,box);
            if(box[0]<=1||box[1]<=1||box[2]>=canvas-1||box[3]>=canvas-1)continue;
            if(box[2]<=box[0]||box[3]<=box[1])throw new IOException("Empty summary animation");
            int l=box[0]-2,t=box[1]-2,w=box[2]-box[0]+4,h=box[3]-box[1]+4;
            return new SummaryFrames(crop(front,l,t,w,h),crop(back,l,t,w,h),
                    w,h,canvas/2-l,canvas/2-t);
        }
        throw new IOException("Summary animation exceeds supported canvas");
    }

    public static Frames load(Path rom,int species,boolean shiny,boolean back,int canvas) throws IOException {
        if(canvas != 96 && canvas != 192 && canvas != 256 && canvas != 384 && canvas != 512) throw new IllegalArgumentException("Invalid sprite canvas");
        if (species > 649) return ExtendedPokemonSprites.load(species, shiny, back, canvas);
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
            Frame root=at(outer,time);var image=new BufferedImage(canvas,canvas,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();
            var layout=layouts.get(root.cell());
            int elapsed=0,sectionStart=0,lastCell=-1;
            for(Frame section:outer){if(section.cell()!=lastCell)sectionStart=elapsed;if(time<elapsed+section.duration())break;elapsed+=section.duration();lastCell=section.cell();}
            for(int p=layout.size()-1;p>=0;p--){var bone=layout.get(p);Frame f=at(parts.get(bone[0]),time-sectionStart);
                var tile=tiles.get(f.cell());if(tile==null){tile=PokedexData.renderCell(graphics,cells,palette,f.cell(),canvas);tiles.put(f.cell(),tile);}
                var transform=new AffineTransform();transform.translate(canvas/2+root.x()+bone[1]+f.x(),canvas/2+root.y()+bone[2]+f.y());transform.scale(f.sx(),f.sy());transform.rotate(f.angle());transform.translate(-canvas/2,-canvas/2);g.drawImage(tile,transform,null);
            }
            g.dispose();output.add(image);
        }
        return new Frames(List.copyOf(output),2);
    }
}
