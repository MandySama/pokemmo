package top.totodile.pokemmo.client.ui.pokemon;

import static top.totodile.pokemmo.server.game.PokemonData.PC;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;

import top.totodile.pokemmo.server.game.PokemonData.*;

/** PcStorageSlotWidget / ExpandableItemButton dimensions and native sprite offsets. */
final class PokemonSlot extends Button {
    final PokemonController controller;
    final int container;
    int position;
    final boolean storage;
    private final Health health = new Health();
    private final Label storageLevel =
            new Label() {
                @Override
                protected boolean handleEvent(Event event) {
                    return false;
                }
            };
    private Image radial, noHeldSource, noHeldIcon;
    private boolean itemsMode;

    void itemMode(boolean active) {
        itemsMode = active;
    }

    boolean canDragPokemon() {
        return !itemsMode || pokemon() == null || pokemon().item() == 0;
    }

    private Runnable select = () -> {};
    private Runnable context = () -> select.run();

    PokemonSlot(PokemonController controller, int container, int position, boolean storage) {
        this.controller = controller;
        this.container = container;
        this.position = position;
        this.storage = storage;
        if (storage && !controller.mobile)
            setModel(new de.matthiasmann.twl.model.ToggleButtonModel());
        setCanAcceptKeyboardFocus(false);
        // ServerRegionTagLabel.Bb(200); SlotGridContainerLayout.PH overrides PC box cells to 300.
        setTooltipDelay(storage && container == PC ? 300 : 200);
        storageLevel.setTheme("/pc-slot-level");
        add(storageLevel);
        health.setTheme("/pokemon-health");
        health.setVisible(!storage);
        add(health);
        addCallback(
                () -> {
                    select.run();
                    if (!storage || controller.mobile) controller.menu(this, storage);
                });
        refresh();
    }

    void bindPosition(int position) {
        cancelClick();
        this.position = position;
        refresh();
    }

    Pokemon pokemon() {
        return controller.state().at(container, position);
    }

    void cancelClick() {
        getModel().setArmed(false);
        getModel().setPressed(false);
    }

    void onSelect(Runnable action) {
        select = action;
    }

    void onContext(Runnable action) {
        context = action;
    }

    void refresh() {
        var p = pokemon();
        storageLevel.setVisible(
                storage && container == PC && !controller.mobile && p != null && !p.egg());
        storageLevel.setText(p == null ? "" : Integer.toString(p.level()));
        double fraction = p == null ? 1 : (double) p.hp() / p.maxHp();
        String theme =
                storage
                        ? "pc-slot"
                        : p == null
                                ? "/monster-slot-empty"
                                : fraction == 0
                                        ? "/monster-slot-purple"
                                        : fraction <= .2
                                                ? "/monster-slot-red"
                                                : fraction <= .5
                                                        ? "/monster-slot-orange"
                                                        : "/monster-slot";
        if (!theme.equals(getTheme())) {
            setTheme(theme);
            if (getGUI() != null) reapplyTheme();
        }
        health.setVisible(!storage && !controller.mobile && p != null && !p.egg());
        health.setValue((float) fraction);
        String healthTheme =
                fraction <= .2
                        ? "/pokemon-health-red"
                        : fraction <= .5 ? "/pokemon-health-orange" : "/pokemon-health";
        if (!healthTheme.equals(health.getTheme())) {
            health.setTheme(healthTheme);
            if (health.getGUI() != null) health.reapplyTheme();
        }
        setTooltipContent(
                p == null
                        ? null
                        : controller.name(p)
                                + "\n"
                                + controller.strings.format(1731, Integer.toString(p.level()))
                                + "\nHP "
                                + p.hp()
                                + "/"
                                + p.maxHp());
    }

    @Override
    protected void layout() {
        int line = storageLevel.getFont() == null ? 16 : storageLevel.getFont().getLineHeight();
        storageLevel.setPosition(getX() + 18, getBottom() - line - 1);
        storageLevel.setSize(Math.max(0, getWidth() - 24), line);
        health.setSize(Math.max(0, Math.min(40, getWidth() - 4)), 8);
        health.setPosition(getX() + 2, getBottom() - 10);
    }

    @Override
    protected void applyTheme(ThemeInfo theme) {
        super.applyTheme(theme);
        // Reference ServerRegionTagLabel only loads this image when radial health is enabled.
        radial = controller.mobile && !storage ? theme.getImage("radial-progress") : null;
    }

