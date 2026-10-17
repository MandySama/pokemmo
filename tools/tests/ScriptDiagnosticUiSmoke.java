package top.totodile.pokemmo.ui;
import top.totodile.pokemmo.*;
import top.totodile.pokemmo.protocol.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.theme.ThemeManager;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import java.nio.file.Path;
public final class ScriptDiagnosticUiSmoke {
    static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[]args)throws Exception{
        GLFW.glfwInit();GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,0);
        long win=GLFW.glfwCreateWindow(1920,1080,"Diagnostic",0,0);
        GLFW.glfwMakeContextCurrent(win);GL.createCapabilities();
        var renderer=new GlfwTwlRenderer();int[] requests={0};
        var panel=new ScriptDiagnosticWindow(()->{},()->requests[0]++);
        var root=new Widget(){protected void layout(){panel.place(0,0,getWidth(),getHeight());}};
        root.setTheme("");root.add(panel);var gui=new GUI(root,renderer,null);
        var theme=ThemeManager.createThemeManager(ClientFiles.data("themes/default/theme.xml").toUri().toURL(),renderer);gui.applyTheme(theme);
        long now=System.nanoTime();var vm=new GbaScriptRuntime("",new CharacterPackets.Position(3,0,5,16,3,1),(op,a,b,t)->1);
        vm.onPosition(vm.position(),now);
        for(int i=0;i<100&&vm.active();i++){now+=600_000_000L;vm.tick(now);if(vm.mode()==GbaScriptRuntime.BUTTON)vm.acknowledge(vm.serial(),StoryPackets.ADVANCE,0,"",now);}
        panel.show(vm.diagnostics(null,now));
        for(int[] size:new int[][]{{1024,768},{800,600},{1920,1080},{1024,768}}){
            renderer.resize(size[0],size[1],size[0],size[1]);gui.setSize(size[0],size[1]);GL11.glViewport(0,0,size[0],size[1]);gui.update();
            check(panel.getX()>=0&&panel.getY()>=0&&panel.getRight()<=size[0]&&panel.getBottom()<=size[1],"window bounds");
        }
        var sectionField=ScriptDiagnosticWindow.class.getDeclaredField("section");sectionField.setAccessible(true);
        var section=(ComboBox<?>)sectionField.get(panel);section.setSelected(1);gui.update();
        var rowsField=ScriptDiagnosticWindow.class.getDeclaredField("rows");rowsField.setAccessible(true);
        var npcRows=(java.util.List<Label>)rowsField.get(panel);
        check(npcRows.stream().anyMatch(l->l.getText().contains(ClientStrings.get(252043))),"NPC category");
        check(npcRows.stream().noneMatch(l->l.getText().contains(ClientStrings.get(252062))),"trace filtered out of NPC category");
        section.setSelected(3);gui.update();section.setSelected(0);gui.update();
        var f=ScriptDiagnosticWindow.class.getDeclaredField("refresh");f.setAccessible(true);var refresh=(Button)f.get(panel);
        refresh.requestKeyboardFocus();gui.handleKey(Event.KEY_RETURN,Event.CHAR_NONE,true);gui.handleKey(Event.KEY_RETURN,Event.CHAR_NONE,false);
        check(requests[0]==1,"manual refresh keyboard callback");panel.busy(true);check(!refresh.isEnabled(),"in-flight refresh disabled");panel.busy(false);
        GL11.glClearColor(0,0,0,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);gui.update();
        var image=new java.awt.image.BufferedImage(1024,768,java.awt.image.BufferedImage.TYPE_INT_ARGB);
        var pixels=org.lwjgl.BufferUtils.createByteBuffer(1024*768*4);GL11.glReadPixels(0,0,1024,768,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
        for(int y=0;y<768;y++)for(int x=0;x<1024;x++){int off=(y*1024+x)*4;image.setRGB(x,767-y,0xff000000|((pixels.get(off)&255)<<16)|((pixels.get(off+1)&255)<<8)|(pixels.get(off+2)&255));}
        javax.imageio.ImageIO.write(image,"png",Path.of("target/script-diagnostic.png").toFile());
        gui.destroy();theme.destroy();GLFW.glfwDestroyWindow(win);GLFW.glfwTerminate();
        System.out.println("PASS real OpenGL/TWL diagnostic layout, resizing, scroll content and manual refresh");
    }
}
