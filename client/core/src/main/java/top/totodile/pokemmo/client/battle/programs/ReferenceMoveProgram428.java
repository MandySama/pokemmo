package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 意念头锤（招式 ID：428）的攻击／事件阶段动画。
 *
 * <p>招式描述：将思念的力量集中在前额进行攻击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110428（名称）和 120428（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/ZenHeadbuttAnimation.java
 *
 * <p>源文件 SHA-256：3d39203cf796e5c7621aec558575f8ab1b9ceb047895d90adc52868b984d3573
 */
final class ReferenceMoveProgram428 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenDelayedParallel(
                                                                        ReferenceMoveTimeline
                                                                                .appendThenNextParallel(
                                                                                        ReferenceMoveTimeline
                                                                                                .sequence()
                                                                                                .beginParallel()
                                                                                                .append(
                                                                                                        owner
                                                                                                                .focusCamera(
                                                                                                                        0,
                                                                                                                        0.25f)),
                                                                                        owner
                                                                                                .tintEnvironment(
                                                                                                        4,
                                                                                                        0,
                                                                                                        0.0f,
                                                                                                        0.8125f,
                                                                                                        0.075f))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1483,
                                                                                                        1,
                                                                                                        14,
                                                                                                        0.0f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .adjustSoundTrack(
                                                                                                        1,
                                                                                                        0,
                                                                                                        0.0f,
                                                                                                        1024.0f))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1437,
                                                                                                        2,
                                                                                                        16,
                                                                                                        1000.0f,
                                                                                                        0.9375f,
                                                                                                        owner.casterHandle))
                                                                                .onStart(
                                                                                        owner
                                                                                                .preloadParticleEffect(
                                                                                                        603))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        603,
                                                                                                        0,
                                                                                                        9,
                                                                                                        8,
                                                                                                        0.5f)),
                                                                        owner.emitParticles(
                                                                                -1, 603, 3, 9, 8,
                                                                                0.5f),
                                                                        1.2f),
                                                        owner.shakeCombatant(
                                                                14, 2, 1, 0.016f, 0.032f, 2.0f,
                                                                0.0f),
                                                        1.22f)
                                                .append(owner.focusCamera(1, 0.2f))
                                                .endGroup()
                                                .endGroup()
                                                .beginSequence()
                                                .waitSeconds(1.42f)
                                                .beginParallel()
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1448,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1475,
                                                                2,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 603, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 603, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 603, 4, 11, 8, 0.5f))
                                                .append(owner.setSpriteAnimationPaused(14, 1)),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                1,
                                                0.016f,
                                                0.032f,
                                                -0.19995117f,
                                                0.19995117f))
                                .append(owner.restoreCamera(0.25f))
                                .append(owner.setSpriteAnimationPaused(14, 0)),
                        owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.075f));
        return result;
    }
}
