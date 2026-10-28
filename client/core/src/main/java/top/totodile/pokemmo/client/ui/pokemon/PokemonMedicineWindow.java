package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.client.ui.common.PlayerTextInput;
import top.totodile.pokemmo.server.game.*;
import java.util.*;

/** InventoryWindow.v6/OV: two-column party, selected move, optional MultiUseItemWindow. */
final class PokemonMedicineWindow extends PopupWindow {
    private final PokemonController controller;
    private final PlayerData.Item item;
    private final GameService.Medicine effect;
    private final GameService.GrowthItem growth;
    private final Label title=new Label();
    private final List<Button> choices=new ArrayList<>();
    private final Button back;
    private final Widget panel=new Widget(){@Override protected void layout(){layoutPanel();}};
    private final PlayerTextInput keyboard;
    private PokemonData.Pokemon target;
    private int selectedMove=-1;
    private EditField amount;
    private final List<Widget> quantity=new ArrayList<>();

    PokemonMedicineWindow(Widget owner,PokemonController controller,PlayerData.Item item){
        super(owner);this.controller=controller;this.item=item;effect=controller.items().medicine(item.itemId());growth=controller.items().growthItem(item.itemId());
        setTheme("/-defaults");setCloseOnClickedOutside(false);setCloseOnEscape(true);
        panel.setTheme("/-defaults");title.setTheme("/label");back=new Button(controller.strings.get(58));back.setTheme("/encounter-return");
        back.addCallback(this::closePopup);panel.add(title);panel.add(back);add(panel);
        keyboard=new PlayerTextInput(controller.mobile,this::isOpen);
        title.setText(controller.items().name(item.itemId()));
        for(int i=0;i<6;i++){
            var p=controller.state().at(PokemonData.PARTY,i);
            var button=new Button(p==null?"-":controller.name(p)+"  Lv. "+p.level()+"\n"+p.hp()+" / "+p.stats().get(0)){
                @Override protected void paintWidget(GUI gui){
                    super.paintWidget(gui);var icon=p==null?null:controller.characterPartyIcon(p);
                    if(icon!=null)icon.draw(getAnimationState(),getX()+6,getY()+(getHeight()-32)/2,32,32);
                }
            };
            boolean stone=p!=null&&controller.dex().get(p.species()).evolutions().stream().anyMatch(e->e.parameter()==growth.stone()&&(e.method()==8||e.method()==17&&p.gender()==0||e.method()==18&&p.gender()==1));
            button.setTheme("/encounter-party-slot");button.setBorderSize(2,40,2,4);button.setEnabled(p!=null&&!p.egg()&&(growth.stone()==0||stone));
            button.addCallback(()->choose(p));choices.add(button);panel.add(button);
        }
    }
    void open(){if(openPopup())position();}
    private void position(){var owner=getOwner();setPosition(owner.getInnerX(),owner.getInnerY());setSize(owner.getInnerWidth(),owner.getInnerHeight());invalidateLayout();}
    @Override protected void layout(){
        var owner=getOwner();setPosition(owner.getInnerX(),owner.getInnerY());setSize(owner.getInnerWidth(),owner.getInnerHeight());
        int width=Math.min(getInnerWidth()-24,controller.mobile?640:500),height=quantity.isEmpty()?(controller.mobile?340:260):250;
        panel.setPosition(getInnerX()+(getInnerWidth()-width)/2,getInnerY()+(getInnerHeight()-height)/2);panel.setSize(width,height);
        layoutPanel();
    }
    private void layoutPanel(){
        int x=panel.getInnerX(),y=panel.getInnerY(),w=panel.getInnerWidth(),row=controller.mobile?80:54;
        title.setPosition(x,y);title.setSize(w,34);y+=38;
        for(int i=0;i<choices.size();i++){var button=choices.get(i);button.setPosition(x+i%2*(w/2),y+i/2*(row+4));button.setSize(w/2-6,row);}
        if(!quantity.isEmpty()){
            int at=x;for(var widget:quantity){int width=widget==amount?80:widget==quantity.get(quantity.size()-1)?100:60;widget.setPosition(at,y+25);widget.setSize(width,40);at+=width+6;}
        }
        back.setPosition(x+w-110,panel.getInnerBottom()-42);back.setSize(110,36);
    }
    private void clearChoices(){for(var button:choices)panel.removeChild(button);choices.clear();}
    private int maximumPp(PokemonData.Pokemon p,int slot){int base=controller.dex().move(p.moves().get(slot)).pp();return base*(5+Math.min(3,Math.max(0,p.ppUps().get(slot))))/5;}
    private void choose(PokemonData.Pokemon p){
        target=p;
        if(growth.ppUps()>0||effect.pp()>0&&effect.action()==3){
            clearChoices();
            for(int i=0;i<4;i++){
                final int slot=i;int move=i<p.moves().size()?p.moves().get(i):0,max=move<=0?0:maximumPp(p,i);
                int pp=move<=0?0:p.pp().get(i)<0?max:p.pp().get(i);
                var button=new Button(move<=0?"-":controller.dex().moveName(move)+"\n"+controller.strings.get(1852)+" "+pp+" / "+max);
                button.setTheme("/encounter-choice-move");button.setEnabled(move>0);button.addCallback(()->{selectedMove=slot;quantity();});choices.add(button);panel.add(button);
            }
            invalidateLayout();
        }else quantity();
    }
    private int maximumUses(){
        if(growth.levels()>0)return Math.min(item.amount(),(100-target.level())/growth.levels());
        if(growth.stone()>0)return 1;
        if(growth.ppUps()>0){if(selectedMove<0||target.moves().get(selectedMove)<=0||controller.dex().move(target.moves().get(selectedMove)).pp()<=1)return 0;int used=target.ppUps().get(selectedMove);return used>=3?0:growth.ppUps()==3?1:Math.min(item.amount(),3-used);}
        if(target.hp()==0)return effect.revive()?1:0;
        if(effect.revive())return 0;
        int max=0;
        if(effect.hp()>0){int unit=effect.percent()?(int)Math.ceil(target.stats().get(0)*effect.hp()/100.0):effect.hp();max=(int)Math.ceil((target.stats().get(0)-target.hp())/(double)Math.max(1,unit));}
        if(effect.statusMask()!=0&&(target.status()&255)!=0&&((target.status()&effect.statusMask())!=0||effect.statusMask()==255))max=Math.max(max,1);
        if(effect.pp()>0)for(int i=0;i<target.moves().size();i++)if(target.moves().get(i)>0&&(selectedMove<0||selectedMove==i)){
            int maximum=maximumPp(target,i),current=target.pp().get(i)<0?maximum:target.pp().get(i);
            max=Math.max(max,(int)Math.ceil((maximum-current)/(double)effect.pp()));
        }
        return Math.min(item.amount(),Math.min(9999,max));
    }
    private void quantity(){
        int maximum=maximumUses();
        if(maximum<=1||!(growth.supported()?growth.multiple():effect.multiple())||item.amount()==1){submit(1);return;}
        clearChoices();panel.setTheme("/base-frame");panel.reapplyTheme();title.setText(controller.strings.format(8045,controller.items().name(item.itemId()),controller.name(target)));
        amount=keyboard.field(4,false);amount.setTheme("/valueadjuster.valueEdit");amount.setText("1");
        var minus=new Button("-");var plus=new Button("+");var all=new Button(controller.strings.get(8118));var use=new Button(controller.strings.get(1410));
        minus.addCallback(()->amount.setText(Integer.toString(Math.max(1,value()-1))));plus.addCallback(()->amount.setText(Integer.toString(Math.min(maximum,value()+1))));
        all.addCallback(()->amount.setText(Integer.toString(maximum)));use.addCallback(()->submit(Math.max(1,Math.min(maximum,value()))));
        quantity.addAll(List.of(minus,amount,plus,all,use));for(var widget:quantity){if(widget!=amount)widget.setTheme("/button");panel.add(widget);}invalidateLayout();
    }
    private int value(){try{return Integer.parseInt(amount.getText());}catch(NumberFormatException error){return 1;}}
    private void submit(int count){closePopup();controller.useMedicine(item,target,count,selectedMove);}
    @Override public void closePopup(){keyboard.close();super.closePopup();}
}
