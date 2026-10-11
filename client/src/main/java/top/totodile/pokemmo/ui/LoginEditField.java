package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.EditField;
import org.lwjgl.glfw.GLFW;

/** 与参考输入框一致，通过 GLFW 访问剪贴板，密码复制时保持掩码。 */
public class LoginEditField extends EditField {
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
