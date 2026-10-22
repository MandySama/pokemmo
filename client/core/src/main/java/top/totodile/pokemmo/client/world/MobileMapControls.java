package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Matrix4;
import top.totodile.pokemmo.client.config.ClientSettings;

/** Reference TwlGui controller atlas and 25/25/250 lower-left D-pad placement. */
public final class MobileMapControls implements AutoCloseable {
    private final Texture texture;
    private final TextureRegion[] images=new TextureRegion[5];
    private final ClientSettings settings;
    private final Matrix4 projection=new Matrix4();
    private float x,y,size;
    private int pointer=-1,dx,dy;
    public MobileMapControls(FileHandle file,ClientSettings settings) {
        this.settings=settings; texture=new Texture(file);
        texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        images[0]=new TextureRegion(texture,1,1,170,170);
        images[1]=new TextureRegion(texture,341,1,170,170);
        images[2]=new TextureRegion(images[1]); images[2].flip(true,false);
        images[3]=new TextureRegion(texture,171,1,170,170); images[3].flip(false,true);
        images[4]=new TextureRegion(texture,171,1,170,170);
    }
    private boolean visible() { return Boolean.parseBoolean(settings.get("client.mobile.onscreencontrols.show","true")); }
    private float number(String key,float fallback,float min,float max) {
        try { float value=Float.parseFloat(settings.get(key,Float.toString(fallback))); return Float.isFinite(value)?Math.max(min,Math.min(max,value)):fallback; }
        catch(NumberFormatException error) { return fallback; }
    }
    private void bounds(int width,int height) {
        float scale=Math.min(width/1280f,height/720f);
        x=Gdx.graphics.getSafeInsetLeft()+25*scale;
        y=Gdx.graphics.getSafeInsetBottom()+25*scale;
        size=250*scale*number("client.mobile.dpad.scale",1,.7f,1.5f);
    }
    public int dx() { return visible()?dx:0; }
    public int dy() { return visible()?dy:0; }
    public void draw(SpriteBatch batch,int width,int height) {
        bounds(width,height); if(!visible()) { release(); return; }
        projection.setToOrtho2D(0,0,width,height);batch.setProjectionMatrix(projection);batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(images[dx<0?1:dx>0?2:dy>0?3:dy<0?4:0],x,y,size,size);batch.end();
    }
    public boolean touchDown(int screenX,int screenY,int pointer) {
        bounds(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        float py=Gdx.graphics.getHeight()-screenY;
        if(!visible() || this.pointer!=-1 || screenX<x || screenX>x+size || py<y || py>y+size)return false;
        this.pointer=pointer; update(screenX,screenY);return true;
    }
    public boolean touchDragged(int x,int y,int pointer) { if(pointer!=this.pointer)return false; update(x,y);return true; }
    public boolean touchUp(int pointer) { if(pointer!=this.pointer)return false; release();return true; }
    private void update(int screenX,int screenY) {
        float horizontal=screenX-x-size/2, vertical=Gdx.graphics.getHeight()-screenY-y-size/2;
        float dead=number("client.mobile.dpad.deadzone_radius",.2f,.1f,.6f)*size/2;
        dx=dy=0;
        if(horizontal*horizontal+vertical*vertical<dead*dead)return;
        if(Math.abs(horizontal)>Math.abs(vertical)) dx=horizontal<0?-1:1;
        else dy=vertical<0?1:-1;
    }
    public void release() { pointer=-1;dx=dy=0; }
    @Override public void close() { texture.dispose(); }
}
