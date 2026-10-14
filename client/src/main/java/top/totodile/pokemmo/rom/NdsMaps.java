package top.totodile.pokemmo.rom;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/** NDS 地区的 CPU 地图资源接口，各卡带独立解码。 */
public interface NdsMaps {
    public record Header(int id, int area, int matrix, int events, int name) {
    }

    public record Matrix(int width, int height, int[] models, int[] headers, float[] offsets) {
        public Matrix(int width, int height, int[] models, int[] headers) { this(width, height, models, headers, new float[models.length]); }
        public int model(int x, int y) {
            return models[y * width + x];
        }
    }

    public record Terrain(int width, int height, short[][] channels, short[][] planes, float[] heights) {
        public Terrain(int width, int height, short[][] channels, short[][] planes) { this(width, height, channels, planes, null); }
        public float height(int x, int y) { return heights == null ? top.totodile.pokemmo.protocol.NdsTerrainHeights.height(value(0,x,y),value(1,x,y),x,y,width,height,planes) : heights[y*width+x]; }
        public int value(int channel, int x, int y) {
            return channels[channel][y * width + x] & 65535;
        }

        public boolean blocked(int x, int y) {
            return x < 0 || y < 0 || x >= width || y >= height
                    || value(2, x, y) == 254 || (value(3, x, y) & 1) != 0;
        }
    }

    public record Chunk(int id, ByteBuffer model, List<Terrain> layers, ByteBuffer objects) {
    }

    public record Building(int id, ByteBuffer model) {
    }

    public record Placement(int model, float x, float y, float z, int rotation) {
    }

    public record Warp(int target, int entrance, int direction, int type, boolean special,
                       int x, int elevation, int y, int width, int height) {
    }


    List<Header> headers();
    Header header(int id);
    int chunkCount();
    Chunk chunk(int id) throws IOException;
    Matrix matrix(int id) throws IOException;
    ByteBuffer textures(int headerId, int season) throws IOException;
    boolean indoor(int headerId);
    ByteBuffer buildingTextures(int headerId, int season) throws IOException;
    List<Building> buildings(int headerId, int season) throws IOException;
    List<Placement> placements(Chunk chunk) throws IOException;
    List<Warp> warps(int headerId) throws IOException;
}
