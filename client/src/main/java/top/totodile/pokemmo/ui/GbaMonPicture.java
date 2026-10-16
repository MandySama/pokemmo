package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.*;
import top.totodile.pokemmo.rom.PokemonAnimation;
import java.nio.file.Path;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;

/** showmonpic 的独立图像资源，加载结果只在 GUI 线程上传。 */
final class GbaMonPicture extends Widget implements AutoCloseable {
    private int species;
    private CompletableFuture<BufferedImage> pending;
    private Texture texture;
    private Image image;
    GbaMonPicture(){setTheme("");setVisible(false);setCanAcceptKeyboardFocus(false);}
    void show(Path rom,int species){
        if(this.species==species)return;close();this.species=species;if(species==0)return;
        setVisible(true);pending=CompletableFuture.supplyAsync(()->{try{return PokemonAnimation.loadSummary(rom,species,false).front().getFirst();}catch(Exception e){throw new java.util.concurrent.CompletionException(e);}});
    }
    @Override protected void paintWidget(GUI gui){
        if(pending!=null&&pending.isDone()){
            var result=pending;pending=null;
            if(!result.isCompletedExceptionally()){
                var pixels=result.join();texture=((GlfwTwlRenderer)gui.getRenderer()).uploadImage(pixels);
                image=texture.getImage(0,0,pixels.getWidth(),pixels.getHeight(),Color.WHITE,false,Texture.Rotation.NONE);
            }
        }
        if(image!=null)image.draw(getAnimationState(),getX(),getY(),getWidth(),getHeight());
    }
    @Override public void close(){if(pending!=null)pending.cancel(true);pending=null;if(texture!=null)texture.destroy();texture=null;image=null;species=0;setVisible(false);}
}
