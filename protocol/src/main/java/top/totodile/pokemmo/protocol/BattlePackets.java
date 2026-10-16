package top.totodile.pokemmo.protocol;

import java.io.IOException;
import java.util.List;

/** 单打操作携带会话及请求序号，结果仅由游戏服务生成。 */
public final class BattlePackets {
    public static final int OPCODE = 0x87, START = 0, MOVE = 1, SWITCH = 2, RUN = 3;
    public record Request(int action, long battle, int revision, int slot) { }
    public record Event(int kind, int side, int value) { }
    public record MoveOption(int slot, int id, int pp, int maxPp) { }
    public record State(boolean accepted, long battle, int revision, int outcome, int active,
                        boolean forceSwitch, int enemyHp, int enemyMaxHp, List<PartyMember> party,
                        List<Event> events, List<Integer> legalMoves, List<MoveOption> options, int enemyActive) {
        public State(boolean accepted,long battle,int revision,int outcome,int active,boolean forceSwitch,int enemyHp,int enemyMaxHp,List<PartyMember> party,List<Event> events,List<Integer> legalMoves,List<MoveOption> options) {
            this(accepted,battle,revision,outcome,active,forceSwitch,enemyHp,enemyMaxHp,party,events,legalMoves,options,0);
        }
        public State(boolean accepted, long battle, int revision, int outcome, int active, boolean forceSwitch,
                     int enemyHp, int enemyMaxHp, List<PartyMember> party, List<Event> events, List<Integer> legalMoves) {
            this(accepted,battle,revision,outcome,active,forceSwitch,enemyHp,enemyMaxHp,party,events,legalMoves,List.of());
        }
    }
    private BattlePackets() { }
    public static byte[] request(Request r) {
        return new PacketData(20).putByte(OPCODE).putByte(r.action()).putLong(r.battle())
                .putInt(r.revision()).putByte(r.slot()).written();
    }
    public static Request readRequest(byte[] bytes) throws IOException {
        var d = new PacketData(bytes);
        if (d.byteValue() != OPCODE) throw new IOException("Invalid battle opcode");
        var r = new Request(d.byteValue(), d.longValue(), d.intValue(), d.byteValue()); d.end();
        if (r.action() > RUN) throw new IOException("Invalid battle action");
        return r;
    }
    public static byte[] response(State s) {
        var d = new PacketData(12000).putByte(OPCODE).putByte(s.accepted()?1:0).putLong(s.battle())
                .putInt(s.revision()).putByte(s.outcome()).putByte(s.active()).putByte(s.forceSwitch()?1:0)
                .putShort(s.enemyHp()).putShort(s.enemyMaxHp());
        PartyMember.write(d,s.party()); d.putShort(s.events().size());
        for(var e:s.events()) d.putByte(e.kind()).putByte(e.side()).putShort(e.value());
        d.putByte(s.legalMoves().size()); for(int slot:s.legalMoves()) d.putByte(slot);
        d.putByte(s.options().size());for(var option:s.options())d.putByte(option.slot()).putShort(option.id()).putByte(option.pp()).putByte(option.maxPp());
        d.putByte(s.enemyActive());
        return d.written();
    }
    public static State decode(byte[] bytes) throws IOException {
        var d = new PacketData(bytes);
        if(d.byteValue()!=OPCODE)throw new IOException("Invalid battle response");
        boolean accepted=d.booleanValue();long id=d.longValue();int revision=d.intValue(),outcome=d.byteValue(),active=d.byteValue();
        boolean forced=d.booleanValue();int hp=d.shortValue(),max=d.shortValue();var party=PartyMember.read(d);
        int n=d.shortValue();if(n>1024)throw new IOException("Too many battle events");
        var events=new java.util.ArrayList<Event>();for(int i=0;i<n;i++)events.add(new Event(d.byteValue(),d.byteValue(),d.shortValue()));
        n=d.byteValue();if(n>4)throw new IOException("Too many moves");var legal=new java.util.ArrayList<Integer>();for(int i=0;i<n;i++)legal.add(d.byteValue());
        n=d.byteValue();if(n>4)throw new IOException("Too many move options");var options=new java.util.ArrayList<MoveOption>();
        for(int i=0;i<n;i++)options.add(new MoveOption(d.byteValue(),d.shortValue(),d.byteValue(),d.byteValue()));
        int enemyActive=d.byteValue();
        d.end();if(enemyActive>5||active>=party.size()||outcome>5||hp>max)throw new IOException("Invalid battle state");
        return new State(accepted,id,revision,outcome,active,forced,hp,max,party,List.copyOf(events),List.copyOf(legal),List.copyOf(options),enemyActive);
    }
}
