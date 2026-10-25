package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.rom.nds.bw.BwPokedex;

import java.util.*;
import java.util.function.IntConsumer;

/**
 * Official EvolutionTreeWidget / BattleStatModTagLabel: compact sprites and item-condition squares.
 */
final class PokedexEvolutionTree extends Widget {
    private final BwPokedex dex;
    private final boolean mobile;
    private final Map<Integer, Widget> cards = new LinkedHashMap<>(),
            conditions = new LinkedHashMap<>();
    private final Map<Integer, Widget[]> arrows = new LinkedHashMap<>();
    private final Map<Integer, Integer> depths = new LinkedHashMap<>(), rows = new HashMap<>();
    private int leaves, maxDepth;

    PokedexEvolutionTree(
            BwPokedex dex, PokedexImages images, boolean mobile, int selected, IntConsumer select) {
        this.dex = dex;
        this.mobile = mobile;
        setTheme("");
        int root = selected;
        var visited = new HashSet<Integer>();
        while (dex.parent(root) > 0 && visited.add(root)) root = dex.parent(root);
        collect(root, 0, new HashSet<>());
        for (int id : depths.keySet()) {
            Button card =
                    new Button() {
                        @Override
                        protected void paintWidget(GUI gui) {
                            super.paintWidget(gui);
                            int size = mobile ? 72 : 36;
                            images.icon(id)
                                    .draw(
                                            getAnimationState(),
                                            getX() + (mobile ? 10 : 5),
                                            getY() + (mobile ? 4 : 2),
                                            size,
                                            size);
                        }
                    };
            card.setTheme("/monsterdex-button-evo-tree");
            card.setCanAcceptKeyboardFocus(false);
            card.setTooltipContent(String.format(Locale.ROOT, "%03d %s", id, dex.name(id)));
            card.setTooltipDelay(200); // TrainerBadgeTagLabel.
            card.addCallback(() -> select.accept(id));
            cards.put(id, card);
            add(card);
            int parent = dex.parent(id);
            if (!depths.containsKey(parent)) continue;
            var evolution =
                    dex.get(parent).evolutions().stream()
                            .filter(e -> e.target() == id)
                            .findFirst()
                            .orElseThrow();
            int item =
                    switch (evolution.method()) {
                        case 1, 2, 3 -> 5218;
                        case 4, 14, 21, 22, 23, 24 -> 5050;
                        case 5, 7 -> 5437;
                        case 6, 8, 17, 18, 19, 20 -> evolution.parameter() + 5000;
                        case 9 -> 5057;
                        case 10 -> 5056;
                        case 11 -> 5058;
                        case 15 -> 5004;
                        case 16 -> 5261;
                        case 25, 26, 27 -> 5442;
                        default -> 0;
                    };
            boolean level = Set.of(4, 14, 23, 24).contains(evolution.method());
            var badge =
                    new ConditionButton() {
                        final Label label =
                                new Label(level ? "Lv. " + evolution.parameter() : "") {
                                    @Override
                                    protected boolean handleEvent(Event event) {
                                        return false;
                                    }
                                };

                        {
                            setTheme("/monsterdex-button");
                            setCanAcceptKeyboardFocus(false);
                            label.setTheme("/pokedex-evolution-name");
                            label.setEnabled(false);
                            add(label);
                            setTooltipContent(dex.evolution(evolution));
                        }

                        @Override
                        protected void layout() {
                            label.setSize(getInnerWidth(), label.computeTextHeight());
                            label.setPosition(getInnerX(), getInnerBottom() - label.getHeight());
                        }

                        @Override
                        protected void paintWidget(GUI gui) {
                            super.paintWidget(gui);
                            int scale = mobile ? 2 : 1, size = 24 * scale;
                            if (item > 0)
                                images.item(item)
                                        .draw(
                                                getAnimationState(),
                                                getX() + 10 * scale,
                                                getY() + (level ? 4 * scale : 10 * scale),
                                                size,
                                                size);
                        }
                    };
            conditions.put(id, badge);
            add(badge);
            Widget[] pair = {new Label("─➤"), new Label("─➤")};
            for (Widget arrow : pair) {
                arrow.setTheme("/pokedex-evolution-arrow");
                add(arrow);
            }
            arrows.put(id, pair);
        }
    }

    private static class ConditionButton extends Button implements GUI.TooltipPositionAdjuster {
        ConditionButton() {
            setTooltipDelay(0);
        } // BattleStatModTagLabel.Bb(0), including unknown conditions.

        @Override
        public void adjustTooltipPosition(
                GUI.TooltipPositionAdjuster.TooltipPositionData position) {
            position.x = getX() + getWidth() / 2;
            position.y = getY() - 5;
            position.alignment = Alignment.BOTTOM;
        }
    }

    private int collect(int id, int depth, Set<Integer> visited) {
        if (!visited.add(id)) return rows.getOrDefault(id, 0);
        depths.put(id, depth);
        maxDepth = Math.max(maxDepth, depth);
        var children =
                dex.get(id).evolutions().stream()
                        .map(BwPokedex.Evolution::target)
                        .distinct()
                        .toList();
        int row;
        if (children.isEmpty()) row = leaves++ * 2;
        else {
            int first = collect(children.get(0), depth + 1, visited), last = first;
            for (int i = 1; i < children.size(); i++)
                last = collect(children.get(i), depth + 1, visited);
            row = (first + last) / 2;
        }
        rows.put(id, row);
        return row;
    }

    private int rowHeight() {
        return mobile ? 120 : 64;
    }

    @Override
    public int getPreferredInnerHeight() {
        return Math.max(mobile ? 360 : 323, leaves * rowHeight() + 32);
    }

    @Override
    protected void layout() {
        int size = mobile ? 92 : 46, step = size * 4;
        int left = getInnerX() + (getInnerWidth() - (maxDepth * step + size)) / 2;
        // The reference centres each card itself, rather than the taller spacing slot around it.
        int top =
                getInnerY()
                        + Math.max(16, (getInnerHeight() - leaves * rowHeight()) / 2)
                        + (rowHeight() - size) / 2;
        for (var entry : cards.entrySet()) {
            int id = entry.getKey();
            Widget card = entry.getValue();
            card.setSize(size, size);
            card.setPosition(left + depths.get(id) * step, top + rows.get(id) * rowHeight() / 2);
        }
        for (var entry : conditions.entrySet()) {
            int id = entry.getKey();
            Widget from = cards.get(dex.parent(id)), to = cards.get(id), badge = entry.getValue();
            badge.setSize(size, size);
            badge.setPosition((from.getRight() + to.getX() - size) / 2, to.getY());
            Widget[] pair = arrows.get(id);
            int width = size;
            pair[0].setSize(width, size);
            pair[0].setPosition((from.getRight() + badge.getX() - width) / 2, badge.getY());
            pair[1].setSize(width, size);
            pair[1].setPosition((badge.getRight() + to.getX() - width) / 2, badge.getY());
        }
    }
}
