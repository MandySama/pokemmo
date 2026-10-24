package top.totodile.pokemmo.client.ui.world;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.client.world.WorldScene;

/** 用户指定的本地地图快捷按钮，使用现有主题按钮。 */
public final class WorldActions extends Widget {
    private final WorldScene world;
    private final boolean mobile;
    private final Button noClip=new Button();
    private final Button speed=new Button();
    private final Button pokedex=new Button();
    private final Button teleport=new Button("传送"),item=new Button("添加道具"),randomItems=new Button("随机道具"),pokemon=new Button("添加宝可梦");
    private int buttonWidth;
    public WorldActions(WorldScene world) {
        this(world,false);
    }
    public WorldActions(WorldScene world,boolean mobile) {
        this.world=world;this.mobile=mobile;
        setTheme("");noClip.setTheme("/button");speed.setTheme("/button");pokedex.setTheme("/button");
        noClip.setCanAcceptKeyboardFocus(false);speed.setCanAcceptKeyboardFocus(false);
        noClip.addCallback(() -> {
            world.movement().setNoClip(!world.movement().noClip());
            refresh();
        });
        speed.addCallback(() -> {
            float current=world.motion().speed();
            world.motion().setSpeed(current>=16?1:current<2?2:current<4?4:current<8?8:16);
            refresh();
        });
        pokedex.setCanAcceptKeyboardFocus(false);pokedex.addCallback(()->{world.commands().execute("pokedex");refresh();});
        for(var button:new Button[]{teleport,randomItems,item,pokemon}){button.setTheme("/button");button.setCanAcceptKeyboardFocus(false);add(button);}
        add(noClip);add(speed);add(pokedex);refresh();
    }
    public void onTeleport(Runnable action){teleport.addCallback(action);}
    public void onItem(Runnable action){item.addCallback(action);}
    public void onRandomItems(Runnable action){randomItems.addCallback(action);}
    public void onPokemon(Runnable action){pokemon.addCallback(action);}
    private Button[] buttons(){return new Button[]{noClip,speed,pokedex,teleport,randomItems,item,pokemon};}
    @Override protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        buttonWidth=0;
    }
    private int fixedButtonWidth() {
        if(buttonWidth==0) {
            for(var button:buttons())buttonWidth=Math.max(buttonWidth,button.getPreferredWidth());
            // 状态切换只更改文字；为现有的最长状态一次性留出宽度。
            buttonWidth=Math.max(buttonWidth,stateWidth(noClip,"穿墙：开启"));
            buttonWidth=Math.max(buttonWidth,stateWidth(noClip,"穿墙：关闭"));
            buttonWidth=Math.max(buttonWidth,stateWidth(speed,"倍速：16×"));
            buttonWidth=Math.max(buttonWidth,stateWidth(pokedex,"图鉴：全解锁"));
            buttonWidth=Math.max(buttonWidth,stateWidth(pokedex,"图鉴：未解锁"));
        }
        return buttonWidth;
    }
    private static int stateWidth(Button button,String text) {
        return button.getFont()==null?button.getPreferredWidth()
                :Math.max(button.getMinWidth(),button.getBorderHorizontal()+button.getFont().computeTextWidth(text));
    }
    private void refresh() {
        noClip.setText("穿墙："+(world.movement().noClip()?"开启":"关闭"));
        speed.setText("倍速："+(int)world.motion().speed()+"×");
        pokedex.setText("图鉴："+(world.commands().pokedexUnlocked()?"全解锁":"未解锁"));
        invalidateLayout();
    }
    public void update(){
        String text="图鉴："+(world.commands().pokedexUnlocked()?"全解锁":"未解锁");
        if(!text.equals(pokedex.getText())){pokedex.setText(text);invalidateLayout();}
    }
    public void position(int x,int y,int width,int height) {
        int buttonWidth=fixedButtonWidth(),h=-5;for(var button:buttons())h+=button.getPreferredHeight()+5;
        setSize(buttonWidth,h);
        setPosition(x+8,mobile?y+70:y+(height-h)/2);
    }
    @Override protected void layout() {
        int width=fixedButtonWidth(),y=getInnerY();
        for(var button:buttons()){button.setPosition(getInnerX(),y);button.setSize(width,button.getPreferredHeight());y=button.getBottom()+5;}
    }
}
