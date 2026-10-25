package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 极落钳（招式 ID：12）的攻击／事件阶段动画。
 *
 * <p>招式描述：用大钳子或剪刀等夹断对手进行攻击. 只要命中就会一击濒死.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110012（名称）和 120012（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/GuillotineAnimation.java
 *
 * <p>源文件 SHA-256：de4de22a1d8a1fd4797d25718970adb7ca549887d7ebb7d4a059f7ba202e3c7b
 */
final class ReferenceMoveProgram12 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.4f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1421,
                                                                1,
                                                                16,
                                                                750.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1701,
                                                                2,
                                                                16,
                                                                916.6667f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1423,
                                                                1,
                                                                16,
                                                                1000.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .onStart(owner.preloadParticleEffect(172))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 172, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 172, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 172, 3, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 172, 4, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 172, 0, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 172, 5, 11, 8, 0.5f),
                                        0.9f),
                                owner.shakeCombatant(16, 2, 3, 0.0f, 0.032f, 0.5f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
