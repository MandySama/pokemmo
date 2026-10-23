package top.totodile.pokemmo.client.rom.nds.bw;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** BlackWhite NdsTextBank.VJ0: per-entry seed, rotating XOR key and optional 9-bit text. */
final class BwText {
    private BwText() {}

    static String[] decode(byte[] bytes) throws IOException {
        try {
            var data=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            int blocks=Short.toUnsignedInt(data.getShort(0)),count=Short.toUnsignedInt(data.getShort(2));
            if(blocks<1||count>8192)throw new IOException("Invalid BW message bank");
            int block=data.getInt(12);
            String[] result=new String[count];
            for(int i=0;i<count;i++) {
                int offset=block+data.getInt(block+4+i*8),length=Short.toUnsignedInt(data.getShort(block+8+i*8));
                if(offset<0||offset+length*2L>bytes.length)throw new IOException("Invalid BW text entry");
                int key=(31881+i*10627)&65535;
                int[] words=new int[length];
                for(int j=0;j<length;j++) {
                    words[j]=Short.toUnsignedInt(data.getShort(offset+j*2))^key;
                    key=((key<<3)|(key>>>13))&65535;
                }
                var text=new StringBuilder();
                for(int j=0;j<length;j++) {
                    int value=words[j];
                    if(value==65535)break;
                    if(value==0xf100) {
                        int bits=0,packed=0;
                        compressed:while(++j<length) {
                            packed|=words[j]<<bits;bits+=16;
                            while(bits>=9) {
                                int ch=packed&511;packed>>>=9;bits-=9;
                                if((ch&255)==255)break compressed;
                                if(ch>1)text.append((char)ch);
                            }
                        }
                        break;
                    }
                    if(value==0xfffe){text.append(' ');continue;}
                    if(value==0xf000) {
                        if(j+2>=length)break;
                        int arguments=words[j+2];j+=2+arguments;continue;
                    }
                    if(value==9325)text.append('♂');
                    else if(value==9326)text.append('♀');
                    else if(value==9350)text.append("PK");
                    else if(value==9351)text.append("MN");
                    else if(value>10)text.append((char)value);
                }
                result[i]=text.toString();
            }
            return result;
        } catch(IndexOutOfBoundsException error){throw new IOException("Truncated BW message bank",error);}
    }
}
