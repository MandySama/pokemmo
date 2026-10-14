package top.totodile.pokemmo.protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** 地图编号和尺寸目录供两端校验位置，不包含卡带像素。 */
public final class FireRedMaps {
    private static final Map<Integer, int[]> SIZES = load("fire-red");
    private static final Map<Integer, int[]> EMERALD_SIZES = load("emerald");
    private static final Map<Integer, int[]> UNOVA_SIZES = load("unova");
    private static final Map<Integer, int[]> SINNOH_SIZES = load("sinnoh");
    private static final Map<Integer, int[]> EMERALD2_SIZES = load("emerald2");
    private FireRedMaps() { }
    private static Map<Integer, int[]> load(String name) {
        Map<Integer, int[]> sizes = new HashMap<>();
        try (var input = FireRedMaps.class.getResourceAsStream("/maps/" + name + ".csv")) {
            if (input == null) throw new IllegalStateException("Missing FireRed map catalog");
            try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    String[] values = line.split(",");
                    int group = Integer.parseInt(values[0]), map = Integer.parseInt(values[1]);
                    sizes.put(group * 256 + map, new int[]{Integer.parseInt(values[2]), Integer.parseInt(values[3])});
                }
            }
        } catch (Exception exception) { throw new ExceptionInInitializerError(exception); }
        return Map.copyOf(sizes);
    }
    public static boolean contains(int group, int map, int x, int y) {
        return contains(0, group, map, x, y);
    }
    public static boolean contains(int region, int group, int map, int x, int y) {
        if (region < 0 || region > 4) return false;
        if (group < 0 || group > 255 || map < 0 || map > 255) return false;
        int[] size = (region == 0 ? SIZES : region == 1 ? EMERALD_SIZES : region == 2 ? UNOVA_SIZES : region == 3 ? SINNOH_SIZES : EMERALD2_SIZES).get(group * 256 + map);
        return size != null && x >= 0 && y >= 0 && x < size[0] && y < size[1];
    }
}
