package top.totodile.pokemmo.rom;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 读取火红训练家表、四种队伍布局及升级招式；输出不含图像的战斗资料。 */
public final class FireRedTrainers {
    /** 按训练家条目的外观编号读取64×64战斗正面图及独立调色板。 */
    public static java.awt.image.BufferedImage portrait(java.nio.file.Path path, int id) throws IOException {
        byte[] rom = java.nio.file.Files.readAllBytes(path);
        var trainers = new FireRedTrainers(rom);
        if (id < 1 || id >= 743) throw new IOException("Invalid trainer portrait ID");
        int picture = trainers.u8(trainers.table + id * 40 + 3);
        int table = -1, count = rom[0xae] == 'E' ? 93 : 148;
        for (int at = 0; at < rom.length - (count + 8) * 8; at += 4) {
            boolean valid = true;
            for (int i = 0; i < 8 && valid; i++) {
                int entry = at + i * 8, palette = at + (count + i) * 8;
                int pixels = trainers.ptr(entry), colors = trainers.ptr(palette);
                valid = trainers.u16(entry + 4) == 2048 && trainers.u16(entry + 6) == i
                        && trainers.u16(palette + 4) == i
                        && pixels >= 0 && colors >= 0
                        && trainers.u8(pixels) == 16 && trainers.u16(pixels + 1) == 2048
                        && trainers.u8(colors) == 16 && trainers.u16(colors + 1) == 32;
            }
            if (valid) { table = at; break; }
        }
        if (table < 0 || picture >= count) throw new IOException("Missing trainer portrait tables");
        var pixels = NdsCompression.decode(java.nio.ByteBuffer.wrap(rom,
                trainers.ptr(table + picture * 8), rom.length - trainers.ptr(table + picture * 8))
                .slice().order(java.nio.ByteOrder.LITTLE_ENDIAN));
        var colors = NdsCompression.decode(java.nio.ByteBuffer.wrap(rom,
                trainers.ptr(table + (count + picture) * 8), rom.length - trainers.ptr(table + (count + picture) * 8))
                .slice().order(java.nio.ByteOrder.LITTLE_ENDIAN));
        var image = new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
            int packed = pixels.get(((y / 8) * 8 + x / 8) * 32 + y % 8 * 4 + x % 8 / 2) & 255;
            int index = (packed >> ((x & 1) * 4)) & 15;
            int color = colors.getShort(index * 2) & 65535;
            if (index != 0) image.setRGB(x, y, 0xff000000 | (color & 31) * 255 / 31 << 16
                    | (color >> 5 & 31) * 255 / 31 << 8 | (color >> 10 & 31) * 255 / 31);
        }
        return image;
    }
    public record Move(int id, int pp) { }
    public record Mon(int species, int level, int iv, int heldItem, List<Move> moves, int rawHeldItem) {
        public Mon { moves = List.copyOf(moves); }
    }
    public record Trainer(int id, String name, boolean doubles, List<Mon> party) {
        public Trainer { party = List.copyOf(party); }
    }
    private final byte[] rom;
    private final int table, learnsets, moves;
    private final Map<Integer,Integer> species = mapping("/gba-national-species.csv");
    private final Map<Integer,Integer> items = mapping("/gba-held-items.csv");

    FireRedTrainers(byte[] rom) throws IOException {
        this.rom = rom;
        boolean hoenn=rom[0xae]=='E';
        // 英文卡带的 RICK 条目位于102号；同时校验队伍指针，避免误认对白里的名字。
        int found = -1;
        byte[] name = hoenn?new byte[]{(byte)189,(byte)187,(byte)198,(byte)208,(byte)195,(byte)200,(byte)255}:new byte[]{(byte)0xcc,(byte)0xc3,(byte)0xbd,(byte)0xc5,(byte)0xff};
        int anchor=hoenn?318:102;
        for(int at=anchor*40+4;at<rom.length-40;at++) if(matches(at,name)) {
            int candidate=at-anchor*40-4;
            if(u8(candidate+anchor*40+32)==(hoenn?1:2) && ptr(candidate+anchor*40+36)>0
                    && u8(candidate+(anchor+1)*40+32)>0) {found=candidate;break;}
        }
        if(found<0)throw new IOException("Unsupported FireRed trainer table");table=found;
        int bulb=find(new byte[]{0x21,2,0x2d,8,0x49,14,0x16,20});
        byte[] pointer=new byte[8];for(int i=0;i<4;i++)pointer[i]=pointer[i+4]=(byte)((bulb+0x08000000)>>(i*8));
        learnsets=find(pointer);
        int pound=find(new byte[]{0,40,0,100,35,0,0,0,51,0,0,0,43,50,1,100,25});
        moves=pound-12;
    }
    public Trainer trainer(int id) throws IOException {
        if(id<1||id>=743)throw new IOException("Invalid trainer ID");
        int at=table+id*40, flags=u8(at), count=u8(at+32), party=ptr(at+36);
        if(flags>3||count<1||count>6||party<0)throw new IOException("Invalid trainer party");
        int stride=(flags&1)!=0?16:8;
        var result=new ArrayList<Mon>();
        for(int i=0;i<count;i++) {
            int mon=party+i*stride, raw=u16(mon+4), level=u8(mon+2), item=(flags&2)!=0?u16(mon+6):0;
            Integer national=species.get(raw), held=items.get(item);
            // 星星沙和金珠在原卡带中没有持有战斗效果，保留原编号供上层展示。
            if(held==null&&(item==108||item==110))held=0;
            if(national==null||held==null||level<1||level>100)throw new IOException("Unknown trainer species/item");
            var ids=new ArrayList<Integer>();
            if((flags&1)!=0) {
                for(int j=0;j<4;j++){int move=u16(mon+((flags&2)!=0?8:6)+j*2);if(move!=0&&!ids.contains(move))ids.add(move);}
            } else {
                int learned=ptr(learnsets+raw*4);
                for(int j=0;j<128;j++) {
                    int entry=u16(learned+j*2);if(entry==65535)break;
                    if((entry>>9)<=level){int move=entry&511;if(!ids.contains(move)){if(ids.size()==4)ids.removeFirst();ids.add(move);}}
                    if(j==127)throw new IOException("Learnset exceeds bound");
                }
            }
            var learned=new ArrayList<Move>();for(int move:ids){if(move<1||move>354)throw new IOException("Invalid trainer move");learned.add(new Move(move,u8(moves+move*12+4)));}
            result.add(new Mon(national,level,u16(mon)*31/255,held,learned,item));
        }
        StringBuilder name=new StringBuilder();for(int i=0;i<12;i++){int ch=u8(at+4+i);if(ch==255)break;if(ch>=187&&ch<=212)name.append((char)('A'+ch-187));else if(ch>=213&&ch<=238)name.append((char)('a'+ch-213));else if(ch==0)name.append(' ');}
        return new Trainer(id,name.isEmpty()?Integer.toString(id):name.toString(),u8(at+24)!=0,result);
    }
    private int u8(int at)throws IOException {if(at<0||at>=rom.length)throw new IOException("Trainer data outside ROM");return rom[at]&255;}
    private int u16(int at)throws IOException {return u8(at)|u8(at+1)<<8;}
    private int ptr(int at)throws IOException {int value=u16(at)|u16(at+2)<<16;value-=0x08000000;return value>=0&&value<rom.length?value:-1;}
    private boolean matches(int at,byte[] needle){if(at<0||at+needle.length>rom.length)return false;for(int i=0;i<needle.length;i++)if(rom[at+i]!=needle[i])return false;return true;}
    private int find(byte[] needle)throws IOException {for(int i=0;i<=rom.length-needle.length;i++)if(matches(i,needle))return i;throw new IOException("Missing trainer data signature");}
    private static Map<Integer,Integer> mapping(String resource) {
        var result=new HashMap<Integer,Integer>();
        try(var input=FireRedTrainers.class.getResourceAsStream(resource)) {
            if(input==null)throw new IOException("Missing trainer mapping");
            for(String line:new String(input.readAllBytes(),StandardCharsets.UTF_8).split("\\R")){if(line.isBlank())continue;var f=line.split(",");result.put(Integer.parseInt(f[0]),Integer.parseInt(f[1]));}
        }catch(IOException e){throw new ExceptionInInitializerError(e);}return Map.copyOf(result);
    }
}
