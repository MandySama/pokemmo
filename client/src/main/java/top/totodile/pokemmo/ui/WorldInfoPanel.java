package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import top.totodile.pokemmo.ClientStrings;
import java.text.NumberFormat;
import java.util.Locale;

/** 世界信息展示地图与频道、服务端余额和星期/时钟/季节，不接收地图输入。 */
final class WorldInfoPanel extends Widget {
    private final Label location = new Label();
    private final Label money = new Label();
    private final Label time = new Label();
    private final PeriodIcon periodIcon = new PeriodIcon();
    private final Widget[] icons = {new Widget(), new Widget(), periodIcon};
    private final Label[] labels = {location, money, time};
    private Image morningIcon, dayIcon, nightIcon;

    /** 原版时段按游戏时钟划分：清晨 04:00-10:59、白天 11:00-20:59、夜晚 21:00-03:59。 */
    private static final int MORNING_MINUTE = 4 * 60, DAY_MINUTE = 11 * 60, NIGHT_MINUTE = 21 * 60;
    /** 星期文字 9700 星期一…9706 星期日；季节文字 9707 春、9708 夏、9709 秋、9710 冬。 */
    private static final int WEEKDAY_ID = 9700, SEASON_ID = 9707;

    WorldInfoPanel() {
        setTheme("/world-info");
        setCanAcceptKeyboardFocus(false);
        String[] themes = {"location", "money", "time-period"};
        for (int i = 0; i < labels.length; i++) {
            icons[i].setTheme(themes[i]); add(icons[i]);
            labels[i].setTheme("label"); labels[i].setCanAcceptKeyboardFocus(false); add(labels[i]);
        }
        setMoney(0);
        update(ClientStrings.get(9282), 1, -1);
        setVisible(false);
    }

    @Override
    protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        var period = theme.getChildTheme("time-period");
        morningIcon = period.getImage("morning");
        dayIcon = period.getImage("day");
        nightIcon = period.getImage("night");
    }

    void setMoney(long value) {
        String text = "$" + NumberFormat.getIntegerInstance(Locale.ROOT).format(value);
        if (!money.getText().equals(text)) { money.setText(text); invalidateLayout(); }
    }

    void update(String name, int channel, long seconds) {
        String locationText = name;
        String timeText;
        if (seconds < 0) {
            timeText = ClientStrings.get(9282);
        } else {
            locationText = name + " Ch. " + channel;
            long daySeconds = Math.floorMod(seconds, 86400);
            int minuteOfDay = (int) (daySeconds / 60);
            // 世界时钟自纪元 4 倍速推进，第 0 天按星期一开始计星期，因此星期随游戏日期每 6 现实小时轮转。
            int weekday = (int) (Math.floorDiv(seconds, 86400) % 7);
            timeText = ClientStrings.get(WEEKDAY_ID + weekday) + ", "
                    + String.format(Locale.ROOT, "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60) + ", "
                    + ClientStrings.get(SEASON_ID + seasonIndex());
            periodIcon.setPeriod(minuteOfDay < MORNING_MINUTE || minuteOfDay >= NIGHT_MINUTE ? 2
                    : minuteOfDay < DAY_MINUTE ? 0 : 1);
        }
        if (!location.getText().equals(locationText) || !time.getText().equals(timeText)) {
            location.setText(locationText);
            time.setText(timeText);
            invalidateLayout();
        }
    }

    /** 原版季节按现实月份轮换：1 月为春季，此后春→夏→秋→冬逐月循环，每月 1 日 UTC 0 点切换。 */
    private static int seasonIndex() {
        return (java.time.LocalDate.now(java.time.ZoneOffset.UTC).getMonthValue() - 1) % 4;
    }

    /** 时段图标按世界时钟在清晨日出、白天太阳和夜晚月亮之间切换。 */
    private final class PeriodIcon extends Widget {
        private int period = -1;

        void setPeriod(int value) {
            // TWL 每帧重绘控件树，时段变化只需更新状态。
            period = value;
        }

        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            Image image = period == 0 ? morningIcon : period == 1 ? dayIcon : period == 2 ? nightIcon : null;
            if (image != null) image.draw(getAnimationState(), getX(), getY());
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
