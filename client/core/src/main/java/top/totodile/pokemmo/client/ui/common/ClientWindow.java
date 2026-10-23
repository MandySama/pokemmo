package top.totodile.pokemmo.client.ui.common;

import de.matthiasmann.twl.*;

/** 常规客户端窗口；下拉列表仍由 PopupWindow 单独处理。 */
public abstract class ClientWindow extends Container {
    private final Widget owner;

    protected ClientWindow(Widget owner) {
        this.owner = owner;
        setTheme("");
        setCanAcceptKeyboardFocus(true);
    }

    protected final Widget getOwner() { return owner; }
    public final boolean isOpen() { return getParent() != null; }

    protected final boolean openPopup() {
        if (owner.getGUI() == null) return false;
        owner.add(this);
        requestKeyboardFocus();
        focusFirstChild();
        return true;
    }

    public final void raiseWindow() {
        if (!isOpen()) return;
        if (owner.getChildIndex(this) != owner.getNumChildren() - 1) {
            owner.removeChild(this);
            owner.add(this);
        }
        requestKeyboardFocus();
    }

    public final boolean isAbove(ClientWindow other) {
        return isOpen() && (!other.isOpen() || owner.getChildIndex(this) > owner.getChildIndex(other));
    }

    public void closePopup() {
        if (isOpen()) owner.removeChild(this);
    }

    @Override public int getPreferredWidth() {
        return Math.min(owner.getInnerWidth(), super.getPreferredWidth());
    }

    @Override public int getPreferredHeight() {
        return Math.min(owner.getInnerHeight(), super.getPreferredHeight());
    }

    @Override protected boolean isMouseInside(Event event) {
        // 参考客户端按各个窗口的边界命中；承载多个窗口的全屏容器不能挡住外部控件。
        for (int i = 0; i < getNumChildren(); i++) {
            Widget child = getChild(i);
            if (child.isVisible() && child.isInside(event.getMouseX(), event.getMouseY())) return true;
        }
        return false;
    }

    @Override protected boolean handleEvent(Event event) {
        if (super.handleEvent(event)) return true;
        if (event.isKeyPressedEvent() && event.getKeyCode() == Event.KEY_ESCAPE) {
            closePopup();
            return true;
        }
        return event.isMouseEvent();
    }
}
