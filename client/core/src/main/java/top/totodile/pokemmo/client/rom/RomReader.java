package top.totodile.pokemmo.client.rom;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/** ROM 文件头校验、NDS 文件表解析与按需读取；每次读取后关闭文件句柄。 */
public final class RomReader {
    private RomReader() {}

    public static byte[] read(RomInfo rom, long offset, int length) throws IOException {
        try (var input = new RandomAccessFile(rom.path().toFile(), "r")) {
            return bytes(input, offset, length);
        }
    }

    public static byte[] readFile(RomInfo rom, String name) throws IOException {
        RomEntry entry = rom.files().get(name);
        if (entry == null) throw new FileNotFoundException(name);
        return read(rom, entry.offset(), entry.length());
    }

    public static byte[] readGba(RomInfo rom, long address, int length) throws IOException {
        if (!rom.type().extension.equals("gba")) throw new IOException("不是 GBA ROM");
        return read(rom, address >= 0x08000000L ? address - 0x08000000L : address, length);
    }

    public static RomInfo inspect(Path path) throws IOException {
        try (var input=new RandomAccessFile(path.toFile(), "r")) {
            byte[] header=bytes(input,0,0x200);
            boolean gba=(header[0xb2]&255)==0x96;
            String code=ascii(header,gba?0xac:0x0c,4);
            RomType type=Arrays.stream(RomType.values()).filter(t -> t.matches(gba?"gba":"nds", code)).findFirst().orElseThrow(() -> new IOException("不支持的 ROM 游戏代码: "+code));
            Map<String,RomEntry> files=new LinkedHashMap<>();
            if (gba) {
                int checksum=0;
                for (int i=0xa0;i<=0xbc;i++) checksum-=header[i]&255;
                if (((checksum-0x19)&255)!=(header[0xbd]&255)) throw new IOException("GBA 文件头校验失败");
                if (input.length()<16L*1024*1024 || input.length()>32L*1024*1024) throw new IOException("GBA ROM 大小无效或文件被截断");
            } else {
                ByteBuffer data=ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
                int crc=0xffff;
                for (int i=0;i<0x15e;i++) {
                    crc^=header[i]&255;
                    for (int bit=0;bit<8;bit++) crc=(crc&1)!=0 ? (crc>>>1)^0xa001 : crc>>>1;
                }
                if (crc!=Short.toUnsignedInt(data.getShort(0x15e))) throw new IOException("NDS 文件头校验失败");
                long used=Integer.toUnsignedLong(data.getInt(0x80));
                if (used<0x200 || used>input.length()) throw new IOException("NDS ROM 大小无效或文件被截断");
                checkSection(input,data,0x20,0x2c); checkSection(input,data,0x30,0x3c);
                long fnt=Integer.toUnsignedLong(data.getInt(0x40)), fat=Integer.toUnsignedLong(data.getInt(0x48));
                int fntSize=data.getInt(0x44), fatSize=data.getInt(0x4c);
                if (fntSize<8 || fntSize>16*1024*1024 || fatSize<=0 || fatSize>8*65536 || fatSize%8!=0)
                    throw new IOException("NDS 文件表无效");
                byte[] names=bytes(input,fnt,fntSize), allocations=bytes(input,fat,fatSize);
                readDirectory(input,names,ByteBuffer.wrap(allocations).order(ByteOrder.LITTLE_ENDIAN),0xf000,"",files,new HashSet<>(),0);
                if (files.isEmpty()) throw new IOException("NDS ROM 没有可读取的资源");
            }
            return new RomInfo(path.toAbsolutePath().normalize(),type,ascii(header,gba?0xa0:0,12),code,
                    header[gba?0xbc:0x1e]&255,input.length(),files);
        } catch (IndexOutOfBoundsException | IllegalArgumentException error) { throw new IOException("ROM 文件表损坏",error); }
    }
    private static void checkSection(RandomAccessFile input, ByteBuffer header, int offset, int size) throws IOException {
        long start=Integer.toUnsignedLong(header.getInt(offset)), length=Integer.toUnsignedLong(header.getInt(size));
        if (start<0x200 || length==0 || start>input.length()-length) throw new IOException("NDS 程序区损坏或文件被截断");
    }
    private static void readDirectory(RandomAccessFile input, byte[] names, ByteBuffer fat, int id, String prefix,
                                      Map<String,RomEntry> files, Set<Integer> visited, int depth) throws IOException {
        ByteBuffer fnt=ByteBuffer.wrap(names).order(ByteOrder.LITTLE_ENDIAN);
        int directories=Short.toUnsignedInt(fnt.getShort(6)), index=id-0xf000;
        if (depth>64 || index<0 || index>=directories || !visited.add(id)) throw new IOException("NDS 目录无效");
        int cursor=fnt.getInt(index*8), fileId=Short.toUnsignedInt(fnt.getShort(index*8+4));
        while (true) {
            int descriptor=names[cursor++]&255;
            if (descriptor==0) break;
            int length=descriptor&127;
            if (length==0) throw new IOException("NDS 文件名无效");
            String name=ascii(names,cursor,length); cursor+=length;
            if (name.contains("/") || name.contains("\\") || name.equals(".") || name.equals("..")) throw new IOException("NDS 路径无效");
            if ((descriptor&128)!=0) {
                int child=Short.toUnsignedInt(fnt.getShort(cursor)); cursor+=2;
                readDirectory(input,names,fat,child,prefix+name+"/",files,visited,depth+1);
            } else {
                long start=Integer.toUnsignedLong(fat.getInt(fileId*8)), end=Integer.toUnsignedLong(fat.getInt(fileId*8+4)); fileId++;
                if (start>end || end>input.length() || end-start>Integer.MAX_VALUE) throw new IOException("NDS 资源超出文件范围");
                if (files.put(prefix+name,new RomEntry(start,(int)(end-start)))!=null) throw new IOException("NDS 资源路径重复");
            }
        }
    }
    private static String ascii(byte[] data,int start,int length) {
        return new String(data,start,length,StandardCharsets.US_ASCII).replace("\0", "").trim();
    }
    private static byte[] bytes(RandomAccessFile input,long offset,int length) throws IOException {
        if (length<0 || offset<0 || offset>input.length()-length) throw new IOException("ROM 文件被截断或读取越界");
        byte[] bytes=new byte[length]; input.seek(offset); input.readFully(bytes); return bytes;
    }
}
