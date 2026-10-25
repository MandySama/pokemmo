package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 伏特替换（招式 ID：521）的攻击／事件阶段动画。
 *
 * <p>招式描述：在攻击之后急速返回, 和后备宝可梦进行替换.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110521（名称）和 120521（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/VoltSwitchAnimation.java
 *
 * <p>源文件 SHA-256：459bf629efd524b8cf58387b95e7c2b6adb9506b01fc29954a1914a03b405cd0
 */
final class ReferenceMoveProgram521 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f)),
                                                owner.tintEnvironment(2, 0, 0.0f, 0.75f, 0.125f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1589,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1522,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1447,
                                                        2,
                                                        14,
                                                        1166.6666f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(687))
                                        .append(owner.emitParticles(-1, 687, 2, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 687, 5, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 687, 3, 9, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(1.2f)
                                        .beginParallel()
                                        .append(owner.launchParticles(687, 4, 9, 11, 0.5f, 360.0f))
                                        .append(owner.launchParticles(687, 5, 9, 11, 0.5f, 360.0f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.6f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 687, 6, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 687, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 687, 1, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1522,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle),
                                2.0f)
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 8, 0.0f, 0.048f, 0.100097656f, -0.100097656f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.4f)
                        .beginParallel()
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.6f)
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 0, 0.75f, 0.0f, 0.125f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
