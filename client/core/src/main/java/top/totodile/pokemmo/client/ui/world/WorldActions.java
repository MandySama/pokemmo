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
    private final Button teleport=new Button("传送"),item=new Button("添加道具"),pokemon=new Button("添加宝可梦");
    private final Label error=new Label();
    public WorldActions(WorldScene world) {
        this(world,false);
    }
    public WorldActions(WorldScene world,boolean mobile) {
        this.world=world;this.mobile=mobile;
        setTheme("");noClip.setTheme("/button");speed.setTheme("/button");pokedex.setTheme("/button");error.setTheme("/label");
        noClip.setCanAcceptKeyboardFocus(false);speed.setCanAcceptKeyboardFocus(false);
        noClip.addCallback(() -> {
            try {world.movement().setNoClip(!world.movement().noClip());error.setText("");}
            catch(IllegalStateException failure){error.setText(failure.getMessage());}
            refresh();
        });
        speed.addCallback(() -> {
            float current=world.motion().speed();
            world.motion().setSpeed(current>=16?1:current<2?2:current<4?4:current<8?8:16);
            refresh();
        });
        pokedex.setCanAcceptKeyboardFocus(false);pokedex.addCallback(()->{world.commands().execute("pokedex");refresh();});
        for(var button:new Button[]{teleport,item,pokemon}){button.setTheme("/button");button.setCanAcceptKeyboardFocus(false);add(button);}
        error.setClip(true);add(noClip);add(speed);add(pokedex);add(error);refresh();
    }
    public void onTeleport(Runnable action){teleport.addCallback(action);}
    public void onItem(Runnable action){item.addCallback(action);}
    public void onPokemon(Runnable action){pokemon.addCallback(action);}
    public void message(String text){error.setText(text==null?"":text);error.setTooltipContent(text);invalidateLayout();}
    private Button[] buttons(){return new Button[]{noClip,speed,pokedex,teleport,item,pokemon};}
    private void refresh() {
        noClip.setText("穿墙："+(world.movement().noClip()?"开启":"关闭"));
        speed.setText("倍速："+(int)world.motion().speed()+"×");
        pokedex.setText("图鉴："+(world.commands().pokedexUnlocked()?"全解锁":"全零"));
        invalidateLayout();
    }
    public void update(){
        String text="图鉴："+(world.commands().pokedexUnlocked()?"全解锁":"全零");
        if(!text.equals(pokedex.getText())){pokedex.setText(text);invalidateLayout();}
    }
    public void position(int x,int y,int width,int height) {
        int buttonWidth=0,h=-5;for(var button:buttons()){buttonWidth=Math.max(buttonWidth,button.getPreferredWidth());h+=button.getPreferredHeight()+5;}
        int messageHeight=error.getText().isEmpty()?0:error.getPreferredHeight()+5;
        setSize(Math.min(Math.max(buttonWidth,error.getPreferredWidth()),Math.max(buttonWidth,width-16)),h+messageHeight);
        setPosition(x+8,mobile?y+70:y+(height-h)/2);
    }
    @Override protected void layout() {
        int width=0,y=getInnerY();for(var button:buttons())width=Math.max(width,button.getPreferredWidth());
        for(var button:buttons()){button.setPosition(getInnerX(),y);button.setSize(width,button.getPreferredHeight());y=button.getBottom()+5;}
        error.setPosition(getInnerX(),y);error.setSize(getInnerWidth(),error.getPreferredHeight());
    }
}
