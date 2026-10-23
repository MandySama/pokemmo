package top.totodile.pokemmo.client.rom.nds.gen4;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.*;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.gba.FireRedMaps.*;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;
import top.totodile.pokemmo.client.world.WorldMaps;
import top.totodile.pokemmo.server.game.GameService.Position;
import static top.totodile.pokemmo.client.rom.nds.gen4.Gen4Terrain.*;

/** Original Platinum/HGSS header, matrix, land, building, texture and event resources.
 * Layouts follow DpptMapHeaderEntry, DpptHgssMapTerrainGrid, NdsTerrainChunkFooter,
 * NdsWildEncounterTable and DpptMapZoneEvents in the reference client.
 */
public final class Gen4Maps implements WorldMaps {
    private record Header(int id,int area,int matrix,int events,int location,int type) {}
    private record Bounds(int x,int y,int width,int height) {}
    private final boolean hgss;
    private final Header[] headers;
    private final Matrix[] matrices;
    private final Bounds[] bounds;
    private final int[] textureIds;
    private final String[] locationNames;
    private final NdsArchive land,textures,buildings,interiorBuildings,events;
    private final Set<Id> ids=new LinkedHashSet<>();
    private final Map<Id,MapData> maps=new HashMap<>();
    private final Map<Integer,Chunk> chunks=lru(48);
    private final Map<Integer,byte[]> textureData=new HashMap<>(),buildingData=new HashMap<>();
    private final Map<Id,List<ModelPlacement>> geometries=lru(6);
    private final Tiles tiles;

