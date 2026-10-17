package top.totodile.pokemmo.ui;
import top.totodile.pokemmo.*;import top.totodile.pokemmo.avatar.*;import top.totodile.pokemmo.protocol.*;import top.totodile.pokemmo.protocol.CharacterPackets.*;import de.matthiasmann.twl.*;import de.matthiasmann.twl.theme.*;import org.lwjgl.glfw.GLFW;import org.lwjgl.opengl.*;import java.util.*;import java.nio.file.*;
public class BattleReplacementUiSmoke {
 static Object get(Object o,String n)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void key(GUI gui,int code){gui.handleKey(code,Event.CHAR_NONE,true);gui.handleKey(code,Event.CHAR_NONE,false);}
 public static void main(String[]args)throws Exception{
 GLFW.glfwInit();GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,0);long win=GLFW.glfwCreateWindow(2560,1600,"Battle resize",0,0);GLFW.glfwMakeContextCurrent(win);GL.createCapabilities();var r=new GlfwTwlRenderer();var panel=new WildEncounterPanel(()->{},m->{throw new AssertionError(m);});var root=new Widget(){protected void layout(){panel.setPosition(0,0);panel.setSize(getWidth(),getHeight());}};root.setTheme("");root.add(panel);var gui=new GUI(root,r,null);var theme=ThemeManager.createThemeManager(ClientFiles.data("themes/default/theme.xml").toUri().toURL(),r);gui.applyTheme(theme);
 var assets=new AvatarAssets(ClientFiles.data("sprites/addons.pak"));int[] ids=new int[12],colors=new int[12];Arrays.fill(ids,-1);for(int cat:new int[]{2,3,6,8,9,11}){var c=assets.choices(cat,0);if(!c.isEmpty())ids[cat]=c.getFirst().id;}

 r.resize(1024,768,1024,768);gui.setSize(1024,768);GL11.glViewport(0,0,1024,768);gui.update();
 var team=List.of(new PartyMember(1,false,false,50,50,10),new PartyMember(7,false,false,30,30,10));
 var options=List.of(new BattlePackets.MoveOption(3,33,20,35),new BattlePackets.MoveOption(1,45,0,40),new BattlePackets.MoveOption(4,22,10,25),new BattlePackets.MoveOption(2,74,20,20));
 var initial=new BattlePackets.State(true,1,1,0,0,false,30,30,team,List.of(),List.of(3,4,2),options);
 var requests=new ArrayList<BattlePackets.Request>();
 var reply=new java.util.concurrent.CompletableFuture<BattlePackets.State>();
 panel.startWild(new RomFiles(Path.of("roms")).selected(0),assets,new Appearance("Controls",0,2,0,ids,colors),team,new EncounterCatalog.Selection(7,5,0,false),0);
 ((java.util.concurrent.CompletableFuture<?>)get(panel,"pending")).join();panel.tick(0);
 panel.connectBattle(request->{requests.add(request);return request.action()==BattlePackets.START?java.util.concurrent.CompletableFuture.completedFuture(initial):reply;},members->{});
 panel.tick(4400);gui.update();Button[] actions=(Button[])get(panel,"actions");check(actions[0].hasKeyboardFocus(),"initial action focus");
 key(gui,Event.KEY_DOWN);check(actions[2].hasKeyboardFocus(),"down to party");key(gui,Event.KEY_RIGHT);check(actions[3].hasKeyboardFocus(),"right to run");key(gui,Event.KEY_LEFT);check(actions[2].hasKeyboardFocus(),"left to party");key(gui,Event.KEY_UP);check(actions[0].hasKeyboardFocus(),"up to fight");
 key(gui,Event.KEY_RETURN);panel.tick(4401);gui.update();Button[] moves=(Button[])get(panel,"moveSlots");check(moves[0].isVisible()&&moves[0].hasKeyboardFocus(),"keyboard opens move grid");check(!moves[1].isEnabled(),"zero PP disabled");
 key(gui,Event.KEY_DOWN);check(moves[2].hasKeyboardFocus(),"move grid down");key(gui,Event.KEY_RIGHT);check(moves[3].hasKeyboardFocus(),"move grid right");
 Widget custom=(Widget)get(panel,"testMove"),back=(Widget)get(panel,"moveBack");check(custom.getX()>moves[3].getRight()&&custom.getY()==moves[3].getY(),"custom picker right of bottom-right slot");check(custom.getRight()<back.getX(),"custom and back do not overlap");
 javax.imageio.ImageIO.write(BattleSceneUiSmoke.shot(1024,768),"png",Path.of("target/battle-replacement-grid.png").toFile());
 key(gui,Event.KEY_J);check(requests.size()==2&&requests.getLast().slot()==2,"confirm submits authoritative slot");
 reply.complete(new BattlePackets.State(true,1,2,0,0,false,30,30,team,List.of(new BattlePackets.Event(0,0,74),new BattlePackets.Event(3,0,0)),List.of(3,4,2),options));
 panel.tick(4402);check(get(panel,"pendingMove")==null&&get(panel,"moveParticles")==null,"miss skips texture load and particles");
 var encounter=(top.totodile.pokemmo.battle.WildEncounter)get(panel,"encounter");check(encounter.update(4402)!=top.totodile.pokemmo.battle.WildEncounter.Phase.MOVE,"miss skips actor animation");panel.tick(4403);check(!((String)get(panel,"serverMessage")).isEmpty(),"miss feedback preserved");panel.tick(5304);panel.tick(5305);gui.update();check(panel.readyForOutcome(),"miss returns to controls");
 key(gui,Event.KEY_SPACE);panel.tick(5306);gui.update();check(moves[0].isVisible(),"space confirmation");key(gui,Event.KEY_ESCAPE);panel.tick(5307);gui.update();check(actions[0].isVisible(),"cancel restores controls");

