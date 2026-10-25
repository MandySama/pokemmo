package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 发劲（招式 ID：395）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手的身体发出冲击波进行攻击. 有时会让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110395（名称）和 120395（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/ForcePalmAnimation.java
 *
 * <p>源文件 SHA-256：633ad6d17f6ee45f182caf03ebfbd874ce3f433c48ede2ded5c2887e2f90f1a7
 */
final class ReferenceMoveProgram395 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1650,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.234375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(570))
                                        .append(owner.emitParticles(-1, 570, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 570, 4, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 570, 5, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 570, 0, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 2, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
