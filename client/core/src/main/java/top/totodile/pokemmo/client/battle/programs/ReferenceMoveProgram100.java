package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 瞬间移动（招式 ID：100）的攻击／事件阶段动画。
 *
 * <p>招式描述：对战时, 训练家使用则一定能换下宝可梦, 而野生宝可梦使用则可能立即逃跑. 对战外使用时, 可以传送到最近一次进入过的宝可梦中心.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵显隐。
 *
 * <p>编号对应参考动画注册表；语言条目为 110100（名称）和 120100（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/TeleportAnimation.java
 *
 * <p>源文件 SHA-256：3eaf3920684945d55c74bc93847cc20aeb12935d009b54a133ea3e88f701db94
 */
final class ReferenceMoveProgram100 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.tintEnvironment(
                                                                                2, 0, 0.0f, 0.9375f,
                                                                                0.025f))
                                                                .waitSeconds(0.6f)
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1487,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1376,
                                                                                1,
                                                                                14,
                                                                                833.3333f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                1, 0, 0.0f,
                                                                                800.0f)),
                                                        owner.adjustSoundTrack(
                                                                1, 1, 480.0f, 320.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1721,
                                                                2,
                                                                14,
                                                                333.33334f,
                                                                0.5859375f,
                                                                owner.casterHandle)),
                                        owner.scaleCombatant(
                                                14, 1, 0, 0.0f, 0.096f, 0.100097656f, 1.0f),
                                        0.06f),
                                owner.shakeCombatant(14, 2, 2, 0.0f, 0.032f, 0.39990234f, 0.0f),
                                0.22f)
                        .append(owner.shakeCombatant(14, 1, 0, 0.0f, 0.32f, 0.0f, 10.0f))
                        .onStart(owner.preloadParticleEffect(268))
                        .append(owner.emitParticles(-1, 268, 0, 9, 8, 0.5f))
                        .append(owner.setCombatantVisibility(14, 3))
                        .append(owner.shakeCombatant(14, 5, 0, 0.0f, 0.32f, 0.0f, 0.0f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 0, 0.9375f, 0.0f, 0.025f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
