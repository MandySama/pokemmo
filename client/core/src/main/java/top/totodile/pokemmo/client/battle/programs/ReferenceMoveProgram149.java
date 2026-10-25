package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 精神波（招式 ID：149）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射神奇的念波进行攻击. 每次使用, 伤害都会改变.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110149（名称）和 120149（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PsywaveAnimation.java
 *
 * <p>源文件 SHA-256：aaeedbd3bc9b72365e1b17249edbf4bc7640a10b0d23e1e93a8b6def5453f9b5
 */
final class ReferenceMoveProgram149 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedOuterParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                ReferenceMoveTimeline
                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                        .appendThenDelayedOuterParallel(
                                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                                        .appendThenDelayedParallel(
                                                                                                                                                                                                ReferenceMoveTimeline
                                                                                                                                                                                                        .sequence()
                                                                                                                                                                                                        .beginParallel()
                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .focusCamera(
                                                                                                                                                                                                                                0,
                                                                                                                                                                                                                                0.3f))
                                                                                                                                                                                                        .endGroup()
                                                                                                                                                                                                        .beginParallel()
                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                                        1434,
                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                16,
                                                                                                                                                                                                                                0.0f,
                                                                                                                                                                                                                                0.9375f,
                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                                        1434,
                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                16,
                                                                                                                                                                                                                                333.33334f,
                                                                                                                                                                                                                                0.9375f,
                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                                        1434,
                                                                                                                                                                                                                                1,
                                                                                                                                                                                                                                16,
                                                                                                                                                                                                                                666.6667f,
                                                                                                                                                                                                                                0.9375f,
                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                        .append(
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .scheduleSound(
                                                                                                                                                                                                                                (byte)
                                                                                                                                                                                                                                        2,
                                                                                                                                                                                                                                (short)
                                                                                                                                                                                                                                        1434,
                                                                                                                                                                                                                                2,
                                                                                                                                                                                                                                16,
                                                                                                                                                                                                                                1000.0f,
                                                                                                                                                                                                                                0.9375f,
                                                                                                                                                                                                                                owner.casterHandle))
                                                                                                                                                                                                        .onStart(
                                                                                                                                                                                                                owner
                                                                                                                                                                                                                        .preloadParticleEffect(
                                                                                                                                                                                                                                313)),
                                                                                                                                                                                                owner
                                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                                313,
                                                                                                                                                                                                                0,
                                                                                                                                                                                                                9,
                                                                                                                                                                                                                11,
                                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                                1200.0f),
                                                                                                                                                                                                0.02f),
                                                                                                                                                                                owner
                                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                                313,
                                                                                                                                                                                                0,
                                                                                                                                                                                                9,
                                                                                                                                                                                                11,
                                                                                                                                                                                                0.5f,
                                                                                                                                                                                                1200.0f),
                                                                                                                                                                                0.04f),
                                                                                                                                                                owner
                                                                                                                                                                        .launchParticles(
                                                                                                                                                                                313,
                                                                                                                                                                                0,
                                                                                                                                                                                9,
                                                                                                                                                                                11,
                                                                                                                                                                                0.5f,
                                                                                                                                                                                1200.0f),
                                                                                                                                                                0.06f),
                                                                                                                                                owner
                                                                                                                                                        .launchParticles(
                                                                                                                                                                313,
                                                                                                                                                                0,
                                                                                                                                                                9,
                                                                                                                                                                11,
                                                                                                                                                                0.5f,
                                                                                                                                                                1200.0f),
                                                                                                                                                0.08f),
                                                                                                                                owner
                                                                                                                                        .launchParticles(
                                                                                                                                                313,
                                                                                                                                                0,
                                                                                                                                                9,
                                                                                                                                                11,
                                                                                                                                                0.5f,
                                                                                                                                                1200.0f),
                                                                                                                                0.1f),
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                313,
                                                                                                                                0,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                1200.0f),
                                                                                                                0.12f),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                313,
                                                                                                                0,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                1200.0f),
                                                                                                0.14f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                313,
                                                                                                0,
                                                                                                9,
                                                                                                11,
                                                                                                0.5f,
                                                                                                1200.0f),
                                                                                0.16f),
                                                                owner.launchParticles(
                                                                        313, 0, 9, 11, 0.5f,
                                                                        1200.0f),
                                                                0.18f),
                                                owner.launchParticles(313, 0, 9, 11, 0.5f, 1200.0f),
                                                0.58f)
                                        .append(owner.focusCamera(1, 0.3f))
                                        .endGroup()
                                        .endGroup()
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9765625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1483,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.8203125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1280.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 320.0f))
                                        .append(owner.emitParticles(-1, 313, 1, 11, 8, 0.5f))
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
