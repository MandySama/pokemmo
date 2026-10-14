package top.totodile.pokemmo.rom;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 白金 BPLC 面元生成 32×32 高度场，保留重叠道路层。 */
final class PlatinumTerrain {
    private record Point(int fx, int x, int fy, int y) { }
    private record Normal(int x, int y, int z) { }
    private record Plane(Point from, Point to, Normal normal, float distance) {
        float height(int x, int y) {
            return distance * 4096f / normal.y()
                    - normal.x() / (float) normal.y() * (x - 15.5f)
                    - normal.z() / (float) normal.y() * (y - 15.5f);
        }
    }

    private PlatinumTerrain() { }

    static List<NdsMaps.Terrain> decode(ByteBuffer data, short[] attributes) {
        List<float[]> layers = new ArrayList<>();
        float[] base = new float[1024]; Arrays.fill(base, Float.NaN); layers.add(base);
        if (data.remaining() >= 16 && data.getInt(0) == 1128809538) {
            data.getInt();
            int pointCount = Short.toUnsignedInt(data.getShort()), normalCount = Short.toUnsignedInt(data.getShort());
            int distanceCount = Short.toUnsignedInt(data.getShort()), planeCount = Short.toUnsignedInt(data.getShort());
            data.getShort(); data.getShort();
            Point[] points = new Point[pointCount];
            for (int i = 0; i < points.length; i++) points[i] = new Point(data.getShort(), data.getShort(), data.getShort(), data.getShort());
            Normal[] normals = new Normal[normalCount];
            for (int i = 0; i < normals.length; i++) normals[i] = new Normal(data.getInt(), data.getInt(), data.getInt());
            float[] distances = new float[distanceCount];
            for (int i = 0; i < distances.length; i++) distances[i] = -(Short.toUnsignedInt(data.getShort()) / 65536f + data.getShort());
            Plane[] planes = new Plane[planeCount];
            for (int i = 0; i < planes.length; i++) {
                planes[i] = new Plane(points[Short.toUnsignedInt(data.getShort())], points[Short.toUnsignedInt(data.getShort())],
                        normals[Short.toUnsignedInt(data.getShort())], distances[Short.toUnsignedInt(data.getShort())]);
            }
            for (Plane plane : planes) {
                if (plane.normal().y() == 0) continue;
                for (int y = Math.max(0, plane.from().y() + 16); y < Math.min(32, plane.to().y() + 16); y++) {
                    for (int x = Math.max(0, plane.from().x() + 16); x < Math.min(32, plane.to().x() + 16); x++) {
                        float height = plane.height(x, y);
                        int cell = y * 32 + x;
                        int layer = 0;
                        while (layer < layers.size() && !Float.isNaN(layers.get(layer)[cell])) {
                            if (Math.abs(layers.get(layer)[cell] - height) <= 1.6f) {
                                if (plane.normal().x() == 0 && plane.normal().z() == 0) layers.get(layer)[cell] = height;
                                break;
                            }
                            layer++;
                        }
                        if (layer >= 4) continue;
                        if (layer == layers.size()) { float[] extra = new float[1024]; Arrays.fill(extra, Float.NaN); layers.add(extra); }
                        if (Float.isNaN(layers.get(layer)[cell])) layers.get(layer)[cell] = height;
                    }
                }
            }
            for (Plane plane : planes) {
                Point from = plane.from(), to = plane.to();
                if (plane.normal().y() == 0 || from.fx() == 0 && from.fy() == 0 && to.fx() == 0 && to.fy() == 0) continue;
                int width = Math.abs(from.x() - to.x() - (to.fx() == 0 ? 0 : 1));
                int height = Math.abs(from.y() - to.y() - (to.fy() == 0 ? 0 : 1));
                int left = from.x() + 16, top = from.y() + 16;
                for (int y = Math.max(0, top); y < Math.min(32, top + height); y++) {
                    for (int x = Math.max(0, left); x < Math.min(32, left + width); x++) {
                        int cell = y * 32 + x;
                        if ((x == left || x == left + width - 1 || y == top || y == top + height - 1)
                                && Float.isNaN(layers.getFirst()[cell])) layers.getFirst()[cell] = plane.height(x, y);
                    }
                }
            }
        }
        List<NdsMaps.Terrain> result = new ArrayList<>();
        for (int layer = 0; layer < layers.size(); layer++) {
            float[] heights = layers.get(layer);
            short[][] channels = new short[4][1024];
            for (int cell = 0; cell < attributes.length; cell++) {
                int flags = attributes[cell] >>> 8 & 255;
                int behavior = attributes[cell] & 255;
                channels[2][cell] = (short) behavior;
                boolean blocked = behavior != 105 && ((flags & 128) != 0 || behavior >= 90 && behavior <= 94);
                channels[3][cell] = (short) ((blocked || Float.isNaN(heights[cell])) ? 1 : 0);
                if (Float.isNaN(heights[cell])) heights[cell] = -10;
            }
            result.add(new NdsMaps.Terrain(32, 32, channels, new short[0][4], heights));
        }
        return List.copyOf(result);
    }
}
