package top.totodile.pokemmo.server.game;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Reference GiftShopManager / GiftShopItem; configuration is copied unchanged. */
public final class GiftShopData {
    private GiftShopData() {}
    public record Item(int id,int targetType,int category,int itemIndexId,int price,int originalPrice,
                       int quantity,int param1,int releaseTime,int seasonal,int featuredOrder) {}
    private static final List<Item> ITEMS=read();
    public static List<Item> items(){return ITEMS;}
    private static List<Item> read(){
        try(var stream=GiftShopData.class.getResourceAsStream("/gift/GiftShop.jsonc")){
            if(stream==null)throw new FileNotFoundException("GiftShop.jsonc");
            var root=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            var items=new ArrayList<Item>();var ids=new HashSet<Integer>();
            for(var e:root.getAsJsonArray("items")){
                var o=e.getAsJsonObject();var item=new Item(n(o,"id",0),n(o,"targetType",1),n(o,"category",0),n(o,"itemIndexId",0),n(o,"price",0),n(o,"originalPrice",0),n(o,"quantity",1),n(o,"param1",0),n(o,"releaseTime",0),n(o,"seasonal",0),n(o,"featuredOrder",-1));
                if(!ids.add(item.id())||item.category()<0||item.category()>8||item.price()<0||item.quantity()<1)throw new IllegalStateException("Invalid gift shop item");
                items.add(item);
            }
            return List.copyOf(items);
        }catch(IOException e){throw new UncheckedIOException(e);}
    }
    private static int n(JsonObject o,String key,int fallback){return o.has(key)?o.get(key).getAsInt():fallback;}
}
