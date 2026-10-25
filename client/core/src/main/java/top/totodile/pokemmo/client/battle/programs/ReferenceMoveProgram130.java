package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 火箭头锤（招式 ID：130）的攻击／事件阶段动画。
 *
 * <p>招式描述：第1回合把头缩进去, 从而提高防御. 第2回合攻击对手.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110130（名称）和 120130（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SkullBashAnimation.java
 *
 * <p>源文件 SHA-256：309c5bec7ac2cbb40b9699b40a52ad41fc0aa6181728a25f6eddd29c205d13c9
 */
final class ReferenceMoveProgram130 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.075f))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .endGroup()
                                        .beginParallel()
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.setSpriteAnimationPaused(14, 0))
                                        .endGroup()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(295))
                                        .append(owner.emitParticles(-1, 295, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 295, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 295, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(0.2f)
                                        .beginParallel(),
                                owner.shakeCombatant(16, 2, 9, 0.0f, 0.032f, 0.5f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
