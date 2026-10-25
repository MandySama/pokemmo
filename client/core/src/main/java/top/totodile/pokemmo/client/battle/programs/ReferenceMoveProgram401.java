package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 水流尾（招式 ID：401）的攻击／事件阶段动画。
 *
 * <p>招式描述：如惊涛骇浪般挥动大尾巴攻击对手.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110401（名称）和 120401（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/AquaTailAnimation.java
 *
 * <p>源文件 SHA-256：fe53c0db19a4fee016da3c9dab8ba1f82562846d174fd2d455c8b1231b44986b
 */
final class ReferenceMoveProgram401 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenNextParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .sequence()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .focusCamera(
                                                                                                                0,
                                                                                                                0.4f))
                                                                                        .endGroup()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1452,
                                                                                                                1,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.625f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1415,
                                                                                                                2,
                                                                                                                14,
                                                                                                                0.0f,
                                                                                                                0.9921875f,
                                                                                                                owner.casterHandle)),
                                                                                owner
                                                                                        .scaleCombatant(
                                                                                                14,
                                                                                                3,
                                                                                                1,
                                                                                                0.016f,
                                                                                                0.064f,
                                                                                                0.30004883f,
                                                                                                -0.30004883f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1897,
                                                                                        2,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                576))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 576, 0,
                                                                                        9, 8,
                                                                                        0.5f)),
                                                                owner.emitParticles(
                                                                        -1, 576, 3, 9, 8, 0.125f),
                                                                0.6f)
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .endGroup()
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1511,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.3125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1651,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 576, 4, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 576, 5, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 576, 1, 11, 8, 0.25f)),
                                                owner.emitParticles(-1, 576, 2, 11, 8, 0.5f),
                                                0.08f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 6, 0.016f, 0.032f, 0.19995117f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
