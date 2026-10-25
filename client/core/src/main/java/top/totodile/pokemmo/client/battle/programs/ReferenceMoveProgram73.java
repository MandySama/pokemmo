package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 寄生种子（招式 ID：73）的攻击／事件阶段动画。
 *
 * <p>招式描述：植入寄生种子后, 将在每回合一点一点吸取对手的HP, 从而用来回复自己的HP.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110073（名称）和 120073（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/LeechSeedAnimation.java
 *
 * <p>源文件 SHA-256：f0f5a59ca8aea0c120f66bf79567855e436690470986aa512736ceb5ad5a5083
 */
final class ReferenceMoveProgram73 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedOuterParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .appendThenDelayedParallel(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .waitThenParallel(
                                                                                                                                0.6f)
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .scheduleSound(
                                                                                                                                                (byte)
                                                                                                                                                        2,
                                                                                                                                                (short)
                                                                                                                                                        1513,
                                                                                                                                                1,
                                                                                                                                                14,
                                                                                                                                                0.0f,
                                                                                                                                                0.9375f,
                                                                                                                                                owner.casterHandle))
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .adjustSoundTrack(
                                                                                                                                                1,
                                                                                                                                                0,
                                                                                                                                                0.0f,
                                                                                                                                                160.0f))
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .scheduleSound(
                                                                                                                                                (byte)
                                                                                                                                                        2,
                                                                                                                                                (short)
                                                                                                                                                        1513,
                                                                                                                                                2,
                                                                                                                                                14,
                                                                                                                                                233.33333f,
                                                                                                                                                0.9375f,
                                                                                                                                                owner.casterHandle))
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .scheduleSound(
                                                                                                                                                (byte)
                                                                                                                                                        2,
                                                                                                                                                (short)
                                                                                                                                                        1513,
                                                                                                                                                1,
                                                                                                                                                14,
                                                                                                                                                466.66666f,
                                                                                                                                                0.9375f,
                                                                                                                                                owner.casterHandle))
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .scheduleSound(
                                                                                                                                                (byte)
                                                                                                                                                        2,
                                                                                                                                                (short)
                                                                                                                                                        1513,
                                                                                                                                                2,
                                                                                                                                                14,
                                                                                                                                                700.0f,
                                                                                                                                                0.9375f,
                                                                                                                                                owner.casterHandle))
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .scheduleSound(
                                                                                                                                                (byte)
                                                                                                                                                        2,
                                                                                                                                                (short)
                                                                                                                                                        1513,
                                                                                                                                                2,
                                                                                                                                                16,
                                                                                                                                                1666.6666f,
                                                                                                                                                0.9375f,
                                                                                                                                                owner.casterHandle))
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .scheduleSound(
                                                                                                                                                (byte)
                                                                                                                                                        2,
                                                                                                                                                (short)
                                                                                                                                                        1513,
                                                                                                                                                1,
                                                                                                                                                16,
                                                                                                                                                1833.3334f,
                                                                                                                                                0.703125f,
                                                                                                                                                owner.casterHandle))
                                                                                                                        .append(
                                                                                                                                owner
                                                                                                                                        .scheduleSound(
                                                                                                                                                (byte)
                                                                                                                                                        2,
                                                                                                                                                (short)
                                                                                                                                                        1513,
                                                                                                                                                2,
                                                                                                                                                16,
                                                                                                                                                2000.0f,
                                                                                                                                                0.3125f,
                                                                                                                                                owner.casterHandle))
                                                                                                                        .onStart(
                                                                                                                                owner
                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                234))
                                                                                                                        .onStart(
                                                                                                                                owner
                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                235))
                                                                                                                        .onStart(
                                                                                                                                owner
                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                236)),
                                                                                                                owner
                                                                                                                        .emitParticles(
                                                                                                                                -1,
                                                                                                                                234,
                                                                                                                                2,
                                                                                                                                2,
                                                                                                                                9,
                                                                                                                                0.0f),
                                                                                                                0.2f),
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                234,
                                                                                                                2,
                                                                                                                2,
                                                                                                                9,
                                                                                                                0.0f),
                                                                                                0.4f),
                                                                                owner.emitParticles(
                                                                                        -1, 234, 2,
                                                                                        2, 9, 0.0f),
                                                                                0.6f),
                                                                owner.emitParticles(
                                                                        -1, 234, 2, 2, 9, 0.0f),
                                                                1.2f)
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(1.4f)
                                                        .beginParallel()
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 235, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 236, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 235, 0, 11, 8, 0.125f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 235, 2, 11, 8, 0.125f))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(1.5f)
                                                        .beginParallel()
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(1.5f)
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(234))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 234, 0, 11, 8, 0.125f)),
                                                owner.emitParticles(-1, 234, 1, 11, 8, 0.125f),
                                                1.6f)
                                        .append(owner.emitParticles(-1, 236, 0, 11, 8, 0.125f)),
                                owner.emitParticles(-1, 236, 2, 11, 8, 0.125f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
