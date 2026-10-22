package top.totodile.pokemmo.client.ui.startup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import de.matthiasmann.twl.theme.ThemeManager;
import java.net.URL;
import java.util.Locale;
import java.util.function.Function;
import top.totodile.pokemmo.client.resources.AssetUrlStreamHandler;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** 首次资源安装复用参考 ROM 解压弹窗的主题、布局和进度绘制方式。 */
public final class ResourceProgressScreen implements AutoCloseable {
    private final ScreenViewport viewport = new ScreenViewport();
    private final GdxTwlRenderer renderer;
    private final ThemeManager theme;
    private final GUI gui;
    private final ResizableFrame frame = new ResizableFrame();
    private final Progress progress = new Progress();
    private final Container root = new Container() {
        @Override protected void layout() {
            frame.adjustSize();
            frame.setPosition(getInnerX() + (getInnerWidth() - frame.getWidth()) / 2,
                    getInnerY() + (getInnerHeight() - frame.getHeight()) / 2);
        }
    };

    public ResourceProgressScreen(Function<String, FileHandle> files, String message) {
        updateViewport();
        renderer = new GdxTwlRenderer(viewport, "sc");
        try {
            URL url = new URL(null, "asset:/data/themes/android/resource-progress.xml", new AssetUrlStreamHandler(files));
            theme = ThemeManager.createThemeManager(url, renderer);
        } catch (java.io.IOException | RuntimeException error) {
            renderer.close();
            throw new IllegalStateException("Cannot load resource progress theme", error);
        }
        root.setTheme("");
        frame.setTheme("/confirm-widget");
        frame.setDraggable(false);
        frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        DialogLayout panel = new DialogLayout();
        panel.setTheme("/confirm-panel");
        Label label = new Label(message);
        progress.setTheme("progressbar");
        panel.setHorizontalGroup(panel.createParallelGroup(label, progress));
        panel.setVerticalGroup(panel.createSequentialGroup(label, progress));
        frame.add(panel);
        root.add(frame);
        gui = new GUI(root, renderer, null);
        gui.applyTheme(theme);
    }

    private void updateViewport() {
        int width = Gdx.graphics.getWidth(), height = Gdx.graphics.getHeight();
        int left = Gdx.graphics.getSafeInsetLeft(), right = Gdx.graphics.getSafeInsetRight();
        int top = Gdx.graphics.getSafeInsetTop(), bottom = Gdx.graphics.getSafeInsetBottom();
        viewport.setUnitsPerPixel(Math.max(1280f / Math.max(1, width), 720f / Math.max(1, height)));
        viewport.update(Math.max(1, width - left - right), Math.max(1, height - top - bottom), true);
        viewport.setScreenPosition(left, bottom);
    }

    public void draw(int percent) {
        updateViewport();
        if (renderer.updateFontScale()) invalidateFonts(root);
        progress.value = Math.max(0, Math.min(100, percent)) / 100f;
        progress.setText(String.format(Locale.ROOT, "%.2f%%", progress.value * 100));
        gui.update();
    }

    private static void invalidateFonts(Widget widget) {
        if (widget instanceof TextWidget text) text.setFont(text.getFont());
        for (int i = 0; i < widget.getNumChildren(); i++) invalidateFonts(widget.getChild(i));
        widget.invalidateLayout();
    }

    @Override public void close() { gui.destroy(); theme.destroy(); renderer.close(); }

    private static final class Progress extends Label {
        private Image fill;
        private float value;
        @Override protected void applyTheme(ThemeInfo info) {
            super.applyTheme(info);
            fill = info.getImage("progressImage");
        }
        @Override protected void paintWidget(GUI gui) {
            if (fill != null) {
                int minimum = fill.getWidth();
                int extra = Math.max(0, getInnerWidth() - minimum);
                fill.draw(getAnimationState(), getInnerX(), getInnerY(), minimum + (int)(extra * value), getInnerHeight());
            }
            super.paintWidget(gui);
        }
    }
}
