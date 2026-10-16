package top.totodile.pokemmo.protocol;

import java.io.*;
import java.util.*;
import static top.totodile.pokemmo.protocol.CharacterPackets.*;

/** 执行导出的 GBA 指令。控制流、变量、显隐和移动均来自 ROM，外部能力由宿主提供。 */
public final class GbaScriptRuntime {
    public static final int NONE=0, BUTTON=1, DRAW=2, YESNO=3, NAME=4, BATTLE=5, WARP=6, MOVEMENT=7, DELAY=8, STARTER=9, CLOCK=10;
    public interface Host {
        int effect(String operation, int first, int second, String text) throws IOException;
    }
    public record ActorView(int id,int graphics,int elevation,boolean visible,NpcPackets.State state) { }
    private final GbaScriptProgram PROGRAM;
    private final int gender;
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
        boolean invisible,controlled;
        byte[] moves=new byte[0];
        long ends;
    }
    public GbaScriptRuntime(String state,Position position,Host host) throws IOException {
        this(0,0,state,position,host);
    }
    public GbaScriptRuntime(int region,int gender,String state,Position position,Host host) throws IOException {
        this.PROGRAM=GbaScriptProgram.forRegion(region);this.gender=gender;this.host=host; now=System.nanoTime(); initializeActors();player=position;playerFromX=position.x();playerFromY=position.y();
        if(state==null||state.isBlank()) {
            pc=PROGRAM.initializer;run(now);mode=NONE;pc=0;calls.clear();queue.clear();text=0;
            if(region==1){player=new Position(25,40,2,2,3,0);playerFromX=2;playerFromY=2;}
        } else restore(state);
        if(!active()&&!(region==1&&(state==null||state.isBlank()))){player=position;playerFromX=position.x();playerFromY=position.y();}
    }
    private void initializeActors() {
        for(var n:PROGRAM.npcs){var a=new Actor();a.group=n.group();a.map=n.map();a.id=n.id();a.graphics=n.graphics();a.elevation=n.elevation();a.flag=n.flag();a.controlled=n.flag()!=0||n.graphics()>=240;a.x=a.fromX=n.x();a.y=a.fromY=n.y();a.facing=n.facing();actors.put(key(a.group,a.map,a.id),a);}
    }
    private static int key(int g,int m,int id){return g<<16|m<<8|id;}
    public boolean active(){return pc!=0||mode!=NONE||!queue.isEmpty();}
    public boolean complete(){return PROGRAM.region==0?variable(0x4055)>=4:variable(0x4084)>=5;}
    public int region(){return PROGRAM.region;}
    public String romHash(){return PROGRAM.romHash();}
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
    private void buffer(int type,int slot,int value){for(int base:new int[]{0,256,768,1024})buffers.remove(base+slot);buffers.put(type+slot,value);}
    private void waitFor(int type){mode=type;serial++;version++;}
    private Actor actor(int id){return actors.get(key(player.group(),player.map(),value(id)));}
    private boolean visible(Actor a){return !a.invisible&&(a.flag==0||!flags.contains(a.flag));}
    public Boolean collisionOverride(Position p){if(p.group()!=player.group()||p.map()!=player.map())return null;Integer cell=buffers.get(20000+p.y()*256+p.x());return cell==null?null:(cell&0xc00)!=0;}
    public boolean occupied(Position p){return actors.values().stream().anyMatch(a->a.id!=255&&(a.controlled||a.flag!=0)&&a.group==p.group()&&a.map==p.map()&&visible(a)&&a.x==p.x()&&a.y==p.y());}
    public void onPosition(Position p,long time) throws IOException {
        now=time;
        if(active()){tick(time);return;}
        boolean moved=player==null||p.group()!=player.group()||p.map()!=player.map()||p.x()!=player.x()||p.y()!=player.y();
        player=p;
        if(PROGRAM.region==1&&p.group()==25&&p.map()==40&&buffers.containsKey(518))player=new Position(p.group(),p.map(),p.x(),p.y(),p.elevation(),p.facing(),new ReturnPoint(buffers.get(518),buffers.get(519),buffers.get(520),buffers.get(521),0));
        playerFromX=p.x();playerFromY=p.y();
        if(holdFrame&&!moved)return;
        holdFrame=false;
        if(loadedGroup!=p.group()||loadedMap!=p.map()) {
            loadedGroup=p.group();loadedMap=p.map();
            variables.keySet().removeIf(k->k>=0x4000&&k<=0x400f);buffers.keySet().removeIf(k->k>=20000);
            for(var a:actors.values())if(a.group==p.group()&&a.map==p.map())a.invisible=false;
            enqueueMap(3);enqueueMap(1);enqueueMap(4);enqueueMap(2);enqueueMap(5);
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
    public void syncNpcs(NpcPackets.Snapshot snapshot,long time){for(var state:snapshot.states()){var a=actors.get(key(snapshot.group(),snapshot.map(),state.id()));if(a!=null&&!a.controlled&&a.flag==0){a.x=state.x();a.y=state.y();a.fromX=state.fromX();a.fromY=state.fromY();a.facing=state.facing();a.ends=time+state.remaining()*1_000_000L;}}}
    public boolean interact(Position p,int id,long time) throws IOException {
        if(active()||!PROGRAM.supports(p.group(),p.map()))return false;
        player=p;var a=actor(id);
        int tx=p.x()+(p.facing()==2?-1:p.facing()==3?1:0),ty=p.y()+(p.facing()==1?-1:p.facing()==0?1:0);
        if(id==255){for(var t:PROGRAM.triggers)if(t.kind()>=100&&t.group()==p.group()&&t.map()==p.map()&&t.x()==tx&&t.y()==ty){set(0x800c,p.facing()+1);queue.add(t.script());nextRoot();run(time);return true;}return false;}
        if(a==null||!visible(a)||time<a.ends||a.x!=tx||a.y!=ty)return false;
        var definition=PROGRAM.npcs.stream().filter(n->n.group()==p.group()&&n.map()==p.map()&&n.id()==id).findFirst().orElseThrow();
        if(definition.script()==0)return false;
        set(0x800f,id);set(0x800c,p.facing()+1);queue.add(definition.script());nextRoot();run(time);return true;
    }
    public void acknowledge(int revision,int kind,int answer,String name,long time) throws IOException {
        if(revision!=serial)return;
        if(mode==BUTTON&&kind==StoryPackets.ADVANCE||mode==DRAW&&kind==StoryPackets.DRAWN) {mode=NONE;run(time);}
        else if(mode==YESNO&&kind==StoryPackets.CHOICE){if(answer<0||answer>1)throw new IOException("Invalid GBA choice");set(0x800d,answer);mode=NONE;run(time);}
        else if(mode==NAME&&kind==StoryPackets.NICKNAME){mode=NONE;host.effect("nickname",variable(0x8004),0,name);run(time);}
        else if(mode==CLOCK&&kind==StoryPackets.CHOICE){if(answer<0||answer>1439)throw new IOException("Invalid clock time");mode=NONE;if(buffers.getOrDefault(529,0)==0)host.effect("clock",answer,0,"");run(time);}
        else if(mode==STARTER&&kind==StoryPackets.CHOICE){
            if(answer<0||answer>2)throw new IOException("Invalid starter selection");
            if(buffers.getOrDefault(522,-1)>=0&&answer!=buffers.get(522))throw new IOException("Starter reward already chosen");
            set(0x4023,answer);set(0x800d,answer);
            buffers.remove(512);buffers.remove(513);buffers.put(514,261);buffers.put(515,2);trainer=0;waitFor(BATTLE);
            host.effect("pokemon",new int[]{252,255,258}[answer],5,Integer.toString(buffers.get(516)));
        }
    }
    public void arrived(long time) throws IOException {if(mode!=WARP)return;mode=NONE;loadedGroup=player.group();loadedMap=player.map();variables.keySet().removeIf(k->k>=0x4000&&k<=0x400f);buffers.keySet().removeIf(k->k>=20000);var continuation=new ArrayDeque<>(queue);queue.clear();enqueueMap(3);enqueueMap(1);enqueueMap(4);enqueueMap(2);enqueueMap(5);queue.addAll(continuation);if(pc!=0){queue.addLast(pc);pc=0;}run(time);}
    public void battleFinished(boolean won,long time)throws IOException {if(mode!=BATTLE)return;now=time;set(0x800d,won?1:0);
        if(PROGRAM.region==1&&trainer!=0&&!won){var heal=PROGRAM.heals.get(buffers.getOrDefault(517,gender+1));if(heal==null)throw new IOException("Unsupported whiteout heal location");pc=0;calls.clear();queue.clear();for(var a:actors.values()){a.moves=new byte[0];a.index=0;a.ends=0;}player=heal;playerFromX=heal.x();playerFromY=heal.y();text=0;trainer=0;waitFor(WARP);host.effect("heal",0,0,"");return;}
        if(won&&trainer>0){flags.add(0x500+trainer);if(buffers.getOrDefault(533,0)!=0)pc=buffers.get(533);}
        text=PROGRAM.textId(buffers.getOrDefault(won?512:513,0));if(text>0)waitFor(BUTTON);else{mode=NONE;run(time);}}
    public void tick(long time)throws IOException {
        now=time;
        for(var a:actors.values())step(a,time);
        if(mode==NONE&&pc!=0||mode==DELAY&&time>=deadline||mode==MOVEMENT&&movementFinished()){mode=NONE;run(time);}
    }
    private boolean movementFinished(){if(waitActor!=0&&waitActor!=255){var a=actor(waitActor);return a==null||a.index>=a.moves.length;}return actors.values().stream().noneMatch(a->a.index<a.moves.length&&a.group==player.group()&&a.map==player.map());}
    private Actor playerActor(){return actors.computeIfAbsent(key(player.group(),player.map(),255),k->{var a=new Actor();a.group=player.group();a.map=player.map();a.id=255;a.x=a.fromX=player.x();a.y=a.fromY=player.y();a.facing=player.facing();return a;});}
    private void applyMovement(int id,int address)throws IOException {
        Actor a=value(id)==255?playerActor():actor(id);if(a==null)throw new IOException("Missing GBA object "+id);
        byte[] data=PROGRAM.movements.get(address);if(data==null)throw new IOException("Missing ROM movement at "+Integer.toHexString(address));
        a.controlled=true;if(a.id==255){a.x=a.fromX=player.x();a.y=a.fromY=player.y();a.facing=player.facing();}
        a.moves=data;a.index=0;a.ends=now;step(a,now);
    }
    private void step(Actor a,long time)throws IOException {
        if(a.index>=a.moves.length||time<a.ends)return;
        int op=u8(a.moves,a.index++);a.fromX=a.x;a.fromY=a.y;
        if(op==254){a.index=a.moves.length;return;}
        String name=PROGRAM.movementNames.get(op);if(name==null)throw new IOException("Unknown ROM movement "+op);
        int duration=1,dir=name.replace("_FAST", "").endsWith("DOWN")?0:name.replace("_FAST", "").endsWith("UP")?1:name.replace("_FAST", "").endsWith("LEFT")?2:name.replace("_FAST", "").endsWith("RIGHT")?3:-1;
        if(name.startsWith("FACE_")&&dir>=0)a.facing=dir;
        else if(name.startsWith("WALK_IN_PLACE")){if(dir>=0)a.facing=dir;duration=140;}
        else if(name.startsWith("WALK_")||name.startsWith("PLAYER_RUN_")){if(dir<0)throw new IOException("Invalid walk");a.facing=dir;a.x+=dir==2?-1:dir==3?1:0;a.y+=dir==1?-1:dir==0?1:0;duration=name.contains("SLOWER")?550:name.contains("SLOW")?400:name.contains("RUN")||name.contains("FAST")?135:275;}
        else if(name.startsWith("JUMP")){int distance=name.contains("JUMP_2")?2:1;if(dir<0)dir=a.facing;a.facing=dir;a.x+=dir==2?-distance:dir==3?distance:0;a.y+=dir==1?-distance:dir==0?distance:0;duration=550;}
        else if(name.startsWith("DELAY_"))duration=Integer.parseInt(name.substring(6))*1000/60;
        else if(name.equals("SET_INVISIBLE"))a.invisible=true;
        else if(name.equals("SET_VISIBLE"))a.invisible=false;
        else if(name.equals("FACE_PLAYER")){a.facing=player.x()<a.x?2:player.x()>a.x?3:player.y()<a.y?1:0;}
        else if(name.startsWith("EMOTE_"))duration=500;
        else throw new IOException("Unsupported ROM movement "+name);
        a.ends=time+duration*1_000_000L;version++;
        if(a.id==255){playerFromX=a.fromX;playerFromY=a.fromY;player=new Position(a.group,a.map,a.x,a.y,player.elevation(),a.facing);}
    }
    private int effect(int instruction,String operation,int first,int second,String argument)throws IOException {
        int next=pc;pc=instruction;try{return host.effect(operation,first,second,argument);}finally{pc=next;}
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
                case 0x08,0x09 -> {int standard=u8(d,1);if(PROGRAM.standards.containsKey(standard)&&(standard<2||standard>6)){if(op==9)calls.push(pc);pc=PROGRAM.standards.get(standard);}else{if(standard<2||standard>6)throw new IOException("Unsupported standard script "+standard);text=PROGRAM.textId(word);waitFor(standard==5?YESNO:BUTTON);if(op==8)pc=0;}}
                case 0x16 -> set(u16(d,1),u16(d,3));
                case 0x17,0x18 -> set(u16(d,1),variable(u16(d,1))+(op==0x17?1:-1)*value(u16(d,3)));
                case 0x19,0x1a -> set(u16(d,1),value(u16(d,3)));
                case 0x21,0x22 -> comparison=Integer.compare(value(u16(d,1)),op==0x21?u16(d,3):value(u16(d,3)));
                case 0x25,0x26 -> {int output=op==0x26?u16(d,1):0,id=u16(d,op==0x26?3:1);String name=PROGRAM.specials.get(id);int result=0;
                    if(name==null)throw new IOException("Unknown GBA special "+id);
                    switch(name){
                        case "UpdatePickStateFromSpecialVar8005","SetFlavorTextFlagFromSpecialVars","QuestLog_CutRecording" -> { }
                        case "GetQuestLogState" -> result=0;
                        case "SetVermilionTrashCans" -> {
                            int first = java.util.concurrent.ThreadLocalRandom.current().nextInt(1,16);
                            var adjacent = new ArrayList<Integer>();
                            if(first>5)adjacent.add(first-5);if(first<=10)adjacent.add(first+5);
                            if((first-1)%5>0)adjacent.add(first-1);if((first-1)%5<4)adjacent.add(first+1);
                            set(0x8004,first);set(0x8005,adjacent.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(adjacent.size())));
                        }
                        case "StartOldManTutorialBattle" -> { buffers.put(514,13);buffers.put(515,5);trainer=0;text=0;waitFor(BATTLE); }
                        case "HealPlayerParty" -> effect(address,"heal",0,0,"");
                        case "GetStarterSpecies" -> result=switch(variable(0x4031)){case 0->1;case 1->7;case 2->4;default->0;};
                        case "ChangePokemonNickname" -> waitFor(NAME);
                        case "GetPlayerFacingDirection" -> result=player.facing()+1;
                        case "ChooseStarter" -> {text=0;buffers.put(516,address);buffers.put(522,effect(address,"starter-choice",address,0,""));waitFor(STARTER);}
                        case "StartWallClock","Special_ViewWallClock" -> {text=0;buffers.put(529,name.equals("StartWallClock")?0:1);buffers.put(530,effect(address,"clock-read",0,0,""));waitFor(CLOCK);}
                        case "GetPlayerBigGuyGirlString" -> buffer(768,0,gender==0?9785:9786);
                        case "GetRivalSonDaughterString" -> buffer(768,0,gender==0?9788:9787);
                        case "SetUnlockedPokedexFlags" -> effect(address,"dex-unlock",1,0,"");
                        case "InitSecretBaseDecorationSprites","TurnOffTVScreen","TurnOnTVScreen" -> { }
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
                case 0x3a -> {int x=value(u16(d,4)),y=value(u16(d,6));player=new Position(u8(d,1),u8(d,2),x,y,3,1);playerFromX=x;playerFromY=y;text=0;waitFor(WARP);}
                case 0x3f -> {buffers.put(518,u8(d,1));buffers.put(519,u8(d,2));buffers.put(520,value(u16(d,4)));buffers.put(521,value(u16(d,6)));var back=new ReturnPoint(u8(d,1),u8(d,2),value(u16(d,4)),value(u16(d,6)),0);player=new Position(player.group(),player.map(),player.x(),player.y(),player.elevation(),player.facing(),back);}
                case 0x43 -> set(0x800d,effect(address,"party-count",0,0,""));
                case 0x42 -> {set(u16(d,1),player.x());set(u16(d,3),player.y());}
                case 0x44,0x45,0x47 -> set(0x800d,effect(address,op==0x44?"item-add":op==0x45?"item-remove":"item-check",value(u16(d,1)),value(u16(d,3)),Integer.toString(op==0x44&&!calls.isEmpty()?calls.peek():address)));
                case 0x46,0x48 -> set(0x800d,1);
                case 0x4a -> {var item=ItemCatalog.get(value(u16(d,1)));if(item==null)throw new IOException("Unknown script item");set(0x800d,switch(item.pocket()){case 4->3;case 6->2;case 5->4;case 3->5;default->1;});}
                case 0x4f -> applyMovement(u16(d,1),pointer(d,3));
                case 0x51 -> {waitActor=value(u16(d,1));if(!movementFinished())waitFor(MOVEMENT);}
                case 0x53,0x55 -> {var a=actor(u16(d,1));if(a==null)throw new IOException("Missing script object");a.controlled=true;a.invisible=op==0x53;if(a.flag!=0){if(op==0x53)flags.add(a.flag);else flags.remove(a.flag);}}
                case 0x5a -> {var a=actor(variable(0x800f));if(a!=null)a.facing=player.x()<a.x?2:player.x()>a.x?3:player.y()<a.y?1:0;}
                case 0x5b -> {int id=value(u16(d,1)),dir=u8(d,3)-1;if(id==255)player=new Position(player.group(),player.map(),player.x(),player.y(),player.elevation(),dir);else{var a=actor(id);if(a!=null)a.facing=dir;}}
                case 0x5c -> {int type=u8(d,1);trainer=u16(d,2);buffers.put(512,type==3||type==9?pointer(d,6):pointer(d,10));buffers.put(513,0);buffers.put(532,pc);buffers.put(533,type==1||type==2?pointer(d,14):type==6||type==8?pointer(d,18):0);text=0;waitFor(BATTLE);}
                case 0x60 -> comparison=flags.contains(0x500+value(u16(d,1)))?1:0;
                case 0x61 -> flags.add(0x500+value(u16(d,1)));
                case 0x62 -> flags.remove(0x500+value(u16(d,1)));
                case 0x5e -> pc=buffers.getOrDefault(532,pc);
                case 0x5f -> pc=buffers.getOrDefault(533,pc);
                case 0x57,0x63 -> {int id=value(u16(d,1));var a=id==255?playerActor():actor(id);if(a==null)throw new IOException("Missing ROM actor");a.controlled=true;a.x=a.fromX=value(u16(d,3));a.y=a.fromY=value(u16(d,5));if(id==255){player=new Position(player.group(),player.map(),a.x,a.y,player.elevation(),player.facing(),player.returnPoint());playerFromX=a.x;playerFromY=a.y;}}
                case 0x64 -> { }
                case 0x65 -> {var a=actor(u16(d,1));if(a!=null)a.facing=KantoNpcs.initialFacing(u8(d,3));}
                case 0x66 -> { }
                case 0x67 -> {int target=pointer(d,1);text=PROGRAM.textId(target<0?word:target);waitFor(DRAW);}
                case 0x68 -> {text=0;serial++;}
                case 0x69,0x6a -> locked=true;
                case 0x6b,0x6c -> {locked=false;text=0;serial++;}
                case 0x6d -> waitFor(BUTTON);
                case 0x75 -> picture=value(u16(d,1));
                case 0x76 -> picture=0;
                case 0x79 -> set(0x800d,effect(address,"pokemon",value(u16(d,1)),u8(d,3),Integer.toString(address)));
                case 0x7d -> buffer(0,u8(d,1),PROGRAM.nationalSpecies(value(u16(d,2))));
                case 0x7e -> buffer(0,u8(d,1),effect(address,"lead-species",0,0,""));
                case 0x7f -> buffer(0,u8(d,1),effect(address,"party-species",value(u16(d,2)),0,""));
                case 0x80 -> buffer(1024,u8(d,1),value(u16(d,2)));
                case 0x85 -> buffer(768,u8(d,1),PROGRAM.textId(pointer(d,2)));
                case 0xd4,0xe2 -> buffer(1024,u8(d,1),value(u16(d,2)));
                case 0x8a -> buffers.put(10000+u8(d,1),u8(d,2)<<8|u8(d,3));
                case 0x9f -> buffers.put(517,u16(d,1));
                case 0xa0 -> set(0x800d,gender);
                case 0x59 -> {var a=actors.get(key(u8(d,4),u8(d,3),value(u16(d,1))));if(a!=null){a.controlled=true;a.invisible=true;}}
                case 0xc3,0xc5,0xa1 -> { }
                case 0xa2 -> {int x=value(u16(d,1)),y=value(u16(d,3));buffers.put(20000+y*256+x,(value(u16(d,5))&1023)|(value(u16(d,7))!=0?0xc00:0));}
                case 0xa6 -> buffers.put(528,u8(d,1));
                case 0x83 -> buffer(256,u8(d,1),value(u16(d,2)));
                case 0x84 -> {int id=value(u16(d,2));int textId=switch(id){case 14->1401;case 15->1406;case 16->2;case 17->3;case 18->1402;case 24->1401;case 25->1402;case 26->2;case 27->3;case 28->1406;default->throw new IOException("Unsupported standard string "+id);};buffer(768,u8(d,1),textId);}
                case 0x97 -> { }
                case 0xac,0xad -> {doorX=value(u16(d,1));doorY=value(u16(d,3));doorState=op==0xac?1:2;}
                case 0xae -> {deadline=time+(doorState==1?120_000_000L:500_000_000L);waitFor(DELAY);}
                default -> throw new IOException("Unsupported GBA opcode 0x"+Integer.toHexString(op)+" at ROM 0x"+Integer.toHexString(address));
            }
            if(calls.size()>64)throw new IOException("GBA call stack budget exceeded");
        }
        throw new IOException("GBA instruction budget exceeded");
    }
    public List<ActorView> actorViews(){return actors.values().stream().filter(a->a.id!=255&&(a.controlled||a.flag!=0)&&a.group==player.group()&&a.map==player.map()).map(a->new ActorView(a.id,a.graphics>=240?variable(0x4010+a.graphics-240):a.graphics,a.elevation,visible(a),new NpcPackets.State(a.id,a.x,a.y,a.fromX,a.fromY,a.facing,(int)Math.max(0,Math.min(550,(a.ends-now+999999)/1000000))))).toList();}
    public StoryPackets.Snapshot snapshot(long time)throws IOException {
        tick(time);var actor=playerActor();int remaining=actor.index<actor.moves.length?(int)Math.max(0,Math.min(550,(actor.ends-time+999999)/1000000)):0;
        return new StoryPackets.Snapshot(serial,text,0,0,null,player,playerFromX,playerFromY,remaining,mode==WARP,
                active(),mode,picture,trainer,doorX,doorY,doorState,actorViews(),Map.copyOf(buffers));
    }
    public String save()throws IOException {var out=new ByteArrayOutputStream();try(var d=new DataOutputStream(out)){d.writeUTF(PROGRAM.hash);d.writeUTF(core());d.writeInt(checkpoints.size());for(var e:checkpoints.entrySet()){d.writeInt(e.getKey());d.writeUTF(e.getValue());}}return Base64.getEncoder().encodeToString(out.toByteArray());}
    private String core()throws IOException {
        var out=new ByteArrayOutputStream();try(var d=new DataOutputStream(out)){
            d.writeInt(2);for(int v:new int[]{pc,comparison,word,mode,serial,text,picture,trainer,waitActor,doorX,doorY,doorState,loadedGroup,loadedMap})d.writeInt(v);
            d.writeLong(Math.max(0,deadline-now));d.writeBoolean(locked);d.writeBoolean(holdFrame);
            for(int v:new int[]{player.group(),player.map(),player.x(),player.y(),player.elevation(),player.facing(),playerFromX,playerFromY})d.writeInt(v);
            writeMap(d,variables);writeMap(d,buffers);d.writeInt(flags.size());for(int v:flags)d.writeInt(v);
            writeInts(d,calls);writeInts(d,queue);d.writeInt(actors.size());for(var a:actors.values()){
                for(int v:new int[]{a.group,a.map,a.id,a.graphics,a.elevation,a.flag,a.x,a.y,a.fromX,a.fromY,a.facing,a.index})d.writeInt(v);
                d.writeBoolean(a.invisible);d.writeBoolean(a.controlled);d.writeLong(Math.max(0,a.ends-now));d.writeInt(a.moves.length);d.write(a.moves);
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
            int format=d.readInt();if(format<1||format>2)throw new IOException("Unsupported GBA state version");pc=d.readInt();comparison=d.readInt();word=d.readInt();mode=d.readInt();serial=d.readInt();text=d.readInt();picture=d.readInt();trainer=d.readInt();waitActor=d.readInt();doorX=d.readInt();doorY=d.readInt();doorState=d.readInt();loadedGroup=d.readInt();loadedMap=d.readInt();deadline=now+d.readLong();locked=d.readBoolean();holdFrame=d.readBoolean();
            player=new Position(d.readInt(),d.readInt(),d.readInt(),d.readInt(),d.readInt(),d.readInt());playerFromX=d.readInt();playerFromY=d.readInt();readMap(d,variables);readMap(d,buffers);readInts(d,flags);readInts(d,calls);readInts(d,queue);actors.clear();int n=bounded(d.readInt(),10000);for(int i=0;i<n;i++){var a=new Actor();a.group=d.readInt();a.map=d.readInt();a.id=d.readInt();a.graphics=d.readInt();a.elevation=d.readInt();a.flag=d.readInt();a.x=d.readInt();a.y=d.readInt();a.fromX=d.readInt();a.fromY=d.readInt();a.facing=d.readInt();a.index=d.readInt();a.invisible=d.readBoolean();a.controlled=format==1||d.readBoolean();a.ends=now+d.readLong();a.moves=d.readNBytes(bounded(d.readInt(),4096));actors.put(key(a.group,a.map,a.id),a);}version++;
        }
    }
    public int entry(String scene) {
        if(PROGRAM.region==1){
            int variable=scene.equals(StoryProgressPackets.HOENN_OPENING)?0x4092:scene.equals(StoryProgressPackets.HOENN_RESCUE)?0x4060:scene.equals(StoryProgressPackets.HOENN_DEX)?0x4084:0;
            int expected=variable==0x4092?0:variable==0x4060?1:4;
            return variable==0?0:PROGRAM.triggers.stream().filter(t->t.variable()==variable&&t.value()==expected&&(variable==0x4084?t.kind()==2:t.kind()==0)).findFirst().orElseThrow().script();
        }
        if(KantoStoryScenes.entry(scene)!=null)return KantoStoryScenes.entry(scene).script();
        if(scene.equals(StoryProgressPackets.OPENING))return PROGRAM.triggers.stream().filter(t->t.group()==3&&t.map()==0&&t.kind()==0&&t.variable()==0x4050).findFirst().orElseThrow().script();
        if(scene.equals(StoryProgressPackets.LAB_INTRODUCTION))return PROGRAM.triggers.stream().filter(t->t.group()==4&&t.map()==3&&t.kind()==2&&t.value()==1).findFirst().orElseThrow().script();
        return 0;
    }
    public boolean canStart(String scene){if(PROGRAM.region==0&&KantoStoryScenes.entry(scene)!=null){var definition=KantoStoryScenes.entry(scene);return !active()&&(definition.variable()==0||variable(definition.variable())==definition.expected());}int entry=entry(scene);return !active()&&entry!=0&&PROGRAM.triggers.stream().anyMatch(t->t.script()==entry&&(t.variable()==0||variable(t.variable())==t.value()));}
    public Position startScene(String scene,long time)throws IOException {
        if(!canStart(scene))throw new IOException("ROM scene prerequisites not met");now=time;
        if(PROGRAM.region==0&&KantoStoryScenes.entry(scene)!=null){beginKantoEntry(KantoStoryScenes.entry(scene),time);return player;}
        var trigger=PROGRAM.triggers.stream().filter(t->t.script()==entry(scene)).findFirst().orElseThrow();
        if(trigger.kind()==0)player=new Position(trigger.group(),trigger.map(),trigger.x(),trigger.y(),3,1);
        else if(PROGRAM.region==1){var location=StoryProgressPackets.scene(scene);player=new Position(location.group(),location.map(),location.x(),location.y(),3,1);}
        if(loadedGroup!=player.group()||loadedMap!=player.map()){loadedGroup=player.group();loadedMap=player.map();variables.keySet().removeIf(k->k>=0x4000&&k<=0x400f);buffers.keySet().removeIf(k->k>=20000);enqueueMap(3);enqueueMap(4);}
        playerFromX=player.x();playerFromY=player.y();queue.add(entry(scene));nextRoot();run(time);return player;
    }
    public boolean canReset(String scene){if(PROGRAM.region==0)return KantoStoryScenes.entry(scene)!=null;int address=entry(scene);return address!=0&&(checkpoints.containsKey(address)||canStart(scene)||scene.equals(StoryProgressPackets.OPENING)||scene.equals(StoryProgressPackets.HOENN_OPENING));}
    public Set<String> completedScenes(){var done=new HashSet<String>();if(PROGRAM.region==1){if(variable(0x4092)>0)done.add(StoryProgressPackets.HOENN_OPENING);if(variable(0x4060)>=3)done.add(StoryProgressPackets.HOENN_RESCUE);if(variable(0x4084)>=5)done.add(StoryProgressPackets.HOENN_DEX);}return done;}
    public Position resetScene(String scene,long time)throws IOException {
        if(PROGRAM.region==0)return resetKantoScene(scene,time);
        now=time;int entry=entry(scene);if(entry==0)throw new IOException("Scene has no exported ROM trigger");String checkpoint=checkpoints.get(entry);
        if(checkpoint==null&&scene.equals(StoryProgressPackets.OPENING))for(var t:PROGRAM.triggers)if(t.variable()==0x4050&&t.value()==0&&checkpoints.containsKey(t.script())){checkpoint=checkpoints.get(t.script());break;}
        if(checkpoint==null){if(scene.equals(StoryProgressPackets.HOENN_OPENING)||scene.equals(StoryProgressPackets.OPENING)){variables.clear();buffers.clear();flags.clear();actors.clear();initializeActors();pc=PROGRAM.initializer;mode=NONE;calls.clear();queue.clear();run(time);}else if(!canStart(scene))throw new IOException("No formal script checkpoint");}
        else restoreCore(checkpoint);
        pc=0;mode=NONE;text=0;picture=0;trainer=0;calls.clear();queue.clear();for(var a:actors.values()){a.moves=new byte[0];a.index=0;a.ends=0;}
        if(scene.equals(StoryProgressPackets.HOENN_OPENING)){player=new Position(25,40,2,2,3,0);checkpoints.clear();loadedGroup=-1;loadedMap=-1;}
        else if(PROGRAM.region==1){var location=StoryProgressPackets.scene(scene);player=new Position(location.group(),location.map(),location.x(),location.y(),3,1);}
        if(scene.equals(StoryProgressPackets.OPENING)){player=new Position(3,0,12,2,3,1);checkpoints.clear();}
        playerFromX=player.x();playerFromY=player.y();holdFrame=true;serial++;version++;if(PROGRAM.region==1)host.effect("dex-state",flags.contains(0x861)?1:0,0,"");return player;
    }
    /** 从初始 ROM 状态构造边界，前跳和回退均不依赖角色的历史快照。 */
    private Position resetKantoScene(String scene, long time) throws IOException {
        if (!canReset(scene)) throw new IOException("Scene has no canonical ROM boundary");
        var initial = StoryProgressPackets.scene(StoryProgressPackets.OPENING);
        var boundary = new GbaScriptRuntime(0, gender, "",
                new Position(initial.group(), initial.map(), initial.x(), initial.y(), 3, 1),
                (operation, first, second, argument) -> operation.equals("party-count") ? 1 : operation.equals("lead-species") ? 1 : operation.startsWith("item-") ? 1 : 0);
        for (var milestone : KantoStoryScenes.progression()) {
            if (milestone.rank() >= KantoStoryScenes.entry(scene).rank()) break;
            try {
                boundary.beginKantoEntry(milestone, time);
                boundary.finishBoundary(time);
            } catch (IOException error) { throw new IOException("Kanto boundary milestone " + milestone.scene(), error); }
        }
        int previousSerial = serial;
        now = time;
        restoreCore(boundary.core());
        checkpoints.clear();
        pc = 0;
        mode = NONE;
        text = picture = trainer = waitActor = 0;
        doorX = doorY = -1;
        doorState = 0;
        locked = false;
        calls.clear();
        queue.clear();
        buffers.clear();
        variables.keySet().removeIf(id -> id >= 0x4000 && id <= 0x400f || id >= 0x8000);
        for (var actor : actors.values()) {
            actor.moves = new byte[0];
            actor.index = 0;
            actor.ends = 0;
        }
        var location = StoryProgressPackets.scene(scene);
        player = new Position(location.group(), location.map(), location.x(), location.y(), 3, 1);
        playerFromX = player.x();
        playerFromY = player.y();
        loadedGroup = loadedMap = -1;
        holdFrame = true;
        serial = previousSerial + 1;
        version++;
        return player;
    }

    private void beginKantoEntry(KantoStoryScenes.Entry definition, long time) throws IOException {
        var location = StoryProgressPackets.scene(definition.scene());
        player = new Position(location.group(), location.map(), location.x(), location.y(), 3, 1);
        playerFromX = player.x();
        playerFromY = player.y();
        loadedGroup = player.group();
        loadedMap = player.map();
        variables.keySet().removeIf(id -> id >= 0x4000 && id <= 0x400f);
        if (definition.variable() != 0) set(definition.variable(), definition.expected());
        if (definition.npc() != 0) set(0x800f, definition.npc());
        set(0x800c, player.facing() + 1);
        pc = 0;
        mode = NONE;
        calls.clear();
        queue.clear();
        enqueueMap(3);
        enqueueMap(4);
        queue.add(definition.script());
        nextRoot();
        run(time);
    }

    /** 仅供构造剧情边界；执行原控制流，宿主不修改真实队伍与背包。 */
    private void finishBoundary(long time) throws IOException {
        for (int step = 0; step < 8192; step++) {
            long clock = time + step * 1_000_000_000L;
            if (!active()) return;
            switch (mode) {
                case BUTTON -> acknowledge(serial, StoryPackets.ADVANCE, 0, "", clock);
                case DRAW -> acknowledge(serial, StoryPackets.DRAWN, 0, "", clock);
                case YESNO -> acknowledge(serial, StoryPackets.CHOICE, 1, "", clock);
                case NAME -> acknowledge(serial, StoryPackets.NICKNAME, 0, "", clock);
                case BATTLE -> battleFinished(true, clock);
                case WARP -> { mode = NONE; run(clock); }
                case NONE, MOVEMENT, DELAY -> { }
                default -> throw new IOException("Unsupported Kanto boundary wait " + mode);
            }
            tick(clock);
        }
        throw new IOException("Kanto boundary execution budget exceeded");
    }

}
