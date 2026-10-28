package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import java.io.IOException;
import java.util.*;
import top.totodile.pokemmo.client.rom.RomInfo;
import top.totodile.pokemmo.client.rom.gba.GbaNpcSprites;

/** Emerald's native rotating-gate OBJ art, affine pivot and 16-frame quarter turn. */
final class HoennGateSprites implements AutoCloseable {
    private final GbaNpcSprites sprites;
    private final Map<Integer,Texture> images=new HashMap<>();
    HoennGateSprites(RomInfo rom)throws IOException{sprites=new GbaNpcSprites(rom);}
    void draw(SpriteBatch batch,HoennRotatingGates.Gate gate){
        var texture=images.computeIfAbsent(gate.shape(),shape->{
            var g=HoennRotatingGates.DATA.get("graphics").get(shape);
            try{var frame=sprites.rawFrame(g.get("offset").asInt(),g.get("width").asInt(),g.get("height").asInt(),g.get("palette").asInt());var pixmap=new Pixmap(frame.width(),frame.height(),Pixmap.Format.RGBA8888);
                try{pixmap.setBlending(Pixmap.Blending.None);for(int y=0;y<frame.height();y++)for(int x=0;x<frame.width();x++)pixmap.drawPixel(x,y,frame.rgba()[y*frame.width()+x]);var result=new Texture(pixmap);result.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);return result;}finally{pixmap.dispose();}
            }catch(IOException e){throw new IllegalStateException("Cannot read native rotating gate",e);}
        });
        float w=texture.getWidth(),h=texture.getHeight();batch.draw(texture,gate.x()*16-w/2,-gate.y()*16-h/2,w/2,h/2,w,h,1,1,-gate.angle(),0,0,(int)w,(int)h,false,false);
    }
    public void close(){images.values().forEach(Texture::dispose);images.clear();}
}
