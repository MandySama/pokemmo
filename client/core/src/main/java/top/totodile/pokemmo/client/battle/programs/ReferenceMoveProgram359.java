package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 臂锤（招式 ID：359）的攻击／事件阶段动画。
 *
 * <p>招式描述：挥舞强力而沉重的拳头, 给予对手伤害. 自己的速度会降低.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110359（名称）和 120359（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/HammerArmAnimation.java
 *
 * <p>源文件 SHA-256：79101c1b1e42061f3f60f021ec7a1db567a1c13bbbc2d868b30e0167c498819c
 */
final class ReferenceMoveProgram359 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
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
                                                        (short) 1424,
                                                        2,
                                                        16,
                                                        250.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        250.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(533))
                                        .append(owner.emitParticles(-1, 533, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 533, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 533, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.30004883f)),
                                owner.shakeCombatant(16, 2, 4, 0.016f, 0.032f, 0.19995117f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
