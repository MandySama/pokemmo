package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 交错闪电（招式 ID：559）的攻击／事件阶段动画。
 *
 * <p>招式描述：释放出巨大的闪电. 受到巨大的火焰影响时, 招式威力会提高.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵显隐；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110559（名称）和 120559（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FusionBoltAnimation.java
 *
 * <p>源文件 SHA-256：e495d519086f05e8a07d7630443cffcae28f95f61f10d785211f1123aed3ebf0
 */
final class ReferenceMoveProgram559 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        2006,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 640.0f, 320.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 1, 14, 1000.0f, 0.0f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1976,
                                        2,
                                        14,
                                        833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1590,
                                        1,
                                        14,
                                        1333.3334f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(22912);
        ReferenceMoveTimeline animationStep16 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                animationStep5
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1995,
                                                                                        2,
                                                                                        16,
                                                                                        2166.6667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                1,
                                                                                                2080.0f,
                                                                                                160.0f))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.8f))
                                                                        .append(
                                                                                owner
                                                                                        .tintEnvironment(
                                                                                                4,
                                                                                                0,
                                                                                                0.0f,
                                                                                                0.75f,
                                                                                                0.05f))
                                                                        .append(
                                                                                owner.tintCombatant(
                                                                                        14, 0.5f,
                                                                                        0.0f, 0.75f,
                                                                                        color))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                731)),
                                                                owner.emitParticles(
                                                                        -1, 731, 4, 9, 8, 0.75f),
                                                                0.6f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 731, 4, 9, 8, 0.75f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 731, 0, 9, 8, 0.75f))
                                                        .onStart(owner.setHealthHudHidden(14, true))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(1.4f)
                                                        .beginParallel()
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14, 1, 0, 0.016f, 0.384f,
                                                                        0.0f, 12.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 731, 3, 9, 8, 0.25f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 731, 1, 9, 8, 0.25f)),
                                                owner.emitParticles(-1, 731, 6, 9, 8, 0.75f),
                                                1.8f)
                                        .append(owner.setCombatantVisibility(14, 3))
                                        .append(owner.emitParticles(-1, 731, 4, 0, 0, 2.5f)),
                                owner.emitParticles(-1, 731, 7, 0, 0, 2.5f),
                                3.4f)
                        .append(owner.emitParticles(-1, 731, 2, 2, 0, 0.0f))
                        .append(owner.focusCamera(1, 0.3f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1816,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(22912);
        ReferenceMoveTimeline animationStep19 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep16,
                                owner.scheduleSound(
                                        (byte) 2,
                                        1981,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle),
                                3.7f)
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color2))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, 0.30004883f, -0.30004883f))
                        .append(owner.shakeCombatant(16, 2, 12, 0.016f, 0.016f, 0.5f, 0.0f))
                        .append(owner.emitParticles(-1, 731, 3, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 731, 5, 11, 8, 0.75f));
        Color color3 = EncounterMoveAnimation.color(22912);
        ReferenceMoveTimeline animationStep20 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep19
                                        .append(owner.emitParticles(-1, 731, 1, 11, 8, 0.75f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.setCombatantVisibility(14, 4)),
                                owner.shakeCombatant(14, 5, 0, 0.016f, 0.128f, 0.0f, 0.0f),
                                4.3f)
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color3));
        color3 = EncounterMoveAnimation.color(22912);
        result =
                animationStep20
                        .append(owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color3))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(4.82f)
                        .beginParallel()
                        .onStart(owner.setHealthHudHidden(14, false))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
