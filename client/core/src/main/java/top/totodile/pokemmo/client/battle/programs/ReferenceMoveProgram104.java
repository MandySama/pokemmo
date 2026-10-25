package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 影子分身（招式 ID：104）的攻击／事件阶段动画。
 *
 * <p>招式描述：通过快速移动来制造分身, 扰乱对手, 从而提高闪避率.
 *
 * <p>动画说明：执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110104（名称）和 120104（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DoubleTeamAnimation.java
 *
 * <p>源文件 SHA-256：cc5558ce66114bde7bf1ff91eb31f6f7ea705cfaa5b188711bd54bc6fd93d735
 */
final class ReferenceMoveProgram104 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1407,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1376,
                                                                                1,
                                                                                14,
                                                                                166.66667f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1407,
                                                                                1,
                                                                                14,
                                                                                416.66666f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1376,
                                                                                1,
                                                                                14,
                                                                                583.3333f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1407,
                                                                                1,
                                                                                14,
                                                                                833.3333f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1376,
                                                                                1,
                                                                                14,
                                                                                1000.0f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1407,
                                                                                1,
                                                                                14,
                                                                                1166.6666f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1376,
                                                                                1,
                                                                                14,
                                                                                1333.3334f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1407,
                                                                                1,
                                                                                14,
                                                                                1500.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1376,
                                                                                1,
                                                                                14,
                                                                                1666.6666f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1407,
                                                                                1,
                                                                                14,
                                                                                1833.3334f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1376,
                                                                                1,
                                                                                14,
                                                                                2000.0f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1407,
                                                                                1,
                                                                                14,
                                                                                2083.3333f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1376,
                                                                                1,
                                                                                14,
                                                                                3333.3333f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                1, 0, 0.0f,
                                                                                2880.0f))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                1, 1, 2880.0f,
                                                                                320.0f))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1)),
                                                        owner.shakeCombatant(
                                                                14, 3, 2, 0.016f, 0.032f, 0.5f,
                                                                0.0f)),
                                                owner.shakeCombatant(
                                                        14, 3, 2, 0.016f, 0.032f, 1.0f, 0.0f)),
                                        owner.shakeCombatant(14, 3, 4, 0.016f, 0.032f, 2.0f, 0.0f)),
                                owner.shakeCombatant(14, 3, 16, 0.0f, 0.016f, 3.0f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
