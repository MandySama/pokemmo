package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientStrings;
import java.util.ArrayList;
import java.util.List;

/** 管理工具列表；仅已接通的入口绑定回调。 */
final class GmMenuWindow extends ResizableFrame {
    private final Button unlockDex = new Button(ClientStrings.get(9310));
    private boolean dexUnlocked;
    private java.util.function.Consumer<Boolean> dexChanged = value -> { };

    void setDexChanged(java.util.function.Consumer<Boolean> callback) { dexChanged = callback; }
    void setDexBusy(boolean busy) { unlockDex.setEnabled(!busy); }
    void setDexUnlocked(boolean unlocked) {
        dexUnlocked = unlocked;
        unlockDex.setText(ClientStrings.get(unlocked ? 9311 : 9310));
        invalidateLayout();
    }

    private final Button speed = new Button(ClientStrings.get(9272).replace("{00}", "1"));
    private int speedMultiplier = 1;
    private java.util.function.IntConsumer speedChanged = value -> { };

    void setSpeedChanged(java.util.function.IntConsumer callback) { speedChanged = callback; }

    void setSpeedMultiplier(int multiplier) {
        speedMultiplier = multiplier;
        speed.setText(ClientStrings.get(9272).replace("{00}", Integer.toString(multiplier)));
        invalidateLayout();
    }

    private final Button noclip = new Button(ClientStrings.get(9270));
    private boolean noclipEnabled;
    private java.util.function.Consumer<Boolean> noclipChanged = value -> { };

    void setNoclipChanged(java.util.function.Consumer<Boolean> callback) { noclipChanged = callback; }

    void setNoclip(boolean enabled) {
        noclipEnabled = enabled;
        noclip.setText(ClientStrings.get(enabled ? 9271 : 9270));
        invalidateLayout();
    }

    private Button teleport;

    void setTeleportEnabled(boolean enabled) { teleport.setEnabled(enabled); }

    private Runnable openTeleport = () -> { };

    void setOpenTeleport(Runnable callback) { openTeleport = callback; }

    private Button computer;

    /** 随身电脑入口只在世界会话内可用。 */
    void setComputerEnabled(boolean enabled) { computer.setEnabled(enabled); }

    private Runnable openComputer = () -> { };

    void setOpenComputer(Runnable callback) { openComputer = callback; }

    private final ToggleButton tab = new ToggleButton(ClientStrings.get(9201));
    private final Widget panel = new Widget();
    private final List<Widget[]> rows = new ArrayList<>();

    GmMenuWindow(Runnable close) {
        setTheme("/gm-menu");
        setTitle(ClientStrings.get(9200));
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        panel.setTheme("content");
        add(panel);
        tab.setTheme("tab");
        tab.setActive(true);
        tab.addCallback(() -> tab.setActive(true));
        add(tab);
        heading(9202);
        commands(9206, 9207, 9208, 9209);
        heading(9203);
        commands(9210, 9211, 9212, 9213, 9214, 9215);
        heading(9204);
        commands(9216, 9217, 9218, 9219, 9220, 9221);
        commands(9222, 9223, 9224, 9225, 9226, 9227, 9228);
        commands(9229, 9230, 9231, 9232, 9233, 9234);
        heading(9205);
        commands(9235, 9236, 9237, 9238, 9239, 9240);
        commands(9241, 9242, 9243, 9244, 9245, 9246, 9247);
        commands(9248);
        noclip.addCallback(() -> {
            setNoclip(!noclipEnabled);
            noclipChanged.accept(noclipEnabled);
        });
        panel.add(noclip);
        Widget[] last = rows.removeLast();
        speed.addCallback(() -> {
            setSpeedMultiplier(speedMultiplier == 8 ? 1 : speedMultiplier * 2);
            speedChanged.accept(speedMultiplier);
        });
        panel.add(speed);
        unlockDex.addCallback(() -> dexChanged.accept(!dexUnlocked));
        panel.add(unlockDex);
        rows.add(new Widget[]{last[0], noclip, speed, unlockDex});
    }

    private void heading(int id) {
        Label label = new Label(ClientStrings.get(id));
        label.setTheme("heading");
        panel.add(label);
        rows.add(new Widget[]{label});
    }

    private void commands(int... ids) {
        Widget[] row = new Widget[ids.length];
        for (int i = 0; i < ids.length; i++) {
            Button button = new Button(ClientStrings.get(ids[i]));
            if (ids[i] == 9235) { teleport = button; button.addCallback(() -> openTeleport.run()); }
            if (ids[i] == 9221) { computer = button; button.addCallback(() -> openComputer.run()); }
            row[i] = button;
            panel.add(row[i]);
        }
        rows.add(row);
    }

    @Override
    protected void layout() {
        super.layout();
        tab.setSize(tab.getPreferredWidth() + 40, 26);
        tab.setPosition(getX() + 15, getY() + 30);
        panel.setPosition(getX() + 5, getY() + 30);
        panel.setSize(getWidth() - 10, getHeight() - 37);
        int y = panel.getY() + 42;
        for (Widget[] row : rows) {
            boolean heading = row[0] instanceof Label;
            int width = 0;
            for (Widget widget : row) width += widget.getPreferredWidth();
            width += (row.length - 1) * 7;
            int x = panel.getX() + (panel.getWidth() - width) / 2;
            for (Widget widget : row) {
                widget.setSize(widget.getPreferredWidth(), heading ? 20 : 26);
                widget.setPosition(x, y);
                x += widget.getWidth() + 7;
            }
            y += heading ? 30 : 38;
            if (!heading && (row == rows.get(1) || row == rows.get(3) || row == rows.get(7))) y += 4;
        }
    }
}
