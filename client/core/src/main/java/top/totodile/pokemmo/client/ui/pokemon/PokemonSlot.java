package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import top.totodile.pokemmo.server.game.PokemonData.*;
import static top.totodile.pokemmo.server.game.PokemonData.PC;

/** PcStorageSlotWidget / ExpandableItemButton dimensions and native sprite offsets. */
final class PokemonSlot extends Button {
    final PokemonController controller;
    final int container;
    int position;
    final boolean storage;
    private final Health health=new Health();
    private Image radial;
    private Runnable select=()->{};
    PokemonSlot(PokemonController controller,int container,int position,boolean storage){
        this.controller=controller;this.container=container;this.position=position;this.storage=storage;setCanAcceptKeyboardFocus(false);
        // ServerRegionTagLabel.Bb(200); SlotGridContainerLayout.PH overrides PC box cells to 300.
        setTooltipDelay(storage&&container==PC?300:200);
        health.setTheme("/pokemon-health");health.setVisible(!storage);add(health);
        addCallback(()->{select.run();if(!storage||controller.mobile)controller.menu(this,storage);});refresh();
    }
    Pokemon pokemon(){return controller.state().at(container,position);}
    void onSelect(Runnable action){select=action;}
    void refresh(){
        var p=pokemon();double fraction=p==null?1:(double)p.hp()/p.maxHp();
        String theme=storage?"pc-slot":p==null?"/monster-slot-empty":fraction==0?"/monster-slot-purple":fraction<=.2?"/monster-slot-red":fraction<=.5?"/monster-slot-orange":"/monster-slot";
        if(!theme.equals(getTheme())){setTheme(theme);if(getGUI()!=null)reapplyTheme();}
        health.setVisible(!storage&&!controller.mobile&&p!=null&&!p.egg());health.setValue((float)fraction);
        String healthTheme=fraction<=.2?"/pokemon-health-red":fraction<=.5?"/pokemon-health-orange":"/pokemon-health";
        if(!healthTheme.equals(health.getTheme())){health.setTheme(healthTheme);if(health.getGUI()!=null)health.reapplyTheme();}
        setTooltipContent(p==null?null:controller.name(p)+"\n"+controller.strings.format(1731,Integer.toString(p.level()))+"   HP "+p.hp()+"/"+p.maxHp());
    }
    @Override protected void layout(){health.setSize(Math.max(0,getWidth()-10),5);health.setPosition(getX()+5,getBottom()-6);}
    @Override protected void applyTheme(ThemeInfo theme){
        super.applyTheme(theme);
        // Reference ServerRegionTagLabel only loads this image when radial health is enabled.
        radial=controller.mobile&&!storage?theme.getImage("radial-progress"):null;
    }
    @Override protected void paintWidget(GUI gui){
        var p=pokemon();if(p==null)return;var images=controller.images();if(images==null)return;
        if(controller.mobile&&!storage&&radial!=null&&gui.getRenderer() instanceof top.totodile.pokemmo.client.twl.GdxTwlRenderer renderer){
            float fraction=p.egg()?1:(float)p.hp()/p.maxHp();int rgba=fraction>.5?785150431:fraction>.2?-207875361:fraction>0?-414434081:-1688619297;
            renderer.drawRadial(radial,getX(),getY(),getWidth(),fraction,new Color((byte)(rgba>>>24),(byte)(rgba>>>16),(byte)(rgba>>>8),(byte)rgba));
        }
        Image icon=images.icon(p.species(),p.form(),p.gender(),p.egg());int size=controller.mobile?72:36;
        int x=getX()+(getWidth()-size)/2+(storage&&!controller.mobile?1:0);
        int y=storage?getY()+(controller.mobile?0:container==0?-4:4):controller.mobile?getY()+8:getY()+(getHeight()-size)/2-2;
        icon.draw(getAnimationState(),x,y,size,size);
        if(p.item()>5000&&p.item()<5627){var item=images.item(p.item());int s=controller.mobile?24:12;item.draw(getAnimationState(),getRight()-s,getBottom()-s-2,s,s);}
        if(p.shiny()){var shiny=images.atlas("shiny-small");if(shiny!=null)shiny.draw(getAnimationState(),getX()+2,getY()+2,controller.mobile?24:12,controller.mobile?24:12);}
    }
    @Override public boolean handleEvent(Event event){
        if(event.getType()==Event.Type.MOUSE_BTNUP&&event.getMouseButton()==Event.MOUSE_RBUTTON){select.run();controller.menu(this,storage);return true;}
        return super.handleEvent(event);
    }
    private static final class Health extends Widget {
        private Image fill;private float fraction;
        void setValue(float value){fraction=value;}
        @Override protected void applyTheme(ThemeInfo theme){super.applyTheme(theme);fill=theme.getImage("progressImage");}
        @Override protected void paintWidget(GUI gui){if(fill!=null)fill.draw(getAnimationState(),getX(),getY(),Math.round(getWidth()*fraction),getHeight());}
        @Override protected boolean handleEvent(Event event){return false;}
    }
}
