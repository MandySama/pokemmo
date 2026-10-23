package top.totodile.pokemmo.client.ui.inventory;

import com.badlogic.gdx.graphics.*;
import de.matthiasmann.twl.renderer.Image;
import java.io.IOException;
import java.nio.*;
import java.util.*;
import top.totodile.pokemmo.client.resources.*;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;
import top.totodile.pokemmo.client.rom.nds.bw.*;
import top.totodile.pokemmo.client.rom.gba.GbaItems;
import top.totodile.pokemmo.client.twl.GdxTwlRenderer;
import top.totodile.pokemmo.server.game.GameService;

/** BlackWhiteRom.loadItems / ItemTemplate.ca: ROM templates, no hand-maintained item catalogue. */
public final class InventoryItems implements AutoCloseable {
    public record Template(int id,int pocket,int price,int group,int region,int maximumStack) {
        public Template(int id,int pocket,int price,int group){this(id,pocket,price,group,10,9999);}
    }
    private record Icon(Texture texture,Image image) {}
    private final ClientResources resources;
    private final ClientStrings strings;
    private final GdxTwlRenderer renderer;
    private final Map<Integer,Template> templates=new HashMap<>();
    private final Map<Integer,Integer> aliases=new HashMap<>(),iconSources=new HashMap<>();
    private final Map<Integer,Icon> icons=new HashMap<>();
    private BwPokemonSprites sprites;
    private BwPokedex dex;
    private boolean loaded;
    private String[] names,descriptions;
    private final Map<Integer,String> gbaNames=new HashMap<>(),gbaDescriptions=new HashMap<>();
    private final Map<Integer,String> pocketNames=new HashMap<>();
    public InventoryItems(ClientResources resources,GdxTwlRenderer renderer){this.resources=resources;this.strings=resources.strings();this.renderer=renderer;}
    public void load()throws IOException {
        if(loaded)return;
        var rom=resources.roms().get(RomType.BLACK_WHITE);if(rom==null)throw new IOException("Black/White ROM required");
        for(var type:List.of(RomType.FIRE_RED,RomType.EMERALD)){
            var gba=resources.roms().get(type);if(gba==null)continue;
            for(var item:GbaItems.load(gba)){
                gbaNames.putIfAbsent(item.id(),item.name());gbaDescriptions.putIfAbsent(item.id(),item.description());
                int group=switch(item.pocket()){case 1->129;case 2->210;default->item.pocket()*100;};
                templates.putIfAbsent(item.id(),new Template(item.id(),item.pocket(),item.price(),group,type.id,9999));
            }
        }
        var archive=new NdsArchive(RomReader.readFile(rom,"a/0/2/4"));
        for(int index=0;index<archive.size();index++){
            var data=ByteBuffer.wrap(archive.readRaw(index)).order(ByteOrder.LITTLE_ENDIAN);
            if(data.remaining()<14)throw new IOException("Truncated BW item template");
            int price=Short.toUnsignedInt(data.getShort())*10;
            int flags=Short.toUnsignedInt(data.getShort(8)),category=flags>>7&7,kind=data.get(13)&255;
            int pocket=switch(category){case 0->1;case 1->7;case 2->4;case 3->5;case 4->2;default->1;};
            if(kind==4)pocket=3;else if(kind==3)pocket=8;
            int group=switch(pocket){case 1->129;case 2->210;default->pocket*100;};
            templates.put(5000+index,new Template(5000+index,pocket,price,group,2,9999));
        }
        var text=new NdsArchive(RomReader.readFile(rom,"a/0/0/2"));
        names=BwText.decode(text.readRaw(54));descriptions=BwText.decode(text.readRaw(53));
        // LocalizationMessageFormatter: IDs 2 and 3 are ROM messages, not XML literals.
        pocketNames.put(2,romText(text,17,7));pocketNames.put(3,romText(text,55,2));
        for(var section:BwPokedexDataPackage.sections(resources.apply("data.pak")))if(section.type()==3)overlayItems(section.data());
        dex=new BwPokedex(rom,strings);dex.loadDataPackage(resources.apply("data.pak"));
        sprites=new BwPokemonSprites(dex);loaded=true;
    }
    // ItemDatabase.getItem uses the ordinary pocket and strings 1450/1451 for unknown templates.
    public Template template(int id){return templates.getOrDefault(aliases.getOrDefault(id,id),new Template(id,1,0,129));}
    public List<GameService.ItemTemplate> catalogue(){return templates.values().stream().filter(t->t.id()>0&&t.id()<=32767&&!aliases.containsKey(t.id())).sorted(Comparator.comparingInt(Template::id)).map(t->new GameService.ItemTemplate(t.id(),t.region(),t.maximumStack())).toList();}
    /** The offline service uses this already-loaded registry; it never opens a second species/item data source. */
    public void gameData(GameService game)throws IOException {
        load();game.gameData(catalogue(),speciesTemplates());
    }
    public List<GameService.SpeciesTemplate> speciesTemplates(){return dex.all().stream().map(s->new GameService.SpeciesTemplate(s.id(),dex.extra(s.id()).genderRatio(),s.stats(),s.abilities().get(0),s.moves().stream().filter(m->m.source()<0).map(m->new GameService.LearnedMove(m.move(),m.level())).toList())).toList();}
    public String speciesName(int id){return dex.name(id);}
    /** DataPackageLoader case 3 -> SessionDisconnectPacket.LI: item masks, aliases and template copies. */
    private void overlayItems(ByteBuffer b)throws IOException {
        var copies=new LinkedHashMap<Integer,Integer>();var copyMasks=new HashMap<Integer,Integer>();int count=Short.toUnsignedInt(b.getShort());
        for(int i=0;i<count;i++){
            int id=Short.toUnsignedInt(b.getShort()),mask=b.getInt();var old=template(id);int price=old.price(),pocket=old.pocket(),group=old.group();
            if((mask&1)!=0){price=b.getInt();b.getInt();}if((mask&2)!=0)skip(b,2);
            if((mask&4)!=0){pocket=Byte.toUnsignedInt(b.get());group=switch(pocket){case 1->129;case 2->210;default->pocket*100;};}
            if((mask&8)!=0)group=Short.toUnsignedInt(b.getShort());if((mask&16)!=0)skip(b,2);
            if((mask&32)!=0)aliases.put(id,Short.toUnsignedInt(b.getShort()));
            if((mask&64)!=0){copies.put(id,Short.toUnsignedInt(b.getShort()));copyMasks.put(id,mask);}
            if((mask&128)!=0)skip(b,5);if((mask&256)!=0)skip(b,2);if((mask&512)!=0)skip(b,2);
            if((mask&1024)!=0)skip(b,3);if((mask&2048)!=0)skip(b,3);if((mask&4096)!=0)skip(b,1);
            if((mask&65536)!=0)skip(b,2);
            if((mask&131072)!=0){
                int kind=Byte.toUnsignedInt(b.get());switch(kind){case 2,3,6->skip(b,4);case 5->skip(b,5);case 4->skip(b,3);}
                int effects=Byte.toUnsignedInt(b.get());for(int j=0;j<effects;j++){b.getInt();int parameters=Byte.toUnsignedInt(b.get());for(int k=0;k<parameters;k++)BwPokedexDataPackage.skipEffectParameter(b);}
            }
            if((mask&524288)!=0)skip(b,3);if((mask&1048576)!=0)skip(b,1);
            if((mask&2097152)!=0)skip(b,Short.toUnsignedInt(b.getShort())*2);
            if((mask&4194304)!=0)skip(b,4);if((mask&16777216)!=0)skip(b,1);if((mask&33554432)!=0)skip(b,1);if((mask&67108864)!=0)skip(b,2);
            templates.put(id,new Template(id,pocket,price,group,old.region(),old.maximumStack()));
        }
        for(var entry:copies.entrySet()){
            int id=entry.getKey(),source=entry.getValue(),mask=copyMasks.get(id);var original=template(source);var target=template(id);
            templates.put(id,new Template(id,(mask&4)!=0?target.pocket():original.pocket(),(mask&1)!=0?target.price():original.price(),(mask&8)!=0?target.group():(mask&4)!=0?target.group():original.group(),original.region(),original.maximumStack()));
            iconSources.put(id,source);
        }
        if(b.hasRemaining())throw new IOException("Unread data.pak item section: "+b.remaining());
    }
    private static void skip(ByteBuffer b,int count){b.position(b.position()+count);}
    private String romText(NdsArchive archive,int table,int index)throws IOException {
        String translated=strings.archive(2,0,table,0,index);
        return translated!=null?translated:BwText.decode(archive.readRaw(table))[index];
    }
    public String pocketName(int id){return pocketNames.getOrDefault(id,strings.get(id));}
    private String text(int id,int table,String[] values,int base){
        int index=id-5000;String translated=strings.archive(2,0,table,0,index);
        return translated!=null?translated:strings.getOrDefault(base+id,values!=null&&index>=0&&index<values.length?values[index]:strings.get(1450));
    }
    private int source(int id){var visited=new HashSet<Integer>();while(visited.add(id)){int next=aliases.getOrDefault(id,iconSources.getOrDefault(id,id));if(next==id)break;id=next;}return id;}
    public String name(int id){id=source(id);if(id<5000)return strings.getOrDefault(240000+id,gbaNames.getOrDefault(id,strings.get(1450)));return templates.containsKey(id)?text(id,54,names,240000):strings.getOrDefault(240000+id,strings.get(1450));}
    public String description(int id){id=source(id);if(id<5000)return strings.getOrDefault(130000+id,gbaDescriptions.getOrDefault(id,strings.get(1451)));return templates.containsKey(id)?text(id,53,descriptions,130000):strings.getOrDefault(130000+id,strings.get(1451));}
    public Image icon(int id){
        var cached=icons.get(id);if(cached!=null)return cached.image;
        try{
            if(!loaded)load();
            var p=sprites.item(source(id));var pixmap=new Pixmap(p.width(),p.height(),Pixmap.Format.RGBA8888);
            try{
                pixmap.setBlending(Pixmap.Blending.None);
                for(int y=0;y<p.height();y++)for(int x=0;x<p.width();x++)pixmap.drawPixel(x,y,p.rgba()[y*p.width()+x]);
                var texture=new Texture(pixmap);texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
                var result=new Icon(texture,renderer.image(texture));icons.put(id,result);return result.image;
            }finally{pixmap.dispose();}
        }catch(IOException e){return null;}
    }
    @Override public void close(){icons.values().forEach(i->i.texture.dispose());icons.clear();}
}
