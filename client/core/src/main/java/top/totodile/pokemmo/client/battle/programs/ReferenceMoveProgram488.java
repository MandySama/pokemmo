package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蓄能焰袭（招式 ID：488）的攻击／事件阶段动画。
 *
 * <p>招式描述：让火焰覆盖全身, 攻击对手. 积蓄力量并提高自己的速度.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110488（名称）和 120488（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FlameChargeAnimation.java
 *
 * <p>源文件 SHA-256：282168128c0e777c2ad4b1e4ca5f42c753c4b4b81af7c56f9c462f8262f9d242
 */
final class ReferenceMoveProgram488 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1535,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1805,
                                        2,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1535,
                                        1,
                                        14,
                                        833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1805,
                                        2,
                                        14,
                                        833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1805,
                                        1,
                                        14,
                                        1666.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1466,
                                        2,
                                        14,
                                        2166.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(658))
                        .append(owner.emitParticles(-1, 658, 0, 9, 8, 0.25f))
                        .append(owner.emitParticles(-1, 658, 1, 9, 8, 0.25f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep13 =
                animationStep9
                        .append(owner.emitParticles(-1, 658, 2, 9, 8, 0.25f))
                        .append(owner.tintCombatant(14, 0.75f, 0.0f, 0.5f, color))
                        .beginSequence()
                        .waitSeconds(2.4f)
                        .beginParallel()
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1424,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1507,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.emitParticles(-1, 658, 4, 11, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep13.append(
                                        owner.emitParticles(-1, 658, 3, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 3, 0.0f, 0.048f, 0.30004883f, -0.30004883f))
                        .append(owner.tintCombatant(14, 0.75f, 0.5f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
