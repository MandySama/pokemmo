package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 恶意追击（招式 ID：372）的攻击／事件阶段动画。
 *
 * <p>招式描述：如果此回合内对手已经受到伤害的话, 招式威力会变成2倍.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110372（名称）和 120372（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/AssuranceAnimation.java
 *
 * <p>源文件 SHA-256：d84a1f0581ca3413cb81181ea91790dd25650d3a110225223e7bba77c29decc2
 */
final class ReferenceMoveProgram372 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.4f))
                                                .endGroup()
                                                .beginParallel()
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1421,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1422,
                                                                1,
                                                                16,
                                                                166.66667f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1424,
                                                                2,
                                                                16,
                                                                166.66667f,
                                                                0.546875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1422,
                                                                2,
                                                                16,
                                                                333.33334f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1424,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.546875f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(547))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 547, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 547, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 547, 2, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 547, 3, 11, 8, 0.5f),
                                        0.4f),
                                owner.shakeCombatant(16, 2, 6, 0.016f, 0.016f, 0.19995117f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
