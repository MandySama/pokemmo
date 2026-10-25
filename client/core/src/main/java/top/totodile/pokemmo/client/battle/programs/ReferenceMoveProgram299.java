package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 火焰踢（招式 ID：299）的攻击／事件阶段动画。
 *
 * <p>招式描述：攻击对手后, 有时会使其陷入灼伤状态. 也容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110299（名称）和 120299（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/BlazeKickAnimation.java
 *
 * <p>源文件 SHA-256：f98055d8cd613bea410f2cde4ab15a59b82bed47c6a37883a4bd9546a3302b00
 */
final class ReferenceMoveProgram299 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .onStart(
                                                                        owner.setHealthHudHidden(
                                                                                18, true))
                                                                .append(owner.focusCamera(1, 0.4f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1445,
                                                                                1,
                                                                                16,
                                                                                0.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                1, 0, 0.0f, 480.0f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                1,
                                                                                16,
                                                                                583.3333f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1426,
                                                                                2,
                                                                                16,
                                                                                583.3333f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                1,
                                                                                16,
                                                                                750.0f,
                                                                                0.46875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                1,
                                                                                16,
                                                                                916.6667f,
                                                                                0.15625f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1420,
                                                                                1,
                                                                                16,
                                                                                1083.3334f,
                                                                                0.078125f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1426,
                                                                                2,
                                                                                16,
                                                                                750.0f,
                                                                                0.46875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1426,
                                                                                2,
                                                                                16,
                                                                                916.6667f,
                                                                                0.234375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1426,
                                                                                2,
                                                                                16,
                                                                                1083.3334f,
                                                                                0.078125f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.shakeCombatant(
                                                                                14, 2, 3, 0.016f,
                                                                                0.096f, 1.0f, 0.0f))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        16, 1))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                465))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 465, 3, 11, 8,
                                                                                0.5f)),
                                                        owner.emitParticles(
                                                                -1, 465, 4, 11, 8, 0.5f),
                                                        0.1f)
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 465, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 465, 1, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 465, 0, 11, 8, 0.5f),
                                        0.5f),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.0f, 0.048f, 0.39990234f, -0.39990234f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
