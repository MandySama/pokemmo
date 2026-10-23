package top.totodile.pokemmo.client.rom.gba;

import java.util.*;

/** 参考 GbaAnimatedTileRegistry / GbaAnimatedTileData 的火红动画槽位及帧指针表。 */
final class FireRedTileAnimations {
    private record Definition(int tileset,int frames,int first,int count,int offset) {}
    private static final Definition[] DEFINITIONS={
        new Definition(0,5,508,4,-141),new Definition(0,8,464,18,-61),
        new Definition(0,8,416,48,-101),new Definition(45,4,976,8,-53),
        new Definition(28,2,880,7,-33),new Definition(57,4,896,8,-33),
        new Definition(7,5,744,8,-53)
    };
    private final byte[] rom;
    private final int table;
    FireRedTileAnimations(byte[] rom) {
        this.rom=rom;
        // GbaRomRegistry.XB：模式后紧接的指针指向 24 字节 tileset 表。
        byte[] pattern={(byte)0xdd,5,0x72,4,40,0,0,0,40,0,0,0};
        int found=-1;
        search:for(int p=0;p<=rom.length-24;p+=2) {
            for(int i=0;i<pattern.length;i++)if(rom[p+i]!=pattern[i])continue search;
            if(rom[p+15]==8 && rom[p+19]==8){found=pointer(p+20);break;}
        }
        if(found<0)throw new IllegalArgumentException("FireRed tileset table not found");
        table=found;
    }
    Map<Integer,byte[][]> frames(int primary,int secondary) {
        var result=new HashMap<Integer,byte[][]>();
        for(Definition definition:DEFINITIONS) {
            int tileset=table+definition.tileset*24;
            if(tileset!=primary && tileset!=secondary)continue;
            int callback=pointer(tileset+16);
            int frameTable=pointer(callback+definition.offset);
            for(int tile=0;tile<definition.count;tile++) {
                byte[][] frames=new byte[definition.frames][];
                for(int frame=0;frame<frames.length;frame++) {
                    int address=pointer(frameTable+frame*4)+tile*32;
                    range(address,32);frames[frame]=Arrays.copyOfRange(rom,address,address+32);
                }
                result.put(definition.first+tile,frames);
            }
        }
        return result;
    }
    private int pointer(int position) {
        range(position,4);
        long value=0;
        for(int i=0;i<4;i++)value|=(long)(rom[position+i]&255)<<(i*8);
        long address=value-0x08000000L;
        if(address<0 || address>=rom.length)throw new IllegalArgumentException("Invalid FireRed animation pointer");
        return (int)address;
    }
    private void range(int position,int size) {
        if(position<0 || position>rom.length-size)throw new IllegalArgumentException("FireRed animation outside ROM");
    }
}
