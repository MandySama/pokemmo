package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 酸液炸弹（招式 ID：491）的攻击／事件阶段动画。
 *
 * <p>招式描述：喷出能溶化对手的液体进行攻击. 大幅降低对手的特防.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110491（名称）和 120491（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/AcidSprayAnimation.java
 *
 * <p>源文件 SHA-256：4e20ee4f3668a152f14562252b4dfa248053a152136e0e76819feb85f49746d5
 */
final class ReferenceMoveProgram491 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenDelayedParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .waitThenParallel(
                                                                                                                0.6f)
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .preloadParticleEffect(
                                                                                                                                660))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .launchParticles(
                                                                                                                                660,
                                                                                                                                2,
                                                                                                                                9,
                                                                                                                                11,
                                                                                                                                0.5f,
                                                                                                                                480.0f)),
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1434,
                                                                                                                2,
                                                                                                                16,
                                                                                                                0.0f,
                                                                                                                0.9921875f,
                                                                                                                owner.casterHandle),
                                                                                                0.02f)
                                                                                        .append(
                                                                                                owner
                                                                                                        .launchParticles(
                                                                                                                660,
                                                                                                                2,
                                                                                                                9,
                                                                                                                11,
                                                                                                                0.5f,
                                                                                                                480.0f)),
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1382,
                                                                                        1,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle),
                                                                                0.04f)
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.6f)),
                                                                owner.launchParticles(
                                                                        660, 2, 9, 11, 0.5f,
                                                                        480.0f),
                                                                0.06f)
                                                        .append(
                                                                owner.launchParticles(
                                                                        660, 2, 9, 11, 0.5f,
                                                                        480.0f))
                                                        .onStart(owner.setHealthHudHidden(18, true))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(0.46f)
                                                        .beginParallel()
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 660, 4, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 660, 3, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 660, 6, 11, 8, 0.75f),
                                                0.54f)
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.128f,
                                                        0.100097656f,
                                                        -0.30004883f))
                                        .append(owner.emitParticles(-1, 660, 0, 11, 8, 0.125f))
                                        .append(owner.emitParticles(-1, 660, 5, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1446,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.546875f,
                                                        owner.casterHandle)),
                                owner.emitParticles(-1, 660, 1, 11, 8, 0.125f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
