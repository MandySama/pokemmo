package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 生蛋（招式 ID：135）的攻击／事件阶段动画。
 *
 * <p>招式描述：恢复自身最大HP量的一半.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110135（名称）和 120135（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SoftBoiledAnimation.java
 *
 * <p>源文件 SHA-256：a69d6405b88ee87e2a7b3c0adea8b02a2aafff986706de2f403b82b54002b211
 */
final class ReferenceMoveProgram135 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.096f,
                                                                        0.30004883f,
                                                                        -0.30004883f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1502,
                                                                        1,
                                                                        14,
                                                                        250.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1358,
                                                                        2,
                                                                        14,
                                                                        916.6667f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(300))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 300, 2, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 300, 3, 9, 8, 0.5f),
                                                0.04f)
                                        .append(owner.emitParticles(-1, 300, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 300, 0, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 300, 4, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
