package top.totodile.pokemmo.client.ui.inventory;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.server.game.PlayerData;

import java.util.function.BiConsumer;

/** InventoryWindow.UX: item attributes decide which actions and submenus exist. */
final class InventoryContextMenu {
    private final InventoryItems items;
    private final ClientStrings strings;
    private final BiConsumer<PlayerData.Item, InventoryWindow.ItemAction> actions;
    private MenuManager manager;

    InventoryContextMenu(
            InventoryItems items,
            ClientStrings strings,
            BiConsumer<PlayerData.Item, InventoryWindow.ItemAction> actions) {
        this.items = items;
        this.strings = strings;
        this.actions = actions;
    }

    Menu model(PlayerData.Item item) {
        var menu = new Menu();
        var t = items.template(item.itemId());
        var a = items.attributes(item.itemId());
        if (a.action != 0) {
            menu.add(
                    new MenuAction(
                            strings.get(1410),
                            () -> actions.accept(item, InventoryWindow.ItemAction.USE)));
            var register = new Menu(strings.get(1412));
            for (int i = 0; i < 9; i++) {
                final int slot = i + 1;
                register.add(
                        new MenuAction(
                                strings.format(1413, Integer.toString(slot)),
                                () -> items.register(slot, item)));
            }
            menu.add(register);
        }
        if (items.holdable(item.itemId()))
            menu.add(
                    new MenuAction(
                            strings.get(1411),
                            () -> actions.accept(item, InventoryWindow.ItemAction.GIVE)));
        if (items.destroyable(item.itemId()))
            menu.add(
                    new MenuAction(
                            strings.get(1424),
                            () -> actions.accept(item, InventoryWindow.ItemAction.DISCARD)));
        menu.add(new MenuAction(strings.get(1423), null));
        return menu;
    }

    void open(PlayerData.Item item, Widget anchor) {
        close();
        manager = model(item).openPopupMenu(anchor, anchor.getX() + 25, anchor.getY() + 25);
        if (manager.getNumChildren() > 0) {
            var popup = manager.getChild(0);
            popup.setPosition(
                    Math.max(
                            manager.getInnerX(),
                            Math.min(popup.getX(), manager.getInnerRight() - popup.getWidth())),
                    Math.max(
                            manager.getInnerY(),
                            Math.min(popup.getY(), manager.getInnerBottom() - popup.getHeight())));
        }
    }

    void close() {
        if (manager != null) {
            manager.closePopup();
            manager = null;
        }
    }

    boolean isOpen() {
        return manager != null && manager.isOpen();
    }
}
