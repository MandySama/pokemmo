package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientStrings;
import java.text.NumberFormat;
import java.util.Locale;

/** 世界信息只展示当前地图、服务端余额和同步的游戏时钟，不接收地图输入。 */
final class WorldInfoPanel extends Widget {
    private final Label location = new Label();
    private final Label money = new Label();
    private final Label time = new Label();
    private final Widget[] icons = new Widget[3];
    private final Label[] labels = {location, money, time};

    WorldInfoPanel() {
        setTheme("/world-info");
        setCanAcceptKeyboardFocus(false);
        String[] themes = {"location", "money", "time"};
        for (int i = 0; i < labels.length; i++) {
            icons[i] = new Widget(); icons[i].setTheme(themes[i]); add(icons[i]);
            labels[i].setTheme("label"); labels[i].setCanAcceptKeyboardFocus(false); add(labels[i]);
        }
        setMoney(0);
        update(ClientStrings.get(9282), -1);
        setVisible(false);
    }

    void setMoney(long value) {
        String text = "$" + NumberFormat.getIntegerInstance(Locale.ROOT).format(value);
        if (!money.getText().equals(text)) { money.setText(text); invalidateLayout(); }
    }

    void update(String name, long seconds) {
        String clock = seconds < 0 ? ClientStrings.get(9282) : String.format(Locale.ROOT, "%02d:%02d",
                Math.floorMod(seconds, 86400) / 3600, Math.floorMod(seconds, 3600) / 60);
        if (!location.getText().equals(name) || !time.getText().equals(clock)) {
            location.setText(name);
            time.setText(clock);
            invalidateLayout();
        }
    }

    @Override
    protected void layout() {
        for (int i = 0; i < labels.length; i++) {
            icons[i].setPosition(getInnerX(), getInnerY() + i * 23);
            icons[i].setSize(18, 18);
            labels[i].setPosition(getInnerX() + 23, getInnerY() + i * 23);
            labels[i].setSize(Math.max(0, getInnerWidth() - 23), 18);
        }
    }

    @Override
    public int getPreferredInnerWidth() {
        int width = 0;
        for (Label label : labels) width = Math.max(width, label.getPreferredWidth());
        return width + 23;
    }

    @Override
    public int getPreferredInnerHeight() { return 64; }
}
