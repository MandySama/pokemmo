package top.totodile.pokemmo.game;

import top.totodile.pokemmo.protocol.*;
import java.util.List;

/** 真实 Node 引擎、Java 快照与网络编码的测试，不读取数据库。 */
public final class BattleReplacementSmoke {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static PartyMember member(int species, int move, int pp) {
        var d = PokemonIndividual.create(species, 100, false, 1, "Test", 0, 0, 0, 0);
        d = d.battleState(List.of(new PokemonIndividual.Move(move, pp, pp, 0)), "");
        int hp = d.stat(species, 100, 0);
        return new PartyMember(species, false, false, hp, hp, 100, d);
    }
    public static void main(String[] args) throws Exception {
        var request = new BattlePackets.Request(BattlePackets.MOVE, 123, 5, 1, 86);
        check(BattlePackets.readRequest(BattlePackets.request(request)).equals(request), "replacement packet round trip");
        var old = new BattlePackets.Request(BattlePackets.MOVE, 123, 5, 1);
        check(BattlePackets.readRequest(BattlePackets.request(old)).equals(old), "ordinary packet remains readable");
        try { BattlePackets.readRequest(BattlePackets.request(new BattlePackets.Request(BattlePackets.RUN, 123, 5, 0, 86))); throw new AssertionError("non-move replacement accepted"); }
        catch (java.io.IOException expected) { }
        var player = member(25, 33, 35);
        try (var battle = new ShowdownBattle(List.of(player), List.of(member(143, 45, 40)), false)) {
            var initial = battle.snapshot();
            var changed = battle.handle(new BattlePackets.Request(BattlePackets.MOVE, initial.battle(), initial.revision(), 1, 86));
            check(changed.accepted(), "replacement accepted");
            check(changed.events().stream().anyMatch(e -> e.kind() == 0 && e.side() == 0 && e.value() == 86), "authoritative replacement move event");
            check(changed.events().stream().anyMatch(e -> e.kind() == 2 && e.side() == 1 && e.value() == 2), "real paralysis");
            var option = changed.options().getFirst();
            check(option.id() == 86 && option.pp() == option.maxPp() - 1, "real replacement option and PP");
            check(changed.party().getFirst().individual().moves().getFirst().equals(player.individual().moves().getFirst()), "learned move and PP not overwritten");
            var stale = battle.handle(new BattlePackets.Request(BattlePackets.MOVE, initial.battle(), initial.revision(), 1, 52));
            check(!stale.accepted() && stale.revision() == changed.revision(), "stale replacement cannot advance");
            var recovered = BattlePackets.decode(BattlePackets.response(battle.snapshot()));
            check(recovered.options().equals(changed.options()) && recovered.events().isEmpty(), "recovery snapshot keeps replacement and PP without replay");
            var restored = battle.handle(new BattlePackets.Request(BattlePackets.MOVE, initial.battle(), recovered.revision(), 1, BattlePackets.RESTORE_MOVE));
            check(restored.accepted() && restored.options().getFirst().id() == 33, "restore learned move");
            check(restored.party().getFirst().individual().moves().getFirst().pp() == 34, "restored move spends learned PP");
        }
        System.out.println("PASS protocol, real Node replacement effect/PP, learned move isolation, stale request, recovery snapshot and restore");
    }
}
