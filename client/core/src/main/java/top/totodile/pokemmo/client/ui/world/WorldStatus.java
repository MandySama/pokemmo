package top.totodile.pokemmo.client.ui.world;

import de.matthiasmann.twl.*;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.LongSupplier;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.world.WorldScene;

/** TrainerHudWindow: location, money and virtual weekday/time, with the original atlas icons. */
final class WorldStatus extends Widget {
    private final WorldScene world;
    private final ClientStrings strings;
    private final LongSupplier clock;
    private final Label location=new Label(), money=new Label("$0"), time=new Label();
    private final Widget placeIcon=new Widget(), moneyIcon=new Widget(), timeIcon=new Widget();
    WorldStatus(WorldScene world,ClientStrings strings,LongSupplier clock) {
        this.world=world;this.strings=strings;this.clock=clock;
        setTheme("/world-status");
        placeIcon.setTheme("location");moneyIcon.setTheme("money");timeIcon.setTheme("day");
        for(Widget child:new Widget[]{placeIcon,location,moneyIcon,money,timeIcon,time})add(child);
        update();
    }
    void update() {
        location.setText(strings.getOrDefault(world.locationStringId(),"--"));
        // WorldTimeManager.ki0 uses four virtual seconds per real second. Local play has no server offset.
        long seconds=Math.floorDiv(clock.getAsLong(),1000)*4;
        int minute=(int)Math.floorMod(Math.floorDiv(seconds,60),1440);
        int hour=minute/60;
        int weekday=(int)Math.floorMod(Math.floorDiv(seconds,86400),7);
        Locale locale=Locale.forLanguageTag(strings.language());
        String day=DayOfWeek.of(weekday==0?7:weekday).getDisplayName(TextStyle.FULL,locale);
        time.setText(strings.format(1155,day,String.format(Locale.ROOT,"%02d:%02d",hour,minute%60)));
        String theme=hour<4||hour>=21?"night":hour<11?"morning":"day";
        if(!theme.equals(timeIcon.getTheme()))timeIcon.setTheme(theme);
    }
    void position(int x,int y) { setPosition(x,y);setSize(getPreferredWidth(),getPreferredHeight()); }
    @Override public int getPreferredInnerWidth() {
        return 23+Math.max(location.getPreferredWidth(),Math.max(money.getPreferredWidth(),time.getPreferredWidth()));
    }
    @Override public int getPreferredInnerHeight() {
        return Math.max(18,Math.max(location.getPreferredHeight(),time.getPreferredHeight()))*3;
    }
    @Override protected void layout() {
        int h=getInnerHeight()/3;
        Widget[] icons={placeIcon,moneyIcon,timeIcon};Label[] labels={location,money,time};
        for(int i=0;i<3;i++) {
            icons[i].setSize(18,18);icons[i].setPosition(getInnerX(),getInnerY()+i*h+(h-18)/2);
            labels[i].adjustSize();labels[i].setPosition(getInnerX()+23,getInnerY()+i*h+(h-labels[i].getHeight())/2);
        }
    }
}
