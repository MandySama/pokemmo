package top.totodile.pokemmo.ui;
import top.totodile.pokemmo.*;
import top.totodile.pokemmo.avatar.*;
import top.totodile.pokemmo.protocol.*;
import top.totodile.pokemmo.protocol.CharacterPackets.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.theme.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.file.*;
public final class BattleStatusFeedbackUiSmoke {
    static Object get(Object o,String n)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
    static void set(Object o,String n,Object v)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[]args)throws Exception{
        GLFW.glfwInit();GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,0);long win=GLFW.glfwCreateWindow(1920,1080,"Status",0,0);
        GLFW.glfwMakeContextCurrent(win);GL.createCapabilities();var renderer=new GlfwTwlRenderer();
        var panel=new WildEncounterPanel(()->{},m->{throw new AssertionError(m);});var chat=new ChatPanel();
        var root=new Widget(){protected void layout(){panel.setPosition(0,0);panel.setSize(getWidth(),getHeight());chat.place(2,getHeight()-2,getWidth()-4,getHeight()-4);}};
        root.setTheme("");root.add(panel);root.add(chat);var gui=new GUI(root,renderer,null);
        var theme=ThemeManager.createThemeManager(ClientFiles.data("themes/default/theme.xml").toUri().toURL(),renderer);gui.applyTheme(theme);
        renderer.resize(1024,768,1024,768);gui.setSize(1024,768);GL11.glViewport(0,0,1024,768);gui.update();
        var assets=new AvatarAssets(ClientFiles.data("sprites/addons.pak"));int[] ids=new int[12],colors=new int[12];Arrays.fill(ids,-1);
        for(int cat:new int[]{2,3,6,8,9,11}){var c=assets.choices(cat,0);if(!c.isEmpty())ids[cat]=c.getFirst().id;}
        var initial=BattlePackets.decode(Files.readAllBytes(Path.of("target/battle-status-initial.bin")));
        var result=BattlePackets.decode(Files.readAllBytes(Path.of("target/battle-status-result.bin")));
        var appearance=new Appearance("Status",0,2,0,ids,colors);var rom=new RomFiles(Path.of("roms")).selected(0);
        var logs=new ArrayList<String>();panel.setBattleLog(m->{logs.add(m);chat.addMessage(4,m);});
        panel.startWild(rom,assets,appearance,initial.party(),new EncounterCatalog.Selection(143,50,0,false),0);
        ((CompletableFuture<?>)get(panel,"pending")).join();panel.tick(0);
        panel.connectBattle(request->CompletableFuture.completedFuture(request.action()==BattlePackets.START?initial:result),members->{});
        panel.tick(4400);gui.update();var submit=WildEncounterPanel.class.getDeclaredMethod("submitBattle",int.class,int.class);submit.setAccessible(true);submit.invoke(panel,BattlePackets.MOVE,1);
        long now=4500;
        for(int i=0;i<200;i++){panel.tick(now);gui.update();var pending=(CompletableFuture<?>)get(panel,"pendingMove");if(pending!=null)pending.join();
            var current=(BattlePackets.State)get(panel,"battleState");if(current.revision()==result.revision()&&get(panel,"resultToApply")==null)break;
            now+=100;
        }
        var enemyStatus=(Label)get(panel,"enemyStatusLabel");var selfStatus=(Label)get(panel,"selfStatusLabel");
        check(enemyStatus.isVisible()&&enemyStatus.getText().equals(ClientStrings.get(252132)),"real engine paralysis displayed persistently");
        check(logs.stream().anyMatch(m->m.contains(ClientStrings.get(252132))),"real status event in battle log");
        var event=WildEncounterPanel.class.getDeclaredMethod("eventMessage",BattlePackets.Event.class,String.class);event.setAccessible(true);
        for(int status=1;status<=6;status++){
            event.invoke(panel,new BattlePackets.Event(2,0,status),"Self");gui.update();
            check(selfStatus.isVisible()&&selfStatus.getText().equals(ClientStrings.get(252130+status)),"self status badge "+status);
        }
        event.invoke(panel,new BattlePackets.Event(BattlePackets.CURE_STATUS,0,6),"Self");gui.update();check(!selfStatus.isVisible(),"cure clears HUD");
        for(var pair:List.of(new int[]{BattlePackets.FAIL,2,252139},new int[]{BattlePackets.FAIL,0,252140},new int[]{BattlePackets.IMMUNE,0,252141},new int[]{BattlePackets.CANT,3,252153},new int[]{BattlePackets.BLOCK,182,252142},new int[]{BattlePackets.NOTHING,0,252144})){
            event.invoke(panel,new BattlePackets.Event(pair[0],1,pair[1]),"Enemy");
            String expected=ClientStrings.get(pair[2]).replace("{00}","Enemy").replace("{01}",pair[0]==BattlePackets.BLOCK?ClientStrings.get(110182):ClientStrings.get(252132));
            check(get(panel,"serverMessage").equals(expected),"localized failure reason "+pair[0]);
        }
        var failed=new BattlePackets.State(true,result.battle(),result.revision()+1,0,result.active(),false,result.enemyHp(),result.enemyMaxHp(),result.party(),
                List.of(new BattlePackets.Event(0,0,105),new BattlePackets.Event(BattlePackets.FAIL,0,0)),result.legalMoves(),result.options(),result.enemyActive(),2);
        set(panel,"pendingBattle",CompletableFuture.completedFuture(failed));panel.tick(now+100);gui.update();
        check(get(panel,"pendingMove")==null,"failed move skips hit particles");panel.tick(now+101);gui.update();
        check(get(panel,"serverMessage").equals(ClientStrings.get(252140)),"queued failure replaces use-move message");
        check(logs.getLast().equals(ClientStrings.get(252140)),"queued failure reaches Battle log");panel.tick(now+1000);gui.update();
        for(int[] size:new int[][]{{800,600},{1920,1080},{1024,768}}){renderer.resize(size[0],size[1],size[0],size[1]);gui.setSize(size[0],size[1]);GL11.glViewport(0,0,size[0],size[1]);gui.update();check(enemyStatus.getRight()<panel.getRight()&&enemyStatus.getX()>=panel.getX(),"badge responsive bounds");}
        var select=ChatPanel.class.getDeclaredMethod("select",int.class);select.setAccessible(true);select.invoke(chat,4);chat.setExpanded(true);chat.setVisible(true);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);gui.update();
        var pixels=org.lwjgl.BufferUtils.createByteBuffer(1024*768*4);GL11.glReadPixels(0,0,1024,768,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
        var image=new java.awt.image.BufferedImage(1024,768,java.awt.image.BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<768;y++)for(int x=0;x<1024;x++){int off=(y*1024+x)*4;image.setRGB(x,767-y,0xff000000|((pixels.get(off)&255)<<16)|((pixels.get(off+1)&255)<<8)|(pixels.get(off+2)&255));}
        javax.imageio.ImageIO.write(image,"png",Path.of("target/battle-status-feedback.png").toFile());
        panel.close();int previousLogs=logs.size();
        var restored=new BattleResumePackets.Snapshot(false,false,new EncounterCatalog.Selection(143,50,0,false),null,
                List.of(new PartyMember(143,false,false,result.enemyHp(),result.enemyMaxHp(),50)),0,
                new BattlePackets.State(true,result.battle(),result.revision(),0,0,false,result.enemyHp(),result.enemyMaxHp(),result.party(),List.of(),result.legalMoves(),result.options(),0,2));
        panel.resumeBattle(rom,assets,appearance,restored,request->{throw new AssertionError("recovery sent START");},members->{},()->{throw new AssertionError("recovery failed");},now+2000);
        ((CompletableFuture<?>)get(panel,"pending")).join();panel.tick(now+2000);gui.update();
        check(enemyStatus.isVisible()&&enemyStatus.getText().equals(ClientStrings.get(252132)),"restored enemy status visible without old events");
        check(logs.size()==previousLogs,"restore does not repeat old status announcements");
        panel.close();chat.close();gui.destroy();theme.destroy();GLFW.glfwDestroyWindow(win);GLFW.glfwTerminate();
        System.out.println("PASS real engine result/OpenGL: six badges, cure, failure/immune/cant/block/nothing text, queued failure/log, no hit animation, resize and status recovery");
    }
}
