package top.totodile.pokemmo.protocol;

import java.util.List;
import java.util.Set;

/** 两端共用可设置形态，形态编号在个体存档中保持稳定。 */
public final class PokemonFormsCatalog {
    private static final Set<Integer> GIGANTAMAX = Set.of(6, 12, 25, 52, 68, 99, 131, 133, 143, 569);

    private PokemonFormsCatalog() { }

    public static boolean supports(int species, int form) {
        return form == 0 || (form == 3 && GIGANTAMAX.contains(species))
                || (species == 6 && (form == 1 || form == 2));
    }

    public static List<Integer> choices(int species) {
        if (species == 6) return List.of(0, 1, 2, 3);
        return GIGANTAMAX.contains(species) ? List.of(0, 3) : List.of(0);
    }
}
