package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 诡计☆（自定义动画注册 ID：3417）的攻击／事件阶段动画。
 *
 * <p>招式描述：谋划诡计, 激活头脑. 大幅提高自己的特攻.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>变体映射：基础招式 ID 为 417；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110417（名称）和 120417（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/NastyPlotAnimation.java
 *
 * <p>源文件 SHA-256：59c1d964a859ae08ce842500f4624fd7997f14d0d907e1b56cd501ee086a49d8
 */
final class ReferenceMoveProgram3417 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1452,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1452,
                                                                        2,
                                                                        14,
                                                                        416.66666f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1452,
                                                                        1,
                                                                        14,
                                                                        666.6667f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1359,
                                                                        2,
                                                                        14,
                                                                        1666.6666f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .onStart(owner.preloadParticleEffect(592))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 592, 0, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 592, 7, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 592, 1, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 592, 2, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 592, 3, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 592, 4, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 592, 5, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 592, 6, 9, 8, 0.5f),
                                                1.7f)
                                        .append(
                                                owner.shakeCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.048f,
                                                        0.0f,
                                                        0.30004883f)),
                                owner.scaleCombatant(14, 2, 1, 0.016f, 0.048f, 0.0f, 0.30004883f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
