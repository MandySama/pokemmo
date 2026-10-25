package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 骨棒乱打（招式 ID：198）的攻击／事件阶段动画。
 *
 * <p>招式描述：用坚硬的骨头殴打对手进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110198（名称）和 120198（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/BoneRushAnimation.java
 *
 * <p>源文件 SHA-256：401303266b6b52ba444d8361948c49f5659ccc35c4fb1011c3e35c8f73f5ce74
 */
final class ReferenceMoveProgram198 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1435,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        1,
                                                        14,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(364))
                                        .append(owner.emitParticles(-1, 364, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 364, 0, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .beginSequence()
                                        .waitSeconds(0.2f)
                                        .beginParallel(),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, 0.19995117f, -0.19995117f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
