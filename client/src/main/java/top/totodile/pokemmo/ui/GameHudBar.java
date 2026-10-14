package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.rom.BlackWhiteItemIcons;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/** 游戏内快捷入口；图像后台解码，纹理只在 GUI 线程创建和释放。 */
final class GameHudBar extends Widget implements AutoCloseable {
    private static final int[] ICONS = {5436, 5432, 5437, 5001, 5431, 5471, 5452, 5459, 5626};
    private static final int[] NAMES = {1100, 1101, 1126, 1161, 1, 8034, 2353, 1116, 1102};
    private final IconButton[] buttons = new IconButton[ICONS.length];
    private CompletableFuture<BufferedImage[]> pending;
    private boolean active;

    GameHudBar(Runnable bag, Runnable card, Runnable dex) {
        setTheme("/game-hud");
        setCanAcceptKeyboardFocus(false);
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new IconButton(i);
            buttons[i].setTheme("icon");
            buttons[i].setCanAcceptKeyboardFocus(false);
            String key = i == 0 ? "B" : i == 1 ? "C" : i == 4 ? "N" : "";
            buttons[i].setTooltipContent(ClientStrings.get(NAMES[i]) + (key.isEmpty() ? "" : " (" + key + ")"));
            add(buttons[i]);
        }
        buttons[0].addCallback(bag);
        buttons[1].addCallback(card);
        buttons[4].addCallback(dex);
        setVisible(false);
    }

    Button menuButton() { return buttons[buttons.length - 1]; }

    void setGameVisible(boolean visible) {
        setVisible(visible);
        if (visible == active) return;
        active = visible;
        if (!visible) { close(); return; }
        pending = CompletableFuture.supplyAsync(() -> {
            try {
                var rom = new RomFiles(ClientFiles.data("../roms")).selected(0);
                if (rom == null) return null;
                var source = new BlackWhiteItemIcons(rom);
                var result = new BufferedImage[ICONS.length];
                for (int i = 0; i < result.length; i++) {
                    var pixels = source.image(ICONS[i]);
                    var strip = new BufferedImage(48, 24, BufferedImage.TYPE_INT_ARGB);
                    for (int y = 0; y < 24; y++) for (int x = 0; x < 24; x++) {
                        int color = pixels.getRGB(x, y);
                        int gray = ((color >>> 16 & 255) + (color >>> 8 & 255) + (color & 255)) / 3;
                        strip.setRGB(x, y, color & 0xff000000 | gray << 16 | gray << 8 | gray);
                        strip.setRGB(x + 24, y, color);
                    }
                    result[i] = strip;
                }
                return result;
            } catch (IOException error) {
                System.getLogger(GameHudBar.class.getName()).log(System.Logger.Level.WARNING,
                        "Unable to load game HUD icons", error);
                return null;
            }
        });
    }

    @Override
    protected void layout() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setPosition(getInnerX() + i * 27, getInnerY());
            buttons[i].setSize(24, 32);
        }
    }

    @Override
    public int getPreferredInnerWidth() { return buttons.length * 27 - 3; }
    @Override
    public int getPreferredInnerHeight() { return 32; }

    @Override
    public void close() {
        if (pending != null) pending.cancel(true);
        pending = null;
        active = false;
        for (IconButton button : buttons) button.release();
    }

    private final class IconButton extends Button implements GUI.TooltipPositionAdjuster {
        private final int index;
        private Texture texture;
        private Image gray, color;

        IconButton(int index) { this.index = index; }

        @Override
        protected void applyTheme(ThemeInfo theme) {
            super.applyTheme(theme);
            setTooltipDelay(theme.getParameter("tooltipDelay", 150));
        }

        @Override
        public void adjustTooltipPosition(TooltipPositionData position) {
            position.x = getX() + getWidth() / 2;
            position.y = getY();
            position.alignment = Alignment.BOTTOM;
        }

        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (texture == null && pending != null && pending.isDone() && !pending.isCancelled()
                    && !pending.isCompletedExceptionally()) {
                var images = pending.getNow(null);
                if (images != null) {
                    texture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(images[index]);
                    gray = texture.getImage(0, 0, 24, 24, Color.WHITE, false, Texture.Rotation.NONE);
                    color = texture.getImage(24, 0, 24, 24, Color.WHITE, false, Texture.Rotation.NONE);
                }
            }
            if (gray != null) (getAnimationState().getAnimationState(STATE_HOVER) ? color : gray)
                    .draw(getAnimationState(), getX(), getY() + 4);
        }

        void release() {
            if (texture != null) texture.destroy();
            texture = null;
            gray = color = null;
        }
    }
}
