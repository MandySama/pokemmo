package top.totodile.pokemmo.protocol;

import java.io.IOException;

/** 剧情确认携带阶段，重复按键和过期响应不能跨过下一段对话。 */
public final class StoryPackets {
    public static final int OPCODE = 0x83;
    public static final int POLL = 0, ADVANCE = 1, RESET = 2, SKIP = 3, START = 4, DRAWN=5, CHOICE=6, NICKNAME=7;
    public record Request(int action,int phase,int answer,String name) { }
    public record Snapshot(int phase, int text, int graphics, int elevation, NpcPackets.State actor,
                           CharacterPackets.Position player, int fromX, int fromY, int remaining, boolean warp,
                           boolean running,int mode,int picture,int trainer,int doorX,int doorY,int doorState,
                           java.util.List<GbaScriptRuntime.ActorView> actors,java.util.Map<Integer,Integer> buffers) {
        public Snapshot(int phase, int text, int graphics, int elevation, NpcPackets.State actor) {
            this(phase, text, graphics, elevation, actor, null, 0, 0, 0, false);
        }
        public Snapshot(int phase,int text,int graphics,int elevation,NpcPackets.State actor,CharacterPackets.Position player,int fromX,int fromY,int remaining,boolean warp) {
            this(phase,text,graphics,elevation,actor,player,fromX,fromY,remaining,warp,phase>=1&&phase<=11&&phase!=4,0,0,0,-1,-1,0,java.util.List.of(),java.util.Map.of());
        }
        public boolean active() { return running; }
    }
    private StoryPackets() { }
    public static byte[] request(int action, int phase) {
        return request(action,phase,0,"");
    }
    public static byte[] request(int action,int phase,int answer,String name) {
        if(action<0||action>NICKNAME||phase<0||name.length()>16)throw new IllegalArgumentException("Invalid script request");
        return new PacketData(50).putByte(OPCODE).putByte(action).putInt(phase).putInt(answer).putString(name).written();
    }
    public static Request readRequest(byte[] packet)throws IOException {
        var d=new PacketData(packet);if(d.byteValue()!=OPCODE)throw new IOException("Invalid story opcode");
        var r=new Request(d.byteValue(),d.intValue(),d.intValue(),d.string(16));d.end();
        if(r.action()>NICKNAME||r.phase()<0)throw new IOException("Invalid script action");return r;
    }
    public static byte[] response(Snapshot snapshot) {
        var actor = snapshot.actor();
        var data = new PacketData(8192).putByte(OPCODE).putInt(snapshot.phase()).putInt(snapshot.text())
                .putByte(snapshot.graphics()).putByte(snapshot.elevation()).putByte(actor != null ? 1 : 0)
                .putByte(actor == null ? 0 : actor.id()).putShort(actor == null ? 0 : actor.x())
                .putShort(actor == null ? 0 : actor.y()).putShort(actor == null ? 0 : actor.fromX())
                .putShort(actor == null ? 0 : actor.fromY()).putByte(actor == null ? 0 : actor.facing())
                .putShort(actor == null ? 0 : actor.remaining());
        var player = snapshot.player(); data.putByte(player == null ? 0 : 1);
        if (player != null) data.putByte(player.group()).putByte(player.map()).putShort(player.x()).putShort(player.y())
                .putByte(player.elevation()).putByte(player.facing()).putShort(snapshot.fromX()).putShort(snapshot.fromY())
                .putShort(snapshot.remaining()).putByte(snapshot.warp() ? 1 : 0);
        data.putByte(snapshot.running()?1:0).putByte(snapshot.mode()).putShort(snapshot.picture()).putShort(snapshot.trainer())
                .putInt(snapshot.doorX()).putInt(snapshot.doorY()).putByte(snapshot.doorState()).putShort(snapshot.actors().size());
        for(var a:snapshot.actors()){var s=a.state();data.putByte(a.id()).putShort(a.graphics()).putByte(a.elevation()).putByte(a.visible()?1:0)
                .putShort(s.x()).putShort(s.y()).putShort(s.fromX()).putShort(s.fromY()).putByte(s.facing()).putShort(s.remaining());}
        data.putShort(snapshot.buffers().size());for(var b:snapshot.buffers().entrySet())data.putShort(b.getKey()).putInt(b.getValue());
        return data.written();
    }
    public static Snapshot decode(byte[] packet) throws IOException {
        var data = new PacketData(packet);
        if (data.byteValue() != OPCODE) throw new IOException("Invalid story response");
        int phase = data.intValue(), text = data.intValue(), graphics = data.byteValue(), elevation = data.byteValue();
        boolean visible = data.booleanValue();
        var actor = new NpcPackets.State(data.byteValue(), data.shortValue(), data.shortValue(), data.shortValue(),
                data.shortValue(), data.byteValue(), data.shortValue());
        CharacterPackets.Position player = null; int fromX = 0, fromY = 0, remaining = 0; boolean warp = false;
        if (data.booleanValue()) {
            player = new CharacterPackets.Position(data.byteValue(), data.byteValue(), data.shortValue(), data.shortValue(), data.byteValue(), data.byteValue());
            fromX = data.shortValue(); fromY = data.shortValue(); remaining = data.shortValue(); warp = data.booleanValue();
            if (!player.valid(0) || remaining > 550 || Math.abs(player.x() - fromX) + Math.abs(player.y() - fromY) > 1)
                throw new IOException("Invalid story player movement");
        }
        boolean running=data.booleanValue();int mode=data.byteValue(),picture=data.shortValue(),trainer=data.shortValue(),doorX=data.intValue(),doorY=data.intValue(),doorState=data.byteValue();
        int count=data.shortValue();if(count>256)throw new IOException("Invalid script actor count");
        var actors=new java.util.ArrayList<GbaScriptRuntime.ActorView>();for(int i=0;i<count;i++){
            int id=data.byteValue(),gfx=data.shortValue(),height=data.byteValue();boolean show=data.booleanValue();
            var s=new NpcPackets.State(id,data.shortValue(),data.shortValue(),data.shortValue(),data.shortValue(),data.byteValue(),data.shortValue());
            if(s.facing()>3||s.remaining()>550||Math.abs(s.x()-s.fromX())+Math.abs(s.y()-s.fromY())>1)throw new IOException("Invalid script actor movement");
            actors.add(new GbaScriptRuntime.ActorView(id,gfx,height,show,s));
        }
        count=data.shortValue();if(count>256)throw new IOException("Invalid script buffer count");var buffers=new java.util.HashMap<Integer,Integer>();for(int i=0;i<count;i++)buffers.put(data.shortValue(),data.intValue());
        data.end();
        if (phase < 0 || text < 0 || actor.facing() > 3 || actor.remaining() > 550
                || actor.x() > 255 || actor.y() > 255
                || Math.abs(actor.x() - actor.fromX()) + Math.abs(actor.y() - actor.fromY()) > 1)
            throw new IOException("Invalid story state");
        return new Snapshot(phase,text,graphics,elevation,visible?actor:null,player,fromX,fromY,remaining,warp,running,mode,picture,trainer,doorX,doorY,doorState,java.util.List.copyOf(actors),java.util.Map.copyOf(buffers));
    }
}
