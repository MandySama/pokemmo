package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 终极吸取（招式 ID：202）的攻击／事件阶段动画。
 *
 * <p>招式描述：吸取对手的养分进行攻击. 可以回复给予对手伤害的一半HP.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110202（名称）和 120202（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/GigaDrainAnimation.java
 *
 * <p>源文件 SHA-256：6aecccb4d515146c86b3ac3f136f264cd60982d787080dde6ba74ba8896d3b65
 */
final class ReferenceMoveProgram202 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1417,
                                        1,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1500.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1440.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1483,
                                        2,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1440.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1358,
                                        1,
                                        14,
                                        1583.3334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1471,
                                        2,
                                        14,
                                        1583.3334f,
                                        0.46875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(368))
                        .append(owner.emitParticles(-1, 368, 0, 11, 9, 0.4f))
                        .append(owner.emitParticles(-1, 368, 1, 9, 8, 0.4f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep8, owner.emitParticles(-1, 368, 2, 9, 8, 0.4f), 0.8f),
                        owner.tintCombatant(14, 0.25f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                animationStep9
                        .append(owner.tintCombatant(14, 0.25f, 0.75f, 0.0f, color))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
