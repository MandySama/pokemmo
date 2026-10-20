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
    private final FreeTypeFontGenerator generator;
    private final BitmapFont font;
    private final StateSelect select;
    private final FontParameter[] parameters;
    private boolean disposed;
    GdxTwlFont(GdxTwlRenderer renderer, URL url, StateSelect select, FontParameter[] parameters) {
        this.renderer=renderer; this.select=select; this.parameters=parameters;
        generator=new FreeTypeFontGenerator(new FileHandle(url.getPath()) {
            @Override public InputStream read() {
                try{return url.openStream();}catch(IOException e){throw new IllegalStateException(e);}
            }
        });
        var config=new FreeTypeFontGenerator.FreeTypeFontParameter();
        FontParameter defaults=parameters[parameters.length-1];
        config.size=defaults.get(GdxTwlRenderer.SIZE);
        config.hinting=FreeTypeFontGenerator.Hinting.valueOf(defaults.get(GdxTwlRenderer.HINTING));
        config.kerning=defaults.get(GdxTwlRenderer.KERNING);
        config.shadowOffsetX=defaults.get(GdxTwlRenderer.SHADOW_X);
        config.shadowOffsetY=defaults.get(GdxTwlRenderer.SHADOW_Y);
        var shadow=defaults.get(GdxTwlRenderer.SHADOW_COLOR);
        config.shadowColor=new com.badlogic.gdx.graphics.Color(shadow.getRedFloat(),shadow.getGreenFloat(),
                shadow.getBlueFloat(),shadow.getAlphaFloat());
        config.incremental=true;
        config.minFilter=com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        config.magFilter=config.minFilter;
        font=generator.generateFont(config);
    }
    public boolean isProportional(){return true;}
    public int getBaseLine(){return Math.round(font.getCapHeight());}
    public int getLineHeight(){return Math.round(font.getLineHeight());}
    public int getSpaceWidth(){return Math.round(font.getSpaceXadvance());}
    public int getEM(){return getLineHeight();}
    public int getEX(){return Math.round(font.getXHeight());}
    public int computeMultiLineTextWidth(CharSequence text){return computeTextWidth(text);}
    // 沿用参考客户端的半像素补偿与向上取整，保证 XML 列宽一致。
    public int computeTextWidth(CharSequence text){return computeTextWidth(text,0,text.length());}
    public int computeTextWidth(CharSequence text,int start,int end){
        if(start==end)return 0;
        GlyphLayout layout=new GlyphLayout();
        layout.setText(font,text,start,end,com.badlogic.gdx.graphics.Color.WHITE,0,com.badlogic.gdx.utils.Align.left,false,null);
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
        font.setColor(renderer.fontTint(param.get(FontParameter.COLOR)));
        renderer.batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        // TWL 使用文字顶边坐标，消除 BitmapFont 绘制时自动添加的 ascent。
        font.draw(renderer.batch,text,x+param.get(GdxTwlRenderer.OFFSET_X),
                renderer.getHeight()-y-font.getAscent()-param.get(GdxTwlRenderer.OFFSET_Y),
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
    public void destroy(){if(!disposed){disposed=true;font.dispose();generator.dispose();}}
}

