package top.totodile.pokemmo.ui;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.*;
import top.totodile.pokemmo.*;
import top.totodile.pokemmo.rom.ItemIconSources;
import top.totodile.pokemmo.protocol.ItemCatalog;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** 只解析已拥有道具；CPU 图像在窗口生命周期内复用，GPU 图像关闭时释放。 */
final class BagItemIcons implements AutoCloseable {
    private final Map<Integer,CompletableFuture<BufferedImage>> pixels=new HashMap<>();
    private final Map<Integer,Texture> textures=new HashMap<>();
    private final Map<Integer,Image> images=new HashMap<>();
    private CompletableFuture<ItemIconSources> source;
    void preload(Collection<Integer> ids) {
        if(source==null)source=CompletableFuture.supplyAsync(()->{
            try{return new ItemIconSources(new RomFiles(ClientFiles.data("../roms")));}
            catch(java.io.IOException e){throw new java.util.concurrent.CompletionException(e);}
        });
        for(int id:ids)if(ItemCatalog.get(id)!=null&&!pixels.containsKey(id)) {
            pixels.put(id,source.thenApplyAsync(loader->{
                try{synchronized(loader){
                    var raw=loader.image(ItemCatalog.get(id));
                    if(raw.getWidth()==24&&raw.getHeight()==24)return raw;
                    var scaled=new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
                    var graphics=scaled.createGraphics();
                    graphics.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    graphics.drawImage(raw,0,0,24,24,null);graphics.dispose();return scaled;
                }}
                catch(java.io.IOException e){throw new java.util.concurrent.CompletionException(e);}
            }));
        }
    }
    Image image(GUI gui,boolean unused,int id) {
        if(images.containsKey(id))return images.get(id);
        var result=pixels.get(id);
        if(result!=null&&result.isDone()) {
            try {
                var texture=((GlfwTwlRenderer)gui.getRenderer()).uploadImage(result.join());
                textures.put(id,texture);images.put(id,texture.getImage(0,0,24,24,Color.WHITE,false,Texture.Rotation.NONE));
            } catch(java.util.concurrent.CompletionException e){images.put(id,null);}
        }
        return images.get(id);
    }
    public void close(){for(var texture:textures.values())texture.destroy();textures.clear();images.clear();}
}
