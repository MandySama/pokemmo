package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.List;

/** 结束开场对白后请求挑战当前会话已验证相邻的NPC。 */
public final class TrainerPackets {
    public static final int OPCODE=0x89;
    public record Ready(int status,int id,String name,int defeat,int after,List<PartyMember> party) {
        public Ready {party=List.copyOf(party);}
    }
    private TrainerPackets() { }
    public static byte[] request(){return new byte[]{(byte)OPCODE};}
    public static byte[] response(Ready r){var d=new PacketData(10000).putByte(OPCODE).putByte(r.status()).putShort(r.id()).putString(r.name()).putInt(r.defeat()).putInt(r.after());PartyMember.write(d,r.party());return d.written();}
    public static Ready decode(byte[] bytes)throws IOException {
        var d=new PacketData(bytes);if(d.byteValue()!=OPCODE)throw new IOException("Invalid trainer response");
        var r=new Ready(d.byteValue(),d.shortValue(),d.string(16),d.intValue(),d.intValue(),PartyMember.read(d));d.end();
        if(r.status()>3||r.status()==1&&(r.id()<1||r.party().isEmpty()))throw new IOException("Invalid trainer party");return r;
    }
}
