package top.totodile.pokemmo.client.battle;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;
import top.totodile.pokemmo.client.ui.common.WordWrapTextFormatter;
import top.totodile.pokemmo.client.ui.inventory.InventoryItems;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import top.totodile.pokemmo.server.game.EncounterData;
import top.totodile.pokemmo.server.game.PlayerData.Item;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;

import java.util.*;
import java.util.function.*;

/** PC BattleCombatLayout.TH0 / CompositeViewContainerLayout / ScrollViewContainerLayout. */
final class EncounterChoices extends Widget {
    private enum View {
        SWITCH,
        BAG,
        ITEM_TARGET,
        MOVE_TARGET,
        ENEMY_TARGET
    }

    private static final int[] POCKETS = {
        7, 5, 3, 8
    }; // BagPocket.qk0: medicine, berries, balls, battle items.
    private final ClientResources resources;
    private final BwPokedex dex;
    private final PokedexImages images;
    private final Consumer<EncounterWindow.Selection> selected;
    private final Runnable cancelled;
    private final top.totodile.pokemmo.client.ui.common.ActionConfirmDialog confirmation;
    private final PokemonButton[] members = new PokemonButton[6];
    private final Button[] moveButtons = new Button[4];
    private final Button back = new Button(),
            previous = new Button("≪"),
            next = new Button("≫"),
            use = new Button(),
            registerBattle = new Button();
    private final Label pocket = new Label(),
            detail = new Label(),
            itemName = new Label(),
            divider = new Label("--------");
    private final Widget rows =
            new Widget() {
                @Override
                protected void layout() {
                    for (int i = 0; i < itemButtons.size(); i++) {
                        itemButtons.get(i).setPosition(getX(), getY() + i * 30);
                        itemButtons.get(i).setSize(getWidth(), 30);
                    }
                }

                @Override
                public int getPreferredInnerHeight() {
                    return itemButtons.size() * 30;
                }
            };
    private final ScrollPane list = new ScrollPane(rows);
    private final java.util.List<Button> itemButtons = new ArrayList<>();
    private final Widget picture =
            new Widget() {
                @Override
                protected void paintWidget(GUI gui) {
                    if (selectedItem != null) {
                        Image icon = items.icon(selectedItem.itemId());
                        if (icon != null)
                            icon.draw(getAnimationState(), getX() + 6, getY() + 6, 48, 48);
                    }
                }
            };
    private InventoryItems items;
    private EncounterData encounter;
    private java.util.List<Item> inventory = java.util.List.of();
    private Item selectedItem;
    private Pokemon target;
    private View view;
    private int pocketIndex;
    private int generation;

    EncounterChoices(
            ClientResources resources,
            BwPokedex dex,
            PokedexImages images,
            Consumer<EncounterWindow.Selection> selected,
            Runnable cancelled) {
        this.resources = resources;
        this.dex = dex;
        this.images = images;
        this.selected = selected;
        this.cancelled = cancelled;
        setTheme("/encounter-panel");
        confirmation =
                new top.totodile.pokemmo.client.ui.common.ActionConfirmDialog(
                        this, resources.strings(), false, image -> {}, () -> {});
        for (int i = 0; i < members.length; i++) {
            final int index = i;
            members[i] = new PokemonButton();
            members[i].addCallback(() -> chooseMember(index));
            add(members[i]);
        }
        for (int i = 0; i < moveButtons.length; i++) {
            final int slot = i;
            var button = moveButtons[i] = new MoveTargetButton();
            button.setTheme("/encounter-choice-move");
            button.addCallback(
                    () -> {
                        if (target != null && target.moves().get(slot) > 0)
                            submit(
                                    new EncounterWindow.Selection(
                                            EncounterWindow.SelectionKind.ITEM,
                                            target.id(),
                                            selectedItem.objectId(),
                                            selectedItem.itemId(),
                                            slot));
                    });
            add(button);
        }
        back.setText(resources.strings().get(58));
        back.setTheme("/encounter-return");
        back.addCallback(this::back);
        previous.addCallback(() -> changePocket(-1));
        next.addCallback(() -> changePocket(1));
        use.setText(resources.strings().get(5029));
        use.setTheme("/encounter-confirm");
        registerBattle.setText(resources.strings().get(1408));
        registerBattle.setTheme("/encounter-register");
        // The official capture contains registration; this reference revision supplies its skin
        // and label but no registration handler. Keep the existing UI-only scope.
        itemName.setTheme("/encounter-caption-left");
        divider.setTheme("/encounter-caption-left");
        picture.setTheme("/encounter-item-picture");
        previous.setTheme("/encounter-bag-arrow");
        next.setTheme("/encounter-bag-arrow");
        use.addCallback(this::useItem);
        pocket.setTheme("/encounter-bag-pocket");
        detail.setTheme("/encounter-bag-detail");
        detail.setClip(true);
        list.setFixed(ScrollPane.Fixed.HORIZONTAL);
        for (var child :
                new Widget[] {
                    back,
                    previous,
                    next,
                    use,
                    registerBattle,
                    pocket,
                    detail,
                    itemName,
                    divider,
                    list,
                    picture
                }) add(child);
        setVisible(false);
    }

