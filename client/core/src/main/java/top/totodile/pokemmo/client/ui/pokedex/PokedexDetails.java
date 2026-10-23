package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.ToggleButtonModel;
import de.matthiasmann.twl.renderer.Image;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;

/** Six detail tabs verified against the official desktop client and monster-dex theme. */
final class PokedexDetails extends Widget {
    static final int[] STAT_NAMES={1720,1721,1722,1724,1725,1723,1726};
    private final BwPokedex dex;
    private final PokedexImages images;
    private final ClientStrings strings;
    private final boolean mobile;
    private final IntConsumer select;
    private final Button[] tabs=new Button[6];
    private final BoxLayout tabBar=new BoxLayout();
    private final PokedexScrollPane pane=new PokedexScrollPane();
    private final Button back;
    private int speciesId,tab,form,sortColumn,season;
    private boolean shiny,descending,unlocked;
    private long animationStart;

    PokedexDetails(BwPokedex dex,PokedexImages images,ClientStrings strings,boolean mobile,IntConsumer select,Runnable goBack) {
        this.dex=dex;this.images=images;this.strings=strings;this.mobile=mobile;this.select=select;setTheme("");
        int[] names={1703,1704,1717,6468,1775,2550};
        if(!mobile){tabBar.setTheme("/pokedex-detail-tabs");add(tabBar);}
        for(int i=0;i<tabs.length;i++){
            int index=i;tabs[i]=new Button(strings.get(names[i]));tabs[i].setTheme(mobile?"/pokedex-tab":"/pokedex-detail-tab");tabs[i].setModel(new ToggleButtonModel());
            tabs[i].setCanAcceptKeyboardFocus(false);tabs[i].addCallback(()->showTab(index));if(mobile)add(tabs[i]);else tabBar.add(tabs[i]);
        }
        add(pane);back=new Button(strings.get(58));back.setTheme("/pokedex-small-button");back.setCanAcceptKeyboardFocus(false);back.addCallback(goBack);
        if(mobile)add(back);
    }
    void show(int id){speciesId=id;form=0;shiny=false;animationStart=System.nanoTime();showTab(0);}
    int speciesId(){return speciesId;}
    void setUnlocked(boolean value){if(unlocked==value)return;unlocked=value;if(speciesId>0)showTab(tab);}
    PokedexScrollPane scrollPane(){return pane;}
    int preferredInformationHeight(){
        return tab==0&&pane.getContent()!=null?26+pane.getBorderVertical()+pane.getContent().getPreferredHeight():0;
    }
    private int rowHeight(){return mobile?42:28;}
    private void showTab(int index){
        pane.cancelTouch();tab=index;
        String paneTheme=index==1?"/pokedex-move-scroll":index==0?"/pokedex-information-scroll":index==4?"/pokedex-locations-scroll":"/pokedex-scroll";
        if(!pane.getTheme().equals(paneTheme)){pane.setTheme(paneTheme);pane.reapplyTheme();}
        for(int i=0;i<tabs.length;i++)((ToggleButtonModel)tabs[i].getModel()).setSelected(i==index);
        Widget page=!unlocked&&index!=0?PokedexRows.value("?????"):switch(index){case 0->information();case 1->moves();case 2->stats();case 3->effectiveness();case 4->encounters();default->evolutions();};
        pane.setContent(page);pane.setScrollPositionY(0);invalidateLayout();
    }
    private Widget information(){
        var s=dex.get(speciesId);var extra=dex.extra(speciesId);var rows=new PokedexForm();
        Widget category=new Widget(){
            final Label label=PokedexRows.value(unlocked?dex.category(speciesId):"?????");
            final Widget types=new PokedexTypeBadge(images,mobile,s.type1()==s.type2()?new int[]{s.type1()}:new int[]{s.type1(),s.type2()});
            {setTheme("/pokedex-information-value");add(label);add(types);types.setVisible(unlocked);label.setTheme("/pokedex-plain");}
            @Override public int getPreferredInnerWidth(){return Math.max(0,getMinWidth()-getBorderHorizontal());}
            @Override public int getPreferredInnerHeight(){return Math.max(label.getPreferredHeight(),types.getPreferredHeight());}
            @Override protected void layout(){int width=types.getPreferredWidth();types.setSize(width,getInnerHeight());types.setPosition(getInnerRight()-width,getInnerY());
                label.setSize(Math.max(1,getInnerWidth()-width-4),getInnerHeight());label.setPosition(getInnerX(),getInnerY());}
        };
        rows.row(PokedexForm.title(strings.get(1706)),category);
        rows.values(strings.get(1707),dex.description(speciesId));
        rows.values(strings.get(1709),String.format(Locale.ROOT,"%.1f m / %.1f kg",s.height()/10.0,s.weight()/10.0));
        String ability=s.abilities().subList(0,2).stream().filter(i->i>0).distinct().map(dex::ability).collect(Collectors.joining(" / "));
        if(s.abilities().get(2)>0)ability+=" / "+strings.format(6480,dex.ability(s.abilities().get(2)));
        Label abilities=PokedexForm.value(ability);abilities.setTooltipContent(s.abilities().stream().filter(i->i>0).distinct().map(dex::abilityDescription).collect(Collectors.joining("\n")));
        rows.row(PokedexForm.title(strings.get(1733)),abilities);
        String items=s.heldItems().stream().filter(i->i>0).distinct().map(dex::item).collect(Collectors.joining(", "));
        rows.values(strings.get(1735),items.isEmpty()?strings.getOrDefault(61,"无"):items);
        rows.values(strings.get(1730),s.eggGroups().stream().distinct().map(dex::eggGroup).collect(Collectors.joining(", ")));
        var ev=new ArrayList<String>();for(int i=0;i<6;i++)if(s.evYield().get(i)>0)ev.add("+"+s.evYield().get(i)+" "+strings.get(STAT_NAMES[i]).replace(":",""));
        rows.values(strings.get(1708),String.join(", ",ev));
        rows.values(strings.get(6492),dex.gender(speciesId));
        rows.values(strings.get(6493),strings.format(6482,dex.growthName(speciesId),String.format(Locale.ROOT,"%,d",dex.experienceAt100(speciesId))));
        Label capture=PokedexForm.value(Integer.toString(extra.catchRate()));capture.setTooltipContent(strings.get(6489));rows.row(PokedexForm.title(strings.get(6494)),capture);
        rows.values(strings.get(1734),extra.tiers().stream().map(t->strings.get(5750+t)).collect(java.util.stream.Collectors.joining(" / ")));
        if(!unlocked)for(int i=1;i<rows.getNumChildren();i+=2)if(rows.getChild(i) instanceof WrappedLabel label){label.replace("?????");label.setTooltipContent(null);}
        Widget portrait=new Widget(){
            final Label name=new Label(String.format(Locale.ROOT,"%s %03d %s",strings.get(1705),speciesId,unlocked?dex.name(speciesId):"???"));
            {setTheme("/pokedex-picture");name.setTheme("/pokedex-picture-name");add(name);}
            // StatefulImageButton is fixed at 170x185 / 200x200; its sprite may overflow it.
            @Override public int getPreferredWidth(){return mobile?200:170;}
            @Override public int getPreferredHeight(){return mobile?200:185;}
            @Override protected void layout(){name.setPosition(getInnerX(),getInnerY());name.setSize(getInnerWidth(),name.computeTextHeight());}
        };
        Button sound=new Button(strings.get(1727));sound.setTheme("/pokedex-small-button");sound.setCanAcceptKeyboardFocus(false);
        Button sparkle=new Button(strings.get(6470));sparkle.setTheme("/pokedex-small-button");sparkle.setCanAcceptKeyboardFocus(false);
        sparkle.addCallback(()->{shiny=!shiny;animationStart=System.nanoTime();});
        Button next=new Button(strings.get(1760));next.setTheme("/pokedex-small-button");next.setCanAcceptKeyboardFocus(false);next.addCallback(()->{form=(form+1)%Math.max(1,extra.formCount());animationStart=System.nanoTime();});
        next.setVisible(extra.formCount()>1);
        return new DialogLayout(){
            {setTheme("/pokedex-information");setAddDefaultGaps(false);setIncludeInvisibleWidgets(false);
                // Preserve the reference's two parallel columns and independently sized left controls.
                var left=createParallelGroup(portrait,sound,sparkle,next);
                setHorizontalGroup(createSequentialGroup().addGroup(left).addGap(15).addWidget(rows));
                var controls=createSequentialGroup().addWidget(portrait).addGap(5).addWidget(sound).addGap(5).addWidget(sparkle).addGap(5).addWidget(next);
                setVerticalGroup(createParallelGroup(controls,createSequentialGroup(rows).addGap()));
            }
            @Override protected void paintOverlay(GUI gui){
                super.paintOverlay(gui);
                Image image=images.animatedFront(speciesId,form,shiny,(System.nanoTime()-animationStart)/1_000_000_000.0);
                if(!unlocked)image=image.createTintedVersion(new Color((byte)0,(byte)0,(byte)0,(byte)127));
                // Native sprite overlay: keep the page scissor, not the picture rectangle's bounds.
                // Reference native frames are 96x96; the animation's 192x128
                // canvas and camera offset must retain that native frame origin.
                int nativeHeight=images.front(speciesId,form,shiny).getHeight();
                int originOffset=PokedexSpriteAnimation.CAMERA_Y-(image.getHeight()-nativeHeight)/2;
                int top=portrait.getChild(0).getBottom();
                var renderer=gui.getRenderer();renderer.clipEnter(getX(),top,getWidth(),Math.max(0,getBottom()-top));
                try{
                    image.draw(portrait.getAnimationState(),portrait.getX()+80+(mobile?20:7)-image.getWidth(),portrait.getY()+80+(mobile?40:20)-image.getHeight()-originOffset*2,image.getWidth()*2,image.getHeight()*2);
                }finally{renderer.clipLeave();}
            }
        };
    }
    private Widget moves(){
        // dex-move-table uses different column widths and renderer padding on Android.
        int[] widths=mobile?new int[]{200,280,130,65,65,65}:new int[]{120,210,65,65,65,65};
        var page=new PokedexRows(mobile?50:30,widths).table().headerHeight(mobile?40:30).rowTheme("/pokedex-move-row");
        page.setTheme("/pokedex-move-table");
        int[] names={1711,1716,1712,1713,1715,1714};Widget[] header=new Widget[6];
        for(int i=0;i<6;i++){int column=i;Button button=new Button(strings.get(names[i]));
            button.setTheme("/pokedex-move-header");button.setCanAcceptKeyboardFocus(false);button.addCallback(()->{if(sortColumn==column)descending=!descending;else{sortColumn=column;descending=false;}showTab(1);});header[i]=button;}
        for(int i=0;i<header.length;i++){
            header[i].getAnimationState().setAnimationState(de.matthiasmann.twl.renderer.AnimationState.StateKey.get("sortAscending"),sortColumn==i&&!descending);
            header[i].getAnimationState().setAnimationState(de.matthiasmann.twl.renderer.AnimationState.StateKey.get("sortDescending"),sortColumn==i&&descending);
        }
        page.row(header);
        var learned=new ArrayList<>(dex.learnset(speciesId));
        Comparator<BwPokedex.LearnedMove> order=switch(sortColumn){
            case 1->Comparator.comparing(m->dex.moveName(m.move()));
            case 2->Comparator.comparingInt(m->dex.move(m.move()).type());
            case 3->Comparator.comparingInt(m->dex.move(m.move()).power());
            case 4->Comparator.comparingInt(m->dex.move(m.move()).pp());
            case 5->Comparator.comparingInt(m->dex.move(m.move()).accuracy());
            default->Comparator.<BwPokedex.LearnedMove>comparingInt(m->m.source()==6?-1:m.source()<0?m.level():256+m.source()).thenComparing(m->dex.moveName(m.move()));
        };
        learned.sort(descending?order.reversed():order);
        for(var learnedMove:learned){
            var move=dex.move(learnedMove.move());Label name=new Label(dex.moveName(move.id()));name.setTooltipContent(dex.moveDescription(move.id()));
            Widget[] cells={new Label(dex.moveSource(learnedMove)),name,new PokedexTypeBadge(images,mobile,move.type()),
                    new Label(move.power()==0?"-":Integer.toString(move.power())),new Label(Integer.toString(move.pp())),
                    new Label(move.accuracy()==0||move.accuracy()>100?"-":Integer.toString(move.accuracy()))};
            page.row(cells);
            for(int i=0;i<cells.length;i++){
                cells[i].setTheme(i==1?"/pokedex-move-name":i==2?"/pokedex-move-type":"/pokedex-move-value");
                cells[i].setClip(true);
            }
        }
        return page;
    }
    private Widget stats(){
        var s=dex.get(speciesId);Widget body=new Widget(){
            @Override protected void layout(){
                int h=rowHeight(),width=Math.min(getInnerWidth(),mobile?620:456),column=Math.max(0,(width-6)/3),x=getInnerX()+(getInnerWidth()-width)/2;
                for(int i=0;i<7;i++){
                    Widget title=getChild(i*3),value=getChild(i*3+1),bar=getChild(i*3+2);
                    title.setSize(column,h);value.setSize(column,h);bar.setSize(Math.max(0,width-6-column*2),h);
                    title.setPosition(x,getInnerY()+i*(h+3));value.setPosition(title.getRight()+3,title.getY());bar.setPosition(value.getRight()+3,title.getY());
                }
            }
        };body.setTheme("");
        String[] styles={"hp","attack","defense","spatk","spdef","speed","total"};
        for(int i=0;i<7;i++){int number=i<6?s.stats().get(i):s.total();body.add(PokedexRows.title(strings.get(STAT_NAMES[i])));Label value=PokedexRows.title(Integer.toString(number));body.add(value);body.add(new StatBar((float)number/(i<6?255:680),styles[i]));}
        return body;
    }
    private Widget effectiveness(){
        var rows=new PokedexRows(rowHeight(),125,275);rows.titles("",strings.get(6461));
        double[] groups={4,2,1,.5,.25,0};int[] labels={6463,6464,6462,6465,6466,6467};
        for(int i=0;i<groups.length;i++){
            var attacks=new ArrayList<Integer>();for(int type=0;type<17;type++)if(dex.effectiveness(type,speciesId)==groups[i])attacks.add(type);
            Widget badges=new Widget(){
                {setTheme("/pokedex-value");for(int type:attacks)add(new PokedexTypeBadge(images,mobile,type));}
                @Override public int getPreferredInnerHeight(){int columns=Math.max(1,(getInnerWidth()+(mobile?6:5))/(mobile?104:54));return Math.max(mobile?28:14,((attacks.size()+columns-1)/columns)*(mobile?31:17));}
                @Override protected void layout(){int w=mobile?104:54,h=mobile?31:17,columns=Math.max(1,(getInnerWidth()+(mobile?6:5))/w);
                    for(int j=0;j<getNumChildren();j++){getChild(j).setSize(w,h);getChild(j).setPosition(getInnerX()+j%columns*w,getInnerY()+j/columns*h);}}
            };
            Label empty=PokedexRows.value("--");empty.setTheme("/pokedex-value-centered");
            rows.row(PokedexRows.title(strings.get(labels[i])),attacks.isEmpty()?empty:badges);
        }
        return centered(rows,mobile?620:400);
    }
    private Widget encounters(){
        var rows=new PokedexLocations();
        rows.titles(strings.get(1776),strings.get(1777),strings.get(1778),strings.get(1779),strings.get(1770),strings.get(1771),strings.get(1772));
        var entries=dex.extra(speciesId).locations().stream().filter(l->l.season()<0||l.season()==season).toList();
        for(var location:entries){
            String level=location.minimum()==location.maximum()?Integer.toString(location.minimum()):location.minimum()+" - "+location.maximum();
            if((location.timeMask()&7)==7)rows.values(location.kind(),dex.regionName(location.region()),location.name(),level,location.rarity());
            else rows.values(location.kind(),dex.regionName(location.region()),location.name(),level,
                    (location.timeMask()&1)!=0?location.rarity():"-",(location.timeMask()&2)!=0?location.rarity():"-",(location.timeMask()&4)!=0?location.rarity():"-");
        }
        if(entries.isEmpty())rows.values("--","--",strings.get(1791),"--","--","--","--");
        return new DialogLayout(){
            final Button[] seasons=new Button[4];
            {setTheme("/pokedex-locations");setAddDefaultGaps(false);
                var tabs=createSequentialGroup();int[] ids={6496,6497,6498,6499};
                for(int i=0;i<4;i++){
                    final int index=i;seasons[i]=new PokedexSeasonButton(images,strings.get(ids[i]),mobile,i,()->{season=index;showTab(4);});
                    seasons[i].setTheme("/pokedex-season-tab");((ToggleButtonModel)seasons[i].getModel()).setSelected(i==season);
                    if(i>0)tabs.addGap(2);tabs.addWidget(seasons[i]);
                }
                setHorizontalGroup(createParallelGroup(createSequentialGroup().addGap().addGroup(tabs),createSequentialGroup(rows)));
                setVerticalGroup(createSequentialGroup().addGroup(createParallelGroup(seasons)).addGap(10).addWidget(rows).addGap());
            }
        };
    }
    private Widget centered(Widget child,int width){
        return new Widget(){
            {setTheme("");add(child);}
            @Override public int getPreferredInnerHeight(){return child.getPreferredHeight()+16;}
            @Override protected void layout(){child.setSize(Math.min(getInnerWidth(),width),child.getPreferredHeight());child.setPosition(getInnerX()+(getInnerWidth()-child.getWidth())/2,getInnerY()+8);}
        };
    }
    private Widget evolutions(){return new PokedexEvolutionTree(dex,images,mobile,speciesId,select);}
    @Override protected void layout(){
        int h=mobile?44:26,x=getInnerX(),gap=2;int[] widths={70,65,85,100,100,85};
        double scale=mobile?(double)getInnerWidth()/515:Math.min(1,(double)getInnerWidth()/515);
        if(mobile)for(int i=0;i<tabs.length;i++){int w=(int)(widths[i]*scale);tabs[i].setPosition(x,getInnerY());tabs[i].setSize(w,h);x+=w+gap;}
        else{tabBar.setPosition(getInnerX(),getInnerY());tabBar.setSize(getInnerWidth(),h);}
        int footer=mobile?50:0;pane.setPosition(getInnerX(),getInnerY()+h);pane.setSize(getInnerWidth(),Math.max(0,getInnerHeight()-h-footer));
        if(mobile){back.setSize(140,44);back.setPosition(getInnerRight()-140,getInnerBottom()-44);}
    }
    static class WrappedLabel extends Label {
        private String original;
        WrappedLabel(String value){super(value);original=value;}
        void replace(String value){original=value;setText(value);}
        void wrap(int width){
            if(getFont()==null||width<=0)return;var out=new StringBuilder();var line=new StringBuilder();
            for(int i=0;i<original.length();){int cp=original.codePointAt(i);i+=Character.charCount(cp);String next=new String(Character.toChars(cp));
                if(cp=='\n'){out.append('\n');line.setLength(0);continue;}
                if(line.length()>0&&getFont().computeTextWidth(line+next)>width){out.append('\n');line.setLength(0);}
                line.append(next);out.append(next);}
            if(!getText().contentEquals(out))setText(out.toString());
        }
    }
    private static final class StatBar extends Widget {
        private final float value;private Image fill,track;
        StatBar(float value,String style){this.value=Math.min(1,value);setTheme("/pokedex-stat-"+style);}
        @Override protected void applyTheme(ThemeInfo info){super.applyTheme(info);fill=info.getImage("progressImage");track=info.getImage("progressBackground");}
        @Override protected void paintWidget(GUI gui){
            int x=getInnerX()+5,y=getInnerY()+(getInnerHeight()-10)/2,width=Math.max(0,getInnerWidth()-10);
            if(track!=null)track.draw(getAnimationState(),x,y,width,10);
            if(fill!=null&&value>0)fill.draw(getAnimationState(),x,y,Math.round(width*value),10);
        }
    }
}
