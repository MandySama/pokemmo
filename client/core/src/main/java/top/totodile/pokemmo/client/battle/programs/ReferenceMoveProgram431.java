package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 攀岩（招式 ID：431）的攻击／事件阶段动画。
 *
 * <p>招式描述：用尽全力扑向对手进行攻击. 有时会使对手混乱.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110431（名称）和 120431（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/RockClimbAnimation.java
 *
 * <p>源文件 SHA-256：f60c082a3ce3296a297f8333f37626b274c7a7cf64e24d8425d3b1546e0934a0
 */
final class ReferenceMoveProgram431 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1416, 1, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 1, 14, 666.6667f, 0.0f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 160.0f, 480.0f))
                        .onStart(owner.preloadParticleEffect(607))
                        .append(owner.emitParticles(-1, 607, 3, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 607, 4, 9, 8, 0.4f))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1416, 1, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 1, 14, 666.6667f, 0.0f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 160.0f, 480.0f))
                        .append(owner.emitParticles(-1, 607, 3, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 607, 4, 9, 8, 0.4f))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1416, 1, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 1, 14, 666.6667f, 0.0f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 160.0f, 480.0f))
                        .append(owner.emitParticles(-1, 607, 3, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 607, 4, 9, 8, 0.4f))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .append(owner.shakeCombatant(14, 2, 1, 0.032f, 0.032f, 1.0f, 0.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1437, 1, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.shakeCombatant(16, 2, 4, 0.016f, 0.016f, 0.5f, 0.0f))
                        .append(owner.emitParticles(-1, 607, 2, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 607, 1, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 607, 2, 11, 8, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1420, 1, 16, 0.0f, 0.859375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1475, 2, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
