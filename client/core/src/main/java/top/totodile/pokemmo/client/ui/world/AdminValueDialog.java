package top.totodile.pokemmo.client.ui.world;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.ClientDialog;
import top.totodile.pokemmo.client.ui.common.PlayerTextInput;

import java.util.function.IntConsumer;

/**
 * InputConfirmDialogWindow used by AdminDatabaseSearchWindow.aB / Qd0: 3 characters, default 100.
 */
final class AdminValueDialog extends ClientDialog {
    private final DialogLayout panel = new DialogLayout();
    private final Label message = new Label();
    private final PlayerTextInput keyboard;
    private final EditField input;
    private final Button okay, cancel;
    private final Runnable onClose;
    private IntConsumer accept;

    AdminValueDialog(Widget owner, ClientStrings strings, boolean mobile, Runnable onClose) {
        super(owner, mobile);
        this.onClose = onClose;
        setTheme("/confirm-widget");
        setCloseOnClickedOutside(false);
        panel.setTheme("/admin-value-panel");
        keyboard = new PlayerTextInput(mobile, this::isOpen);
        input = keyboard.field(3, false);
        okay = new Button(strings.get(52));
        cancel = new Button(strings.get(53));
        okay.addCallback(this::submit);
        cancel.addCallback(this::closePopup);
        panel.setHorizontalGroup(panel.createParallelGroup(message, input, okay, cancel));
        panel.setVerticalGroup(panel.createSequentialGroup(message, input, okay, cancel));
        add(panel);
    }

    void show(String prompt, IntConsumer accept) {
        this.accept = accept;
        message.setText(prompt);
        input.setText("100");
        if (openPopup()) {
            positionWindow();
            input.requestKeyboardFocus();
            input.setSelection(0, 3);
        }
    }

    private void submit() {
        String value = input.getText().strip();
        try {
            int number = Integer.parseInt(value);
            closePopup();
            accept.accept(number);
        } catch (NumberFormatException ignored) {
            input.requestKeyboardFocus();
        }
    }

    void positionWindow() {
        if (!isOpen()) return;
        var owner = getOwner();
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
    public void closePopup() {
        boolean open = isOpen();
        super.closePopup();
        keyboard.close();
        if (open) onClose.run();
    }

    @Override
    protected boolean handleEventPopup(Event event) {
        if (event.isKeyPressedEvent() && event.getKeyCode() == Event.KEY_ESCAPE) {
            closePopup();
            return true;
        }
        if (event.isKeyPressedEvent() && event.getKeyCode() == Event.KEY_RETURN) {
            submit();
            return true;
        }
        return super.handleEventPopup(event);
    }
}
