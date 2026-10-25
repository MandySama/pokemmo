package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.ToggleButtonModel;

import top.totodile.pokemmo.client.ui.common.PlayerWindow;
import top.totodile.pokemmo.server.game.GameService;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;

import java.util.*;

/** PC PokemonSummaryWindow: official 506x316 frame, six data pages and shared sprites. */
public final class PokemonSummaryWindow extends PlayerWindow {
    private final PokemonController controller;
    private String pokemonId;
    private Pokemon pokemon;
    private int page;
    private boolean back;
    private PopupWindow rarityMenu, rarityChoices, nestedChoices;
    private final Map<Button, Runnable> submenuHover = new LinkedHashMap<>();
    private Button hoveredSubmenu;
    private int previewIndex;
    private final Health happiness = new Health();
    private final Button levelPreview = new Button();
    private final Label effortHelpLabel = new Label();
    private final Button effortHelp = new Button();
    private String currentTrainer = "—";
    private final Widget content =
            new Widget() {
                @Override
                protected void layout() {
                    layoutSummary();
                }
            };
    private final Widget body = new Widget();
    private final Button[] tabs = new Button[9];
    private final List<Widget> rows = new ArrayList<>();
    private final SummaryParticles particles;
    private final PokemonMovesPage movesPage;
    private final PokemonHistoryPage historyPage;
    private final PokemonHonorsPage honorsPage;
    private final Health health = new Health();
    private final Health experience = new Health();
    private final Widget dexIcon =
            new Widget() {
                @Override
                protected void paintWidget(GUI gui) {
                    var image = itemIcon.apply(5431);
                    if (image != null) image.draw(getAnimationState(), getX(), getY(), 24, 24);
                }
            };
    private final Widget nameIcon =
            new Widget() {
                @Override
                protected void paintWidget(GUI gui) {
                    var image = controller.images().atlas("icon_textedit");
                    if (image != null) image.draw(getAnimationState(), getX(), getY(), 16, 16);
                }
            };
    private final java.util.function.IntFunction<de.matthiasmann.twl.renderer.Image> itemIcon;
    private final Label name = new Label(), level = new Label(), held = new Label();
    private final Widget typeBackground =
            new Widget() {
                private de.matthiasmann.twl.renderer.Image source, tinted;
                private int lastType = -1;

                @Override
                protected void applyTheme(ThemeInfo theme) {
                    super.applyTheme(theme);
                    source = theme.getImage("typeImage");
                    lastType = -1;
                }

                @Override
                protected void paintWidget(GUI gui) {
                    if (source == null || pokemon == null || pokemon.egg()) return;
                    int type = controller.dex().backgroundType(pokemon.species());
                    if (type != lastType) {
                        lastType = type;
                        tinted = source.createTintedVersion(typeColor(type));
                    }
                    tinted.draw(getAnimationState(), getX(), getY(), getWidth(), getHeight());
                }
            };
    private final Button portrait =
            new Button() {
                @Override
                protected boolean handleEvent(Event event) {
                    if (event.getType() == Event.Type.MOUSE_BTNUP
                            && event.getMouseButton() == Event.MOUSE_RBUTTON) {
                        openRarityMenu(event.getMouseX(), event.getMouseY());
                        return true;
                    }
                    return super.handleEvent(event);
                }
            };
    private final Widget portraitVisual =
            new Widget() {
                @Override
                protected void paintWidget(GUI gui) {
                    paintPortrait();
                }

                @Override
                protected boolean handleEvent(Event event) {
                    return false;
                }
            };

    private void paintPortrait() {
        if (pokemon == null || controller.images() == null) return;
        var image =
                pokemon.egg()
                        ? controller
                                .images()
                                .icon(pokemon.species(), pokemon.form(), pokemon.gender(), true)
                        : controller
                                .images()
                                .animated(
                                        pokemon.species(),
                                        pokemon.form(),
                                        pokemon.shiny(),
                                        back,
                                        System.nanoTime() / 1e9);
        float scale = pokemon.alpha() ? 2.666667f : 2;
        int w = Math.round(image.getWidth() * scale), h = Math.round(image.getHeight() * scale);
        // TaskCallbackXt0 FBO ground is CAMERA_Y + HEIGHT/2 (7/8 of its height).
        int drawY =
                portrait.getY()
                        + (portrait.getHeight() - image.getHeight() * 2) / 2
                        - Math.round((scale - 2) * image.getHeight() * 7 / 8f);
        int drawX = portrait.getX() + (portrait.getWidth() - w) / 2;
        if (pokemon.alpha() && !pokemon.egg())
            controller
                    .images()
                    .drawAlpha(
                            image,
                            portrait.getAnimationState(),
                            drawX,
                            drawY,
                            w,
                            h,
                            pokemon.shiny());
        else image.draw(portrait.getAnimationState(), drawX, drawY, w, h);
    }

    private final Widget types =
            new Widget() {
                @Override
                protected void paintWidget(GUI gui) {
                    if (pokemon == null || pokemon.egg() || controller.images() == null) return;
                    var species = controller.dex().get(pokemon.species());
                    var first = controller.images().type(species.type1());
                    if (first != null) first.draw(getAnimationState(), getX(), getY(), 49, 14);
                    if (species.type2() != species.type1()) {
                        var second = controller.images().type(species.type2());
                        if (second != null)
                            second.draw(getAnimationState(), getX() + 50, getY(), 49, 14);
                    }
                }
            };
    private final Button[] markButtons = new Button[4];
    private final Widget selectedParticle =
            new Widget() {
                @Override
                protected void paintWidget(GUI gui) {
                    if (pokemon == null
                            || pokemon.egg()
                            || pokemon.honors().selectedParticle() <= 0) return;
                    var image =
                            pokemon.honors().selectedParticle() == 0 && pokemon.secretShiny()
                                    ? controller.images().atlas("secret_shiny_particle")
                                    : controller
                                            .images()
                                            .atlas(
                                                    "particle_id",
                                                    pokemon.honors().selectedParticle());
                    if (image != null) image.draw(getAnimationState(), getX(), getY(), 24, 24);
                }

                @Override
                protected boolean handleEvent(Event event) {
                    return pokemon != null
                            && !pokemon.egg()
                            && pokemon.honors().selectedParticle() > 0
                            && event.isMouseEvent();
                }

                @Override
                protected Object getTooltipContentAt(int x, int y) {
                    return pokemon == null
                                    || pokemon.egg()
                                    || pokemon.honors().selectedParticle() <= 0
                            ? null
                            : PokemonHonorsPage.particleTooltip(
                                    controller,
                                    pokemon.honors().selectedParticle(),
                                    pokemon.secretShiny());
                }
            };
    private final Widget[] statusBadges = new Widget[3];
    private final Widget rarityIcons =
            new Widget() {
                @Override
                protected void layout() {
                    int y = getY();
                    for (int i = 0; i < statusBadges.length; i++) {
                        var badge = statusBadges[i];
                        boolean visible = rarityIcon(i) != null;
                        badge.setVisible(visible);
                        if (visible) {
                            badge.setPosition(getX(), y);
                            badge.setSize(24, 24);
                            y += 24;
                        }
                    }
                }
            };

