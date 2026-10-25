package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 龙尾（招式 ID：525）的攻击／事件阶段动画。
 *
 * <p>招式描述：弹飞对手, 强制拉后备宝可梦上场. 如果对手为野生宝可梦, 战斗将直接结束.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110525（名称）和 120525（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/DragonTailAnimation.java
 *
 * <p>源文件 SHA-256：c3cd7ed5a48988b10672a17c887d918b1d7a0fc31dc3c62dffd99a0c3a57a03c
 */
final class ReferenceMoveProgram525 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .onStart(owner.setHealthHudHidden(18, true))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1438,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        250.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(690))
                        .append(owner.emitParticles(-1, 690, 0, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 690, 1, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 690, 2, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 690, 3, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 690, 4, 11, 8, 0.25f))
                        .append(owner.shakeCombatant(16, 2, 12, 0.0f, 0.032f, 1.0f, 0.0f))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.064f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
