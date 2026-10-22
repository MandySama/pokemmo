package top.totodile.pokemmo.client.rom.gba;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import top.totodile.pokemmo.client.rom.RomInfo;
import top.totodile.pokemmo.client.rom.RomType;

/** FireRed map banks/layouts, LZ10 tiles, BGR555 palettes and 4bpp metatiles.
 * Table signature follows the reference client's GbaRomRegistry (gz).
 * No extracted ROM assets are written to the installation directory.
 */
public final class FireRedMaps {
    public record Id(int bank, int map) {}
    public record Warp(int x, int y, int elevation, int targetWarp, Id target) {}
    public record Connection(int direction, int offset, Id target) {}
    public record MapData(Id id, int width, int height, int borderWidth, int borderHeight,
                          int[] blocks, int[] border, int primary, int secondary,
                          List<Warp> warps, List<Connection> connections) {
        public boolean contains(int x, int y) { return x >= 0 && y >= 0 && x < width && y < height; }
        public int block(int x, int y) {
            return contains(x,y) ? blocks[y*width+x]
                    : border[Math.floorMod(y,borderHeight)*borderWidth+Math.floorMod(x,borderWidth)];
        }
        public Warp warp(int x, int y) {
            return warps.stream().filter(w -> w.x==x && w.y==y).findFirst().orElse(null);
        }
    }
    public record Tiles(int count, int[][] pixels, int[] attributes) {
        public int behavior(int id) { return attributes[id] & 511; }
        public int layer(int id) { return attributes[id] >>> 29 & 3; }
    }
    private final byte[] rom;
    private final Map<Id,Integer> headers = new LinkedHashMap<>();
    private final Map<Id,MapData> maps = new HashMap<>();
    private final Map<Long,Tiles> tiles = new LinkedHashMap<>(16, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Long,Tiles> e) { return size()>8; }
    };
    public FireRedMaps(RomInfo info) throws IOException {
        if (info.type()!=RomType.FIRE_RED) throw new IllegalArgumentException("FireRed ROM required");
        rom=Files.readAllBytes(info.path());
        byte[] signature={(byte)0x80,0x18,0,0x68,(byte)0x89,0x0b,9,0x18,8,0x68,0x70,0x47};
        int table=-1;
        search: for(int p=0;p<=rom.length-signature.length-4;p+=2) {
            for(int j=0;j<signature.length;j++) if(rom[p+j]!=signature[j]) continue search;
            table=pointer(p+signature.length); break;
        }
        if(table<0) throw invalid("Map bank table not found");
        var banks=new ArrayList<Integer>();
        for(int p=table;banks.size()<100 && validPointer(p);p+=4) banks.add(pointer(p));
        if(banks.isEmpty() || banks.size()==100) throw invalid("Invalid map banks");
        for(int bank=0;bank<banks.size();bank++) {
            int begin=banks.get(bank), end=bank+1<banks.size()?banks.get(bank+1):table;
            int count=(end-begin)/4;
            if(end<begin || (end-begin)%4!=0 || count>256) throw invalid("Invalid map bank size");
            for(int map=0;map<count;map++) headers.put(new Id(bank,map),pointer(begin+map*4));
        }
    }
    public Set<Id> ids() { return Collections.unmodifiableSet(headers.keySet()); }
    public MapData map(Id id) { return maps.computeIfAbsent(id,this::readMap); }
    private MapData readMap(Id id) {
        Integer h=headers.get(id);
        if(h==null) throw invalid("Unknown map "+id);
        int layout=pointer(h), width=u32(layout), height=u32(layout+4);
        if(width<1 || height<1 || width>1024 || height>1024 || (long)width*height>262144)
            throw invalid("Invalid map dimensions");
        int bw=u8(layout+24), bh=u8(layout+25);
        if(bw<1 || bh<1 || bw>16 || bh>16) throw invalid("Invalid map border");
        var warps=new ArrayList<Warp>();
        if(validPointer(h+4)) {
            int events=pointer(h+4), count=u8(events+1);
            if(count>0) {
                int p=pointer(events+8);
                for(int i=0;i<count;i++,p+=8)
                    warps.add(new Warp(s16(p),s16(p+2),u8(p+4),u8(p+5),new Id(u8(p+7),u8(p+6))));
            }
        }
        var connections=new ArrayList<Connection>();
        if(validPointer(h+12)) {
            int c=pointer(h+12), count=u32(c);
            if(count<0 || count>16) throw invalid("Invalid map connections");
            if(count>0) {
                int p=pointer(c+4);
                for(int i=0;i<count;i++,p+=12) connections.add(new Connection(u32(p),u32(p+4),new Id(u8(p+8),u8(p+9))));
            }
        }
        return new MapData(id,width,height,bw,bh,shorts(pointer(layout+12),width*height),shorts(pointer(layout+8),bw*bh),
                pointer(layout+16),pointer(layout+20),List.copyOf(warps),List.copyOf(connections));
    }
    public Tiles tiles(MapData map) {
        long key=(long)map.primary<<32 | Integer.toUnsignedLong(map.secondary);
        return tiles.computeIfAbsent(key, ignored -> decodeTiles(map.primary,map.secondary));
    }
    private Tiles decodeTiles(int primary,int secondary) {
        int first=count(primary), second=count(secondary), count=first+second;
        if(first!=640 || count>1024) throw invalid("Unsupported FireRed tileset size");
        byte[] a=graphics(primary,first*32), b=graphics(secondary,(1024-first)*32);
        int[] palette=new int[256];
        for(int i=0;i<256;i++) {
            int source=pointer((i<96?primary:secondary)+8), c=u16(source+i*2);
            int r=c&31, g=c>>5&31, blue=c>>10&31;
            palette[i]=((r<<3|r>>2)<<24)|((g<<3|g>>2)<<16)|((blue<<3|blue>>2)<<8)|255;
        }
        int[][] pixels={new int[count*256],new int[count*256]};
        int[] attributes=new int[count];
        for(int id=0;id<count;id++) {
            int source=id<first?primary:secondary, local=id<first?id:id-first;
            attributes[id]=u32(pointer(source+20)+local*4);
            int metatile=pointer(source+12)+local*16;
            for(int layer=0;layer<2;layer++) for(int q=0;q<4;q++) {
                int entry=u16(metatile+layer*8+q*2), tile=entry&1023, pal=entry>>>12;
                byte[] graphics=tile<first?a:b;
                int tileOffset=(tile<first?tile:tile-first)*32;
                // Some maps reference VRAM animation slots absent from the initial compressed data.
                if(tileOffset+32>graphics.length) continue;
                for(int y=0;y<8;y++) for(int x=0;x<8;x++) {
                    int sx=(entry&1024)!=0?7-x:x, sy=(entry&2048)!=0?7-y:y;
                    int packed=graphics[tileOffset+sy*4+sx/2]&255, color=packed>>((sx&1)*4)&15;
                    pixels[layer][id*256+(q/2*8+y)*16+q%2*8+x]=color==0?0:palette[pal*16+color];
                }
            }
        }
        return new Tiles(count,pixels,attributes);
    }
    private int count(int tileset) {
        int bytes=pointer(tileset+20)-pointer(tileset+12);
        if(bytes<=0 || bytes%16!=0 || bytes/16>640) throw invalid("Invalid metatile table");
        return bytes/16;
    }
    private byte[] graphics(int tileset,int rawLength) {
        int p=pointer(tileset+4);
        if(u8(tileset)==0) { range(p,rawLength); return Arrays.copyOfRange(rom,p,p+rawLength); }
        if(u8(p)!=0x10) throw invalid("Invalid LZ10 header");
        int size=u32(p)>>>8;
        if(size<1 || size>32768) throw invalid("Invalid tile graphics length");
        byte[] result=new byte[size]; int out=0; p+=4;
        while(out<size) {
            int flags=u8(p++);
            for(int bit=128;bit!=0 && out<size;bit>>=1) {
                if((flags&bit)==0) result[out++]=(byte)u8(p++);
                else {
                    int token=u8(p++)<<8|u8(p++), length=(token>>>12)+3, back=(token&4095)+1;
                    if(back>out || out+length>size) throw invalid("Invalid LZ10 back reference");
                    for(int i=0;i<length;i++) { result[out]=result[out-back]; out++; }
                }
            }
        }
        return result;
    }
    private int[] shorts(int p,int count) {
        range(p,count*2); int[] result=new int[count];
        for(int i=0;i<count;i++) result[i]=u16(p+i*2);
        return result;
    }
    private void range(int p,int size) {
        if(p<0 || size<0 || p>rom.length-size) throw invalid("ROM read outside file");
    }
    private boolean validPointer(int p) {
        if(p<0 || p>rom.length-4) return false;
        long value=Integer.toUnsignedLong(u32(p));
        return value>=0x08000000L && value<0x08000000L+rom.length;
    }
    private int pointer(int p) {
        if(!validPointer(p)) throw invalid("Invalid ROM pointer at "+Integer.toHexString(p));
        return u32(p)-0x08000000;
    }
    private int u8(int p) { range(p,1); return rom[p]&255; }
    private int u16(int p) { return u8(p)|u8(p+1)<<8; }
    private int s16(int p) { return (short)u16(p); }
    private int u32(int p) { return u16(p)|u16(p+2)<<16; }
    private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}
