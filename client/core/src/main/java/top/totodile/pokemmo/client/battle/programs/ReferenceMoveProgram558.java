package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 交错火焰（招式 ID：558）的攻击／事件阶段动画。
 *
 * <p>招式描述：释放出巨大的火焰. 受到巨大的闪电影响时, 招式威力会提高.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110558（名称）和 120558（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FusionFlareAnimation.java
 *
 * <p>源文件 SHA-256：f22cfca742db98ebfd030680b86cc994dd71d112b9cf8ffb331960f1b689b11a
 */
final class ReferenceMoveProgram558 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1589,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1976,
                                        1,
                                        14,
                                        833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1995,
                                        2,
                                        14,
                                        1333.3334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1966,
                                        1,
                                        14,
                                        2166.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1885,
                                        1,
                                        2,
                                        2833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1885,
                                        2,
                                        16,
                                        3000.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.focusCamera(0, 0.8f))
                        .onStart(owner.preloadParticleEffect(730));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        animationStep7.append(
                                                                owner.emitParticles(
                                                                        -1, 730, 0, 9, 8, 0.0f)),
                                                        owner.tintCombatant(
                                                                14, 0.5f, 0.0f, 0.5f, color),
                                                        0.4f)
                                                .append(
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.75f, 0.05f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 730, 2, 0, 0, 3.25f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 730, 5, 0, 0, 3.25f)),
                                        owner.emitParticles(-1, 730, 6, 0, 0, 3.25f),
                                        1.6f),
                                owner.emitParticles(-1, 730, 1, 0, 0, 3.25f),
                                2.4f),
                        owner.emitParticles(-1, 730, 3, 0, 0, 3.25f),
                        2.6f);
        Color color2 = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep14 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep13
                                .append(owner.emitParticles(-1, 730, 4, 1, 0, 0.0f))
                                .append(owner.focusCamera(1, 0.3f)),
                        owner.tintCombatant(14, 0.5f, 0.5f, 0.0f, color2),
                        2.9f);
        color2 = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep18 =
                animationStep14
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.5f, color2))
                        .append(owner.shakeCombatant(16, 2, 12, 0.016f, 0.016f, 0.5f, 0.0f))
                        .append(owner.emitParticles(-1, 730, 1, 11, 8, 0.0f))
                        .append(owner.emitParticles(-1, 730, 3, 11, 8, 0.0f))
                        .append(owner.emitParticles(-1, 730, 6, 11, 8, 2.5f))
                        .append(owner.emitParticles(-1, 730, 6, 11, 8, 1.5f));
        Color color3 = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep18
                                        .append(owner.emitParticles(-1, 730, 6, 11, 8, 0.75f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(16, 2, 1, 0.016f, 0.064f, 0.5f, -0.5f),
                                3.7f)
                        .append(owner.restoreCamera(1.25f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(4.1f)
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.tintCombatant(16, 0.5f, 0.5f, 0.0f, color3))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
