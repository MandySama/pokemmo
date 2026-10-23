package top.totodile.pokemmo.client.ui.pokedex;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.MathUtils;
import de.matthiasmann.twl.renderer.Image;
import java.io.IOException;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokemonAnimation;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/**
 * Reference TaskCallbackXt0.yq/run adapted to the client's LibGDX types.
 * Keeps its 192x128 FBO, camera (0,48), cell pivots, part ordering, matrix order and nearest filtering.
 */
final class PokedexSpriteAnimation implements AutoCloseable {
    static final int WIDTH=192,HEIGHT=128;
    static final int CAMERA_Y=HEIGHT*3/8;
    private final BwPokemonAnimation animation;
    private final GdxTwlRenderer renderer;
    private final BwPokemonAnimation.Cell[] cells;
    private final Sprite[] sprites;
    private TextureAtlas atlas;
    private SpriteBatch batch;
    private FrameBuffer buffer;
    private Image image;
    private final OrthographicCamera camera=new OrthographicCamera();
    private int previous=-1;

    PokedexSpriteAnimation(BwPokemonAnimation animation,GdxTwlRenderer renderer)throws IOException {
        this.animation=animation;this.renderer=renderer;
        cells=new BwPokemonAnimation.Cell[animation.cellCount()];sprites=new Sprite[cells.length];
        var packer=new PixmapPacker(256,1024,Pixmap.Format.RGBA8888,1,false);
        try{
            for(int i=0;i<cells.length;i++){
                cells[i]=animation.cell(i);var cell=cells[i].pixels();var pixels=new Pixmap(cell.width(),cell.height(),Pixmap.Format.RGBA8888);
                try{
                    pixels.setBlending(Pixmap.Blending.None);
                    for(int y=0;y<cell.height();y++)for(int x=0;x<cell.width();x++)pixels.drawPixel(x,y,cell.rgba()[y*cell.width()+x]);
                    packer.pack(Integer.toString(i),pixels);
                }finally{pixels.dispose();}
            }
            renderer.renderOffscreen(()->{
                atlas=packer.generateTextureAtlas(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest,false);
                for(int i=0;i<cells.length;i++)sprites[i]=new Sprite(atlas.findRegion(Integer.toString(i)));
                batch=new SpriteBatch();buffer=new FrameBuffer(Pixmap.Format.RGBA8888,WIDTH,HEIGHT,false);
                buffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
                camera.setToOrtho(true,WIDTH,HEIGHT);camera.position.set(0,CAMERA_Y,.1f);camera.lookAt(0,CAMERA_Y,0);camera.update();
                var region=new TextureRegion(buffer.getColorBufferTexture());region.flip(true,false);image=renderer.image(region);
            });
        }catch(RuntimeException error){close();throw error;}finally{packer.dispose();}
    }
    Image at(double seconds)throws IOException {
        int tick=Math.floorMod((int)Math.floor(seconds*60),animation.durationTicks());
        if(tick!=previous){
            var pose=animation.pose(tick);
            renderer.renderOffscreen(()->{
                buffer.begin();
                try{
                    Gdx.gl.glClearColor(0,0,0,0);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                    batch.setProjectionMatrix(camera.combined);batch.begin();
                    for(var draw:pose){
                        var cell=cells[draw.cell()];var sprite=sprites[draw.cell()];
                        float width=sprite.getWidth(),height=sprite.getHeight();
                        float offsetX=(cell.originX()-width/2)*(draw.flipX()?1:-1);
                        float offsetY=(cell.originY()-height/2)*(draw.flipY()?-1:1);
                        float cos=MathUtils.cosDeg(draw.rotation()),sin=MathUtils.sinDeg(draw.rotation());
                        // TaskCallbackXt0: translation * scale * rotation * cell-pivot translation.
                        float x=draw.x()+draw.scaleX()*(cos*offsetX-sin*offsetY);
                        float y=-draw.y()+draw.scaleY()*(sin*offsetX+cos*offsetY);
                        sprite.setFlip(draw.flipX(),draw.flipY());sprite.setScale(draw.scaleX(),draw.scaleY());sprite.setRotation(draw.rotation());
                        sprite.setPosition(x-width/2,y-height/2);sprite.draw(batch);
                    }
                    batch.end();
                }finally{if(batch.isDrawing())batch.end();buffer.end();}
            });
            previous=tick;
        }
        return image;
    }
    @Override public void close(){if(buffer!=null)buffer.dispose();if(atlas!=null)atlas.dispose();if(batch!=null)batch.dispose();}
}
