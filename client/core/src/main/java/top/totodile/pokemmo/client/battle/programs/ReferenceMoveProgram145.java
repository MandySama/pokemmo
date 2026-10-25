package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 泡沫（招式 ID：145）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手用力吹起无数泡泡进行攻击. 有时会降低对手的速度.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110145（名称）和 120145（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/BubbleAnimation.java
 *
 * <p>源文件 SHA-256：37237adfbc0d7a3f8cf3db3127194c993746c1375ae82d8bab774516e467869a
 */
final class ReferenceMoveProgram145 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.3f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1480,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1480,
                                                                        2,
                                                                        16,
                                                                        833.3333f,
                                                                        0.46875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 960.0f, 480.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 1760.0f, 480.0f))
                                                        .onStart(owner.preloadParticleEffect(310)),
                                                owner.emitParticles(-1, 310, 0, 9, 11, 0.5f),
                                                0.4f)
                                        .append(owner.focusCamera(1, 0.8f))
                                        .endGroup()
                                        .endGroup()
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 310, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 8, 0.016f, 0.032f, 0.050048828f, -0.050048828f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