    void openSwitch(EncounterData data) {
        generation++;
        encounter = data;
        selectedItem = null;
        target = null;
        view = View.SWITCH;
        show();
    }

    void openTargets(EncounterData data) {
        generation++;
        encounter = data;
        selectedItem = null;
        target = null;
        view = View.ENEMY_TARGET;
        show();
    }

    void openBag(
            EncounterData data,
            InventoryItems catalog,
            Consumer<Consumer<java.util.List<Item>>> load) {
        encounter = data;
        items = catalog;
        view = View.BAG;
        selectedItem = null;
        inventory = java.util.List.of();
        int request = ++generation;
        rebuildItems();
        show();
        load.accept(
                value -> {
                    if (request != generation || !isVisible()) return;
                    inventory = java.util.List.copyOf(value);
                    rebuildItems();
                });
    }

    private void show() {
        setVisible(true);
        refresh();
        if (view == View.SWITCH || view == View.ITEM_TARGET || view == View.ENEMY_TARGET)
            members[0].requestKeyboardFocus();
        else previous.requestKeyboardFocus();
    }

    void close() {
        generation++;
        confirmation.closePopup();
        setVisible(false);
    }

    int panelHeight() {
        return view == View.BAG ? 265 : 115;
    }

    private void refresh() {
        boolean bag = view == View.BAG;
        for (var child :
                new Widget[] {
                    previous,
                    next,
                    use,
                    registerBattle,
                    pocket,
                    detail,
                    itemName,
                    divider,
                    list,
                    picture
                }) child.setVisible(bag);
        use.setVisible(bag && selectedItem != null);
        registerBattle.setVisible(bag && selectedItem != null);
        divider.setVisible(bag && selectedItem != null);
        java.util.List<Pokemon> candidates =
                view == View.ENEMY_TARGET ? encounter.wilds() : encounter.party();
        if (view == View.ITEM_TARGET && items.battleAction(selectedItem.itemId()) == 6)
            candidates = java.util.List.of(encounter.player());
        else if (view == View.ITEM_TARGET && items.battleAction(selectedItem.itemId()) == 5)
            candidates = java.util.List.of(encounter.player());
        for (int i = 0; i < members.length; i++) {
            members[i].pokemon = i < candidates.size() ? candidates.get(i) : null;
            members[i].refresh();
            members[i].setVisible(
                    view == View.SWITCH || view == View.ITEM_TARGET || view == View.ENEMY_TARGET);
        }
        for (var button : moveButtons) button.setVisible(view == View.MOVE_TARGET);
        if (view == View.MOVE_TARGET)
            for (int i = 0; i < 4; i++) {
                int id = target.moves().get(i);
                int max =
                        id == 0
                                ? 0
                                : Math.min(
                                        64,
                                        dex.move(id).pp()
                                                + dex.move(id).pp() * target.ppUps().get(i) / 5);
                int pp = target.pp().get(i) < 0 ? max : target.pp().get(i);
                var button = (MoveTargetButton) moveButtons[i];
                button.setText(id == 0 ? "-" : dex.moveName(id));
                button.pp.setText(resources.strings().get(1852) + " " + pp + " / " + max);
                button.type = id == 0 ? null : images.type(dex.move(id).type());
            }
        invalidateLayout();
    }