    @Override
    protected void paintWidget(GUI gui) {
        var p = pokemon();
        if (p == null) return;
        var images = controller.images();
        if (images == null) return;
        if (controller.mobile
                && !storage
                && radial != null
                && gui.getRenderer()
                        instanceof top.totodile.pokemmo.client.twl.GdxTwlRenderer renderer) {
            float fraction = p.egg() ? 1 : (float) p.hp() / p.maxHp();
            int rgba =
                    fraction > .5
                            ? 785150431
                            : fraction > .2 ? -207875361 : fraction > 0 ? -414434081 : -1688619297;
            renderer.drawRadial(
                    radial,
                    getX(),
                    getY(),
                    getWidth(),
                    fraction,
                    new Color(
                            (byte) (rgba >>> 24),
                            (byte) (rgba >>> 16),
                            (byte) (rgba >>> 8),
                            (byte) rgba));
        }
        Image icon = pokemonIcon();
        int size = controller.mobile ? 72 : 36;
        int x = getX() + (getWidth() - size) / 2 + (storage && !controller.mobile ? 1 : 0);
        int y =
                storage
                        ? getY() + (controller.mobile ? 0 : -4)
                        : controller.mobile ? getY() + 8 : getY() - 4;
        if (itemsMode && p.item() == 0) {
            if (noHeldSource != icon) {
                noHeldSource = icon;
                noHeldIcon = icon.createTintedVersion(new Color(0xff808080));
            }
            icon = noHeldIcon;
        }
        if (itemsMode && p.item() > 0) {
            var held = controller.itemIcon(p.item());
            if (held != null) {
                icon = held;
                size = 32;
                x = getX() + (getWidth() - 32) / 2;
                y = getY() + 4;
            }
        }
        if (!controller.draggingIcon(this)) icon.draw(getAnimationState(), x, y, size, size);
        if (!storage && !controller.mobile && p.item() > 0) {
            images.heldItem().draw(getAnimationState(), getX() + 30, getY() + 18, 13, 10);
        } else if (p.item() > 5000 && p.item() < 5627) {
            var item = images.item(p.item());
            int s = controller.mobile ? 24 : 12;
            item.draw(getAnimationState(), getRight() - s, getBottom() - s - 2, s, s);
        }
        if (storage && !controller.mobile) {
            if (!p.egg() && p.gender() >= 0) {
                var gender =
                        images.atlas(p.gender() == 1 ? "icon_gender_female" : "icon_gender_male");
                if (gender != null)
                    gender.draw(getAnimationState(), getX() + 2, getBottom() - 17, 16, 16);
            }
            if (p.hiddenAbility()) {
                var hidden = images.atlas("HiddenAbilitySmall");
                if (hidden != null)
                    hidden.draw(getAnimationState(), getRight() - 19, getY() + 3, 16, 16);
            }
            if (!p.egg() && !p.honors().particles().isEmpty()) {
                var particle = images.atlas("particle_small");
                if (particle != null)
                    particle.draw(
                            getAnimationState(), getX() + 1, getY() + (p.shiny() ? 14 : 1), 16, 16);
            }
        }
        if ((container == PC || storage) && p.shiny()) {
            boolean pc = storage && !controller.mobile;
            var shiny = images.atlas(pc && p.secretShiny() ? "secret_shiny-small" : "shiny-small");
            if (shiny != null) {
                int badge = controller.mobile ? 24 : pc ? 16 : 12;
                shiny.draw(
                        getAnimationState(),
                        getX() + (pc ? 1 : 2),
                        getY() + (pc ? 1 : 2),
                        badge,
                        badge);
            }
        }
    }

    Image pokemonIcon() {
        var p = pokemon();
        var images = controller.images();
        return container == PC
                ? images.icon(
                        p.species(),
                        p.form(),
                        p.gender(),
                        p.egg(),
                        !controller.mobile && !p.egg() && p.hp() == 0,
                        !controller.mobile && p.alpha(),
                        p.shiny())
                : images.partyIcon(
                        p.species(),
                        p.form(),
                        p.gender(),
                        p.egg(),
                        !controller.mobile && !p.egg() && p.hp() == 0,
                        p.shiny(),
                        p.alpha());
    }

    @Override
    public boolean handleEvent(Event event) {
        if (event.getType() == Event.Type.MOUSE_BTNUP
                && event.getMouseButton() == Event.MOUSE_RBUTTON) {
            if (!storage && !controller.mobile) controller.summary(this);
            else {
                context.run();
                controller.menu(this, storage, event.getMouseX(), event.getMouseY());
            }
            return true;
        }
        return super.handleEvent(event);
    }

    private static final class Health extends Widget {
        private Image fill;
        private float fraction;

        void setValue(float value) {
            fraction = value;
        }

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
