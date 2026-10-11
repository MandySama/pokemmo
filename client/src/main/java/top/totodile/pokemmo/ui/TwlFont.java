package top.totodile.pokemmo.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.HAlignment;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.StateSelect;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/** 参考客户端的 FreeType 字体链路：CJK 字体配合 NotoSans 拉丁字符回退。 */
final class TwlFont implements Font {
    private static final FontParameter.Parameter<Integer> SIZE = FontParameter.newParameter("size", 12);
    private static final FontParameter.Parameter<Integer> CJK_SIZE = FontParameter.newParameter("size_cjk", 14);
    private static final FontParameter.Parameter<Float> GAMMA = FontParameter.newParameter("gamma", 1.8f);
    private static final FontParameter.Parameter<Integer> RENDER_COUNT = FontParameter.newParameter("render_count", 2);
    private static final FontParameter.Parameter<Integer> BORDER_WIDTH = FontParameter.newParameter("border_width", 0);
    private static final FontParameter.Parameter<Color> BORDER_COLOR = FontParameter.newParameter("border_color", new Color(0xFF434343));
    private static final FontParameter.Parameter<Integer> OFFSET_X = FontParameter.newParameter("offsetX", 0);
    private static final FontParameter.Parameter<Integer> OFFSET_Y = FontParameter.newParameter("offsetY", 0);

    private final GlfwTwlRenderer renderer;
    private final StateSelect select;
    private final FontParameter[] parameters;
    private final FreeType.Library library;
    private FreeType.Face latinFace;
    private FreeType.Face cjkFace;
    private final int baseline;
    private final int lineHeight;
    private final Map<Character, Glyph> glyphs = new HashMap<>();
    private boolean destroyed;

    private record Glyph(Image image, int left, int top, int advance, FreeType.Face face, int index) {
    }

    /** 在 ThemeManager 解析字体属性之前注册当前适配器支持的参数。 */
    static void initialize() {
    }

    TwlFont(GlfwTwlRenderer renderer, URL url, StateSelect select, FontParameter[] parameters) throws IOException {
        this.renderer = renderer;
        this.select = select;
        this.parameters = parameters;
        library = FreeType.initFreeType();
        try {
            FontParameter base = parameters[parameters.length - 1];
            // 参考资源的集合顺序为 SC、TC、JP；中文使用索引 0，回退字体保持同字重。
            cjkFace = loadFace(url, 0);
            latinFace = loadFace(new URL(url, url.getPath().endsWith("Bold.ttc")
                    ? "NotoSans-Bold.ttf" : "NotoSans-Medium.ttf"), 0);
            // 参考 bigCJKFontSizes：中文环境对整个字体采用 size_cjk，包含英文和数字。
            int size = base.get(CJK_SIZE) > 0 ? base.get(CJK_SIZE) : base.get(SIZE);
            cjkFace.setPixelSizes(0, size);
            baseline = FreeType.toInt(cjkFace.getSize().getMetrics().getAscender());
            lineHeight = FreeType.toInt(cjkFace.getSize().getMetrics().getHeight());
            latinFace.setPixelSizes(0, size);
        } catch (IOException | RuntimeException exception) {
            destroy();
            throw exception;
        }
    }

    private FreeType.Face loadFace(URL url, int index) throws IOException {
        try (var input = url.openStream()) {
            byte[] bytes = input.readAllBytes();
            return library.newMemoryFace(bytes, bytes.length, index);
        }
    }

    @Override
    public boolean isProportional() {
        return true;
    }

    @Override
    public int getBaseLine() {
        return baseline;
    }

    @Override
    public int getLineHeight() {
        return lineHeight;
    }

    @Override
    public int getSpaceWidth() {
        return glyph(' ').advance();
    }

    @Override
    public int getEM() {
        return lineHeight;
    }

    @Override
    public int getEX() {
        return baseline / 2;
    }

    @Override
    public int computeTextWidth(CharSequence text) {
        return computeTextWidth(text, 0, text.length());
    }

    @Override
    public int computeTextWidth(CharSequence text, int start, int end) {
        int width = 0;
        Glyph previous = null;
        for (int i = start; i < end; i++) {
            Glyph current = glyph(text.charAt(i));
            width += kerning(previous, current) + current.advance();
            previous = current;
        }
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
        Glyph previous = null;
        while (i < end) {
            Glyph current = glyph(text.charAt(i));
            width -= kerning(previous, current) + current.advance();
            if (width < 0) break;
            previous = current;
            i++;
        }
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
        Glyph previous = null;
        for (int i = start; i < end; i++) {
            Glyph glyph = glyph(text.charAt(i));
            x += kerning(previous, glyph);
            if (glyph.image() != null) {
                glyph.image().createTintedVersion(color).draw(state,
                        x + glyph.left() + current.get(OFFSET_X),
                        y + baseline - glyph.top() + current.get(OFFSET_Y));
            }
            x += glyph.advance();
            previous = glyph;
        }
        return x - left;
    }

