package top.totodile.pokemmo.client;

import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.Button;
import java.util.function.IntSupplier;

/** 移动端右上角的电量和菜单入口。 */
final class MobileStatusIcons extends Widget {
    private final Widget battery = new Widget();
    private final Button menu = new Button();
    private final IntSupplier batteryPercent;
    private long nextCheck;
    private int displayedBucket = -1;

    MobileStatusIcons(IntSupplier batteryPercent) {
        this.batteryPercent = batteryPercent;
        setTheme("");
        battery.setTheme("/mobile-battery");
        battery.setVisible(false);
        menu.setTheme("/mobile-menu");
        menu.setCanAcceptKeyboardFocus(false);
        add(battery);
        add(menu);
    }

    void onMenu(Runnable action) { menu.addCallback(action); }

    void update(long now) {
        if (now < nextCheck) return;
        nextCheck = now + 500_000_000L;
        int percent = batteryPercent.getAsInt();
        if (percent < 0 || percent > 100) {
            battery.setVisible(false);
            return;
        }
        int bucket = Math.max(10, Math.round(percent / 10f) * 10);
        if (bucket != displayedBucket) {
            displayedBucket = bucket;
            battery.setTheme("/mobile-battery-" + bucket);
            battery.reapplyTheme();
        }
        battery.setVisible(true);
    }

    @Override public int getPreferredInnerWidth() { return 120; }
    @Override public int getPreferredInnerHeight() { return 60; }

    @Override protected void layout() {
        battery.setPosition(getInnerX(), getInnerY());
        battery.setSize(60, 60);
        menu.setPosition(getInnerX() + 60, getInnerY());
        menu.setSize(60, 60);
    }
}
