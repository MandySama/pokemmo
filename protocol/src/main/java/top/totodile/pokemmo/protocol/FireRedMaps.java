package top.totodile.pokemmo.protocol;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** 地图编号和尺寸目录供两端校验位置，不包含卡带像素。 */
public final class FireRedMaps {
    private static final Map<Integer, int[]> SIZES = load();
    private FireRedMaps() { }
    private static Map<Integer, int[]> load() {
        Map<Integer, int[]> sizes = new HashMap<>();
        try (var input = FireRedMaps.class.getResourceAsStream("/maps/fire-red.csv")) {
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
        if (group < 0 || group > 255 || map < 0 || map > 255) return false;
        int[] size = SIZES.get(group * 256 + map);
        return size != null && x >= 0 && y >= 0 && x < size[0] && y < size[1];
    }
}
