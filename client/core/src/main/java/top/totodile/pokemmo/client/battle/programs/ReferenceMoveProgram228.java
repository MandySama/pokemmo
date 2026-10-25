package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 追打（招式 ID：228）的攻击／事件阶段动画。
 *
 * <p>招式描述：当对手替换宝可梦上场时使出此招式的话, 能够以2倍的威力进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110228（名称）和 120228（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/PursuitAnimation.java
 *
 * <p>源文件 SHA-256：27052cf1f171e709e3cbade9cc4b9affe5d587ed3ae29e88bc1388434ba407d9
 */
final class ReferenceMoveProgram228 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.tintEnvironment(
                                                                                4, 0, 0.0f, 0.75f,
                                                                                0.075f))
                                                                .append(owner.focusCamera(1, 0.4f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1478,
                                                                                2,
                                                                                16,
                                                                                0.0f,
                                                                                0.8984375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1472,
                                                                                1,
                                                                                16,
                                                                                0.0f,
                                                                                0.78125f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                1,
                                                                                16,
                                                                                666.6667f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                394)),
                                                        owner.emitParticles(
                                                                -1, 394, 1, 11, 8, 0.5f),
                                                        0.24f)
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 394, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 394, 0, 11, 8, 0.5f)),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                2,
                                                0.016f,
                                                0.032f,
                                                0.19995117f,
                                                -0.19995117f))
                                .append(owner.restoreCamera(0.4f))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.075f));
        return result;
    }
}
