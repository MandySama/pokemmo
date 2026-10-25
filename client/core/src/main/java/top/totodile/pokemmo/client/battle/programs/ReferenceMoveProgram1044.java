package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 济贫（自定义动画注册 ID：1044）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 111044（名称）和 121044（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/BatonPassAnimation.java
 *
 * <p>源文件 SHA-256：f476ab20275951c36ccd04ca23c20389c6ceae7e8fa3105429b0b3b5659bdb5f
 */
final class ReferenceMoveProgram1044 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.6f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1410,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 624.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        14,
                                                        750.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.16f,
                                                        -0.19995117f,
                                                        0.19995117f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.preloadParticleEffect(392))
                                        .append(owner.emitParticles(-1, 392, 1, 9, 8, 0.625f))
                                        .append(owner.emitParticles(-1, 392, 0, 9, 8, 0.625f))
                                        .append(owner.emitParticles(-1, 392, 2, 9, 8, 0.625f)),
                                owner.emitParticles(-1, 392, 4, 9, 8, 0.625f),
                                0.6f)
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.16f, 0.19995117f, -0.19995117f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
