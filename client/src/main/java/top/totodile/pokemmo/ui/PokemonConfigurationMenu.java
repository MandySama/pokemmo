package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.Menu;
import de.matthiasmann.twl.MenuAction;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.protocol.PartyMember;
import top.totodile.pokemmo.protocol.PokemonCatalog;
import top.totodile.pokemmo.protocol.PokemonConfigPackets;

import java.util.function.Consumer;

/** 详情页的分级配置菜单；当前值不可重复选择，操作由服务端完成。 */
final class PokemonConfigurationMenu {
    private final PartyMember member;
    private final Consumer<PokemonConfigPackets.Request> submit;
    private Consumer<String> costumeSelected;

    PokemonConfigurationMenu withCostumes(Consumer<String> action) { costumeSelected = action; return this; }

    PokemonConfigurationMenu(PartyMember member, Consumer<PokemonConfigPackets.Request> submit) {
        this.member = member;
        this.submit = submit;
    }

    Menu build() {
        Menu root = new Menu();
        if (costumeSelected != null) addCostumes(root);
        Menu set = submenu(root, 1008);
        var d = member.individual();
        Menu shiny = submenu(set, 5614);
        toggle(shiny, PokemonConfigPackets.Action.SHINY, member.shiny());
        Menu alpha = submenu(set, 8119);
        toggle(alpha, PokemonConfigPackets.Action.ALPHA, member.alpha());
        var availableForms = top.totodile.pokemmo.protocol.PokemonFormsCatalog.choices(member.species());
        if (availableForms.size() > 1) {
            Menu forms = submenu(set, 9796);
            int[] labels = {9600, 9797, 9798, 9799};
            for (int i : availableForms) {
                choice(forms, ClientStrings.get(labels[i]), PokemonConfigPackets.Action.FORM, 0, i, d.form() != i);
            }
        }
        var species = PokemonCatalog.get(member.species());
        Menu gender = submenu(set, 1055);
        for (int value = 0; value < 2; value++) {
            boolean allowed = species.gender() != 255 && (species.gender() == 254 ? value == 1 : species.gender() != 0 || value == 0);
            choice(gender, ClientStrings.get(1056 + value), PokemonConfigPackets.Action.GENDER, 0, value,
                    allowed && d.gender() != value);
        }
        Menu ability = submenu(set, 1824);
        Menu hidden = submenu(ability, 1886);
        choice(hidden, ClientStrings.get(9727), PokemonConfigPackets.Action.ABILITY, 0, 2,
                species.hiddenAbility() > 0 && d.ability() != species.hiddenAbility());
        choice(hidden, ClientStrings.get(65), PokemonConfigPackets.Action.ABILITY, 0, 0,
                d.ability() == species.hiddenAbility());
        int[] abilities = {species.ability1(), species.ability2(), species.hiddenAbility()};
        for (int i = 0; i < abilities.length; i++) if (abilities[i] > 0) {
            choice(ability, ClientStrings.get(210000 + abilities[i]), PokemonConfigPackets.Action.ABILITY, 0, i,
                    d.ability() != abilities[i]);
        }
        Menu nature = submenu(set, 9406);
        for (int i = 0; i < 25; i++) {
            String text = ClientStrings.get(180000 + i);
            int plus = i / 5, minus = i % 5;
            if (plus != minus) text += " (+10% " + ClientStrings.get(9690 + plus) + " / -10% " + ClientStrings.get(9690 + minus) + ")";
            choice(nature, text, PokemonConfigPackets.Action.NATURE, 0, i, d.nature() != i);
        }
        Menu ivs = submenu(set, 1849);
        presets(ivs, PokemonConfigPackets.Action.IVS, 0, new int[]{31, 15, 0}, false);
        choice(ivs, ClientStrings.get(9724), PokemonConfigPackets.Action.IVS, 0, -1, true);
        Menu evs = submenu(set, 1800);
        presets(evs, PokemonConfigPackets.Action.EVS, 0, new int[]{85, 42, 0}, true);
        int[] stats = {1809, 1812, 1813, 1814, 1815, 1816};
        for (int i = 0; i < 6; i++) {
            Menu stat = submenu(evs, stats[i]);
            presets(stat, PokemonConfigPackets.Action.EV, i, new int[]{252, 126, 0}, false);
        }
        Menu friendship = submenu(set, 1811);
        presets(friendship, PokemonConfigPackets.Action.FRIENDSHIP, 0, new int[]{255, 127, 0}, false);
        Menu ball = submenu(set, 2);
        // 个体记录的球编号从 1 开始，名称目录的球编号从 0 开始。
        for (int i = 0; i < 25; i++) {
            int name = i < 16 ? 248001 + i : i < 24 ? 245476 + i : 245576;
            choice(ball, ClientStrings.get(name), PokemonConfigPackets.Action.BALL, 0, i + 1, d.ball() != i + 1);
        }
        root.add(ClientStrings.get(53), () -> { });
        return root;
    }

