package top.totodile.pokemmo.client.rom.nds.bw;

import java.io.IOException;
import java.nio.*;
import java.util.*;
import top.totodile.pokemmo.client.rom.RomReader;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;

/** BW icon and battle sprite NCGR/NCLR/NCER composition, following loadSprites / NitroOamData.oQ. */
public final class BwPokemonSprites {
    public record Pixels(int width,int height,int[] rgba) {}
    private final BwPokedex dex;
    private final NdsArchive icons;
    private NdsArchive battle;
    private final byte[] iconCells,iconColors,iconPaletteIds;
    private byte[] battleCells;

    public BwPokemonSprites(BwPokedex dex) throws IOException {
        this.dex=dex;icons=dex.archive("a/0/0/7");
        iconCells=icons.read(2);iconColors=icons.read(0);
        byte[] h=RomReader.read(dex.rom(),0,0x40);
        byte[] arm=decompressArm(RomReader.read(dex.rom(),i32(h,0x20),i32(h,0x2c)));
        byte[] signature={0,17,17,17,0,0,0,0,34,34,17,17,0,17,34,34};
        int table=-1;
        for(int i=0;i<=arm.length-712;i++) {
            boolean match=true;
            for(int j=0;j<signature.length;j++)if(arm[i+j]!=signature[j]){match=false;break;}
            if(match){table=i;break;}
        }
        if(table<0)throw new IOException("Missing BW icon palette table");
        iconPaletteIds=Arrays.copyOfRange(arm,table,table+712);
    }
    public Pixels icon(int id) throws IOException {
        dex.get(id);
        return compose(icons.read(id*2+7),iconColors,iconCells,0,36,iconPaletteIds[id]&15);
    }
    public Pixels front(int id) throws IOException {
        dex.get(id);
        if(battle==null){battle=dex.archive("a/0/0/4");battleCells=battle.read(4);}
        return compose(battle.read(id*20),battle.read(id*20+18),battleCells,0,96,-1);
    }
    private static Pixels compose(byte[] tiles,byte[] palette,byte[] cells,int frame,int size,int paletteOverride) throws IOException {
        try {
            if(i32(tiles,0)!=0x4e434752||i32(palette,0)!=0x4e434c52||i32(cells,0)!=0x4e434552)
                throw new IOException("Invalid Nitro Pokémon sprite");
            int depth=i32(tiles,28),dataOffset=24+i32(tiles,44),tileLength=i32(tiles,40);
            if(depth!=3&&depth!=4)throw new IOException("Unsupported NCGR depth "+depth);
            int paletteStart=24+i32(palette,36),paletteSize=i32(palette,32)/2;
            int[] colors=new int[paletteSize];
            for(int i=0;i<colors.length;i++) {
                int c=u16(palette,paletteStart+i*2);
                colors[i]=((c&31)*255/31)<<24|((c>>5&31)*255/31)<<16|((c>>10&31)*255/31)<<8|255;
            }
            int count=u16(cells,24),cellSize=u16(cells,26)==1?16:8;
            if(frame>=count)throw new IOException("Missing NCER frame");
            int cellStart=24+i32(cells,28),cell=cellStart+frame*cellSize;
            int objects=u16(cells,cell),oam=cellStart+count*cellSize+i32(cells,cell+4),mapping=i32(cells,32)&3;
            var order=new ArrayList<Integer>();for(int i=0;i<objects;i++)order.add(oam+i*6);
            order.sort(Comparator.comparingInt((Integer p)->-(u16(cells,p+4)>>10&3)));
            int[] output=new int[size*size];
            int[][] widths={{8,16,32,64},{16,32,32,64},{8,8,16,32}};
            int[][] heights={{8,16,32,64},{8,8,16,32},{16,32,32,64}};
            for(int p:order) {
                int a=u16(cells,p),b=u16(cells,p+2),c=u16(cells,p+4),shape=a>>14,dimension=b>>14;
                if(shape>=3)continue;
                int w=widths[shape][dimension],h=heights[shape][dimension];
                int x=b&511;if(x>=256)x-=512;
                int y=(byte)a,base=((c&1023)<<mapping)*32;
                boolean flipX=(a&256)==0&&(b&4096)!=0,flipY=(a&256)==0&&(b&8192)!=0;
                int bank=paletteOverride>=0?paletteOverride:c>>12;
                if(bank*(depth==3?16:256)>=colors.length)bank=0;
                for(int py=0;py<h;py++)for(int px=0;px<w;px++) {
                    int sx=flipX?w-1-px:px,sy=flipY?h-1-py:py;
                    int pixel=((sy/8)*(w/8)+sx/8)*64+(sy%8)*8+sx%8;
                    int index=base+(depth==3?pixel/2:pixel);
                    if(index>=tileLength)continue;
                    int value=tiles[dataOffset+index]&255;
                    if(depth==3)value=(value>>((pixel&1)*4))&15;
                    if(value==0)continue;
                    int dx=size/2+x+px,dy=size/2+y+py;
                    if(dx>=0&&dy>=0&&dx<size&&dy<size)output[dy*size+dx]=colors[bank*(depth==3?16:256)+value];
                }
            }
            return new Pixels(size,size,output);
        }catch(IndexOutOfBoundsException error){throw new IOException("Truncated Pokémon sprite",error);}
    }
    /** Same backwards LZ as reference BlzDecompressor (the palette indices live in ARM9). */
    private static byte[] decompressArm(byte[] input) throws IOException {
        try {
            int n=input.length,extra=i32(input,n-4);
            if(extra==0)return input;
            int header=input[n-5]&255,packed=i32(input,n-8)&0xffffff;
            if(header<8||header>11||packed<header||packed>n||extra<0||extra>16*1024*1024-n)
                throw new IOException("Invalid ARM9 compression footer");
            int start=n-packed,source=n-header,destination=n+extra;
            byte[] out=Arrays.copyOf(input,destination);
            while(destination>start) {
                int flags=out[--source]&255;
                for(int bit=7;bit>=0&&destination>start;bit--) {
                    if((flags&(1<<bit))==0)out[--destination]=out[--source];
                    else {
                        int token=(out[--source]&255)<<8;token|=out[--source]&255;
                        int length=(token>>>12)+3,distance=(token&4095)+3;
                        while(length-->0&&destination>start){destination--;out[destination]=out[destination+distance];}
                    }
                }
            }
            return out;
        }catch(IndexOutOfBoundsException error){throw new IOException("Truncated ARM9",error);}
    }
    private static int u16(byte[] b,int p){return BwPokedex.u16(b,p);}
    private static int i32(byte[] b,int p){return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt(p);}
}
