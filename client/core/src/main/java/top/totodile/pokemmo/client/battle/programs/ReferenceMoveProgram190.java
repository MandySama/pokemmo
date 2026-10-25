package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 章鱼桶炮（招式 ID：190）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手的脸等喷出墨汁进行攻击. 有时会降低对手的命中率.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110190（名称）和 120190（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/OctazookaAnimation.java
 *
 * <p>源文件 SHA-256：a5282a313a89c9e1cff6ace783f202b0e466cd0c574b5ee9913ce7c9aa60c70e
 */
final class ReferenceMoveProgram190 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1475,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1407,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 320.0f, 320.0f))
                                                        .onStart(owner.preloadParticleEffect(356))
                                                        .append(
                                                                owner.launchParticles(
                                                                        356, 0, 9, 11, 0.5f, 0.0f)),
                                                owner.emitParticles(-1, 356, 1, 9, 8, 0.5f),
                                                0.4f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 356, 2, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, -0.19995117f, 0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
