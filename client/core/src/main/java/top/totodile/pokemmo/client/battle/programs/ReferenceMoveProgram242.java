package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 咬碎（招式 ID：242）的攻击／事件阶段动画。
 *
 * <p>招式描述：用锋锐的牙齿咬住对手, 会降低对手防御.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110242（名称）和 120242（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/CrunchAnimation.java
 *
 * <p>源文件 SHA-256：3c1369e8e4b36b3895199b365e279a69a0d54e4baaf099e6da32190cd36232c0
 */
final class ReferenceMoveProgram242 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1720,
                                        1,
                                        16,
                                        416.66666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1488,
                                        2,
                                        16,
                                        416.66666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1720,
                                        0,
                                        16,
                                        416.66666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(409))
                        .append(owner.emitParticles(-1, 409, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 409, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 409, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 409, 3, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 409, 4, 11, 8, 0.5f))
                        .beginSequence()
                        .waitSeconds(0.24f)
                        .beginParallel()
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
