package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.server.game.*;
import java.util.*;

/** Reference BattleSwitchReturnButton (eg_1) and PopupDialogContainerLayout (QV). */
final class PokemonMachineWindow extends PopupWindow {
    private final PokemonController controller;
    private final PlayerData.Item item;
    private final int move;
    private final Label title=new Label();
    private final List<Button> choices=new ArrayList<>();
    private final Widget panel=new Widget(){@Override protected void layout(){layoutPanel();}};
    private final Button back;
    private int selected;

    PokemonMachineWindow(Widget owner,PokemonController controller,PlayerData.Item item){
        super(owner);this.controller=controller;this.item=item;move=controller.items().machineMove(item.itemId());
        setTheme("/-defaults");setCloseOnClickedOutside(false);setCloseOnEscape(true);
        panel.setTheme("/-defaults");title.setTheme("/label");title.setText(controller.items().name(item.itemId()));
        back=new Button(controller.strings.get(58));back.setTheme("/encounter-return");back.addCallback(this::closePopup);
        panel.add(title);panel.add(back);add(panel);
        for(int i=0;i<6;i++){
            var p=controller.state().at(PokemonData.PARTY,i);
            boolean known=p!=null&&p.moves().contains(move),can=p!=null&&!p.egg()&&controller.dex().get(p.species()).moves().stream().anyMatch(m->m.source()==4&&m.move()==move);
            String status=controller.strings.get(can?(known?1868:78):79);
            var button=new Button(p==null?"-":controller.name(p)+"\n"+status){
                @Override protected void paintWidget(GUI gui){super.paintWidget(gui);var icon=p==null?null:controller.characterPartyIcon(p);if(icon!=null)icon.draw(getAnimationState(),getX()+6,getY()+(getHeight()-32)/2,32,32);}
            };
            button.setTheme("/encounter-party-slot");button.setBorderSize(2,40,2,4);button.setEnabled(can&&!known);
            button.addCallback(()->choose(p));choices.add(button);panel.add(button);
        }
    }
    void open(){if(openPopup()){position();choices.get(0).requestKeyboardFocus();}}
    private void position(){var owner=getOwner();setPosition(owner.getInnerX(),owner.getInnerY());setSize(owner.getInnerWidth(),owner.getInnerHeight());invalidateLayout();}
    @Override protected void layout(){
        var owner=getOwner();setPosition(owner.getInnerX(),owner.getInnerY());setSize(owner.getInnerWidth(),owner.getInnerHeight());
        int width=Math.min(getInnerWidth()-24,controller.mobile?640:500),height=controller.mobile?340:260;
        panel.setPosition(getInnerX()+(getInnerWidth()-width)/2,getInnerY()+(getInnerHeight()-height)/2);panel.setSize(width,height);layoutPanel();
    }
    private void layoutPanel(){
        int x=panel.getInnerX(),y=panel.getInnerY(),w=panel.getInnerWidth(),row=controller.mobile?80:54;
        title.setPosition(x,y);title.setSize(w,34);y+=38;
        for(int i=0;i<choices.size();i++){var b=choices.get(i);b.setPosition(x+i%2*(w/2),y+i/2*(row+4));b.setSize(w/2-6,row);}
        back.setPosition(x+w-110,panel.getInnerBottom()-42);back.setSize(110,36);
    }
    private void choose(PokemonData.Pokemon p){
        for(int i=0;i<4;i++)if(p.moves().get(i)<=0){submit(p,i);return;}
        for(var button:choices)panel.removeChild(button);choices.clear();
        for(int i=0;i<5;i++){
            int id=i==4?move:p.moves().get(i);final int slot=i;
            var button=new Button(controller.dex().moveName(id));button.setTheme("/encounter-choice-move");
            button.setTooltipContent(controller.dex().moveDescription(id));button.setEnabled(i<4);
            button.addCallback(()->submit(p,slot));choices.add(button);panel.add(button);
        }
        selected=0;invalidateLayout();choices.get(0).requestKeyboardFocus();
    }
    boolean key(int key){
        if(!isOpen())return false;
        int confirm=Integer.parseInt(controller.resources.preferences().get("client.controls.gdx.key_a",Integer.toString(com.badlogic.gdx.Input.Keys.J)));
        int cancel=Integer.parseInt(controller.resources.preferences().get("client.controls.gdx.key_b",Integer.toString(com.badlogic.gdx.Input.Keys.X)));
        if(key==cancel||key==com.badlogic.gdx.Input.Keys.ESCAPE||key==com.badlogic.gdx.Input.Keys.BACK){closePopup();return true;}
        if(key==confirm||key==com.badlogic.gdx.Input.Keys.ENTER){if(choices.get(selected).isEnabled())choices.get(selected).getModel().fireActionCallback();return true;}
        int next=selected;
        if(key==com.badlogic.gdx.Input.Keys.RIGHT&&selected%2==0)next++;
        if(key==com.badlogic.gdx.Input.Keys.LEFT&&selected%2==1)next--;
        if(key==com.badlogic.gdx.Input.Keys.DOWN)next+=2;
        if(key==com.badlogic.gdx.Input.Keys.UP)next-=2;
        if(next>=0&&next<choices.size()){selected=next;choices.get(selected).requestKeyboardFocus();}
        return true;
    }
    private void submit(PokemonData.Pokemon p,int slot){closePopup();controller.useMedicine(item,p,1,slot);}
}
