package top.totodile.pokemmo.protocol;

import java.io.*;
import java.util.*;
import static top.totodile.pokemmo.protocol.CharacterPackets.*;

/** 执行导出的 GBA 指令。控制流、变量、显隐和移动均来自 ROM，外部能力由宿主提供。 */
public final class GbaScriptRuntime {
    public static final int NONE=0, BUTTON=1, DRAW=2, YESNO=3, NAME=4, BATTLE=5, WARP=6, MOVEMENT=7, DELAY=8;
    public interface Host {
        int effect(String operation, int first, int second, String text) throws IOException;
    }
    public record ActorView(int id,int graphics,int elevation,boolean visible,NpcPackets.State state) { }
    private static final GbaScriptProgram PROGRAM=GbaScriptProgram.INSTANCE;
    private final Host host;
    private final Map<Integer,Integer> variables=new TreeMap<>(), buffers=new TreeMap<>();
    private final Set<Integer> flags=new TreeSet<>();
    private final Map<Integer,Actor> actors=new TreeMap<>();
    private final Deque<Integer> calls=new ArrayDeque<>(), queue=new ArrayDeque<>();
    private final Map<Integer,String> checkpoints=new TreeMap<>();
    private int pc,comparison,word,mode,serial=1,text,picture,trainer,waitActor,doorX=-1,doorY=-1,doorState;
    private long now,deadline,version;
    private Position player;
    private int playerFromX,playerFromY;
    private boolean locked,holdFrame;
    private int loadedGroup=-1,loadedMap=-1;
    private static final class Actor {
        int group,map,id,graphics,elevation,flag,x,y,fromX,fromY,facing,index;
        boolean invisible;
        byte[] moves=new byte[0];
        long ends;
    }
    public GbaScriptRuntime(String state,Position position,Host host) throws IOException {
        this.host=host; now=System.nanoTime(); initializeActors();player=position;playerFromX=position.x();playerFromY=position.y();
        if(state==null||state.isBlank()) {
            pc=PROGRAM.initializer;run(now);mode=NONE;pc=0;calls.clear();queue.clear();text=0;
        } else restore(state);
        if(!active()){player=position;playerFromX=position.x();playerFromY=position.y();}
    }
    private void initializeActors() {
        for(var n:PROGRAM.npcs){var a=new Actor();a.group=n.group();a.map=n.map();a.id=n.id();a.graphics=n.graphics();a.elevation=n.elevation();a.flag=n.flag();a.x=a.fromX=n.x();a.y=a.fromY=n.y();a.facing=n.facing();actors.put(key(a.group,a.map,a.id),a);}
    }
    private static int key(int g,int m,int id){return g<<16|m<<8|id;}
    public boolean active(){return pc!=0||mode!=NONE||!queue.isEmpty();}
    public boolean complete(){return variable(0x4055)>=4;}
    public int variable(int id){return variables.getOrDefault(id,0);}
    public Set<Integer> flags(){return Set.copyOf(flags);}
    public long version(){return version;}
    public int mode(){return mode;}
    public int serial(){return serial;}
    public int trainer(){return trainer;}
    public Position position(){return player;}
    private int value(int operand){return operand>=0x4000?variable(operand):operand;}
    private void set(int id,int value){variables.put(id,value&65535);version++;}
    private boolean condition(int c){return switch(c){case 0->comparison<0;case 1->comparison==0;case 2->comparison>0;case 3->comparison<=0;case 4->comparison>=0;case 5->comparison!=0;default->throw new IllegalArgumentException("Invalid GBA comparison");};}
    private static int u8(byte[] d,int at){return d[at]&255;}
    private static int u16(byte[] d,int at){return u8(d,at)|u8(d,at+1)<<8;}
    private static int pointer(byte[] d,int at){return (u16(d,at)|u16(d,at+2)<<16)-0x08000000;}
    private void waitFor(int type){mode=type;serial++;version++;}
    private Actor actor(int id){return actors.get(key(player.group(),player.map(),value(id)));}
    private boolean visible(Actor a){return !a.invisible&&(a.flag==0||!flags.contains(a.flag));}
    public boolean occupied(Position p){return actors.values().stream().anyMatch(a->a.id!=255&&a.group==p.group()&&a.map==p.map()&&visible(a)&&a.x==p.x()&&a.y==p.y());}
    public void onPosition(Position p,long time) throws IOException {
        now=time;
        if(active()){tick(time);return;}
        boolean moved=player==null||p.group()!=player.group()||p.map()!=player.map()||p.x()!=player.x()||p.y()!=player.y();
        player=p;playerFromX=p.x();playerFromY=p.y();
        if(holdFrame&&!moved)return;
        holdFrame=false;
        if(loadedGroup!=p.group()||loadedMap!=p.map()) {
            loadedGroup=p.group();loadedMap=p.map();
            variables.keySet().removeIf(k->k>=0x4000&&k<=0x400f);
            for(var a:actors.values())if(a.group==p.group()&&a.map==p.map())a.invisible=false;
            enqueueMap(3);enqueueMap(4);enqueueMap(2);
        }
        if(!active()) {
            for(var t:PROGRAM.triggers)if(t.kind()==0&&t.group()==p.group()&&t.map()==p.map()&&t.x()==p.x()&&t.y()==p.y()&&variable(t.variable())==t.value()) {queue.add(t.script());break;}
            if(queue.isEmpty())enqueueMap(2);
        }
        nextRoot();run(time);
    }
    private void enqueueMap(int kind){for(var t:PROGRAM.triggers)if(t.kind()==kind&&t.group()==player.group()&&t.map()==player.map()&&(t.variable()==0||variable(t.variable())==t.value()))queue.add(t.script());}
    private void nextRoot() throws IOException {
        if(pc!=0||queue.isEmpty())return;
        int start=queue.remove();checkpoints.putIfAbsent(start,core());pc=start;calls.clear();version++;
    }
    public boolean interact(Position p,int id,long time) throws IOException {
        if(active()||!PROGRAM.supports(p.group(),p.map()))return false;
        player=p;var a=actor(id);
        int tx=p.x()+(p.facing()==2?-1:p.facing()==3?1:0),ty=p.y()+(p.facing()==1?-1:p.facing()==0?1:0);
        if(a==null||!visible(a)||a.x!=tx||a.y!=ty)return false;
        var definition=PROGRAM.npcs.stream().filter(n->n.group()==p.group()&&n.map()==p.map()&&n.id()==id).findFirst().orElseThrow();
        if(definition.script()==0)return false;
        set(0x800f,id);set(0x800c,p.facing()+1);queue.add(definition.script());nextRoot();run(time);return true;
    }
    public void acknowledge(int revision,int kind,int answer,String name,long time) throws IOException {
        if(revision!=serial)return;
        if(mode==BUTTON&&kind==StoryPackets.ADVANCE||mode==DRAW&&kind==StoryPackets.DRAWN) {mode=NONE;run(time);}
        else if(mode==YESNO&&kind==StoryPackets.CHOICE){if(answer<0||answer>1)throw new IOException("Invalid GBA choice");set(0x800d,answer);mode=NONE;run(time);}
        else if(mode==NAME&&kind==StoryPackets.NICKNAME){host.effect("nickname",variable(0x8004),0,name);mode=NONE;run(time);}
    }
    public void arrived(long time) throws IOException {if(mode!=WARP)return;mode=NONE;loadedGroup=player.group();loadedMap=player.map();variables.keySet().removeIf(k->k>=0x4000&&k<=0x400f);var continuation=new ArrayDeque<>(queue);queue.clear();enqueueMap(3);enqueueMap(4);enqueueMap(2);queue.addAll(continuation);if(pc!=0){queue.addLast(pc);pc=0;}run(time);}
    public void battleFinished(boolean won,long time)throws IOException {if(mode!=BATTLE)return;now=time;set(0x800d,won?1:0);text=buffers.getOrDefault(won?512:513,0);if(text>0)waitFor(BUTTON);else{mode=NONE;run(time);}}
    public void tick(long time)throws IOException {
        now=time;
        for(var a:actors.values())step(a,time);
        if(mode==DELAY&&time>=deadline||mode==MOVEMENT&&movementFinished()){mode=NONE;run(time);}
    }
    private boolean movementFinished(){if(waitActor!=0&&waitActor!=255){var a=actor(waitActor);return a==null||a.index>=a.moves.length;}return actors.values().stream().noneMatch(a->a.index<a.moves.length&&a.group==player.group()&&a.map==player.map());}
    private Actor playerActor(){return actors.computeIfAbsent(key(player.group(),player.map(),255),k->{var a=new Actor();a.group=player.group();a.map=player.map();a.id=255;a.x=a.fromX=player.x();a.y=a.fromY=player.y();a.facing=player.facing();return a;});}
    private void applyMovement(int id,int address)throws IOException {
        Actor a=value(id)==255?playerActor():actor(id);if(a==null)throw new IOException("Missing GBA object "+id);
        byte[] data=PROGRAM.movements.get(address);if(data==null)throw new IOException("Missing ROM movement at "+Integer.toHexString(address));
        if(a.id==255){a.x=a.fromX=player.x();a.y=a.fromY=player.y();a.facing=player.facing();}
        a.moves=data;a.index=0;a.ends=now;step(a,now);
    }
    private void step(Actor a,long time)throws IOException {
        if(a.index>=a.moves.length||time<a.ends)return;
        int op=u8(a.moves,a.index++);a.fromX=a.x;a.fromY=a.y;
        if(op==254){a.index=a.moves.length;return;}
        String name=PROGRAM.movementNames.get(op);if(name==null)throw new IOException("Unknown ROM movement "+op);
        int duration=1,dir=name.endsWith("DOWN")?0:name.endsWith("UP")?1:name.endsWith("LEFT")?2:name.endsWith("RIGHT")?3:-1;
        if(name.startsWith("FACE_")&&dir>=0)a.facing=dir;
        else if(name.startsWith("WALK_IN_PLACE")){if(dir>=0)a.facing=dir;duration=140;}
        else if(name.startsWith("WALK_")||name.startsWith("PLAYER_RUN_")){if(dir<0)throw new IOException("Invalid walk");a.facing=dir;a.x+=dir==2?-1:dir==3?1:0;a.y+=dir==1?-1:dir==0?1:0;duration=name.contains("SLOWER")?550:name.contains("SLOW")?400:name.contains("RUN")?135:275;}
        else if(name.startsWith("DELAY_"))duration=Integer.parseInt(name.substring(6))*1000/60;
        else if(name.equals("SET_INVISIBLE"))a.invisible=true;
        else if(name.equals("SET_VISIBLE"))a.invisible=false;
        else if(name.equals("FACE_PLAYER")){a.facing=player.x()<a.x?2:player.x()>a.x?3:player.y()<a.y?1:0;}
        else if(name.startsWith("EMOTE_"))duration=500;
        else throw new IOException("Unsupported ROM movement "+name);
        a.ends=time+duration*1_000_000L;version++;
        if(a.id==255){playerFromX=a.fromX;playerFromY=a.fromY;player=new Position(a.group,a.map,a.x,a.y,player.elevation(),a.facing);}
    }
    private void run(long time)throws IOException {
        now=time;
        for(int budget=0;budget<4096;budget++){
            if(mode!=NONE)return;
            if(pc==0){nextRoot();if(pc==0){locked=false;return;}}
            int address=pc;byte[] d=PROGRAM.code.get(pc);if(d==null)throw new IOException("Missing ROM opcode at "+Integer.toHexString(pc));
            int op=u8(d,0);pc+=d.length;version++;
            switch(op){
                case 0x00,0x01 -> { }
                case 0x02 -> {pc=0;calls.clear();locked=false;}
                case 0x03 -> pc=calls.isEmpty()?0:calls.pop();
                case 0x04 -> {calls.push(pc);pc=pointer(d,1);}
                case 0x05 -> pc=pointer(d,1);
                case 0x06,0x07 -> {if(condition(u8(d,1))){if(op==7)calls.push(pc);pc=pointer(d,2);}}
                case 0x0f -> {if(u8(d,1)!=0)throw new IOException("Unsupported GBA local word");word=pointer(d,2);}
                case 0x08,0x09 -> {int standard=u8(d,1);if(standard<2||standard>6)throw new IOException("Unsupported standard script "+standard);text=word;waitFor(standard==5?YESNO:BUTTON);if(op==8)pc=0;}
                case 0x16 -> set(u16(d,1),u16(d,3));
                case 0x17,0x18 -> set(u16(d,1),variable(u16(d,1))+(op==0x17?1:-1)*value(u16(d,3)));
                case 0x19,0x1a -> set(u16(d,1),value(u16(d,3)));
                case 0x21,0x22 -> comparison=Integer.compare(value(u16(d,1)),op==0x21?u16(d,3):value(u16(d,3)));
                case 0x25,0x26 -> {int output=op==0x26?u16(d,1):0,id=u16(d,op==0x26?3:1);String name=PROGRAM.specials.get(id);int result=0;
                    if(name==null)throw new IOException("Unknown GBA special "+id);
                    switch(name){
                        case "UpdatePickStateFromSpecialVar8005","SetFlavorTextFlagFromSpecialVars" -> { }
                        case "HealPlayerParty" -> host.effect("heal",0,0,"");
                        case "GetStarterSpecies" -> result=switch(variable(0x4031)){case 0->1;case 1->7;case 2->4;default->0;};
                        case "ChangePokemonNickname" -> waitFor(NAME);
                        case "GetPlayerFacingDirection" -> result=player.facing()+1;
                        default -> throw new IOException("Unsupported GBA special "+name+" at "+Integer.toHexString(address));
                    }
                    if(output!=0)set(output,result);
                }
                case 0x27 -> { }
                case 0x28 -> {deadline=time+u16(d,1)*1_000_000_000L/60;waitFor(DELAY);}
                case 0x29,0xd0 -> flags.add(u16(d,1));
                case 0x2a -> {int flag=u16(d,1);flags.remove(flag);for(var a:actors.values())if(a.flag==flag)a.invisible=false;}
                case 0x2b -> comparison=flags.contains(u16(d,1))?0:-1;
                case 0x2f,0x30,0x31,0x32,0x33,0x34,0x35,0xc7,0xca,0xcb -> { }
                case 0x39 -> {int group=u8(d,1),map=u8(d,2),x=value(u16(d,4)),y=value(u16(d,6));if(u8(d,3)!=255)throw new IOException("Unsupported indexed script warp");player=new Position(group,map,x,y,3,1);playerFromX=x;playerFromY=y;text=0;waitFor(WARP);}
                case 0x42 -> {set(u16(d,1),player.x());set(u16(d,3),player.y());}
                case 0x44,0x45,0x47 -> set(0x800d,host.effect(op==0x44?"item-add":op==0x45?"item-remove":"item-check",value(u16(d,1)),value(u16(d,3)),""));
                case 0x4f -> applyMovement(u16(d,1),pointer(d,3));
                case 0x51 -> {waitActor=value(u16(d,1));if(!movementFinished())waitFor(MOVEMENT);}
                case 0x53,0x55 -> {var a=actor(u16(d,1));if(a==null)throw new IOException("Missing script object");a.invisible=op==0x53;if(a.flag!=0){if(op==0x53)flags.add(a.flag);else flags.remove(a.flag);}}
                case 0x5a -> {var a=actor(variable(0x800f));if(a!=null)a.facing=player.x()<a.x?2:player.x()>a.x?3:player.y()<a.y?1:0;}
                case 0x5b -> {int id=value(u16(d,1)),dir=u8(d,3)-1;if(id==255)player=new Position(player.group(),player.map(),player.x(),player.y(),player.elevation(),dir);else{var a=actor(id);if(a!=null)a.facing=dir;}}
                case 0x5c -> {trainer=u16(d,2);buffers.put(512,pointer(d,6));buffers.put(513,pointer(d,10));text=0;waitFor(BATTLE);}
                case 0x57,0x63 -> {var a=actor(u16(d,1));if(a==null)throw new IOException("Missing ROM actor");a.x=a.fromX=value(u16(d,3));a.y=a.fromY=value(u16(d,5));}
                case 0x64 -> { }
                case 0x65 -> {var a=actor(u16(d,1));if(a!=null)a.facing=KantoNpcs.initialFacing(u8(d,3));}
                case 0x66 -> { }
                case 0x67 -> {text=pointer(d,1);waitFor(DRAW);}
                case 0x68 -> {text=0;serial++;}
                case 0x69,0x6a -> locked=true;
                case 0x6b,0x6c -> {locked=false;text=0;serial++;}
                case 0x6d -> waitFor(BUTTON);
                case 0x75 -> picture=value(u16(d,1));
                case 0x76 -> picture=0;
                case 0x79 -> set(0x800d,host.effect("pokemon",value(u16(d,1)),u8(d,3),Integer.toString(address)));
                case 0x7d -> buffers.put(u8(d,1),value(u16(d,2)));
                case 0x83 -> buffers.put(256+u8(d,1),value(u16(d,2)));
                case 0x97 -> { }
                case 0xac,0xad -> {doorX=value(u16(d,1));doorY=value(u16(d,3));doorState=op==0xac?1:2;}
                case 0xae -> {deadline=time+(doorState==1?120_000_000L:500_000_000L);waitFor(DELAY);}
                default -> throw new IOException("Unsupported GBA opcode 0x"+Integer.toHexString(op)+" at ROM 0x"+Integer.toHexString(address));
            }
            if(calls.size()>64)throw new IOException("GBA call stack budget exceeded");
        }
        throw new IOException("GBA instruction budget exceeded");
    }
    public List<ActorView> actorViews(){return actors.values().stream().filter(a->a.id!=255&&a.group==player.group()&&a.map==player.map()).map(a->new ActorView(a.id,a.graphics,a.elevation,visible(a),new NpcPackets.State(a.id,a.x,a.y,a.fromX,a.fromY,a.facing,(int)Math.max(0,Math.min(550,(a.ends-now+999999)/1000000))))).toList();}
    public StoryPackets.Snapshot snapshot(long time)throws IOException {
        tick(time);var actor=playerActor();int remaining=actor.index<actor.moves.length?(int)Math.max(0,Math.min(550,(actor.ends-time+999999)/1000000)):0;
        return new StoryPackets.Snapshot(serial,text,0,0,null,player,playerFromX,playerFromY,remaining,mode==WARP,
                active(),mode,picture,trainer,doorX,doorY,doorState,actorViews(),Map.copyOf(buffers));
    }
    public String save()throws IOException {var out=new ByteArrayOutputStream();try(var d=new DataOutputStream(out)){d.writeUTF(PROGRAM.hash);d.writeUTF(core());d.writeInt(checkpoints.size());for(var e:checkpoints.entrySet()){d.writeInt(e.getKey());d.writeUTF(e.getValue());}}return Base64.getEncoder().encodeToString(out.toByteArray());}
    private String core()throws IOException {
        var out=new ByteArrayOutputStream();try(var d=new DataOutputStream(out)){
            d.writeInt(1);for(int v:new int[]{pc,comparison,word,mode,serial,text,picture,trainer,waitActor,doorX,doorY,doorState,loadedGroup,loadedMap})d.writeInt(v);
            d.writeLong(Math.max(0,deadline-now));d.writeBoolean(locked);d.writeBoolean(holdFrame);
            for(int v:new int[]{player.group(),player.map(),player.x(),player.y(),player.elevation(),player.facing(),playerFromX,playerFromY})d.writeInt(v);
            writeMap(d,variables);writeMap(d,buffers);d.writeInt(flags.size());for(int v:flags)d.writeInt(v);
            writeInts(d,calls);writeInts(d,queue);d.writeInt(actors.size());for(var a:actors.values()){
                for(int v:new int[]{a.group,a.map,a.id,a.graphics,a.elevation,a.flag,a.x,a.y,a.fromX,a.fromY,a.facing,a.index})d.writeInt(v);
                d.writeBoolean(a.invisible);d.writeLong(Math.max(0,a.ends-now));d.writeInt(a.moves.length);d.write(a.moves);
            }
        }return Base64.getEncoder().encodeToString(out.toByteArray());
    }
    private static void writeMap(DataOutputStream d,Map<Integer,Integer> m)throws IOException{d.writeInt(m.size());for(var e:m.entrySet()){d.writeInt(e.getKey());d.writeInt(e.getValue());}}
    private static void writeInts(DataOutputStream d,Collection<Integer> c)throws IOException{d.writeInt(c.size());for(int v:c)d.writeInt(v);}
    private static void readMap(DataInputStream d,Map<Integer,Integer> m)throws IOException{m.clear();int n=bounded(d.readInt(),10000);for(int i=0;i<n;i++)m.put(d.readInt(),d.readInt());}
    private static void readInts(DataInputStream d,Collection<Integer> c)throws IOException{c.clear();int n=bounded(d.readInt(),10000);for(int i=0;i<n;i++)c.add(d.readInt());}
    private static int bounded(int n,int max)throws IOException{if(n<0||n>max)throw new IOException("Invalid GBA state count");return n;}
    public void restore(String state)throws IOException {try(var d=new DataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(state)))){if(!d.readUTF().equals(PROGRAM.hash))throw new IOException("GBA script ROM mismatch");restoreCore(d.readUTF());checkpoints.clear();int n=bounded(d.readInt(),1000);for(int i=0;i<n;i++)checkpoints.put(d.readInt(),d.readUTF());}catch(IllegalArgumentException e){throw new IOException("Invalid GBA state",e);}}
    private void restoreCore(String core)throws IOException {
        try(var d=new DataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(core)))){
            if(d.readInt()!=1)throw new IOException("Unsupported GBA state version");pc=d.readInt();comparison=d.readInt();word=d.readInt();mode=d.readInt();serial=d.readInt();text=d.readInt();picture=d.readInt();trainer=d.readInt();waitActor=d.readInt();doorX=d.readInt();doorY=d.readInt();doorState=d.readInt();loadedGroup=d.readInt();loadedMap=d.readInt();deadline=now+d.readLong();locked=d.readBoolean();holdFrame=d.readBoolean();
            player=new Position(d.readInt(),d.readInt(),d.readInt(),d.readInt(),d.readInt(),d.readInt());playerFromX=d.readInt();playerFromY=d.readInt();readMap(d,variables);readMap(d,buffers);readInts(d,flags);readInts(d,calls);readInts(d,queue);actors.clear();int n=bounded(d.readInt(),1000);for(int i=0;i<n;i++){var a=new Actor();a.group=d.readInt();a.map=d.readInt();a.id=d.readInt();a.graphics=d.readInt();a.elevation=d.readInt();a.flag=d.readInt();a.x=d.readInt();a.y=d.readInt();a.fromX=d.readInt();a.fromY=d.readInt();a.facing=d.readInt();a.index=d.readInt();a.invisible=d.readBoolean();a.ends=now+d.readLong();a.moves=d.readNBytes(bounded(d.readInt(),4096));actors.put(key(a.group,a.map,a.id),a);}version++;
        }
    }
    public int entry(String scene) {
        if(scene.equals(StoryProgressPackets.OPENING))return PROGRAM.triggers.stream().filter(t->t.group()==3&&t.map()==0&&t.kind()==0&&t.variable()==0x4050).findFirst().orElseThrow().script();
        if(scene.equals(StoryProgressPackets.LAB_INTRODUCTION))return PROGRAM.triggers.stream().filter(t->t.group()==4&&t.map()==3&&t.kind()==2&&t.value()==1).findFirst().orElseThrow().script();
        return 0;
    }
    public boolean canStart(String scene){int entry=entry(scene);return !active()&&entry!=0&&PROGRAM.triggers.stream().anyMatch(t->t.script()==entry&&(t.variable()==0||variable(t.variable())==t.value()));}
    public Position startScene(String scene,long time)throws IOException {
        if(!canStart(scene))throw new IOException("ROM scene prerequisites not met");now=time;
        var trigger=PROGRAM.triggers.stream().filter(t->t.script()==entry(scene)).findFirst().orElseThrow();
        if(trigger.kind()==0)player=new Position(trigger.group(),trigger.map(),trigger.x(),trigger.y(),3,1);
        if(loadedGroup!=player.group()||loadedMap!=player.map()){loadedGroup=player.group();loadedMap=player.map();variables.keySet().removeIf(k->k>=0x4000&&k<=0x400f);enqueueMap(3);enqueueMap(4);}
        playerFromX=player.x();playerFromY=player.y();queue.add(entry(scene));nextRoot();run(time);return player;
    }
    public Position resetScene(String scene,long time)throws IOException {
        now=time;int entry=entry(scene);if(entry==0)throw new IOException("Scene has no exported ROM trigger");String checkpoint=checkpoints.get(entry);
        if(checkpoint==null&&scene.equals(StoryProgressPackets.OPENING))for(var t:PROGRAM.triggers)if(t.variable()==0x4050&&t.value()==0&&checkpoints.containsKey(t.script())){checkpoint=checkpoints.get(t.script());break;}
        if(checkpoint==null){if(!scene.equals(StoryProgressPackets.OPENING))throw new IOException("No formal script checkpoint");variables.clear();buffers.clear();flags.clear();actors.clear();initializeActors();pc=PROGRAM.initializer;mode=NONE;calls.clear();queue.clear();run(time);}
        else restoreCore(checkpoint);
        pc=0;mode=NONE;text=0;picture=0;trainer=0;calls.clear();queue.clear();for(var a:actors.values()){a.moves=new byte[0];a.index=0;a.ends=0;}
        if(scene.equals(StoryProgressPackets.OPENING)){player=new Position(3,0,12,2,3,1);checkpoints.clear();}
        playerFromX=player.x();playerFromY=player.y();holdFrame=true;serial++;version++;return player;
    }
}
