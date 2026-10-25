package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 麻痹粉（招式 ID：78）的攻击／事件阶段动画。
 *
 * <p>招式描述：撒出麻痹粉, 从而让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110078（名称）和 120078（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/StunSporeAnimation.java
 *
 * <p>源文件 SHA-256：60e908f11b6c66a91605d5536358a320d38b25f4e92b180f967b36c381f79231
 */
final class ReferenceMoveProgram78 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(242))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 242, 0, 11, 8, 0.75f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 242, 1, 11, 8, 0.75f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1718,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1458,
                                                        2,
                                                        16,
                                                        666.6667f,
                                                        0.9375f,
                                                        owner.casterHandle),
                                                0.6f)
                                        .append(
                                                owner.tintCombatant(
                                                        16,
                                                        3.25f,
                                                        0.0f,
                                                        0.5f,
                                                        EncounterMoveAnimation.color(990)))
                                        .append(
                                                owner.tintCombatant(
                                                        16,
                                                        3.25f,
                                                        0.5f,
                                                        0.0f,
                                                        EncounterMoveAnimation.color(990))),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.032f, 0.016f, 0.020019531f, -0.020019531f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
