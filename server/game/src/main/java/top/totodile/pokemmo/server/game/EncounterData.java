package top.totodile.pokemmo.server.game;

import java.util.List;

/** WildEncounterService / BattleGenerator: two sides, one active Pokemon per side. */
public record EncounterData(
        PokemonData.Pokemon player, PokemonData.Pokemon wild, List<PokemonData.Pokemon> party) {
    public EncounterData {
        party = List.copyOf(party);
    }

    // BattlePokemonCommandType.RUN / BattleRunResultType.NORMAL_SUCCESS.
    public static final int RUN = 3, NORMAL_SUCCESS = 0;

    public int command(int type) {
        if (type != RUN)
            throw new IllegalArgumentException("Only RUN is enabled in encounter simulation");
        return NORMAL_SUCCESS;
    }
}
