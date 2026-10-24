package top.totodile.pokemmo.client.ui.character;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.GZIPInputStream;
import top.totodile.pokemmo.server.game.GameService;

/** Shares the character editor's addon composition and saved outfit in the map scene. */
public final class OverworldCharacter implements AutoCloseable {
    private final CharacterAppearance appearance;
    private final CharacterPreview sprites;
    private final Texture shadow;
    private final java.util.List<Texture> surf=new java.util.ArrayList<>();
    private boolean surfInitialized;
    public void surfSprites(top.totodile.pokemmo.client.rom.RomInfo rom){
        if(surfInitialized)return;surfInitialized=true;
        try{for(var frame:top.totodile.pokemmo.client.rom.gba.GbaSurfSprites.load(rom)){var pixels=new Pixmap(frame.width(),frame.height(),Pixmap.Format.RGBA8888);try{for(int y=0;y<frame.height();y++)for(int x=0;x<frame.width();x++)pixels.drawPixel(x,y,frame.rgba()[y*frame.width()+x]);var texture=new Texture(pixels);texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);surf.add(texture);}finally{pixels.dispose();}}}
        catch(IOException|RuntimeException error){surf.forEach(Texture::dispose);surf.clear();com.badlogic.gdx.Gdx.app.error("surf","Cannot load reference Surf sprite",error);}
    }
    private GameService.Appearance savedAppearance;
    public void appearance(GameService.Appearance saved){appearance.loadSaved(saved,true);savedAppearance=saved;}
    public OverworldCharacter(CharacterAppearance catalog,GameService.Appearance saved) {
        this(catalog,saved,com.badlogic.gdx.Gdx.files.local("data/sprites/game.pak"));
    }
    public OverworldCharacter(CharacterAppearance catalog,GameService.Appearance saved,FileHandle gameSprites) {
        savedAppearance=saved;appearance=catalog.restore(saved);
        sprites=new CharacterPreview(appearance,null,false,()->savedAppearance.skin(),()->savedAppearance.gender(),System::currentTimeMillis);
        try {shadow=loadShadow(gameSprites);}
        catch(RuntimeException failure){sprites.close();appearance.dispose();throw failure;}
    }
    private static Texture loadShadow(FileHandle gameSprites) {
        // GlBufferStateManager：game.pak 为 GZIP，8 字节头、纹理数量、逐张 PNG 长度及数据。
        try(var input=new GZIPInputStream(gameSprites.read())) {
            byte[] bytes=input.readAllBytes();
            if(bytes.length<9 || bytes[8]<3)throw new IOException("Missing character shadow texture");
            ByteBuffer data=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);data.position(9);
            for(int index=0;index<=2;index++) {
                if(data.remaining()<4)throw new IOException("Invalid game sprite archive");
                int length=data.getInt();
                if(length<1 || length>data.remaining())throw new IOException("Invalid game sprite image");
                if(index==2) {
                    Pixmap pixels=new Pixmap(bytes,data.position(),length);
                    try {
                        Texture texture=new Texture(pixels);
                        texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
                        return texture;
                    } finally {pixels.dispose();}
                }
                data.position(data.position()+length);
            }
            throw new IOException("Missing character shadow texture");
        } catch(IOException failure){throw new IllegalStateException("Cannot load character shadow",failure);}
    }
    public void draw(SpriteBatch batch,float x,float feetY,int direction,float walkTime,boolean moving) {
        draw(batch,x,feetY,direction,walkTime,moving,0);
    }
    public void draw(SpriteBatch batch,float x,float feetY,int direction,float walkTime,boolean moving,int mode) {
        if(mode!=3)drawShadow(batch,x,feetY);
        drawBody(batch,x,feetY,direction,walkTime,moving,mode);
    }
    public void drawBody(SpriteBatch batch,float x,float feetY,int direction,float walkTime,boolean moving,int mode) {
        int base=direction==3?2:direction;
        int first=mode==3?27+base:mode==2?18+base:mode==1&&moving?9+base*3:base;
        int left=mode==3?30+base:mode==2?21+base*2:mode==1?first+1:3+base*2;
        int right=mode==3?33+base:mode==2?left+1:mode==1?first+2:4+base*2;
        int[] frames={first,left,first,right};
        // KeyframeSplineInterpolator.pv0：步行、跑步、骑车三套帧，整数像素落点。
        int frame=moving?frames[(int)(walkTime/.16f)%4]:first;
        // GdxParticleController.wH0：24×24 贴图，以中心为原点缩放 0.75；
        // 将参考的向下 Y 坐标换算到当前脚点，保留贴图自身的半透明像素。
        float centerX=Math.round(x),groundY=Math.round(feetY);
        if(mode==3&&!surf.isEmpty()){
            int bob=(int)(System.currentTimeMillis()/300%2),index=(base==0?0:base==1?2:4)+bob;
            groundY+=bob*.75f;var texture=surf.get(index);float width=texture.getWidth()*.75f,height=texture.getHeight()*.75f;
            batch.draw(texture,centerX-width/2,groundY-8,width,height,0,0,texture.getWidth(),texture.getHeight(),direction==3,false);
        }
        batch.setColor(Color.WHITE);
        sprites.drawWorld(batch,(int)centerX,(int)groundY,frame,direction==3);
    }
    public void drawShadow(SpriteBatch batch,float x,float feetY) {
        batch.setColor(Color.WHITE);
        batch.draw(shadow,Math.round(x)-9.5f,Math.round(feetY)-4,18,18);
    }
    @Override public void close() { shadow.dispose();surf.forEach(Texture::dispose);surf.clear();sprites.close(); appearance.dispose(); }
}
