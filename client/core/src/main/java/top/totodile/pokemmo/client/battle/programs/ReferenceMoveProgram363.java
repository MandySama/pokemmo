package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 自然之恩（招式 ID：363）的攻击／事件阶段动画。
 *
 * <p>招式描述：从树果上获得力量进行攻击. 根据携带的树果, 招式属性和威力会改变.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110363（名称）和 120363（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/NaturalGiftAnimation.java
 *
 * <p>源文件 SHA-256：488577301dc1c811f3dcd7c06c1cd25633f793a72d51fa6e8260792599312418
 */
final class ReferenceMoveProgram363 {

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
                                                        (short) 1515,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        2,
                                                        14,
                                                        750.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(537))
                                        .append(owner.emitParticles(-1, 537, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 537, 2, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 537, 1, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 537, 3, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
