package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 求雨（招式 ID：240）的攻击／事件阶段动画。
 *
 * <p>招式描述：在5回合内一直降雨, 从而提高水属性的招式威力. 火属性的招式威力则降低.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110240（名称）和 120240（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/RainDanceAnimation.java
 *
 * <p>源文件 SHA-256：76474873155f83bc9ef878ab9a3416aa367cd0390492251201f285e9310c3db4
 */
final class ReferenceMoveProgram240 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.6f))
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.1f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1680,
                                                        2,
                                                        14,
                                                        166.66667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        14,
                                                        2000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 1280.0f, 640.0f))
                                        .onStart(owner.preloadParticleEffect(407)),
                                owner.emitParticles(-1, 407, 0, 0, 0, 0.5f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.075f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
