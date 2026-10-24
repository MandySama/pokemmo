package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import java.util.function.Supplier;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;

/** PokemonSummaryWindow attacksTab: four move cards, reorder arrows and ability description. */
final class PokemonMovesPage extends Widget {
    private final PokemonController controller;
    private final Supplier<Pokemon> selected;
    private final Button[] up=new Button[4],down=new Button[4];
    private final MoveCard[] cards=new MoveCard[4];
    private final Label[] marks=new Label[4];
    private final Label abilityTitle=new Label(),ability=new Label(),description=new Label();
    PokemonMovesPage(PokemonController controller,Supplier<Pokemon> selected){
        this.controller=controller;this.selected=selected;setTheme("");
        for(int i=0;i<4;i++){
            final int slot=i;cards[i]=new MoveCard(i);add(cards[i]);
            marks[i]=new Label();marks[i].setTheme("/label-monster-area.label-monster-title");add(marks[i]);
            if(i>0){up[i]=arrow("arrow_white_up",()->move(slot,slot-1));add(up[i]);}
            if(i<3){down[i]=arrow("arrow_white_down",()->move(slot,slot+1));add(down[i]);}
        }
        abilityTitle.setText(controller.strings.get(1824));abilityTitle.setTheme("/label-monster-area.label-monster-title");
        ability.setTheme("/label-monster-area.label-monster-value");description.setTheme("/summary-move-description");
        add(abilityTitle);add(ability);add(description);
    }
    private Button arrow(String image,Runnable callback){
        var button=new Button(){@Override protected void paintWidget(GUI gui){var icon=controller.images().atlas(image);if(icon!=null)icon.draw(getAnimationState(),getX(),getY(),16,16);}};
        button.setTheme("");button.setCanAcceptKeyboardFocus(false);button.addCallback(callback);return button;
    }
    private void move(int from,int to){var p=selected.get();if(p!=null&&!controller.busy())controller.moveMove(p.id(),from,to);}
    void refresh(){
        var p=selected.get();if(p==null)return;
        for(int i=0;i<4;i++){
            cards[i].refresh();if(up[i]!=null)up[i].setVisible(p.moves().get(i)!=0&&p.moves().get(i-1)!=0);
            if(down[i]!=null)down[i].setVisible(p.moves().get(i)!=0&&p.moves().get(i+1)!=0);
        }
        ability.setText(controller.dex().ability(p.ability()));
        invalidateLayout();
    }
    private String wrap(String text,int width){
        if(description.getFont()==null)return text;
        var result=new StringBuilder();String line="";
        for(char c:text.replace("\r","").toCharArray()){
            if(c=='\n'){result.append(line).append('\n');line="";continue;}
            if(!line.isEmpty()&&description.getFont().computeTextWidth(line+c)>width){
                int space=line.lastIndexOf(' ');
                if(space>0){result.append(line,0,space).append('\n');line=line.substring(space+1);}
                else {result.append(line).append('\n');line="";}
            }
            line+=c;
        }return result.append(line).toString();
    }
    @Override protected void layout(){
        int x=getX(),y=getY();
        for(int i=0;i<4;i++){
            int top=y+i*48;cards[i].setPosition(x+48,top);cards[i].setSize(164,42);
            marks[i].setPosition(x,top+8);marks[i].setSize(20,25);
            if(up[i]!=null){up[i].setPosition(x+25,top+(i==3?13:0));up[i].setSize(16,16);}
            if(down[i]!=null){down[i].setPosition(x+25,top+(i==0?13:24));down[i].setSize(16,16);}
        }
        abilityTitle.setPosition(x,y+194);abilityTitle.setSize(103,25);ability.setPosition(x+106,y+194);ability.setSize(110,25);
        description.setPosition(x,y+224);description.setSize(238,43);var p=selected.get();description.setText(p==null?"":wrap(controller.dex().abilityDescription(p.ability()),228));
    }
    private final class MoveCard extends Button {
        final int slot;
        final Label name=new Label(),pp=new Label();
        MoveCard(int slot){this.slot=slot;setTheme("/summary-move-card");setCanAcceptKeyboardFocus(false);name.setTheme("/summary-move-text");pp.setTheme("/summary-move-text");add(name);add(pp);setTooltipDelay(200);}
        void refresh(){
            var p=selected.get();int id=p.moves().get(slot);
            if(id==0){name.setText(controller.strings.get(61));pp.setText("");setTooltipContent(null);return;}
            var move=controller.dex().move(id);int maximum=Math.min(64,move.pp()+move.pp()*p.ppUps().get(slot)/5);
            // -1 is an untouched/full move slot in older local saves, resolved from ROM PP.
            int current=p.pp().get(slot)<0?maximum:p.pp().get(slot);
            name.setText(controller.dex().moveName(id));pp.setText("PP: "+current+" / "+maximum);
            setTooltipContent(name.getText()+"\n"+controller.strings.get(1850)+" "+move.power()+"  "+controller.strings.get(1851)+" "+move.accuracy()+"\n"+controller.dex().moveDescription(id));
        }
        @Override protected void layout(){name.setPosition(getX(),getY()+4);name.setSize(110,17);pp.setPosition(getX(),getY()+21);pp.setSize(110,17);}
        @Override protected void paintWidget(GUI gui){var p=selected.get();if(p==null||p.moves().get(slot)==0)return;var image=controller.images().skillType(controller.dex().move(p.moves().get(slot)).type());if(image!=null)image.draw(getAnimationState(),getX()+110,getY()+6,50,29);}
    }
}
