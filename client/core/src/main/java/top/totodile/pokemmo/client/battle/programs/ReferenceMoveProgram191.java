package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 撒菱（招式 ID：191）的攻击／事件阶段动画。
 *
 * <p>招式描述：在对手的脚下扔撒菱. 对替换出场的对手的宝可梦给予伤害.
 *
 * <p>动画说明：播放参考粒子特效；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110191（名称）和 120191（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SpikesAnimation.java
 *
 * <p>源文件 SHA-256：dbbcfea6d5fe8961d82bf1e93aebf99600954c2ab3196dec14d1ee34e4d0e81a
 */
final class ReferenceMoveProgram191 {

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
                                                                                                                        .setSpriteAnimationPaused(
                                                                                                                                18,
                                                                                                                                1))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1421,
                                                                                                                                1,
                                                                                                                                14,
                                                                                                                                166.66667f,
                                                                                                                                0.78125f,
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
                                                                                                                                333.33334f,
                                                                                                                                0.78125f,
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
                                                                                                                                500.0f,
                                                                                                                                0.78125f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1436,
                                                                                                                                2,
                                                                                                                                16,
                                                                                                                                500.0f,
                                                                                                                                0.78125f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1436,
                                                                                                                                2,
                                                                                                                                16,
                                                                                                                                666.6667f,
                                                                                                                                0.78125f,
                                                                                                                                owner.casterHandle))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .scheduleSound(
                                                                                                                                (byte)
                                                                                                                                        2,
                                                                                                                                (short)
                                                                                                                                        1436,
                                                                                                                                2,
                                                                                                                                16,
                                                                                                                                833.3333f,
                                                                                                                                0.78125f,
                                                                                                                                owner.casterHandle))
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                357))
                                                                                                        .waitSeconds(
                                                                                                                0.8f),
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                357,
                                                                                                                0,
                                                                                                                9,
                                                                                                                1,
                                                                                                                0.25f,
                                                                                                                240.0f),
                                                                                                0.2f),
                                                                                owner
                                                                                        .launchParticles(
                                                                                                357,
                                                                                                0,
                                                                                                9,
                                                                                                1,
                                                                                                0.25f,
                                                                                                240.0f),
                                                                                0.4f)
                                                                        .append(
                                                                                owner
                                                                                        .launchParticles(
                                                                                                357,
                                                                                                0,
                                                                                                9,
                                                                                                1,
                                                                                                0.25f,
                                                                                                240.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 357, 1,
                                                                                        1, 8,
                                                                                        0.25f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 357, 2,
                                                                                        1, 8,
                                                                                        0.25f))
                                                                        .waitSeconds(0.8f),
                                                                owner.launchParticles(
                                                                        357, 0, 9, 0, 0.25f,
                                                                        240.0f),
                                                                0.6f),
                                                owner.launchParticles(357, 0, 9, 0, 0.25f, 240.0f),
                                                0.8f)
                                        .append(owner.launchParticles(357, 0, 9, 0, 0.25f, 240.0f))
                                        .append(owner.emitParticles(-1, 357, 1, 0, 8, 0.25f)),
                                owner.emitParticles(-1, 357, 2, 0, 8, 0.25f))
                        .append(owner.setSpriteAnimationPaused(18, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
