import top.totodile.pokemmo.*;import top.totodile.pokemmo.avatar.*;import top.totodile.pokemmo.persistence.*;import top.totodile.pokemmo.protocol.*;import top.totodile.pokemmo.protocol.CharacterPackets.*;import top.totodile.pokemmo.game.*;import top.totodile.pokemmo.net.*;import java.util.*;import java.util.concurrent.*;import java.net.*;import java.nio.file.*;
public class NpcRewardNetworkSmoke {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}static <T>T await(CompletableFuture<T> f)throws Exception{return f.get(20,TimeUnit.SECONDS);}
 static Position[] findPair(WorldRules rules,int region) {
 int season=(java.time.LocalDate.now(java.time.ZoneOffset.UTC).getMonthValue()-1)%4;
 int hour=(int)Math.floorMod(java.time.Instant.now().getEpochSecond()*4/3600,24);int time=hour>=4&&hour<10?0:hour>=10&&hour<20?1:2;
 for(var table:EncounterCatalog.tables())if(table.region()==region&&(table.kind()==0||table.kind()==1)&&(table.season()<0||table.season()==season)&&(table.time()<0||table.time()==time)) {
 for(int y=0;y<1024;y++){if(!FireRedMaps.contains(region,table.group(),table.map(),0,y))break;for(int x=0;x<1024;x++){
 if(!FireRedMaps.contains(region,table.group(),table.map(),x,y))break;
 Position p;try{p=rules.at(table.group(),table.map(),x,y,0,null);}catch(Exception e){continue;}
 if(EncounterCatalog.walking(region,p,rules.encounterBehavior(p),season,time)==null)continue;
 for(int direction=0;direction<4;direction++){var next=rules.move(p,direction,false);if(next.moved()&&!next.warp()&&p.group()==next.position().group()&&p.map()==next.position().map()&&EncounterCatalog.walking(region,next.position(),rules.encounterBehavior(next.position()),season,time)!=null){var back=rules.move(next.position(),direction^1,false);if(back.moved()&&back.position().x()==x&&back.position().y()==y)return new Position[]{p,new Position(p.group(),p.map(),p.x(),p.y(),p.elevation(),direction)};}}
 }}}
 throw new AssertionError("No regional grass pair "+region);
 }
 public static void main(String[]args)throws Exception{
 var d=PokemonIndividual.create(1,5,false,1,"Growth",0,0,0,0);
 var mon=new PartyMember(1,false,false,d.stat(1,5,0)-2,d.stat(1,5,0),5,d);
 var grown=BattleRewards.grow(mon,2000);
 check(grown.level()>5&&grown.maxHp()-grown.hp()==2,"growth recalculates level and preserves missing HP");
 var capped=BattleRewards.grow(mon,Long.MAX_VALUE/2);
 check(capped.level()==100&&capped.individual().experience()==PokemonCatalog.experience(PokemonCatalog.get(1).growth(),100),"experience cap");
 var rules=new FireRedWorldRules();Position start=new Position(1,0,46,45,3,3);int first=3;
 var db=new Database();db.initialize();var accounts=new AccountStore(db);String suffix=Long.toHexString(System.nanoTime()),user="field_"+suffix;int account=0;
 try{accounts.create(user,"isolated-field-test");account=accounts.find(user).orElseThrow().id();var assets=new AvatarAssets(ClientFiles.data("sprites/addons.pak"));int[] ids=new int[12];Arrays.fill(ids,-1);ids[10]=0;for(int c:new int[]{2,3,6,8,9,11})ids[c]=assets.choices(c,0).getFirst().id;var appearance=new Appearance("W"+suffix,0,2,0,ids,new int[12]);var store=new CharacterStore(db,new AvatarCatalog(ClientFiles.data("sprites/addons.pak")));var info=store.create(account,appearance).characters().getFirst();check(store.savePosition(account,info.id(),start),"spawn on grass");
 
 store.search(account,info.id(),new SearchPackets.Request(SearchPackets.ADD_POKEMON,1,5),true);
 store.search(account,info.id(),new SearchPackets.Request(SearchPackets.ADD_POKEMON,6,20),true);
 var original=store.pcParty(account,info.id()).party();var damaged=new ArrayList<PartyMember>(original);var firstMon=damaged.getFirst();damaged.set(0,new PartyMember(firstMon.species(),firstMon.shiny(),firstMon.alpha(),1,firstMon.maxHp(),firstMon.level(),firstMon.individual()));store.saveBattleParty(account,info.id(),original,damaged,0);
 var alternate=store.create(account,new Appearance("Z"+suffix,0,2,0,ids,new int[12])).characters().stream().filter(c->!c.appearance().name().equals(appearance.name())).findFirst().orElseThrow();
 var keys=HandshakeKeys.generate();Path pub=Path.of("target/battle-reconnect.public");Files.write(pub,keys.getPublic().getEncoded());System.setProperty("pokemmo.login.publicKey",pub.toString());
 try(var server=new GameServer(new InetSocketAddress("127.0.0.1",0),1,100,keys.getPrivate(),keys.getPublic(),store)){Thread.ofPlatform().daemon().start(()->{try{server.serve();}catch(Exception e){}});try(var net=new GameConnection()){
 check(await(net.connect(new GameEndpoint(account,1,"127.0.0.1",server.port(),GameTicket.issue(account,user,1,keys.getPrivate()))))==GameConnection.Result.CONNECTED,"admission");await(net.enterCharacter(info.id()));await(net.worldCommand(new byte[]{WorldPackets.LOADED}));Thread.sleep(520);await(net.worldCommand(WorldPackets.settings(true,8,false)));
 check(await(net.trainerBattle()).status()==0,"no adjacent conversation cannot challenge");
 var npc=await(net.npcCommand(NpcPackets.request(1,3)));check(npc.talking()==3&&npc.dialogueText()==0,"adjacent Rick introduction");
 var ready=await(net.trainerBattle());check(ready.status()==1&&ready.id()==102,"authoritative trainer ID");
 check(ready.party().size()==2&&ready.party().get(0).species()==13&&ready.party().get(1).species()==10,"ROM Rick full team");
 check(ready.party().stream().allMatch(m->m.level()==6),"ROM exact levels");
 check(ready.party().get(0).individual().moves().stream().map(PokemonIndividual.Move::id).toList().equals(List.of(40,81)),"ROM default learnset");
 await(net.npcCommand(NpcPackets.request(2,255)));
 Position p=start;int direction=3;check(!await(net.worldCommand(WorldPackets.move(p.x(),p.y(),direction,false))).moved(),"trainer locks movement");
 var battle=await(net.battleCommand(new BattlePackets.Request(BattlePackets.START,0,0,0)));
check(battle.accepted()&&battle.outcome()==0&&battle.battle()>0,"real engine created");
check(battle.party().equals(store.pcParty(account,info.id()).party()),"server initial state");
var bad=await(net.battleCommand(new BattlePackets.Request(BattlePackets.MOVE,battle.battle(),battle.revision(),99)));
check(!bad.accepted()&&bad.revision()==battle.revision(),"invalid move rejected");
check(await(net.configurePokemon(new PokemonConfigPackets.Request(battle.party().getFirst().individual().id(),PokemonConfigPackets.Action.SHINY,0,1))).status()!=PokemonConfigPackets.Status.SUCCESS,"config locked");
var cannotRun=await(net.battleCommand(new BattlePackets.Request(BattlePackets.RUN,battle.battle(),battle.revision(),0)));check(!cannotRun.accepted(),"trainer cannot escape");

 int turns=0;
 while(!battle.forceSwitch()&&battle.outcome()==0&&turns++<20){int slot=battle.legalMoves().getFirst();for(var o:battle.options())if(o.id()==45&&battle.legalMoves().contains(o.slot()))slot=o.slot();battle=await(net.battleCommand(new BattlePackets.Request(BattlePackets.MOVE,battle.battle(),battle.revision(),slot)));}
 check(battle.forceSwitch(),"forced replacement reached before disconnect");var beforeDisconnect=battle;net.close();
 long deadline=System.nanoTime()+5_000_000_000L;while(server.onlineCount()!=0&&System.nanoTime()<deadline)Thread.sleep(10);check(server.onlineCount()==0,"disconnect cleanup");
 try(var next=new GameConnection()){
 check(await(next.connect(new GameEndpoint(account,1,"127.0.0.1",server.port(),GameTicket.issue(account,user,1,keys.getPrivate()))))==GameConnection.Result.CONNECTED,"reconnect admission");
 await(next.enterCharacter(alternate.id()));check(await(next.resumeBattle()).state()==null,"other character cannot recover battle");
 await(next.enterCharacter(info.id()));await(next.worldCommand(new byte[]{WorldPackets.LOADED}));var recovered=await(next.resumeBattle());
 Files.write(Path.of("target/battle-reconnect-forced.bin"),BattleResumePackets.response(recovered));
 verifyAutomaticEntry(next,store.list(account).stream().filter(c->c.id()==info.id()).findFirst().orElseThrow(),recovered.state().battle());
 check(recovered.trainerBattle()&&recovered.trainer().id()==102&&recovered.opponents().size()==2,"same trainer and full opposing team");
 check(recovered.state().battle()==beforeDisconnect.battle()&&recovered.state().revision()>=beforeDisconnect.revision(),"same live engine and revision");check(recovered.state().forceSwitch()&&recovered.state().party().equals(beforeDisconnect.party()),"forced switch HP PP status retained");check(recovered.state().enemyHp()==beforeDisconnect.enemyHp()&&recovered.state().events().isEmpty(),"enemy HP retained and no turn replay");
 battle=recovered.state();check(!await(next.battleCommand(new BattlePackets.Request(BattlePackets.MOVE,battle.battle(),0,1))).accepted(),"stale turn cannot replay");
 battle=await(next.battleCommand(new BattlePackets.Request(BattlePackets.SWITCH,battle.battle(),battle.revision(),1)));check(battle.accepted()&&battle.active()==1,"continue replacement after reconnect");
 turns=0;while(battle.outcome()==0&&turns++<100){if(battle.forceSwitch()){int slot=-1;for(int i=0;i<battle.party().size();i++)if(i!=battle.active()&&battle.party().get(i).hp()>0)slot=i;check(slot>=0,"alive replacement");battle=await(next.battleCommand(new BattlePackets.Request(BattlePackets.SWITCH,battle.battle(),battle.revision(),slot)));}else{int slot=battle.legalMoves().getFirst();var active=battle.party().get(battle.active());for(var o:battle.options())try{if(PokemonCatalog.move(active.species(),o.id()).power()>0){slot=o.slot();break;}}catch(Exception ignored){}battle=await(next.battleCommand(new BattlePackets.Request(BattlePackets.MOVE,battle.battle(),battle.revision(),slot)));}}
 check(battle.outcome()==1,"battle finishes after reconnect");
 long expBefore=beforeDisconnect.party().get(1).individual().experience();
 check(battle.party().get(1).individual().experience()>expBefore,"surviving participant gains experience: before="+expBefore+", after="+battle.party().get(1).individual().experience()+", events="+battle.events());
 check(battle.party().get(0).individual().experience()==beforeDisconnect.party().get(0).individual().experience(),"fainted participant gains no experience");
 long prize=BattleRewards.money(0,TrainerCatalog.scriptTrainer(102));
 check(prize==72,"ROM Rick prize is 72");
 check(store.stats(account,info.id(),0).money()==prize,"prize saved atomically");
 check(battle.events().stream().anyMatch(e->e.kind()==BattlePackets.EXPERIENCE),"experience notice");
 check(battle.events().stream().anyMatch(e->e.kind()==BattlePackets.PRIZE_MONEY),"money notice");
 var again=await(next.battleCommand(new BattlePackets.Request(BattlePackets.START,0,0,0)));
 check(again.party().equals(battle.party())&&again.events().isEmpty(),"START cannot award twice");
 store.saveBattleParty(account,info.id(),beforeDisconnect.party(),battle.party(),102,battle.battle(),(int)prize);
 check(store.stats(account,info.id(),0).money()==prize,"duplicate settlement ledger does not pay twice");
 var completed=battle;next.close();deadline=System.nanoTime()+5_000_000_000L;while(server.onlineCount()!=0&&System.nanoTime()<deadline)Thread.sleep(10);
 try(var finalConnection=new GameConnection()){
 check(await(finalConnection.connect(new GameEndpoint(account,1,"127.0.0.1",server.port(),GameTicket.issue(account,user,1,keys.getPrivate()))))==GameConnection.Result.CONNECTED,"completed battle reconnect");await(finalConnection.enterCharacter(info.id()));await(finalConnection.worldCommand(new byte[]{WorldPackets.LOADED}));var ending=await(finalConnection.resumeBattle());Files.write(Path.of("target/battle-reconnect-completed.bin"),BattleResumePackets.response(ending));check(store.stats(account,info.id(),0).money()==prize,"reconnect does not pay twice");check(ending.state().party().equals(completed.party()),"reconnect retains experience and levels");check(ending.state().outcome()==1&&ending.state().battle()==completed.battle(),"completed result retained until exit acknowledgement");
 await(finalConnection.encounterCommand(EncounterPackets.FINISH,true));check(await(finalConnection.resumeBattle()).state()==null,"finish acknowledgement clears recovery");check(store.defeatedTrainers(account,info.id()).contains(102),"trainer victory persists");
 await(finalConnection.leaveWorld());var grass=findPair(new FireRedWorldRules(),0);store.savePosition(account,info.id(),grass[0]);await(finalConnection.enterCharacter(info.id()));await(finalConnection.worldCommand(new byte[]{WorldPackets.LOADED}));Thread.sleep(520);await(finalConnection.worldCommand(WorldPackets.settings(false,8,true)));
 Position current=grass[0];int wildDirection=grass[1].facing();EncounterCatalog.Selection selected=null;
 for(int step=0;step<240&&selected==null;step++){Thread.sleep(40);var moved=await(finalConnection.worldCommand(WorldPackets.move(current.x(),current.y(),wildDirection,false)));check(moved.moved(),"grass movement");current=moved.position();wildDirection^=1;if(moved.encounter()!=null){if(moved.encounter().selections().size()==1)selected=moved.encounter();else await(finalConnection.encounterCommand(EncounterPackets.FINISH,true));}}
 check(selected!=null,"natural single encounter");var wild=await(finalConnection.battleCommand(new BattlePackets.Request(BattlePackets.START,0,0,0)));finalConnection.close();deadline=System.nanoTime()+5_000_000_000L;while(server.onlineCount()!=0&&System.nanoTime()<deadline)Thread.sleep(10);
 try(var wildReconnect=new GameConnection()){
 check(await(wildReconnect.connect(new GameEndpoint(account,1,"127.0.0.1",server.port(),GameTicket.issue(account,user,1,keys.getPrivate()))))==GameConnection.Result.CONNECTED,"wild reconnect admission");await(wildReconnect.enterCharacter(info.id()));await(wildReconnect.worldCommand(new byte[]{WorldPackets.LOADED}));var wildSnapshot=await(wildReconnect.resumeBattle());check(!wildSnapshot.trainerBattle()&&wildSnapshot.trainer()==null&&wildSnapshot.encounter().equals(selected),"same natural encounter restored");check(wildSnapshot.state().battle()==wild.battle()&&wildSnapshot.state().party().equals(wild.party()),"wild engine and party preserved");var escaped=await(wildReconnect.battleCommand(new BattlePackets.Request(BattlePackets.RUN,wildSnapshot.state().battle(),wildSnapshot.state().revision(),0)));check(escaped.accepted()&&escaped.outcome()==4,"wild escape works after reconnect");await(wildReconnect.encounterCommand(EncounterPackets.FINISH,true));check(await(wildReconnect.resumeBattle()).state()==null,"wild recovery cleared");
 }

 }
 }
 System.out.println("PASS real TCP + PostgreSQL + Node: NPC EXP/prize persistence, fainted exclusion, growth/cap, duplicate ledger/START, reconnect rewards, isolation and wild escape");
 }}
 }finally{try(var c=db.connect()){c.setAutoCommit(false);try(var q=c.prepareStatement("DELETE FROM characters WHERE account_id IN (SELECT id FROM accounts WHERE username=?)")){q.setString(1,user);q.executeUpdate();}try(var q=c.prepareStatement("DELETE FROM accounts WHERE username=?")){q.setString(1,user);q.executeUpdate();}c.commit();}db.close();}
 }

 static void set(Object o,String n,Object value)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,value);}
 static Object get(Object o,String n)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static void verifyAutomaticEntry(GameConnection net,CharacterInfo info,long battle)throws Exception{
 org.lwjgl.glfw.GLFW.glfwInit();org.lwjgl.glfw.GLFW.glfwWindowHint(org.lwjgl.glfw.GLFW.GLFW_VISIBLE,0);long window=org.lwjgl.glfw.GLFW.glfwCreateWindow(1024,768,"Reconnect entry",0,0);org.lwjgl.glfw.GLFW.glfwMakeContextCurrent(window);org.lwjgl.opengl.GL.createCapabilities();org.lwjgl.opengl.GL11.glViewport(0,0,1024,768);
 try(var screen=new top.totodile.pokemmo.ui.LoginScreen()){
 screen.attachInput(window);set(screen,"gameConnection",net);set(screen,"authenticated",true);set(screen,"gameConnected",true);set(screen,"enterResult",CompletableFuture.completedFuture(info));set(screen,"worldMapResult",CompletableFuture.completedFuture(top.totodile.pokemmo.MapSources.gba(new RomFiles(ClientFiles.data("../roms")).selected(1),false)));
 long deadline=System.nanoTime()+30_000_000_000L;Object panel=get(screen,"encounterPanel");
 while(System.nanoTime()<deadline){screen.render(1024,768,1024,768);var state=(BattlePackets.State)get(panel,"battleState");if(state!=null&&get(panel,"pending")==null)break;Thread.sleep(12);}
 var state=(BattlePackets.State)get(panel,"battleState");check(state!=null&&state.battle()==battle&&state.forceSwitch(),"automatic character-entry battle recovery");check(((de.matthiasmann.twl.Widget)get(panel,"partyChoice")).isVisible(),"automatic entry displays forced replacement");check(get(screen,"battleResumeResult")==null,"entry recovery request consumed");set(screen,"gameConnection",null);
 }
 org.lwjgl.glfw.GLFW.glfwDestroyWindow(window);org.lwjgl.glfw.GLFW.glfwTerminate();
 }
 static boolean battleSlotLegal(BattlePackets.MoveOption option,BattlePackets.State battle){return battle.legalMoves().contains(option.slot());}
}