    private void chooseMember(int index) {
        Pokemon pokemon = members[index].pokemon;
        if (pokemon == null || pokemon.egg()) return;
        if (view == View.ENEMY_TARGET) {
            if (pokemon.hp() <= 0) return;
            submit(
                    new EncounterWindow.Selection(
                            EncounterWindow.SelectionKind.TARGET, pokemon.id(), "", 0, -1));
        } else if (view == View.SWITCH) {
            if (pokemon.hp() <= 0 || pokemon.id().equals(encounter.player().id())) return;
            submit(
                    new EncounterWindow.Selection(
                            EncounterWindow.SelectionKind.SWITCH, pokemon.id(), "", 0, -1));
        } else if (view == View.ITEM_TARGET) {
            target = pokemon;
            if (items.battleAction(selectedItem.itemId()) == 3) {
                view = View.MOVE_TARGET;
                refresh();
                moveButtons[0].requestKeyboardFocus();
            } else
                submit(
                        new EncounterWindow.Selection(
                                EncounterWindow.SelectionKind.ITEM,
                                pokemon.id(),
                                selectedItem.objectId(),
                                selectedItem.itemId(),
                                -1));
        }
    }

    private void submit(EncounterWindow.Selection choice) {
        if (choice.kind() == EncounterWindow.SelectionKind.ITEM
                && items.confirmBattleUse(choice.itemId()))
            confirmation.show(
                    resources.strings().format(1435, items.name(choice.itemId())),
                    () -> complete(choice));
        else complete(choice);
    }

    private void complete(EncounterWindow.Selection choice) {
        close();
        selected.accept(choice);
    }

    private void changePocket(int direction) {
        int index = pocketIndex + direction;
        if (index < 0 || index >= POCKETS.length) return;
        pocketIndex = index;
        rebuildItems();
    }

    private void rebuildItems() {
        for (var button : itemButtons) rows.removeChild(button);
        itemButtons.clear();
        selectedItem = null;
        itemName.setText("");
        divider.setVisible(false);
        pocket.setText(resources.strings().get(new int[] {1404, 1406, 2, 1405}[pocketIndex]));
        if (items != null) {
            // The reference groups equal templates across inventory stacks, then sorts by item id.
            var groups = new TreeMap<Integer, java.util.List<Item>>();
            for (var item : inventory)
                if (item.amount() > 0
                        && items.battleAction(item.itemId()) != 0
                        && items.template(item.itemId()).pocket() == POCKETS[pocketIndex])
                    groups.computeIfAbsent(item.itemId(), ignored -> new ArrayList<>()).add(item);
            for (var group : groups.values()) {
                var item = group.get(0);
                int amount = group.stream().mapToInt(Item::amount).sum();
                var button =
                        new Button(
                                items.name(item.itemId())
                                        + " x"
                                        + java.text.NumberFormat.getInstance().format(amount)) {
                            @Override
                            protected boolean handleEvent(Event event) {
                                if (event.getType() == Event.Type.MOUSE_ENTERED) selectItem(item);
                                return super.handleEvent(event);
                            }

                            @Override
                            protected void keyboardFocusGained() {
                                super.keyboardFocusGained();
                                selectItem(item);
                            }
                        };
                button.setTheme("/encounter-bag-entry");
                button.addCallback(() -> selectItem(item));
                button.setTooltipContent(items.description(item.itemId()));
                rows.add(button);
                itemButtons.add(button);
            }
            if (!groups.isEmpty()) selectItem(groups.firstEntry().getValue().get(0));
        }
        detail.setText(
                selectedItem == null
                        ? ""
                        : WordWrapTextFormatter.LPt2(28, items.description(selectedItem.itemId())));
        use.setVisible(view == View.BAG && selectedItem != null);
        registerBattle.setVisible(view == View.BAG && selectedItem != null);
        list.setScrollPositionY(0);
        rows.invalidateLayout();
        invalidateLayout();
    }

    private void selectItem(Item item) {
        selectedItem = item;
        itemName.setText(items.name(item.itemId()));
        divider.setVisible(true);
        registerBattle.setVisible(view == View.BAG);
        detail.setText(WordWrapTextFormatter.LPt2(28, items.description(item.itemId())));
        use.setVisible(true);
        invalidateLayout();
    }

    private void useItem() {
        if (selectedItem == null) return;
        int action = items.battleAction(selectedItem.itemId());
        if (action == 1)
            submit(
                    new EncounterWindow.Selection(
                            EncounterWindow.SelectionKind.ITEM,
                            "",
                            selectedItem.objectId(),
                            selectedItem.itemId(),
                            -1));
        else if (action == 2 || action == 3 || action == 5 || action == 6) {
            view = View.ITEM_TARGET;
            show();
        }
    }

    private void back() {
        if (view == View.MOVE_TARGET) {
            view = View.ITEM_TARGET;
            refresh();
        } else if (view == View.ITEM_TARGET) {
            view = View.BAG;
            refresh();
        } else {
            close();
            cancelled.run();
        }
    }

