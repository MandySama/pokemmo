package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 破壳（招式 ID：504）的攻击／事件阶段动画。
 *
 * <p>招式描述：打破外壳, 降低自己的防御和特防, 但大幅提高攻击、特攻和速度.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110504（名称）和 120504（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/ShellSmashAnimation.java
 *
 * <p>源文件 SHA-256：3d1ccaaf7996206805bcbdbd0541d06c9c50631332a0038e4cb0d4f0f1149829
 */
final class ReferenceMoveProgram504 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .sequence()
                                                                                                        .beginParallel()
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                1487,
                                                                                                                                1,
                                                                                                                                14,
                                                                                                                                333.33334f,
                                                                                                                                0.625f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .focusCamera(
                                                                                                                                0,
                                                                                                                                0.4f))
                                                                                                        .endGroup()
                                                                                                        .beginParallel()
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scaleCombatant(
                                                                                                                                14,
                                                                                                                                1,
                                                                                                                                0,
                                                                                                                                0.0f,
                                                                                                                                0.32f,
                                                                                                                                0.100097656f,
                                                                                                                                0.100097656f))
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                670)),
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                670,
                                                                                                                0,
                                                                                                                9,
                                                                                                                8,
                                                                                                                0.3499756f),
                                                                                                0.2f)
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                670,
                                                                                                                3,
                                                                                                                9,
                                                                                                                8,
                                                                                                                0.25f)),
                                                                                owner.emitParticles(
                                                                                        -1, 670, 1,
                                                                                        9, 8, 0.5f),
                                                                                0.4f)
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 670, 2,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 670, 2,
                                                                                        9, 8,
                                                                                        0.5f)),
                                                                owner.emitParticles(
                                                                        -1, 670, 2, 9, 8, 0.5f),
                                                                0.6f)
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.032f,
                                                                        0.100097656f,
                                                                        0.0f)),
                                                owner.scaleCombatant(
                                                        14,
                                                        1,
                                                        1,
                                                        0.0f,
                                                        0.128f,
                                                        1.1999512f,
                                                        1.1999512f),
                                                0.8f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1532,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1449,
                                                        2,
                                                        14,
                                                        83.333336f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.scaleCombatant(14, 1, 0, 0.0f, 0.032f, 1.0f, 1.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
