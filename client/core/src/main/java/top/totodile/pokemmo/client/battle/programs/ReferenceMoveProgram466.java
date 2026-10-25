package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 奇异之风（招式 ID：466）的攻击／事件阶段动画。
 *
 * <p>招式描述：突然刮起毛骨悚然的暴风攻击对手. 有时会提高自己的全部能力.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110466（名称）和 120466（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/OminousWindAnimation.java
 *
 * <p>源文件 SHA-256：075cc78feb325790332257c8345d3106faf60a20e539a54a2115c865bf328cf7
 */
final class ReferenceMoveProgram466 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .onStart(owner.preloadParticleEffect(642))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1497,
                                        1,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 1120.0f, 640.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1425,
                                        2,
                                        14,
                                        0.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 1120.0f, 640.0f));
        Color color = EncounterMoveAnimation.color(12300);
        ReferenceMoveTimeline animationStep6 =
                animationStep5
                        .append(owner.emitParticles(-1, 642, 0, 0, 8, 0.0f))
                        .append(owner.tintEnvironment(4, 6150, 0.0f, 0.75f, 0.05f))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(12300);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        animationStep6,
                                        owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color),
                                        0.4f)
                                .append(owner.setSpriteAnimationPaused(16, 1)),
                        owner.shakeCombatant(16, 2, 13, 0.032f, 0.016f, 0.30004883f, 0.0f),
                        1.6f);
        color = EncounterMoveAnimation.color(12300);
        ReferenceMoveTimeline animationStep8 =
                animationStep7.append(owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color));
        color = EncounterMoveAnimation.color(12300);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep8.append(
                                        owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color)),
                                owner.tintEnvironment(4, 6150, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
