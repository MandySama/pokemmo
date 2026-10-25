package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 踢倒（招式 ID：67）的攻击／事件阶段动画。
 *
 * <p>招式描述：用力踢对手的脚, 使其摔倒进行攻击. 对手越重, 威力越大.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110067（名称）和 120067（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/LowKickAnimation.java
 *
 * <p>源文件 SHA-256：e526c019b58b3a394501cc73dd1e86fabd67da59752a8094661a6099eaff23dc
 */
final class ReferenceMoveProgram67 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(1, 0.4f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1421,
                                                                                1,
                                                                                16,
                                                                                0.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                1,
                                                                                16,
                                                                                250.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1424,
                                                                                2,
                                                                                16,
                                                                                250.0f,
                                                                                0.625f,
                                                                                owner.casterHandle))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                228))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 228, 1, 11, 8,
                                                                                0.5f)),
                                                        owner.emitParticles(
                                                                -1, 228, 2, 11, 8, 0.5f),
                                                        0.2f),
                                                owner.emitParticles(-1, 228, 0, 11, 8, 0.5f),
                                                0.36f)
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.shakeCombatant(
                                                        16, 2, 1, 0.016f, 0.032f, 1.0f, 0.0f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
