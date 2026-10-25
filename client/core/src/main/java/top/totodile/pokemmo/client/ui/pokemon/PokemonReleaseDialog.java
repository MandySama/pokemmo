package top.totodile.pokemmo.client.ui.pokemon;

import de.matthiasmann.twl.*;

import top.totodile.pokemmo.client.ui.common.ClientDialog;
import top.totodile.pokemmo.server.game.PokemonData.Pokemon;

import java.util.*;

/** DesktopClientRootHost.pF0: three-column release preview and yes/no confirmation. */
final class PokemonReleaseDialog extends ClientDialog {
    private final PokemonController controller;
    private final Widget panel =
            new Widget() {
                @Override
                protected void layout() {
                    layoutPanel();
                }
            };
    private final Label title = new Label();
    private final Button yes, no;
    private final Widget entries =
            new Widget() {
                @Override
                public int getPreferredInnerHeight() {
                    return ((values.size() + 2) / 3) * 52;
                }

                @Override
                protected void layout() {
                    for (int i = 0; i < getNumChildren(); i++) {
                        var row = getChild(i);
                        row.setPosition(
                                getInnerX() + i % 3 * (getInnerWidth() / 3),
                                getInnerY() + i / 3 * 52);
                        row.setSize(getInnerWidth() / 3, 48);
                    }
                }
            };
    private final ScrollPane scroll = new ScrollPane(entries);
    private List<Pokemon> values = List.of();

    PokemonReleaseDialog(Widget owner, PokemonController controller) {
        super(owner, controller.mobile);
        this.controller = controller;
        setTheme("/confirm-widget");
        setCloseOnClickedOutside(false);
        panel.setTheme("/confirm-panel");
        entries.setTheme("");
        title.setTheme("/release-confirm-title");
        scroll.setTheme("/scrollpane");
        scroll.setFixed(ScrollPane.Fixed.HORIZONTAL);
        yes = new Button(controller.strings.get(50));
        no = new Button(controller.strings.get(51));
        yes.setTheme("/button");
        no.setTheme("/button");
        yes.addCallback(
                () -> {
                    var selected = values;
                    closePopup();
                    controller.release(selected);
                });
        no.addCallback(this::closePopup);
        panel.add(title);
        panel.add(scroll);
        panel.add(yes);
        panel.add(no);
        add(panel);
    }

    void show(List<Pokemon> selected) {
        values = List.copyOf(selected);
        entries.removeAllChildren();
        for (var pokemon : values) entries.add(new Entry(pokemon));
        title.setText(controller.strings.format(2310, ""));
        if (openPopup()) {
            scroll.setScrollPositionY(0);
            invalidateLayout();
            no.requestKeyboardFocus();
        }
    }

    @Override
    protected void layout() {
        var owner = getOwner().getGUI();
        setPosition(0, 0);
        setSize(owner.getWidth(), owner.getHeight());
        panel.setSize(Math.min(690, getWidth() - 20), Math.min(508, getHeight() - 20));
        panel.setPosition(
                (getWidth() - panel.getWidth()) / 2, (getHeight() - panel.getHeight()) / 2);
    }

    private void layoutPanel() {
        int x = panel.getInnerX(),
                y = panel.getInnerY(),
                w = panel.getInnerWidth(),
                h = panel.getInnerHeight();
        title.setSize(w, 20);
        title.setPosition(x, y);
        scroll.setSize(w, Math.max(0, h - 90));
        scroll.setPosition(x, y + 23);
        yes.setSize(w, 27);
        yes.setPosition(x, panel.getInnerBottom() - 61);
        no.setSize(w, 27);
        no.setPosition(x, panel.getInnerBottom() - 28);
    }

    @Override
    public void closePopup() {
        super.closePopup();
        values = List.of();
        entries.removeAllChildren();
    }

    private final class Entry extends Widget {
        private final Pokemon pokemon;
        private final Label name = new Label(), ivs = new PokemonStorageWindow.IvLabel();

        Entry(Pokemon pokemon) {
            this.pokemon = pokemon;
            setTheme("");
            name.setTheme("/label");
            ivs.setTheme("/label");
            name.setText(
                    controller.strings.format(1731, Integer.toString(pokemon.level()))
                            + " "
                            + controller.name(pokemon));
            ivs.setText(
                    pokemon.ivs().stream()
                            .map(String::valueOf)
                            .collect(java.util.stream.Collectors.joining("/")));
            add(name);
            add(ivs);
        }

        @Override
        protected void layout() {
            name.setPosition(getX() + 36, getY());
            name.setSize(Math.max(0, getWidth() - 64), 20);
            ivs.setPosition(getX() + 36, getY() + 21);
            ivs.setSize(Math.max(0, getWidth() - 64), 20);
        }

        @Override
        protected void paintWidget(GUI gui) {
            var images = controller.images();
            images.partyIcon(
                            pokemon.species(),
                            pokemon.form(),
                            pokemon.gender(),
                            false,
                            false,
                            pokemon.shiny(),
                            pokemon.alpha())
                    .draw(getAnimationState(), getX(), getY() + 5, 36, 36);
            var badges = new ArrayList<de.matthiasmann.twl.renderer.Image>();
            if (pokemon.item() > 0) badges.add(images.heldItem());
            if (pokemon.hiddenAbility()) badges.add(images.atlas("HiddenAbilitySmall"));
            if (!pokemon.honors().particles().isEmpty()) badges.add(images.atlas("particle_small"));
            if (pokemon.shiny())
                badges.add(
                        images.atlas(pokemon.secretShiny() ? "secret_shiny-small" : "shiny-small"));
            for (int i = 0; i < badges.size(); i++)
                if (badges.get(i) != null)
                    badges.get(i)
                            .draw(
                                    getAnimationState(),
                                    getRight() - 28 + (i % 2) * 14,
                                    getY() + (i / 2) * 21,
                                    14,
                                    14);
        }
    }
}
