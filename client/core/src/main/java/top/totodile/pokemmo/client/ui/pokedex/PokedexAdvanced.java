package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import java.util.*;
import java.util.function.Function;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;

/** Official advanced search: egg group, abilities, two types, item, registration and six stat ranges. */
final class PokedexAdvanced extends Widget {
    private final PokedexFilter filter;
    private final ClientStrings strings;
    private final boolean mobile;
    private final EditField query;
    private final Button clear,apply;
    private final List<Widget[]> rows=new ArrayList<>();
    private final PokedexChoice egg,ability,first,second,item,status;
    private final List<Integer> items;
    private final NumberField[] minimum=new NumberField[6],maximum=new NumberField[6];
    PokedexAdvanced(BwPokedex dex,ClientStrings strings,boolean mobile,PokedexFilter filter,
                    Function<Boolean,EditField> fields,Runnable applied) {
        this.filter=filter;this.strings=strings;this.mobile=mobile;setTheme("");
        query=fields.apply(false);query.setTheme("/pokedex-search");add(query);
        clear=new Button(strings.get(8008));clear.setTheme("/pokedex-small-button");
        clear.setCanAcceptKeyboardFocus(false);clear.addCallback(this::reset);add(clear);
        String[] types=new String[18];types[0]=strings.get(6495);for(int i=0;i<17;i++)types[i+1]=dex.type(i);
        String[] groups=new String[16];groups[0]="--";for(int i=1;i<16;i++)groups[i]=dex.eggGroup(i);
        String[] abilities=new String[165];abilities[0]=strings.get(6495);for(int i=1;i<165;i++)abilities[i]=dex.ability(i);
        items=new ArrayList<>();items.add(0);dex.all().stream().flatMap(s->s.heldItems().stream()).filter(i->i>0).distinct().sorted().forEach(items::add);
        String[] held=items.stream().map(i->i==0?strings.get(6495):dex.item(i)).toArray(String[]::new);
        egg=new PokedexChoice(()->{},groups);ability=new PokedexChoice(()->{},abilities);
        first=new PokedexChoice(()->{},types);second=new PokedexChoice(()->{},types);item=new PokedexChoice(()->{},held);
        status=new PokedexChoice(()->{},strings.get(6495),strings.get(6479),strings.get(1701).replace(":",""),strings.get(1702).replace(":",""));
        row(strings.get(1730),egg,strings.get(1733),ability);
        row(strings.get(6475),first,strings.get(6476),second);
        row(strings.get(1735),item,strings.get(6474),status);
        int[] statIds={1720,1721,1722,1724,1725,1723};
        for(int i=0;i<6;i++){
            minimum[i]=new NumberField(fields.apply(true),0);maximum[i]=new NumberField(fields.apply(true),255);
            String stat=strings.get(statIds[i]).replace(":","");
            row(strings.format(6477,stat),minimum[i],strings.format(6478,stat),maximum[i]);
        }
        apply=new Button(strings.get(8112));apply.setTheme("/pokedex-small-button");apply.setCanAcceptKeyboardFocus(false);
        apply.addCallback(()->{
            filter.query=query.getText();filter.eggGroup=egg.selected();filter.ability=ability.selected();
            filter.type1=first.selected()-1;filter.type2=second.selected()-1;filter.heldItem=items.get(item.selected());filter.status=status.selected();
            for(int i=0;i<6;i++){filter.minimum[i]=minimum[i].value();filter.maximum[i]=maximum[i].value();}
            applied.run();
        });add(apply);
    }
    void query(String value){query.setText(value);}
    boolean closeChoices(){for(PokedexChoice choice:List.of(egg,ability,first,second,item,status))if(choice.closeChoices())return true;return false;}
    private void reset(){
        filter.reset();query.setText("");for(PokedexChoice choice:List.of(egg,ability,first,second,item,status))choice.select(0);
        for(int i=0;i<6;i++){minimum[i].set(0);maximum[i].set(255);}
    }
    private void row(String left,Widget first,String right,Widget second){
        Label a=new Label(left),b=new Label(right);a.setTheme("/pokedex-title");b.setTheme("/pokedex-title");
        Widget[] row={a,first,b,second};rows.add(row);for(Widget child:row)add(child);
    }
    @Override protected void layout(){
        int gap=4,line=mobile?43:32,label=mobile?145:135,x=getInnerX(),y=getInnerY(),w=getInnerWidth();
        clear.setSize(mobile?120:95,line);clear.setPosition(getInnerRight()-clear.getWidth(),y);
        query.setSize(Math.max(1,w-clear.getWidth()-gap),line);query.setPosition(x,y);
        int value=Math.max(35,(w-2*label-3*gap)/2);y+=line+gap;
        for(Widget[] row:rows){
            int left=x;
            for(int col=0;col<4;col++){int width=(col&1)==0?label:value;row[col].setPosition(left,y);row[col].setSize(width,line);left+=width+gap;}
            y+=line+gap;
        }
        apply.setSize(mobile?140:95,line);apply.setPosition(getInnerRight()-apply.getWidth(),y);
    }
    private final class NumberField extends Widget {
        private final EditField edit;private final Button less=new Button("-"),more=new Button("+");
        NumberField(EditField edit,int initial){
            this.edit=edit;setTheme("");edit.setTheme("/pokedex-search");edit.setMaxTextLength(3);set(initial);
            less.setTheme("/pokedex-small-button");more.setTheme("/pokedex-small-button");
            less.setCanAcceptKeyboardFocus(false);more.setCanAcceptKeyboardFocus(false);
            less.addCallback(()->set(value()-1));more.addCallback(()->set(value()+1));add(less);add(edit);add(more);
        }
        int value(){try{return Math.max(0,Math.min(255,Integer.parseInt(edit.getText())));}catch(NumberFormatException ignored){return 0;}}
        void set(int value){edit.setText(Integer.toString(Math.max(0,Math.min(255,value))));}
        @Override protected void layout(){int side=mobile?32:25;less.setSize(side,getInnerHeight());more.setSize(side,getInnerHeight());
            less.setPosition(getInnerX(),getInnerY());more.setPosition(getInnerRight()-side,getInnerY());
            edit.setPosition(getInnerX()+side+3,getInnerY());edit.setSize(Math.max(1,getInnerWidth()-2*side-6),getInnerHeight());}
    }
}
