package top.totodile.pokemmo.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongSupplier;

/** Reference nf_0.COn / Modern_Util_q3_0: black map fade, painted before the HUD. */
public final class WorldTransition implements AutoCloseable {
    private final LongSupplier clock;
    private final Matrix4 projection=new Matrix4();
    private SpriteBatch batch;
    private Texture black;
    private long started;
    private int from,target,opacity;
    private CompletableFuture<Void> covered;

    public WorldTransition(LongSupplier milliseconds){clock=milliseconds;}

    /** GdxParticleEmitterNode.Iu0: COn.m(0,255,500), then wait for destination readiness. */
    public CompletableFuture<Void> cover(){
        if(covered!=null&&!covered.isDone())covered.cancel(false);
        covered=new CompletableFuture<>();fade(0,255);return covered;
    }

    /** GBA/NDS map renderers: nf_0.w30(500,false), i.e. opaque black -> transparent. */
    public void reveal(){fade(255,0);}

    /** Do not let a superseded or cancelled request uncover a newer destination load. */
    public void restore(CompletableFuture<Void> request){if(covered==request)reveal();}

    private void fade(int start,int end){from=start;target=end;opacity=start;started=clock.getAsLong();}
    public void reset(){
        opacity=from=target=0;
        if(covered!=null&&!covered.isDone())covered.cancel(false);
        covered=null;
    }
    public void draw(int width,int height){
        // Modern_Util_q3_0.r0 uses linear interpolation and integer alpha in [0,255].
        if(opacity!=target){
            float fraction=Math.min(1f,Math.max(0f,(clock.getAsLong()-started)/500f));
            opacity=(int)((target-from)*fraction+from);
        }
        if(opacity==255&&covered!=null&&!covered.isDone())covered.complete(null);
        if(opacity==0)return;
        if(batch==null){
            var pixel=new Pixmap(1,1,Pixmap.Format.RGBA8888);
            try{pixel.setColor(Color.BLACK);pixel.fill();black=new Texture(pixel);}
            finally{pixel.dispose();}
            batch=new SpriteBatch();
        }
        Gdx.gl.glViewport(0,0,width,height);
        batch.setProjectionMatrix(projection.setToOrtho2D(0,0,width,height));
        batch.setColor(1f,1f,1f,opacity/255f);
        batch.begin();batch.draw(black,0,0,width,height);batch.end();
    }
    @Override public void close(){reset();if(batch!=null)batch.dispose();if(black!=null)black.dispose();}
}
