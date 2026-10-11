package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.HAlignment;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.StateSelect;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** 复用参考客户端字体，通过 Java2D 栅格化并缓存字形纹理。 */
final class TwlFont implements Font {
    private static final FontParameter.Parameter<Integer> SIZE = FontParameter.newParameter("size", 12);
    private static final FontParameter.Parameter<Integer> CJK_SIZE = FontParameter.newParameter("size_cjk", 14);
    private static final FontParameter.Parameter<Integer> BORDER_WIDTH = FontParameter.newParameter("border_width", 0);
    private static final FontParameter.Parameter<Color> BORDER_COLOR = FontParameter.newParameter("border_color", new Color(0xFF434343));
    private static final FontParameter.Parameter<Integer> OFFSET_X = FontParameter.newParameter("offsetX", 0);
    private static final FontParameter.Parameter<Integer> OFFSET_Y = FontParameter.newParameter("offsetY", 0);

    private final GlfwTwlRenderer renderer;
    private final StateSelect select;
    private final FontParameter[] parameters;
    private final java.awt.Font font;
    private final java.awt.Font cjkFont;
    private final FontMetrics metrics;
    private final FontMetrics latinMetrics;
    private final int lineHeight;
    private final Map<Character, Image> glyphs = new HashMap<>();

    /** 在 ThemeManager 解析字体属性之前注册当前适配器支持的参数。 */
    static void initialize() {
    }

    TwlFont(GlfwTwlRenderer renderer, URL url, StateSelect select, FontParameter[] parameters) throws IOException {
        this.renderer = renderer;
        this.select = select;
        this.parameters = parameters;
        try (var input = url.openStream()) {
            java.awt.Font[] faces = java.awt.Font.createFonts(input);
            java.awt.Font selected = faces[0];
            for (java.awt.Font face : faces) {
                if (face.getFontName(java.util.Locale.ROOT).contains("CJK SC")) {
                    selected = face;
                    break;
                }
            }
            FontParameter base = parameters[parameters.length - 1];
            font = selected.deriveFont(base.get(SIZE).floatValue());
            cjkFont = selected.deriveFont(base.get(CJK_SIZE).floatValue());
        } catch (java.awt.FontFormatException exception) {
            throw new IOException("Invalid UI font", exception);
        }
        var graphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
        graphics.setFont(font);
        latinMetrics = graphics.getFontMetrics();
        graphics.setFont(cjkFont);
        metrics = graphics.getFontMetrics();
        // FontMetrics 会分别向上取整升部和降部，额外多出一像素；使用完整行度量。
        lineHeight = (int) Math.ceil(cjkFont.getLineMetrics("Mg", graphics.getFontRenderContext()).getHeight());
        graphics.dispose();
    }

    @Override
    public boolean isProportional() {
        return true;
    }

    @Override
    public int getBaseLine() {
        return metrics.getAscent();
    }

    @Override
    public int getLineHeight() {
        return lineHeight;
    }

    @Override
    public int getSpaceWidth() {
        return latinMetrics.charWidth(' ');
    }

    @Override
    public int getEM() {
        return latinMetrics.charWidth('M');
    }

    @Override
    public int getEX() {
        return metrics.getAscent() / 2;
    }

    @Override
    public int computeTextWidth(CharSequence text) {
        return computeTextWidth(text, 0, text.length());
    }

    @Override
    public int computeTextWidth(CharSequence text, int start, int end) {
        int width = 0;
        for (int i = start; i < end; i++) width += charWidth(text.charAt(i));
        return width;
    }

    @Override
    public int computeMultiLineTextWidth(CharSequence text) {
        int width = 0;
        for (String line : text.toString().split("\n", -1)) {
            width = Math.max(width, computeTextWidth(line));
        }
        return width;
    }

    @Override
    public int computeVisibleGlpyhs(CharSequence text, int start, int end, int width) {
        int i = start;
        while (i < end && (width -= charWidth(text.charAt(i))) >= 0) i++;
        return i - start;
    }

    @Override
    public int drawText(AnimationState state, int x, int y, CharSequence text) {
        return drawText(state, x, y, text, 0, text.length());
    }

    @Override
    public int drawText(AnimationState state, int x, int y, CharSequence text, int start, int end) {
        int left = x;
        FontParameter current = parameters[select.evaluate(state)];
        Color color = current.get(FontParameter.COLOR);
        for (int i = start; i < end; i++) {
            char character = text.charAt(i);
            if (!Character.isWhitespace(character)) {
                glyphs.computeIfAbsent(character, this::createGlyph).createTintedVersion(color)
                        .draw(state, x - 2 + current.get(OFFSET_X), y + current.get(OFFSET_Y));
            }
            x += charWidth(character);
        }
        return x - left;
    }

    private Image createGlyph(char character) {
        var image = new BufferedImage(Math.max(1, charWidth(character)) + 4,
                getLineHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setFont(character >= 0x2E80 ? cjkFont : font);
        FontParameter base = parameters[parameters.length - 1];
        if (base.get(BORDER_WIDTH) > 0) {
            graphics.setColor(new java.awt.Color(base.get(BORDER_COLOR).toARGB(), true));
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    graphics.drawString(String.valueOf(character), 2 + dx, getBaseLine() + dy);
                }
            }
        }
        graphics.setColor(java.awt.Color.WHITE);
        graphics.drawString(String.valueOf(character), 2, getBaseLine());
        graphics.dispose();
        return renderer.uploadText(image);
    }

    private int charWidth(char character) {
        return (character >= 0x2E80 ? metrics : latinMetrics).charWidth(character);
    }

    @Override
    public int drawMultiLineText(AnimationState state, int x, int y, CharSequence text, int width, HAlignment align) {
        int top = y;
        for (String line : text.toString().split("\n", -1)) {
            int gap = width - computeTextWidth(line);
            drawText(state, x + (align == HAlignment.RIGHT ? gap : align == HAlignment.CENTER ? gap / 2 : 0), y, line);
            y += getLineHeight();
        }
        return y - top;
    }

    @Override
    public FontCache cacheText(FontCache cache, CharSequence text) {
        return null;
    }

    @Override
    public FontCache cacheText(FontCache cache, CharSequence text, int start, int end) {
        return null;
    }

    @Override
    public FontCache cacheMultiLineText(FontCache cache, CharSequence text, int width, HAlignment align) {
        return null;
    }

    @Override
    public void destroy() {
        glyphs.clear();
    }
}
