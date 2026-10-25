package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 替身（招式 ID：164）的攻击／事件阶段动画。
 *
 * <p>招式描述：削减少许自己的HP, 制造分身. 分身将成为自己的替身.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110164（名称）和 120164（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SubstituteAnimation.java
 *
 * <p>源文件 SHA-256：afebfcd988f967efeb6b867516f99cee59f027b1078af9a48b391965a730adbf
 */
final class ReferenceMoveProgram164 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1437,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1452,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1452,
                                                        1,
                                                        14,
                                                        416.66666f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.preloadParticleEffect(330)),
                                owner.emitParticles(-1, 330, 0, 9, 8, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
