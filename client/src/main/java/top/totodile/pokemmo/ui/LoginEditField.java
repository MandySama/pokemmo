package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.Menu;
import de.matthiasmann.twl.MenuManager;
import de.matthiasmann.twl.Widget;
import org.lwjgl.glfw.GLFW;

/** 通过 GLFW 访问剪贴板，密码复制时保持掩码。 */
public class LoginEditField extends EditField {
    @Override
    protected void showPopupMenu(Event event) {
        // 菜单以上方的输入框中心为锚点，不跟随右键点击坐标；靠近屏幕边缘时限制位置。
        Menu menu = createPopupMenu();
        MenuManager manager = menu.openPopupMenu(this);
        Widget popup = manager.getPopupForMenu(menu);
        if (popup != null) {
            int x = getX() + (getWidth() - popup.getWidth()) / 2;
            int y = getY() - popup.getHeight();
            popup.setPosition(Math.max(0, Math.min(x, manager.getInnerRight() - popup.getWidth())),
                    Math.max(0, y));
        }
    }

    @Override
    public void copyToClipboard() {
        String text = hasSelection() ? getSelectedText() : getText();
        if (isPasswordMasking()) {
            text = String.valueOf(getPasswordChar()).repeat(text.length());
        }
        GLFW.glfwSetClipboardString(0, text);
    }

    @Override
    public void cutToClipboard() {
        if (!hasSelection()) {
            selectAll();
        }
        copyToClipboard();
        // 由基类删除选区并更新显示、触发编辑回调。
        super.insertText("");
    }

    @Override
    public void pasteFromClipboard() {
        String text = GLFW.glfwGetClipboardString(0);
        if (text != null) {
            insertText(text.replace("\uFEFF", "").replace("\n", ""));
        }
    }
}
