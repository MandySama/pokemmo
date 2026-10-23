package top.totodile.pokemmo.client.rom.nds.gen4;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/** DpptHgssMapTerrainGrid / NdsTerrainChunkFooter / NdsTerrainElevationChunk. */
final class Gen4Terrain {
    record Matrix(int width,int height,int[] headers,int[] land,float[] heights) {}
    record Building(int id,float x,float y,float z) {}
    record Chunk(int[] cells,float[] heights,byte[] model,Building[] buildings) {}
    static ByteBuffer buffer(byte[] data) {return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);}
    static Matrix matrix(byte[] data) {
        var b=buffer(data);int width=b.get()&255,height=b.get()&255;
        boolean indexed=b.get()!=0,elevated=b.get()!=0;
        int name=b.get()&255;b.position(b.position()+name);
        int count=width*height;
        if(count<1||count>65536)throw new IllegalArgumentException("Invalid Gen4 matrix size");
        int[] headers=indexed?new int[count]:null,land=new int[count];float[] heights=new float[count];
        if(indexed)for(int i=0;i<count;i++)headers[i]=b.getShort();
        // The reference grid stores byte / 8 in tile units; Nitro model units are 16 per tile.
        if(elevated)for(int i=0;i<count;i++)heights[i]=b.get()*2f;
        for(int i=0;i<count;i++)land[i]=b.getShort();
        return new Matrix(width,height,headers,land,heights);
    }
    static Chunk chunk(byte[] data,boolean hgss) {
        var b=buffer(data);
        int movementSize=b.getInt(),buildingSize=b.getInt(),modelSize=b.getInt(),heightSize=b.getInt();
        int header=hgss?20:16,extra=hgss?Short.toUnsignedInt(b.getShort(18)):0;
        int cellsAt=header+extra,buildingsAt=cellsAt+movementSize,modelAt=buildingsAt+buildingSize,heightAt=modelAt+modelSize;
        // Platinum land 506 includes two padding bytes after its 48-byte placement records.
        if(movementSize<2048||buildingSize<0||modelSize<0||heightSize<0||heightAt>data.length-heightSize)
            throw new IllegalArgumentException("Invalid Gen4 terrain sections");
        int[] cells=new int[1024];b.position(cellsAt);
        for(int i=0;i<cells.length;i++)cells[i]=Short.toUnsignedInt(b.getShort());
        Building[] buildings=new Building[buildingSize/48];
        for(int i=0;i<buildings.length;i++) {
            b.position(buildingsAt+i*48);
            // Placement fixed-point coordinates are in tiles. The reference building factory
            // converts them to 0.25-unit tiles; our renderer uses 16-unit Nitro tiles.
            buildings[i]=new Building(b.getInt(),b.getInt()/4096f,b.getInt()/4096f,b.getInt()/4096f);
        }
        return new Chunk(cells,height(data,heightAt,heightSize),Arrays.copyOfRange(data,modelAt,heightAt),buildings);
    }
    /** The first walkable surface follows the reference's layer 0 slope rasterisation. */
    private static float[] height(byte[] data,int offset,int length) {
        float[] result=new float[1024];
        if(length<16)return result;
        var b=buffer(data);b.position(offset);
        if(b.getInt()!=1128809538)return result;
        int boundsCount=Short.toUnsignedInt(b.getShort()),normalCount=Short.toUnsignedInt(b.getShort()),planeCount=Short.toUnsignedInt(b.getShort());
        int slopes=Short.toUnsignedInt(b.getShort());b.getShort();b.getShort();
        int[][] bounds=new int[boundsCount][4],normals=new int[normalCount][3];float[] planes=new float[planeCount];
        for(var bound:bounds)for(int j=0;j<4;j++)bound[j]=b.getShort();
        for(var normal:normals)for(int j=0;j<3;j++)normal[j]=b.getInt();
        for(int i=0;i<planes.length;i++)planes[i]=-b.getInt()/65536f;
        boolean[] seen=new boolean[1024];
        for(int i=0;i<slopes;i++) {
            int[] from=bounds[Short.toUnsignedInt(b.getShort())],to=bounds[Short.toUnsignedInt(b.getShort())],normal=normals[Short.toUnsignedInt(b.getShort())];
            float plane=planes[Short.toUnsignedInt(b.getShort())];
            if(normal[1]==0)continue;
            int x0=from[1]+16,y0=from[3]+16,x1=to[1]+16,y1=to[3]+16;
            // Non-integral edge records include the final grid cell in the reference's second pass.
            if(to[0]!=0)x1++;if(to[2]!=0)y1++;
            for(int y=Math.max(0,y0);y<Math.min(32,y1);y++)for(int x=Math.max(0,x0);x<Math.min(32,x1);x++) {
                int index=y*32+x;
                float h=plane*4096f/normal[1]-normal[0]/(float)normal[1]*(x-15.5f)-normal[2]/(float)normal[1]*(y-15.5f);
                if(!seen[index]||(Math.abs(result[index]/16f-h)<=1.6f&&normal[0]==0&&normal[2]==0)) {
                    result[index]=h*16f;seen[index]=true;
                }
            }
        }
        return result;
    }
    /** Reference BlzDecompressor: backwards ARM9 LZ stream used by HGSS. */
    static byte[] arm9(byte[] input) {
        int n=input.length;var b=buffer(input);int extra=b.getInt(n-4);
        if(extra==0)return input;
        int header=input[n-5]&255,packed=b.getInt(n-8)&0xffffff;
        if(header<8||header>11||packed<header||packed>n||extra<0||extra>16*1024*1024-n)
            throw new IllegalArgumentException("Invalid compressed ARM9 footer");
        int start=n-packed,source=n-header,destination=n+extra;
        byte[] out=Arrays.copyOf(input,destination);
        while(destination>start) {
            int flags=out[--source]&255;
            for(int bit=7;bit>=0&&destination>start;bit--) {
                if((flags&(1<<bit))==0)out[--destination]=out[--source];
                else {
                    int token=(out[--source]&255)<<8;token|=out[--source]&255;
                    int count=(token>>>12)+3,distance=(token&4095)+3;
                    while(count-->0&&destination>start){destination--;out[destination]=out[destination+distance];}
                }
            }
        }
        return out;
    }
}
