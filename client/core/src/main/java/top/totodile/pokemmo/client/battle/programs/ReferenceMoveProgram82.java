package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 龙之怒（招式 ID：82）的攻击／事件阶段动画。
 *
 * <p>招式描述：将愤怒的冲击波撞向对手进行攻击. 必定会给予40的伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110082（名称）和 120082（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DragonRageAnimation.java
 *
 * <p>源文件 SHA-256：208eec7a9e54274606746c5ad44146c850c34a0bef35f599349debbeb9b125d2
 */
final class ReferenceMoveProgram82 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1492,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 1344.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 1120.0f, 160.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1425,
                                                                        2,
                                                                        16,
                                                                        666.6667f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1535,
                                                                        1,
                                                                        16,
                                                                        750.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1535,
                                                                        1,
                                                                        16,
                                                                        916.6667f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1535,
                                                                        1,
                                                                        16,
                                                                        1083.3334f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1535,
                                                                        1,
                                                                        16,
                                                                        1250.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1536,
                                                                        2,
                                                                        16,
                                                                        1416.6666f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(246))
                                                        .append(
                                                                owner.launchParticles(
                                                                        246, 1, 9, 11, 0.5f, 0.0f))
                                                        .append(
                                                                owner.launchParticles(
                                                                        246, 2, 9, 11, 0.5f, 0.0f)),
                                                owner.launchParticles(246, 3, 9, 11, 0.5f, 0.0f),
                                                0.2f)
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.6f)
                                        .beginParallel()
                                        .append(owner.emitParticles(-1, 246, 0, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 8, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
