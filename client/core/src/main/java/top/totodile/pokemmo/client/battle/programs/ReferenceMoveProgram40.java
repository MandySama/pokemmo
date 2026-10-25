package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 毒针（招式 ID：40）的攻击／事件阶段动画。
 *
 * <p>招式描述：将有毒的针刺入对手进行攻击. 有时会让对手陷入中毒状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110040（名称）和 120040（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PoisonStingAnimation.java
 *
 * <p>源文件 SHA-256：82ab6016c7a347d65875bd2bf7fa4ab41fbe28c235dfb3dcd0131b662c8d60b0
 */
final class ReferenceMoveProgram40 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1421,
                                                                        1,
                                                                        16,
                                                                        333.33334f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        1,
                                                                        16,
                                                                        500.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1454,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14, 2, 1, 0.016f, 0.048f,
                                                                        1.0f, 0.0f))
                                                        .onStart(owner.preloadParticleEffect(201))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 201, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 201, 1, 11, 8, 0.5f)),
                                                owner.launchParticles(201, 2, 9, 11, 0.5f, 240.0f),
                                                0.2f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel()
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 2, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
