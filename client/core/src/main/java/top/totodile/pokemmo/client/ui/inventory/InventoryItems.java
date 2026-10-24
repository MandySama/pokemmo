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
    /** ItemTemplate fields used by InventoryWindow.UX and PokemonStatTextFormatter.Sp0. */
    static final class Attributes {
        boolean tradable=true,depositable=true,holdable=true,tossable=true,buyable,bound,percent;
        int action,battleAction,sellPrice,move=-1,ball=-1,recovery,friendship,stat=-1,statAmount;
        Attributes(){}
        Attributes(Attributes a){tradable=a.tradable;depositable=a.depositable;holdable=a.holdable;tossable=a.tossable;buyable=a.buyable;bound=a.bound;percent=a.percent;action=a.action;battleAction=a.battleAction;sellPrice=a.sellPrice;move=a.move;ball=a.ball;recovery=a.recovery;friendship=a.friendship;stat=a.stat;statAmount=a.statAmount;}
    }
    private final ClientResources resources;
    private final ClientStrings strings;
    private final GdxTwlRenderer renderer;
    private final Map<Integer,Template> templates=new HashMap<>();
    private final Map<Integer,Integer> aliases=new HashMap<>(),iconSources=new HashMap<>();
    private final Map<Integer,Integer> copySources=new HashMap<>(),nameIds=new HashMap<>(),descriptionIds=new HashMap<>();
    private final Map<Integer,int[]> potionNames=new HashMap<>();
    private final Map<Integer,Icon> icons=new HashMap<>();
    private final Map<Integer,Icon> grayIcons=new HashMap<>();
    private final Map<Integer,Attributes> attributes=new HashMap<>();
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
            var detail=new Attributes();detail.action=pocket==4?4:Byte.toUnsignedInt(data.get(10))>0&&data.get(10)!=7?1:0;
            if(kind==4)detail.ball=Byte.toUnsignedInt(data.get(16));
            attributes.put(5000+index,detail);
        }
        var text=new NdsArchive(RomReader.readFile(rom,"a/0/0/2"));
        names=BwText.decode(text.readRaw(54));descriptions=BwText.decode(text.readRaw(53));
        // LocalizationMessageFormatter's BW banks, including references used by built-in item names.
        strings.registerRomMessages(2,0,54,245000,0,names);strings.registerRomMessages(2,0,53,135000,0,descriptions);
        strings.registerRomMessages(2,0,203,110000,0,BwText.decode(text.readRaw(203)));
        strings.registerRomMessages(2,0,202,120000,0,BwText.decode(text.readRaw(202)));
        var typeNames=BwText.decode(text.readRaw(199));
        for(int type=0;type<18;type++)if(type!=9){int entry=type>9?type-1:type;strings.registerRomMessages(2,0,199,230000+type,entry,typeNames[entry]);}
        // BlackWhiteRom.loadItems: ARM9's TM/HM move table follows this four-word signature.
        byte[] arm=BwPokemonSprites.arm9(rom),signature=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).putInt(56165208).putInt(56492888).putInt(59114373).putInt(59245447).array();
        int moveTable=-1;for(int p=0;p<=arm.length-signature.length;p++)if(Arrays.equals(arm,p,p+signature.length,signature,0,signature.length)){moveTable=p+signature.length;break;}
        if(moveTable<0)throw new IOException("Missing BW item move table");
        var moveData=ByteBuffer.wrap(arm).order(ByteOrder.LITTLE_ENDIAN);moveData.position(moveTable);
        for(var t:templates.values().stream().filter(t->t.region()==2&&t.pocket()==4).sorted(Comparator.comparingInt(Template::id)).toList())attributes.get(t.id()).move=Short.toUnsignedInt(moveData.getShort());
        // LocalizationMessageFormatter: IDs 2 and 3 are ROM messages, not XML literals.
        pocketNames.put(2,romText(text,17,7));pocketNames.put(3,romText(text,55,2));
        ReferenceItemDefinitions.load(this,resources.roms().get(RomType.EMERALD)!=null);
        for(var section:BwPokedexDataPackage.sections(resources.apply("data.pak")))if(section.type()==3)overlayItems(section.data());
        dex=new BwPokedex(rom,strings);dex.loadDataPackage(resources.apply("data.pak"));
        // VanityItemDefinitionRegistry.rn0: ordinary TMs use the icon for their move's type.
        int[] typeIcons={5332,5335,5367,5411,5353,5350,5403,5392,5401,0,5362,5345,5349,5351,5330,5340,5329,5328};
        for(var t:templates.values())if(t.pocket()==4&&attributes(t.id()).move>0&&!hiddenMachine(t.id())){
            int type=dex.move(attributes(t.id()).move).type();if(type>=0&&type<typeIcons.length)iconSources.put(t.id(),typeIcons[type]);
        }
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
    void define(int id,int icon,int name,int description,int price,int pocket){
        int group=switch(pocket){case 1->129;case 2->210;default->pocket*100;};
        templates.put(id,new Template(id,pocket,price,group,10,9999));attributes.put(id,new Attributes());
        iconSources.put(id,icon);nameIds.put(id,name);descriptionIds.put(id,description);
    }
    void definePotion(int id,int source,int strength){
        var base=template(source);templates.put(id,new Template(id,base.pocket(),base.price(),base.group(),base.region(),base.maximumStack()));
        attributes.put(id,new Attributes(attributes(source)));copySources.put(id,source);iconSources.put(id,source);potionNames.put(id,new int[]{source,strength});
    }
    void register(int slot,top.totodile.pokemmo.server.game.PlayerData.Item item){
        if(slot<1||slot>9)throw new IllegalArgumentException("Invalid hotbar slot");
        resources.preferences().save(Map.of("client.hotbar.item_id"+slot,Integer.toString(item.itemId()),"client.hotbar.item_object_id"+slot,item.objectId()));
    }
    /** DataPackageLoader case 3 -> SessionDisconnectPacket.LI: item masks, aliases and template copies. */
    private void overlayItems(ByteBuffer b)throws IOException {
        var copies=new LinkedHashMap<Integer,Integer>();var copyMasks=new HashMap<Integer,Integer>();int count=Short.toUnsignedInt(b.getShort());
        for(int i=0;i<count;i++){
            int id=Short.toUnsignedInt(b.getShort()),mask=b.getInt();var old=template(id);int price=old.price(),pocket=old.pocket(),group=old.group();
            var detail=new Attributes(attributes.getOrDefault(aliases.getOrDefault(id,id),new Attributes()));
            if((mask&1)!=0){price=b.getInt();detail.sellPrice=b.getInt();}if((mask&2)!=0){detail.tradable=b.get()==1;detail.bound=b.get()==1;}
            if((mask&4)!=0){pocket=Byte.toUnsignedInt(b.get());group=switch(pocket){case 1->129;case 2->210;default->pocket*100;};}
            if((mask&8)!=0)group=b.getShort();if((mask&16)!=0){detail.action=Byte.toUnsignedInt(b.get());detail.battleAction=Byte.toUnsignedInt(b.get());}
            if((mask&32)!=0)aliases.put(id,Short.toUnsignedInt(b.getShort()));
            if((mask&64)!=0){copies.put(id,Short.toUnsignedInt(b.getShort()));copyMasks.put(id,mask);}
            if((mask&128)!=0){detail.recovery=b.getShort();detail.percent=b.get()==1;skip(b,2);}if((mask&256)!=0)skip(b,2);if((mask&512)!=0)detail.move=b.getShort();
            if((mask&1024)!=0){detail.stat=b.get();detail.statAmount=b.getShort();}if((mask&2048)!=0)skip(b,3);if((mask&4096)!=0)skip(b,1);
            if((mask&8192)!=0)detail.buyable=true;if((mask&32768)!=0)detail.depositable=false;if((mask&8388608)!=0)detail.holdable=false;
            if((mask&65536)!=0)skip(b,2);
            if((mask&131072)!=0){
                int kind=Byte.toUnsignedInt(b.get());switch(kind){case 2,3,6->skip(b,4);case 5->skip(b,5);case 4->skip(b,3);}
                int effects=Byte.toUnsignedInt(b.get());for(int j=0;j<effects;j++){b.getInt();int parameters=Byte.toUnsignedInt(b.get());for(int k=0;k<parameters;k++)BwPokedexDataPackage.skipEffectParameter(b);}
            }
            if((mask&524288)!=0){detail.friendship=b.getShort();skip(b,1);}if((mask&1048576)!=0)skip(b,1);
            if((mask&2097152)!=0)skip(b,Short.toUnsignedInt(b.getShort())*2);
            if((mask&4194304)!=0)skip(b,4);if((mask&16777216)!=0)skip(b,1);if((mask&33554432)!=0)skip(b,1);if((mask&67108864)!=0)skip(b,2);
            templates.put(id,new Template(id,pocket,price,group,old.region(),old.maximumStack()));
            attributes.put(id,detail);
        }
        for(var entry:copies.entrySet()){
            int id=entry.getKey(),source=entry.getValue(),mask=copyMasks.get(id);var original=template(source);var target=template(id);
            templates.put(id,new Template(id,(mask&4)!=0?target.pocket():original.pocket(),(mask&1)!=0?target.price():original.price(),(mask&8)!=0?target.group():(mask&4)!=0?target.group():original.group(),original.region(),original.maximumStack()));
            iconSources.put(id,source);
            copySources.put(id,source);
            var copied=new Attributes(attributes.getOrDefault(aliases.getOrDefault(source,source),new Attributes()));var targetDetail=attributes.get(id);
            if((mask&2)!=0){copied.tradable=targetDetail.tradable;copied.bound=targetDetail.bound;}
            if((mask&1)!=0)copied.sellPrice=targetDetail.sellPrice;
            attributes.put(id,copied);
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
    private int textSource(int id){var visited=new HashSet<Integer>();while(visited.add(id)){int next=aliases.getOrDefault(id,copySources.getOrDefault(id,id));if(next==id)break;id=next;}return id;}
    public String name(int id){if(dex!=null&&template(id).pocket()==4&&attributes(id).move>0)return rawName(hiddenMachine(id)?5420:5328).replaceAll("[0-9０-９,]{2,10}(?!\\})","")+" - "+dex.moveName(attributes(id).move);var potion=potionNames.get(id);if(potion!=null)return name(potion[1]).replaceAll("(?i)"+java.util.regex.Pattern.quote(name(5017)),java.util.regex.Matcher.quoteReplacement(name(potion[0])));return rawName(id);}
    private String rawName(int id){id=textSource(id);if(nameIds.containsKey(id)){int key=nameIds.get(id);return key>=245000&&key<245627?text(key-240000,54,names,240000):strings.get(key);}if(id<5000)return strings.getOrDefault(240000+id,gbaNames.getOrDefault(id,strings.get(1450)));return templates.containsKey(id)?text(id,54,names,240000):strings.getOrDefault(240000+id,strings.get(1450));}
    public String description(int id){if(dex!=null&&template(id).pocket()==4&&attributes(id).move>0)return dex.moveDescription(attributes(id).move);id=textSource(id);if(descriptionIds.containsKey(id)){int key=descriptionIds.get(id);return key<0?"":key>=135000&&key<135627?text(key-130000,53,descriptions,130000):strings.get(key);}if(id<5000)return strings.getOrDefault(130000+id,gbaDescriptions.getOrDefault(id,strings.get(1451)));return templates.containsKey(id)?text(id,53,descriptions,130000):strings.getOrDefault(130000+id,strings.get(1451));}
    int group(int id){if(dex!=null&&template(id).pocket()==4&&attributes(id).move>0)return hiddenMachine(id)?998:1000+dex.move(attributes(id).move).type();return template(id).group();}
    String groupTitle(int id){int group=group(id);if(template(id).pocket()==4&&attributes(id).move>0)return hiddenMachine(id)?strings.get(10798):strings.format(10799,strings.get(230000+dex.move(attributes(id).move).type()));return group<800?strings.getOrDefault(10000+group,""):"";}
    Attributes attributes(int id){return attributes.getOrDefault(aliases.getOrDefault(id,id),new Attributes());}
    boolean hiddenMachine(int id){int n=aliases.getOrDefault(id,id);return n>=339&&n<=346||n>=5420&&n<=5425||n>=8420&&n<=8427||n>=1297&&n<=1298||n>=9420&&n<=9427;}
    public String tooltip(int id,boolean mobile){
        var a=attributes(id);var t=template(id);var details=new StringBuilder();
        int sell=a.sellPrice>0&&a.sellPrice<=t.price()?a.sellPrice:t.price()/2;
        if(t.pocket()!=2){if(t.price()!=0&&a.buyable)details.append(strings.format(1926,java.text.NumberFormat.getInstance().format(t.price()))).append('\n');if(sell!=0)details.append(strings.format(1930,java.text.NumberFormat.getInstance().format(sell))).append('\n');}
        if(!a.tradable||t.pocket()==2||hiddenMachine(id))details.append(strings.get(1425)).append('\n');
        if(a.bound)details.append(strings.get(1447)).append('\n');
        if(!a.depositable)details.append(strings.get(1445)).append('\n');
        if(!a.holdable)details.append(strings.get(1436)).append('\n');
        if(a.recovery>0)details.append(strings.format(a.percent?1427:1426,Integer.toString(a.recovery))).append('\n');
        String desc=top.totodile.pokemmo.client.ui.common.WordWrapTextFormatter.w70(description(id),mobile?38:22);
        return name(id)+"\n"+(details.isEmpty()?"":"--------\n"+details)+"--------\n"+desc;
    }
    public Image tabIcon(int id,boolean selected){return image(id,!selected);}
    public Image icon(int id){return image(id,false);}
    /** BwTrainerModelLoader.F10: resource-package override first, then the ROM sprite registry. */
    private Image image(int id,boolean gray){
        var cache=gray?grayIcons:icons;var cached=cache.get(id);if(cached!=null)return cached.image;
        try{
            if(!loaded)load();int imageId=source(id);
            var external=resources.apply("sprites/itemicons/"+imageId+".png");
            if(!external.exists()){imageId=id;external=resources.apply("sprites/itemicons/"+id+".png");}
            if(external.exists()){
                var url=new java.net.URL(null,"asset:/sprites/itemicons/"+imageId+".png",new AssetUrlStreamHandler(resources));
                Image image;
                if(gray)image=renderer.grayscale(url);
                else {var texture=renderer.loadTexture(url,null,"nearest");image=texture.getImage(0,0,texture.getWidth(),texture.getHeight(),de.matthiasmann.twl.Color.WHITE,false,de.matthiasmann.twl.renderer.Texture.Rotation.NONE);}
                cache.put(id,new Icon(null,image));return image;
            }
            var p=sprites.item(source(id),gray);var pixmap=new Pixmap(p.width(),p.height(),Pixmap.Format.RGBA8888);
            try{
                pixmap.setBlending(Pixmap.Blending.None);
                for(int y=0;y<p.height();y++)for(int x=0;x<p.width();x++)pixmap.drawPixel(x,y,p.rgba()[y*p.width()+x]);
                var texture=new Texture(pixmap);texture.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);
                var result=new Icon(texture,renderer.image(texture));cache.put(id,result);return result.image;
            }finally{pixmap.dispose();}
        }catch(IOException e){return null;}
    }
    @Override public void close(){for(var cache:List.of(icons,grayIcons)){cache.values().forEach(i->{if(i.texture!=null)i.texture.dispose();});cache.clear();}}
}
