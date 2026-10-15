package top.totodile.pokemmo.rom;

import top.totodile.pokemmo.ClientFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;

/** 中心地图目录与 ROM 电脑地块共同约束交互，排除住宅和室外。 */
public final class PokemonCenters {
    private static final Set<String> MAPS = load();

    private PokemonCenters() {}

    public static boolean contains(int region, int group, int map) {
        return MAPS.contains(region + "," + group + "," + map);
    }

    private static Set<String> load() {
        try (var lines = Files.lines(ClientFiles.data("maps/pokemon-centers.csv"))) {
            return lines.map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException error) {
            throw new java.io.UncheckedIOException(error);
        }
    }
}
