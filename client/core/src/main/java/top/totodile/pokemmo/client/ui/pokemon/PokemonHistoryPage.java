package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.server.game.PokemonData.Pokemon;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/** Reference PokemonSummaryWindow historyTab, with ROM-backed characteristic text. */
final class PokemonHistoryPage extends Widget {
    private final PokemonController controller;
    private final Supplier<Pokemon> selected;
    private final Label title = new Label(), information = new Label();
    private final Widget inherited =
            new Widget() {
                @Override
                protected void paintWidget(GUI gui) {
                    var p = selected.get();
                    if (p == null || controller.images() == null) return;
                    var image = controller.images().egg();
                    if (image != null)
                        image.draw(getAnimationState(), getX() - 30, getY() - 35, 96, 96);
                }
            };
    private String text = "";

    PokemonHistoryPage(PokemonController controller, Supplier<Pokemon> selected) {
        this.controller = controller;
        this.selected = selected;
        setTheme("");
        title.setTheme("/label-monster-area.label-monster-title");
        title.setText(controller.strings.get(1846));
        information.setTheme("/summary-history-information");
        information.setClip(true);
        inherited.setTheme("");
        add(title);
        add(information);
        add(inherited);
    }

    void refresh(String trainer) {
        var p = selected.get();
        if (p == null) return;
        var strings = controller.strings;
        var origin = p.origin();
        var result = new StringBuilder();
        if (origin.time() > 0) {
            String action =
                    strings.get(
                            origin.level() == -1
                                    ? 1873
                                    : p.egg() || origin.address() < 0 && origin.address() >= -7
                                            ? 1874
                                            : 1872);
            String prefix =
                    !origin.trainer().isEmpty() && !origin.trainer().equals(trainer)
                            ? strings.get(1871)
                            : "";
            String level =
                    !p.egg() && origin.level() > 0
                            ? strings.format(1875, Integer.toString(origin.level()))
                            : "";
            String location = "";
            if (origin.address() == -1) {
                location = strings.get(1876);
                prefix = "";
            } else if (origin.address() <= -2 && origin.address() >= -7) {
                int id =
                        switch (origin.address()) {
                            case -2 -> 101775;
                            case -3 -> 101773;
                            case -4 -> 101771;
                            case -6 -> 101730;
                            case -7 -> 101754;
                            default -> 101769;
                        };
                location = strings.format(1888, strings.get(id));
                prefix = "";
            } else {
                int code =
                        origin.region() == 0
                                ? 139912 + (origin.address() & 255)
                                : 140000 + origin.region() * 1000 + (origin.address() & 255);
                String place =
                        origin.address() == Integer.MIN_VALUE || origin.level() == 0
                                ? ""
                                : strings.getOrDefault(code, "");
                if (place.isEmpty()) place = controller.dex().regionName(origin.region());
                if (origin.fateful()) place = strings.get(1884);
                if (!place.isEmpty()) location = strings.format(origin.raid() ? 1889 : 1877, place);
            }
            String date =
                    strings.format(
                            1878,
                            DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss")
                                    .format(
                                            Instant.ofEpochSecond(origin.time())
                                                    .atZone(ZoneId.systemDefault())));
            // Current official containers have seven slots; the reference's older container has
            // five.
            result.append(
                    strings.get(1870).contains("{05}")
                            ? strings.format(1870, prefix, action, level, location, "", date, "")
                            : strings.format(1870, prefix, action, level, location, date));
        } else result.append("—");
        if (!p.egg()) {
            int up = p.nature() / 5, down = p.nature() % 5;
            int[] flavors = {0, 4, 2, 1, 3};
            result.append("\n\n")
                    .append(
                            up == down
                                    ? strings.get(1883)
                                    : strings.format(
                                            1881,
                                            strings.get(100100 + 2 * flavors[up]),
                                            strings.get(100100 + 2 * flavors[down])));
            int[] order = {0, 1, 2, 5, 3, 4};
            int stat = 0, max = -1;
            for (int i = 0; i < order.length; i++)
                if (p.ivs().get(order[i]) > max) {
                    max = p.ivs().get(order[i]);
                    stat = i;
                }
            if (max == 0) stat = (int) (Math.abs((long) origin.personality()) % 6);
            result.append("\n\n").append(controller.dex().characteristic(stat, max));
            if (p.hiddenAbility()) result.append("\n\n").append(strings.get(1885));
        }
        text = result.toString();
        information.setTooltipContent(null);
        invalidateLayout();
        var moves =
                origin.inheritedMoves().stream()
                        .filter(id -> id > 0)
                        .map(controller.dex()::moveName)
                        .toList();
        inherited.setVisible(!moves.isEmpty());
        inherited.setTooltipContent(strings.format(1860, String.join("\n", moves)));
        inherited.setTooltipDelay(0);
    }

    private String wrap(int width) {
        var font = information.getFont();
        if (font == null) return text;
        var out = new StringBuilder();
        String line = "";
        for (char c : text.replace("\r", "").toCharArray()) {
            if (c == '\n') {
                out.append(line).append('\n');
                line = "";
                continue;
            }
            if (!line.isEmpty() && font.computeTextWidth(line + c) > width) {
                int space = line.lastIndexOf(' ');
                if (space > 0) {
                    out.append(line, 0, space).append('\n');
                    line = line.substring(space + 1);
                } else {
                    out.append(line).append('\n');
                    line = "";
                }
            }
            line += c;
        }
        return out.append(line).toString();
    }

    @Override
    protected void layout() {
        title.setPosition(getX(), getY());
        title.setSize(205, 25);
        information.setPosition(getX(), getY() + 28);
        information.setSize(205, 234);
        information.setText(wrap(195));
        inherited.setPosition(getX() + 215, getY() + 28);
        inherited.setSize(32, 32);
    }
}
