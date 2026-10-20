package top.totodile.pokemmo.client.twl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import de.matthiasmann.twl.HAlignment;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.StateSelect;
import java.io.*;
import java.net.URL;

/** TWL 字体接口的 FreeType 实现，支持按需生成中文字符。 */
final class GdxTwlFont implements Font {
    private final GdxTwlRenderer renderer;
    private FreeTypeFontGenerator generator;
    private BitmapFont font;
    private final URL source;
    private final GdxTwlFont original;
    private FreeTypeFontGenerator fallbackGenerator;
    private BitmapFont fallbackFont;
    private final StateSelect select;
    private final FontParameter[] parameters;
    private boolean disposed;
    GdxTwlFont(GdxTwlRenderer renderer, URL url, StateSelect select, FontParameter[] parameters) {
        this.renderer=renderer; this.source=url; this.select=select; this.parameters=parameters; this.original=null;
    }
    private GdxTwlFont(GdxTwlFont original, StateSelect select, FontParameter[] parameters) {
        this.renderer=original.renderer; this.source=null; this.original=original;
        this.select=select; this.parameters=parameters;
    }
    @Override public Font derive(StateSelect select, FontParameter... parameters) {
        GdxTwlFont copy = new GdxTwlFont(this, select, parameters);
        renderer.trackFont(copy);
        return copy;
    }
    @Override public void initialize() {
        if (font != null || disposed) return;
        if (original != null) {
            original.initialize();
            font=original.font;
            return;
        }
        FontParameter defaults=parameters[parameters.length-1];
        String[] faces=defaults.get(ThemeFontParameters.FACES).split(",");
        int face=0;
        if(renderer.typeface!=null && faces.length>1)
            for(int i=0;i<faces.length;i++) if(faces[i].equalsIgnoreCase(renderer.typeface)) { face=i; break; }
        FileHandle file = file(source);
        generator=new FreeTypeFontGenerator(file, face);
        float scale=renderer.fontScale();
        var config=ThemeFontParameters.generation(defaults, renderer.typeface, scale);
        if(config.incremental) config.packer=renderer.fontPacker(defaults.get(ThemeFontParameters.UNIQUE_ATLAS));
        var data = new FreeTypeFontGenerator.FreeTypeBitmapFontData() {
            @Override public BitmapFont.Glyph getGlyph(char character) {
                BitmapFont.Glyph glyph=super.getGlyph(character);
                if(glyph!=null && (generator.hasGlyph(character) || fallbackFont==null)) return glyph;
                return fallbackFont==null ? glyph : fallbackFont.getData().getGlyph(character);
            }
        };
        if(config.incremental && (file.name().equals("NotoSansCJK-Medium.ttc") || file.name().equals("NotoSansCJK-Bold.ttc"))) {
            try {
                FileHandle fallback=file(new URL(source, file.name().replace("NotoSansCJK-", "NotoSans-").replace(".ttc", ".ttf")));
                if(fallback.exists()) {
                    fallbackGenerator=new FreeTypeFontGenerator(fallback);
                    fallbackFont=fallbackGenerator.generateFont(config);
                }
            } catch(IOException error) { throw new IllegalStateException("读取后备字体失败",error); }
        }
        font=generator.generateFont(config, data);
        // 参考 TwlFont.init：字体缩放时不再按逻辑像素取整字形顶点。
        // 否则一个逻辑像素对应多个屏幕像素时，字形间距及边缘会产生额外跳动。
        font.setUseIntegerPositions(Math.abs(scale - 1f) < 0.00001f);
        font.getData().markupEnabled=defaults.get(ThemeFontParameters.MARKUP);
        font.getData().setScale(scale);
        if(fallbackFont!=null) {
            fallbackFont.getData().setScale(scale);
            var target=fallbackFont.getData(); var base=font.getData();
            target.lineHeight=base.lineHeight; target.capHeight=base.capHeight; target.ascent=base.ascent;
            target.descent=base.descent; target.down=base.down; target.spaceXadvance=base.spaceXadvance;
            target.xHeight=base.xHeight;
        }
    }
    private static FileHandle file(URL url) {
        try {
            Object resource=url.getContent(new Class<?>[]{FileHandle.class});
            if(resource instanceof FileHandle) return (FileHandle)resource;
        } catch(IOException error) { throw new IllegalStateException("读取主题字体失败",error); }
        return new FileHandle(url.getPath()) {
            @Override public InputStream read() {
                try { return url.openStream(); } catch(IOException error) { throw new IllegalStateException(error); }
            }
        };
    }
    private BitmapFont font() { initialize(); return font; }
    public boolean isProportional(){return true;}
    public int getBaseLine(){return Math.round(font().getCapHeight());}
    public int getLineHeight(){return Math.round(font().getLineHeight());}
    public int getSpaceWidth(){return Math.round(font().getSpaceXadvance());}
    public int getEM(){return getLineHeight();}
    public int getEX(){return Math.round(font().getXHeight());}
    public int computeMultiLineTextWidth(CharSequence text){return computeTextWidth(text);}
    // 沿用参考客户端的半像素补偿与向上取整，保证 XML 列宽一致。
    public int computeTextWidth(CharSequence text){return computeTextWidth(text,0,text.length());}
    public int computeTextWidth(CharSequence text,int start,int end){
        if(start==end)return 0;
        GlyphLayout layout=new GlyphLayout();
        layout.setText(font(),text,start,end,com.badlogic.gdx.graphics.Color.WHITE,0,com.badlogic.gdx.utils.Align.left,false,null);
        return (int)Math.ceil(layout.width+0.5f);
    }
    public int computeVisibleGlpyhs(CharSequence text,int start,int end,int width){
        int count=0;
        while(start+count<end && computeTextWidth(text,start,start+count+1)<=width)count++;
        return count;
    }
    public int drawText(AnimationState state,int x,int y,CharSequence text){return drawText(state,x,y,text,0,text.length());}
    public int drawText(AnimationState state,int x,int y,CharSequence text,int start,int end){
        FontParameter param=parameters[select.evaluate(state)];
        font().setColor(renderer.fontTint(param.get(FontParameter.COLOR)));
        renderer.batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        // TWL 使用文字顶边坐标，消除 BitmapFont 绘制时自动添加的 ascent。
        font().draw(renderer.batch,text,x+param.get(GdxTwlRenderer.OFFSET_X),
                renderer.getHeight()-y-font().getAscent()-param.get(GdxTwlRenderer.OFFSET_Y),
                start,end,0,com.badlogic.gdx.utils.Align.left,false);
        return computeTextWidth(text,start,end);
    }
    public int drawMultiLineText(AnimationState state,int x,int y,CharSequence text,int width,HAlignment align){
        String[] lines=copyCharacters(text,0,text.length()).split("\n",-1);
        for(String line:lines){
            int extra=width-computeTextWidth(line);
            drawText(state,x+(align==HAlignment.CENTER?extra/2:align==HAlignment.RIGHT?extra:0),y,line);
            y+=getLineHeight();
        }
        return lines.length*getLineHeight();
    }
    public FontCache cacheText(FontCache previous,CharSequence text){return cacheText(previous,text,0,text.length());}
    public FontCache cacheText(FontCache previous,CharSequence text,int start,int end){return cache(copyCharacters(text,start,end),0,null);}
    public FontCache cacheMultiLineText(FontCache previous,CharSequence text,int width,HAlignment align){return cache(copyCharacters(text,0,text.length()),width,align);}
    // TWL 密码遮罩仅保证 length/charAt 可用，不能调用 subSequence 或 toString。
    private static String copyCharacters(CharSequence text,int start,int end){
        StringBuilder copy=new StringBuilder(end-start);
        for(int i=start;i<end;i++)copy.append(text.charAt(i));
        return copy.toString();
    }
    private FontCache cache(String text,int width,HAlignment align){
        return new FontCache(){
            public int getWidth(){return align==null?computeTextWidth(text):width;}
            public int getHeight(){return getLineHeight()*(align==null?1:text.split("\n",-1).length);}
            public void draw(AnimationState state,int x,int y){
                if(align==null)drawText(state,x,y,text);else drawMultiLineText(state,x,y,text,width,align);
            }
            public void destroy(){}
        };
    }
    public void destroy(){
        if(disposed)return;
        disposed=true;
        resetResources();
    }
    void resetResources(){
        if(original==null){
            if(font!=null)font.dispose();
            if(fallbackFont!=null)fallbackFont.dispose();
            if(generator!=null)generator.dispose();
            if(fallbackGenerator!=null)fallbackGenerator.dispose();
        }
        font=null;
        fallbackFont=null;
        generator=null;
        fallbackGenerator=null;
    }
}

