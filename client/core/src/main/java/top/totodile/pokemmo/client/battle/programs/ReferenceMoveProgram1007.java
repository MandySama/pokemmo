package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 摄魂术（自定义动画注册 ID：1007）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色。
 *
 * <p>编号对应参考动画注册表；语言条目为 111007（名称）和 121007（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1007Animation.java
 *
 * <p>源文件 SHA-256：153a686c05edb7e99fdf9cedb985e8aed9ac183fd38f382381e1d6227a84ef16
 */
final class ReferenceMoveProgram1007 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.6f))
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
                        .onStart(owner.preloadParticleEffect(1007))
                        .append(owner.emitParticles(-1, 1007, 0, 11, 9, 0.4f))
                        .append(owner.emitParticles(-1, 1007, 1, 9, 8, 0.4f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep8, owner.emitParticles(-1, 1007, 2, 9, 8, 0.4f), 0.8f),
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
