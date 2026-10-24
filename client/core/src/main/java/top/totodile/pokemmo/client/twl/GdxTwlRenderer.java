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
    public static final FontParameter.Parameter<Integer> SIZE = FontParameter.newParameter("size", 11);
    public static final FontParameter.Parameter<Integer> OFFSET_X = FontParameter.newParameter("offsetX", 0);
    public static final FontParameter.Parameter<Integer> OFFSET_Y = FontParameter.newParameter("offsetY", 0);
    public static final FontParameter.Parameter<Integer> UNDERLINE_OFFSET = FontParameter.newParameter("underlineOffset", 0);
    final String typeface;
    private com.badlogic.gdx.graphics.g2d.PixmapPacker sharedFontPacker;
    private final List<com.badlogic.gdx.graphics.g2d.PixmapPacker> fontPackers = new ArrayList<>();
    final SpriteBatch batch = new SpriteBatch();
    private final Viewport viewport;
    private final Deque<float[]> tints = new ArrayDeque<>();
    private final Deque<Rect> clips = new ArrayDeque<>();
    private final Map<String, Atlas> textures = new HashMap<>();
    private final List<GdxTwlFont> fonts = new ArrayList<>();
    private final List<Cursor> nativeCursors = new ArrayList<>();
    private CacheContext context;
    private Cursor.SystemCursor currentSystemCursor;
    private float currentFontScale;
    private ThemeCursor themeCursor;
    private ThemeCursor fullscreenArrow, fullscreenText;
    private Cursor hiddenCursor;
    private com.badlogic.gdx.graphics.Texture white;
    private com.badlogic.gdx.graphics.Pixmap whitePixels;
    private final List<com.badlogic.gdx.graphics.Pixmap> derivedPixels = new ArrayList<>();
    private final AnimationState cursorState = new AnimationState() {
        public int getAnimationTime(StateKey key){return (int)(getTimeMillis()&Integer.MAX_VALUE);}
        public boolean getAnimationState(StateKey key){return false;}
        public boolean getShouldAnimateState(StateKey key){return true;}
    };
    public GdxTwlRenderer(Viewport viewport) {
        this(viewport, null);
    }
    public GdxTwlRenderer(Viewport viewport, String typeface) {
        ThemeFontParameters.register();
        this.typeface = typeface;
        this.viewport = viewport;
        currentFontScale = fontScale();
        tints.push(new float[]{1,1,1,1});
        context = createNewCacheContext();
    }
    float fontScale() {
        // 参考 TwlGui.Ew / TwlFont.lPt8：每个物理像素对应的逻辑单位。
        // 将 back buffer 比例计入，兼容桌面的高 DPI 窗口。
        if (viewport.getScreenWidth() <= 0 || viewport.getWorldWidth() <= 0 || Gdx.graphics.getBackBufferWidth() <= 0)
            return 1f;
        return viewport.getWorldWidth() / viewport.getScreenWidth()
                * Gdx.graphics.getWidth() / Gdx.graphics.getBackBufferWidth();
    }
    /** Must run before GUI layout/rendering so cached text can be invalidated by the caller. */
    public boolean updateFontScale() {
        float nextScale = fontScale();
        if (Math.abs(nextScale - currentFontScale) < 0.00001f) return false;
        fonts.forEach(GdxTwlFont::resetResources);
        disposeFontPackers();
        currentFontScale = nextScale;
        return true;
    }
    void trackFont(GdxTwlFont font) { fonts.add(font); }
    /** 纹理由调用方管理；沿用主题图像相同的坐标、裁剪和着色流程。 */
    public Image image(com.badlogic.gdx.graphics.Texture texture) {
        return new Atlas(texture).getImage(0, 0, texture.getWidth(), texture.getHeight(), Color.WHITE, false,
                de.matthiasmann.twl.renderer.Texture.Rotation.NONE);
    }
    /** Let the reference sprite renderer use its FBO, then restore the TWL viewport and clip stack. */
    public void renderOffscreen(Runnable render) {
        boolean drawing=batch.isDrawing();if(drawing)batch.end();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        try{render.run();}finally{viewport.apply();if(drawing)batch.begin();applyClip();}
    }
    public Image image(TextureRegion region) {
        return new AtlasImage(new TextureRegion(region),Color.WHITE,false,de.matthiasmann.twl.renderer.Texture.Rotation.NONE);
    }
    private com.badlogic.gdx.graphics.glutils.ShaderProgram spriteGlow;
    /** Reference GdxRenderViewPortManager: glow around the composed sprite, not each body part. */
    public void drawSpriteGlow(Image image,AnimationState state,int x,int y,int width,int height,boolean shiny,String fragment) {
        if(!(image instanceof AtlasImage atlasImage)){image.draw(state,x,y,width,height);return;}
        if(spriteGlow==null){
            var defaults=SpriteBatch.createDefaultShader();
            try{spriteGlow=new com.badlogic.gdx.graphics.glutils.ShaderProgram(defaults.getVertexShaderSource(),fragment);}
            finally{defaults.dispose();}
            if(!spriteGlow.isCompiled())throw new IllegalStateException(spriteGlow.getLog());
        }
        var previous=batch.getShader();batch.setShader(spriteGlow);
        try{
            spriteGlow.setUniformf("glowSize",shiny?1f:.8f);
            spriteGlow.setUniformf("glowIntensity",1.5f);spriteGlow.setUniformf("glowThreshold",.1f);
            spriteGlow.setUniformf("glowColor",shiny?1f:249/255f,shiny?208/255f:56/255f,shiny?51/255f:34/255f);
            spriteGlow.setUniformf("glowInvTexSize",1f/(atlasImage.region.getTexture().getWidth()-1),1f/(atlasImage.region.getTexture().getHeight()-1));
            spriteGlow.setUniformf("grayScale",0f);image.draw(state,x,y,width,height);
        }finally{batch.setShader(previous);}
    }
    com.badlogic.gdx.graphics.g2d.PixmapPacker fontPacker(boolean unique) {
        if (!unique && sharedFontPacker != null) return sharedFontPacker;
        var packer = new com.badlogic.gdx.graphics.g2d.PixmapPacker(unique ? 2048 : 1024, unique ? 2048 : 1024,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888, 1, false);
        packer.setTransparentColor(new com.badlogic.gdx.graphics.Color(1, 1, 1, 0));
        fontPackers.add(packer);
        if (!unique) sharedFontPacker = packer;
        return packer;
    }
    public long getTimeMillis() { return System.nanoTime() / 1_000_000L; }
    public Input getInput() { return null; }
    public int getWidth() { return (int) viewport.getWorldWidth(); }
    public int getHeight() { return (int) viewport.getWorldHeight(); }
    public boolean startRendering() {
        viewport.apply();
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, getWidth(), getHeight()));
        batch.begin();
        clipEnter(0, 0, getWidth(), getHeight());
        return true;
    }
    /** 移动端全屏窗口的背景铺满画布，前景仍由安全区视口布局、命中和裁剪。 */
    public void drawScreenBackground(Image image, AnimationState state) {
        if (image == null || viewport.getScreenWidth() <= 0 || viewport.getScreenHeight() <= 0) return;
        batch.end();
        var savedClips = new java.util.ArrayList<>(clips);
        clips.clear();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        float scaleX = viewport.getWorldWidth() / viewport.getScreenWidth();
        float scaleY = viewport.getWorldHeight() / viewport.getScreenHeight();
        int left = Math.round(viewport.getScreenX() * scaleX);
        int bottom = Math.round(viewport.getScreenY() * scaleY);
        int top = Math.round((Gdx.graphics.getHeight() - viewport.getScreenY() - viewport.getScreenHeight()) * scaleY);
        int width = Math.round(Gdx.graphics.getWidth() * scaleX);
        int height = Math.round(Gdx.graphics.getHeight() * scaleY);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(-left, -bottom, width, height));
        batch.begin();
        image.draw(state, -left, -top, width, height);
        batch.end();
        viewport.apply();
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, getWidth(), getHeight()));
        batch.begin();
        clips.addAll(savedClips);
        applyClip();
    }
    public void endRendering() {
        if(themeCursor!=null && Gdx.app.getType()==ApplicationType.Desktop){
            // 光标属于屏幕覆盖层，不继承最后一个控件的裁剪区域。
            batch.flush();
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
            var point=viewport.unproject(new com.badlogic.gdx.math.Vector2(Gdx.input.getX(),Gdx.input.getY()));
            themeCursor.image.draw(cursorState,Math.round(point.x)-themeCursor.hotX,
                    getHeight()-Math.round(point.y)-themeCursor.hotY);
        }
        batch.end(); Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST); clips.clear();
    }
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
        trackFont(font);
        return font;
    }
    public de.matthiasmann.twl.renderer.Texture loadTexture(URL url, String format, String filter) throws IOException {
        String key = url + "#" + format + ":" + filter;
        Atlas atlas = textures.get(key);
        if (atlas == null) {
            // 使用受管理纹理，Android 从后台恢复且 GL 上下文重建时重新读取资源。
            Object content = url.getContent(new Class<?>[]{FileHandle.class});
            FileHandle resource = content instanceof FileHandle ? (FileHandle)content : new FileHandle(url.getPath()) {
                @Override public java.io.InputStream read() {
                    try { return url.openStream(); }
                    catch(IOException e) { throw new IllegalStateException("读取主题图片失败", e); }
                }
            };
            var texture = new com.badlogic.gdx.graphics.Texture(resource);
            var sampling = "linear".equals(filter) ? com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
                    : com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
            texture.setFilter(sampling, sampling);
            atlas = new Atlas(texture);
            textures.put(key, atlas);
        }
        return atlas;
    }
    public Image grayscale(URL url) throws IOException {
        String key=url+"#gray";
        Atlas atlas=textures.get(key);
        if(atlas==null){
            var file=(FileHandle)url.getContent(new Class<?>[]{FileHandle.class});
            byte[] bytes=file.readBytes();
            var original=new com.badlogic.gdx.graphics.Pixmap(bytes,0,bytes.length);
            var gray=new com.badlogic.gdx.graphics.Pixmap(original.getWidth(),original.getHeight(),com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            gray.setBlending(com.badlogic.gdx.graphics.Pixmap.Blending.None);
            for(int y=0;y<gray.getHeight();y++)for(int x=0;x<gray.getWidth();x++){
                int rgba=original.getPixel(x,y), average=((rgba>>>24)+((rgba>>>16)&255)+((rgba>>>8)&255))/3;
                gray.drawPixel(x,y,(average<<24)|(average<<16)|(average<<8)|(rgba&255));
            }
            original.dispose();derivedPixels.add(gray);
            atlas=new Atlas(new com.badlogic.gdx.graphics.Texture(new com.badlogic.gdx.graphics.glutils.PixmapTextureData(gray,null,false,false,true)));
            textures.put(key,atlas);
        }
        return atlas.getImage(0,0,atlas.getWidth(),atlas.getHeight(),Color.WHITE,false,de.matthiasmann.twl.renderer.Texture.Rotation.NONE);
    }
    public LineRenderer getLineRenderer() { return null; }
    public void drawParticle(Image image,float x,float y,float size,float angle,float alpha){
        if(!(image instanceof AtlasImage atlas)||alpha<=0)return;
        tint(atlas.color);float packed=batch.getPackedColor();var color=batch.getColor();batch.setColor(color.r,color.g,color.b,color.a*alpha);
        batch.draw(atlas.region,x-size/2,getHeight()-y-size/2,size/2,size/2,size,size,1,1,angle);batch.setPackedColor(packed);
    }
    /** CompassRoseRenderer's textured radial progress, converted to TWL's top-left coordinates. */
    public void drawRadial(Image image,int x,int y,int size,float fraction,Color color){
        if(!(image instanceof AtlasImage atlas)||fraction<=0)return;
        var region=atlas.region;float packed=packed(atlas.color.multiply(color));
        float stop=Math.min(1,fraction)*8;
        for(int sector=0;sector<8&&sector<stop;sector++){
            float a=sector*(float)Math.PI/4,b=Math.min(sector+1,stop)*(float)Math.PI/4;
            float ax=(float)Math.sin(a),ay=-(float)Math.cos(a),bx=(float)Math.sin(b),by=-(float)Math.cos(b);
            float an=2*Math.max(Math.abs(ax),Math.abs(ay)),bn=2*Math.max(Math.abs(bx),Math.abs(by));
            ax=.5f+ax/an;ay=.5f+ay/an;bx=.5f+bx/bn;by=.5f+by/bn;
            float[] vertices=new float[20];float[] points={.5f,.5f,ax,ay,bx,by,bx,by};
            for(int i=0;i<4;i++){float px=points[i*2],py=points[i*2+1];int at=i*5;vertices[at]=x+px*size;vertices[at+1]=getHeight()-y-py*size;vertices[at+2]=packed;vertices[at+3]=region.getU()+px*(region.getU2()-region.getU());vertices[at+4]=region.getV()+py*(region.getV2()-region.getV());}
            batch.draw(region.getTexture(),vertices,0,vertices.length);
        }
    }
    public OffscreenRenderer getOffscreenRenderer() { return null; }
    public FontMapper getFontMapper() { return null; }
    public DynamicImage createDynamicImage(int width, int height) { return null; }
    public Image createGradient(Gradient gradient) { return new GdxGradientImage(this,gradient); }
    void gradientQuad(boolean horizontal,int x,int y,int width,int height,float from,float to,Color a,Color b){
        if(white==null){
            whitePixels=new com.badlogic.gdx.graphics.Pixmap(1,1,com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            whitePixels.setColor(1,1,1,1);whitePixels.fill();
            white=new com.badlogic.gdx.graphics.Texture(new com.badlogic.gdx.graphics.glutils.PixmapTextureData(whitePixels,null,false,false,true));
        }
        float left=horizontal?x+from:x,right=horizontal?x+to:x+width;
        float top=getHeight()-(horizontal?y:y+from),bottom=getHeight()-(horizontal?y+height:y+to);
        float ca=packed(a),cb=packed(b);
        float[] vertices={left,bottom,horizontal?ca:cb,0,1,left,top,ca,0,0,
                right,top,horizontal?cb:ca,1,0,right,bottom,cb,1,1};
        batch.draw(white,vertices,0,vertices.length);
    }
    private float packed(Color color){
        float[] t=tints.peek();
        return com.badlogic.gdx.graphics.Color.toFloatBits(color.getRedFloat()*t[0],color.getGreenFloat()*t[1],
                color.getBlueFloat()*t[2],color.getAlphaFloat()*t[3]);
    }
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
        // 四条边使用同一坐标换算；分别取整起点和宽高会使相邻图块差一个屏幕像素。
        int left = Math.round(rect.getX() * sx);
        int right = Math.round(rect.getRight() * sx);
        int bottom = Math.round((getHeight() - rect.getBottom()) * sy);
        int top = Math.round((getHeight() - rect.getY()) * sy);
        Gdx.gl.glScissor(viewport.getScreenX() + left, viewport.getScreenY() + bottom,
                Math.max(0, right - left), Math.max(0, top - bottom));
    }
    public void setCursor(MouseCursor cursor) {
        if (Gdx.app.getType() != ApplicationType.Desktop) return;
        themeCursor=cursor instanceof ThemeCursor?(ThemeCursor)cursor:null;
        if(themeCursor!=null && themeCursor.nativeCursor!=null && !Gdx.graphics.isFullscreen()){
            Gdx.graphics.setCursor(themeCursor.nativeCursor);
            themeCursor=null;currentSystemCursor=null;return;
        }
        // 独占全屏下部分系统/驱动不显示原生光标，直接绘制官方主题的对应光标。
        if (themeCursor == null && Gdx.graphics.isFullscreen()) {
            themeCursor = cursor == SystemMouseCursor.TEXT ? fullscreenText : fullscreenArrow;
        }
        if(themeCursor!=null){
            if(hiddenCursor==null){
                var pixels=new com.badlogic.gdx.graphics.Pixmap(16,16,com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
                pixels.setColor(0,0,0,0);pixels.fill();
                hiddenCursor=Gdx.graphics.newCursor(pixels,0,0);pixels.dispose();
            }
            Gdx.graphics.setCursor(hiddenCursor);currentSystemCursor=null;return;
        }
        Cursor.SystemCursor next = cursor == SystemMouseCursor.TEXT
                ? Cursor.SystemCursor.Ibeam : Cursor.SystemCursor.Arrow;
        if (next != currentSystemCursor) {
            Gdx.graphics.setSystemCursor(next);
            currentSystemCursor = next;
        }
    }
    public void setFullscreenCursors(Image arrow, Image text) {
        fullscreenArrow = arrow == null ? null : new ThemeCursor(arrow, 1, 1);
        fullscreenText = text == null ? null : new ThemeCursor(text, 3, 8);
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
        if(hiddenCursor!=null)hiddenCursor.dispose();
        for(var cursor:nativeCursors)cursor.dispose();
        nativeCursors.clear();
        if(white!=null)white.dispose();
        if(whitePixels!=null)whitePixels.dispose();
        fonts.forEach(GdxTwlFont::destroy);
        textures.values().forEach(Atlas::destroy);
        derivedPixels.forEach(com.badlogic.gdx.graphics.Pixmap::dispose);
        disposeFontPackers();
        batch.dispose();
        if(spriteGlow!=null)spriteGlow.dispose();
    }
    private void disposeFontPackers() {
        for (var packer : fontPackers) {
            for (var page : packer.getPages()) if (page.getTexture() != null) page.getTexture().dispose();
            packer.dispose();
        }
        fontPackers.clear();
        sharedFontPacker = null;
    }
    private final class Atlas implements de.matthiasmann.twl.renderer.Texture {
        final com.badlogic.gdx.graphics.Texture texture;
        Atlas(com.badlogic.gdx.graphics.Texture texture) { this.texture=texture; }
        public int getWidth(){return texture.getWidth();}
        public int getHeight(){return texture.getHeight();}
        public Image getImage(int x,int y,int w,int h,Color tint,boolean tiled,Rotation rotation) {
            TextureRegion region = new TextureRegion(texture,x,y,Math.abs(w),Math.abs(h));
            region.flip(w<0,h<0);
            return new AtlasImage(region,tint==null?Color.WHITE:tint,tiled,rotation);
        }
        public MouseCursor createCursor(int x,int y,int w,int h,int hx,int hy,Image image){
            Image cursorImage=image==null?getImage(x,y,w,h,Color.WHITE,false,Rotation.NONE):image;
            // TwlThemeImageManager / CroppedTextureAtlasRegion: imageRef uses software;
            // an atlas rectangle uses an RGBA, power-of-two native cursor.
            Cursor nativeCursor=null;
            if(image==null && Gdx.app.getType()==ApplicationType.Desktop){
                var data=texture.getTextureData();
                if(!data.isPrepared())data.prepare();
                var source=data.consumePixmap();
                var pixels=new com.badlogic.gdx.graphics.Pixmap(com.badlogic.gdx.math.MathUtils.nextPowerOfTwo(w),
                        com.badlogic.gdx.math.MathUtils.nextPowerOfTwo(h),com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
                try{
                    pixels.setBlending(com.badlogic.gdx.graphics.Pixmap.Blending.None);
                    pixels.setColor(0,0,0,0);pixels.fill();
                    pixels.drawPixmap(source,0,0,x,y,w,h);
                    nativeCursor=Gdx.graphics.newCursor(pixels,hx,hy);
                    nativeCursors.add(nativeCursor);
                }finally{pixels.dispose();if(data.disposePixmap())source.dispose();}
            }
            return new ThemeCursor(cursorImage,hx,hy,nativeCursor);
        }
        public void themeLoadingDone(){}
        public void destroy(){texture.dispose();}
    }
    private final class AtlasImage implements Image {
        final TextureRegion region; final Color color; final boolean tiled;
        final de.matthiasmann.twl.renderer.Texture.Rotation rotation;
        final com.badlogic.gdx.graphics.g2d.Sprite sprite;
        AtlasImage(TextureRegion region,Color color,boolean tiled,de.matthiasmann.twl.renderer.Texture.Rotation rotation){
            this.region=region;this.color=color;this.tiled=tiled;this.rotation=rotation;
            sprite=new com.badlogic.gdx.graphics.g2d.Sprite(region);
            for(int index=0;index<rotation.ordinal();index++)sprite.rotate90(true);
        }
        public int getWidth(){return rotation.ordinal()%2==0?region.getRegionWidth():region.getRegionHeight();}
        public int getHeight(){return rotation.ordinal()%2==0?region.getRegionHeight():region.getRegionWidth();}
        public void draw(AnimationState state,int x,int y){draw(state,x,y,getWidth(),getHeight());}
        public void draw(AnimationState state,int x,int y,int width,int height) {
            if(width<=0 || height<=0 || getWidth()==0 || getHeight()==0)return;
            tint(color);
            sprite.setColor(batch.getColor());
            if(!tiled){sprite.setBounds(x,GdxTwlRenderer.this.getHeight()-y-height,width,height);sprite.draw(batch);return;}
            clipEnter(x,y,width,height);
            for(int dy=0;dy<height;dy+=getHeight()) for(int dx=0;dx<width;dx+=getWidth()) {
                sprite.setBounds(x+dx,GdxTwlRenderer.this.getHeight()-y-dy-getHeight(),getWidth(),getHeight());sprite.draw(batch);
            }
            clipLeave();
        }
        public Image createTintedVersion(Color tint){return new AtlasImage(region,color.multiply(tint),tiled,rotation);}
    }
    private record ThemeCursor(Image image,int hotX,int hotY,Cursor nativeCursor) implements MouseCursor {
        ThemeCursor(Image image,int hotX,int hotY){this(image,hotX,hotY,null);}
    }
}
