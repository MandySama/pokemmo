package top.totodile.pokemmo.client.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import java.util.*;

/** GameSettingsWindow.jo0 / Dx0：窗口、独占全屏、无边框窗口。 */
public final class ScreenModes {
    private ScreenModes() {}

    public record Resolution(int width, int height) {
        @Override public String toString() { return width + " x " + height; }
    }

    public static List<Resolution> resolutions(Resolution saved) {
        Set<Resolution> values = new HashSet<>();
        for (Graphics.DisplayMode mode : Gdx.graphics.getDisplayModes()) {
            if (mode.height >= 768 || mode.width >= 1024) values.add(new Resolution(mode.width, mode.height));
        }
        values.add(saved);
        return values.stream().sorted(Comparator.comparingInt(Resolution::height).reversed()
                .thenComparing(Comparator.comparingInt(Resolution::width).reversed())).toList();
    }

    public static boolean apply(int mode, Resolution size) {
        if (mode == 1) {
            Graphics.DisplayMode selected = Arrays.stream(Gdx.graphics.getDisplayModes())
                    .filter(value -> value.width == size.width && value.height == size.height)
                    .max(Comparator.comparingInt((Graphics.DisplayMode value) -> value.bitsPerPixel)
                            .thenComparingInt(value -> value.refreshRate)).orElse(Gdx.graphics.getDisplayMode());
            return Gdx.graphics.setFullscreenMode(selected);
        }
        Gdx.graphics.setUndecorated(mode == 2);
        return Gdx.graphics.setWindowedMode(size.width, size.height);
    }
}
