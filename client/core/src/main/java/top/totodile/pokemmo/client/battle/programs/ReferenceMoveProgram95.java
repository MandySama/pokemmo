package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 催眠术（招式 ID：95）的攻击／事件阶段动画。
 *
 * <p>招式描述：施以诱导睡意的暗示, 让对手陷入睡眠状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110095（名称）和 120095（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/HypnosisAnimation.java
 *
 * <p>源文件 SHA-256：1fb5636aa22f315e18f57b98d37c3aab04e621966e0e81d8b34d91bc6fa3370d
 */
final class ReferenceMoveProgram95 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .onStart(owner.preloadParticleEffect(264))
                                                        .append(
                                                                owner.launchParticles(
                                                                        264, 0, 9, 11, 0.5f, 0.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1455,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1450,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.15625f,
                                                        owner.casterHandle),
                                                0.6f)
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        6,
                                                        0.016f,
                                                        0.016f,
                                                        0.020019531f,
                                                        -0.020019531f))
                                        .append(owner.emitParticles(-1, 264, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 264, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 264, 4, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1461,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1462,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.859375f,
                                                        owner.casterHandle)),
                                owner.adjustSoundTrack(1, 0, 80.0f, 320.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
