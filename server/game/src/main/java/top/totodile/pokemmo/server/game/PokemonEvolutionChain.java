package top.totodile.pokemmo.server.game;

import java.util.*;

/**
 * Connected species from the loaded reference evolution archive, including reverse links and
 * branches.
 */
public final class PokemonEvolutionChain {
    private PokemonEvolutionChain() {}

    public static List<Integer> family(int species, Map<Integer, List<Integer>> evolutions) {
        var family = new TreeSet<Integer>();
        family.add(species);
        boolean changed;
        do {
            changed = false;
            for (var entry : evolutions.entrySet())
                for (int target : entry.getValue()) {
                    if (!evolutions.containsKey(target)) continue;
                    if (family.contains(entry.getKey()) || family.contains(target)) {
                        changed |= family.add(entry.getKey());
                        changed |= family.add(target);
                    }
                }
        } while (changed);
        return List.copyOf(family);
    }
}
