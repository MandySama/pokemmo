package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import java.util.*;
import java.util.function.*;
import top.totodile.pokemmo.client.ui.pokedex.PokedexChoice;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;

/** QuickSlotAssignMenuWidget.MQ: local PC filters; searches highlight slots without moving data. */
final class PokemonStorageSearch extends Widget {
    private final PokemonController controller;
    private final List<Widget[]> rows=new ArrayList<>();
    private final List<PokedexChoice> choices=new ArrayList<>();
    private final List<Numeric> numbers=new ArrayList<>();
    private final List<Supplier<Predicate<Pokemon>>> rules=new ArrayList<>();
    private final List<BooleanSupplier> modified=new ArrayList<>();
    private final List<ToggleButton> checks=new ArrayList<>();
    private final List<Predicate<Pokemon>> applied=new ArrayList<>();
    private final List<Button> markButtons=new ArrayList<>();
    private int markMask;
    private Widget[] pending;
    private final ScrollPane scroll;
    private final Widget form=new Widget(){
        @Override public int getPreferredHeight(){return rows.size()*36;}
        @Override protected void layout(){int y=getInnerY(),label=150,gap=5,value=Math.max(50,(getInnerWidth()-2*label-3*gap)/2);for(var row:rows){int x=getInnerX();for(int i=0;i<row.length;i++){int w=(i%2)==0?label:value;row[i].setPosition(x,y);row[i].setSize(row[i] instanceof ToggleButton?24:w,32);x+=w+gap;}y+=36;}}
    };
    final Button clear,apply;
        PokemonStorageSearch(PokemonController controller,Runnable cleared,Runnable searched){
        this.controller=controller;setTheme("");form.setTheme("");
        clear=new Button(controller.strings.get(8008));apply=new Button(controller.strings.get(8112));for(var b:List.of(clear,apply)){b.setTheme("/button");b.setCanAcceptKeyboardFocus(false);add(b);}
        clear.addCallback(()->{reset();cleared.run();});apply.addCallback(()->{applied.clear();for(int i=0;i<rules.size();i++)if(modified.get(i).getAsBoolean())applied.add(rules.get(i).get());searched.run();});
        scroll=new top.totodile.pokemmo.client.ui.common.TouchScrollPane(form);scroll.setTheme("/scrollpane");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);add(scroll);
        build();
    }
    private String text(int id){return controller.strings.get(id);}
    private void build(){
        var dex=controller.dex();if(dex==null)return;
        var marks=new Widget(){@Override protected void layout(){for(int i=0;i<markButtons.size();i++){markButtons.get(i).setPosition(getX()+i*24,getY()+6);markButtons.get(i).setSize(20,20);}}};marks.setTheme("");
        for(int i=0;i<5;i++){final int bit=i;var b=new Button(){@Override protected void paintWidget(GUI gui){var image=controller.images().atlas("mark",bit);if(image!=null){if((markMask&(1<<bit))==0)image=image.createTintedVersion(new Color(0xff404040));image.draw(getAnimationState(),getX()+2,getY()+2,16,16);}}};b.setTheme("/pc-tool-button");b.setCanAcceptKeyboardFocus(false);b.addCallback(()->markMask^=1<<bit);marks.add(b);markButtons.add(b);}
        field(text(8131),marks);rules.add(()->{int mask=markMask;return p->(p.marks()&mask)==mask;});modified.add(()->markMask!=0);finishRow();
        choose(8100,new String[]{text(8101),text(8102)},(p,i)->p.gender()==i);
        choose(8103,new String[]{text(49),text(10996),text(5614),text(61)},(p,i)->switch(i){case 0->true;case 1->p.secretShiny();case 2->p.shiny()&&!p.secretShiny();default->!p.shiny();});
        choose(8106,java.util.stream.IntStream.range(0,25).mapToObj(dex::nature).toArray(String[]::new),(p,i)->p.nature()==i);
        choose(8107,java.util.stream.IntStream.rangeClosed(1,15).mapToObj(dex::eggGroup).toArray(String[]::new),(p,i)->dex.get(p.species()).eggGroups().contains(i+1));
        choose(8099,new String[]{text(8104),text(8105)},(p,i)->p.hiddenAbility()==(i==0));
        choose(8119,new String[]{text(8104),text(8105),text(8098)},(p,i)->i==2?p.origin().fateful():p.alpha()==(i==0));
        numeric(text(8108),1,100,1,(p,v)->p.level()>=v);numeric(text(8109),1,100,100,(p,v)->p.level()<=v);
        int[] stats={1720,1721,1722,1724,1725,1723};
        for(int i=0;i<6;i++){final int stat=i;String name=text(stats[i]).replace(":","");numeric(controller.strings.format(8110,name),0,31,0,(p,v)->p.ivs().get(stat)>=v);numeric(controller.strings.format(8111,name),0,31,31,(p,v)->p.ivs().get(stat)<=v);}
        numeric(text(8092),0,6,0,(p,v)->p.ivs().stream().filter(n->n==31).count()>=v);
        checkbox(8086,p->dex.extra(p.species()).genderRatio()!=254);
        choose(8116,java.util.stream.IntStream.range(0,39).mapToObj(i->PokemonHonorsPage.particleTooltip(controller,i,false).split("\\n")[0]).toArray(String[]::new),(p,i)->p.honors().particles().contains(i));
        int maxMove=dex.all().stream().flatMap(s->s.moves().stream()).mapToInt(m->m.move()).max().orElse(559);
        choose(8096,java.util.stream.IntStream.rangeClosed(1,maxMove).mapToObj(dex::moveName).toArray(String[]::new),(p,i)->p.moves().contains(i+1));
        String[] tiers=java.util.stream.IntStream.of(5750,5751,5752,5753,5754,5755,5757).mapToObj(this::text).toArray(String[]::new);choose(8115,tiers,(p,i)->dex.extra(p.species()).tiers().contains(i));
        numeric(text(8097),2011,java.time.Year.now().getValue(),2011,(p,v)->p.origin().time()>0&&java.time.Instant.ofEpochSecond(p.origin().time()).atZone(java.time.ZoneOffset.UTC).getYear()==v);
        for(int i=0;i<6;i++){final int stat=i;String name=text(stats[i]).replace(":","");numeric(controller.strings.format(8093,name),0,252,0,(p,v)->p.evs().get(stat)>=v);numeric(controller.strings.format(8094,name),0,252,252,(p,v)->p.evs().get(stat)<=v);}
        int[] abilities=dex.all().stream().flatMap(s->s.abilities().stream()).filter(n->n>0).distinct().sorted().mapToInt(n->n).toArray();
        choose(8088,Arrays.stream(abilities).mapToObj(dex::ability).toArray(String[]::new),(p,i)->p.ability()==abilities[i]);
        checkbox(8085,p->p.species()!=132);finishRow();
    }
    private void choose(int id,String[] entries,BiPredicate<Pokemon,Integer> rule){String[] options=new String[entries.length+1];options[0]="--";System.arraycopy(entries,0,options,1,entries.length);var choice=new PokedexChoice(()->{},options);choices.add(choice);field(text(id),choice);modified.add(()->choice.selected()>0);rules.add(()->{int selected=choice.selected()-1;return p->rule.test(p,selected);});}
    private void numeric(String title,int min,int max,int initial,BiPredicate<Pokemon,Integer> rule){var field=new Numeric(min,max,initial);numbers.add(field);field(title,field);modified.add(()->field.value()!=initial);rules.add(()->{int value=field.value();return p->rule.test(p,value);});}
    private void checkbox(int id,Predicate<Pokemon> rule){var check=new ToggleButton();check.setTheme("/togglebutton");checks.add(check);field(text(id),check);modified.add(check::isActive);rules.add(()->rule);}
    private void field(String title,Widget value){var label=new Label(title);label.setTheme("/label-title-dex-long");if(pending==null)pending=new Widget[]{label,value};else{var row=new Widget[]{pending[0],pending[1],label,value};rows.add(row);for(var child:row)form.add(child);pending=null;}}
    private void finishRow(){if(pending!=null){rows.add(pending);for(var child:pending)form.add(child);pending=null;}}
    void reset(){applied.clear();markMask=0;for(var choice:choices)choice.select(0);for(var number:numbers)number.reset();for(var check:checks)check.setActive(false);}
    boolean active(){return !applied.isEmpty();}
    boolean matches(Pokemon p){return applied.stream().allMatch(rule->rule.test(p));}
    boolean closeChoices(){for(var choice:choices)if(choice.closeChoices())return true;return false;}
    @Override protected void layout(){int x=getInnerX(),y=getInnerY(),w=getInnerWidth(),h=getInnerHeight();clear.setPosition(x+w-100,y+5);clear.setSize(95,30);scroll.setPosition(x+5,y+40);scroll.setSize(w-10,Math.max(1,h-80));apply.setPosition(x+w-100,y+h-35);apply.setSize(95,30);}
    private static final class Numeric extends Widget {
        final EditField edit=new EditField();final Button less=new Button("-"),more=new Button("+");final int min,max,initial;
        Numeric(int min,int max,int initial){this.min=min;this.max=max;this.initial=initial;setTheme("");edit.setTheme("/editfield");edit.setMaxTextLength(4);for(var b:List.of(less,more)){b.setTheme("/button");b.setCanAcceptKeyboardFocus(false);add(b);}add(edit);less.addCallback(()->set(value()-1));more.addCallback(()->set(value()+1));reset();}
        int value(){try{return Math.max(min,Math.min(max,Integer.parseInt(edit.getText())));}catch(NumberFormatException ignored){return initial;}}
        void set(int n){edit.setText(Integer.toString(Math.max(min,Math.min(max,n))));}void reset(){set(initial);}
        @Override protected void layout(){int x=getInnerX(),y=getInnerY(),w=getInnerWidth(),h=getInnerHeight();less.setPosition(x,y);less.setSize(25,h);edit.setPosition(x+28,y);edit.setSize(Math.max(1,w-56),h);more.setPosition(x+w-25,y);more.setSize(25,h);}
    }
}
