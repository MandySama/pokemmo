package top.totodile.pokemmo.client.ui.menu;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Widget;

import top.totodile.pokemmo.client.resources.ClientStrings;

import java.util.ArrayList;
import java.util.List;

/** PC 端右下角菜单，按钮顺序及固定尺寸与参考客户端相同。 */
public final class DesktopMenu extends Widget {
    private final List<Button> buttons = new ArrayList<>();
    private static final int[] TEXT_IDS = {1172, 1175, 1190, 1005, 1006, 1007, 1124, 1008, 1009};

    public DesktopMenu(ClientStrings strings) {
        // 菜单按钮直接继承 logingui 下的标准 button 主题。
        setTheme("");
        // 点击时把焦点移到无背景的容器，取消原控件焦点；按钮本身不获得焦点。
        setCanAcceptKeyboardFocus(true);
        for (int id : TEXT_IDS) {
            Button button = new Button(strings.get(id));
            button.setCanAcceptKeyboardFocus(false);
            buttons.add(button);
            add(button);
        }
    }

    public void onClientManagement(Runnable action) {
        buttons.get(1).addCallback(action);
    }

    public void onMods(Runnable action) {
        buttons.get(2).addCallback(action);
    }

    public void onExit(Runnable action) {
        buttons.get(8).addCallback(action);
    }

    public void onSettings(Runnable action) {
        buttons.get(7).addCallback(action);
    }

    @Override
    public int getPreferredInnerWidth() {
        return 200;
    }

    @Override
    public int getPreferredInnerHeight() {
        return buttons.size() * 26;
    }

    @Override
    protected void layout() {
        for (int index = 0; index < buttons.size(); index++) {
            Button button = buttons.get(index);
            button.setPosition(getInnerX(), getInnerY() + index * 26);
            button.setSize(200, 26);
        }
    }
}
