package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;

/** Species pages from PokedexWindow: information, learnset, stats, encounter data and evolution tree. */
final class PokedexDetails extends Widget {
    private static final int[] STAT_NAMES={1720,1721,1722,1724,1725,1723,1726};
    private final BwPokedex dex;
    private final PokedexImages images;
    private final ClientStrings strings;
    private final boolean mobile;
    private final IntConsumer select;
    private final Button[] tabs=new Button[5];
    private final PokedexScrollPane pane=new PokedexScrollPane();
    private final Button back;
    private int speciesId,tab;

    PokedexDetails(BwPokedex dex,PokedexImages images,ClientStrings strings,boolean mobile,IntConsumer select,Runnable goBack) {
        this.dex=dex;this.images=images;this.strings=strings;this.mobile=mobile;this.select=select;setTheme("");
        int[] names={1703,1704,1717,1775,2550};
        for(int i=0;i<tabs.length;i++) {
            int index=i;tabs[i]=new Button(strings.get(names[i]));tabs[i].setTheme("/pokedex-tab");
            tabs[i].setCanAcceptKeyboardFocus(false);tabs[i].addCallback(()->showTab(index));add(tabs[i]);
        }
        pane.setTheme("/pokedex-scroll");pane.setFixed(ScrollPane.Fixed.HORIZONTAL);add(pane);
        back=new Button(strings.get(58));back.setTheme("/button");back.setCanAcceptKeyboardFocus(false);back.addCallback(goBack);add(back);
    }
    void show(int id){speciesId=id;showTab(0);}
    int speciesId(){return speciesId;}
    PokedexScrollPane scrollPane(){return pane;}
    private void showTab(int index) {
        pane.cancelTouch();
        tab=index;
        for(int i=0;i<tabs.length;i++)tabs[i].getAnimationState().setAnimationState(Button.STATE_SELECTED,i==index);
        Widget page=switch(index){case 0->information();case 1->moves();case 2->stats();case 3->encounters();default->evolutions();};
        pane.setContent(page);pane.setScrollPositionY(0);invalidateLayout();
    }
    private Widget information() {
        var s=dex.get(speciesId);
        var rows=new Rows(mobile?38:28);
        rows.addPair(strings.get(1706),dex.type(s.type1())+(s.type1()!=s.type2()?" / "+dex.type(s.type2()):""));
        rows.addPair(strings.get(1707),dex.category(speciesId));
        rows.addPair(strings.get(1709),String.format(Locale.ROOT,"%.1f m",s.height()/10.0));
        rows.addPair(strings.get(1710),String.format(Locale.ROOT,"%.1f kg",s.weight()/10.0));
        rows.addPair(strings.get(1733),s.abilities().subList(0,2).stream().filter(i->i>0).distinct().map(dex::ability).collect(Collectors.joining(" / ")))
                .setTooltipContent(s.abilities().subList(0,2).stream().filter(i->i>0).distinct().map(dex::abilityDescription).collect(Collectors.joining("\n")));
        rows.addPair(strings.get(1886),dex.ability(s.abilities().get(2))).setTooltipContent(dex.abilityDescription(s.abilities().get(2)));
        rows.addPair(strings.get(1734),"—");
        rows.addPair(strings.get(1730),s.eggGroups().stream().distinct().map(dex::eggGroup).collect(Collectors.joining(" / ")));
        var ev=new ArrayList<String>();for(int i=0;i<6;i++)if(s.evYield().get(i)>0)ev.add("+"+s.evYield().get(i)+" "+strings.get(STAT_NAMES[i]).replace(":",""));
        rows.addPair(strings.get(1708),String.join(", ",ev));
        rows.addPair(strings.get(1735),s.heldItems().stream().filter(i->i>0).distinct().map(dex::item).collect(Collectors.joining(" / ")));
        Widget picture=new Widget(){@Override protected void paintWidget(GUI gui){images.front(speciesId).draw(getAnimationState(),getX()+(getWidth()-192)/2,getY(),192,192);}};
        picture.setTheme("");
        WrappedLabel description=new WrappedLabel(dex.description(speciesId));description.setTheme("/pokedex-description");
        return new Widget() {
            {setTheme("");add(picture);add(description);add(rows);}
            @Override public int getPreferredInnerHeight(){return Math.max(320,rows.getPreferredHeight());}
            @Override protected void layout() {
                int left=mobile?230:185;
                picture.setPosition(getInnerX(),getInnerY()+10);picture.setSize(left,192);
                description.setPosition(getInnerX()+4,getInnerY()+210);description.wrap(left-8);description.setSize(left-8,description.getPreferredHeight());
                rows.setPosition(getInnerX()+left+5,getInnerY()+5);rows.setSize(Math.max(100,getInnerWidth()-left-5),rows.getPreferredHeight());
            }
        };
    }
    private Widget moves() {
        var page=new Rows(mobile?40:30);
        page.addColumns(strings.get(1711),strings.get(1716),strings.get(1712),strings.get(1713),strings.get(1714),strings.get(1715));
        for(var learned:dex.get(speciesId).moves()) {
            var m=dex.move(learned.move());
            page.addColumns(learned.level()<=1?strings.get(1732):Integer.toString(learned.level()),dex.moveName(m.id()),dex.type(m.type()),
                    m.power()==0?"—":Integer.toString(m.power()),m.accuracy()==0||m.accuracy()>100?"—":m.accuracy()+"%",Integer.toString(m.pp()));
        }
        return page;
    }
    private Widget stats() {
        var s=dex.get(speciesId);var page=new Widget(){
            @Override public int getPreferredInnerHeight(){return (mobile?48:38)*7+20;}
            @Override protected void layout(){
                int row=mobile?48:38,x=getInnerX()+10,y=getInnerY()+10;
                for(int i=0;i<7;i++) {
                    Widget label=getChild(i*3),value=getChild(i*3+1),bar=getChild(i*3+2);
                    label.setPosition(x,y+i*row);label.setSize(110,row-4);
                    value.setPosition(x+115,y+i*row);value.setSize(65,row-4);
                    bar.setPosition(x+190,y+i*row+(row-10)/2);bar.setSize(Math.max(20,Math.min(250,getInnerWidth()-210)),10);
                }
            }
        };page.setTheme("");
        String[] colors={"#70B858","#F15858","#E8A04C","#A878C8","#C8A040","#5898D8","#D0D0D0"};
        for(int i=0;i<7;i++) {
            int value=i<6?s.stats().get(i):s.total();
            page.add(label(strings.get(STAT_NAMES[i])));page.add(label(Integer.toString(value)));
            page.add(new StatBar((float)value/(i<6?255:680),Color.parserColor(colors[i])));
        }
        return page;
    }
    private Widget encounters() {
        var rows=new Rows(mobile?40:30);
        rows.addColumns(strings.get(1777),strings.get(1778),strings.get(1779),strings.get(1781));
        // Official distribution comes from game-server encounter records; no records exist locally yet.
        rows.addColumns("—","—","—","—");return rows;
    }
    private Widget evolutions() {
        return new PokedexEvolutionTree(dex,images,mobile,speciesId,select);
    }
    private Label label(String text){Label value=new Label(text);value.setTheme("/pokedex-value");return value;}
    @Override protected void layout() {
        int h=mobile?44:30,gap=3,w=(getInnerWidth()-4*gap)/5;
        for(int i=0;i<tabs.length;i++){tabs[i].setPosition(getInnerX()+i*(w+gap),getInnerY());tabs[i].setSize(w,h);}
        back.setSize(mobile?140:100,h);back.setPosition(getInnerRight()-back.getWidth(),getInnerBottom()-h);
        pane.setPosition(getInnerX(),getInnerY()+h+5);pane.setSize(getInnerWidth(),Math.max(0,getInnerHeight()-2*h-10));
    }
    static class WrappedLabel extends Label {
        private final String original;
        WrappedLabel(String value){super(value);original=value;}
        void wrap(int width) {
            if(getFont()==null||width<=0)return;
            var out=new StringBuilder();var line=new StringBuilder();
            for(int i=0;i<original.length();) {
                int cp=original.codePointAt(i);i+=Character.charCount(cp);
                String next=new String(Character.toChars(cp));
                if(cp=='\n'){out.append('\n');line.setLength(0);continue;}
                if(line.length()>0&&getFont().computeTextWidth(line+next)>width){out.append('\n');line.setLength(0);}
                line.append(next);out.append(next);
            }
            if(!getText().contentEquals(out))setText(out.toString());
        }
    }
    private final class Rows extends Widget {
        private final List<Label[]> rows=new ArrayList<>();private final int rowHeight;private int preferred;
        Rows(int height){rowHeight=height;setTheme("");}
        Label addPair(String name,String value){return addColumns(name,value)[1];}
        Label[] addColumns(String... values) {
            Label[] row=new Label[values.length];
            for(int i=0;i<values.length;i++){row[i]=new WrappedLabel(values[i].isEmpty()?"—":values[i]);row[i].setTheme(values.length==2?i==0?"/pokedex-title":"/pokedex-value":rows.isEmpty()?"/pokedex-title":"/pokedex-value");add(row[i]);}
            rows.add(row);return row;
        }
        @Override public int getPreferredInnerHeight(){return Math.max(preferred,rows.size()*(rowHeight+4));}
        @Override protected void layout(){
            int y=getInnerY();
            for(Label[] row:rows) {
                int x=getInnerX(),height=rowHeight;
                int available=Math.max(1,getInnerWidth()-4*(row.length-1));
                for(int col=0;col<row.length;col++) {
                    int w=row.length==2?(col==0?Math.min(110,getInnerWidth()/3):getInnerWidth()-Math.min(110,getInnerWidth()/3)-4):
                            row.length==6?(col==1?available*30/100:available*14/100):available/row.length;
                    var label=(WrappedLabel)row[col];label.wrap(Math.max(1,w-12));height=Math.max(height,label.getPreferredHeight());
                    label.setPosition(x,y);label.setSize(w,height);x+=w+4;
                }
                for(Label label:row)label.setSize(label.getWidth(),height);
                y+=height+4;
            }
            int next=y-getInnerY();if(next!=preferred){preferred=next;invalidateLayout();}
        }
    }
    private static final class StatBar extends Widget {
        private final float value;private final Color color;private Image fill;
        StatBar(float value,Color color){this.value=Math.min(1,value);this.color=color;setTheme("/pokedex-statbar");}
        @Override protected void applyTheme(ThemeInfo info){super.applyTheme(info);Image image=info.getImage("progressImage");fill=image==null?null:image.createTintedVersion(color);}
        @Override protected void paintWidget(GUI gui){if(fill!=null&&value>0)fill.draw(getAnimationState(),getInnerX(),getInnerY(),Math.round(getInnerWidth()*value),getInnerHeight());}
    }
}
