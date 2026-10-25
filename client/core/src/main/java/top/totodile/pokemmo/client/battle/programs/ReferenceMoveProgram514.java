package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 报仇（招式 ID：514）的攻击／事件阶段动画。
 *
 * <p>招式描述：为倒下的同伴报仇. 如果上一回合有同伴倒下, 威力就会提高.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110514（名称）和 120514（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/RetaliateAnimation.java
 *
 * <p>源文件 SHA-256：8ca4bed5d09bb8f07713c3634ef7d29c6751f610435038efa8f58d1ec4d660bf
 */
final class ReferenceMoveProgram514 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1426,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.625f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1426,
                                                                                1,
                                                                                14,
                                                                                666.6667f,
                                                                                0.3125f,
                                                                                owner.casterHandle))
                                                                .append(owner.focusCamera(0, 0.3f))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                679))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 679, 0, 11, 8,
                                                                                0.25f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1444,
                                                                                1,
                                                                                14,
                                                                                0.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1)),
                                                        owner.emitParticles(
                                                                -1, 679, 1, 9, 8, 0.25f),
                                                        0.32f)
                                                .append(owner.focusCamera(1, 0.25f))
                                                .append(
                                                        owner.scaleCombatant(
                                                                16,
                                                                2,
                                                                6,
                                                                0.0f,
                                                                0.064f,
                                                                -0.19995117f,
                                                                0.19995117f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                2,
                                                                16,
                                                                166.66667f,
                                                                0.78125f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                1,
                                                                16,
                                                                333.33334f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                2,
                                                                16,
                                                                500.0f,
                                                                0.9921875f,
                                                                owner.casterHandle)),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1420,
                                                1,
                                                16,
                                                666.6667f,
                                                0.9921875f,
                                                owner.casterHandle),
                                        0.52f),
                                owner.emitParticles(-1, 679, 0, 11, 8, 0.5f),
                                0.72f)
                        .append(owner.restoreCamera(0.3f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
