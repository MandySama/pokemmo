package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.ToggleButtonModel;
import java.util.*;
import java.util.function.IntConsumer;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;

/** Official EV horde search: season, EV stat, location and three time-of-day columns. */
final class PokedexHordes extends Widget {
    private record Place(String kind,int region,String name) {}
    private final BwPokedex dex;
    private final PokedexImages images;
    private final ClientStrings strings;
    private final boolean mobile;
    private final IntConsumer select;
    private final Button[] seasons=new Button[4],stats=new Button[6];
    private final PokedexScrollPane pane=new PokedexScrollPane();
    private int season,stat;
    PokedexHordes(BwPokedex dex,PokedexImages images,ClientStrings strings,boolean mobile,IntConsumer select){
        this.dex=dex;this.images=images;this.strings=strings;this.mobile=mobile;this.select=select;setTheme("");
        int[] ids={6496,6497,6498,6499};
        for(int i=0;i<4;i++){int index=i;seasons[i]=new PokedexSeasonButton(images,strings.get(ids[i]),mobile,i,()->{season=index;refresh();});add(seasons[i]);}
        for(int i=0;i<6;i++){int index=i;stats[i]=button(strings.get(PokedexDetails.STAT_NAMES[i]).replace(":",""),()->{stat=index;refresh();});add(stats[i]);}
        add(pane);refresh();
    }
    private Button button(String text,Runnable action){Button button=new Button(text);button.setTheme("/pokedex-tab");button.setModel(new ToggleButtonModel());button.setCanAcceptKeyboardFocus(false);button.addCallback(action);return button;}
    PokedexScrollPane scrollPane(){return pane;}
    private void refresh(){
        pane.cancelTouch();for(int i=0;i<4;i++)((ToggleButtonModel)seasons[i].getModel()).setSelected(i==season);
        for(int i=0;i<6;i++)((ToggleButtonModel)stats[i].getModel()).setSelected(i==stat);
        var matches=new LinkedHashMap<Place,List<Set<Integer>>>();
        for(var pokemon:dex.all())if(pokemon.evYield().get(stat)>0)for(var location:dex.extra(pokemon.id()).locations()){
            if(!location.rarity().contains("群")||location.season()>=0&&location.season()!=season)continue;
            var groups=matches.computeIfAbsent(new Place(location.kind(),location.region(),location.name()),p->List.of(new LinkedHashSet<>(),new LinkedHashSet<>(),new LinkedHashSet<>()));
            for(int time=0;time<3;time++)if((location.timeMask()&(1<<time))!=0)groups.get(time).add(pokemon.id());
        }
        var rows=new PokedexRows(mobile?44:32,75,70,190,100,100,100).table(0,1,2,3,4,5);
        rows.titles(strings.get(1776),strings.get(1777),strings.get(1778),strings.get(1770),strings.get(1771),strings.get(1772));
        for(var entry:matches.entrySet()){
            Place p=entry.getKey();rows.row(PokedexRows.value(p.kind),PokedexRows.value(dex.regionName(p.region)),PokedexRows.value(p.name),
                    icons(entry.getValue().get(0)),icons(entry.getValue().get(1)),icons(entry.getValue().get(2)));
        }
        if(matches.isEmpty())rows.values("--","--",strings.get(1791),"--","--","--");
        pane.setContent(rows);pane.setScrollPositionY(0);
    }
    private Widget icons(Set<Integer> species){
        Widget cell=new Widget(){
            @Override public int getPreferredInnerHeight(){int size=mobile?40:30,columns=Math.max(1,getInnerWidth()/size);return Math.max(mobile?44:32,(getNumChildren()+columns-1)/columns*size);}
            @Override protected void layout(){int size=mobile?40:30,columns=Math.max(1,getInnerWidth()/size);for(int i=0;i<getNumChildren();i++){getChild(i).setSize(size,size);getChild(i).setPosition(getInnerX()+i%columns*size,getInnerY()+i/columns*size);}}
        };cell.setTheme("/pokedex-value");
        for(int id:species){
            Button button=new Button(){@Override protected void paintWidget(GUI gui){int size=mobile?40:30;images.icon(id).draw(getAnimationState(),getX(),getY(),size,size);}};
            button.setTheme("/pokedex-icon-button");button.setCanAcceptKeyboardFocus(false);button.setTooltipContent(dex.name(id)+" +"+dex.get(id).evYield().get(stat));button.addCallback(()->select.accept(id));cell.add(button);
        }
        return cell;
    }
    @Override protected void layout(){
        int h=mobile?42:26,y=getInnerY();
        for(int i=0;i<4;i++){seasons[i].setSize(mobile?90:80,h);seasons[i].setPosition(getInnerX()+i*(mobile?90:80),y);}
        for(int i=0;i<6;i++){int w=getInnerWidth()/6;stats[i].setSize(w,h);stats[i].setPosition(getInnerX()+i*w,y+h);}
        pane.setPosition(getInnerX(),y+h*2+5);pane.setSize(getInnerWidth(),Math.max(1,getInnerHeight()-h*2-5));
    }
}
