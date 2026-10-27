package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import top.totodile.pokemmo.server.game.PlayerData;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;
import java.util.*;

/** Reference ItemUpgradeRefineSlotWidget.TK0 / ItemSlotContextMenuBuilder.De0. */
final class PokemonHeldItemMenu {
    private final PokemonController controller;
    private PopupWindow menu, choices;
    private final Map<Button, List<PlayerData.Item>> pockets = new LinkedHashMap<>();
    private Button hovered;
    private Pokemon target;
    private long generation;

    PokemonHeldItemMenu(PokemonController controller) { this.controller = controller; }

    void open(Widget anchor, Pokemon pokemon) {
        close();
        if (controller.busy() || controller.items() == null) return;
        target = pokemon;
        long version = generation;
        controller.inventory(inventory -> {
            if (version != generation || anchor.getGUI() == null || controller.busy()) return;
            var items = controller.items();
            var buttons = new ArrayList<Button>();
            // BagPocket.Oy order; the reference includes only nonempty eligible categories.
            int[] order = {1, 7, 8, 2, 3, 4, 5, 6};
            int[] icons = {5436, 5017, 5057, 5476, 5004, 5252, 5155, 5459};
            for (int i = 0; i < order.length; i++) {
                int pocket = order[i];
                var available = inventory.stream()
                        .filter(item -> item.amount() > 0 && item.region() < 0
                                && items.holdable(item.itemId())
                                && items.template(item.itemId()).pocket() == pocket)
                        .sorted(Comparator.comparing((PlayerData.Item item) -> items.name(item.itemId()),
                                String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(item -> items.name(item.itemId())))
                        .toList();
                if (available.isEmpty()) continue;
                int label = switch(pocket) {
                    case 1 -> 1401; case 2 -> 1402; case 3 -> 2; case 4 -> 3;
                    case 5 -> 1406; case 6 -> 1403; case 7 -> 1404; default -> 1405;
                };
                Button button = button(controller.strings.getOrDefault(label, items.pocketName(label)), icons[i]);
                pockets.put(button, available);
                button.addCallback(() -> showChoices(button, pokemon));
                buttons.add(button);
            }
            if (buttons.isEmpty()) {
                Button empty = button(controller.strings.get(6007), 0);
                empty.setEnabled(false);
                buttons.add(empty);
            }
            if (pokemon.item() > 0) {
                Button remove = button(controller.strings.get(1656), 0);
                remove.addCallback(() -> { close(); controller.heldItem(pokemon, -1); });
                buttons.add(remove);
            }
            menu = popup(anchor, buttons, 154, anchor.getRight() - 154,
                    anchor.getY() - buttons.size() * 36 - 8);
        });
    }

    private Button button(String text, int icon) {
        var button = new Button(text) {
            @Override protected void paintWidget(GUI gui) {
                super.paintWidget(gui);
                var image = icon > 0 ? controller.itemIcon(icon) : null;
                if (image != null) image.draw(getAnimationState(), getX() + 3,
                        getY() + (getHeight() - 24) / 2, 24, 24);
            }
        };
        button.setTheme("/pokemon-ball-menu-button");
        button.setCanAcceptKeyboardFocus(false);
        // Keep room for the same 24px icon and 3px inset as kf0_1.
        button.setBorderSize(3, 31, 3, 6);
        return button;
    }

    private void showChoices(Button owner, Pokemon pokemon) {
        if (choices != null) choices.closePopup();
        hovered = owner;
        var buttons = new ArrayList<Button>();
        for (var item : pockets.get(owner)) {
            var button = button(item.amount() + "x " + controller.items().name(item.itemId()), item.itemId());
            button.setTooltipContent(controller.items().tooltipContent(item.itemId()));
            button.setTooltipDelay(150);
            button.addCallback(() -> { close(); controller.heldItem(pokemon, item.itemId()); });
            buttons.add(button);
        }
        int width = Math.max(180, buttons.stream().mapToInt(Button::getPreferredWidth).max().orElse(0) + 12);
        int x = menu.getRight();
        if (x + width > owner.getGUI().getWidth()) x = menu.getX() - width;
        choices = popup(owner, buttons, width, x, owner.getY());
    }

    private PopupWindow popup(Widget owner, List<Button> buttons, int width, int x, int y) {
        var popup = new PopupWindow(owner);
        popup.setTheme("/pokemon-menu");
        popup.setCloseAndIgnoreMouseDownOutside(true);
        popup.setRouteToParentPopup(true);
        popup.setRequestCloseCallback(this::close);
        var rows = new Widget() {
            @Override protected void layout() {
                int rowY = getInnerY();
                for (var button : buttons) {
                    button.setPosition(getInnerX(), rowY);
                    button.setSize(getInnerWidth(), 36);
                    rowY += 36;
                }
            }
            @Override public int getPreferredInnerHeight() { return buttons.size() * 36; }
        };
        rows.setTheme("");
        buttons.forEach(rows::add);
        var scroll = new ScrollPane(rows);
        scroll.setTheme("/scrollpane");
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        popup.add(scroll);
        if (popup.openPopup()) {
            int height = Math.min(popup.getGUI().getHeight() - 16,
                    buttons.size() * 36 + popup.getBorderVertical() + scroll.getBorderVertical());
            width = Math.min(width, popup.getGUI().getWidth() - 16);
            popup.setSize(width, height);
            popup.setPosition(Math.max(8, Math.min(x, popup.getGUI().getWidth() - width - 8)),
                    Math.max(8, Math.min(y, popup.getGUI().getHeight() - height - 8)));
        }
        return popup;
    }

    void hover(int x, int y) {
        if (menu == null || !menu.isOpen()) return;
        if (choices != null && choices.isInside(x, y)) return;
        for (var button : pockets.keySet())
            if (button != hovered && button.isInside(x, y)) { showChoices(button, target); return; }
    }

    void giveToParty(Widget anchor, PlayerData.Item item) {
        close();
        if (controller.busy() || controller.items() == null || !controller.items().holdable(item.itemId())) return;
        var buttons = new ArrayList<Button>();
        for (var pokemon : controller.state().pokemons().stream()
                .filter(p -> p.container() == top.totodile.pokemmo.server.game.PokemonData.PARTY)
                .sorted(Comparator.comparingInt(Pokemon::position)).toList()) {
            var button = new Button(controller.name(pokemon)) {
                @Override protected void paintWidget(GUI gui) {
                    super.paintWidget(gui);
                    var icon = controller.characterPartyIcon(pokemon);
                    if (icon != null) icon.draw(getAnimationState(), getX() + 3, getY() + 2, 32, 32);
                }
            };
            button.setTheme("/pokemon-ball-menu-button");
            button.setCanAcceptKeyboardFocus(false);
            button.addCallback(() -> { close(); controller.heldItem(pokemon, item.itemId()); });
            buttons.add(button);
        }
        if (buttons.isEmpty()) {
            var empty = button(controller.strings.get(6007), 0);
            empty.setEnabled(false);
            buttons.add(empty);
        }
        menu = popup(anchor, buttons, 180, com.badlogic.gdx.Gdx.input.getX(),
                com.badlogic.gdx.Gdx.input.getY());
    }

    void close() {
        generation++;
        if (choices != null) choices.closePopup();
        if (menu != null) menu.closePopup();
        choices = menu = null;
        pockets.clear();
        hovered = null;
        target = null;
    }
}
