package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.PlayerWindow;

/** Official incubator-frame / vm0: descriptor left, three-column slots right.
 * The supplied PC screenshot uses four rows; no incubator inventory is fabricated. */
public final class IncubatorWindow extends PlayerWindow {
    private final Label description=new Label();
    private final Widget content=new Widget(){@Override protected void layout(){arrange();}};
    private final Button monster=new Button(),remove,fill;
    private final ToggleButton[] slots=new ToggleButton[12];
    public IncubatorWindow(Widget owner,ClientStrings strings,boolean mobile,Runnable close){
        super(owner,mobile,"/incubator-frame",456,480,close);
        frame.setTitle(strings.get(1471));content.setTheme("/incubator-content");frame.add(content);
        description.setTheme("/incubator-description");description.setText(strings.get(101792));
        monster.setTheme("/incubator-slot");monster.setEnabled(false);content.add(monster);content.add(description);
        for(int i=0;i<slots.length;i++){
            final int index=i;slots[i]=new ToggleButton();slots[i].setTheme("/incubator-slot");
            slots[i].setTooltipContent(strings.get(101802));
            slots[i].addCallback(()->{for(int j=0;j<slots.length;j++)slots[j].setActive(j==index);});
            content.add(slots[i]);
        }
        remove=new Button(strings.get(1479));fill=new Button(strings.get(1480));
        for(var b:new Button[]{remove,fill}){b.setTheme("/button");b.setEnabled(false);content.add(b);}
    }
    @Override protected void refresh(){}
    private void arrange(){
        content.setPosition(frame.getInnerX(),frame.getInnerY());content.setSize(frame.getInnerWidth(),frame.getInnerHeight());
        int x=content.getInnerX(),y=content.getInnerY(),w=content.getInnerWidth(),h=content.getInnerHeight();
        int left=Math.min(180,w*43/100),right=x+left+26,rw=Math.max(0,w-left-36),slotW=Math.min(56,rw/3-8),slotH=mobile?62:46,gap=12;
        monster.setPosition(x+(left-slotW)/2,y+8);monster.setSize(slotW,slotH);
        description.setPosition(x,y+slotH+22);description.setSize(left,Math.max(0,h-slotH-22));
        for(int i=0;i<slots.length;i++){slots[i].setPosition(right+(i%3)*(slotW+16),y+12+(i/3)*(slotH+gap));slots[i].setSize(slotW,slotH);}
        int by=y+12+4*(slotH+gap);remove.setPosition(right,by);remove.setSize(rw,30);fill.setPosition(right,by+34);fill.setSize(rw,30);
    }
    @Override protected void layout(){super.layout();arrange();}
}
