package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 吸取拳（招式 ID：409）的攻击／事件阶段动画。
 *
 * <p>招式描述：用拳头吸取对手的力量. 可以回复给予对手伤害的一半HP.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110409（名称）和 120409（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/DrainPunchAnimation.java
 *
 * <p>源文件 SHA-256：0bdb250bb4c275783335788c817887a1f3a24c2c40cbf6f74803421067c30342
 */
final class ReferenceMoveProgram409 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel(),
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.625f, 0.025f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1421,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                1,
                                                                16,
                                                                166.66667f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1504,
                                                                2,
                                                                16,
                                                                166.66667f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1480,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                1,
                                                                16,
                                                                1666.6666f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                1, 0, 320.0f, 480.0f))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                1, 1, 1120.0f, 480.0f))
                                                .append(owner.focusCamera(1, 0.25f))
                                                .onStart(owner.preloadParticleEffect(584))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 584, 3, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 584, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 584, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 584, 1, 11, 9, 0.25f))
                                                .append(owner.setSpriteAnimationPaused(16, 1)),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                1,
                                                0.016f,
                                                0.064f,
                                                -0.30004883f,
                                                0.30004883f),
                                        0.6f)
                                .append(owner.focusCamera(0, 0.25f))
                                .endGroup()
                                .endGroup()
                                .endGroup()
                                .beginParallel()
                                .append(owner.restoreCamera(0.25f))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.tintEnvironment(4, 0, 0.625f, 0.0f, 0.025f));
        return result;
    }
}
