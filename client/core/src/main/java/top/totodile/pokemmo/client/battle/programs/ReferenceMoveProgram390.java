package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 毒菱（招式 ID：390）的攻击／事件阶段动画。
 *
 * <p>招式描述：在对手的脚下撒毒菱. 使对手替换出场的宝可梦中毒.
 *
 * <p>动画说明：播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110390（名称）和 120390（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/ToxicSpikesAnimation.java
 *
 * <p>源文件 SHA-256：1fd79dda7bac77f67d6e4438777af649101b521ba3586294961483a8de36c202
 */
final class ReferenceMoveProgram390 {

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
                                                                                        .appendThenDelayedParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .sequence()
                                                                                                        .beginParallel()
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1421,
                                                                                                                                1,
                                                                                                                                14,
                                                                                                                                0.0f,
                                                                                                                                0.8984375f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1421,
                                                                                                                                2,
                                                                                                                                14,
                                                                                                                                300.0f,
                                                                                                                                0.8984375f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1421,
                                                                                                                                1,
                                                                                                                                14,
                                                                                                                                600.0f,
                                                                                                                                0.8984375f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .setSpriteAnimationPaused(
                                                                                                                                18,
                                                                                                                                1))
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                565))
                                                                                                        .waitSeconds(
                                                                                                                0.8f),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                565,
                                                                                                                1,
                                                                                                                9,
                                                                                                                1,
                                                                                                                0.25f,
                                                                                                                240.0f),
                                                                                                0.2f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                565,
                                                                                                1,
                                                                                                9,
                                                                                                1,
                                                                                                0.25f,
                                                                                                240.0f),
                                                                                0.4f)
                                                                        .append(
                                                                                owner
                                                                                        .launchParticles(
                                                                                                565,
                                                                                                1,
                                                                                                9,
                                                                                                1,
                                                                                                0.25f,
                                                                                                240.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 565, 2,
                                                                                        1, 8,
                                                                                        0.25f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 565, 3,
                                                                                        1, 8,
                                                                                        0.25f))
                                                                        .waitSeconds(0.8f),
                                                                owner.launchParticles(
                                                                        565, 1, 9, 0, 0.25f,
                                                                        240.0f),
                                                                0.6f),
                                                owner.launchParticles(565, 1, 9, 0, 0.25f, 240.0f),
                                                0.8f)
                                        .append(owner.launchParticles(565, 1, 9, 0, 0.25f, 240.0f))
                                        .append(owner.emitParticles(-1, 565, 2, 0, 8, 0.25f)),
                                owner.emitParticles(-1, 565, 3, 0, 8, 0.25f))
                        .append(owner.setSpriteAnimationPaused(18, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