    private void addCostumes(Menu root) {
        var fashion = submenu(root, 9790);
        String current = top.totodile.pokemmo.rom.PokemonCostumes.selected(member);
        fashion.add(new MenuAction(ClientStrings.get(9791), () -> costumeSelected.accept("")).setEnabled(!current.isEmpty()));
        if (!top.totodile.pokemmo.rom.PokemonCostumes.catalogReady()) {
            fashion.add(new MenuAction(ClientStrings.get(top.totodile.pokemmo.rom.PokemonCostumes.catalogFailed() ? 9812 : 9811), () -> { }).setEnabled(false));
            return;
        }
        var costumes = top.totodile.pokemmo.rom.PokemonCostumes.choices(member.species());
        var groups = new java.util.TreeMap<Integer, java.util.List<top.totodile.pokemmo.rom.PokemonCostumes.Costume>>();
        for (var costume : costumes) {
            int other = costume.species() == member.species() ? costume.donor() : costume.species();
            groups.computeIfAbsent(other, ignored -> new java.util.ArrayList<>()).add(costume);
        }
        int index = 0;
        Menu page = null;
        for (var group : groups.entrySet()) {
            if (index % 24 == 0) {
                page = new Menu(ClientStrings.get(9810).replace("{00}", Integer.toString(index + 1))
                        .replace("{01}", Integer.toString(Math.min(index + 24, groups.size()))));
                fashion.add(page);
            }
            index++;
            Menu donor = new Menu(ClientStrings.get(150000 + group.getKey()));
            page.add(donor);
            Menu variants = donor;
            int variant = 0;
            for (var costume : group.getValue()) {
                if (group.getValue().size() > 24 && variant % 24 == 0) {
                    variants = new Menu(ClientStrings.get(9810).replace("{00}", Integer.toString(variant + 1))
                            .replace("{01}", Integer.toString(Math.min(variant + 24, group.getValue().size()))));
                    donor.add(variants);
                }
                variant++;
                String label = ClientStrings.get(150000 + costume.species()) + " × "
                        + ClientStrings.get(150000 + costume.donor()) + " [" + costume.id() + "]";
                var action = new MenuAction(label, () -> costumeSelected.accept(costume.id())).setEnabled(!current.equals(costume.id()));
                if (!costume.artist().isBlank()) action.setTooltipContent(ClientStrings.get(9813).replace("{00}", costume.artist()));
                variants.add(action);
            }
        }
    }

    private void toggle(Menu menu, PokemonConfigPackets.Action action, boolean current) {
        choice(menu, ClientStrings.get(9727), action, 0, 1, !current);
        choice(menu, ClientStrings.get(65), action, 0, 0, current);
    }

    private void presets(Menu menu, PokemonConfigPackets.Action action, int index, int[] values, boolean reset) {
        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            boolean enabled = switch (action) {
                case IVS -> member.individual().ivs().stream().anyMatch(v -> v != value);
                case EVS -> member.individual().evs().stream().anyMatch(v -> v != value);
                case EV -> member.individual().evs().get(index) != value
                        && member.individual().evs().stream().mapToInt(Integer::intValue).sum() - member.individual().evs().get(index) + value <= 510;
                case FRIENDSHIP -> member.individual().friendship() != value;
                default -> true;
            };
            choice(menu, ClientStrings.get(i == 2 && reset ? 9725 : 9721 + i), action, index, value, enabled);
        }
    }

    private static Menu submenu(Menu parent, int label) {
        Menu menu = new Menu(ClientStrings.get(label));
        parent.add(menu);
        return menu;
    }

    private void choice(Menu menu, String label, PokemonConfigPackets.Action action, int index, int value, boolean enabled) {
        menu.add(new MenuAction(label, () -> submit.accept(new PokemonConfigPackets.Request(
                member.individual().id(), action, index, value))).setEnabled(enabled));
    }
}
