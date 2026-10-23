package top.totodile.pokemmo.client.ui.pokedex;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import de.matthiasmann.twl.renderer.Image;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import top.totodile.pokemmo.client.rom.nds.bw.BwPokemonSprites;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;

/** Small icons stay cached; large detail sprites use a bounded texture cache. */
final class PokedexImages implements AutoCloseable {
    private record Entry(Texture texture,Image image) {}
    private final BwPokemonSprites source;
    private final GdxTwlRenderer renderer;
    private final Map<Integer,Entry> icons=new HashMap<>();
    private final Map<Integer,Entry> fronts=new LinkedHashMap<>(16,.75f,true);
    PokedexImages(BwPokemonSprites source,GdxTwlRenderer renderer){this.source=source;this.renderer=renderer;}
    Image icon(int id){return load(id,false);}
    Image front(int id){return load(id,true);}
    private Image load(int id,boolean front) {
        var cache=front?fronts:icons;
        Entry entry=cache.get(id);
        if(entry==null) {
            try {
                var pixels=front?source.front(id):source.icon(id);
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
    @Override public void close(){icons.values().forEach(e->e.texture.dispose());fronts.values().forEach(e->e.texture.dispose());icons.clear();fronts.clear();}
}
