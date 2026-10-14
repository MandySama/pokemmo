package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.ClientStrings;

/** 主菜单锚定快捷按钮上方，点击外部或 Esc 关闭，不穿透到地图操作。 */
final class GameMenuPopup extends PopupWindow {
    private final Button[] buttons = new Button[8];
    private int menuWidth = 94, rowHeight = 31;

    GameMenuPopup(Widget owner, Runnable settings, Runnable logout, Runnable quit) {
        super(owner);
        setTheme("/game-menu-popup");
        setCloseAndIgnoreMouseDownOutside(true);
        int[] names = {1117, 1111, 1112, 2807, 7900, 1124, 1159, 1114};
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Button(ClientStrings.get(names[i]));
            buttons[i].setTheme("button");
            add(buttons[i]);
        }
        buttons[1].addCallback(() -> { closePopup(); settings.run(); });
        buttons[6].addCallback(() -> { closePopup(); logout.run(); });
        buttons[7].addCallback(() -> { closePopup(); quit.run(); });
    }

    @Override
    protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        menuWidth = theme.getParameter("menuWidth", 94);
        rowHeight = theme.getParameter("rowHeight", 31);
    }

    void showMenu() {
        if (isOpen()) { closePopup(); return; }
        if (openPopup()) {
            requestKeyboardFocus(null);
            setSize(menuWidth, rowHeight * buttons.length + getBorderVertical());
            positionAboveOwner();
        }
    }

    private void positionAboveOwner() {
        if (getGUI() == null) return;
        setPosition(Math.max(0, Math.min(getOwner().getRight() - getWidth(), getGUI().getWidth() - getWidth())),
                Math.max(0, getOwner().getY() - getHeight()));
    }

    @Override
    protected void layout() {
        positionAboveOwner();
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setPosition(getInnerX(), getInnerY() + i * rowHeight);
            buttons[i].setSize(getInnerWidth(), rowHeight);
        }
    }
}