 var hitReply=new java.util.concurrent.CompletableFuture<BattlePackets.State>();
 panel.connectBattle(request->{requests.add(request);return request.action()==BattlePackets.START?java.util.concurrent.CompletableFuture.completedFuture(initial):hitReply;},members->{});
 panel.tick(5400);gui.update();key(gui,Event.KEY_RETURN);panel.tick(5401);gui.update();
 var picker=(ComboBox<String>)get(panel,"testMove");picker.setSelected(((List<Integer>)get(panel,"testMoveIds")).indexOf(52)+1);moves[0].requestKeyboardFocus();key(gui,Event.KEY_J);
 check(requests.getLast().slot()==3 && requests.getLast().replacementMove()==52,"actual replacement ID and authoritative slot submitted"); picker.setSelected(((List<Integer>)get(panel,"testMoveIds")).indexOf(1)+1);
 hitReply.complete(new BattlePackets.State(true,1,3,0,0,false,20,30,team,List.of(new BattlePackets.Event(0,0,52),new BattlePackets.Event(4,1,20)),List.of(3,4,2),List.of(new BattlePackets.MoveOption(3,52,24,25),options.get(1),options.get(2),options.get(3))));
 panel.tick(5402);check(encounter.moveId()==52&&get(panel,"pendingMove")!=null,"hit uses authoritative server move even if picker changes while waiting");
 ((java.util.concurrent.CompletableFuture<?>)get(panel,"pendingMove")).join();panel.tick(5403);gui.update();check(get(panel,"moveParticles")!=null,"normal hit loads particles");
 for(int i=0;i<8;i++){panel.tick(200000+i*1000);gui.update();}
 check(((Label)get(moves[0],"title")).getText().equals(ClientStrings.get(110052)),"replacement actual name displayed");
 check(((Label)get(moves[0],"pp")).getText().contains("24 / 25"),"replacement actual PP displayed");

 panel.close();
 panel.startWild(new RomFiles(Path.of("roms")).selected(0),assets,new Appearance("Controls",0,2,0,ids,colors),team,new EncounterCatalog.Selection(7,5,0,false),0);
 ((java.util.concurrent.CompletableFuture<?>)get(panel,"pending")).join();panel.tick(0);panel.tick(4400);gui.update();key(gui,Event.KEY_RETURN);panel.tick(4401);gui.update();
 moves=(Button[])get(panel,"moveSlots");moves[0].requestKeyboardFocus();key(gui,Event.KEY_J);check(get(panel,"pendingMove")!=null,"offline preview loads asynchronously");key(gui,Event.KEY_ESCAPE);check(get(panel,"pendingMove")==null&&((Number)get(panel,"requestedPreviewMove")).intValue()==0,"cancel clears pending preview");panel.tick(4402);gui.update();key(gui,Event.KEY_RETURN);panel.tick(4403);gui.update();
 for(int[]d:new int[][]{{800,600},{1920,1080},{1024,768}}){r.resize(d[0],d[1],d[0],d[1]);gui.setSize(d[0],d[1]);gui.update();check(custom.getX()>moves[3].getRight()&&custom.getRight()<back.getX(),"responsive custom placement");}
 check(((top.totodile.pokemmo.battle.WildEncounter)get(panel,"encounter")).update(4403)==top.totodile.pokemmo.battle.WildEncounter.Phase.SELECT_MOVE,"reopening does not play canceled preview");
 panel.close();gui.destroy();theme.destroy();GLFW.glfwDestroyWindow(win);GLFW.glfwTerminate();System.out.println("PASS real OpenGL/TWL replacement request ID/slot, authoritative animation, PP controls, navigation, miss, async cancel and resize");
 }
}
