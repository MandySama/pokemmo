package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 广域防守（招式 ID：469）的攻击／事件阶段动画。
 *
 * <p>招式描述：使用者的队伍在一回合内免疫大范围攻击. 如果连续使用, 守住、看穿和挺住可能会失败.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110469（名称）和 120469（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/WideGuardAnimation.java
 *
 * <p>源文件 SHA-256：c495e72e7725ae4463977ea120ee609714819224497c164532d989feceecf12c
 */
final class ReferenceMoveProgram469 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.waitThenParallel(0.4f)
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1863,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                1,
                                                                14,
                                                                1166.6666f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1450,
                                                                2,
                                                                14,
                                                                0.0f,
                                                                0.46875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1450,
                                                                2,
                                                                14,
                                                                1500.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                2,
                                                                14,
                                                                2500.0f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.shakeCombatant(
                                                                14, 3, 7, 0.0f, 0.032f, 1.5f, 0.0f))
                                                .onStart(owner.preloadParticleEffect(645))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 645, 2, 9, 8, 0.5f)),
                                        owner.emitParticles(-1, 645, 1, 9, 8, 0.5f),
                                        1.6f),
                                owner.emitParticles(-1, 645, 0, 9, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