    private Glyph glyph(char character) {
        return glyphs.computeIfAbsent(character, this::createGlyph);
    }

    private int kerning(Glyph previous, Glyph current) {
        if (previous == null || previous.face() != current.face()) return 0;
        return FreeType.toInt(current.face().getKerning(previous.index(), current.index(), FreeType.FT_KERNING_DEFAULT));
    }

    private Glyph createGlyph(char character) {
        // 参考 GdxFreeTypeGlyphGenerator：先查询主字体，仅缺失字形才使用回退字体。
        FreeType.Face face = cjkFace.getCharIndex(character) != 0 ? cjkFace : latinFace;
        FontParameter base = parameters[parameters.length - 1];
        int border = base.get(BORDER_WIDTH);
        // 默认字体 hinting_cjk=Slight；描边字体 hinting_cjk=Full。
        int flags = FreeType.FT_LOAD_DEFAULT | (border == 0 ? FreeType.FT_LOAD_TARGET_LIGHT : FreeType.FT_LOAD_TARGET_NORMAL);
        if (!face.loadChar(character, flags)) throw new IllegalStateException("Cannot load UI glyph: " + character);
        FreeType.GlyphSlot slot = face.getGlyph();
        int advance = FreeType.toInt(slot.getAdvanceX()) + border;
        FreeType.Glyph nativeGlyph = slot.getGlyph();
        FreeType.Glyph outline = null;
        try {
            nativeGlyph.toBitmap(FreeType.FT_RENDER_MODE_NORMAL);
            FreeType.Bitmap bitmap = nativeGlyph.getBitmap();
            if (bitmap.getWidth() == 0 || bitmap.getRows() == 0) {
                return new Glyph(null, 0, 0, advance, face, face.getCharIndex(character));
            }
            BufferedImage fill = rasterize(bitmap, Color.WHITE, base.get(GAMMA));
            BufferedImage image;
            int left = nativeGlyph.getLeft();
            int top = nativeGlyph.getTop();
            int fillX = 0;
            int fillY = 0;
            if (border > 0) {
                // 与参考 FreeTypeFontGenerator 一致，使用原生圆角描边。
                outline = slot.getGlyph();
                FreeType.Stroker stroker = library.createStroker();
                try {
                    stroker.set(border * 64, FreeType.FT_STROKER_LINECAP_ROUND, FreeType.FT_STROKER_LINEJOIN_ROUND, 0);
                    outline.strokeBorder(stroker, false);
                } finally {
                    stroker.dispose();
                }
                outline.toBitmap(FreeType.FT_RENDER_MODE_NORMAL);
                image = rasterize(outline.getBitmap(), base.get(BORDER_COLOR), base.get(GAMMA));
                fillX = left - outline.getLeft();
                fillY = outline.getTop() - top;
                left = outline.getLeft();
                top = outline.getTop();
            } else {
                image = new BufferedImage(fill.getWidth(), fill.getHeight(), BufferedImage.TYPE_INT_ARGB);
            }
            var graphics = image.createGraphics();
            for (int i = 0; i < base.get(RENDER_COUNT); i++) {
                graphics.drawImage(fill, fillX, fillY, null);
            }
            graphics.dispose();
            return new Glyph(renderer.uploadText(image), left, top, advance, face, face.getCharIndex(character));
        } finally {
            if (outline != null) outline.dispose();
            nativeGlyph.dispose();
        }
    }

    private BufferedImage rasterize(FreeType.Bitmap bitmap, Color color, float gamma) {
        var tint = new com.badlogic.gdx.graphics.Color(color.toARGB() << 8 | color.toARGB() >>> 24);
        Pixmap pixmap = bitmap.getPixmap(Pixmap.Format.RGBA8888, tint, gamma);
        try {
            var image = new BufferedImage(pixmap.getWidth(), pixmap.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < pixmap.getHeight(); y++) {
                for (int x = 0; x < pixmap.getWidth(); x++) {
                    int rgba = pixmap.getPixel(x, y);
                    image.setRGB(x, y, rgba >>> 8 | rgba << 24);
                }
            }
            return image;
        } finally {
            pixmap.dispose();
        }
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
        if (destroyed) return;
        destroyed = true;
        glyphs.clear();
        if (latinFace != null) latinFace.dispose();
        if (cjkFace != null) cjkFace.dispose();
        library.dispose();
    }
}
