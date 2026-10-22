package top.totodile.pokemmo.client.ui.common;

import de.matthiasmann.twl.*;
import java.util.function.Consumer;
import top.totodile.pokemmo.client.resources.ClientStrings;

/** 移动端退出确认，沿用 confirm-widget / confirm-panel 的资源层级。 */
public final class ExitConfirmation extends PopupWindow {
    private final DialogLayout panel = new DialogLayout();
    private final Button yes;
    private final Button no;
    private final Consumer<de.matthiasmann.twl.renderer.Image> screenBackground;
    private final Runnable onCancel;

    public ExitConfirmation(Widget owner, ClientStrings strings, Runnable exit, Runnable onCancel,
                     Consumer<de.matthiasmann.twl.renderer.Image> screenBackground) {
        super(owner);
        this.screenBackground = screenBackground;
        this.onCancel = onCancel;
        setTheme("/confirm-widget");
        setCloseOnClickedOutside(false);
        panel.setTheme("/confirm-panel");
        panel.setAddDefaultGaps(false);
        Label message = new Label(strings.get(1115));
        yes = new Button(strings.get(50));
        no = new Button(strings.get(51));
        yes.addCallback(() -> { super.closePopup(); exit.run(); });
        no.addCallback(this::closePopup);
        Container spacer = new Container();
        spacer.setTheme("container");
        panel.setHorizontalGroup(panel.createParallelGroup()
                .addWidget(message).addWidget(spacer).addGroup(panel.createParallelGroup(yes, no)));
        panel.setVerticalGroup(panel.createSequentialGroup().addWidget(message)
                .addGap(DialogLayout.DEFAULT_GAP).addWidget(spacer).addGap(DialogLayout.DEFAULT_GAP)
                .addGroup(panel.createSequentialGroup().addWidget(yes)
                        .addGap(DialogLayout.DEFAULT_GAP).addWidget(no)).addGap());
        add(panel);
    }

    public void open() {
        if (isOpen()) return;
        if (openPopup()) {
            positionWindow();
            yes.requestKeyboardFocus();
        }
    }

    public void positionWindow() {
        if (!isOpen()) return;
        Widget owner = getOwner();
        setPosition(owner.getInnerX(), owner.getInnerY());
        setSize(owner.getInnerWidth(), owner.getInnerHeight());
    }

    @Override protected void layout() {
        panel.adjustSize();
        panel.setPosition(getInnerX() + (getInnerWidth() - panel.getWidth()) / 2,
                getInnerY() + (getInnerHeight() - panel.getHeight()) / 2);
    }

    @Override protected void paintBackground(GUI gui) { screenBackground.accept(getBackground()); }

    @Override public void closePopup() {
        boolean wasOpen = isOpen();
        super.closePopup();
        if (wasOpen) onCancel.run();
    }
}
