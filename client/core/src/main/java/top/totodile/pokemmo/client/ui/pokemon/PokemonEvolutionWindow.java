package top.totodile.pokemmo.client.ui.pokemon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.Image;
import top.totodile.pokemmo.client.resources.ClientResources;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.rom.nds.model.*;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.client.ui.common.ClientDialog;
import top.totodile.pokemmo.client.ui.pokedex.PokedexImages;
import java.io.IOException;
import java.util.List;
import java.util.function.IntConsumer;

/** BwEvolutionScene timeline, native model/messages/sprites and the existing cancel control. */
public final class PokemonEvolutionWindow extends ClientDialog implements AutoCloseable {
    private final ClientResources resources;
    private final BwPokedex dex;
    private final Label message = new Label();
    private final Button cancel;
    private final Widget view = new Widget() {
        @Override protected void paintWidget(GUI gui) { paintScene((GdxTwlRenderer)gui.getRenderer()); }
    };
    private PokedexImages images;
    private NitroModelRenderer scenery;
    private ModelBatch batch;
    private AnimationController animation;
    private FrameBuffer buffer;
    private Image background;
    private final PerspectiveCamera camera = new PerspectiveCamera(45,1,1);
    private long started;
    private int source,target,form;
    private boolean shiny,completed,finished;
    private String name;
    private IntConsumer reply;
    private final top.totodile.pokemmo.client.ui.common.ActionConfirmDialog confirmation;
    private long pausedAt,pausedNanos;

    public PokemonEvolutionWindow(Widget owner, boolean mobile, ClientResources resources, BwPokedex dex) {
        super(owner,mobile);
        this.resources=resources;this.dex=dex;
        setTheme("/-defaults");setCloseOnEscape(false);setCloseOnClickedOutside(false);
        view.setTheme("/-defaults");message.setTheme("/label");
        cancel=new Button(resources.strings().get(53));cancel.setTheme("/button");
        cancel.addCallback(this::cancel);
        add(view);add(message);add(cancel);
        confirmation=new top.totodile.pokemmo.client.ui.common.ActionConfirmDialog(owner,resources.strings(),mobile,
                image->{if(image!=null)image.draw(getAnimationState(),getX(),getY(),getWidth(),getHeight());},
                ()->{if(pausedAt!=0){pausedNanos+=System.nanoTime()-pausedAt;pausedAt=0;}});
    }

    public void open(int source,int target,int form,boolean shiny,String nickname,IntConsumer reply) {
        open(source,target,form,shiny,nickname,true,reply);
    }
    public void open(int source,int target,int form,boolean shiny,String nickname,boolean cancellable,IntConsumer reply) {
        this.source=source;this.target=target;this.form=form;this.shiny=shiny;this.reply=reply;
        name=nickname.isEmpty()?dex.name(source):nickname;completed=finished=false;
        started=System.nanoTime();pausedAt=pausedNanos=0;message.setText(dex.evolutionMessage(name,target,false));
        cancel.setEnabled(cancellable);cancel.setVisible(cancellable);openPopup();position();
        if(resources.audio()!=null){
            resources.audio().music(new top.totodile.pokemmo.client.audio.RomAudio.Track(2,1011));
            resources.audio().sound(this,0,2,1010,0,1,0);
        }
    }

    private void load(GdxTwlRenderer renderer)throws IOException {
        if(images!=null)return;
        images=new PokedexImages(new BwPokemonSprites(dex),renderer,resources);
        var archive=dex.archive("a/1/8/2");
        byte[] model=archive.read(8);
        scenery=new NitroModelRenderer(NdsNitroModelParser.readAll(model,model,
                List.of(archive.read(6),archive.read(9),archive.read(7))).get(0));
        scenery.instance.transform.scale(2,2,2);
        animation=new AnimationController(scenery.instance);
        if(scenery.instance.animations.size>0)animation.setAnimation(scenery.instance.animations.first().id,-1);
        batch=new ModelBatch();
    }

    public void position() {
        setPosition(getOwner().getInnerX(),getOwner().getInnerY());
        setSize(getOwner().getInnerWidth(),getOwner().getInnerHeight());
        invalidateLayout();
    }