    private String rarityIcon(int index) {
        if (pokemon == null || pokemon.egg()) return null;
        return switch (index) {
            case 0 -> pokemon.shiny() ? (pokemon.secretShiny() ? "secret_shiny" : "shiny") : null;
            case 1 -> usingHiddenAbility() ? "HiddenAbility" : null;
            default -> pokemon.alpha() ? "alphaLight" : null;
        };
    }

    private String rarityTooltip(int index) {
        return switch (index) {
            case 0 -> controller.strings.get(pokemon.secretShiny() ? 10996 : 5614);
            case 1 -> controller.strings.get(1886);
            default -> controller.strings.format(1887, controller.strings.get(0));
        };
    }

    private final Button lock =
            new Button() {
                private de.matthiasmann.twl.renderer.Image selected, unselected;

                @Override
                protected void paintWidget(GUI gui) {
                    if (pokemon == null || controller.images() == null) return;
                    if (selected == null) {
                        selected = controller.images().atlas("mark", 4);
                        if (selected != null)
                            unselected = selected.createTintedVersion(new Color(0xff404040));
                    }
                    var image = (pokemon.marks() & 16) != 0 ? selected : unselected;
                    if (image != null)
                        image.draw(getAnimationState(), getX() + 4, getY() + 4, 16, 16);
                }
            };
    private final Widget headerIcons =
            new Widget() {
                @Override
                protected void paintWidget(GUI gui) {
                    if (pokemon == null) return;
                    var ball = controller.images().ball(pokemon.ball());
                    if (ball != null)
                        ball.draw(getAnimationState(), getX() + 1, getY() + 6, 16, 16);
                    if (pokemon.gender() >= 0 && !pokemon.egg()) {
                        var gender =
                                controller
                                        .images()
                                        .atlas(
                                                pokemon.gender() == 1
                                                        ? "icon_gender_female"
                                                        : "icon_gender_male");
                        if (gender != null)
                            gender.draw(getAnimationState(), getX() + 155, getY() + 6, 16, 16);
                    }
                }
            };
    private final Widget item =
            new Widget() {
                @Override
                protected void paintWidget(GUI gui) {
                    if (pokemon != null && pokemon.item() != 0) {
                        var image = itemIcon.apply(pokemon.item());
                        if (image != null)
                            image.draw(getAnimationState(), getX() + 6, getY() + 6, 24, 24);
                    }
                }
            };

