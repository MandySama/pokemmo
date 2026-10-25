package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import java.util.*;
import top.totodile.pokemmo.client.ui.common.PlayerWindow;
import top.totodile.pokemmo.client.ui.pokedex.PokedexChoice;

/** Reference EncounterRecorderWindow; shares the counter's local preview records. */
public final class EncounterRecorderWindow extends PlayerWindow {
    private static final String[] CATEGORIES={"上次闪光","种族累计","野外","野外甜甜香气","野外其他","头目","巢穴","神秘精灵球","孵蛋","化石","行程"};
    private final PokemonController pokemon;
    private final EncounterCounterWindow counter;
    private final Widget content=new Widget(){@Override protected void layout(){layoutContent();}};
    private final ToggleButton profile=new ToggleButton("资料"),history=new ToggleButton("历史"),hudToggle=new ToggleButton(),missed=new ToggleButton();
    private final Label totalLabel=new Label("遭遇:"),total=new Label(),rateLabel=new Label("野外闪率:"),rate=new Label("1 / 30000"),note=new Label(),missedLabel=new Label("显示错过的闪光:");
    private final Button hudLabel=new Button("总计"),speciesHeader=new Button("宝可梦 ↕"),countHeader=new Button("种族遭遇"),pinHeader=new Button("置顶");
    private final Label timeHeader=new Label("捕获时间"),historySpeciesHeader=new Label("种族遭遇");
    private final PokedexChoice category=new PokedexChoice(()->{},CATEGORIES);
    private final Widget list=new Widget(){
        @Override public int getPreferredInnerHeight(){return getNumChildren()*42;}
        @Override protected void layout(){for(int i=0;i<getNumChildren();i++){var row=getChild(i);row.setPosition(getInnerX(),getInnerY()+i*42);row.setSize(getInnerWidth(),40);}}
    };
    private final ScrollPane scroll=new ScrollPane(list);
    private boolean historyPage,sortName,ascending;

