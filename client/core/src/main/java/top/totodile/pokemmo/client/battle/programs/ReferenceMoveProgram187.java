package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 腹鼓（招式 ID：187）的攻击／事件阶段动画。
 *
 * <p>招式描述：将自己的HP减少最大HP的一半, 从而最大限度提高自己的攻击.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110187（名称）和 120187（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/BellyDrumAnimation.java
 *
 * <p>源文件 SHA-256：416a26af4cba0c7ef89ff2e5b6aafa825fe43b8bf986276eae8a4b84c15b0353
 */
final class ReferenceMoveProgram187 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1525,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1525,
                                                        2,
                                                        14,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1525,
                                                        1,
                                                        14,
                                                        333.33334f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1525,
                                                        2,
                                                        14,
                                                        500.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(353))
                                        .append(owner.emitParticles(-1, 353, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 353, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 353, 2, 9, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(14, 1)),
                                owner.scaleCombatant(
                                        14, 2, 3, 0.016f, 0.048f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
