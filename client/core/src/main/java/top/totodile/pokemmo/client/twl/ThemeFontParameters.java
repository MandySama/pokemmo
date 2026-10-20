package top.totodile.pokemmo.client.twl;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.Hinting;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.FontParameter;
import de.matthiasmann.twl.renderer.FontParameter.Parameter;

/** 参考 TwlFontConfigParameter 的原始 XML 参数名、类型和默认值。 */
final class ThemeFontParameters {
    static final Parameter<Integer> SIZE_CJK = FontParameter.newParameter("size_cjk", -1);
    static final Parameter<Boolean> MONO = FontParameter.newParameter("mono", false);
    static final Parameter<Hinting> HINTING = FontParameter.newParameter("hinting", Hinting.AutoMedium);
    static final Parameter<Hinting> HINTING_CJK = FontParameter.newParameter("hinting_cjk", Hinting.None);
    static final Parameter<Float> GAMMA = FontParameter.newParameter("gamma", 1.8f);
    static final Parameter<Integer> RENDER_COUNT = FontParameter.newParameter("render_count", 2);
    static final Parameter<Float> BORDER_WIDTH = FontParameter.newParameter("border_width", 0f);
    static final Parameter<Color> BORDER_COLOR = FontParameter.newParameter("border_color", Color.BLACK);
    static final Parameter<Boolean> BORDER_STRAIGHT = FontParameter.newParameter("border_straight", false);
    static final Parameter<Float> BORDER_GAMMA = FontParameter.newParameter("border_gamma", 1.8f);
    static final Parameter<Integer> SHADOW_X = FontParameter.newParameter("shadow_offset_x", 0);
    static final Parameter<Integer> SHADOW_Y = FontParameter.newParameter("shadow_offset_y", 0);
    static final Parameter<Color> SHADOW_COLOR = FontParameter.newParameter("shadow_color", new Color((byte)0, (byte)0, (byte)0, (byte)191));
    static final Parameter<Integer> SPACE_X = FontParameter.newParameter("space_x", 0);
    static final Parameter<Integer> SPACE_Y = FontParameter.newParameter("space_y", 0);
    static final Parameter<Boolean> KERNING = FontParameter.newParameter("kerning", true);
    static final Parameter<TextureFilter> MIN_FILTER = FontParameter.newParameter("min_filter", TextureFilter.Nearest);
    static final Parameter<TextureFilter> MAG_FILTER = FontParameter.newParameter("mag_filter", TextureFilter.Nearest);
    static final Parameter<Color> FONT_COLOR = FontParameter.newParameter("font_color", Color.WHITE);
    static final Parameter<String> CHARACTERS = FontParameter.newParameter("characters", "\u0000ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890\"!\u0060?\\'.,;:()[]{}<>|/@\\^$€-%+=#_&~*\u007F\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008A\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F\u00A0¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ");
    static final Parameter<Boolean> INCREMENTAL = FontParameter.newParameter("incremental", true);
    static final Parameter<String> FACES = FontParameter.newParameter("faces", "");
    static final Parameter<Boolean> UNIQUE_ATLAS = FontParameter.newParameter("unique_atlas", false);
    static final Parameter<Boolean> MARKUP = FontParameter.newParameter("markup", false);

    static void register() { }
    static com.badlogic.gdx.graphics.Color color(Color color) {
        return new com.badlogic.gdx.graphics.Color(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());
    }
    static FreeTypeFontGenerator.FreeTypeFontParameter generation(FontParameter state, String typeface) {
        return generation(state, typeface, 1f);
    }
    static FreeTypeFontGenerator.FreeTypeFontParameter generation(FontParameter state, String typeface, float scale) {
        var config = new FreeTypeFontGenerator.FreeTypeFontParameter();
        config.size = state.get(GdxTwlRenderer.SIZE);
        config.hinting = state.get(HINTING);
        if (typeface != null) {
            if (state.get(SIZE_CJK) > 0) config.size = state.get(SIZE_CJK);
            if (state.get(HINTING_CJK) != Hinting.None) config.hinting = state.get(HINTING_CJK);
        }
        if (scale <= 0.001f) scale = 1f;
        config.size = Math.max(3, (int)(config.size / scale));
        config.mono = state.get(MONO);
        config.gamma = state.get(GAMMA);
        config.renderCount = state.get(RENDER_COUNT);
        config.borderWidth = state.get(BORDER_WIDTH) / scale;
        config.borderColor = color(state.get(BORDER_COLOR));
        config.borderStraight = state.get(BORDER_STRAIGHT);
        config.borderGamma = state.get(BORDER_GAMMA);
        config.shadowOffsetX = (int)(state.get(SHADOW_X) / scale);
        config.shadowOffsetY = (int)(state.get(SHADOW_Y) / scale);
        config.shadowColor = color(state.get(SHADOW_COLOR));
        config.spaceX = state.get(SPACE_X);
        config.spaceY = state.get(SPACE_Y);
        config.kerning = state.get(KERNING);
        config.minFilter = state.get(MIN_FILTER);
        config.magFilter = state.get(MAG_FILTER);
        config.color = color(state.get(FONT_COLOR));
        config.characters = state.get(CHARACTERS);
        config.incremental = state.get(INCREMENTAL);
        return config;
    }
}
