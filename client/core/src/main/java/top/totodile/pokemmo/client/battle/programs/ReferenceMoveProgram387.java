package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 珍藏（招式 ID：387）的攻击／事件阶段动画。
 *
 * <p>招式描述：当战斗中已学会的招式全部使用过后, 才能开始使出珍藏的招式.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110387（名称）和 120387（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/LastResortAnimation.java
 *
 * <p>源文件 SHA-256：0defd2f1fe229daf7c0d863de4e90db699c5d8845d68a225969da78894c13247
 */
final class ReferenceMoveProgram387 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1471,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1280.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1424,
                                                        2,
                                                        16,
                                                        666.6667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1469,
                                                        1,
                                                        16,
                                                        666.6667f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(562))
                                        .append(owner.emitParticles(-1, 562, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 562, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 562, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(0.6f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 3, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
