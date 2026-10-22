package top.totodile.pokemmo.client.startup;

import java.io.IOException;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 保留 glTF 无法表达的 NDS 面剔除、深度与 Polygon ID。 */
final class RomMaterials {
    static Map<String,Integer> read(byte[] bytes) throws IOException {
        try {
            ByteBuffer b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            int section=b.getInt(16), dictionary=section+8;
            int count=bytes[dictionary+1]&255, records=dictionary+16+count*4;
            if (count<1) throw new IOException("Missing Nitro model");
            int model=section+b.getInt(records), materials=model+b.getInt(model+8);
            dictionary=materials+4; count=bytes[dictionary+1]&255; records=dictionary+16+count*4;
            Map<String,Integer> result=new LinkedHashMap<>();
            for (int i=0;i<count;i++) {
                int start=records+count*4+i*16, length=0;
                while (length<16 && bytes[start+length]!=0) length++;
                String name=new String(bytes,start,length,StandardCharsets.US_ASCII);
                result.put(name,b.getInt(materials+b.getInt(records+i*4)+12));
            }
            return result;
        } catch (IndexOutOfBoundsException error) { throw new IOException("Invalid Nitro materials",error); }
    }
}
