package top.totodile.pokemmo.server.game;

/** PokemonExperienceGrowthRate.Iy0; ROM growth IDs (server exp_type uses a different order). */
public final class PokemonExperience {
    private PokemonExperience() {}

    public static int atLevel(int growth, int level) {
        if (level > 100) return Integer.MAX_VALUE;
        double cube = Math.pow(level, 3);
        return Math.max(
                0,
                (int)
                        switch (growth) {
                            case 0 -> cube;
                            case 1 ->
                                    level <= 50
                                            ? cube * (100 - level) / 50
                                            : level <= 68
                                                    ? cube * (150 - level) / 100
                                                    : level <= 98
                                                            ? ((1911 - level * 10) / 3.0)
                                                                    * cube
                                                                    / 500
                                                            : cube * (160 - level) / 100;
                            case 2 ->
                                    level <= 15
                                            ? ((level + 1) / 3.0 + 24) / 50 * cube
                                            : level <= 36
                                                    ? (level + 14) / 50.0 * cube
                                                    : (level / 2.0 + 32) / 50 * cube;
                            case 3 -> cube * 1.2 - level * 15 * level + level * 100 - 140;
                            case 4 -> cube * 4 / 5;
                            case 5 -> cube * 5 / 4;
                            default ->
                                    throw new IllegalArgumentException(
                                            "Unknown growth rate: " + growth);
                        });
    }
}
