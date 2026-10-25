package top.totodile.pokemmo.client.battle;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.SimpleListModel;

import top.totodile.pokemmo.client.resources.ClientStrings;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;

import java.util.*;
import java.util.function.IntConsumer;

/** Test catalogue: reference MoveSelectionSearchListWidget's ID/name search and native list. */
final class BattleMovePicker extends PopupWindow {
    private final BwPokedex dex;
    private final IntConsumer selected;
    private final EditField search = new EditField();
    private final ListBox<String> list = new ListBox<>();
    private final Button use, cancel;
    private List<Integer> visible = List.of();

    BattleMovePicker(Widget owner, BwPokedex dex, ClientStrings strings, IntConsumer selected) {
        super(owner);
        this.dex = dex;
        this.selected = selected;
        setTheme("/world-menu");
        search.setTheme("/editfield");
        search.setTooltipContent("招式编号 / 名称");
        search.addCallback(
                key -> {
                    if (key == Event.KEY_RETURN) choose();
                    else if (key == Event.KEY_ESCAPE) closePopup();
                    else filter();
                });
        add(search);
        list.setTheme("/comboboxPopup.listbox");
        list.addCallback(
                reason -> {
                    if (reason.actionRequested()) choose();
                });
        add(list);
        use = new Button("使用");
        use.setTheme("/button");
        use.addCallback(this::choose);
        add(use);
        cancel = new Button(strings.get(53));
        cancel.setTheme("/button");
        cancel.addCallback(this::closePopup);
        add(cancel);
        filter();
    }

    void open() {
        search.setText("");
        filter();
        if (openPopup()) {
            var gui = getGUI();
            int w = Math.min(360, gui.getWidth() - 8), h = Math.min(420, gui.getHeight() - 8);
            setSize(w, h);
            setPosition((gui.getWidth() - w) / 2, (gui.getHeight() - h) / 2);
            search.requestKeyboardFocus();
        }
    }

    private void filter() {
        String query = search.getText().strip().toLowerCase(Locale.ROOT);
        Integer number = null;
        try {
            number = Integer.valueOf(query);
        } catch (NumberFormatException ignored) {
        }
        final Integer id = number;
        visible =
                dex.allMoves().stream()
                        .map(BwPokedex.Move::id)
                        .filter(
                                move ->
                                        query.isEmpty()
                                                || id != null && move.equals(id)
                                                || id == null
                                                        && dex.moveName(move)
                                                                .toLowerCase(Locale.ROOT)
                                                                .contains(query))
                        .toList();
        list.setModel(
                new SimpleListModel<String>() {
                    @Override
                    public int getNumEntries() {
                        return visible.size();
                    }

                    @Override
                    public String getEntry(int index) {
                        int move = visible.get(index);
                        return move + "  " + dex.moveName(move);
                    }
                });
        list.setSelected(visible.isEmpty() ? -1 : 0);
        useEnabled();
    }

    private void useEnabled() {
        if (use != null) use.setEnabled(!visible.isEmpty());
    }

    private void choose() {
        int index = list.getSelected();
        if (!isOpen() || index < 0 || index >= visible.size()) return;
        int id = visible.get(index);
        closePopup();
        selected.accept(id);
    }

    @Override
    protected void layout() {
        int x = getInnerX(), y = getInnerY(), w = getInnerWidth(), h = getInnerHeight();
        search.setPosition(x, y);
        search.setSize(w, 30);
        list.setPosition(x, y + 34);
        list.setSize(w, Math.max(0, h - 72));
        use.setPosition(x, getInnerBottom() - 30);
        use.setSize((w - 4) / 2, 30);
        cancel.setPosition(use.getRight() + 4, getInnerBottom() - 30);
        cancel.setSize((w - 4) / 2, 30);
    }
}
