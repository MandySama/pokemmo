package top.totodile.pokemmo.client.twl;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Gradient;
import de.matthiasmann.twl.renderer.Image;

import java.util.ArrayList;
import java.util.List;

/** TWL 渐变 stop 与 SCALE/CLAMP/REPEAT/MIRROR 的分段绘制。 */
final class GdxGradientImage implements Image {
    private final GdxTwlRenderer renderer;
    private final Gradient.Type type;
    private final Gradient.Wrap wrap;
    private final List<Gradient.Stop> stops;
    private final Color tint;
    private final float period;

    GdxGradientImage(GdxTwlRenderer renderer, Gradient gradient) {
        this.renderer = renderer;
        type = gradient.getType();
        wrap = gradient.getWrap();
        tint = Color.WHITE;
        stops = new ArrayList<>(List.of(gradient.getStops()));
        if (stops.isEmpty()) throw new IllegalArgumentException("Gradient requires a stop");
        float end = stops.get(stops.size() - 1).getPos();
        if (wrap == Gradient.Wrap.MIRROR) {
            for (int index = stops.size() - 2; index >= 0; index--)
                stops.add(
                        new Gradient.Stop(
                                2 * end - stops.get(index).getPos(), stops.get(index).getColor()));
            end *= 2;
        }
        period = end;
    }

    private GdxGradientImage(GdxGradientImage original, Color tint) {
        renderer = original.renderer;
        type = original.type;
        wrap = original.wrap;
        stops = original.stops;
        period = original.period;
        this.tint = original.tint.multiply(tint);
    }

    public int getWidth() {
        return type == Gradient.Type.HORIZONTAL ? Math.round(period) : 1;
    }

    public int getHeight() {
        return type == Gradient.Type.VERTICAL ? Math.round(period) : 1;
    }

    public void draw(AnimationState state, int x, int y) {
        draw(state, x, y, getWidth(), getHeight());
    }

    public void draw(AnimationState state, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        float length = type == Gradient.Type.HORIZONTAL ? width : height;
        if (period <= 0 || stops.size() == 1) {
            segment(
                    x,
                    y,
                    width,
                    height,
                    0,
                    length,
                    stops.get(0).getColor(),
                    stops.get(0).getColor());
            return;
        }
        float scale = wrap == Gradient.Wrap.SCALE ? length / period : 1;
        for (float offset = 0; offset < length; offset += period * scale) {
            for (int index = 1; index < stops.size(); index++) {
                var a = stops.get(index - 1);
                var b = stops.get(index);
                float from = offset + a.getPos() * scale, to = offset + b.getPos() * scale;
                if (from >= length) break;
                Color end = b.getColor();
                if (to > length) {
                    end = interpolate(a.getColor(), end, (length - from) / (to - from));
                    to = length;
                }
                segment(x, y, width, height, from, to, a.getColor(), end);
            }
            if (wrap == Gradient.Wrap.CLAMP) {
                Color end = stops.get(stops.size() - 1).getColor();
                if (period < length) segment(x, y, width, height, period, length, end, end);
                break;
            }
            if (wrap == Gradient.Wrap.SCALE) break;
        }
    }

    private void segment(
            int x, int y, int width, int height, float from, float to, Color a, Color b) {
        if (to <= from) return;
        renderer.gradientQuad(
                type == Gradient.Type.HORIZONTAL,
                x,
                y,
                width,
                height,
                from,
                to,
                a.multiply(tint),
                b.multiply(tint));
    }

    private static Color interpolate(Color a, Color b, float t) {
        return new Color(
                (byte) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (byte) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (byte) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                (byte) Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    public Image createTintedVersion(Color color) {
        return new GdxGradientImage(this, color);
    }
}
