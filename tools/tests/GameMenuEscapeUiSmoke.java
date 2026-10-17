package top.totodile.pokemmo.ui;

import top.totodile.pokemmo.world.ClientWorld;
import top.totodile.pokemmo.protocol.CharacterPackets.Position;
import de.matthiasmann.twl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import java.lang.reflect.*;

/** 使用实际 GLFW 回调与 TWL 弹窗，世界替身只统计输入是否穿透。 */
public final class GameMenuEscapeUiSmoke {
    private static Object get(Object o,String n)throws Exception {var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
    private static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
    private static void click(GUI gui,Widget w){int x=w.getX()+w.getWidth()/2,y=w.getY()+w.getHeight()/2;gui.handleMouse(x,y,-1,false);gui.handleMouse(x,y,0,true);gui.handleMouse(x,y,0,false);}
    public static void main(String[]args)throws Exception{
        check(GLFW.glfwInit(),"GLFW");GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,0);
        long window=GLFW.glfwCreateWindow(1024,768,"Escape",0,0);GLFW.glfwMakeContextCurrent(window);GL.createCapabilities();GL11.glViewport(0,0,1024,768);
        try(var screen=new LoginScreen()){
            screen.attachInput(window);screen.render(1024,768,1024,768);
            int[] escapeLeaks={0}, movement={0};
            var world=(ClientWorld)Proxy.newProxyInstance(ClientWorld.class.getClassLoader(),new Class<?>[]{ClientWorld.class},(proxy,method,values)->{
                if(method.getName().equals("key")){if((int)values[0]==GLFW.GLFW_KEY_ESCAPE)escapeLeaks[0]++;else movement[0]++;}
                if(method.getName().equals("position"))return new Position(3,0,5,16,3,1);
                if(method.getReturnType()==boolean.class)return false;
                if(method.getReturnType()==int.class)return method.getName().equals("speedMultiplier")?1:0;
                if(method.getReturnType()==String.class)return "Test";
                return null;
            });
            var worldField=LoginScreen.class.getDeclaredField("world");worldField.setAccessible(true);worldField.set(screen,world);
            ((Widget)get(screen,"loginWindow")).setVisible(false);
            var hud=(GameHudBar)get(screen,"gameHud");hud.setGameVisible(true);
            var gui=(GUI)get(screen,"gui");gui.update();
            var menu=(GameMenuPopup)get(screen,"gameMenu");
            var key=GLFW.glfwSetKeyCallback(window,null);
            try{
                for(int i=0;i<40;i++){
                    key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_PRESS,0);
                    check(menu.isOpen()==(i%2==0),"rapid press opens/closes exactly once "+i);
                    check((int)get(gui,"keyRepeatDelay")==0,"TWL Escape repeat released after dispatch");
                    for(int n=0;n<5;n++)key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_REPEAT,0);
                    gui.update();check(menu.isOpen()==(i%2==0),"native repeat cannot toggle menu");
                    key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_RELEASE,0);
                }
                // A mouse reopen after an Esc close must survive TWL's repeat timer.
                click(gui,hud.menuButton());gui.update();check(menu.isOpen(),"mouse opens menu");
                key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_PRESS,0);key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_RELEASE,0);
                check(!menu.isOpen(),"Esc closes mouse-opened menu");click(gui,hud.menuButton());gui.update();
                Thread.sleep(650);gui.update();check(menu.isOpen(),"reopened menu survives internal repeat delay");
                menu.closePopup();
                var confirm=LoginScreen.class.getDeclaredMethod("confirmGameMenu",int.class,Runnable.class);confirm.setAccessible(true);
                confirm.invoke(screen,1160,(Runnable)()->{throw new AssertionError("Esc confirmed logout");});
                check(((PopupWindow)get(screen,"menuConfirmation")).isOpen(),"confirmation opens");
                key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_PRESS,0);
                check(!((PopupWindow)get(screen,"menuConfirmation")).isOpen()&&!menu.isOpen(),"Esc only dismisses confirmation");
                key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_RELEASE,0);
                key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_PRESS,0);check(menu.isOpen(),"next press opens main menu");key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_RELEASE,0);menu.closePopup();
                var settings=LoginScreen.class.getDeclaredMethod("openSettings");settings.setAccessible(true);settings.invoke(screen);
                check(((Widget)get(screen,"settings")).isVisible(),"settings opens");
                key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_PRESS,0);check(!((Widget)get(screen,"settings")).isVisible()&&!menu.isOpen(),"Esc only closes settings");key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_RELEASE,0);
                key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_PRESS,0);check(menu.isOpen(),"Esc opens menu after settings");key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_RELEASE,0);menu.closePopup();
                var story=(ResizableFrame)get(screen,"storyMenu");story.setHardVisible(true);
                key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_PRESS,0);check(!story.isVisible()&&!menu.isOpen(),"Esc only closes story window");key.invoke(window,GLFW.GLFW_KEY_ESCAPE,0,GLFW.GLFW_RELEASE,0);
                key.invoke(window,GLFW.GLFW_KEY_W,0,GLFW.GLFW_PRESS,0);key.invoke(window,GLFW.GLFW_KEY_W,0,GLFW.GLFW_RELEASE,0);
                check(movement[0]==2,"normal movement press/release remains intact");check(escapeLeaks[0]==0,"Esc never reaches world movement");check(get(screen,"world")==world,"rapid Escape never logs out");
            }finally{GLFW.glfwSetKeyCallback(window,key);}
            System.out.println("PASS real GLFW/OpenGL/TWL: rapid Esc, held repeats, mouse reopen/timer, confirmation/settings/story ownership, no world leakage or logout");
        }finally{GLFW.glfwDestroyWindow(window);GLFW.glfwTerminate();}
    }
}

