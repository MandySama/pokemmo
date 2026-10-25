package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰冻光束（招式 ID：58）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发射冰冻光束进行攻击. 有时会让对手陷入冰冻状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110058（名称）和 120058（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/IceBeamAnimation.java
 *
 * <p>源文件 SHA-256：a221d140bcebfcc86969357fcd1634402de87d51a9d069df60dce400783517e3
 */
final class ReferenceMoveProgram58 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1)),
                                                        owner.tintEnvironment(
                                                                4, 32592, 0.0f, 0.75f, 0.05f),
                                                        0.4f)
                                                .append(owner.focusCamera(1, 0.8f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1450,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.703125f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                1,
                                                                14,
                                                                816.6667f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1488,
                                                                2,
                                                                16,
                                                                416.66666f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1488,
                                                                2,
                                                                16,
                                                                666.6667f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1516,
                                                                1,
                                                                16,
                                                                416.66666f,
                                                                0.78125f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(219))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 219, 0, 9, 11, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 219, 1, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 219, 2, 11, 8, 0.5f),
                                        0.6f),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.tintEnvironment(4, 32592, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
