package top.totodile.pokemmo.client.ui.world;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.client.world.WorldScene;

/** 用户指定的本地地图快捷按钮，使用现有主题按钮。 */
public final class WorldActions extends Widget {
    private final WorldScene world;
    private final Button noClip=new Button();
    private final Button speed=new Button();
    private final Button pokedex=new Button();
    private final Label error=new Label();
    public WorldActions(WorldScene world) {
        this.world=world;
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
        add(noClip);add(speed);add(pokedex);add(error);refresh();
    }
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
        int buttonWidth=Math.max(pokedex.getPreferredWidth(),Math.max(noClip.getPreferredWidth(),speed.getPreferredWidth()));
        int h=noClip.getPreferredHeight()+speed.getPreferredHeight()+pokedex.getPreferredHeight()+10;
        int messageHeight=error.getText().isEmpty()?0:error.getPreferredHeight()+5;
        setSize(Math.max(buttonWidth,error.getPreferredWidth()),h+messageHeight);
        setPosition(x+8,y+(height-h)/2);
    }
    @Override protected void layout() {
        int width=Math.max(pokedex.getPreferredWidth(),Math.max(noClip.getPreferredWidth(),speed.getPreferredWidth()));
        noClip.setPosition(getInnerX(),getInnerY());noClip.setSize(width,noClip.getPreferredHeight());
        speed.setPosition(getInnerX(),noClip.getBottom()+5);speed.setSize(width,speed.getPreferredHeight());
        pokedex.setPosition(getInnerX(),speed.getBottom()+5);pokedex.setSize(width,pokedex.getPreferredHeight());
        error.setPosition(getInnerX(),pokedex.getBottom()+5);error.adjustSize();
    }
}