    public PokemonSummaryWindow(
            Widget owner,
            PokemonController controller,
            java.util.function.IntFunction<de.matthiasmann.twl.renderer.Image> itemIcon,
            Runnable closed) {
        super(owner, false, "/monster-frame", 506, 316, closed);
        this.controller = controller;
        this.itemIcon = itemIcon;
        particles = new SummaryParticles(controller);
        movesPage = new PokemonMovesPage(controller, () -> pokemon);
        historyPage = new PokemonHistoryPage(controller, () -> pokemon);
        honorsPage = new PokemonHonorsPage(controller, () -> pokemon);
        frame.setTitle(controller.strings.get(0) + " " + controller.strings.get(1801));
        content.setTheme("");
        body.setTheme("/label-monster-area");
        portrait.setTheme("");
        portrait.setClip(false);
        types.setTheme("");
        typeBackground.setTheme("/summary-type-background");
        content.add(typeBackground);
        name.setTheme("/monster-frame.nameplate.label-monster-name");
        level.setTheme("/monster-frame.nameplate.label-level");
        held.setTheme("/monster-frame.itemplate.label2");
        String[] themes = {
            "info",
            "stats",
            "stats2",
            "stats3",
            "attack",
            "history",
            "stats4",
            "ribbons",
            "particles"
        };
        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            var button = new Button();
            button.setModel(new ToggleButtonModel());
            button.setTheme("/monster-frame-tab-" + themes[i]);
            button.setCanAcceptKeyboardFocus(false);
            button.addCallback(() -> showPage(index));
            tabs[i] = button;
            content.add(button);
        }
        item.setTheme("");
        health.setTheme("/monsterframe-hp-progressbar");
        happiness.setTheme("/monsterframe-happiness-progressbar");
        levelPreview.setTheme("/summary-level-toggle");
        levelPreview.setTooltipContent(controller.strings.get(1858));
        levelPreview.setTooltipDelay(100);
        levelPreview.setCanAcceptKeyboardFocus(false);
        levelPreview.addCallback(
                () -> {
                    previewIndex = (previewIndex + 1) % 7;
                    showPage(1);
                });
        experience.setTheme("/monsterframe-xp-progressbar");
        dexIcon.setTheme("");
        nameIcon.setTheme("");
        content.add(body);
        content.add(name);
        content.add(headerIcons);
        content.add(level);
        content.add(particles);
        content.add(portrait);
        content.add(rarityIcons);
        content.add(lock);
        content.add(types);
        selectedParticle.setTheme("");
        content.add(selectedParticle);
        content.add(held);
        content.add(item);
        frame.add(content);
        int[] markColors = {0xffffb3ba, 0xffffffba, 0xffbaffc9, 0xffbae1ff};
        for (int i = 0; i < markButtons.length; i++) {
            final int index = i;
            var button =
                    new Button() {
                        private de.matthiasmann.twl.renderer.Image selected, unselected;

                        @Override
                        protected void paintWidget(GUI gui) {
                            if (pokemon == null || pokemon.egg() || controller.images() == null)
                                return;
                            if (selected == null) {
                                var source = controller.images().atlas("mark", index);
                                if (source != null) {
                                    selected =
                                            source.createTintedVersion(
                                                    new Color(markColors[index]));
                                    unselected = source.createTintedVersion(new Color(0xff404040));
                                }
                            }
                            var image =
                                    (pokemon.marks() & (1 << index)) != 0 ? selected : unselected;
                            if (image != null)
                                image.draw(getAnimationState(), getX(), getY(), 16, 16);
                        }
                    };
            button.setTheme("");
            button.setCanAcceptKeyboardFocus(false);
            button.addCallback(
                    () -> {
                        if (pokemon != null && !pokemon.egg() && !controller.busy())
                            controller.setMarks(pokemonId, pokemon.marks() ^ (1 << index));
                    });
            markButtons[i] = button;
            content.add(button);
        }
        portrait.setCanAcceptKeyboardFocus(false);
        portrait.addCallback(
                () -> {
                    if (pokemon != null && !pokemon.egg()) back = !back;
                });
        effortHelpLabel.setTheme("/label");
        effortHelpLabel.setText(controller.strings.get(1838));
        effortHelp.setTheme("/summary-help-button");
        effortHelp.setCanAcceptKeyboardFocus(false);
        effortHelp.setTooltipContent(controller.strings.get(1839));
        effortHelp.setTooltipDelay(0);
        for (int i = 0; i < statusBadges.length; i++) {
            final int index = i;
            var badge =
                    new Widget() {
                        @Override
                        protected void paintWidget(GUI gui) {
                            String icon = rarityIcon(index);
                            if (icon != null) {
                                var image = controller.images().atlas(icon);
                                if (image != null)
                                    image.draw(getAnimationState(), getX(), getY(), 24, 24);
                            }
                        }

                        @Override
                        protected boolean handleEvent(Event event) {
                            return event.isMouseEvent();
                        }

                        @Override
                        protected Object getTooltipContentAt(int x, int y) {
                            return rarityIcon(index) == null ? null : rarityTooltip(index);
                        }
                    };
            badge.setTheme("");
            badge.setTooltipDelay(0);
            statusBadges[i] = badge;
            rarityIcons.add(badge);
        }
        selectedParticle.setTooltipDelay(0);
        lock.setTooltipContent("锁定标记");
        lock.setTooltipDelay(0);
        rarityIcons.setTheme("");
        lock.setTheme("");
        lock.setCanAcceptKeyboardFocus(false);
        lock.addCallback(
                () -> {
                    if (pokemon != null && !controller.busy())
                        controller.setMarks(pokemonId, pokemon.marks() ^ 16);
                });
        portraitVisual.setTheme("");
        portraitVisual.setClip(false);
        add(portraitVisual);
        controller.onChanged(this::update);
    }

    public boolean shows(String id) {
        return isOpen() && id.equals(pokemonId);
    }

    public void cascadeFrom(PokemonSummaryWindow previous) {
        int x = previous.frame.getX() + 28, y = previous.frame.getY() + 48;
        if (x > 0
                && y > 0
                && x <= getRight() - frame.getWidth()
                && y <= getBottom() - frame.getHeight()) frame.setPosition(x, y);
    }

    public void show(Pokemon value, String trainer) {
        currentTrainer = trainer == null ? "—" : trainer;
        show(value);
    }

    public void show(Pokemon value) {
        closeRarityMenu();
        if (!controller.ready()) return;
        pokemonId = value.id();
        pokemon = value;
        page = 0;
        back = false;
        previewIndex = 0;
        particles.reset();
        if (isOpen()) {
            raiseWindow();
            update();
        } else open();
    }

    @Override
    protected void refresh() {
        update();
    }

    private void update() {
        if (!isOpen() || pokemonId == null) return;
        var current =
                controller.state().pokemons().stream()
                        .filter(p -> p.id().equals(pokemonId))
                        .findFirst()
                        .orElse(null);
        if (current == null) {
            closePopup();
            return;
        }
        pokemon = current;
        name.setText(controller.name(pokemon));
        level.setText(
                controller.strings.format(
                        1731, pokemon.egg() ? "???" : Integer.toString(pokemon.level())));
        String item =
                pokemon.item() == 0
                        ? controller.strings.get(61)
                        : pokemon.item() > 5000 && pokemon.item() < 5627
                                ? controller.dex().item(pokemon.item() - 5000)
                                : controller.strings.getOrDefault(100000 + pokemon.item(), "—");
        held.setText(controller.strings.get(1842) + " " + item);
        showPage(pokemon.egg() && page != 0 && page != 5 ? 0 : page);
    }

    private String originalTrainer() {
        String trainer = pokemon.origin().trainer();
        if (trainer.isEmpty()) return currentTrainer;
        if (trainer.equals("{STRING_5685}")) return controller.strings.get(5685);
        if (trainer.equals("{STRING_5684}")) return controller.strings.get(5684);
        return trainer;
    }

    private Label label(String text, String theme) {
        var value = new Label(text);
        value.setTheme("/label-monster-area." + theme);
        return value;
    }

    private void pair(int title, String value) {
        pair(controller.strings.get(title), value, false);
    }

    private void pair(String title, String value, boolean complete) {
        var key = label(title, "label-monster-title");
        var field =
                label(
                        value,
                        complete ? "label-monster-value-stat-complete" : "label-monster-value");
        body.add(key);
        body.add(field);
        rows.add(key);
        rows.add(field);
    }

    private void showPage(int selected) {
        page = selected;
        body.removeAllChildren();
        rows.clear();
        for (int i = 0; i < tabs.length; i++) {
            ((ToggleButtonModel) tabs[i].getModel()).setSelected(i == page);
            tabs[i].setEnabled(!pokemon.egg() || i == 0 || i == 5);
        }
        var strings = controller.strings;
        var dex = controller.dex();
        switch (page) {
            case 0 -> {
                pair(
                        (strings.get(1).contains(strings.get(0))
                                        ? strings.get(1)
                                        : strings.get(0) + strings.get(1))
                                + ":",
                        pokemon.egg()
                                ? "???"
                                : String.format(Locale.ROOT, "%03d", pokemon.species()),
                        false);
                pair(1803, controller.name(pokemon));
                pair(
                        1805,
                        pokemon.egg()
                                ? "???"
                                : strings.getOrDefault(
                                        180000 + pokemon.nature(), dex.nature(pokemon.nature())));
                pair(1807, originalTrainer());
                boolean maximum = !pokemon.egg() && pokemon.level() == 100;
                int base = dex.experienceAtLevel(pokemon.species(), pokemon.level());
                int next = dex.experienceAtLevel(pokemon.species(), pokemon.level() + 1);
                int current = pokemon.experience();
                var format = java.text.NumberFormat.getIntegerInstance(Locale.US);
                pair(
                        1810,
                        pokemon.egg()
                                ? "???"
                                : current < 0 ? "—" : format.format(maximum ? base : current));
                pair(
                        1829,
                        pokemon.egg()
                                ? "???"
                                : maximum
                                        ? "-"
                                        : current < 0
                                                ? "—"
                                                : next - current < 1
                                                        ? strings.get(1879)
                                                        : format.format(next - current));
                if (!pokemon.egg() && current >= 0) {
                    if (!maximum && next - current < 1)
                        ((Label) rows.get(9)).setText(format.format(next - base) + "+");
                    rows.get(9)
                            .setTooltipContent(
                                    strings.format(
                                            1882,
                                            format.format(current),
                                            format.format(dex.experienceAt100(pokemon.species()))));
                    rows.get(11)
                            .setTooltipContent(
                                    !maximum && next - current < 1
                                            ? strings.get(1880)
                                            : (current - base)
                                                    + " / "
                                                    + format.format(next - base));
                }
                for (int index : new int[] {1, 3})
                    rows.get(index).setTheme("/label-monster-area.summary-info-value-icon");
                experience.fraction =
                        pokemon.egg()
                                ? 0
                                : maximum
                                        ? 1
                                        : current < 0
                                                ? 0
                                                : Math.max(
                                                        0,
                                                        Math.min(
                                                                1,
                                                                (float) (current - base)
                                                                        / (next - base)));
                body.add(experience);
                body.add(dexIcon);
                body.add(nameIcon);
            }
            case 1 -> {
                int[] ids = {1809, 1812, 1813, 1814, 1815, 1816};
                int target = new int[] {0, 5, 50, 55, 65, 80, 100}[previewIndex];
                var stats = target == 0 ? pokemon.stats() : previewStats(target);
                String suffix = target == 0 ? "" : strings.format(1859, Integer.toString(target));
                pair(
                        1809,
                        target == 0
                                ? pokemon.hp() + " / " + pokemon.maxHp()
                                : stats.get(0) + suffix);
                pair(1811, (pokemon.friendship() * 100 / 255) + "%");
                for (int i = 1; i < 6; i++) pair(ids[i], stats.get(i) + suffix);
                health.fraction = (float) pokemon.hp() / pokemon.maxHp();
                String theme =
                        health.fraction <= .2
                                ? "/monsterframe-hp-progressbar-red"
                                : health.fraction <= .5
                                        ? "/monsterframe-hp-progressbar-orange"
                                        : "/monsterframe-hp-progressbar";
                health.setTheme(theme);
                body.add(health);
                health.reapplyTheme();
                happiness.fraction = pokemon.friendship() / 255f;
                body.add(happiness);
                body.add(levelPreview);
            }
            case 2, 3 -> {
                var values = page == 2 ? pokemon.evs() : pokemon.ivs();
                int base = page == 2 ? 1817 : 1830, limit = page == 2 ? 252 : 31;
                for (int i = 0; i < 6; i++) {
                    pair(
                            strings.get(base + i),
                            Integer.toString(values.get(i)),
                            values.get(i) == limit);
                    if (page == 3 && values.get(i) == 0)
                        rows.get(rows.size() - 1)
                                .setTheme("/label-monster-area.label-monster-value-stat-bad");
                }
                int total = values.stream().mapToInt(Integer::intValue).sum(),
                        max = page == 2 ? 510 : 186;
                pair(strings.get(1726), total + " / " + max, total == max);
                if (page == 3) {
                    for (int i = 0; i < 6; i++) {
                        if (values.get(i) == 31)
                            rows.get(i * 2 + 1).setTheme("/label-monster-area.summary-iv-complete");
                        else if (values.get(i) == 0)
                            rows.get(i * 2 + 1).setTheme("/label-monster-area.summary-iv-bad");
                    }
                    if (total == 186 || total == 0)
                        rows.get(13)
                                .setTheme(
                                        total == 186
                                                ? "/label-monster-area.summary-iv-complete"
                                                : "/label-monster-area.summary-iv-bad");
                }
                effortHelpLabel.setText(strings.get(page == 2 ? 1838 : 1836));
                effortHelp.setTooltipContent(strings.get(page == 2 ? 1839 : 1837));
                body.add(effortHelpLabel);
                body.add(effortHelp);
            }
            case 4 -> {
                body.add(movesPage);
                movesPage.refresh();
            }
            case 5 -> {
                body.add(historyPage);
                historyPage.refresh(currentTrainer);
            }
            case 6, 7, 8 -> {
                body.add(honorsPage);
                honorsPage.refresh(page);
            }
            default -> throw new IllegalStateException("Unknown Pokemon summary page");
        }
        if (!pokemon.egg()) particles.type(controller.dex().backgroundType(pokemon.species()));
        particles.setVisible(!pokemon.egg());
        typeBackground.setVisible(true);
        rarityIcons.invalidateLayout();
        content.invalidateLayout();
    }

    private void layoutSummary() {
        // PokemonSummaryWindow.K8 uses fixed coordinates on the PC frame.
        int x = frame.getX(), y = frame.getY();
        portraitVisual.setPosition(getX(), getY());
        portraitVisual.setSize(getWidth(), getHeight());
        rarityIcons.setPosition(x + 476, y + 58);
        rarityIcons.setSize(24, 72);
        int count =
                pokemon == null || pokemon.egg()
                        ? 0
                        : (pokemon.shiny() ? 1 : 0)
                                + (usingHiddenAbility() ? 1 : 0)
                                + (pokemon.alpha() ? 1 : 0);
        lock.setPosition(x + 476, y + 58 + count * 24);
        lock.setSize(24, 24);
        particles.setPosition(x + 275, y + 35);
        particles.setSize(224, 227);
        typeBackground.setPosition(x + 274, y + 33);
        typeBackground.setSize(232, 231);
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setPosition(x + 12 + i * 26, y + 10);
            tabs[i].setSize(20, 20);
        }
        body.setPosition(x + 10, y + 43);
        body.setSize(250, 258);
        int top = page <= 3 ? y + 42 : body.getY() + 5;
        for (int i = 0; i < rows.size(); i += 2) {
            rows.get(i).setPosition(body.getX() + 3, top);
            rows.get(i).setSize(page <= 3 ? 105 : 116, (page == 2 || page == 3) ? 26 : 25);
            rows.get(i + 1).setPosition(page <= 3 ? x + 120 : body.getX() + 121, top);
            rows.get(i + 1).setSize(page <= 3 ? 125 : 122, (page == 2 || page == 3) ? 26 : 25);
            top += (page == 2 || page == 3) ? 32 : 30;
            if (page == 1 && (i == 0 || i == 2)) {
                var bar = i == 0 ? health : happiness;
                bar.setPosition(body.getX() + 7, top);
                bar.setSize(230, 8);
                top += 13;
            }
        }
        if (page == 2 || page == 3) {
            effortHelpLabel.setPosition(x + 30, y + 268);
            effortHelpLabel.setSize(effortHelpLabel.getPreferredWidth(), 22);
            effortHelp.setPosition(effortHelpLabel.getRight() + 4, y + 271);
            effortHelp.setSize(16, 16);
        }
        if (page >= 6) {
            honorsPage.setPosition(x + 16, y + 42);
            honorsPage.setSize(244, 266);
        }
        if (page == 5) {
            historyPage.setPosition(x + 16, y + 42);
            historyPage.setSize(244, 262);
        }
        if (page == 4) {
            movesPage.setPosition(x + 12, y + 42);
            movesPage.setSize(246, 270);
        }
        if (page == 1) {
            levelPreview.setPosition(x + 229, y + 281);
            levelPreview.setSize(16, 16);
        }
        if (page == 0) {
            experience.setPosition(x + 16, top);
            experience.setSize(229, 8);
            dexIcon.setPosition(x + 219, y + 42);
            dexIcon.setSize(24, 24);
            nameIcon.setPosition(x + 223, y + 76);
            nameIcon.setSize(16, 16);
        }
        name.setPosition(x + 295, y + 35);
        name.setSize(129, 25);
        level.setPosition(x + 446, y + 35);
        level.setSize(55, 25);
        headerIcons.setPosition(x + 276, y + 35);
        headerIcons.setSize(171, 25);
        portrait.setPosition(x + 275, y + 59);
        portrait.setSize(222, 181);
        types.setPosition(x + 277, y + 246);
        types.setSize(99, 16);
        boolean hasTypes = pokemon != null && !pokemon.egg();
        boolean dual =
                hasTypes
                        && controller.dex().get(pokemon.species()).type1()
                                != controller.dex().get(pokemon.species()).type2();
        for (int i = 0; i < markButtons.length; i++) {
            var button = markButtons[i];
            button.setVisible(hasTypes);
            button.setPosition(x + 277 + (dual ? 100 : 50) + 2 + i * 16, y + 245);
            button.setSize(16, 16);
        }
        selectedParticle.setPosition(x + 476, y + 233);
        selectedParticle.setSize(24, 24);
        held.setPosition(x + 288, y + 273);
        held.setSize(166, 26);
        item.setPosition(x + 464, y + 269);
        item.setSize(36, 36);
    }

    private List<Integer> previewStats(int level) {
        var base = controller.dex().get(pokemon.species()).stats();
        var values = new ArrayList<Integer>();
        int[] natureOrder = {1, 2, 5, 3, 4};
        int up = natureOrder[pokemon.nature() / 5], down = natureOrder[pokemon.nature() % 5];
        for (int i = 0; i < 6; i++) {
            int value =
                    ((2 * base.get(i) + pokemon.ivs().get(i) + pokemon.evs().get(i) / 4) * level)
                                    / 100
                            + (i == 0 ? level + 10 : 5);
            if (i == 0 && pokemon.species() == 292) value = 1;
            if (i != 0 && up != down) value = (int) (value * (i == up ? 1.1 : i == down ? .9 : 1));
            values.add(value);
        }
        return values;
    }

    @Override
    protected boolean handleFrameEvent(Event event) {
        if (event.isKeyPressedEvent()
                && (event.getKeyCode() == Event.KEY_LEFT
                        || event.getKeyCode() == Event.KEY_RIGHT)) {
            int next = page;
            do {
                next =
                        Math.floorMod(
                                next + (event.getKeyCode() == Event.KEY_LEFT ? -1 : 1),
                                tabs.length);
            } while (!tabs[next].isEnabled());
            showPage(next);
            return true;
        }
        return false;
    }

    @Override
    public void closePopup() {
        closeRarityMenu();
        super.closePopup();
        pokemonId = null;
        pokemon = null;
    }

    private boolean usingHiddenAbility() {
        if (pokemon == null || !pokemon.hiddenAbility() || controller.dex() == null) return false;
        var abilities = controller.dex().get(pokemon.species()).abilities();
        return abilities.size() > 2
                && abilities.get(2) > 0
                && pokemon.ability() == abilities.get(2);
    }

    private void closeRarityMenu() {
        submenuHover.clear();
        hoveredSubmenu = null;
        if (nestedChoices != null) {
            nestedChoices.closePopup();
            nestedChoices = null;
        }
        if (rarityChoices != null) {
            rarityChoices.closePopup();
            rarityChoices = null;
        }
        if (rarityMenu != null) {
            rarityMenu.closePopup();
            rarityMenu = null;
        }
    }

    private void openRarityMenu(int x, int y) {
        if (pokemon == null || controller.busy()) return;
        closeRarityMenu();
        rarityMenu = new PopupWindow(portrait);
        rarityMenu.setTheme("/pokemon-menu");
        rarityMenu.setRequestCloseCallback(this::closeRarityMenu);
        rarityMenu.setCloseAndIgnoreMouseDownOutside(true);
        rarityMenu.setRouteToParentPopup(true);
        var buttons = new ArrayList<Button>();
        var flags =
                new top.totodile.pokemmo.server.game.GameService.PokemonFlag[] {
                    top.totodile.pokemmo.server.game.GameService.PokemonFlag.SHINY,
                    top.totodile.pokemmo.server.game.GameService.PokemonFlag.ALPHA
                };
        int[] labels = {8011, 8119};
        for (int i = 0; i < flags.length; i++) {
            final var flag = flags[i];
            var button = new Button(controller.strings.get(labels[i]));
            button.setTheme("/pokemon-menu-button");
            button.setCanAcceptKeyboardFocus(false);
            submenuHover.put(button, () -> openRarityChoices(button, flag));
            buttons.add(button);
        }
        GameService.PokemonSetting[] settings = {
            GameService.PokemonSetting.GENDER,
            GameService.PokemonSetting.ABILITY,
            GameService.PokemonSetting.NATURE,
            GameService.PokemonSetting.PARTICLE,
            GameService.PokemonSetting.IV,
            GameService.PokemonSetting.EV,
            GameService.PokemonSetting.FRIENDSHIP,
            GameService.PokemonSetting.BALL,
            GameService.PokemonSetting.RIBBON
        };
        int[] settingLabels = {8100, 1824, 5585, 3057, 1849, 1800, 1811, 2, 1847};
        for (int i = 0; i < settings.length; i++) {
            final var setting = settings[i];
            var button = menuButton(controller.strings.get(settingLabels[i]), () -> {});
            submenuHover.put(button, () -> openSettingMenu(button, setting));
            buttons.add(button);
        }
        for (var setting :
                new GameService.PokemonSetting[] {
                    GameService.PokemonSetting.LEVEL,
                    GameService.PokemonSetting.STATUS,
                    GameService.PokemonSetting.EVOLUTION
                }) {
            var button =
                    menuButton(
                            setting == GameService.PokemonSetting.LEVEL
                                    ? "设置等级"
                                    : setting == GameService.PokemonSetting.STATUS ? "状态" : "进化链",
                            () -> {});
            button.setEnabled(
                    !pokemon.egg()
                            && (setting != GameService.PokemonSetting.EVOLUTION
                                    || controller.dex().evolutionFamily(pokemon.species()).size()
                                            > 1));
            submenuHover.put(
                    button,
                    () -> {
                        if (button.isEnabled()) openSettingMenu(button, setting);
                    });
            buttons.add(button);
        }
        var ot = menuButton("设置初训", () -> {});
        submenuHover.put(ot, () -> openSettingMenu(ot, GameService.PokemonSetting.OT));
        buttons.add(buttons.size() - 1, ot);
        var content = new ScrollPane(menuRows(buttons));
        content.setTheme("/scrollpane");
        content.setFixed(ScrollPane.Fixed.HORIZONTAL);
        rarityMenu.add(content);
        if (rarityMenu.openPopup()) {
            int height =
                    Math.min(
                            getHeight() - 20,
                            buttons.size() * 31
                                    + rarityMenu.getBorderVertical()
                                    + content.getBorderVertical());
            rarityMenu.setPosition(
                    Math.max(getX(), Math.min(x, getRight() - 120)),
                    Math.max(getY(), Math.min(y, getBottom() - height)));
            rarityMenu.setSize(120, height);
        }
    }

    private Widget menuRows(List<Button> buttons) {
        return new Widget() {
            {
                setTheme("");
                for (var button : buttons) add(button);
            }

            @Override
            public int getPreferredHeight() {
                return buttons.size() * 31;
            }

            @Override
            protected void layout() {
                for (int i = 0; i < buttons.size(); i++) {
                    buttons.get(i).setPosition(getInnerX(), getInnerY() + i * 31);
                    buttons.get(i).setSize(getInnerWidth(), 31);
                }
            }
        };
    }

    private void openRarityChoices(
            Button owner, top.totodile.pokemmo.server.game.GameService.PokemonFlag flag) {
        if (nestedChoices != null) {
            nestedChoices.closePopup();
            nestedChoices = null;
        }
        if (rarityChoices != null) rarityChoices.closePopup();
        rarityChoices = new PopupWindow(owner);
        rarityChoices.setTheme("/pokemon-menu");
        rarityChoices.setRequestCloseCallback(this::closeRarityMenu);
        rarityChoices.setCloseAndIgnoreMouseDownOutside(true);
        rarityChoices.setRouteToParentPopup(true);
        var buttons = new ArrayList<Button>();
        boolean current =
                switch (flag) {
                    case SHINY -> pokemon.shiny();
                    case SECRET_SHINY -> pokemon.secretShiny();
                    case HIDDEN_ABILITY -> pokemon.hiddenAbility();
                    case ALPHA -> pokemon.alpha();
                };
        for (boolean value : new boolean[] {true, false}) {
            var button = new Button(Boolean.toString(value));
            button.setTheme("/pokemon-menu-button");
            button.setEnabled(current != value);
            button.setCanAcceptKeyboardFocus(false);
            button.addCallback(
                    () -> {
                        String id = pokemonId;
                        closeRarityMenu();
                        controller.setFlag(id, flag, value);
                    });
            buttons.add(button);
        }
        if (flag == GameService.PokemonFlag.SHINY)
            for (boolean value : new boolean[] {true, false}) {
                var button =
                        menuButton(
                                "Secret " + value,
                                () -> {
                                    String id = pokemonId;
                                    closeRarityMenu();
                                    controller.setFlag(
                                            id, GameService.PokemonFlag.SECRET_SHINY, value);
                                });
                button.setEnabled(pokemon.secretShiny() != value);
                buttons.add(button);
            }
        rarityChoices.add(menuRows(buttons));
        if (rarityChoices.openPopup()) {
            int height = buttons.size() * 31 + rarityChoices.getBorderVertical();
            rarityChoices.setPosition(
                    Math.min(owner.getRight(), getRight() - 100),
                    Math.min(owner.getY(), getBottom() - height));
            rarityChoices.setSize(100, height);
        }
    }

    private Button menuButton(String text, Runnable action) {
        var button = new Button(text);
        button.setTheme("/pokemon-menu-button");
        button.setCanAcceptKeyboardFocus(false);
        button.addCallback(action);
        return button;
    }

    private void applySetting(GameService.PokemonSetting setting, int index, int value) {
        String id = pokemonId;
        closeRarityMenu();
        controller.setSetting(id, setting, index, value);
    }

    public void hoverMenus(int x, int y) {
        if (rarityMenu == null || !rarityMenu.isOpen()) return;
        if (rarityChoices != null
                && rarityMenu.isInside(x, y)
                && !rarityChoices.isInside(x, y)
                && (nestedChoices == null || !nestedChoices.isInside(x, y))) {
            Widget target = rarityMenu.getGUI().getWidgetAt(x, y);
            while (target != null && !(target instanceof Button)) target = target.getParent();
            if (target != null && target != rarityChoices.getOwner()) {
                if (nestedChoices != null) {
                    nestedChoices.closePopup();
                    nestedChoices = null;
                }
                rarityChoices.closePopup();
                rarityChoices = null;
                hoveredSubmenu = null;
            }
        }
        submenuHover.entrySet().removeIf(entry -> entry.getKey().getGUI() == null);
        for (var entry : List.copyOf(submenuHover.entrySet())) {
            var button = entry.getKey();
            if (!button.isInside(x, y)) continue;
            if (nestedChoices != null
                    && nestedChoices.isInside(x, y)
                    && !descendant(button, nestedChoices)) continue;
            if (rarityChoices != null
                    && rarityChoices.isInside(x, y)
                    && !descendant(button, rarityChoices)) continue;
            boolean clipped = false;
            for (Widget parent = button.getParent(); parent != null; parent = parent.getParent())
                if (parent instanceof ScrollPane && !parent.isInside(x, y)) {
                    clipped = true;
                    break;
                }
            if (!clipped && button != hoveredSubmenu) {
                hoveredSubmenu = button;
                entry.getValue().run();
            }
            if (!clipped) return;
        }
        if (rarityChoices != null
                && rarityChoices.isInside(x, y)
                && (nestedChoices == null || !nestedChoices.isInside(x, y))) {
            if (nestedChoices != null) {
                nestedChoices.closePopup();
                nestedChoices = null;
            }
            hoveredSubmenu = null;
        }
    }

    private boolean descendant(Widget child, Widget parent) {
        for (var node = child; node != null; node = node.getParent())
            if (node == parent) return true;
        return false;
    }

    private void showOptions(Button owner, List<Button> buttons, int width) {
        int x = owner.getRight(), y = owner.getY();
        boolean nested = rarityChoices != null && descendant(owner, rarityChoices);
        if (x + width > getRight()) x = Math.max(getX(), owner.getX() - width);
        if (nestedChoices != null) {
            nestedChoices.closePopup();
            nestedChoices = null;
        }
        if (!nested && rarityChoices != null) rarityChoices.closePopup();
        var popup = new PopupWindow(owner);
        popup.setTheme("/pokemon-menu");
        popup.setRequestCloseCallback(this::closeRarityMenu);
        popup.setCloseAndIgnoreMouseDownOutside(true);
        popup.setRouteToParentPopup(true);
        var scroll = new ScrollPane(menuRows(buttons));
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        scroll.setTheme("/scrollpane");
        popup.add(scroll);
        if (nested) nestedChoices = popup;
        else rarityChoices = popup;
        if (popup.openPopup()) {
            int height =
                    Math.min(
                            getHeight() - 20,
                            buttons.size() * 31
                                    + popup.getBorderVertical()
                                    + scroll.getBorderVertical());
            popup.setPosition(
                    Math.max(getX(), x), Math.max(getY(), Math.min(y, getBottom() - height)));
            popup.setSize(width, height);
        }
    }

    private void openSettingMenu(Button owner, GameService.PokemonSetting setting) {
        var buttons = new ArrayList<Button>();
        var dex = controller.dex();
        switch (setting) {
            case STATUS -> {
                int[] values = {0, -1, 7, 8, 16, 32, 64, -128};
                int[] labels = {1020, 5211, 5220, 5221, 5222, 5223, 5224, 5225};
                int current = pokemon.hp() == 0 ? -1 : (byte) pokemon.status();
                if (current > 0 && (current & 7) != 0) current = 7;
                for (int i = 0; i < values.length; i++) {
                    final int value = values[i];
                    var button =
                            menuButton(
                                    controller.strings.get(labels[i]),
                                    () -> applySetting(setting, 0, value));
                    button.setEnabled(current != value);
                    buttons.add(button);
                }
            }
            case LEVEL -> {
                for (int i = 1; i <= 100; i++) {
                    final int value = i;
                    var button =
                            menuButton(Integer.toString(i), () -> applySetting(setting, 0, value));
                    button.setEnabled(i != pokemon.level());
                    buttons.add(button);
                }
            }
            case EVOLUTION -> {
                for (int species : dex.evolutionFamily(pokemon.species())) {
                    var button =
                            menuButton(dex.name(species), () -> applySetting(setting, 0, species));
                    button.setEnabled(species != pokemon.species());
                    buttons.add(button);
                }
            }
            case OT -> {
                String[] values = {"{STRING_5685}", "{STRING_5684}", "unknown", currentTrainer};
                String[] names = {
                    controller.strings.get(5685),
                    controller.strings.get(5684),
                    "Unknown OT",
                    currentTrainer
                };
                for (int i = 0; i < values.length; i++) {
                    final int value = i;
                    var button = menuButton(names[i], () -> applySetting(setting, 0, value));
                    button.setEnabled(!values[i].equals(pokemon.origin().trainer()));
                    buttons.add(button);
                }
            }
            case BALL -> {
                for (int i = 0; i < 25; i++) {
                    final int ball = i;
                    int item = i < 16 ? i + 1 : i < 24 ? i + 476 : 576;
                    String text =
                            i < 16
                                    ? controller.strings.getOrDefault(248001 + i, dex.item(item))
                                    : dex.item(item);
                    var button =
                            new Button(text) {
                                @Override
                                protected void paintWidget(GUI gui) {
                                    super.paintWidget(gui);
                                    var image = controller.images().ball(ball);
                                    if (image != null)
                                        image.draw(
                                                getAnimationState(),
                                                getX() + 8,
                                                getY() + (getHeight() - 16) / 2,
                                                16,
                                                16);
                                }
                            };
                    button.setTheme("/pokemon-ball-menu-button");
                    button.setCanAcceptKeyboardFocus(false);
                    button.setEnabled(i != pokemon.ball());
                    button.addCallback(() -> applySetting(setting, 0, ball));
                    buttons.add(button);
                }
            }
            case IV -> {
                int[] values = {31, 15, 0, -1};
                String[] names = {"Max", "Half", "Zero", "Random"};
                for (int i = 0; i < values.length; i++) {
                    final int value = values[i];
                    buttons.add(menuButton(names[i], () -> applySetting(setting, -1, value)));
                }
            }
            case FRIENDSHIP -> {
                int[] values = {255, 127, 0};
                String[] names = {"Max", "Half", "Zero"};
                for (int i = 0; i < values.length; i++) {
                    final int value = values[i];
                    var button = menuButton(names[i], () -> applySetting(setting, 0, value));
                    button.setEnabled(pokemon.friendship() != value);
                    buttons.add(button);
                }
            }
            case PARTICLE -> {
                var all = menuButton("<ADD ALL>", () -> applySetting(setting, -1, 0));
                all.setEnabled(pokemon.honors().particles().stream().distinct().count() < 39);
                buttons.add(all);
                for (int i = 0; i <= 38; i++) {
                    final int id = i;
                    var button =
                            menuButton(
                                    controller.strings.get(10800 + i),
                                    () -> applySetting(setting, 0, id));
                    button.setEnabled(!pokemon.honors().particles().contains(i));
                    buttons.add(button);
                }
            }
            case RIBBON -> {
                var all = menuButton("<ADD ALL>", () -> applySetting(setting, -2, 0));
                all.setEnabled(
                        !java.util.Arrays.stream(PokemonHonorsPage.RIBBONS)
                                        .allMatch(r -> pokemon.honors().ribbons().contains(r[0]))
                                || pokemon.honors().contestRibbons().stream().anyMatch(v -> v < 4));
                buttons.add(all);
                buttons.add(menuButton("Clear", () -> applySetting(setting, -1, 0)));
                for (int[] ribbon : PokemonHonorsPage.RIBBONS) {
                    var button =
                            menuButton(
                                    dex.ribbonText(ribbon[3]),
                                    () -> applySetting(setting, 0, ribbon[0]));
                    button.setEnabled(!pokemon.honors().ribbons().contains(ribbon[0]));
                    buttons.add(button);
                }
                for (int rank = 0; rank < 4; rank++)
                    for (int category = 0; category < 5; category++) {
                        final int c = category, r = rank;
                        var button =
                                menuButton(
                                        dex.ribbonText(category * 4 + 2 + rank),
                                        () -> applySetting(setting, c + 1, r + 1));
                        button.setEnabled(
                                pokemon.honors().contestRibbons().get(category) < rank + 1);
                        buttons.add(button);
                    }
            }
            case NATURE -> {
                for (int n = 0; n < 25; n++) {
                    final int value = n;
                    var button =
                            menuButton(
                                    controller.strings.getOrDefault(180000 + n, dex.nature(n)),
                                    () -> applySetting(setting, 0, value));
                    button.setEnabled(n != pokemon.nature());
                    buttons.add(button);
                }
            }
            case GENDER -> {
                int ratio = dex.extra(pokemon.species()).genderRatio();
                for (int n = 0; n < 2; n++) {
                    final int value = n;
                    var button =
                            menuButton(
                                    controller.strings.get(8101 + n),
                                    () -> applySetting(setting, 0, value));
                    button.setEnabled(
                            n != pokemon.gender()
                                    && ratio != 255
                                    && (ratio != 0 || n == 0)
                                    && (ratio != 254 || n == 1));
                    buttons.add(button);
                }
            }
            case ABILITY -> {
                var abilities = dex.get(pokemon.species()).abilities();
                var seen = new HashSet<Integer>();
                for (int n = 0; n < abilities.size(); n++) {
                    final int slot = n;
                    int value = abilities.get(n);
                    if (value <= 0 || n < 2 && !seen.add(value)) continue;
                    var button =
                            menuButton(
                                    dex.ability(value), () -> applySetting(setting, slot, value));
                    button.setEnabled(
                            value != pokemon.ability() || usingHiddenAbility() != (slot == 2));
                    button.setTooltipContent(dex.abilityDescription(value));
                    buttons.add(button);
                }
            }
            case EV -> {
                buttons.add(menuButton("Max", () -> applySetting(setting, -1, 85)));
                buttons.add(menuButton("Half", () -> applySetting(setting, -1, 42)));
                buttons.add(menuButton("Reset", () -> applySetting(setting, -1, 0)));
                int[] titles = {1809, 1812, 1813, 1814, 1815, 1816};
                for (int i = 0; i < 6; i++) {
                    final int stat = i;
                    var button =
                            menuButton(
                                    controller.strings.get(titles[i]).replace(":", ""), () -> {});
                    submenuHover.put(
                            button,
                            () -> {
                                var options = new ArrayList<Button>();
                                options.add(
                                        menuButton("Max", () -> applySetting(setting, stat, 252)));
                                options.add(
                                        menuButton("Half", () -> applySetting(setting, stat, 126)));
                                options.add(
                                        menuButton("Zero", () -> applySetting(setting, stat, 0)));
                                showOptions(button, options, 110);
                            });
                    buttons.add(button);
                }
            }
        }
        showOptions(
                owner,
                buttons,
                setting == GameService.PokemonSetting.NATURE
                                || setting == GameService.PokemonSetting.RIBBON
                        ? 190
                        : 150);
    }

    /** EventTriggerHandlerRegistry.I2 / PokemonSummaryWindow.Qa, ROM type IDs omit Fairy. */
    static Color typeColor(int romType) {
        int type = romType >= 9 ? romType + 1 : romType;
        return new Color(
                switch (type) {
                    case 0 -> 0xffe8cf84;
                    case 1 -> -554912;
                    case 3 -> 0xffd289d3;
                    case 4 -> -1398141;
                    case 5 -> -3884411;
                    case 6 -> -5516419;
                    case 7 -> -5659169;
                    case 10 -> 0xffff7e7e;
                    case 11 -> 0xff00ccff;
                    case 12 -> 0xff7eff96;
                    case 13 -> 0xfffff47e;
                    case 14 -> -5928280;
                    case 15 -> 0xffb0bec4;
                    case 16 -> -10259268;
                    case 17 -> -12369050;
                    default -> 0xffffffff;
                });
    }

    private static final class Health extends Widget {
        private float fraction;
        private de.matthiasmann.twl.renderer.Image fill;

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
}
