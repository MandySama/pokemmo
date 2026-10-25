package top.totodile.pokemmo.server.game.battle;

import top.totodile.pokemmo.server.game.EncounterData;

/** Reserved for the reference BattleManager / BattleSwitchService implementation. */
public final class CustomBattleEngine implements BattleEngine {
    @Override
    public Session start(EncounterData encounter) {
        throw new UnsupportedOperationException(
                "The custom battle rules engine is reserved for future implementation");
    }
}