    @Override protected void layout() {
        view.setPosition(getInnerX(),getInnerY());view.setSize(getInnerWidth(),getInnerHeight());
        int margin=Math.max(12,getInnerWidth()/40);
        cancel.adjustSize();cancel.setSize(Math.max(100,cancel.getWidth()),Math.max(32,cancel.getHeight()));
        cancel.setPosition(getInnerRight()-margin-cancel.getWidth(),getInnerBottom()-margin-cancel.getHeight());
        message.setPosition(getInnerX()+margin,cancel.getY()-48);
        message.setSize(Math.max(1,getInnerWidth()-2*margin),48);
    }

    private double seconds(){return ((pausedAt==0?System.nanoTime():pausedAt)-started-pausedNanos)/1_000_000_000.0;}

    public void update() {
        if(!isOpen()||finished)return;
        if(confirmation.isOpen()){confirmation.positionWindow();return;}
        position();double time=seconds();
        // Native ru0*2: success at 24, completion at 35; cancellation ends before success.
        if(time>=12&&!completed){completed=true;cancel.setEnabled(false);
            message.setText(dex.evolutionMessage(name,target,true));
            if(resources.audio()!=null)resources.audio().cry(this,target,form,0,0);
        }
        if(time>=17.5)finish(1);
    }

    public boolean key(int key) {
        if(!isOpen())return false;
        if(confirmation.isOpen())return false;
        if(key==Event.KEY_ESCAPE||key==Event.KEY_X)cancel();
        return true;
    }

    private void cancel(){if(cancel.isEnabled()&&!completed&&!confirmation.isOpen()){pausedAt=System.nanoTime();confirmation.show(resources.strings().get(6041),()->finish(0));}}
    private void finish(int result){
        if(finished)return;finished=true;var callback=reply;reply=null;closePopup();
        if(resources.audio()!=null){resources.audio().stop(this);resources.audio().music(null);}
        if(callback!=null)callback.accept(result);
    }

    private void paintScene(GdxTwlRenderer renderer) {
        try {
            load(renderer);
            int w=Math.max(1,view.getWidth()),h=Math.max(1,view.getHeight());
            double time=seconds();
            renderer.renderOffscreen(()->{
                if(buffer==null||buffer.getWidth()!=w||buffer.getHeight()!=h){
                    if(buffer!=null)buffer.dispose();buffer=new FrameBuffer(Pixmap.Format.RGBA8888,w,h,true);
                    var texture=new TextureRegion(buffer.getColorBufferTexture());texture.flip(false,true);background=renderer.image(texture);
                }
                camera.viewportWidth=w;camera.viewportHeight=h;camera.near=.01f;camera.far=100;
                camera.position.set(0,0,-4);camera.lookAt(0,-.75f,0);camera.update();
                animation.update(Math.max(0,Math.min(.1f,Gdx.graphics.getDeltaTime()))*2);
                buffer.begin();try{
                    Gdx.gl.glDepthMask(true);Gdx.gl.glClearColor(0,0,0,1);
                    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
                    batch.begin(camera);batch.render(scenery.instance);batch.end();
                }finally{buffer.end();Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);Gdx.gl.glDisable(GL20.GL_CULL_FACE);}
            });
            background.draw(getAnimationState(),view.getX(),view.getY(),w,h);
            // Reference's 2D sprite fallback when a 3D monster model is unavailable.
            boolean changing=time>=1&&time<12;
            int species=completed||changing&&((int)(time*7)&1)!=0?target:source;
            var image=images.animatedFront(species,form,shiny,time);
            float scale=Math.min(w*.35f/image.getWidth(),h*.4f/image.getHeight())
                    *(changing?(float)(.8+.2*Math.abs(Math.sin(time*7))):1f);
            int iw=Math.round(image.getWidth()*scale),ih=Math.round(image.getHeight()*scale);
            image.draw(getAnimationState(),view.getX()+(w-iw)/2,view.getY()+(h-ih)/2-h/10,iw,ih);
        }catch(IOException error){throw new java.io.UncheckedIOException(error);}
    }

    @Override public boolean isInside(int x,int y){return x>=getX()&&x<getRight()&&y>=getY()&&y<getBottom();}
    @Override public void close(){
        reply=null;confirmation.closePopup();closePopup();if(resources.audio()!=null)resources.audio().stop(this);
        if(images!=null)images.close();if(scenery!=null)scenery.close();if(batch!=null)batch.dispose();if(buffer!=null)buffer.dispose();
    }
}
