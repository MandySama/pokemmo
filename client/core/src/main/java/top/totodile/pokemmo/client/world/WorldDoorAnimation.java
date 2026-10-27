package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import top.totodile.pokemmo.client.rom.gba.FireRedMaps;
import top.totodile.pokemmo.client.rom.gba.FireRedMaps.*;

/** WeatherAnimatedDecalBlender door frames and 60ms open/close timeline. */
final class WorldDoorAnimation implements AutoCloseable {
    private Id map;
    private int x,y;
    private Texture[] frames;
    private boolean exiting;
    private float elapsed;
    private int height;

    boolean active(){return frames!=null;}
    boolean entering(){return active()&&!exiting;}
    boolean ready(){return !entering()||elapsed>=.720f;}
    void update(float delta,boolean revealing){
        if(!active()||!Float.isFinite(delta)||delta<=0)return;
        if(exiting&&revealing)return;
        elapsed+=delta;
        if(exiting&&elapsed>=.500f)close();
    }
    boolean begin(FireRedMaps maps,MapData data,int x,int y,boolean exiting){
        if(active())return true;
        var decoded=maps.doorFrames(data,x,y);
        if(decoded==null||(decoded.flags()&512)!=0)return false;
        boolean tall=(decoded.flags()&256)!=0;
        int parts=tall?2:1;
        if(decoded.pixels().length<3*parts)return false;
        this.map=data.id();this.x=x;this.y=y;this.exiting=exiting;elapsed=0;height=tall?32:16;
        frames=new Texture[3];
        for(int f=0;f<3;f++){
            var image=new Pixmap(16,height,Pixmap.Format.RGBA8888);
            try{for(int part=0;part<parts;part++)for(int row=0;row<16;row++)for(int col=0;col<16;col++)
                image.drawPixel(col,part*16+row,decoded.pixels()[f*parts+part][row*16+col]);
                frames[f]=new Texture(image);
            }finally{image.dispose();}
        }
        return true;
    }
    float actorOffset(){
        if(!active())return 0;
        return exiting? -1+Math.min(1,elapsed/.275f): -Math.min(1,Math.max(0,elapsed-.120f)/.275f);
    }
    boolean actorWalking(){return active()&&(exiting?elapsed>0&&elapsed<.275f:elapsed>=.120f&&elapsed<.395f);}
    float actorTime(){return exiting?elapsed:Math.max(0,elapsed-.120f);}
    boolean hidesActor(){return entering()&&elapsed>=.395f;}
    // The ROM's first animation frame is still ajar. Fully closed is the original
    // map metatile, restored when the 720ms entry sequence finishes (qR).
    boolean replaces(Id id,int tx,int ty){return active()&&(!entering()||!ready())&&map.equals(id)&&tx==x&&(ty==y||height==32&&ty==y-1);}
    void draw(SpriteBatch batch,int pass,Id id,int tx,int ty,int drawX,int drawY){
        if(pass!=0||!map.equals(id)||tx!=x||ty!=y)return;
        int frame;
        if(exiting)frame=elapsed<.380f?2:elapsed<.440f?1:0;
        else frame=elapsed<.060f?0:elapsed<.120f?1:elapsed<.600f?2:elapsed<.660f?1:0;
        batch.draw(frames[frame],drawX*16,-(drawY+1)*16,16,height);
    }
    @Override public void close(){if(frames!=null)for(var texture:frames)texture.dispose();frames=null;}
}
