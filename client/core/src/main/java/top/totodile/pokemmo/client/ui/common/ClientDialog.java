package top.totodile.pokemmo.client.ui.common;

import de.matthiasmann.twl.*;

/** PC dialogs use the regular window layer; Android retains its existing modal popup host. */
public abstract class ClientDialog extends Container {
    private final Widget owner;
    private final boolean mobile;
    private final PopupWindow mobileHost;
    private boolean closeOnEscape = true;

    protected ClientDialog(Widget owner, boolean mobile) {
        this.owner = owner;
        this.mobile = mobile;
        mobileHost = mobile ? new PopupWindow(owner) : null;
        if (mobileHost != null) {
            // A transparent host must not stop absolute child themes at an absent "popupwindow" theme.
            mobileHost.setTheme("");
            mobileHost.setCloseOnClickedOutside(false);
            mobileHost.setCloseOnEscape(false);
            mobileHost.add(this);
        }
        setCanAcceptKeyboardFocus(true);
    }

    public final Widget getOwner() {
        return owner;
    }

    public final boolean isOpen() {
        return mobile ? mobileHost.isOpen() : getParent() != null;
    }

    public final void setCloseOnClickedOutside(boolean close) {
        if (mobileHost != null) mobileHost.setCloseOnClickedOutside(close);
    }

    public final void setCloseOnEscape(boolean close) {
        closeOnEscape = close;
    }

    public boolean openPopup() {
        if (owner.getGUI() == null) return false;
        if (mobile) {
            mobileHost.setPosition(owner.getInnerX(), owner.getInnerY());
            mobileHost.setSize(owner.getInnerWidth(), owner.getInnerHeight());
            if (!mobileHost.openPopup()) return false;
        } else if (!isOpen()) owner.getGUI().getRootPane().add(this);
        setVisible(true);
        requestKeyboardFocus();
        focusFirstChild();
        return true;
    }

    public final void raiseWindow() {
        if (!isOpen() || mobile) return;
        var parent = getParent();
        if (parent.getChildIndex(this) != parent.getNumChildren() - 1) {
            parent.removeChild(this);
            parent.add(this);
        }
        requestKeyboardFocus();
    }

    public void closePopup() {
        if (mobile) mobileHost.closePopup();
        else if (getParent() != null) getParent().removeChild(this);
    }

    @Override
    public boolean isInside(int x, int y) {
        if (mobile) return super.isInside(x, y);
        for (int i = 0; i < getNumChildren(); i++) {
            var child = getChild(i);
            if (child.isVisible() && child.isInside(x, y)) return true;
        }
        return false;
    }

    @Override
    protected void paintBackground(GUI gui) {
        if (mobile) super.paintBackground(gui);
    }

    protected boolean handleEventPopup(Event event) {
        return super.handleEvent(event);
    }

    @Override
    protected boolean handleEvent(Event event) {
        if (handleEventPopup(event)) return true;
        if (closeOnEscape && event.isKeyPressedEvent() && event.getKeyCode() == Event.KEY_ESCAPE) {
            closePopup();
            return true;
        }
        return event.isMouseEvent() && isInside(event.getMouseX(), event.getMouseY());
    }
}
