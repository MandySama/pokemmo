package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 高速星星（招式 ID：129）的攻击／事件阶段动画。
 *
 * <p>招式描述：发射星形的光攻击对手. 攻击必定会命中.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110129（名称）和 120129（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SwiftAnimation.java
 *
 * <p>源文件 SHA-256：68ea90632b75603aa3115de49e22b8a0c5b36efadac2f3ec6856e975d7a22f1b
 */
final class ReferenceMoveProgram129 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .sequence()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .tintEnvironment(
                                                                                                                4,
                                                                                                                0,
                                                                                                                0.0f,
                                                                                                                0.75f,
                                                                                                                0.075f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .focusCamera(
                                                                                                                0,
                                                                                                                0.25f))
                                                                                        .endGroup()
                                                                                        .beginParallel()
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                294))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                1483,
                                                                                                                2,
                                                                                                                16,
                                                                                                                0.0f,
                                                                                                                0.625f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                294,
                                                                                                                2,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                294,
                                                                                                                3,
                                                                                                                9,
                                                                                                                8,
                                                                                                                0.5f)),
                                                                                owner.emitParticles(
                                                                                        -1, 294, 1,
                                                                                        9, 11,
                                                                                        0.5f),
                                                                                0.2f),
                                                                owner.emitParticles(
                                                                        -1, 294, 1, 9, 11, 0.5f),
                                                                0.4f),
                                                owner.emitParticles(-1, 294, 1, 9, 11, 0.5f),
                                                0.6f)
                                        .append(owner.emitParticles(-1, 294, 1, 9, 11, 0.5f))
                                        .append(owner.focusCamera(1, 0.25f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.8f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 294, 1, 9, 11, 0.5f))
                                        .append(owner.emitParticles(-1, 294, 0, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.0f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1482,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9765625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1482,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.9765625f,
                                                        owner.casterHandle)),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.016f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.restoreCamera(0.25f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
