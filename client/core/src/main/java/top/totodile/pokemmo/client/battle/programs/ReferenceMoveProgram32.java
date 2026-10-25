package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 角钻（招式 ID：32）的攻击／事件阶段动画。
 *
 * <p>招式描述：用旋转的角刺入对手进行攻击. 只要命中就会一击濒死.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110032（名称）和 120032（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/HornDrillAnimation.java
 *
 * <p>源文件 SHA-256：98101d1b3a227ec8ed31b7a91a735e6e39055553878c271979ec2c9fc6c64611
 */
final class ReferenceMoveProgram32 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .onStart(owner.preloadParticleEffect(194))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1445,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        16,
                                                                        1166.6666f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 800.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1704,
                                                                        1,
                                                                        16,
                                                                        1166.6666f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 194, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 194, 2, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 194, 4, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 194, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 194, 3, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 194, 5, 11, 8, 0.5f),
                                                0.2f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 12, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
