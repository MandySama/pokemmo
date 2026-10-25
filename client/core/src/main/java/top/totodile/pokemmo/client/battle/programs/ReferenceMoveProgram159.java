package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 棱角化（招式 ID：159）的攻击／事件阶段动画。
 *
 * <p>招式描述：增加身体的角, 变得棱棱角角, 从而提高自己的攻击.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110159（名称）和 120159（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SharpenAnimation.java
 *
 * <p>源文件 SHA-256：ab9ea2322be22019243d8a727183752f4638636841e21e817b04eaf93e54d5d5
 */
final class ReferenceMoveProgram159 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        2,
                                                        14,
                                                        500.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1461,
                                                        1,
                                                        14,
                                                        1000.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1709,
                                                        2,
                                                        14,
                                                        1416.6666f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1709,
                                                        1,
                                                        14,
                                                        1583.3334f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(323))
                                        .append(owner.emitParticles(-1, 323, 0, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 323, 1, 9, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
