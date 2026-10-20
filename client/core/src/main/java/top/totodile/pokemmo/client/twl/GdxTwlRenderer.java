package top.totodile.pokemmo.client.twl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.input.Input;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.StateSelect;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/** 将 TWL 的左上角坐标、裁剪和主题图片绘制到 LibGDX。 */
public final class GdxTwlRenderer implements Renderer, AutoCloseable {
    public enum SystemMouseCursor implements MouseCursor { TEXT }
    public static final FontParameter.Parameter<Integer> SIZE = FontParameter.newParameter("size", 18);
    public static final FontParameter.Parameter<Integer> OFFSET_X = FontParameter.newParameter("offsetX", 0);
    public static final FontParameter.Parameter<Integer> OFFSET_Y = FontParameter.newParameter("offsetY", 0);
    public static final FontParameter.Parameter<String> HINTING = FontParameter.newParameter("hinting", "AutoMedium");
    public static final FontParameter.Parameter<Boolean> KERNING = FontParameter.newParameter("kerning", true);
    public static final FontParameter.Parameter<Integer> SHADOW_X = FontParameter.newParameter("shadowOffsetX", 0);
    public static final FontParameter.Parameter<Integer> SHADOW_Y = FontParameter.newParameter("shadowOffsetY", 0);
    public static final FontParameter.Parameter<Color> SHADOW_COLOR = FontParameter.newParameter("shadowColor", Color.BLACK);
    final SpriteBatch batch = new SpriteBatch();
    private final Viewport viewport;
    private final Deque<float[]> tints = new ArrayDeque<>();
    private final Deque<Rect> clips = new ArrayDeque<>();
    private final Map<String, Atlas> textures = new HashMap<>();
    private final List<GdxTwlFont> fonts = new ArrayList<>();
    private CacheContext context;
    private Cursor.SystemCursor currentSystemCursor;
    public GdxTwlRenderer(Viewport viewport) {
        this.viewport = viewport;
        tints.push(new float[]{1,1,1,1});
        context = createNewCacheContext();
    }
    public long getTimeMillis() { return System.nanoTime() / 1_000_000L; }
    public Input getInput() { return null; }
    public int getWidth() { return (int) viewport.getWorldWidth(); }
    public int getHeight() { return (int) viewport.getWorldHeight(); }
    public boolean startRendering() {
        viewport.apply();
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, getWidth(), getHeight()));
        batch.begin();
        return true;
    }
    public void endRendering() { batch.end(); Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST); clips.clear(); }
    public CacheContext createNewCacheContext() {
        return new CacheContext() {
            boolean valid = true;
            public boolean isValid() { return valid; }
            public void destroy() { valid = false; }
        };
    }
    public void setActiveCacheContext(CacheContext value) { context = value; }
    public CacheContext getActiveCacheContext() { return context; }
    public Font loadFont(URL url, StateSelect select, FontParameter... parameters) throws IOException {
        GdxTwlFont font = new GdxTwlFont(this, url, select, parameters);
        fonts.add(font);
        return font;
    }
    public de.matthiasmann.twl.renderer.Texture loadTexture(URL url, String format, String filter) throws IOException {
        String key = url.toString();
        Atlas atlas = textures.get(key);
        if (atlas == null) {
            // 使用受管理纹理，Android 从后台恢复且 GL 上下文重建时重新读取资源。
            var texture = new com.badlogic.gdx.graphics.Texture(new FileHandle(url.getPath()) {
                @Override public java.io.InputStream read() {
                    try { return url.openStream(); }
                    catch(IOException e) { throw new IllegalStateException("读取主题图片失败", e); }
                }
            });
            var sampling = "linear".equals(filter) ? com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
                    : com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
            texture.setFilter(sampling, sampling);
            atlas = new Atlas(texture);
            textures.put(key, atlas);
        }
        return atlas;
    }
    public LineRenderer getLineRenderer() { return null; }
    public OffscreenRenderer getOffscreenRenderer() { return null; }
    public FontMapper getFontMapper() { return null; }
    public DynamicImage createDynamicImage(int width, int height) { return null; }
    public Image createGradient(Gradient gradient) { throw new UnsupportedOperationException("登录主题不使用渐变资源"); }
    public void clipEnter(int x, int y, int width, int height) {
        Rect rect = new Rect(x, y, width, height);
        if (!clips.isEmpty()) rect.intersect(clips.peek());
        clips.push(rect); applyClip();
    }
    public void clipEnter(Rect rect) { clipEnter(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()); }
    public boolean clipIsEmpty() { return !clips.isEmpty() && clips.peek().isEmpty(); }
    public void clipLeave() { clips.pop(); applyClip(); }
    private void applyClip() {
        batch.flush();
        if (clips.isEmpty()) { Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST); return; }
        Rect rect = clips.peek();
        float sx = (float) viewport.getScreenWidth() / getWidth();
        float sy = (float) viewport.getScreenHeight() / getHeight();
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(viewport.getScreenX() + Math.round(rect.getX()*sx),
                viewport.getScreenY() + Math.round((getHeight()-rect.getBottom())*sy),
                Math.max(0, Math.round(rect.getWidth()*sx)), Math.max(0, Math.round(rect.getHeight()*sy)));
    }
    public void setCursor(MouseCursor cursor) {
        if (Gdx.app.getType() != ApplicationType.Desktop) return;
        Cursor.SystemCursor next = cursor == SystemMouseCursor.TEXT
                ? Cursor.SystemCursor.Ibeam : Cursor.SystemCursor.Arrow;
        if (next != currentSystemCursor) {
            Gdx.graphics.setSystemCursor(next);
            currentSystemCursor = next;
        }
    }
    public void setMousePosition(int x, int y) {}
    public void setMouseButton(int button, boolean state) {}
    public void pushGlobalTintColor(float r,float g,float b,float a) {
        float[] parent=tints.peek(); tints.push(new float[]{parent[0]*r,parent[1]*g,parent[2]*b,parent[3]*a});
    }
    public void popGlobalTintColor() { tints.pop(); }
    void tint(Color color) {
        float[] tint=tints.peek();
        batch.setColor(color.getRedFloat()*tint[0],color.getGreenFloat()*tint[1],color.getBlueFloat()*tint[2],color.getAlphaFloat()*tint[3]);
    }
    com.badlogic.gdx.graphics.Color fontTint(Color color) {
        tint(color); return batch.getColor();
    }
    public void close() {
        setCursor(null);
        fonts.forEach(GdxTwlFont::destroy);
        textures.values().forEach(Atlas::destroy);
        batch.dispose();
    }
    private final class Atlas implements de.matthiasmann.twl.renderer.Texture {
        final com.badlogic.gdx.graphics.Texture texture;
        Atlas(com.badlogic.gdx.graphics.Texture texture) { this.texture=texture; }
        public int getWidth(){return texture.getWidth();}
        public int getHeight(){return texture.getHeight();}
        public Image getImage(int x,int y,int w,int h,Color tint,boolean tiled,Rotation rotation) {
            TextureRegion region = new TextureRegion(texture,x,y,Math.abs(w),Math.abs(h));
            region.flip(w<0,h<0);
            if(rotation!=Rotation.NONE) throw new UnsupportedOperationException("登录主题不使用旋转图片");
            return new AtlasImage(region,tint==null?Color.WHITE:tint,tiled);
        }
        public MouseCursor createCursor(int x,int y,int w,int h,int hx,int hy,Image image){return null;}
        public void themeLoadingDone(){}
        public void destroy(){texture.dispose();}
    }
    private final class AtlasImage implements Image {
        final TextureRegion region; final Color color; final boolean tiled;
        AtlasImage(TextureRegion region,Color color,boolean tiled){this.region=region;this.color=color;this.tiled=tiled;}
        public int getWidth(){return region.getRegionWidth();}
        public int getHeight(){return region.getRegionHeight();}
        public void draw(AnimationState state,int x,int y){draw(state,x,y,getWidth(),getHeight());}
        public void draw(AnimationState state,int x,int y,int width,int height) {
            if(width<=0 || height<=0 || getWidth()==0 || getHeight()==0)return;
            tint(color);
            if(!tiled){batch.draw(region,x,GdxTwlRenderer.this.getHeight()-y-height,width,height);return;}
            for(int dy=0;dy<height;dy+=getHeight()) for(int dx=0;dx<width;dx+=getWidth()) {
                int w=Math.min(getWidth(),width-dx),h=Math.min(getHeight(),height-dy);
                TextureRegion part=new TextureRegion(region,0,0,w,h);
                batch.draw(part,x+dx,GdxTwlRenderer.this.getHeight()-y-dy-h,w,h);
            }
        }
        public Image createTintedVersion(Color tint){return new AtlasImage(region,color.multiply(tint),tiled);}
    }
}
