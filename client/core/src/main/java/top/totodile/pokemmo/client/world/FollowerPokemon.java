package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.nds.bw.BwFollowerSprites;
import top.totodile.pokemmo.server.game.GameService.Follower;

/** GdxFollowerParticleRenderer: 0.75 sprite scale, direction frames and a 150+300ms spawn fade. */
public final class FollowerPokemon implements AutoCloseable {
    private final ClientResources resources;
    private final PlayerMotion owner;
    private final WorldMaps maps;
    private final FollowerMotion motion=new FollowerMotion();
    private final List<Texture> frames=new ArrayList<>();
    private int groundPadding;
    private Follower selected=Follower.none();
    private double elapsed,lastMoved,recallStarted=-1;
    private float recallOpacity;
    private final int animationOffset=java.util.concurrent.ThreadLocalRandom.current().nextInt(332);
    private Follower pending;
    private Consumer<String> pendingError;
    private int[][] directions;
    private float[] offsets=new float[4];
    public FollowerPokemon(ClientResources resources,PlayerMotion owner,WorldMaps maps){this.resources=resources;this.owner=owner;this.maps=maps;}
    public void select(Follower value,Consumer<String> error){
        if(pending==null&&selected.equals(value)||value.equals(pending))return;
        if(!frames.isEmpty()){
            if(pending==null){recallOpacity=opacity();recallStarted=elapsed;}
            pending=value;pendingError=error;return;
        }
        load(value,error);
    }
    private void load(Follower value,Consumer<String> error){
        close();selected=value;elapsed=lastMoved=0;
        if(value.species()==0)return;
        try {
            var source=resources.followerSprites();
            if(!source.available(value)){error.accept(resources.strings().get(6910));return;}
            directions=source.directions(value);offsets=source.adjustments(value);
            var images=source.frames(value);
            // Keep one fixed frame origin, as in GdxFollowerParticleRenderer. Recentring
            // each pose on its opaque pixels turns the walking animation into vertical jitter.
            groundPadding=Integer.MAX_VALUE;
            try{for(var image:images){int bottom=-1;for(int py=0;py<image.getHeight();py++)for(int px=0;px<image.getWidth();px++)if((image.getPixel(px,py)&255)!=0)bottom=py;if(bottom>=0)groundPadding=Math.min(groundPadding,image.getHeight()-bottom-1);var texture=new Texture(image);texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);frames.add(texture);}}
            finally{images.forEach(Pixmap::dispose);}
            if(groundPadding==Integer.MAX_VALUE)groundPadding=0;
            var position=owner.movement().position();int direction=position.direction();
            int x=position.x()+(direction==2?1:direction==3?-1:0),y=position.y()+(direction==0?-1:direction==1?1:0);
            var map=owner.movement().map();
            if(!map.contains(x,y)||(map.block(x,y)&0xc00)!=0||map.block(x,y)>>>12==15||maps.isWater(maps.tiles(map).behavior(map.block(x,y)&1023))){x=position.x();y=position.y();}
            motion.reset(x,y,direction);
        }catch(IOException|RuntimeException failure){close();com.badlogic.gdx.Gdx.app.error("follower","Cannot load follower sprites",failure);error.accept(resources.strings().get(6910));}
    }
    public void step(PlayerMotion.Step step){motion.step(step);}
    public void update(float delta){
        if(Float.isFinite(delta)&&delta>0)elapsed+=delta;if(owner.moving())lastMoved=elapsed;
        if(pending!=null&&elapsed-recallStarted>=.3){var value=pending;var error=pendingError;load(value,error);}
    }
    public float x(){return motion.x(owner.stepProgress());}
    public float y(){return motion.y(owner.stepProgress());}
    public boolean visible(){return !frames.isEmpty()&&(Math.abs(x()-owner.x())+Math.abs(y()-owner.y())>.01f);}
    public int elevation(){var map=owner.movement().map();int x=Math.round(x()),y=Math.round(y());return map.contains(x,y)?map.block(x,y)>>>12:owner.movement().elevation();}
    public float opacity(){return pending!=null?recallOpacity*(float)Math.max(0,1-(elapsed-recallStarted)/.3):(float)Math.min(1,Math.max(0,(elapsed-.15)/.3));}
    public void draw(SpriteBatch batch,float footX,float footY){
        if(!visible())return;int direction=motion.direction();
        long animationTime=System.currentTimeMillis()+animationOffset;
        int index=FollowerAnimation.frame(directions[direction],animationTime,frames.size());if(index<0||index>=frames.size())return;var image=frames.get(index);
        float alpha=opacity();
        int bounce;try{bounce=Integer.parseInt(resources.preferences().get("client.gameplay.follower_bounce","0"));}catch(NumberFormatException invalid){bounce=0;}
        boolean raised=bounce!=2&&(bounce==0||elapsed-lastMoved<=.3)&&FollowerAnimation.bounce(animationTime);
        footY+=raised?1:0;footX+=offsets[1]*.25f;footY-=offsets[2]*.25f;
        batch.setColor(1,1,1,alpha);float scale=.75f*(offsets[0]>0?offsets[0]:1);
        float width=image.getWidth()*scale;
        boolean mirror=FollowerAnimation.mirror(direction,frames.size());
        batch.draw(image,footX-width/2+(mirror?width:0),footY-groundPadding*scale,mirror?-width:width,image.getHeight()*scale);
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
    }
    @Override public void close(){frames.forEach(Texture::dispose);frames.clear();groundPadding=0;pending=null;pendingError=null;recallStarted=-1;}
}
