package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 挥指（招式 ID：118）的攻击／事件阶段动画。
 *
 * <p>招式描述：挥动手指刺激自己的大脑, 从所有的招式中任意使出1个.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110118（名称）和 120118（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/MetronomeAnimation.java
 *
 * <p>源文件 SHA-256：b1e5a22985c6a28c2755271d7b34043b6a0afa780fbc1ef3b8cad618071fc774
 */
final class ReferenceMoveProgram118 {

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
                                                        (short) 1452,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        1,
                                                        14,
                                                        833.3333f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        1,
                                                        14,
                                                        1166.6666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        1,
                                                        14,
                                                        1500.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        2,
                                                        14,
                                                        1666.6666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(284))
                                        .append(owner.emitParticles(-1, 284, 12, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 13, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 2, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 8, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 4, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 10, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 6, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 12, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 13, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 3, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 9, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 5, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 284, 11, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 284, 7, 9, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
