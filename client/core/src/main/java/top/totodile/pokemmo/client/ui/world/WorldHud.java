package top.totodile.pokemmo.client.ui.world;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.util.function.IntFunction;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.world.WorldScene;

/** Basic overworld HUD. Window bodies and online game services are supplied separately. */
public final class WorldHud extends Widget {
    private final WorldScene world;
    private final WorldStatus status;
    private final Widget bar=new Widget(), menu=new Widget(), chat=new Widget();
    private final Button chatToggle=new Button();
    private final EditField chatInput=new EditField();
    private final Button[] shortcuts=new Button[9];
    private final Button[] entries=new Button[8];
    private final boolean mobile;
    public WorldHud(WorldScene world,ClientStrings strings,boolean mobile,IntFunction<Image> icons,
                    Runnable settings,Runnable logout,Runnable exit) {
        this.world=world;this.mobile=mobile;setTheme("");
        status=new WorldStatus(world,strings,System::currentTimeMillis);add(status);
        bar.setTheme("/world-bar");menu.setTheme("/world-menu");chat.setTheme("/world-chat");
        int[] texts={1100,1101,1126,1161,1,8034,2353,1116,1102};
        int[] ids={5436,5432,5437,5001,5431,5471,5452,5459,5626};
        for(int i=0;i<shortcuts.length;i++) {
            final int iconId=ids[i];
            Button button=new Button() {
                private Image icon;
                @Override protected void applyTheme(ThemeInfo theme) {super.applyTheme(theme);icon=icons.apply(iconId);}
                @Override protected void paintWidget(GUI gui) {
                    super.paintWidget(gui);
                    if(icon!=null)icon.draw(getAnimationState(),getX(),getY()+(getHeight()-24)/2,24,24);
                }
            };
            button.setTheme("shortcut");button.setCanAcceptKeyboardFocus(false);
            button.setTooltipContent(strings.get(texts[i]));shortcuts[i]=button;bar.add(button);
        }
        shortcuts[8].addCallback(()->{menu.setVisible(!menu.isVisible());invalidateLayout();});
        int[] menuTexts={1117,1111,1112,2807,7900,1124,1159,1114};
        for(int i=0;i<entries.length;i++) {
            Button entry=new Button(strings.get(menuTexts[i]));entry.setTheme("button");
            entry.setCanAcceptKeyboardFocus(false);entries[i]=entry;menu.add(entry);
        }
        entries[1].addCallback(()->{closeMenu();settings.run();});
        entries[6].addCallback(()->{closeMenu();logout.run();});
        entries[7].addCallback(()->{closeMenu();exit.run();});
        chatToggle.setTheme("/world-chat-toggle");chatToggle.setCanAcceptKeyboardFocus(false);
        chatToggle.setTooltipContent(strings.get(1208));
        chatToggle.addCallback(()->{
            chat.setVisible(!chat.isVisible());world.setInputBlocked(chat.isVisible());
            invalidateLayout();if(chat.isVisible())chatInput.requestKeyboardFocus();
        });
        chatInput.setTheme("editfield");chat.add(chatInput);
        add(bar);add(chatToggle);add(chat);add(menu);menu.setVisible(false);chat.setVisible(false);
    }
    private void closeMenu(){menu.setVisible(false);invalidateLayout();}
    public boolean dismissMenuOutside(int x,int y) {
        if(menu.isVisible()&&!menu.isInside(x,y)&&!shortcuts[8].isInside(x,y)) {
            closeMenu();return true;
        }
        return false;
    }
    public void update(){status.update();}
    public boolean closeOverlay() {
        if(menu.isVisible()){closeMenu();return true;}
        if(chat.isVisible()){chat.setVisible(false);world.setInputBlocked(false);invalidateLayout();return true;}
        return false;
    }
    public boolean editingChat(){return chat.isVisible();}
    public boolean ownsPointer(int x,int y) {
        return bar.isInside(x,y)||chatToggle.isInside(x,y)
                ||menu.isVisible()&&menu.isInside(x,y)||chat.isVisible()&&chat.isInside(x,y);
    }
    public void position(int x,int y,int width,int height){setPosition(x,y);setSize(width,height);}
    @Override protected void layout() {
        status.position(getInnerX()+5,getInnerY()+5);
        int iconSize=mobile?40:24,gap=mobile?6:3;
        int barWidth=shortcuts.length*(iconSize+gap)-gap;
        bar.setSize(barWidth,iconSize+5);bar.setPosition(getInnerRight()-barWidth-5,getInnerBottom()-bar.getHeight());
        for(int i=0;i<shortcuts.length;i++) {
            shortcuts[i].setSize(iconSize,iconSize);shortcuts[i].setPosition(bar.getX()+i*(iconSize+gap),bar.getY());
        }
        chatToggle.setSize(mobile?40:24,mobile?40:24);
        chatToggle.setPosition(getInnerX(),getInnerBottom()-chatToggle.getHeight());
        chat.setSize(Math.min(500,getInnerWidth()-10),Math.min(220,getInnerHeight()/2));
        chat.setPosition(getInnerX()+5,chatToggle.getY()-chat.getHeight());
        chatInput.setSize(chat.getInnerWidth(),chatInput.getPreferredHeight());
        chatInput.setPosition(chat.getInnerX(),chat.getInnerBottom()-chatInput.getHeight());
        int row=mobile?40:32;
        menu.setSize(148,row*entries.length+8);
        menu.setPosition(getInnerRight()-menu.getWidth()-4,bar.getY()-menu.getHeight());
        for(int i=0;i<entries.length;i++) {
            entries[i].setSize(menu.getInnerWidth(),row);
            entries[i].setPosition(menu.getInnerX(),menu.getInnerY()+i*row);
        }
    }
}
