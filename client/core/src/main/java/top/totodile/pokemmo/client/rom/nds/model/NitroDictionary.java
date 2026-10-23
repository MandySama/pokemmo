package top.totodile.pokemmo.client.rom.nds.model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/** 参考 BinaryRecordSequence：树索引、定长记录与 16 字节名称表。 */
final class NitroDictionary {
    final int count,records,names,end,stride;
    private final ByteBuffer data;
    NitroDictionary(ByteBuffer data,int offset,int stride) {
        this.data=data; this.stride=stride;
        count=data.get(offset+1)&255;
        records=offset+16+count*4; names=records+count*stride; end=names+count*16;
        if(offset<0 || end>data.limit())throw new IllegalArgumentException("Truncated Nitro dictionary");
    }
    int record(int index) { return records+index*stride; }
    String name(int index) { return name(data,names+index*16); }
    static String name(ByteBuffer data,int offset) {
        byte[] bytes=new byte[16];
        for(int i=0;i<16;i++)bytes[i]=data.get(offset+i);
        int length=0; while(length<16 && bytes[length]!=0)length++;
        return new String(bytes,0,length,StandardCharsets.US_ASCII).trim();
    }
    static int u16(ByteBuffer data,int offset) { return data.getShort(offset)&65535; }
}
