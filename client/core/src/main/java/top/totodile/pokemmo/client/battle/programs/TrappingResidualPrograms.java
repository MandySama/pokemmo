package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/** BattleAction024Packet mode 1: recurring trapping damage, separate from move-use animations. */
public final class TrappingResidualPrograms {
    private TrappingResidualPrograms() {}

    public static ReferenceMoveTimeline build(EncounterMoveAnimation owner, int move) {
        return switch (move) {
            case 20, 35 -> BindingResidualProgram.build(owner);
            case 83 -> FireSpinResidualProgram.build(owner);
            case 128 -> ClampResidualProgram.build(owner);
            case 250 -> WhirlpoolResidualProgram.build(owner);
            case 328 -> SandTombResidualProgram.build(owner);
            case 463, 3463 -> MagmaStormResidualProgram.build(owner);
            default -> BindingResidualProgram.build(owner);
        };
    }

    public static java.util.Set<Integer> effects(int move) {
        return switch (move) {
            case 20, 35 -> java.util.Set.of(197);
            case 83 -> java.util.Set.of(247);
            case 128 -> java.util.Set.of(293);
            case 250 -> java.util.Set.of(416);
            case 328 -> java.util.Set.of(498);
            case 463, 3463 -> java.util.Set.of(639);
            default -> java.util.Set.of(197);
        };
    }
}
