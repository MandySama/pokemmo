package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 天使之吻（招式 ID：186）的攻击／事件阶段动画。
 *
 * <p>招式描述：像天使般可爱地亲吻对手, 从而使对手混乱.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110186（名称）和 120186（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SweetKissAnimation.java
 *
 * <p>源文件 SHA-256：9a43b057b74fc0b1d6676cdff708aaf0f4278d32bb775ae497c372248ed40d9d
 */
final class ReferenceMoveProgram186 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1723,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9765625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1723,
                                                        1,
                                                        16,
                                                        333.33334f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1358,
                                                        2,
                                                        16,
                                                        1666.6666f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.preloadParticleEffect(352))
                                        .append(owner.emitParticles(-1, 352, 0, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 352, 1, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
