package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 烧净（招式 ID：510）的攻击／事件阶段动画。
 *
 * <p>招式描述：用火焰攻击对手. 对手携带树果等时, 会烧掉, 使其不能使用.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110510（名称）和 120510（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/IncinerateAnimation.java
 *
 * <p>源文件 SHA-256：91c42644dd09e765885dfec35b7e52bb72129d03222809d2468479a40b269220
 */
final class ReferenceMoveProgram510 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .waitSeconds(0.6f)
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1806,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(675))
                                        .append(owner.emitParticles(-1, 675, 1, 9, 11, 0.5f)),
                                owner.emitParticles(-1, 675, 0, 9, 11, 0.5f),
                                0.4f)
                        .append(owner.focusCamera(1, 0.25f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.8f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        666.6667f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                animationStep7,
                                                                owner.emitParticles(
                                                                        -1, 675, 3, 11, 8, 0.25f),
                                                                1.2f)
                                                        .append(
                                                                owner.tintCombatant(
                                                                        16, 0.75f, 0.0f, 0.625f,
                                                                        color)),
                                                owner.emitParticles(-1, 675, 2, 11, 8, 0.25f),
                                                1.6f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.emitParticles(-1, 675, 4, 11, 8, 0.25f),
                                1.9f),
                        owner.scaleCombatant(16, 2, 6, 0.016f, 0.032f, 0.30004883f, -0.100097656f),
                        2.3f);
        Color color2 = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep10, owner.emitParticles(-1, 675, 5, 11, 8, 0.5f))
                        .append(owner.tintCombatant(16, 1.25f, 0.625f, 0.0f, color2))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
