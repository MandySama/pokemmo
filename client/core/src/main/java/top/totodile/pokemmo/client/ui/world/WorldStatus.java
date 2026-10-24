package top.totodile.pokemmo.client.ui.world;

import de.matthiasmann.twl.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.LongSupplier;
import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.world.WorldScene;

/** TrainerHudWindow: location, money and virtual weekday/time, with the original atlas icons. */
final class WorldStatus extends DialogLayout {
    private final WorldScene world;
    private final ClientStrings strings;
    private final LongSupplier clock;
    private final DecimalFormat moneyFormat;
    private long lastSecond=Long.MIN_VALUE,lastOrigin=Long.MIN_VALUE;
    private final Label location=new Label(), money=new Label("$0"), time=new Label();
    private final Label placeIcon=new Label(), moneyIcon=new Label(), timeIcon=new Label();
    WorldStatus(WorldScene world,ClientStrings strings,LongSupplier clock) {
        this.world=world;this.strings=strings;this.clock=clock;
        moneyFormat=new DecimalFormat("$###,###.###",DecimalFormatSymbols.getInstance());
        setTheme("/world-status");
        setAddDefaultGaps(false);
        placeIcon.setTheme("location");moneyIcon.setTheme("money");timeIcon.setTheme("day");
        timeIcon.setTooltipDelay(50); // TrainerHudWindow.Jg0
        setHorizontalGroup(createSequentialGroup()
                .addGroup(createParallelGroup(placeIcon,moneyIcon,timeIcon))
                .addGap(DEFAULT_GAP)
                .addGroup(createParallelGroup(location,money,time)));
        setVerticalGroup(createSequentialGroup()
                .addGroup(createParallelGroup(placeIcon,location))
                .addGap(DEFAULT_GAP)
                .addGroup(createParallelGroup(moneyIcon,money))
                .addGap(DEFAULT_GAP)
                .addGroup(createParallelGroup(timeIcon,time)));
        update();
    }
    void update() {
        String romName=world.locationName();
        var info=world.worldInfo();
        String name=strings.getOrDefault(world.locationStringId(),romName==null?"--":romName);
        location.setText(name+(info.channel()<0?"":" Ch. "+(info.channel()+1)));
        money.setText(moneyFormat.format(info.money()));
        // WorldTimeManager.ki0: four virtual seconds per real second, relative to the service's clock origin.
        long millis=clock.getAsLong();
        long second=Math.floorDiv(millis,1000);
        if(second==lastSecond&&info.timeOrigin()==lastOrigin)return;
        lastSecond=second;lastOrigin=info.timeOrigin();
        long seconds=(second-info.timeOrigin())*4;
        int minute=(int)Math.floorMod(Math.floorDiv(seconds,60),1440);
        int hour=minute/60;
        int weekday=(int)Math.floorMod(Math.floorDiv(seconds,86400),7);
        Locale locale=Locale.forLanguageTag(strings.language());
        String day=DayOfWeek.of(weekday==0?7:weekday).getDisplayName(TextStyle.FULL,locale);
        // The current official HUD also appends WorldTimeManager.YG's UTC month season.
        int season=(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).getMonthValue()-1)%4;
        time.setText(strings.format(1155,day,String.format(Locale.ROOT,"%02d:%02d",hour,minute%60))+", "+strings.get(6496+season));
        String theme=hour<4||hour>=21?"night":hour<11?"morning":"day";
        if(!theme.equals(timeIcon.getTheme())){timeIcon.setTheme(theme);if(getGUI()!=null)timeIcon.reapplyTheme();}
        timeIcon.setTooltipContent(strings.get(hour<4||hour>=21?1158:hour<11?1148:hour<18?1157:1149));
    }
    void position(int x,int y) { setPosition(x,y);setSize(getPreferredWidth(),getPreferredHeight()); }
}
