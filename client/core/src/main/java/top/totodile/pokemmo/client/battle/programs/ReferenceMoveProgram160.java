package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 纹理（招式 ID：160）的攻击／事件阶段动画。
 *
 * <p>招式描述：改变自身属性为自己学会的第一个招式的属性.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110160（名称）和 120160（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ConversionAnimation.java
 *
 * <p>源文件 SHA-256：6c36512c1943f001d3e2d17d2fcbd2072d6cbfe0ac625e60c90237613b8ff966
 */
final class ReferenceMoveProgram160 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(324))
                                        .append(owner.emitParticles(-1, 324, 0, 9, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        -0.19995117f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1358,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1471,
                                        2,
                                        14,
                                        750.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
