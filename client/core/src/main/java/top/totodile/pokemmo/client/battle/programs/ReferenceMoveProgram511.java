package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 延后（招式 ID：511）的攻击／事件阶段动画。
 *
 * <p>招式描述：压制对手, 从而将其行动顺序放到最后.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110511（名称）和 120511（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/QuashAnimation.java
 *
 * <p>源文件 SHA-256：e54d1c897f66cf26b5cf5d422d141bd0804ea1c63a3701fd32b9bdf5dd142947
 */
final class ReferenceMoveProgram511 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1427,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1455,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 320.0f, 160.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        2,
                                                                        16,
                                                                        833.3333f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1441,
                                                                        1,
                                                                        16,
                                                                        1000.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(676))
                                                        .append(owner.focusCamera(1, 0.5f))
                                                        .beginSequence()
                                                        .waitSeconds(0.2f)
                                                        .beginParallel()
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 676, 0, 11, 8, 0.75f)),
                                                owner.emitParticles(-1, 676, 1, 11, 8, 0.75f),
                                                0.8f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.32f, 0.39990234f, -0.39990234f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
