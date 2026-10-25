package top.totodile.pokemmo.server.game;

import java.util.List;

/** WildEncounterService / BattleGenerator: one player combatant and up to five wild debut slots. */
public record EncounterData(
        PokemonData.Pokemon player,
        List<PokemonData.Pokemon> wilds,
        List<PokemonData.Pokemon> party) {
    public EncounterData {
        party = List.copyOf(party);
        wilds = List.copyOf(wilds);
        if (wilds.size() != 1 && wilds.size() != 3 && wilds.size() != 5)
            throw new IllegalArgumentException("Encounter size must be 1, 3 or 5");
    }

    public EncounterData(
            PokemonData.Pokemon player, PokemonData.Pokemon wild, List<PokemonData.Pokemon> party) {
        this(player, List.of(wild), party);
    }

    public PokemonData.Pokemon wild() {
        return wilds.get(0);
    }

    public boolean horde() {
        return wilds.size() > 1;
    }

    // BattlePokemonCommandType.RUN / BattleRunResultType.NORMAL_SUCCESS.
    public static final int RUN = 3, NORMAL_SUCCESS = 0;

    public int command(int type) {
        if (type != RUN)
            throw new IllegalArgumentException("Only RUN is enabled in encounter simulation");
        return NORMAL_SUCCESS;
    }
}
