package top.totodile.pokemmo.client.rom.gba;

import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokemonSprites;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/** GbaRomRegistry ft/yY/y10/T70 and MapTallGrassOverlayProvider.KN (64px tiled portrait). */
public record GbaTrainer(int id,int portrait,int trainerClass,String type,String name,BwPokemonSprites.Pixels pixels) {
    public static GbaTrainer load(RomInfo info,int id,int model)throws IOException{
        byte[] rom=Files.readAllBytes(info.path());
        boolean kanto=info.type()==RomType.FIRE_RED;
        int tables=find(rom,"000E06B018BC9846A146F0BC02BC0847");
        int palettes=pointer(rom,tables),pictures=pointer(rom,tables+4),portrait=model&255;
        int trainers=pointer(rom,find(rom,kanto?"0820FFF757FB12E0":"FEF704F9044830E0"));
        int entry=-1;
        if(id>=0){entry=trainers+id*40;if(entry+40>rom.length||(rom[entry]&255)>3)throw new IOException("Invalid GBA trainer identity "+id);portrait=rom[entry+3]&255;}
        else{
            // Inline scripts sometimes specify only a model. Derive its class, never a random name.
            for(int row=trainers,count=0;row+40<=rom.length&&count<1000&&(rom[row]&255)<=3;row+=40,count++)
                if((rom[row+3]&255)==portrait){entry=row;break;}
            if(entry<0)throw new IOException("Missing GBA trainer model "+portrait);
        }
        if(portrait>=(kanto?148:93))throw new IOException("Invalid GBA trainer portrait "+portrait);
        int trainerClass=rom[entry+1]&255;
        int classes=find(rom,kanto?"6E0000003200270059000000":"950100000000050094010000");
        String type=GbaText.decode(rom,classes+trainerClass*13);
        String name=id>=0?GbaText.decode(rom,entry+4):"";
        int picture=pictures+portrait*8,tag=u16(rom,picture+6),palette=-1;
        Set<Integer> seen=new HashSet<>();
        for(int row=palettes;row+8<=rom.length;row+=8){
            int colorTag=u16(rom,row+4);
            if(!seen.add(colorTag))break;
            int address;
            try{address=pointer(rom,row);}catch(IOException end){break;}
            if(colorTag==tag){palette=address;break;}
        }
        if(palette<0)throw new IOException("Missing GBA trainer palette "+tag);
        byte[] tiles=GbaItemIcons.unpack(rom,pointer(rom,picture)),colors=GbaItemIcons.unpack(rom,palette);
        if(tiles.length<2048||colors.length<32)throw new IOException("Invalid GBA trainer graphics");
        int[] rgba=new int[64*64];
        for(int y=0;y<64;y++)for(int x=0;x<64;x++){
            int tile=y/8*8+x/8,index=(tiles[tile*32+(y&7)*4+(x&7)/2]>>>((x&1)*4))&15;
            if(index==0)continue;
            int c=u16(colors,index*2),r=c&31,g=c>>5&31,b=c>>10&31;
            rgba[y*64+x]=(r<<3|r>>2)<<24|(g<<3|g>>2)<<16|(b<<3|b>>2)<<8|255;
        }
        return new GbaTrainer(id,portrait,trainerClass,type,name,new BwPokemonSprites.Pixels(64,64,rgba));
    }
    private static int find(byte[] rom,String hex)throws IOException{
        byte[] pattern=HexFormat.of().parseHex(hex);
        search:for(int at=0;at<=rom.length-pattern.length;at++){
            for(int i=0;i<pattern.length;i++)if(rom[at+i]!=pattern[i])continue search;
            return at+pattern.length;
        }
        throw new IOException("Missing reference GBA trainer table "+hex);
    }
    private static int u16(byte[] bytes,int at){return (bytes[at]&255)|(bytes[at+1]&255)<<8;}
    private static int pointer(byte[] rom,int at)throws IOException{
        int p=u16(rom,at)|u16(rom,at+2)<<16;
        if((p&0xfe000000)!=0x08000000||(p&0x1ffffff)>=rom.length)throw new IOException("Invalid GBA trainer pointer");
        return p&0x1ffffff;
    }
}
