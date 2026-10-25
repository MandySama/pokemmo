package top.totodile.pokemmo.client.ui.pokedex;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.ToggleButtonModel;
import de.matthiasmann.twl.renderer.Image;

/** Official season tab: translated label followed by the matching indexed atlas icon. */
final class PokedexSeasonButton extends Button {
    private final Label label =
            new Label(getAnimationState()) {
                @Override
                protected boolean handleEvent(Event event) {
                    return false;
                }
            };
    private final Image icon;
    private final int iconSize;
    private final int iconOffsetY;

    PokedexSeasonButton(
            PokedexImages images, String text, boolean mobile, int season, Runnable action) {
        setTheme("/pokedex-tab");
        setModel(new ToggleButtonModel());
        setCanAcceptKeyboardFocus(false);
        label.setText(text);
        label.setTheme("/pokedex-plain");
        add(label);
        icon = images.atlas("season", season);
        iconSize = mobile ? 32 : 16;
        iconOffsetY = mobile ? 0 : 2;
        addCallback(action);
    }

    @Override
    public int getPreferredInnerWidth() {
        return label.getPreferredWidth() + 3 + iconSize;
    }

    @Override
    public int getPreferredInnerHeight() {
        return Math.max(label.getPreferredHeight(), iconSize);
    }

    @Override
    protected void layout() {
        int width = label.getPreferredWidth(), height = label.getPreferredHeight();
        label.setSize(width, height);
        label.setPosition(
                getInnerX() + (getInnerWidth() - width - 3 - iconSize) / 2,
                getInnerY() + (getInnerHeight() - height) / 2);
    }

    @Override
    protected void paintWidget(GUI gui) {
        super.paintWidget(gui);
        if (icon != null)
            icon.draw(
                    getAnimationState(),
                    label.getRight() + 3,
                    getInnerY() + (getInnerHeight() - iconSize) / 2 + iconOffsetY,
                    iconSize,
                    iconSize);
    }
}