    @Override
    protected boolean handleEvent(Event event) {
        if (event.isKeyPressedEvent()
                && (event.getKeyCode() == Event.KEY_ESCAPE || event.getKeyCode() == Event.KEY_B)) {
            back();
            return true;
        }
        if (event.isKeyPressedEvent()) {
            if (view == View.BAG
                    && (event.getKeyCode() == Event.KEY_LEFT
                            || event.getKeyCode() == Event.KEY_RIGHT)) {
                changePocket(event.getKeyCode() == Event.KEY_LEFT ? -1 : 1);
                return true;
            }
            Button[] group =
                    view == View.SWITCH || view == View.ITEM_TARGET || view == View.ENEMY_TARGET
                            ? members
                            : view == View.MOVE_TARGET
                                    ? moveButtons
                                    : itemButtons.toArray(Button[]::new);
            int columns =
                    view == View.SWITCH || view == View.ITEM_TARGET || view == View.ENEMY_TARGET
                            ? 3
                            : view == View.MOVE_TARGET ? 2 : 1;
            int index = -1;
            for (int i = 0; i < group.length; i++) if (group[i].hasKeyboardFocus()) index = i;
            int step =
                    switch (event.getKeyCode()) {
                        case Event.KEY_RIGHT -> 1;
                        case Event.KEY_LEFT -> -1;
                        case Event.KEY_UP -> -columns;
                        case Event.KEY_DOWN -> columns;
                        default -> 0;
                    };
            if (step != 0 && group.length > 0) {
                if (index >= 0
                        && ((step == 1 && columns > 1 && index % columns == columns - 1)
                                || (step == -1 && columns > 1 && index % columns == 0)))
                    return true;
                int targetIndex =
                        Math.max(0, Math.min(group.length - 1, index < 0 ? 0 : index + step));
                group[targetIndex].requestKeyboardFocus();
                if (view == View.BAG) list.scrollToAreaY(targetIndex * 30, 30, 0);
                return true;
            }
        }
        return super.handleEvent(event);
    }

    @Override
    protected void layout() {
        for (int i = 0; i < 6; i++) {
            members[i].setPosition(getX() + 7 + (i % 3) * 225, getY() + 7 + (i / 3) * 54);
            members[i].setSize(225, 54);
        }
        for (int i = 0; i < 4; i++) {
            moveButtons[i].setPosition(getX() + 7 + (i % 2) * 211, getY() + 7 + (i / 2) * 54);
            moveButtons[i].setSize(209, 52);
        }
        confirmation.positionWindow();
        back.setPosition(getRight() - 103, getBottom() - 37);
        back.setSize(96, 30);
        previous.setPosition(getX() + 7, getY() + 7);
        previous.setSize(64, 30);
        pocket.setPosition(getX() + 71, getY() + 7);
        pocket.setSize(172, 30);
        next.setPosition(getX() + 243, getY() + 7);
        next.setSize(64, 30);
        list.setPosition(getX() + 7, getY() + 39);
        list.setSize(300, getHeight() - 46);
        picture.setPosition(getX() + 312, getY() + 64);
        picture.setSize(64, 64);
        itemName.setPosition(getX() + 382, getY() + 60);
        itemName.setSize(Math.max(1, getWidth() - 396), 24);
        divider.setPosition(getX() + 382, getY() + 82);
        divider.setSize(150, 20);
        detail.setPosition(getX() + 382, getY() + 103);
        detail.setSize(Math.max(1, getWidth() - 396), Math.max(1, getHeight() - 148));
        use.setPosition(getX() + 342, getBottom() - 37);
        use.setSize(96, 30);
        registerBattle.setPosition(getX() + 550, getBottom() - 37);
        registerBattle.setSize(96, 30);
    }

    /** TableViewContainerLayout: move name, native type badge and PP caption. */
    private final class MoveTargetButton extends Button {
        final Label pp = new SlotLabel();
        Image type;

        MoveTargetButton() {
            pp.setTheme("/encounter-caption-left");
            add(pp);
        }

