package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 三重攻击（招式 ID：161）的攻击／事件阶段动画。
 *
 * <p>招式描述：用3种光线进行攻击. 有时会让对手陷入麻痹、灼伤或冰冻的状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110161（名称）和 120161（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/TriAttackAnimation.java
 *
 * <p>源文件 SHA-256：55cb1d6eeb5b9e765fcc5fa38baca82dc67e5cf2e17bd09e0859dfd0811c93d0
 */
final class ReferenceMoveProgram161 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenNextParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .sequence()
                                                                                                        .beginParallel()
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .focusCamera(
                                                                                                                                0,
                                                                                                                                0.4f))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .setSpriteAnimationPaused(
                                                                                                                                14,
                                                                                                                                1)),
                                                                                                owner
                                                                                                        .tintEnvironment(
                                                                                                                4,
                                                                                                                0,
                                                                                                                0.0f,
                                                                                                                0.75f,
                                                                                                                0.075f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1496,
                                                                                                                2,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1496,
                                                                                                                1,
                                                                                                                14,
                                                                                                                166.66667f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                325))
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                326))
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                327))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                325,
                                                                                                                0,
                                                                                                                9,
                                                                                                                8,
                                                                                                                0.5f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                326,
                                                                                                                1,
                                                                                                                9,
                                                                                                                8,
                                                                                                                0.5f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                327,
                                                                                                                1,
                                                                                                                9,
                                                                                                                8,
                                                                                                                0.5f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                325,
                                                                                                                2,
                                                                                                                9,
                                                                                                                8,
                                                                                                                0.5f)),
                                                                                owner.emitParticles(
                                                                                        -1, 326, 2,
                                                                                        9, 11,
                                                                                        0.5f),
                                                                                1.2f)
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1497,
                                                                                        2,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.703125f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                1,
                                                                                                480.0f,
                                                                                                640.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1475,
                                                                                        1,
                                                                                        16,
                                                                                        166.66667f,
                                                                                        0.9765625f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1475,
                                                                                        2,
                                                                                        16,
                                                                                        250.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1475,
                                                                                        1,
                                                                                        16,
                                                                                        333.33334f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1475,
                                                                                        2,
                                                                                        16,
                                                                                        416.66666f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 325, 1,
                                                                                        11, 8,
                                                                                        0.5f)),
                                                                owner.shakeCombatant(
                                                                        16,
                                                                        2,
                                                                        3,
                                                                        0.032f,
                                                                        0.016f,
                                                                        0.30004883f,
                                                                        0.0f),
                                                                2.0f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1522,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 327, 0, 11, 8, 0.5f)),
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        3,
                                                        0.032f,
                                                        0.016f,
                                                        0.30004883f,
                                                        0.0f),
                                                2.8f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1488,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 326, 0, 11, 8, 0.5f)),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
