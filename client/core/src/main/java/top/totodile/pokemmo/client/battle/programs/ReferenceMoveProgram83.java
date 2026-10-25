package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 火焰旋涡（招式 ID：83）的攻击／事件阶段动画。
 *
 * <p>招式描述：将对手困在激烈的火焰旋涡中, 在4-5回合内进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110083（名称）和 120083（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FireSpinAnimation.java
 *
 * <p>源文件 SHA-256：8f3495e15e84d9223f12e25727b9bb8be7037464aeb1c3b26f23024e66ce2ad0
 */
final class ReferenceMoveProgram83 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .onStart(
                                                                        owner.setHealthHudHidden(
                                                                                18, true))
                                                                .append(owner.focusCamera(1, 0.4f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                247)),
                                                        owner.emitParticles(
                                                                -1, 247, 0, 11, 8, 0.25f),
                                                        0.12f)
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .append(
                                                        owner.scaleCombatant(
                                                                16,
                                                                2,
                                                                1,
                                                                0.016f,
                                                                0.032f,
                                                                0.30004883f,
                                                                -0.30004883f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1426,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1425,
                                                                2,
                                                                16,
                                                                0.0f,
                                                                0.703125f,
                                                                owner.casterHandle)),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1376,
                                                2,
                                                16,
                                                1000.0f,
                                                0.0f,
                                                owner.casterHandle),
                                        0.18f),
                                owner.emitParticles(-1, 247, 1, 11, 8, 0.25f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
