package top.totodile.pokemmo.client.ui.inventory;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.ui.common.*;
import top.totodile.pokemmo.client.ui.common.ClientDialog;
import top.totodile.pokemmo.server.game.PlayerData;

import java.util.function.BiConsumer;

/**
 * InventoryWindow.Ob / NUL: quantity (1..stack, default 1), then irreversible-action confirmation.
 */
final class ItemDiscardDialog {
    private final ClientStrings strings;
    private final InventoryItems items;
    private final Quantity quantity;
    private final ActionConfirmDialog confirmation;
    private final BiConsumer<PlayerData.Item, Integer> destroy;
    private PlayerData.Item selected;

    ItemDiscardDialog(
            Widget owner,
            ClientStrings strings,
            InventoryItems items,
            boolean mobile,
            BiConsumer<PlayerData.Item, Integer> destroy,
            Runnable onClose) {
        this.strings = strings;
        this.items = items;
        this.destroy = destroy;
        quantity = new Quantity(owner, strings, mobile, onClose);
        confirmation =
                new ActionConfirmDialog(
                        owner,
                        strings,
                        mobile,
                        image -> {
                            if (owner.getGUI().getRenderer()
                                    instanceof
                                    top.totodile.pokemmo.client.twl.GdxTwlRenderer renderer)
                                renderer.drawScreenBackground(image, owner.getAnimationState());
                        },
                        onClose);
    }

    void show(PlayerData.Item item) {
        if (isOpen() || item.amount() < 1 || !items.destroyable(item.itemId())) return;
        selected = item;
        if (item.amount() == 1) confirm(1);
        else
            quantity.show(
                    strings.format(1432, items.name(item.itemId())), item.amount(), this::confirm);
    }

    private void confirm(int amount) {
        var item = selected;
        confirmation.show(
                strings.format(1431, Integer.toString(amount), items.name(item.itemId())),
                () -> destroy.accept(item, amount));
    }

    boolean isOpen() {
        return quantity.isOpen() || confirmation.isOpen();
    }

    void close() {
        quantity.closePopup();
        confirmation.closePopup();
        selected = null;
    }

    void position() {
        quantity.positionWindow();
        confirmation.positionWindow();
    }

    private static final class Quantity extends ClientDialog {
        private final DialogLayout panel = new DialogLayout();
        private final Label message = new Label();
        private final EditField input;
        private final PlayerTextInput keyboard;
        private final Button decrease = new Button("-"),
                increase = new Button("+"),
                all,
                accept,
                cancel;
        private final Runnable onClose;
        private int maximum;
        private java.util.function.IntConsumer confirmed;

        Quantity(Widget owner, ClientStrings strings, boolean mobile, Runnable onClose) {
            super(owner, mobile);
            this.onClose = onClose;
            setTheme("/confirm-widget");
            setCloseOnClickedOutside(false);
            panel.setTheme("/confirm-panel");
            keyboard = new PlayerTextInput(mobile, this::isOpen);
            input = keyboard.field(5, false);
            input.setTheme("/valueadjuster.valueEdit");
            decrease.setTheme("/valueadjuster.decButton");
            increase.setTheme("/valueadjuster.incButton");
            all = new Button(strings.get(49));
            accept = new Button(strings.get(60));
            cancel = new Button(strings.get(53));
            decrease.addCallback(() -> change(-1));
            increase.addCallback(() -> change(1));
            all.addCallback(() -> input.setText(Integer.toString(maximum)));
            accept.addCallback(this::submit);
            cancel.addCallback(this::closePopup);
            panel.setHorizontalGroup(
                    panel.createParallelGroup()
                            .addWidget(message)
                            .addGroup(panel.createSequentialGroup(decrease, input, increase, all))
                            .addWidget(accept)
                            .addWidget(cancel));
            panel.setVerticalGroup(
                    panel.createSequentialGroup()
                            .addWidget(message)
                            .addGap(DialogLayout.DEFAULT_GAP)
                            .addGroup(panel.createParallelGroup(decrease, input, increase, all))
                            .addGap(DialogLayout.DEFAULT_GAP)
                            .addWidget(accept)
                            .addGap(DialogLayout.DEFAULT_GAP)
                            .addWidget(cancel));
            add(panel);
        }

        void show(String prompt, int max, java.util.function.IntConsumer action) {
            maximum = max;
            confirmed = action;
            message.setText(prompt);
            input.setText("1");
            if (openPopup()) {
                positionWindow();
                accept.requestKeyboardFocus();
            }
        }

        int value() {
            try {
                return Integer.parseInt(input.getText().strip());
            } catch (NumberFormatException invalid) {
                return 0;
            }
        }

        void change(int delta) {
            input.setText(Integer.toString(Math.max(1, Math.min(maximum, value() + delta))));
        }

        void submit() {
            int amount = value();
            if (amount < 1 || amount > maximum) {
                input.requestKeyboardFocus();
                return;
            }
            var action = confirmed;
            closePopup();
            if (action != null) action.accept(amount);
        }

        void positionWindow() {
            if (isOpen()) {
                var owner = getOwner();
                setPosition(owner.getInnerX(), owner.getInnerY());
                setSize(owner.getInnerWidth(), owner.getInnerHeight());
            }
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
            confirmed = null;
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
}
