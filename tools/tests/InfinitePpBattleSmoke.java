package top.totodile.pokemmo.game;

import top.totodile.pokemmo.protocol.*;
import java.util.List;

public final class InfinitePpBattleSmoke {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static PartyMember member(int species, int move, int pp, int maxPp) {
        var d = PokemonIndividual.create(species, 100, false, 1, "Test", 0, 0, 0, 0);
        d = d.battleState(List.of(new PokemonIndividual.Move(move, pp, maxPp, 0)), "");
        int hp = d.stat(species, 100, 0);
        return new PartyMember(species, false, false, hp, hp, 100, d);
    }
    public static void main(String[] args) throws Exception {
        var player = member(25, 33, 0, 35);
        try (var battle = new ShowdownBattle(List.of(player), List.of(member(143, 45, 40, 40)), false)) {
            battle.setInfinitePp(true);
            var initial = battle.snapshot();
            check(initial.options().getFirst().id() == 33 && initial.options().getFirst().pp() == 35, "zero PP becomes usable with full displayed PP");
            var result = battle.handle(new BattlePackets.Request(BattlePackets.MOVE, initial.battle(), initial.revision(), 1));
            check(result.accepted() && result.options().getFirst().pp() == 35, "real move does not consume PP");
            check(result.party().getFirst().individual().moves().getFirst().pp() == 0, "persistent PP remains unchanged");
            var resumed = BattlePackets.decode(BattlePackets.response(battle.snapshot()));
            check(resumed.options().getFirst().pp() == 35 && resumed.events().isEmpty(), "recovery snapshot has unlimited PP");
            battle.setInfinitePp(false);
            check(battle.snapshot().options().getFirst().id() == 165, "disable restores zero PP and Struggle");
        }
        check(InfinitePpPackets.decode(InfinitePpPackets.response(new InfinitePpPackets.State(true, true))).enabled(), "setting codec");
        System.out.println("PASS Java/real Node: zero PP, actual move, displayed/persistent PP isolation, snapshot recovery, disable");
    }
}
