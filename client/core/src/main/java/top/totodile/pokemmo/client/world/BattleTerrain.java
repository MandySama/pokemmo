package top.totodile.pokemmo.client.world;

/** Reference BattleFacilityType / rh0_1: visual terrain of the encounter location. */
public enum BattleTerrain {
    GRASS(4),
    DARK_GRASS(4),
    SAND(11),
    UNDERWATER(13),
    WATER(9),
    CALM_WATER(9),
    DIRT(8),
    CAVE(8),
    ROCK(5),
    ROUTE(4),
    INSIDE(10);

    private final int background;

    BattleTerrain(int background) {
        this.background = background;
    }

    public int background() {
        return background;
    }
}
