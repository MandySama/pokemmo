package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.renderer.Texture;
import top.totodile.pokemmo.ClientStrings;
import top.totodile.pokemmo.ClientFiles;
import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.rom.BlackWhiteItemIcons;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/** 空背包保留分类与搜索交互；没有物品来源时不填充演示道具。 */
final class BagWindow extends ResizableFrame implements AutoCloseable {
    private static final int[] NAMES = {1401, 1404, 1405, 1402, 2, 3, 1406, 1403};
    private static final int[] ICONS = {5436, 5017, 5057, 5476, 5004, 5252, 5155, 5459};
    private final Pocket[] pockets = new Pocket[NAMES.length];
    private final EditField search = new EditField();
    private CompletableFuture<BufferedImage[]> iconResult;
    private int selected;

    BagWindow(Runnable close) {
        setTheme("/bag-window");
        setResizableAxis(ResizableAxis.NONE);
        addCloseCallback(close);
        search.setTheme("search");
        search.setMaxTextLength(64);
        search.setTooltipContent(ClientStrings.get(9290));
        add(search);
        for (int i = 0; i < pockets.length; i++) {
            int index = i;
            pockets[i] = new Pocket();
            pockets[i].setTheme("pocket");
            pockets[i].setTooltipContent(ClientStrings.get(NAMES[i]));
            pockets[i].addCallback(() -> select(index));
            add(pockets[i]);
        }
        select(0);
    }

    void open() {
        search.setText("");
        setHardVisible(true);
        requestKeyboardFocus();
        if (iconResult == null) iconResult = CompletableFuture.supplyAsync(() -> {
            try {
                var rom = new RomFiles(ClientFiles.data("../roms")).selected(0);
                if (rom == null) return null;
                var source = new BlackWhiteItemIcons(rom);
                BufferedImage[] images = new BufferedImage[ICONS.length];
                for (int i = 0; i < images.length; i++) {
                    BufferedImage pixels = source.image(ICONS[i]);
                    BufferedImage strip = new BufferedImage(48, 24, BufferedImage.TYPE_INT_ARGB);
                    for (int y = 0; y < 24; y++) for (int x = 0; x < 24; x++) {
                        int color = pixels.getRGB(x, y);
                        int gray = ((color >> 16 & 255) + (color >> 8 & 255) + (color & 255)) / 3;
                        strip.setRGB(x, y, color);
                        strip.setRGB(x + 24, y, color & 0xff000000 | gray << 16 | gray << 8 | gray);
                    }
                    images[i] = strip;
                }
                return images;
            } catch (IOException exception) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Unable to load bag pocket icons", exception);
                return null;
            }
        });
    }

    void select(int index) {
        selected = Math.floorMod(index, pockets.length);
        for (int i = 0; i < pockets.length; i++) pockets[i].setActive(i == selected);
        setTitle(ClientStrings.get(1400) + " - " + ClientStrings.get(NAMES[selected]));
    }

    void nextPocket(boolean backwards) { select(selected + (backwards ? -1 : 1)); }
    boolean editingSearch() { return search.hasKeyboardFocus(); }

    @Override
    protected void layout() {
        super.layout();
        search.setPosition(getRight() - 210, getY() + 40);
        search.setSize(180, 28);
        for (int i = 0; i < pockets.length; i++) {
            pockets[i].setPosition(getX() + 10 + i * 53, getY() + 10);
            pockets[i].setSize(53, 26);
        }
    }

    @Override
    public void close() {
        if (iconResult != null) iconResult.cancel(true);
        iconResult = null;
        for (Pocket pocket : pockets) pocket.release();
    }

    private final class Pocket extends ToggleButton {
        private Texture texture;
        private Image image, inactive;
        @Override
        protected void paintWidget(GUI gui) {
            super.paintWidget(gui);
            if (image == null && iconResult != null && iconResult.isDone() && !iconResult.isCancelled()) {
                BufferedImage[] images = iconResult.getNow(null);
                if (images != null) {
                    int index = 0;
                    while (pockets[index] != this) index++;
                    BufferedImage pixels = images[index];
                    texture = ((GlfwTwlRenderer) gui.getRenderer()).uploadImage(pixels);
                    image = texture.getImage(0, 0, 24, 24, Color.WHITE, false, Texture.Rotation.NONE);
                    inactive = texture.getImage(24, 0, 24, 24, Color.WHITE, false, Texture.Rotation.NONE);
                }
            }
            if (image != null) (isActive() ? image : inactive).draw(getAnimationState(), getX() + 15, getY());
        }
        private void release() {
            if (texture != null) texture.destroy();
            texture = null;
            image = inactive = null;
        }
    }
}