    public EncounterRecorderWindow(Widget owner,PokemonController pokemon,EncounterCounterWindow counter,Runnable closed){
        super(owner,false,"/encounter-recorder-frame",540,405,closed);this.pokemon=pokemon;this.counter=counter;
        frame.setTitle("遭遇记录仪");content.setTheme("/encounter-recorder-content");frame.add(content);list.setTheme("");
        for(var tab:new ToggleButton[]{profile,history}){tab.setTheme("/encounter-recorder-tab");tab.setCanAcceptKeyboardFocus(false);content.add(tab);}
        profile.addCallback(()->switchPage(false));history.addCallback(()->switchPage(true));
        totalLabel.setTheme("/label");rateLabel.setTheme("/label");missedLabel.setTheme("/label");
        for(var label:new Label[]{total,rate,note})label.setTheme("/encounter-recorder-value");
        hudLabel.setTheme("/encounter-recorder-hud-label");hudLabel.setCanAcceptKeyboardFocus(false);hudLabel.addCallback(()->counter.hudVisible(!counter.isVisible()));
        hudToggle.setTheme("/togglebutton");hudToggle.setCanAcceptKeyboardFocus(false);hudToggle.addCallback(()->counter.hudVisible(hudToggle.isActive()));
        missed.setTheme("/togglebutton");missed.setCanAcceptKeyboardFocus(false);
        category.setTheme("/combobox");
        for(var button:new Button[]{speciesHeader,countHeader,pinHeader}){button.setTheme("/encounter-recorder-header");button.setCanAcceptKeyboardFocus(false);}
        timeHeader.setTheme("/encounter-recorder-header-label");historySpeciesHeader.setTheme("/encounter-recorder-header-label");
        speciesHeader.addCallback(()->{if(sortName)ascending=!ascending;else{sortName=true;ascending=true;}refresh();});
        countHeader.addCallback(()->{if(!sortName)ascending=!ascending;else{sortName=false;ascending=false;}refresh();});
        scroll.setTheme("/scrollpane");scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        for(var widget:new Widget[]{totalLabel,total,rateLabel,rate,hudLabel,hudToggle,category,note,missed,missedLabel,speciesHeader,countHeader,pinHeader,timeHeader,historySpeciesHeader,scroll})content.add(widget);
        counter.onChanged(()->{if(isOpen())refresh();});
    }
    private void switchPage(boolean history){historyPage=history;category.closeChoices();scroll.setScrollPositionY(0);refresh();}
    @Override protected void refresh(){
        profile.setActive(!historyPage);history.setActive(historyPage);totalLabel.setText(historyPage?"已捕获闪光宝可梦:":"遭遇:");total.setText(historyPage?"0":Integer.toString(counter.total()));hudToggle.setActive(counter.isVisible());
        for(var widget:new Widget[]{rateLabel,rate,hudLabel,hudToggle,note,pinHeader})widget.setVisible(!historyPage);
        missed.setVisible(historyPage);missedLabel.setVisible(historyPage);timeHeader.setVisible(historyPage);historySpeciesHeader.setVisible(historyPage);
        speciesHeader.setText(historyPage?"宝可梦":"宝可梦 ↕");countHeader.setText(historyPage?"遭遇":"种族遭遇");
        list.removeAllChildren();
        if(!historyPage){
            var values=new ArrayList<>(counter.records());
            if(sortName)values.sort(Comparator.comparing(e->pokemon.dex().name(e.species())));
            else if(ascending)values.sort(Comparator.comparingInt(EncounterCounterWindow.Entry::count));
            if(sortName&&!ascending)Collections.reverse(values);
            for(var value:values)list.add(new SpeciesRow(value));
        }
        content.invalidateLayout();
    }
    public void back(){if(!category.closeChoices())closePopup();}
    @Override public void closePopup(){category.closeChoices();super.closePopup();}
    @Override protected boolean handleFrameEvent(Event event){
        if(event.isKeyPressedEvent()&&event.getKeyCode()==Event.KEY_ESCAPE&&category.closeChoices())return true;return false;
    }
    private void layoutContent(){
        int x=content.getInnerX(),y=content.getInnerY(),w=content.getInnerWidth(),h=content.getInnerHeight();
        place(profile,x+7,y,62,26);place(history,x+69,y,62,26);
        place(totalLabel,x+2,y+34,historyPage?126:62,30);place(total,x+(historyPage?132:66),y+34,historyPage?70:100,30);
        place(rateLabel,x+2,y+69,64,30);place(rate,x+66,y+69,100,30);
        place(hudLabel,x+203,y+34,36,30);place(hudToggle,x+239,y+34,26,26);
        place(category,x+w-212,y+34,210,30);place(note,x+w-212,y+69,210,30);
        place(missedLabel,x+w-156,y+69,126,30);place(missed,x+w-28,y+69,26,26);
        int first=w-146;
        place(speciesHeader,x+2,y+106,first,32);place(countHeader,x+first+6,y+106,68,32);place(pinHeader,x+first+78,y+106,68,32);
        place(timeHeader,x+w-142,y+106,140,32);
        if(historyPage){place(speciesHeader,x+2,y+106,w-310,32);place(countHeader,x+w-304,y+106,68,32);place(historySpeciesHeader,x+w-232,y+106,86,32);}
        place(scroll,x+2,y+140,w-4,Math.max(0,h-144));
    }
    private static void place(Widget widget,int x,int y,int w,int h){widget.setPosition(x,y);widget.setSize(w,h);}
    private final class SpeciesRow extends Widget {
        private final Widget icon=new Widget(){@Override protected void paintWidget(GUI gui){var image=pokemon.images().icon(value.species());image.draw(getAnimationState(),getX()+(getWidth()-36)/2,getY()+(getHeight()-36)/2,36,36);}};
        private final Label name,count;
        private final Button pin;
        private final EncounterCounterWindow.Entry value;
        SpeciesRow(EncounterCounterWindow.Entry value){
            this.value=value;setTheme("");icon.setTheme("/encounter-recorder-cell");name=new Label(pokemon.dex().name(value.species()));name.setTheme("/encounter-recorder-cell-name");count=new Label(Integer.toString(value.count()));count.setTheme("/encounter-recorder-cell-count");
            pin=new Button(counter.pinned(value.species())?"取消置顶":"置顶");pin.setTheme("/encounter-recorder-pin");pin.setCanAcceptKeyboardFocus(false);pin.addCallback(()->counter.togglePinned(value.species()));
            add(icon);add(name);add(count);add(pin);
        }
        @Override protected void layout(){int w=getInnerWidth(),first=w-144;place(icon,getX(),getY(),38,getHeight());place(name,getX()+41,getY(),Math.max(0,first-41),getHeight());place(count,getX()+first+3,getY(),68,getHeight());place(pin,getX()+first+75,getY(),68,getHeight());}
    }
}
