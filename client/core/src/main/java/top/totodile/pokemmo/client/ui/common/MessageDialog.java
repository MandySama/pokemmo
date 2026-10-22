package top.totodile.pokemmo.client.ui.common;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import java.util.function.Consumer;
import top.totodile.pokemmo.client.resources.ClientStrings;

/** 对齐参考 SimpleConfirmDialogWindow：居中文案、单个“好的”按钮和模态遮罩。 */
public final class MessageDialog extends PopupWindow {
    private final DialogLayout panel=new DialogLayout();
    private final Label message=new Label();
    private final Button okay;
    private final boolean mobile;
    private final Consumer<Image> screenBackground;
    private final Runnable onClose;
    private Runnable afterClose;

    public MessageDialog(Widget owner, ClientStrings strings, boolean mobile,
                         Consumer<Image> screenBackground, Runnable onClose) {
        super(owner);
        this.mobile=mobile;
        this.screenBackground=screenBackground;
        this.onClose=onClose;
        setTheme("/confirm-widget");
        setCloseOnClickedOutside(false);
        panel.setTheme("/confirm-panel");
        okay=new Button(strings.get(52));
        okay.addCallback(this::closePopup);
        panel.setHorizontalGroup(panel.createParallelGroup(message,okay));
        panel.setVerticalGroup(panel.createSequentialGroup(message,okay));
        add(panel);
    }

    public void show(String text) {
        show(text,null);
    }

    public void show(String text,Runnable afterClose) {
        this.afterClose=afterClose;
        message.setText(text);
        if (!isOpen() && !openPopup()) return;
        positionWindow();
        invalidateLayout();
        okay.requestKeyboardFocus();
    }

    public void positionWindow() {
        if (!isOpen()) return;
        Widget owner=getOwner();
        setPosition(owner.getInnerX(),owner.getInnerY());
        setSize(owner.getInnerWidth(),owner.getInnerHeight());
    }

    @Override protected void layout() {
        panel.adjustSize();
        panel.setPosition(getInnerX()+(getInnerWidth()-panel.getWidth())/2,
                getInnerY()+(getInnerHeight()-panel.getHeight())/2);
    }

    @Override protected void paintBackground(GUI gui) {
        if (mobile) screenBackground.accept(getBackground()); else super.paintBackground(gui);
    }

    @Override public void closePopup() {
        boolean wasOpen=isOpen();
        super.closePopup();
        if (wasOpen) {
            Runnable action=afterClose;
            afterClose=null;
            onClose.run();
            if(action!=null)action.run();
        }
    }

    @Override protected boolean handleEventPopup(Event event) {
        if (event.isKeyPressedEvent() && (event.getKeyCode()==Event.KEY_ESCAPE || event.getKeyCode()==Event.KEY_RETURN)) {
            closePopup(); return true;
        }
        return super.handleEventPopup(event);
    }
}
