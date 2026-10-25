package top.totodile.pokemmo.client.ui.common;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.client.resources.ClientStrings;

import java.util.function.Consumer;

/** 参考 ActionConfirmDialogWindow：居中文案，下方纵向排列“是 / 否”。 */
public final class ActionConfirmDialog extends PopupWindow {
    private final DialogLayout panel = new DialogLayout();
    private final Label message = new Label();
    private final Button yes, no;
    private final boolean mobile;
    private final Consumer<Image> background;
    private final Runnable onClose;
    private Runnable confirmed;

    public ActionConfirmDialog(
            Widget owner,
            ClientStrings strings,
            boolean mobile,
            Consumer<Image> background,
            Runnable onClose) {
        super(owner);
        this.mobile = mobile;
        this.background = background;
        this.onClose = onClose;
        setTheme("/confirm-widget");
        setCloseOnClickedOutside(false);
        panel.setTheme("/confirm-panel");
        panel.setAddDefaultGaps(false);
        yes = new Button(strings.get(50));
        no = new Button(strings.get(51));
        yes.addCallback(
                () -> {
                    Runnable action = confirmed;
                    closePopup();
                    if (action != null) action.run();
                });
        no.addCallback(this::closePopup);
        Widget spacer = new Widget();
        spacer.setTheme("container");
        panel.setHorizontalGroup(panel.createParallelGroup(message, spacer, yes, no));
        panel.setVerticalGroup(
                panel.createSequentialGroup()
                        .addWidget(message)
                        .addGap(DialogLayout.DEFAULT_GAP)
                        .addWidget(spacer)
                        .addGap(DialogLayout.DEFAULT_GAP)
                        .addWidget(yes)
                        .addGap(DialogLayout.DEFAULT_GAP)
                        .addWidget(no)
                        .addGap(DialogLayout.DEFAULT_GAP));
        add(panel);
    }

    public void show(String text, Runnable confirmed) {
        this.confirmed = confirmed;
        message.setText(text);
        if (!isOpen() && !openPopup()) {
            this.confirmed = null;
            return;
        }
        positionWindow();
        invalidateLayout();
        yes.requestKeyboardFocus();
    }

    public void positionWindow() {
        if (!isOpen()) return;
        Widget owner = getOwner();
        setPosition(owner.getInnerX(), owner.getInnerY());
        setSize(owner.getInnerWidth(), owner.getInnerHeight());
    }

    @Override
    protected void layout() {
        panel.adjustSize();
        panel.setPosition(
                getInnerX() + (getInnerWidth() - panel.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - panel.getHeight()) / 2);
    }

    @Override
    protected void paintBackground(GUI gui) {
        if (mobile) background.accept(getBackground());
        else super.paintBackground(gui);
    }

    @Override
    public void closePopup() {
        boolean wasOpen = isOpen();
        confirmed = null;
        super.closePopup();
        if (wasOpen) onClose.run();
    }

    @Override
    protected boolean handleEventPopup(Event event) {
        if (event.isKeyPressedEvent() && event.getKeyCode() == Event.KEY_ESCAPE) {
            closePopup();
            return true;
        }
        return super.handleEventPopup(event);
    }
}