    public Gen4Maps(RomInfo rom) throws IOException {
        hgss=rom.type()==RomType.HEART_GOLD_SOUL_SILVER;
        if(!hgss&&rom.type()!=RomType.PLATINUM)throw new IllegalArgumentException("Platinum or HGSS ROM required");
        var matrixArchive=archive(rom,hgss?"a/0/4/1":"fielddata/mapmatrix/map_matrix.narc");
        land=archive(rom,hgss?"a/0/6/5":"fielddata/land_data/land_data.narc");
        textures=archive(rom,hgss?"a/0/4/4":"fielddata/areadata/area_map_tex/map_tex_set.narc");
        buildings=archive(rom,hgss?"a/0/4/0":"fielddata/build_model/build_model.narc");
        interiorBuildings=hgss?archive(rom,"a/1/4/8"):buildings;
        events=archive(rom,hgss?"a/0/3/2":"fielddata/eventdata/zone_event.narc");
        var messages=archive(rom,hgss?"a/0/2/7":"msgdata/pl_msg.narc");
        int locationBank=hgss?279:433;
        // DpptTextBank.E1 adjusts archive indices for Japanese and Korean releases.
        if(rom.code().endsWith("J"))locationBank-=hgss?7:6;
        else if(rom.code().endsWith("K"))locationBank-=5;
        locationNames=Gen4Text.decode(messages.readRaw(locationBank));
        var area=archive(rom,hgss?"a/0/4/2":"fielddata/areadata/area_data.narc");
        textureIds=new int[area.size()];
        for(int i=0;i<textureIds.length;i++)textureIds[i]=Short.toUnsignedInt(buffer(area.readRaw(i)).getShort(2));
        matrices=new Matrix[matrixArchive.size()];
        for(int i=0;i<matrices.length;i++)matrices[i]=matrix(matrixArchive.readRaw(i));
        var cartridge=buffer(RomReader.read(rom,0,0x200));
        byte[] arm=RomReader.read(rom,Integer.toUnsignedLong(cartridge.getInt(0x20)),cartridge.getInt(0x2c));
        if(hgss)arm=arm9(arm);
        var b=buffer(arm);
        int signatureA=hgss?33489410:1573448,signatureB=hgss?11076050:1573449,table=-1;
        for(int p=0;p<=arm.length-8;p+=4)if(b.getInt(p)==signatureA&&b.getInt(p+4)==signatureB){table=p+(hgss?16:8);break;}
        if(table<0)throw new IOException("Gen4 map header table not found");
        int count=RomReader.readFile(rom,"fielddata/maptable/mapname.bin").length/16;
        if(count<1||table>arm.length-count*24)throw new IOException("Invalid Gen4 map header table");
        headers=new Header[count];bounds=new Bounds[count];
        for(int i=0;i<count;i++) {
            int p=table+i*24;
            headers[i]=new Header(i,arm[p+(hgss?1:0)]&255,Short.toUnsignedInt(b.getShort(p+(hgss?4:2))),
                    Short.toUnsignedInt(b.getShort(p+16)),arm[p+18]&255,Short.toUnsignedInt(b.getShort(p+(hgss?21:22)))&7);
            if(headers[i].matrix>=matrices.length)throw new IOException("Gen4 header has invalid matrix: "+i);
            Matrix m=matrices[headers[i].matrix];
            if(m.headers()==null)bounds[i]=new Bounds(0,0,m.width(),m.height());
            else {
                int minX=m.width(),minY=m.height(),maxX=-1,maxY=-1;
                for(int y=0;y<m.height();y++)for(int x=0;x<m.width();x++)if(m.headers()[y*m.width()+x]==i&&m.land()[y*m.width()+x]>=0) {
                    minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);
                }
                if(maxX>=minX)bounds[i]=new Bounds(minX,minY,maxX-minX+1,maxY-minY+1);
            }
            if(i>1&&bounds[i]!=null)ids.add(id(i));
        }
        // Movement consumes the original low byte behaviour; rendering consumes Nitro models.
        int[] attributes=new int[256];for(int i=0;i<256;i++)attributes[i]=i;
        tiles=new Tiles(256,new int[][]{new int[256*256],new int[256*256]},attributes,List.of());
    }
    private static <K,V> Map<K,V> lru(int size) {
        return new LinkedHashMap<>(16,.75f,true){protected boolean removeEldestEntry(Map.Entry<K,V> e){return size()>size;}};
    }
    private static NdsArchive archive(RomInfo rom,String path)throws IOException{return new NdsArchive(RomReader.readFile(rom,path));}
    private static byte[] read(NdsArchive archive,int index) {
        try{return archive.readRaw(index);}catch(IOException e){throw new UncheckedIOException(e);}
    }
    private static Id id(int header){return new Id(header>>>8,header&255);}
    private int index(Id id) {
        int value=id.bank()*256+id.map();
        if(!ids.contains(id))throw new IllegalArgumentException("Unknown Gen4 map "+id);
        return value;
    }
    private Chunk chunk(int id){return chunks.computeIfAbsent(id,i->{
        try{return Gen4Terrain.chunk(read(land,i),hgss);}
        catch(RuntimeException error){throw new IllegalArgumentException("Cannot read Gen4 terrain "+i,error);}
    });}
    @Override public Set<Id> ids(){return Collections.unmodifiableSet(ids);}
    @Override public boolean is3d(){return true;}
    @Override public boolean includesNeighborGeometry(){return true;}
    @Override public Position transition(Id id,int x,int y,int direction) {
        int n=index(id);Header h=headers[n];Bounds region=bounds[n];Matrix matrix=matrices[h.matrix];
        if(matrix.headers()==null)return null;
        int globalX=region.x*32+x,globalY=region.y*32+y;
        int cx=Math.floorDiv(globalX,32),cy=Math.floorDiv(globalY,32);
        if(cx<0||cy<0||cx>=matrix.width()||cy>=matrix.height())return null;
        int cell=cy*matrix.width()+cx,target=matrix.headers()[cell];
        if(target==n||target<0||target>=headers.length||!ids.contains(id(target))||matrix.land()[cell]<0||headers[target].matrix!=h.matrix)return null;
        Bounds destination=bounds[target];
        return new Position(target>>>8,target&255,globalX-destination.x*32,globalY-destination.y*32,direction);
    }
    @Override public boolean isWater(int behavior){return behavior==16||behavior==17||behavior==19||behavior==21;}
    @Override public boolean isDoor(int behavior){return behavior==105;}
    @Override public boolean blocked(int behavior,int dx,int dy){return false;}
    @Override public int locationStringId(Id id){return (hgss?144000:143000)+headers[index(id)].location;}
    @Override public String locationName(Id id){
        int location=headers[index(id)].location;
        return location<locationNames.length?locationNames[location]:null;
    }
    @Override public Tiles tiles(MapData map){return tiles;}
    @Override public MapData map(Id id){return maps.computeIfAbsent(id,this::decodeMap);}
    private MapData decodeMap(Id id) {
        int n=index(id);Header h=headers[n];Bounds region=bounds[n];Matrix m=matrices[h.matrix];
        int width=region.width*32,height=region.height*32;int[] blocks=new int[width*height];Arrays.fill(blocks,0xc00);
        for(int cy=0;cy<region.height;cy++)for(int cx=0;cx<region.width;cx++) {
            int landId=m.land()[(region.y+cy)*m.width()+region.x+cx];if(landId<0)continue;
            Chunk chunk=chunk(landId);
            for(int y=0;y<32;y++)for(int x=0;x<32;x++) {
                int cell=chunk.cells()[y*32+x],behavior=cell&255;
                // WorldNpcEntityModel.LPt1: doors bypass the high-byte collision flag.
                boolean blocked=behavior!=105&&((cell&0x8000)!=0||behavior>=90&&behavior<=94);
                blocks[(cy*32+y)*width+cx*32+x]=behavior|(blocked?0xc00:0);
            }
        }
        List<Warp> warps=new ArrayList<>();
        if(h.events<events.size()) {
            var b=buffer(read(events,h.events));
            int count=b.getInt();b.position(b.position()+count*20);
            count=b.getInt();b.position(b.position()+count*32);
            count=b.getInt();
            for(int i=0;i<count;i++) {
                int x=Short.toUnsignedInt(b.getShort())-region.x*32,y=Short.toUnsignedInt(b.getShort())-region.y*32;
                int target=Short.toUnsignedInt(b.getShort()),targetWarp=Short.toUnsignedInt(b.getShort());b.getInt();
                warps.add(new Warp(x,y,0,targetWarp,id(target)));
            }
        }
        List<Connection> connections=new ArrayList<>();
        if(m.headers()!=null)for(Id other:ids) {
            int k=other.bank()*256+other.map();if(k==n||headers[k].matrix!=h.matrix)continue;
            Bounds target=bounds[k];
            if(region.x<target.x+target.width&&target.x<region.x+region.width) {
                if(region.y+region.height==target.y)connections.add(new Connection(1,(target.x-region.x)*32,other));
                if(target.y+target.height==region.y)connections.add(new Connection(2,(target.x-region.x)*32,other));
            }
            if(region.y<target.y+target.height&&target.y<region.y+region.height) {
                if(target.x+target.width==region.x)connections.add(new Connection(3,(target.y-region.y)*32,other));
                if(region.x+region.width==target.x)connections.add(new Connection(4,(target.y-region.y)*32,other));
            }
        }
        return new MapData(id,width,height,1,1,blocks,new int[]{0xc00},0,0,List.copyOf(warps),List.copyOf(connections));
    }
    @Override public List<ModelPlacement> geometry(Id id) {return geometries.computeIfAbsent(id,this::decodeGeometry);}
    private List<ModelPlacement> decodeGeometry(Id id) {
        int n=index(id);Header h=headers[n];Bounds region=bounds[n];Matrix m=matrices[h.matrix];
        var result=new ArrayList<ModelPlacement>();
        // The exterior matrix is the authoritative neighbour layout, including concave regions.
        int pad=m.headers()==null?0:1;
        for(int cy=Math.max(0,region.y-pad);cy<Math.min(m.height(),region.y+region.height+pad);cy++)
            for(int cx=Math.max(0,region.x-pad);cx<Math.min(m.width(),region.x+region.width+pad);cx++) {
                int cell=cy*m.width()+cx,landId=m.land()[cell];if(landId<0)continue;
                int header=m.headers()==null?n:m.headers()[cell];
                Header owner=header>=0&&header<headers.length?headers[header]:h;
                Chunk chunk=chunk(landId);int textureId=textureIds[owner.area];
                if(!hgss)textureId=switch(landId){case 177,178->14;case 173->6;case 174->15;case 175->7;case 176->12;case 179->19;default->textureId;};
                byte[] texture=textureData.computeIfAbsent(textureId,i->read(textures,i));
                float x=(cx-region.x)*512+256,z=(cy-region.y)*512+256,y=m.heights()[cell];
                if(chunk.model().length>0)result.add(new ModelPlacement(chunk.model(),texture,x,y,z,0));
                // DpptMapHeaderEntry.ts0 selects HGSS's separate interior building archive.
                boolean indoors=owner.type==4||hgss&&switch(owner.id){case 115,220,222,223,224,247,248,249->true;default->false;};
                NdsArchive models=hgss&&indoors?interiorBuildings:buildings;
                if(!hgss&&landId==71)continue;
                for(Building building:chunk.buildings()) {
                    int model=building.id();if(hgss&&model==221)model=23;
                    if(hgss&&indoors&&(model==124||model==125))continue;
                    if(model<0||model>=models.size())continue;
                    int key=model+(models==interiorBuildings&&hgss?65536:0);
                    int selected=model;
                    byte[] bytes=buildingData.computeIfAbsent(key,ignored->read(models,selected));
                    result.add(new ModelPlacement(bytes,null,x+building.x(),y+building.y(),z+building.z(),0));
                }
            }
        return List.copyOf(result);
    }
    @Override public float height(Id id,float x,float y) {
        // GdxParticleEmitterNode.w7 interpolates the complete source/destination position,
        // including height. Cardinal motion makes this the same linear height interpolation.
        int tx=(int)Math.floor(x),ty=(int)Math.floor(y);float fx=x-tx,fy=y-ty;
        float a=sampleHeight(id,tx,ty);if(!Float.isFinite(a))return 0;
        float b=fx==0?a:sampleHeight(id,tx+1,ty),c=fy==0?a:sampleHeight(id,tx,ty+1);
        float d=fx==0?c:fy==0?b:sampleHeight(id,tx+1,ty+1);
        if(!Float.isFinite(b))b=a;if(!Float.isFinite(c))c=a;if(!Float.isFinite(d))d=a;
        return (a+(b-a)*fx)*(1-fy)+(c+(d-c)*fx)*fy;
    }
    private float sampleHeight(Id id,int tx,int ty) {
        int n=index(id);Header h=headers[n];Bounds region=bounds[n];Matrix m=matrices[h.matrix];
        int cx=Math.floorDiv(tx,32)+region.x,cy=Math.floorDiv(ty,32)+region.y;
        if(cx<0||cy<0||cx>=m.width()||cy>=m.height())return Float.NaN;
        int cell=cy*m.width()+cx,landId=m.land()[cell];
        return landId<0?Float.NaN:m.heights()[cell]+chunk(landId).heights()[Math.floorMod(ty,32)*32+Math.floorMod(tx,32)];
    }
    @Override public Position start() {
        Id id=id(hgss?60:411);MapData m=map(id);
        for(Warp warp:m.warps())if(m.contains(warp.x(),warp.y()+1)&&walkable(m.block(warp.x(),warp.y()+1)))
            return new Position(id.bank(),id.map(),warp.x(),warp.y()+1,0);
        for(int y=0;y<m.height();y++)for(int x=0;x<m.width();x++)if(walkable(m.block(x,y)))return new Position(id.bank(),id.map(),x,y,0);
        throw new IllegalStateException("No walkable starting location in Gen4 town");
    }
    private boolean walkable(int block){return (block&0xc00)==0&&!isWater(block&255);}
}
