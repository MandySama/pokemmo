package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蝶舞（招式 ID：483）的攻击／事件阶段动画。
 *
 * <p>招式描述：轻巧地跳起神秘而又美丽的舞蹈. 提高自己的特攻、特防和速度.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110483（名称）和 120483（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/QuiverDanceAnimation.java
 *
 * <p>源文件 SHA-256：2711004a23d53fc4bd104db6773b5ce826762f0bf617a30c6da9b3a4495c71d2
 */
final class ReferenceMoveProgram483 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1863,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                800.0f,
                                                                                                320.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        14,
                                                                                        1166.6666f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1820,
                                                                                        2,
                                                                                        14,
                                                                                        416.66666f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.3f))
                                                                        .endGroup()
                                                                        .beginParallel(),
                                                                owner.tintEnvironment(
                                                                        2, 0, 0.0f, 0.5f, 0.075f),
                                                                0.2f)
                                                        .onStart(owner.preloadParticleEffect(654))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 654, 1, 9, 8, 0.25f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 654, 2, 9, 8, 0.0f)),
                                                owner.emitParticles(-1, 654, 3, 9, 8, 0.0f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.tintEnvironment(2, 0, 0.5f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
