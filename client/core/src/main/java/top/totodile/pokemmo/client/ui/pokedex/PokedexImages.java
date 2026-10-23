package top.totodile.pokemmo.client.ui.pokedex;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import top.totodile.pokemmo.client.resources.ClientResources;
import de.matthiasmann.twl.renderer.Image;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokemonSprites;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** Small icons stay cached; large detail sprites use a bounded texture cache. */
public final class PokedexImages implements AutoCloseable {
    private record Entry(Texture texture,Image image) {}
    private final BwPokemonSprites source;
    private final GdxTwlRenderer renderer;
    private final Map<Integer,Entry> icons=new HashMap<>();
    private final Map<Integer,Entry> fronts=new LinkedHashMap<>(16,.75f,true);
    private final Map<Integer,PokedexSpriteAnimation> animations=new LinkedHashMap<>(4,.75f,true);
    private TextureAtlas atlas;
    private String language="zh";
    PokedexImages(BwPokemonSprites source,GdxTwlRenderer renderer){this.source=source;this.renderer=renderer;}
    public PokedexImages(BwPokemonSprites source,GdxTwlRenderer renderer,ClientResources resources){this(source,renderer);language=resources.strings().language().split("-")[0];atlas=new TextureAtlas(resources.apply("sprites/atlas/main.atlas"));}
    public Image atlas(String name){if(atlas==null)return null;var region=atlas.findRegion(name);return region==null?null:renderer.image(region);}
    Image atlas(String name,int index){if(atlas==null)return null;var region=atlas.findRegion(name,index);return region==null?null:renderer.image(region);}
    public Image type(int type){int id=type>=9?type+1:type;Image image=atlas("monster_type_"+id+"_"+language);return image==null?atlas("monster_type_"+id+"_en"):image;}
    public Image icon(int id){return load(id,false);}
    public Image icon(int id,int form,int gender,boolean egg){
        int key=1<<24|id|form<<10|(gender+1)<<20|(egg?1<<23:0);Entry entry=icons.get(key);
        if(entry==null){
            try{
                var pixels=source.icon(id,form,gender,egg);var pixmap=new Pixmap(pixels.width(),pixels.height(),Pixmap.Format.RGBA8888);
                try{
                    pixmap.setBlending(Pixmap.Blending.None);
                    for(int y=0;y<pixels.height();y++)for(int x=0;x<pixels.width();x++)pixmap.drawPixel(x,y,pixels.rgba()[y*pixels.width()+x]);
                    var texture=new Texture(pixmap);texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
                    entry=new Entry(texture,renderer.image(texture));icons.put(key,entry);
                }finally{pixmap.dispose();}
            }catch(IOException error){throw new UncheckedIOException(error);}
        }return entry.image;
    }
    Image icon(int id,boolean seen){Image image=icon(id);return seen?image:image.createTintedVersion(new de.matthiasmann.twl.Color((byte)0,(byte)0,(byte)0,(byte)180));}
    public Image item(int id){return load(-id,false);}
    Image front(int id){return load(id,true);}
    Image front(int id,int form,boolean shiny){return load(id|form<<10|(shiny?1<<20:0),true);}
    public Image animatedFront(int id,int form,boolean shiny,double seconds){
        int key=id|form<<10|(shiny?1<<20:0);
        try{
            var animation=animations.get(key);
            if(animation==null){
                animation=new PokedexSpriteAnimation(source.frontAnimation(id,form,shiny),renderer);animations.put(key,animation);
                if(animations.size()>4){var entries=animations.values().iterator();entries.next().close();entries.remove();}
            }
            return animation.at(seconds);
        }catch(IOException error){throw new UncheckedIOException(error);}
    }
    private Image load(int id,boolean front) {
        var cache=front?fronts:icons;
        Entry entry=cache.get(id);
        if(entry==null) {
            try {
                var pixels=front?source.front(id&1023,id>>10&1023,(id&(1<<20))!=0):id<0?source.item(-id):source.icon(id);
                Pixmap pixmap=new Pixmap(pixels.width(),pixels.height(),Pixmap.Format.RGBA8888);
                try {
                    pixmap.setBlending(Pixmap.Blending.None);
                    for(int y=0;y<pixels.height();y++)for(int x=0;x<pixels.width();x++)pixmap.drawPixel(x,y,pixels.rgba()[y*pixels.width()+x]);
                    Texture texture=new Texture(pixmap);texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
                    entry=new Entry(texture,renderer.image(texture));cache.put(id,entry);
                }finally{pixmap.dispose();}
                if(fronts.size()>16){var iterator=fronts.values().iterator();iterator.next().texture.dispose();iterator.remove();}
            }catch(IOException error){throw new UncheckedIOException(error);}
        }
        return entry.image;
    }
    @Override public void close(){icons.values().forEach(e->e.texture.dispose());fronts.values().forEach(e->e.texture.dispose());animations.values().forEach(PokedexSpriteAnimation::close);icons.clear();fronts.clear();animations.clear();if(atlas!=null)atlas.dispose();}
}
