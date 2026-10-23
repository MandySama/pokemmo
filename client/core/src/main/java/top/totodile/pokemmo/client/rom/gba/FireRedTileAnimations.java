package top.totodile.pokemmo.client.rom.gba;

import java.util.*;

/** 参考 GbaAnimatedTileRegistry / GbaAnimatedTileData 的 GBA 动画槽位、帧表及相位。 */
final class FireRedTileAnimations {
    private record Definition(int tileset,int frames,int first,int count,int offset,int rotation) {
        Definition(int tileset,int frames,int first,int count,int offset) {this(tileset,frames,first,count,offset,0);}
    }
    private static final Definition[] FIRE_RED={
        new Definition(0,5,508,4,-141),new Definition(0,8,464,18,-61),
        new Definition(0,8,416,48,-101),new Definition(45,4,976,8,-53),
        new Definition(28,2,880,7,-33),new Definition(57,4,896,8,-33),
        new Definition(7,5,744,8,-53)
    };
    private static final List<Definition> EMERALD=emeraldDefinitions();
    private static List<Definition> emeraldDefinitions() {
        var result=new ArrayList<>(List.of(
                new Definition(0,4,508,4,203),new Definition(0,8,432,30,243),
                new Definition(0,8,464,10,283),new Definition(0,4,496,6,323),
                new Definition(2,2,960,4,2043),new Definition(3,4,682,6,2175),
                new Definition(4,4,736,4,2251),new Definition(6,4,800,4,1479),
                new Definition(6,4,672,4,1999),new Definition(6,4,804,4,1479,2),
                new Definition(12,4,976,30,1275),new Definition(12,8,1008,8,1355),
                new Definition(13,8,752,96,2375),new Definition(14,4,730,6,1779),
                new Definition(15,4,730,6,1735),new Definition(16,2,496,4,2911),
                new Definition(30,2,1008,9,2007),new Definition(44,4,1008,4,1135),
                new Definition(46,3,1008,12,1999),new Definition(46,3,976,20,2007),
                new Definition(51,2,656,16,2011),new Definition(58,2,992,4,2007),
                new Definition(58,4,1016,1,1967),new Definition(65,3,663,8,2047),
                new Definition(65,3,647,8,2091)));
        for(int i=0;i<8;i++) {
            result.add(new Definition(2,8,640+i*4,4,2003,i));
            result.add(new Definition(11,8,736+i*4,4,1755,i));
        }
        for(int i=0;i<16;i++)result.add(new Definition(5,16,608+i*4,4,1735,i));
        return List.copyOf(result);
    }
    private final byte[] rom;
    private final int table;
    private final boolean emerald;
    FireRedTileAnimations(byte[] rom,boolean emerald) {
        this.rom=rom;
        this.emerald=emerald;
        // GbaRomRegistry.XB：模式后紧接的指针指向 24 字节 tileset 表。
        byte[] pattern=emerald?new byte[]{(byte)0xdc,5,(byte)0xdd,5,30,0,0,0,30,0,0,0}
                :new byte[]{(byte)0xdd,5,0x72,4,40,0,0,0,40,0,0,0};
        int found=-1;
        search:for(int p=0;p<=rom.length-24;p+=2) {
            for(int i=0;i<pattern.length;i++)if(rom[p+i]!=pattern[i])continue search;
            if(rom[p+15]==8 && rom[p+19]==8){found=pointer(p+20);break;}
        }
        if(found<0)throw new IllegalArgumentException("GBA tileset table not found");
        table=found;
    }
    Map<Integer,byte[][]> frames(int primary,int secondary) {
        var result=new HashMap<Integer,byte[][]>();
        for(Definition definition:emerald?EMERALD:Arrays.asList(FIRE_RED)) {
            // The reference accounts for an eight-byte gap after Emerald tileset 57.
            int tileset=table+definition.tileset*24+(emerald && definition.tileset>57?8:0);
            if(tileset!=primary && tileset!=secondary)continue;
            int callback=pointer(tileset+(emerald?20:16));
            int frameTable=pointer(callback+definition.offset);
            for(int tile=0;tile<definition.count;tile++) {
                byte[][] frames=new byte[definition.frames][];
                for(int frame=0;frame<frames.length;frame++) {
                    int rotated=Math.floorMod(frame-definition.rotation,definition.frames);
                    int address=pointer(frameTable+rotated*4)+tile*32;
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
        if(address<0 || address>=rom.length)throw new IllegalArgumentException("Invalid GBA animation pointer");
        return (int)address;
    }
    private void range(int position,int size) {
        if(position<0 || position>rom.length-size)throw new IllegalArgumentException("GBA animation outside ROM");
    }
}
