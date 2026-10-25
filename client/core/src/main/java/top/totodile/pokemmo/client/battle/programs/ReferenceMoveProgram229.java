package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 高速旋转（招式 ID：229）的攻击／事件阶段动画。
 *
 * <p>招式描述：移除己方场地上的撒菱、隐形岩、毒菱, 摆脱自身束缚、寄生种子状态等等, 诸如此类. 使用后速度提升.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110229（名称）和 120229（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/RapidSpinAnimation.java
 *
 * <p>源文件 SHA-256：a303cbcd45934d1642e95adf659bf8b2f3b92669f07552c4e2805b7ef218a579
 */
final class ReferenceMoveProgram229 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.6f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1816,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(395))
                                        .append(owner.emitParticles(-1, 395, 1, 9, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 395, 2, 9, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.shakeCombatant(
                                                        16, 2, 4, 0.0f, 0.032f, 1.0f, 0.0f)),
                                owner.emitParticles(-1, 395, 0, 11, 8, 0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
