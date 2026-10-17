package top.totodile.pokemmo.ui;
import top.totodile.pokemmo.*;import top.totodile.pokemmo.avatar.*;import top.totodile.pokemmo.protocol.*;import top.totodile.pokemmo.protocol.CharacterPackets.*;import de.matthiasmann.twl.*;import de.matthiasmann.twl.theme.*;import org.lwjgl.glfw.GLFW;import org.lwjgl.opengl.*;import java.util.*;import java.nio.file.*;
public class NpcRewardUiSmoke {
 static Object get(Object o,String n)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static void key(GUI gui,int code){gui.handleKey(code,Event.CHAR_NONE,true);gui.handleKey(code,Event.CHAR_NONE,false);}
 public static void main(String[]args)throws Exception{
 GLFW.glfwInit();GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,0);long win=GLFW.glfwCreateWindow(2560,1600,"Battle resize",0,0);GLFW.glfwMakeContextCurrent(win);GL.createCapabilities();var r=new GlfwTwlRenderer();var panel=new WildEncounterPanel(()->{},m->{throw new AssertionError(m);});var root=new Widget(){protected void layout(){panel.setPosition(0,0);panel.setSize(getWidth(),getHeight());}};root.setTheme("");root.add(panel);var gui=new GUI(root,r,null);var theme=ThemeManager.createThemeManager(ClientFiles.data("themes/default/theme.xml").toUri().toURL(),r);gui.applyTheme(theme);
 var assets=new AvatarAssets(ClientFiles.data("sprites/addons.pak"));int[] ids=new int[12],colors=new int[12];Arrays.fill(ids,-1);for(int cat:new int[]{2,3,6,8,9,11}){var c=assets.choices(cat,0);if(!c.isEmpty())ids[cat]=c.getFirst().id;}

 r.resize(1024,768,1024,768);gui.setSize(1024,768);GL11.glViewport(0,0,1024,768);gui.update();
 var d=PokemonIndividual.create(1,5,false,1,"Rewards",0,0,0,0);
 var mon=new PartyMember(1,false,false,d.stat(1,5,0),d.stat(1,5,0),5,d);
 var grown=BattleRewards.grow(mon,600);var team=List.of(mon);
 panel.startWild(new RomFiles(Path.of("roms")).selected(0),assets,new Appearance("Rewards",0,2,0,ids,colors),team,new EncounterCatalog.Selection(7,5,0,false),0);
 ((java.util.concurrent.CompletableFuture<?>)get(panel,"pending")).join();panel.tick(0);
 var result=new BattlePackets.State(true,1,2,1,0,false,0,20,List.of(grown),List.of(new BattlePackets.Event(BattlePackets.EXPERIENCE,0,600),new BattlePackets.Event(BattlePackets.LEVEL_UP,0,grown.level()),new BattlePackets.Event(BattlePackets.PRIZE_MONEY,0,72)),List.of());
 panel.connectBattle(request->java.util.concurrent.CompletableFuture.completedFuture(result),members->{});
 panel.tick(4400);gui.update();check(((String)get(panel,"serverMessage")).contains("600"),"experience notice");
 panel.tick(5801);gui.update();check(((String)get(panel,"serverMessage")).contains("升到了"),"level notice");
 check(((PartyMember)get(panel,"self")).level()==grown.level(),"HUD level updated");
 panel.tick(7202);gui.update();check(((String)get(panel,"serverMessage")).contains("72"),"prize notice");
 var label=(Label)get(panel,"message");check(label.isVisible()&&label.getText().contains("72")&&label.getWidth()>0&&label.getHeight()>0,"visible prize label");check(!((Button[])get(panel,"actions"))[0].isVisible(),"actions hidden during rewards: transport="+get(panel,"battleTransport")+", pending="+get(panel,"resultToApply")+", class="+panel.getClass().getProtectionDomain().getCodeSource().getLocation());gui.update();
 var buffer=java.nio.ByteBuffer.allocateDirect(1024*768*4);GL11.glReadPixels(0,0,1024,768,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,buffer);
 var image=new java.awt.image.BufferedImage(1024,768,java.awt.image.BufferedImage.TYPE_INT_ARGB);
 for(int y=0;y<768;y++)for(int x=0;x<1024;x++){int i=(y*1024+x)*4;image.setRGB(x,767-y,0xff000000|(buffer.get(i)&255)<<16|(buffer.get(i+1)&255)<<8|(buffer.get(i+2)&255));}
 javax.imageio.ImageIO.write(image,"png",Path.of("target/npc-reward-ui.png").toFile());
 panel.tick(8603);check(((BattlePackets.State)get(panel,"battleState")).party().equals(List.of(grown)),"final result retained");
 panel.close();gui.destroy();theme.destroy();GLFW.glfwDestroyWindow(win);GLFW.glfwTerminate();
 System.out.println("PASS reward messages and grown HUD in real OpenGL/TWL (simulated server result)");
 }
}
