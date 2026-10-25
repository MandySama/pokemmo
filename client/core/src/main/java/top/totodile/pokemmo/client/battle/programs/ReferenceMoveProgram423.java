package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰冻牙（招式 ID：423）的攻击／事件阶段动画。
 *
 * <p>招式描述：用藏有冷气的牙齿咬住对手. 有时会使对手畏缩或陷入冰冻状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110423（名称）和 120423（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/IceFangAnimation.java
 *
 * <p>源文件 SHA-256：3d92c13385020f3cdfb1781d15a889daa221f19cb711029958473db2206d06d9
 */
final class ReferenceMoveProgram423 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.4f))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1720,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1521,
                                                                2,
                                                                16,
                                                                366.66666f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(598))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 598, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 598, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 598, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 598, 3, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 598, 4, 11, 8, 0.5f),
                                        0.2f),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
