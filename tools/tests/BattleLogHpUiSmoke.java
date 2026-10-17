package top.totodile.pokemmo.ui;
import top.totodile.pokemmo.*;
import top.totodile.pokemmo.avatar.*;
import top.totodile.pokemmo.protocol.*;
import top.totodile.pokemmo.protocol.CharacterPackets.*;
import top.totodile.pokemmo.battle.BattleHpAnimation;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.theme.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
public final class BattleLogHpUiSmoke {
    static Object get(Object o,String n)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[]args)throws Exception{
        GLFW.glfwInit();GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,0);long win=GLFW.glfwCreateWindow(1024,768,"HP",0,0);
        GLFW.glfwMakeContextCurrent(win);GL.createCapabilities();var renderer=new GlfwTwlRenderer();
        var panel=new WildEncounterPanel(()->{},m->{throw new AssertionError(m);});var chat=new ChatPanel();
        var root=new Widget(){protected void layout(){panel.setPosition(0,0);panel.setSize(getWidth(),getHeight());chat.place(2,766,1020,764);}};
        root.setTheme("");root.add(panel);root.add(chat);var gui=new GUI(root,renderer,null);
        var theme=ThemeManager.createThemeManager(ClientFiles.data("themes/default/theme.xml").toUri().toURL(),renderer);gui.applyTheme(theme);
        renderer.resize(1024,768,1024,768);gui.setSize(1024,768);GL11.glViewport(0,0,1024,768);gui.update();
        var assets=new AvatarAssets(ClientFiles.data("sprites/addons.pak"));int[] ids=new int[12],colors=new int[12];Arrays.fill(ids,-1);
        for(int cat:new int[]{2,3,6,8,9,11}){var c=assets.choices(cat,0);if(!c.isEmpty())ids[cat]=c.getFirst().id;}
        var team=List.of(new PartyMember(1,false,false,50,50,10));var options=List.of(new BattlePackets.MoveOption(1,33,35,35));
        var initial=new BattlePackets.State(true,71,1,0,0,false,30,30,team,List.of(),List.of(1),options);
        var endTeam=List.of(new PartyMember(1,false,false,35,50,10));
        var result=new BattlePackets.State(true,71,2,1,0,false,0,30,endTeam,List.of(new BattlePackets.Event(4,1,0),new BattlePackets.Event(1,1,0),new BattlePackets.Event(4,0,20),new BattlePackets.Event(4,0,35),new BattlePackets.Event(BattlePackets.EXPERIENCE,0,123),new BattlePackets.Event(BattlePackets.PRIZE_MONEY,0,456)),List.of(),options);
        var logs=new ArrayList<String>();panel.setBattleLog(m->{logs.add(m);chat.addMessage(4,m);});
        panel.startWild(new RomFiles(Path.of("roms")).selected(0),assets,new Appearance("HP",0,2,0,ids,colors),team,new EncounterCatalog.Selection(7,5,0,false),0);
        ((CompletableFuture<?>)get(panel,"pending")).join();panel.tick(0);
        panel.connectBattle(request->CompletableFuture.completedFuture(request.action()==BattlePackets.START?initial:result),members->{});
        panel.tick(4400);gui.update();var submit=WildEncounterPanel.class.getDeclaredMethod("submitBattle",int.class,int.class);submit.setAccessible(true);submit.invoke(panel,BattlePackets.MOVE,1);
        panel.tick(4500);gui.update();var enemy=(BattleHpAnimation)get(panel,"enemyHp");
        check(enemy.value(4500)==30,"damage begins at previous displayed HP");
        panel.tick(5000);gui.update();check(enemy.value(5000)>0&&enemy.value(5000)<30,"enemy drain has intermediate value");check(logs.size()==1,"faint waits for drain");
        panel.tick(5500);gui.update();check(enemy.value(5500)==0&&logs.size()==2,"drain completes then faint message");
        panel.tick(6300);gui.update();var self=(BattleHpAnimation)get(panel,"selfHp");check(self.value(6300)==50,"self drain begins at original HP");
        panel.tick(6600);gui.update();int shown=Integer.parseInt(((Label)get(panel,"hp")).getText().split(" /")[0]);
        check(shown>20&&shown<50,"numeric HP animates together with bar");
        panel.tick(6900);gui.update();check(self.value(6900)==20,"healing begins from damage target");
        panel.tick(7075);gui.update();check(self.value(7075)>20&&self.value(7075)<35,"healing interpolates upward");
        panel.tick(7250);panel.tick(8650);panel.tick(10050);gui.update();
        check(self.value(10050)==35&&enemy.value(10050)==0,"exact final values");
        check(logs.size()==7,"damage, faint, self damage/heal, experience, prize and outcome logged once");
        var records=(List<String[]>)get(chat,"records");check(records.size()==7&&records.stream().allMatch(r->r[0].equals("4")),"all logs go to Battle channel");
        for(int i=0;i<10;i++)panel.tick(10051+i);check(logs.size()==7,"frame updates do not duplicate logs");
        var select=ChatPanel.class.getDeclaredMethod("select",int.class);select.setAccessible(true);select.invoke(chat,4);chat.setExpanded(true);chat.setVisible(true);gui.update();
        check(((String)get(chat,"history")).contains(ClientStrings.get(9826)),"Battle tab displays victory");
        select.invoke(chat,0);check(((String)get(chat,"history")).isEmpty(),"battle records do not leak to Local");select.invoke(chat,4);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);gui.update();
        var pixels=org.lwjgl.BufferUtils.createByteBuffer(1024*768*4);GL11.glReadPixels(0,0,1024,768,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
        var image=new java.awt.image.BufferedImage(1024,768,java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<768;y++)for(int x=0;x<1024;x++){int off=(y*1024+x)*4;image.setRGB(x,767-y,0xff000000|((pixels.get(off)&255)<<16)|((pixels.get(off+1)&255)<<8)|(pixels.get(off+2)&255));}
        javax.imageio.ImageIO.write(image,"png",Path.of("target/battle-log-hp.png").toFile());
        int previousLogs=logs.size();
        var recoveryState=new BattlePackets.State(true,71,2,1,0,false,0,30,endTeam,List.of(),List.of(),options);
        var recovery=new BattleResumePackets.Snapshot(false,false,new EncounterCatalog.Selection(7,5,0,false),null,
                List.of(new PartyMember(7,false,false,0,30,5)),0,recoveryState);
        panel.close();
        panel.resumeBattle(new RomFiles(Path.of("roms")).selected(0),assets,new Appearance("HP",0,2,0,ids,colors),recovery,
                request->{throw new AssertionError("recovery must not submit START");},members->{},()->{throw new AssertionError("recovery failed");},20000);
        ((CompletableFuture<?>)get(panel,"pending")).join();panel.tick(20000);gui.update();
        check(logs.size()==previousLogs,"resume does not re-log historical outcome");
        check(((BattleHpAnimation)get(panel,"selfHp")).value(20000)==35&&((BattleHpAnimation)get(panel,"enemyHp")).value(20000)==0,
                "resume displays authoritative HP immediately without drain replay");
        panel.close();chat.close();gui.destroy();theme.destroy();GLFW.glfwDestroyWindow(win);GLFW.glfwTerminate();
        System.out.println("PASS real OpenGL HP drain/heal/numbers, faint ordering, exact values, Battle channel event ordering/dedup and filtering; mocked battle responses");
    }
}
