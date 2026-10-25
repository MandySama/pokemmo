package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 打断特效（自定义动画注册 ID：1080）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111080（名称）和 121080（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/FakeOutAnimation.java
 *
 * <p>源文件 SHA-256：7f083a3f0524bcfb216d6e184f10a31bad68b39386200638179d4f4cfd031a38
 */
final class ReferenceMoveProgram1080 {

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
                                                        (short) 1422,
                                                        1,
                                                        16,
                                                        666.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1713,
                                                        2,
                                                        16,
                                                        83.333336f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 64.0f, 192.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 240.0f, 80.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(418))
                                        .append(owner.emitParticles(-1, 418, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 4, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 5, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 6, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 16, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 8, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 9, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 10, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 11, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 12, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 13, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 14, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 418, 16, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(0.6f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, -0.30004883f, 0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
