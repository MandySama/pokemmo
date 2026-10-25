package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 盘蜷（招式 ID：489）的攻击／事件阶段动画。
 *
 * <p>招式描述：盘蜷着集中精神. 提高自己的攻击、防御和命中率.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110489（名称）和 120489（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/CoilAnimation.java
 *
 * <p>源文件 SHA-256：05e9a7d6f1a5bbedc345e1d9e1e2848ca9226005eee580be02808ddd9bdf6da1
 */
final class ReferenceMoveProgram489 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(0, 0.3f))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .onStart(owner.preloadParticleEffect(659))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1863,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                1,
                                                                14,
                                                                1166.6666f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1863,
                                                                2,
                                                                14,
                                                                1166.6666f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                2, 0, 1136.0f, 1424.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                2,
                                                                14,
                                                                2500.0f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 659, 0, 9, 8, 0.25f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 659, 1, 9, 8, 0.25f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 659, 3, 9, 8, 0.25f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 659, 5, 9, 8, 0.25f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 659, 2, 9, 8, 0.25f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 659, 4, 9, 8, 0.25f)),
                                        owner.emitParticles(-1, 659, 6, 9, 8, 0.25f),
                                        0.12f),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.64f, 0.100097656f, -0.100097656f))
                        .append(owner.restoreCamera(0.3f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
