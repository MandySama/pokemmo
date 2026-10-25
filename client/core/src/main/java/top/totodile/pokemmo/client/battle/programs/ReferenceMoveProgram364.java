package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 佯攻（招式 ID：364）的攻击／事件阶段动画。
 *
 * <p>招式描述：能够攻击正在使用守住或看穿等招式的对手. 解除其守护效果.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110364（名称）和 120364（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/FeintAnimation.java
 *
 * <p>源文件 SHA-256：d49c2e9e99d3d4d1520154eb7078bd76640597b6d5482d91de1fdc389d37756c
 */
final class ReferenceMoveProgram364 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        2,
                                                                        16,
                                                                        250.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        1,
                                                                        16,
                                                                        916.6667f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(538))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 538, 4, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 538, 5, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 538, 3, 11, 8, 0.5f),
                                                0.2f)
                                        .append(owner.emitParticles(-1, 538, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 538, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 538, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.0f)
                                        .beginParallel()
                                        .append(
                                                owner.shakeCombatant(
                                                        16, 2, 7, 0.0f, 0.048f, 0.30004883f, 0.0f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, -0.19995117f, 0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
