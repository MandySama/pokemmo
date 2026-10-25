package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 摔打（招式 ID：21）的攻击／事件阶段动画。
 *
 * <p>招式描述：使用长长的尾巴或藤蔓等摔打对手进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110021（名称）和 120021（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SlamAnimation.java
 *
 * <p>源文件 SHA-256：5dc744b68a393099da1906839eb42f4a64afc2a02edeb11efdb23d6cb5c9e686
 */
final class ReferenceMoveProgram21 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.shakeCombatant(
                                                        14, 2, 1, 0.016f, 0.064f, 1.0f, 0.0f))
                                        .onStart(owner.preloadParticleEffect(183))
                                        .append(owner.emitParticles(-1, 183, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 183, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.032f,
                                                        0.064f,
                                                        0.30004883f,
                                                        -0.19995117f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1421,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        2,
                                                        16,
                                                        83.333336f,
                                                        0.859375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1424,
                                        1,
                                        16,
                                        83.333336f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
