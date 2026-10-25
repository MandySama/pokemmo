package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 迷昏拳（招式 ID：146）的攻击／事件阶段动画。
 *
 * <p>招式描述：有节奏地出拳攻击对手. 有时会使对手混乱.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110146（名称）和 120146（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DizzyPunchAnimation.java
 *
 * <p>源文件 SHA-256：4bc2cc90e623605754dcc33d509439b6eaeb710162fb62032e3cfe4f9e2b6b05
 */
final class ReferenceMoveProgram146 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.032f,
                                                                        0.30004883f,
                                                                        -0.30004883f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1530,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        2,
                                                                        16,
                                                                        833.3333f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 160.0f, 640.0f))
                                                        .onStart(owner.preloadParticleEffect(311))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 311, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 311, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 311, 2, 11, 8, 0.5f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1)),
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1530,
                                                        0,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        0,
                                                        16,
                                                        833.3333f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(0, 1, 160.0f, 640.0f))
                                        .append(owner.emitParticles(-1, 311, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 311, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 311, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
