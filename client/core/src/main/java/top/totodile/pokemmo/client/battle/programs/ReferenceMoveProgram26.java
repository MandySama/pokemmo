package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 飞踢（招式 ID：26）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出高高的腾空踢攻击对手. 如果踢偏则自己会受到伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110026（名称）和 120026（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/JumpKickAnimation.java
 *
 * <p>源文件 SHA-256：19060d8af9f55746cabaaaafc1a8949a3a548fd3f514e7569506fe58b259b09c
 */
final class ReferenceMoveProgram26 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .onStart(owner.preloadParticleEffect(188))
                                                        .append(
                                                                owner.launchParticles(
                                                                        188, 0, 9, 11, 0.5f, 0.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1444,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1437,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        500.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        583.3333f,
                                                        0.859375f,
                                                        owner.casterHandle),
                                                0.4f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.52f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 188, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 3, 0.016f, 0.032f, -0.19995117f, 0.100097656f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
