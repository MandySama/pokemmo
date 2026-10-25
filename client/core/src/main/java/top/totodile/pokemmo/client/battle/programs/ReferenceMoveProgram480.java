package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 山岚摔（招式 ID：480）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手使出强烈的一击. 攻击必定会击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110480（名称）和 120480（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/StormThrowAnimation.java
 *
 * <p>源文件 SHA-256：c6d3b80b3a8a7e9fe5f5f1487a3f32a32fa66a33626183da1b1b9349bfece42e
 */
final class ReferenceMoveProgram480 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1804,
                                                                                        1,
                                                                                        16,
                                                                                        166.66667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1804,
                                                                                        1,
                                                                                        16,
                                                                                        500.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1420,
                                                                                        2,
                                                                                        16,
                                                                                        1216.6666f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1420,
                                                                                        2,
                                                                                        16,
                                                                                        1283.3334f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .scaleCombatant(
                                                                                                14,
                                                                                                2,
                                                                                                3,
                                                                                                0.016f,
                                                                                                0.064f,
                                                                                                0.30004883f,
                                                                                                -0.30004883f))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                651))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                14,
                                                                                                1))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 651, 2,
                                                                                        11, 8,
                                                                                        0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 651, 3,
                                                                                        11, 8,
                                                                                        0.5f)),
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        3,
                                                                        0.016f,
                                                                        0.064f,
                                                                        -0.39990234f,
                                                                        0.5f),
                                                                0.9f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 651, 0, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 651, 1, 11, 8, 0.5f),
                                                0.96f)
                                        .append(owner.emitParticles(-1, 651, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 651, 1, 11, 8, 0.25f)),
                                owner.scaleCombatant(16, 2, 1, 0.016f, 0.064f, 0.39990234f, -0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
