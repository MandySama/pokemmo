package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰冻伏特（招式 ID：553）的蓄力阶段动画。
 *
 * <p>招式描述：用覆盖着电流的冰块, 在第2回合撞向对手. 有时会让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110553（名称）和 120553（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FreezeShockAnimation.java
 *
 * <p>源文件 SHA-256：aa3d9ad0eef3ce037f95bc10e3c95dff4887fb021773fba4fce65003c183417e
 */
final class ReferenceChargeProgram553 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1522,
                                                        1,
                                                        14,
                                                        166.66667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1895,
                                                        2,
                                                        14,
                                                        333.33334f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(720))
                                        .append(owner.emitParticles(-1, 720, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 720, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 720, 2, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 720, 3, 9, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
