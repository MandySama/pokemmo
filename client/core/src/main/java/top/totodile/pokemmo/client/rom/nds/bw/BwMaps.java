package top.totodile.pokemmo.client.rom.nds.bw;

import java.io.*;
import java.util.*;
import top.totodile.pokemmo.client.rom.*;
import top.totodile.pokemmo.client.rom.nds.NdsArchive;
import top.totodile.pokemmo.client.rom.gba.FireRedMaps.*;
import top.totodile.pokemmo.client.world.WorldMaps;
import top.totodile.pokemmo.server.game.GameService.Position;

/** BW field maps. Layout follows BwMapHeaderEntry, BwMapTerrainGrid, NdsMapLayoutModel,
 * BwMapZoneEvents, NdsFieldLayoutTable and MapTilePropertyReader in the reference client. */
public final class BwMaps implements WorldMaps {
    private static final Tiles TILES=new Tiles(3,new int[][]{new int[768],new int[768]},new int[]{0,1,2},List.of());
    private record Archive(NdsArchive data,int count) {
        byte[] read(int id) {try{return data.readRaw(id);}catch(IOException e){throw new UncheckedIOException(e);}}
    }
    private record Header(int area,int matrix,int events,int location,int kind,int camera,int teleportX,int teleportY) {}
    private record Matrix(int width,int height,int[] land,int[] headers) {}
    private record Bounds(int x,int y,int width,int height,int cellWidth,int cellHeight) {}
    private record Land(byte[] data,int model,int objects,int width,int height,short[][] cells,short[][] slopes) {}
    private record Building(byte[] model,int linked,int dx,int dy,int dz) {}
    private final RomInfo rom;
    private final Header[] headers;
    private final byte[] areas,cameras;
    private final Archive matrices,landArchive,events;
    private final Map<String,Archive> archives=new HashMap<>();
    private final Map<Integer,Matrix> matrixCache=new HashMap<>();
    private final Map<Integer,Bounds> bounds=new HashMap<>();
    private final Map<Integer,Land> lands=new LinkedHashMap<>(32,.75f,true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Integer,Land> e){return size()>64;}
    };
    private final Map<Id,MapData> maps=new HashMap<>();
    private final Map<String,Map<Integer,Building>> buildings=new HashMap<>();
    private final Map<String,byte[]> textures=new HashMap<>();
    private final Set<Id> ids;

    public BwMaps(RomInfo rom) throws IOException {
        if(rom.type()!=RomType.BLACK_WHITE)throw new IllegalArgumentException("Black/White ROM required");
        this.rom=rom;
        matrices=archive("/a/0/0/9");landArchive=archive("/a/0/0/8");events=archive("/a/1/2/5");
        areas=RomReader.readFile(rom,"a/0/1/3");
        cameras=archive("/a/0/6/0").read(0);
        byte[] table=archive("/a/0/1/2").read(0);
        if(table.length%48!=0)throw new IOException("Invalid BW map headers");
        headers=new Header[table.length/48];
        for(int i=0;i<headers.length;i++) {
            int p=i*48;
            headers[i]=new Header(u16(table,p+2),u16(table,p+4),u16(table,p+22),table[p+26]&255,table[p]&255,table[p+29]&254,i32(table,p+36),i32(table,p+44));
        }
        var available=new LinkedHashSet<Id>();
        for(int i=0;i<headers.length;i++)if(bounds(i)!=null)available.add(id(i));
        ids=Collections.unmodifiableSet(available);
    }
    public static Id id(int header){return new Id(header>>>8,header&255);}
    private static int number(Id id){return id.bank()<<8|id.map();}
    @Override public Position start(){return new Position(1,133,14,14,0);}
    @Override public Set<Id> ids(){return ids;}
    @Override public boolean is3d(){return true;}
    public record TeleportPoint(int header,int x,int y,boolean noGrid) {}
    /** TeleportMenuWindow.sL(2), including its native coordinate corrections and no-grid list. */
    public List<TeleportPoint> teleportPoints(){
        var result=new ArrayList<TeleportPoint>();
        for(int id=0;id<headers.length;id++){
            var h=headers[id];if(h.teleportX==0||h.matrix!=0)continue;
            int x=h.teleportX,y=h.teleportY;
            switch(id){case 205-> {x=180;y=147;}case 235->{x=298;y=757;}case 238->{x=725;y=715;}case 319->{x=756;y=648;}case 345->{x=203;y=183;}case 348->{x=358;y=175;}case 355->{x=434;y=120;}case 365->{x=463;y=175;}case 368->{x=592;y=175;}case 370->{x=675;y=204;}case 374->{x=710;y=362;}case 383->{x=478;y=442;}}
            result.add(new TeleportPoint(id,x,y,false));
        }
        result.add(new TeleportPoint(0,43,39,false));result.add(new TeleportPoint(295,43,39,false));
        // MapPointDataBinaryParser.Hm0(2, header), in native header iteration order.
        for(int id:new int[]{28,30,36,66,114,121,137,209,211,214,241,242,243,244,249,255,338,339,340,341})result.add(new TeleportPoint(id,0,0,true));
        return List.copyOf(result);
    }
    @Override public Position teleportPosition(Id id,int x,int y,int direction){
        var region=bounds(number(id));if(region==null)throw new IllegalArgumentException("BW map has no terrain: "+number(id));
        return new Position(id.bank(),id.map(),x-region.x*region.cellWidth,y-region.y*region.cellHeight,direction);
    }
    @Override public boolean includesNeighborGeometry(){return true;}
    @Override public boolean isDoor(int behavior){return behavior==2;}
    @Override public boolean isWater(int behavior){return behavior==1;}
    @Override public boolean blocked(int behavior,int dx,int dy){return false;}
    @Override public int locationStringId(Id id){return 142000+header(id).location;}
    @Override public CameraView camera(Id id) {return camera(id,false);}
    @Override public CameraView camera(Id id,boolean mobile) {
        int p=header(id).camera/2*44;check(cameras,p,44);
        float fov=s16(cameras,p+18)/96f,distance=(s16(cameras,p)/64f+1)*64;
        if(number(id)==28)fov=s16(cameras,p+18)/196f;
        if(number(id)==250)fov=s16(cameras,p+18)/96f-8;
        // BwWorldMapManager.Be: mobile changes both the field of view and orbit distance.
        if(mobile&&number(id)!=250){fov*=.6f;distance=s16(cameras,p)/42f*64;}
        if(number(id)==0)distance*=2;
        float pitch=u16(cameras,p+4)/65536f*360;
        if(number(id)==137){distance*=1.25f;pitch=30;}
        return new CameraView(fov,distance,pitch);
    }
    @Override public Tiles tiles(MapData map){return TILES;}
    @Override public MapData map(Id id){return maps.computeIfAbsent(id,this::readMap);}
    @Override public Position transition(Id id,int x,int y,int direction) {
        int n=number(id);Bounds source=bounds(n);Matrix m=matrix(header(id).matrix);
        if(m.headers==null)return null;
        int gx=source.x*source.cellWidth+x,gy=source.y*source.cellHeight+y;
        int cx=Math.floorDiv(gx,source.cellWidth),cy=Math.floorDiv(gy,source.cellHeight);
        if(cx<0||cy<0||cx>=m.width||cy>=m.height)return null;
        int target=m.headers[cy*m.width+cx];
        if(target<0||target==n||!ids.contains(id(target)))return null;
        Bounds destination=bounds(target);
        int tx=gx-destination.x*destination.cellWidth,ty=gy-destination.y*destination.cellHeight;
        if(tx<0||ty<0||tx>=destination.width*destination.cellWidth||ty>=destination.height*destination.cellHeight)return null;
        Id next=id(target);return new Position(next.bank(),next.map(),tx,ty,direction);
    }

    private MapData readMap(Id id) {
        int n=number(id);Header h=header(id);Bounds b=bounds(n);Matrix m=matrix(h.matrix);
        if(b==null)throw new IllegalArgumentException("BW map has no terrain: "+n);
        int width=b.width*b.cellWidth,height=b.height*b.cellHeight;
        int[] blocks=new int[width*height];Arrays.fill(blocks,0xc00);
        for(int y=0;y<b.height;y++)for(int x=0;x<b.width;x++) {
            int chunk=m.land[(y+b.y)*m.width+x+b.x];if(chunk<0)continue;
            Land land=land(chunk);
            if(land.cells.length==0)continue;
            for(int ty=0;ty<land.height;ty++)for(int tx=0;tx<land.width;tx++) {
                int flags=land.cells[ty*land.width+tx][3]&65535;
                blocks[(y*b.cellHeight+ty)*width+x*b.cellWidth+tx]=((flags&1)!=0?0xc00:0)|((flags&2)!=0?1:0);
            }
        }
        List<Warp> warps=warps(n,b);
        for(Warp w:warps)if(w.x()>=0&&w.y()>=0&&w.x()<width&&w.y()<height)
            blocks[w.y()*width+w.x()]=2;
        var connections=new LinkedHashSet<Connection>();
        if(m.headers!=null) {
            for(int x=0;x<b.width;x++) {
                connect(connections,n,m,b,b.x+x,b.y-1,2);
                connect(connections,n,m,b,b.x+x,b.y+b.height,1);
            }
            for(int y=0;y<b.height;y++) {
                connect(connections,n,m,b,b.x-1,b.y+y,3);
                connect(connections,n,m,b,b.x+b.width,b.y+y,4);
            }
        }
        return new MapData(id,width,height,1,1,blocks,new int[]{0xc00},n,0,warps,List.copyOf(connections));
    }
    private void connect(Set<Connection> out,int source,Matrix matrix,Bounds a,int x,int y,int direction) {
        if(x<0||y<0||x>=matrix.width||y>=matrix.height)return;
        int target=matrix.headers[y*matrix.width+x];
        if(target<0||target==source||target>=headers.length)return;
        Bounds b=bounds(target);if(b==null)return;
        // Only a matching physical edge can use the shared rectangular-map connection contract.
        if(direction==1&&b.y!=a.y+a.height||direction==2&&b.y+b.height!=a.y
                ||direction==3&&b.x+b.width!=a.x||direction==4&&b.x!=a.x+a.width)return;
        int offset=direction<=2?(b.x-a.x)*a.cellWidth:(b.y-a.y)*a.cellHeight;
        out.add(new Connection(direction,offset,id(target)));
    }
    private List<Warp> warps(int number,Bounds bounds) {
        int event=headers[number].events;if(event==65535||event>=events.count)return List.of();
        byte[] data=events.read(event);if(data.length<8||i32(data,0)<1)return List.of();
        int count=data[6]&255,p=8+(data[4]&255)*20+(data[5]&255)*36;
        var result=new ArrayList<Warp>();var extra=new ArrayList<Warp>();
        for(int i=0;i<count;i++,p+=20) {
            check(data,p,20);
            int target=u16(data,p),targetWarp=u16(data,p+2),x=s16(data,p+8)>>4,y=s16(data,p+12)>>4;
            // Curved-path warps use a path index instead of rectangular tile coordinates.
            boolean curved=u16(data,p+6)==1;
            if(curved){x=-1;y=-1;}
            else {x-=bounds.x*bounds.cellWidth;y-=bounds.y*bounds.cellHeight;}
            Warp warp=new Warp(x,y,0,targetWarp,id(target));result.add(warp);
            int w=u16(data,p+14),h=u16(data,p+16);
            if(!curved&&w<=32&&h<=32)for(int dy=0;dy<h;dy++)for(int dx=0;dx<w;dx++)
                if(dx!=0||dy!=0)extra.add(new Warp(x+dx,y+dy,0,targetWarp,id(target)));
        }
        result.addAll(extra);return List.copyOf(result);
    }
    @Override public List<ModelPlacement> geometry(Id id) {
        Header h=header(id);Bounds b=bounds(number(id));Matrix m=matrix(h.matrix);
        var result=new ArrayList<ModelPlacement>();
        // BwWorldMapManager loads the surrounding 3x3 chunks at ordinary camera distance.
        for(int cy=-1;cy<=b.height;cy++)for(int cx=-1;cx<=b.width;cx++) {
            if(cx+b.x<0||cy+b.y<0||cx+b.x>=m.width||cy+b.y>=m.height)continue;
            int cell=(cy+b.y)*m.width+cx+b.x,index=m.land[cell];if(index<0)continue;
            Header owner=m.headers!=null&&m.headers[cell]>=0?headers[m.headers[cell]]:h;
            Land land=land(index);int area=owner.area;
            check(areas,area*10,10);int groundTexture=u16(areas,area*10+2);
            boolean indoor=areas[area*10+6]==0;
            int objectsTexture=groundTexture<210?(groundTexture<2?0:(groundTexture-2)/4):groundTexture-210;
            float x=(cx*b.cellWidth+b.cellWidth*.5f)*16,z=(cy*b.cellHeight+b.cellHeight*.5f)*16;
            result.add(new ModelPlacement(model(land.data,land.model),texture("/a/0/1/4",groundTexture),x,0,z,0));
            String objectPath=indoor?"/a/2/3/0":"/a/2/2/9";
            String texturePath=indoor?"/a/1/7/7":"/a/1/7/6";
            var library=buildings(objectPath,objectsTexture);
            byte[] texture=texture(texturePath,objectsTexture);
            int count=i32(land.data,land.objects),p=land.objects+4;
            if(count<0||count>4096)throw new IllegalArgumentException("Invalid BW building count");
            for(int i=0;i<count;i++,p+=16) {
                check(land.data,p,16);
                float bx=x+i32(land.data,p)/65536f*16,by=i32(land.data,p+4)/65536f*16,bz=z-i32(land.data,p+8)/65536f*16;
                float rotation=(land.data[p+13]&255)/64f*90;
                int object=(land.data[p+14]&255)<<8|land.data[p+15]&255;
                Building building=library.get(object);if(building==null)continue;
                result.add(new ModelPlacement(building.model,texture,bx,by,bz,rotation));
                Building linked=library.get(building.linked);
                if(linked!=null)result.add(new ModelPlacement(linked.model,texture,
                        bx+building.dx*.016f*64,by,bz+building.dz*.015f*64,rotation));
            }
        }
        return List.copyOf(result);
    }
    @Override public float height(Id id,float x,float y) {
        Bounds b=bounds(number(id));Matrix m=matrix(header(id).matrix);
        int tx=(int)Math.floor(x),ty=(int)Math.floor(y),cx=Math.floorDiv(tx,b.cellWidth),cy=Math.floorDiv(ty,b.cellHeight);
        if(cx+b.x<0||cy+b.y<0||cx+b.x>=m.width||cy+b.y>=m.height)return 0;
        int chunk=m.land[(cy+b.y)*m.width+cx+b.x];if(chunk<0)return 0;
        Land land=land(chunk);if(land.cells.length==0)return 0;
        int lx=Math.floorMod(tx,b.cellWidth),ly=Math.floorMod(ty,b.cellHeight);
        short[] cell=land.cells[ly*land.width+lx];
        int plane=cell[0],elevation=cell[1]&65535;
        if(plane==2) {
            if(elevation>=land.slopes.length)return 0;
            short[] slope=land.slopes[elevation];plane=slope[1]<<2;elevation=slope[3]&65535;
        }
        int slope=plane>>2;
        if(slope<0||slope>=BwElevation.PLANE.length)slope=0;
        if(elevation>=BwElevation.HEIGHT.length)elevation=0;
        float[] normal=BwElevation.PLANE[slope];
        float value=BwElevation.HEIGHT[elevation]*normal[2];
        value+=(x-cx*b.cellWidth-(land.width*.5f-.5f))*normal[0];
        value+=(y-cy*b.cellHeight-(land.height*.5f-.5f))*normal[1];
        return value*16;
    }
    private Bounds bounds(int id) {
        if(bounds.containsKey(id))return bounds.get(id);
        Header h=headers[id];Matrix m=matrix(h.matrix);
        int x1=m.width,y1=m.height,x2=-1,y2=-1,first=-1;
        for(int y=0;y<m.height;y++)for(int x=0;x<m.width;x++) {
            int i=y*m.width+x;
            if(m.land[i]<0||m.headers!=null&&m.headers[i]!=id)continue;
            x1=Math.min(x1,x);y1=Math.min(y1,y);x2=Math.max(x2,x);y2=Math.max(y2,y);first=m.land[i];
        }
        Bounds b=null;
        if(first>=0) {
            Land l=land(first);
            int w=l.cells.length==0&&h.kind==1?128:l.width;
            int height=l.cells.length==0&&h.kind==1?128:l.height;
            b=new Bounds(x1,y1,x2-x1+1,y2-y1+1,w,height);
        }
        bounds.put(id,b);return b;
    }
    private Matrix matrix(int id) {
        return matrixCache.computeIfAbsent(id,n->{
            byte[] data=matrices.read(n);int w=u16(data,4),h=u16(data,6),size=w*h;
            if(w<1||h<1||w>256||h>256)throw new IllegalArgumentException("Invalid BW matrix size");
            int[] land=new int[size],owners=i32(data,0)==1?new int[size]:null;
            for(int i=0;i<size;i++)land[i]=i32(data,8+i*4);
            if(owners!=null)for(int i=0;i<size;i++)owners[i]=i32(data,8+size*4+i*4);
            return new Matrix(w,h,land,owners);
        });
    }
    private Land land(int index) {
        return lands.computeIfAbsent(index,id->{
            byte[] data=landArchive.read(id);int type=i32(data,0),model=i32(data,4);
            int layers=type==149326?0:type==213591||type==214098?1:2;
            int objects=i32(data,8+layers*4),w=32,h=32;
            short[][] cells=new short[0][],slopes=new short[0][];
            if(layers>0) {
                int start=i32(data,8);w=u16(data,start);h=u16(data,start+2);
                if(w<1||h<1||w>128||h>128)throw new IllegalArgumentException("Invalid BW land dimensions");
                cells=new short[w*h][4];int p=start+4;
                for(short[] cell:cells)for(int q=0;q<4;q++) {
                    if(type==214098)p+=4;cell[q]=(short)u16(data,p);p+=2;
                }
                int end=layers>1?i32(data,12):objects,count=(end-p)/8;
                if(count<0||count>65536)throw new IllegalArgumentException("Invalid BW height planes");
                slopes=new short[count][4];
                for(short[] slope:slopes)for(int q=0;q<4;q++){slope[q]=(short)u16(data,p);p+=2;}
            }
            return new Land(data,model,objects,w,h,cells,slopes);
        });
    }
    private Map<Integer,Building> buildings(String path,int index) {
        return buildings.computeIfAbsent(path+":"+index,key->{
            byte[] data=uncheckedArchive(path).read(index);
            if(u16(data,0)!=0x4241)throw new IllegalArgumentException("Invalid BW building library");
            int count=u16(data,2)/2;var result=new HashMap<Integer,Building>();
            for(int i=0;i<count;i++) {
                int record=i32(data,4+i*4),payload=i32(data,4+count*4+i*4);
                result.put(u16(data,record),new Building(model(data,payload),s16(data,record+4),
                        s16(data,record+6),s16(data,record+8),s16(data,record+10)));
            }
            return result;
        });
    }
    private Header header(Id id) {
        int n=number(id);if(n<0||n>=headers.length)throw new IllegalArgumentException("Unknown BW map "+id);return headers[n];
    }
    private byte[] texture(String path,int index){return textures.computeIfAbsent(path+":"+index,key->uncheckedArchive(path).read(index));}
    private Archive uncheckedArchive(String path){try{return archive(path);}catch(IOException e){throw new UncheckedIOException(e);}}
    private Archive archive(String path) throws IOException {
        Archive existing=archives.get(path);if(existing!=null)return existing;
        byte[] data=RomReader.readFile(rom,path.startsWith("/")?path.substring(1):path);int p=u16(data,12),count=-1;
        while(p+12<=data.length){int type=i32(data,p),size=i32(data,p+4);if(type==0x46415442){count=u16(data,p+8);break;}if(size<8)break;p+=size;}
        if(count<0)throw new IOException("Missing BW archive table: "+path);
        var value=new Archive(new NdsArchive(data),count);archives.put(path,value);return value;
    }
    private static byte[] model(byte[] data,int p){if(i32(data,p)!=0x30444d42)throw new IllegalArgumentException("Missing BW BMD0");int length=i32(data,p+8);check(data,p,length);return Arrays.copyOfRange(data,p,p+length);}
    private static void check(byte[] data,int p,int length){if(p<0||length<0||p>data.length-length)throw new IllegalArgumentException("Truncated BW map data");}
    private static int u16(byte[] data,int p){check(data,p,2);return(data[p]&255)|(data[p+1]&255)<<8;}
    private static int s16(byte[] data,int p){return(short)u16(data,p);}
    private static int i32(byte[] data,int p){return u16(data,p)|u16(data,p+2)<<16;}
}
