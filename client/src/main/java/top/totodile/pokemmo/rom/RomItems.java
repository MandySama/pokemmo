package top.totodile.pokemmo.rom;

import top.totodile.pokemmo.RomFiles;
import top.totodile.pokemmo.protocol.ItemCatalog;
import java.io.IOException;
import java.nio.*;
import java.nio.file.*;
import java.util.*;

/** 各卡带保留自己的编号区间；GBA 同编号保留先加载项，自定义项最后覆盖。 */
public final class RomItems {
    private RomItems() { }
    public static Map<Integer, ItemCatalog.Item> load(RomFiles roms) throws IOException {
        Map<Integer, ItemCatalog.Item> result = new TreeMap<>();
        if (roms.selected(0) != null) nds(result,roms.selected(0),0,"/a/0/2/4",5000,2);
        if (roms.selected(3) != null) nds(result,roms.selected(3),3,"/itemtool/itemdata/pl_item_data.narc",8000,3);
        if (roms.selected(4) != null) nds(result,roms.selected(4),4,"/a/0/1/7",9000,4);
        for (int source : new int[]{1,2}) if (roms.selected(source) != null) gba(result,roms.selected(source),source);
        for (var custom : ItemCatalog.items()) if (custom.source() == -1) result.put(custom.id(),custom);
        return Collections.unmodifiableMap(result);
    }
    private static void nds(Map<Integer, ItemCatalog.Item> result, Path rom, int source, String path, int base, int region) throws IOException {
        var archive = new NdsArchive(rom,path);
        for (int index=0;index<archive.size();index++) {
            int id=base+index;
            if (source!=0 && index>=113) id+=22;
            if (source==4 && index>=406) id++;
            var data=archive.file(index);
            if (data.limit()<14) throw new IOException("Truncated item parameters");
            int category=(Short.toUnsignedInt(data.getShort(8))>>>7)&7;
            int pocket=source==0 ? switch(category){case 1->1;case 2->5;case 3->6;case 4->3;default->0;}
                    : switch(category){case 1->1;case 2->4;case 3->5;case 4->6;case 6->2;case 7->3;default->0;};
            int kind=Byte.toUnsignedInt(data.get(13));
            if(kind==4)pocket=4;else if(kind==3)pocket=2;
            result.put(id,new ItemCatalog.Item(id,pocket,pocket==3?1:9999,region,240000+id,id,source));
        }
    }
    private static void gba(Map<Integer, ItemCatalog.Item> result, Path rom, int source) throws IOException {
        byte[] bytes=Files.readAllBytes(rom);
        var data=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        int start=data.getInt(456)-0x08000000,count=source==1?375:377;
        if(start<0 || (long)start+count*44>data.limit())throw new IOException("Invalid GBA item table");
        for(int index=0;index<count;index++) {
            int offset=start+index*44,id=Short.toUnsignedInt(data.getShort(offset+14));
            if(result.containsKey(id))continue;
            int category=Byte.toUnsignedInt(data.get(offset+26));
            int pocket=source==1 ? switch(category){case 2->3;case 3->4;case 4->5;case 5->6;case 6->7;case 7->1;case 8->2;default->0;}
                    : switch(category){case 2->4;case 3->5;case 4->6;case 5->3;case 6->7;case 7->1;case 8->2;default->0;};
            result.put(id,new ItemCatalog.Item(id,pocket,pocket==3?1:9999,source-1,240000+id,id,source));
        }
    }
}
