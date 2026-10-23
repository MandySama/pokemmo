package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Matrix4;
import top.totodile.pokemmo.client.config.ClientSettings;

/** TwlGui controller atlas and AvatarCosmeticItemManager.oK0's Android control placement. */
public final class MobileMapControls implements AutoCloseable {
    private final Texture texture;
    private final TextureRegion[] images=new TextureRegion[5];
    private final ClientSettings settings;
    private final Matrix4 projection=new Matrix4();
    private float x,y,size;
    private int pointer=-1,dx,dy;
    private final TextureRegion[] actionImages=new TextureRegion[2];
    private final int[] actionPointers={-1,-1},actionKeys={0,0};
    private final float[] actionX=new float[2];
    private float actionY,actionSize;
    private boolean enabled=true;
    public MobileMapControls(FileHandle file,ClientSettings settings) {
        this.settings=settings; texture=new Texture(file);
        texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        images[0]=new TextureRegion(texture,1,1,170,170);
        images[1]=new TextureRegion(texture,341,1,170,170);
        images[2]=new TextureRegion(images[1]); images[2].flip(true,false);
        images[3]=new TextureRegion(texture,171,1,170,170); images[3].flip(false,true);
        images[4]=new TextureRegion(texture,171,1,170,170);
        actionImages[0]=new TextureRegion(texture,1,171,90,90);
        actionImages[1]=new TextureRegion(texture,91,171,90,90);
    }
    private boolean visible() { return enabled&&Boolean.parseBoolean(settings.get("client.mobile.onscreencontrols.show","true")); }
    public void enabled(boolean value){enabled=value;if(!value)release();}
    private float number(String key,float fallback,float min,float max) {
        try { float value=Float.parseFloat(settings.get(key,Float.toString(fallback))); return Float.isFinite(value)?Math.max(min,Math.min(max,value)):fallback; }
        catch(NumberFormatException error) { return fallback; }
    }
    private void bounds(int width,int height) {
        float scale=Math.min(width/1280f,height/720f);
        x=Gdx.graphics.getSafeInsetLeft()+100*scale;
        y=Gdx.graphics.getSafeInsetBottom()+100*scale;
        size=250*scale*number("client.mobile.dpad.scale",1,.7f,1.5f);
        actionSize=120*scale*number("client.mobile.ab.scale",1,.7f,1.5f);
        float right=width-Gdx.graphics.getSafeInsetRight();
        boolean reversed=Boolean.parseBoolean(settings.get("client.mobile.ab.reversed","false"));
        actionX[reversed?1:0]=right-actionSize*2-180*scale;
        actionX[reversed?0:1]=right-actionSize-150*scale;
        actionY=Gdx.graphics.getSafeInsetBottom()+(reversed?120*scale:220*scale-actionSize);
    }
    public int dx() { return visible()?dx:0; }
    public int dy() { return visible()?dy:0; }
    public void draw(SpriteBatch batch,int width,int height) {
        bounds(width,height); if(!visible()) { release(); return; }
        projection.setToOrtho2D(0,0,width,height);batch.setProjectionMatrix(projection);batch.begin();
        batch.setColor(Color.WHITE);
        batch.draw(images[dx<0?1:dx>0?2:dy>0?3:dy<0?4:0],x,y,size,size);
        for(int i=0;i<2;i++)batch.draw(actionImages[i],actionX[i],actionY,actionSize,actionSize);batch.end();
    }
    public boolean touchDown(int screenX,int screenY,int pointer) {
        bounds(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        float py=Gdx.graphics.getHeight()-screenY;
        if(visible())for(int i=0;i<2;i++)if(actionPointers[i]<0&&screenX>=actionX[i]&&screenX<=actionX[i]+actionSize&&py>=actionY&&py<=actionY+actionSize){
            actionPointers[i]=pointer;int fallback=i==0?Input.Keys.J:Input.Keys.K;
            try{actionKeys[i]=Integer.parseInt(settings.get("client.controls.gdx.key_"+(i==0?"a":"b"),Integer.toString(fallback)));}catch(NumberFormatException invalid){actionKeys[i]=fallback;}
            Gdx.input.getInputProcessor().keyDown(actionKeys[i]);return true;
        }
        if(!visible() || this.pointer!=-1 || screenX<x || screenX>x+size || py<y || py>y+size)return false;
        this.pointer=pointer; update(screenX,screenY);return true;
    }
    public boolean touchDragged(int x,int y,int pointer) { for(int value:actionPointers)if(value==pointer)return true;if(pointer!=this.pointer)return false; update(x,y);return true; }
    public boolean touchUp(int pointer) { for(int i=0;i<2;i++)if(actionPointers[i]==pointer){releaseAction(i);return true;}if(pointer!=this.pointer)return false;this.pointer=-1;dx=dy=0;return true; }
    private void update(int screenX,int screenY) {
        float horizontal=screenX-x-size/2, vertical=Gdx.graphics.getHeight()-screenY-y-size/2;
        float dead=number("client.mobile.dpad.deadzone_radius",.2f,.1f,.6f)*size/2;
        dx=dy=0;
        if(horizontal*horizontal+vertical*vertical<dead*dead)return;
        if(Math.abs(horizontal)>Math.abs(vertical)) dx=horizontal<0?-1:1;
        else dy=vertical<0?1:-1;
    }
    private void releaseAction(int i){actionPointers[i]=-1;int key=actionKeys[i];actionKeys[i]=0;if(key!=0&&Gdx.input.getInputProcessor()!=null)Gdx.input.getInputProcessor().keyUp(key);}
    public void release() { pointer=-1;dx=dy=0;for(int i=0;i<2;i++)releaseAction(i); }
    @Override public void close() { texture.dispose(); }
}
