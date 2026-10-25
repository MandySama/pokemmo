package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 变圆（招式 ID：111）的攻击／事件阶段动画。
 *
 * <p>招式描述：将身体蜷曲变圆, 从而提高自己的防御.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110111（名称）和 120111（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DefenseCurlAnimation.java
 *
 * <p>源文件 SHA-256：f624d0f33d7b059908b5dbbe6157dc96c6939fdfbb9f0a70ccbdfa6993831f2d
 */
final class ReferenceMoveProgram111 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(0, 0.4f))
                                                .endGroup()
                                                .beginParallel()
                                                .onStart(owner.preloadParticleEffect(276))
                                                .append(owner.emitParticles(-1, 276, 0, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 276, 1, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 276, 2, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 276, 3, 9, 8, 0.5f))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1463,
                                                                2,
                                                                14,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1376,
                                                                2,
                                                                14,
                                                                1333.3334f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                2, 0, 0.0f, 1280.0f)),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1358,
                                                1,
                                                14,
                                                1000.0f,
                                                0.9375f,
                                                owner.casterHandle),
                                        0.12f),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.016f, 0.144f, 0.100097656f, -0.100097656f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
