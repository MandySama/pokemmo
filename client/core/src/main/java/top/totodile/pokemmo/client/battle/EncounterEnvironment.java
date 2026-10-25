package top.totodile.pokemmo.client.battle;

import top.totodile.pokemmo.client.world.BattleTerrain;

/** ROM indices from ViewportBoundingBox/I4 and Gdx3DModelBatch.Wi0; no generated scenery. */
public record EncounterEnvironment(
        int backgroundModel, int backgroundAnimation, int platformModel) {
    // I4: background id, platform, model, animation, seasonal.
    private static final int[][] BACKGROUNDS = {
        {1, 0, 34, 35, 0}, {2, 1, 36, 40, 1}, {3, 5, 44, 45, 0},
        {4, 1, 46, 50, 1}, {5, 6, 54, 55, 0}, {6, 7, 56, 57, 0},
        {7, 8, 58, 59, 0}, {8, 13, 60, 61, 0}, {9, 14, 62, 63, 0},
        {10, 4, 64, 65, 0}, {11, 17, 66, 67, 0}, {12, 21, 70, 71, 0},
        {13, 22, 72, 73, 0}, {14, 23, 74, 75, 0}, {15, 24, 76, 77, 0},
        {16, 25, 78, 79, 0}, {17, 27, 82, 83, 0}, {18, 27, 78, 79, 0},
        {100, 23, 82, 35, 0}, {101, 33, 76, 77, 0}, {102, 31, 60, 35, 0}
    };

    /** Map headers override the facility background in Unova, as in Wi0. */
    public static EncounterEnvironment resolve(
            BattleTerrain terrain, int mapBackground, int season) {
        int selected = mapBackground > 0 ? mapBackground : terrain.background();
        int[] background = BACKGROUNDS[0];
        for (int[] candidate : BACKGROUNDS)
            if (candidate[0] == selected) {
                background = candidate;
                break;
            }
        int platform = background[1];
        if (platform == 0)
            platform =
                    switch (terrain) {
                        case INSIDE -> 6;
                        case WATER, CALM_WATER -> 14;
                        case UNDERWATER -> 15;
                        default -> 0;
                    };
        int offset = background[4] == 0 ? 0 : Math.floorMod(season, 4);
        return new EncounterEnvironment(background[2] + offset, background[3] + offset, platform);
    }

    /** Original neutral preview scene for callers without a map context. */
    public static EncounterEnvironment preview() {
        return resolve(BattleTerrain.ROUTE, 1, 0);
    }
}
