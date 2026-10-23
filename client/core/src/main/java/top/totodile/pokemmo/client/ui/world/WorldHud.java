package top.totodile.pokemmo.client.ui.world;

import com.badlogic.gdx.Input;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.util.function.IntFunction;
import top.totodile.pokemmo.client.config.ClientSettings;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.world.WorldScene;
import top.totodile.pokemmo.client.ui.menu.MobileSideMenu;
import top.totodile.pokemmo.client.ui.menu.MobileStatusIcons;

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
    private final MobileStatusIcons mobileStatus;
    private final MobileSideMenu mobileMenu;
    private Runnable bagAction=()->{},trainerAction=()->{},dexAction=()->{},mailAction=()->{};
    private top.totodile.pokemmo.client.ui.pokemon.PartyPanel party;
    private java.util.function.Consumer<String> command=line->{};
    public WorldHud(WorldScene world,ClientStrings strings,boolean mobile,ClientSettings preferences,IntFunction<Image> icons,
                    Runnable settings,Runnable logout,Runnable exit) {
        this(world,strings,mobile,preferences,icons,settings,logout,exit,()->-1);
    }
    public WorldHud(WorldScene world,ClientStrings strings,boolean mobile,ClientSettings preferences,IntFunction<Image> icons,
                    Runnable settings,Runnable logout,Runnable exit,java.util.function.IntSupplier batteryPercent) {
        this.world=world;this.mobile=mobile;setTheme("");
        status=new WorldStatus(world,strings,System::currentTimeMillis);add(status);
        bar.setTheme("/world-bar");menu.setTheme("/world-menu");chat.setTheme("/world-chat");
        int[] texts={1100,1101,1126,1161,1,8000,2353,1116,1102};
        int[] ids={5436,5432,5437,5001,5431,5471,5452,5459,5626};
        String[] bindings={"bag","trainer","friendlist",null,"codex",null,null,null,"gamemenu"};
        int[] keys={Input.Keys.B,Input.Keys.C,Input.Keys.M,0,Input.Keys.N,0,0,0,Input.Keys.D};
        // ChatBadgePrefixLabel: name + configured key, centred above the shortcut.
        class ShortcutButton extends Button implements GUI.TooltipPositionAdjuster {
            private final int index;
            private Image icon;
            ShortcutButton(int index){this.index=index;}
            @Override protected void applyTheme(ThemeInfo theme) {super.applyTheme(theme);icon=icons.apply(ids[index]);}
            @Override public Object getTooltipContent() {
                String name=strings.get(texts[index]);
                if(mobile||bindings[index]==null)return name;
                try {
                    int key=Integer.parseInt(preferences.get("client.controls.gdx."+bindings[index],Integer.toString(keys[index])));
                    String keyName=key>0&&key<=255?Input.Keys.toString(key):null;
                    return keyName==null?name:name+" ("+keyName+")";
                }catch(NumberFormatException invalid){return name;}
            }
            @Override public void adjustTooltipPosition(GUI.TooltipPositionAdjuster.TooltipPositionData position) {
                position.x=getX()+getWidth()/2;position.y=getY();position.alignment=Alignment.BOTTOM;
            }
            @Override protected void paintWidget(GUI gui) {
                super.paintWidget(gui);
                if(icon!=null)icon.draw(getAnimationState(),getX(),getY()+(getHeight()-24)/2,24,24);
            }
        }
        for(int i=0;i<shortcuts.length;i++) {
            Button button=new ShortcutButton(i);
            button.setTheme("shortcut");button.setCanAcceptKeyboardFocus(false);
            shortcuts[i]=button;bar.add(button);
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
        shortcuts[0].addCallback(()->{closeMenu();bagAction.run();});shortcuts[1].addCallback(()->{closeMenu();trainerAction.run();});
        shortcuts[4].addCallback(()->{closeMenu();dexAction.run();});shortcuts[6].addCallback(()->{closeMenu();mailAction.run();});
        mobileStatus=mobile?new MobileStatusIcons(batteryPercent):null;
        mobileMenu=mobile?new MobileSideMenu(strings,mobileStatus,System::currentTimeMillis,icons):null;
        if(mobile){
            bar.setVisible(false);removeChild(status);
            mobileMenu.worldEntries(strings,icons,status,()->bagAction.run(),()->trainerAction.run(),()->dexAction.run(),()->mailAction.run(),logout,exit);
            mobileMenu.onSettings(settings);mobileStatus.onMenu(mobileMenu::open);add(mobileStatus);add(mobileMenu);
        }
    }
    private void closeMenu(){menu.setVisible(false);if(mobileMenu!=null)mobileMenu.close();invalidateLayout();}
    public void party(top.totodile.pokemmo.client.ui.pokemon.PartyPanel panel){party=panel;add(panel);if(mobileMenu!=null){removeChild(mobileMenu);add(mobileMenu);}invalidateLayout();}
    public void onCommand(java.util.function.Consumer<String> action){command=action;}
    public void openChat(){chat.setVisible(true);world.setInputBlocked(true);invalidateLayout();chatInput.requestKeyboardFocus();}
    public void submitChat(){String line=chatInput.getText();chatInput.setText("");closeOverlay();if(!line.isBlank())command.accept(line);}
    public void onBag(Runnable open){bagAction=open;}
    public void onTrainer(Runnable open){trainerAction=open;}
    public void onPokedex(Runnable open){dexAction=open;}
    public void onMail(Runnable open){mailAction=open;}
    public boolean dismissMenuOutside(int x,int y) {
        if(mobileMenu!=null&&mobileMenu.isVisible()&&!mobileMenu.contains(x,y)){mobileMenu.close();return true;}
        if(menu.isVisible()&&!menu.isInside(x,y)&&!shortcuts[8].isInside(x,y)) {
            closeMenu();return true;
        }
        return false;
    }
    public void update(){status.update();if(mobileStatus!=null){mobileStatus.update(System.nanoTime());mobileMenu.update();}}
    public boolean menuOpen(){return mobileMenu!=null&&mobileMenu.isVisible();}
    public boolean closeOverlay() {
        if(mobileMenu!=null&&mobileMenu.isVisible()){mobileMenu.close();return true;}
        if(menu.isVisible()){closeMenu();return true;}
        if(chat.isVisible()){chat.setVisible(false);world.setInputBlocked(false);invalidateLayout();return true;}
        return false;
    }
    public boolean editingChat(){return chat.isVisible();}
    public boolean ownsPointer(int x,int y) {
        return !mobile&&bar.isInside(x,y)||chatToggle.isInside(x,y)
                ||mobileStatus!=null&&mobileStatus.isVisible()&&mobileStatus.isInside(x,y)||mobileMenu!=null&&mobileMenu.contains(x,y)
                ||party!=null&&party.isInside(x,y)
                ||menu.isVisible()&&menu.isInside(x,y)||chat.isVisible()&&chat.isInside(x,y);
    }
    public void position(int x,int y,int width,int height){setPosition(x,y);setSize(width,height);}
    @Override protected void layout() {
        if(party!=null){int w=Math.min(party.getPreferredWidth(),getInnerWidth()),h=Math.max(0,Math.min(party.getPreferredHeight(),getInnerHeight()-90));party.setSize(w,h);party.setPosition(getInnerRight()-w-5,mobile?getInnerBottom()-h-10:getInnerY()+(getInnerHeight()-h)/2);}
        if(!mobile)status.position(getInnerX(),getInnerY());
        if(mobileStatus!=null){mobileStatus.adjustSize();mobileMenu.position();}
        int iconSize=mobile?40:24,gap=mobile?6:3;
        int barWidth=shortcuts.length*(iconSize+gap)-gap;
        bar.setSize(barWidth,iconSize+5);bar.setPosition(getInnerRight()-barWidth-5,getInnerBottom()-bar.getHeight());
        for(int i=0;i<shortcuts.length;i++) {
            shortcuts[i].setSize(iconSize,iconSize);shortcuts[i].setPosition(bar.getX()+i*(iconSize+gap),bar.getY());
        }
        chatToggle.setSize(mobile?60:24,mobile?60:24);
        chatToggle.setPosition(getInnerX()+(mobile?5:0),mobile?getInnerY()+5:getInnerBottom()-chatToggle.getHeight());
        chat.setSize(Math.max(0,Math.min(500,getInnerWidth()-10)),Math.min(220,getInnerHeight()/2));
        chat.setPosition(getInnerX()+5,mobile?chatToggle.getBottom():chatToggle.getY()-chat.getHeight());
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
