package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 直冲钻（招式 ID：529）的攻击／事件阶段动画。
 *
 * <p>招式描述：像钢钻一样, 一边旋转身体一边撞击对手. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110529（名称）和 120529（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/DrillRunAnimation.java
 *
 * <p>源文件 SHA-256：8592c022a3f0531ed64587abd327a08105c172586a6b1b368f9ca6927cfb01b4
 */
final class ReferenceMoveProgram529 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1489,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1376,
                                                                                1,
                                                                                16,
                                                                                500.0f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(owner.focusCamera(1, 0.65f))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                693))
                                                                .append(
                                                                        owner.launchParticles(
                                                                                693, 2, 9, 11, 0.5f,
                                                                                240.0f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 693, 4, 11, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        16, 1)),
                                                        owner.shakeCombatant(
                                                                16,
                                                                2,
                                                                8,
                                                                0.032f,
                                                                0.016f,
                                                                0.30004883f,
                                                                0.0f),
                                                        0.3f)
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1652,
                                                                2,
                                                                16,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1402,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 693, 5, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 693, 0, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 693, 1, 11, 8, 0.5f),
                                        0.58f),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.064f, 0.39990234f, -0.39990234f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
