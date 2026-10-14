package top.totodile.pokemmo.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/** NDS 地形的高度、平面法线查表；高度单位为地图格。 */
public final class NdsTerrainHeights {
    private static final float[] HEIGHTS;
    private static final float[][] NORMALS;

    static {
        try (var raw = NdsTerrainHeights.class.getResourceAsStream("/maps/nds-heights.gz")) {
            if (raw == null) throw new IOException("Missing NDS height tables");
            try (var input = new DataInputStream(new GZIPInputStream(raw))) {
                if (input.readInt() != 0x4e444831) throw new IOException("Invalid NDS height tables");
                HEIGHTS = new float[input.readInt()];
                for (int i = 0; i < HEIGHTS.length; i++) HEIGHTS[i] = input.readFloat();
                NORMALS = new float[input.readInt()][];
                for (int i = 0; i < NORMALS.length; i++) {
                    NORMALS[i] = new float[input.readInt()];
                    for (int j = 0; j < NORMALS[i].length; j++) NORMALS[i][j] = input.readFloat();
                }
            }
        } catch (IOException error) {
            throw new ExceptionInInitializerError(error);
        }
    }

    private NdsTerrainHeights() {
    }

    public static float height(int form, int index, int x, int y, int width, int height, short[][] planes) {
        int normal = form >> 2;
        if (form == 2) {
            if (index >= planes.length) return 0;
            normal = planes[index][1];
            index = planes[index][3];
        }
        if (index < 0 || index >= HEIGHTS.length) index = 0;
        if (form == 0) return HEIGHTS[index];
        if (normal < 0 || normal >= NORMALS.length) normal = 0;
        float[] vector = NORMALS[normal];
        return (x - width * 0.5f + 0.5f) * vector[0]
                + (y - height * 0.5f + 0.5f) * vector[1] + HEIGHTS[index] * vector[2];
    }
}
