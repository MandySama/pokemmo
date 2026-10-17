import top.totodile.pokemmo.rom.FireRedRegionMap;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

/** Exports the same regional image and section coordinates used by the game. */
public final class ExportKantoOverview {
    public static void main(String[] args) throws Exception {
        FireRedRegionMap map = FireRedRegionMap.load(Files.readAllBytes(Path.of(args[0])));
        Path output = Path.of(args[1]);
        Files.createDirectories(output);
        ImageIO.write(map.image(0), "png", output.resolve("kanto-overview.png").toFile());
        StringBuilder json = new StringBuilder("{");
        for (int section = 0; section < 256; section++) {
            int[] position = map.position(section);
            if (position == null || position[0] != 0) continue;
            if (json.length() > 1) json.append(',');
            json.append('"').append(section).append("\":[")
                    .append(map.pixelX(position[1])).append(',')
                    .append(map.pixelY(position[2])).append(']');
        }
        Files.writeString(output.resolve("kanto-positions.json"), json.append('}').toString());
    }
}
