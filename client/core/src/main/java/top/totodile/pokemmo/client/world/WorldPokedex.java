package top.totodile.pokemmo.client.world;

import top.totodile.pokemmo.server.game.GameService.PokedexState;

import java.util.function.Consumer;

/** 本地图鉴调试按钮共用的状态及持久化。 */
public final class WorldPokedex {
    private final Consumer<PokedexState> savePokedex;
    private PokedexState pokedexState;

    public WorldPokedex(PokedexState state, Consumer<PokedexState> savePokedex) {
        this.pokedexState = state;
        this.savePokedex = savePokedex;
    }

    /** Apply authoritative capture state without saving a stale client snapshot back. */
    public void restore(PokedexState state) {
        pokedexState = state;
    }

    public void setUnlocked(boolean unlocked) {
        if (unlocked != pokedexState.unlocked()) {
            var state = new PokedexState(unlocked);
            savePokedex.accept(state);
            pokedexState = state;
        }
    }

    public boolean pokedexUnlocked() {
        return pokedexState.unlocked();
    }

    public PokedexState pokedexState() {
        return pokedexState;
    }

    /** User-requested local fixture; keep WorldService's fourth (caught alpha) bitset intact. */
    public void randomizePokedex(java.util.random.RandomGenerator random) {
        var entries = new java.util.HashMap<Integer, Integer>();
        for (int species = 1; species <= PokedexState.LAST_SPECIES; species++) {
            int level = random.nextInt(4);
            int flags = ((1 << level) - 1) | (pokedexState.entries().getOrDefault(species, 0) & 8);
            if (flags != 0) entries.put(species, flags);
        }
        var state = new PokedexState(entries);
        savePokedex.accept(state);
        pokedexState = state;
    }
}
