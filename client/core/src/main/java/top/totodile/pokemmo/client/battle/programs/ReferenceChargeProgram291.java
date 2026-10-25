package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 潜水（招式 ID：291）的蓄力阶段动画。
 *
 * <p>招式描述：第1回合潜入, 第2回合浮上来进行攻击.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110291（名称）和 120291（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/DiveAnimation.java
 *
 * <p>源文件 SHA-256：07431c741895873fae43353810c4574edafe4b061eb3dd64060820a1615241b7
 */
final class ReferenceChargeProgram291 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .beginSequence()
                                                        .waitSeconds(0.32f)
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1762,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle)),
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.128f,
                                                        -0.39990234f,
                                                        0.39990234f),
                                                0.4f)
                                        .onStart(owner.preloadParticleEffect(457))
                                        .append(
                                                owner.shakeCombatant(
                                                        14, 1, 1, 0.016f, 0.48f, 0.0f, -20.0f)),
                                owner.emitParticles(-1, 457, 2, 9, 8, 0.0f),
                                0.56f)
                        .append(owner.emitParticles(-1, 457, 2, 9, 8, -0.25f))
                        .append(owner.emitParticles(-1, 457, 2, 9, 8, -0.25f))
                        .append(owner.emitParticles(-1, 457, 0, 9, 8, 0.0f))
                        .append(owner.emitParticles(-1, 457, 0, 9, 8, 0.0f))
                        .append(owner.emitParticles(-1, 457, 0, 9, 8, 0.0f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.16f)
                        .beginParallel()
                        .append(owner.setCombatantVisibility(14, 3))
                        .append(owner.shakeCombatant(14, 5, 0, 0.0f, 0.016f, 0.0f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