        @Override
        protected void layout() {
            pp.setPosition(getX() + 9, getY() + 28);
            pp.setSize(getWidth() - 18, 20);
        }

        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (type != null) type.draw(getAnimationState(), getX() + 153, getY() + 9);
        }
    }

    private static final class SlotLabel extends Label {
        @Override
        protected boolean handleEvent(Event event) {
            return false;
        }
    }

    private static final class Health extends Widget {
        float fraction;
        Image fill;

        @Override
        protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme);
            fill = theme.getImage("progressImage");
        }

        @Override
        protected void paintWidget(GUI gui) {
            if (fill != null)
                fill.draw(
                        getAnimationState(),
                        getX(),
                        getY(),
                        Math.round(getWidth() * fraction),
                        getHeight());
        }

        @Override
        protected boolean handleEvent(Event event) {
            return false;
        }
    }

    private final class PokemonButton extends Button {
        Pokemon pokemon;
        final Label name = new SlotLabel(), level = new SlotLabel(), hp = new SlotLabel();
        final Health health = new Health();

        PokemonButton() {
            setTheme("/encounter-party-slot");
            for (var label : new Label[] {name, level, hp}) {
                label.setTheme("/encounter-caption-left");
                add(label);
            }
            health.setTheme("/monsterframe-hp-progressbar");
            add(health);
        }

        void refresh() {
            name.setText(
                    pokemon == null
                            ? ""
                            : pokemon.name().isEmpty()
                                    ? dex.name(pokemon.species())
                                    : pokemon.name());
            level.setText(
                    pokemon == null || pokemon.egg()
                            ? ""
                            : resources.strings().get(59) + " " + pokemon.level());
            double percent =
                    pokemon == null || pokemon.maxHp() <= 0
                            ? 0
                            : Math.round(((float) pokemon.hp() * 100f / pokemon.maxHp()) * 10d)
                                    / 10d;
            hp.setText(
                    pokemon == null || pokemon.egg()
                            ? ""
                            : resources
                                    .strings()
                                    .format(
                                            5231,
                                            Integer.toString(pokemon.hp()),
                                            Integer.toString(pokemon.maxHp()),
                                            java.text.NumberFormat.getInstance().format(percent)));
            // BattlePokemonSlotButton.W(PF) displays a percentage for opposing combatants.
            if (view == View.ENEMY_TARGET && pokemon != null && !pokemon.egg())
                hp.setText(
                        resources
                                .strings()
                                .format(
                                        5230,
                                        java.text.NumberFormat.getInstance().format(percent)));
            boolean unavailable =
                    pokemon == null
                            || view == View.SWITCH
                                    && (pokemon.egg()
                                            || pokemon.hp() <= 0
                                            || pokemon.id().equals(encounter.player().id()));
            getAnimationState()
                    .setAnimationState(
                            de.matthiasmann.twl.renderer.AnimationState.StateKey.get("unavailable"),
                            unavailable);
            health.setVisible(pokemon != null && !pokemon.egg());
            if (pokemon != null) {
                float ratio = (float) pokemon.hp() / pokemon.maxHp();
                String skin =
                        ratio < .25f
                                ? "/monsterframe-hp-progressbar-red"
                                : ratio < .5f
                                        ? "/monsterframe-hp-progressbar-orange"
                                        : "/monsterframe-hp-progressbar";
                health.setTheme(skin);
                health.reapplyTheme();
                health.fraction = ratio;
            }
        }

        @Override
        protected void layout() {
            name.setPosition(getX() + 48, getY() + 2);
            name.setSize(115, 20);
            level.setPosition(getX() + 160, getY() + 2);
            level.setSize(62, 19);
            health.setPosition(getX() + 50, getY() + 23);
            health.setSize(168, 8);
            hp.setPosition(getX() + 48, getY() + 31);
            hp.setSize(174, 20);
        }

        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (pokemon == null) return;
            var icon =
                    images.partyIcon(
                            pokemon.species(),
                            pokemon.form(),
                            pokemon.gender(),
                            pokemon.egg(),
                            false,
                            pokemon.shiny(),
                            pokemon.alpha());
            if (icon != null) icon.draw(getAnimationState(), getX() + 6, getY() + 3, 36, 36);
            // BattleCombatLayout.FU calls SF(-1): the party cards do not show gender badges.
            int status =
                    switch (pokemon.status()) {
                        case 32 -> 0;
                        case 64 -> 1;
                        case 16 -> 2;
                        case 8 -> 3;
                        case 7 -> 4;
                        case -128, 128 -> 6;
                        default -> -1;
                    };
            if (status >= 0) {
                var image = images.atlas("status_ailment", status);
                if (image != null)
                    image.draw(getAnimationState(), getX() + 24, getY() + 31, 12, 10);
            }
            if (pokemon.item() > 0)
                images.heldItem().draw(getAnimationState(), getX() + 200, getY() + 28, 16, 14);
        }
    }
}
