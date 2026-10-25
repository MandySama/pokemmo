package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 精神冲击（招式 ID：473）的攻击／事件阶段动画。
 *
 * <p>招式描述：将神奇的念波实体化攻击对手. 给予物理伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110473（名称）和 120473（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/PsyshockAnimation.java
 *
 * <p>源文件 SHA-256：096241d3b42436d551004c9a5e6916964e8331879e38c15c9779e3df3fadbee4
 */
final class ReferenceMoveProgram473 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .waitThenParallel(
                                                                                                0.6f)
                                                                                        .append(
                                                                                                owner
                                                                                                        .setSpriteAnimationPaused(
                                                                                                                14,
                                                                                                                1))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1363,
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
                                                                                                                        1363,
                                                                                                                1,
                                                                                                                14,
                                                                                                                100.0f,
                                                                                                                0.625f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1363,
                                                                                                                1,
                                                                                                                14,
                                                                                                                200.0f,
                                                                                                                0.625f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1363,
                                                                                                                1,
                                                                                                                14,
                                                                                                                300.0f,
                                                                                                                0.625f,
                                                                                                                owner.casterHandle)),
                                                                                owner
                                                                                        .shakeCombatant(
                                                                                                14,
                                                                                                2,
                                                                                                10,
                                                                                                0.0f,
                                                                                                0.032f,
                                                                                                0.30004883f,
                                                                                                0.0f),
                                                                                0.4f)
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1895,
                                                                                        1,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.625f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1589,
                                                                                        1,
                                                                                        16,
                                                                                        416.66666f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1623,
                                                                                        2,
                                                                                        16,
                                                                                        616.6667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                0,
                                                                                                672.0f,
                                                                                                320.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1652,
                                                                                        1,
                                                                                        16,
                                                                                        1250.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1475,
                                                                                        1,
                                                                                        16,
                                                                                        1500.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                646))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 646, 0,
                                                                                        11, 8,
                                                                                        0.5f)),
                                                                owner.emitParticles(
                                                                        -1, 646, 1, 11, 8, 0.5f),
                                                                0.6f)
                                                        .onStart(owner.setHealthHudHidden(18, true))
                                                        .onStart(owner.preloadParticleEffect(618)),
                                                owner.emitParticles(-1, 618, 0, 11, 8, 0.5f),
                                                1.8f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 5, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
