package top.totodile.pokemmo.client.rom.nds;

import java.io.IOException;
import java.nio.*;
import java.util.*;

/** Nitro NARC 分块及 LZ10/LZ11；对应参考 NitroArchiveBlock/NitroFileEntry。 */
public final class NdsArchive {
    private final byte[] data;
    private final int[] starts, ends;
    private final int content;
    public NdsArchive(byte[] data) throws IOException {
        if (data.length<16) throw new IOException("Truncated NARC archive");
        this.data=data;
        try {
            ByteBuffer b=ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            if (b.getInt()!=0x4352414e) throw new IOException("Not a NARC archive");
            int table=-1, body=-1, count=0;
            for (int p=Short.toUnsignedInt(b.getShort(12));p<data.length;) {
                int type=b.getInt(p), size=b.getInt(p+4);
                if (size<8 || size>data.length-p) throw new IOException("Invalid NARC block");
                if (type==0x46415442) { table=p+12; count=Short.toUnsignedInt(b.getShort(p+8)); }
                if (type==0x46494d47) body=p+8;
                p+=size;
            }
            if (table<0 || body<0) throw new IOException("Missing NARC blocks");
            content=body; starts=new int[count]; ends=new int[count];
            for (int i=0;i<count;i++) {
                starts[i]=b.getInt(table+i*8); ends[i]=b.getInt(table+i*8+4);
                if (starts[i]<0 || ends[i]<starts[i] || ends[i]>data.length-content) throw new IOException("Invalid NARC entry");
            }
        } catch (IndexOutOfBoundsException error) { throw new IOException("Truncated NARC archive",error); }
    }
    public byte[] read(int index) throws IOException {
        return decompress(readRaw(index));
    }
    public int size() { return starts.length; }
    /** Structured map entries are not LZ streams even when their first byte is 0x10/0x11. */
    public byte[] readRaw(int index) throws IOException {
        if (index<0 || index>=starts.length) throw new IOException("Missing NARC entry "+index);
        return Arrays.copyOfRange(data,content+starts[index],content+ends[index]);
    }
    public static byte[] decompress(byte[] input) throws IOException {
        if (input.length==0 || (input[0]!=0x10 && input[0]!=0x11)) return input;
        try {
            int length=(input[1]&255)|((input[2]&255)<<8)|((input[3]&255)<<16), p=4, out=0;
            if (length==0) { length=ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN).getInt(4); p=8; }
            if (length<=0 || length>64*1024*1024) throw new IOException("Invalid LZ output size");
            byte[] result=new byte[length];
            while (out<length) {
                int flags=input[p++]&255;
                for (int bit=7;bit>=0 && out<length;bit--) {
                    if ((flags&(1<<bit))==0) { result[out++]=input[p++]; continue; }
                    int a=input[p++]&255, b=input[p++]&255, count, distance;
                    if (input[0]==0x10) { count=(a>>4)+3; distance=((a&15)<<8|b)+1; }
                    else if ((a>>4)==0) { count=((a&15)<<4|b>>4)+17; distance=((b&15)<<8|(input[p++]&255))+1; }
                    else if ((a>>4)==1) {
                        int c=input[p++]&255;
                        count=((a&15)<<12|b<<4|c>>4)+273; distance=((c&15)<<8|(input[p++]&255))+1;
                    } else { count=(a>>4)+1; distance=((a&15)<<8|b)+1; }
                    if (distance>out || count>length-out) throw new IOException("Invalid LZ back reference");
                    for (int i=0;i<count;i++,out++) result[out]=result[out-distance];
                }
            }
            return result;
        } catch (IndexOutOfBoundsException error) { throw new IOException("Truncated LZ stream",error); }
    }
}
