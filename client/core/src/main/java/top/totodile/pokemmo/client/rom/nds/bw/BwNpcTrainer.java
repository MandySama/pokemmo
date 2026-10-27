package top.totodile.pokemmo.client.rom.nds.bw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.random.RandomGenerator;

/** BlackWhiteRom.Ep/jx, BwTrainerDataEntry.A5 and CharacterAvatarAccessoryProvider.KN. */
public record BwNpcTrainer(int id, int portrait, String type, String name,
                           BwPokemonSprites.Pixels pixels) {
    public static BwNpcTrainer random(BwPokedex dex, RandomGenerator random) throws IOException {
        var data = dex.archive("a/0/9/2");
        var candidates = new ArrayList<Integer>();
        for (int i = 0; i < data.size(); i++) {
            byte[] entry = data.readRaw(i);
            if (entry.length < 20 || (entry[3] & 255) == 0) continue;
            int portrait = portrait(entry[1] & 255);
            // These three use a separate animated bitmap path in the reference loader.
            if (portrait != 37 && portrait != 38 && portrait != 40) candidates.add(i);
        }
        if (candidates.isEmpty()) throw new IOException("No BW NPC trainers available");
        int id = candidates.get(random.nextInt(candidates.size()));
        int type = data.readRaw(id)[1] & 255, portrait = portrait(type);
        var graphics = dex.archive("a/0/7/2");
        var pixels = BwPokemonSprites.compose(graphics.read(portrait * 8),
                graphics.read(portrait * 8 + 7), graphics.read(portrait * 8 + 2), 0, 80, 0);
        return new BwNpcTrainer(id, portrait, dex.trainerClass(type), dex.trainerName(id), pixels);
    }

    private static int portrait(int type) {
        if (type < 47) return type;
        if (type == 47) return 40;
        if (type < 92) return type - 1;
        return switch (type) {
            case 92 -> 71; case 93 -> 4; case 94 -> 70; case 95 -> 74;
            case 96 -> 75; case 97 -> 69; case 98 -> 42; case 99 -> 41;
            case 100 -> 91; case 101 -> 40; case 102 -> 92; case 103 -> 93;
            case 104 -> 94; default -> 0;
        };
    }
}
