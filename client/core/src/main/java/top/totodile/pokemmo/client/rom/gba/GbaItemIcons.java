package top.totodile.pokemmo.client.rom.gba;

import java.io.IOException;
import java.nio.*;
import java.nio.file.Files;
import java.util.*;
import top.totodile.pokemmo.client.rom.*;

/** GbaRom br_2.Mh and MapWaterWheelBladeSpriteProvider: 3x3 4bpp tiles, LZ10 palette. */
public final class GbaItemIcons {
    public record Icon(byte[] tiles,byte[] palette) {
        public int[] rgba(boolean gray){
            int[] colors=new int[16],pixels=new int[24*24];
            for(int i=1;i<16;i++){
                int value=(palette[i*2]&255)|(palette[i*2+1]&255)<<8;
                int r=value&31,g=value>>5&31,b=value>>10&31;
                r=r<<3|r>>2;g=g<<3|g>>2;b=b<<3|b>>2;
                if(gray)r=g=b=(r+g+b)/3;
                colors[i]=r<<24|g<<16|b<<8|255;
            }
            for(int y=0;y<24;y++)for(int x=0;x<24;x++){
                int index=(y/8*3+x/8)*32+(y%8)*4+x%8/2;
                pixels[y*24+x]=colors[(tiles[index]>>((x&1)*4))&15];
            }
            return pixels;
        }
    }
    public static Map<Integer,Icon> load(RomInfo info)throws IOException{
        byte[] rom=Files.readAllBytes(info.path());var input=ByteBuffer.wrap(rom).order(ByteOrder.LITTLE_ENDIAN);
        byte[] signature=HexFormat.of().parseHex("40188018006802BC08470000");int match=-1;
        outer:for(int i=0;i<=rom.length-signature.length;i++){for(int j=0;j<signature.length;j++)if(rom[i+j]!=signature[j])continue outer;match=i;break;}
        if(match<0)throw new IOException("Missing GBA item icon table");
        int at=match+signature.length+(info.type()==RomType.FIRE_RED?4:0);
        int table=pointer(input,at),count=info.type()==RomType.FIRE_RED?375:377;
        Map<Integer,Icon> result=new HashMap<>();Map<Long,Icon> shared=new HashMap<>();
        for(int id=0;id<count;id++){
            int tiles=pointer(input,table+id*8),palette=pointer(input,table+id*8+4);
            long key=(long)tiles<<32|Integer.toUnsignedLong(palette);Icon icon=shared.get(key);
            if(icon==null){byte[] t=unpack(rom,tiles),p=unpack(rom,palette);if(t.length<288||p.length<32)throw new IOException("Invalid GBA item icon");icon=new Icon(t,p);shared.put(key,icon);}
            result.put(id,icon);
        }
        return Map.copyOf(result);
    }
    private static int pointer(ByteBuffer input,int at)throws IOException{
        if(at<0||at>input.limit()-4)throw new IOException("Invalid GBA icon pointer");
        int value=input.getInt(at)-0x08000000;
        if(value<0||value>=input.limit())throw new IOException("Invalid GBA icon address");return value;
    }
    private static byte[] unpack(byte[] rom,int at)throws IOException{
        if(at>rom.length-4||(rom[at]&255)!=0x10)throw new IOException("Invalid GBA icon LZ10 header");
        int size=(rom[at+1]&255)|(rom[at+2]&255)<<8|(rom[at+3]&255)<<16;
        if(size<1||size>4096)throw new IOException("Invalid GBA icon length");
        byte[] out=new byte[size];int pos=0;at+=4;
        try{while(pos<size){int flags=rom[at++]&255;for(int bit=128;bit!=0&&pos<size;bit>>=1){
            if((flags&bit)==0)out[pos++]=rom[at++];
            else{int token=(rom[at++]&255)<<8|(rom[at++]&255),length=(token>>>12)+3,back=(token&4095)+1;
                if(back>pos||pos+length>size)throw new IOException("Invalid GBA icon LZ10 reference");
                for(int i=0;i<length;i++){out[pos]=out[pos-back];pos++;}}
        }}}catch(IndexOutOfBoundsException e){throw new IOException("Truncated GBA icon",e);}return out;
    }
}
