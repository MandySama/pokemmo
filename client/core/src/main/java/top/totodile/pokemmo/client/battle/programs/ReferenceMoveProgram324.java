package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 信号光束（招式 ID：324）的攻击／事件阶段动画。
 *
 * <p>招式描述：发射神奇的光线进行攻击. 有时会使对手混乱.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110324（名称）和 120324（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SignalBeamAnimation.java
 *
 * <p>源文件 SHA-256：a2ec294cef4a035ec37e4b1cdb58919deb22874f3e3afacc4d0509666f0bc075
 */
final class ReferenceMoveProgram324 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .onStart(owner.preloadParticleEffect(494))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1455, 1, 14, 0.0f, 0.546875f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1450, 2, 14, 0.0f, 0.546875f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        2,
                                        14,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 1120.0f, 480.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1752,
                                        1,
                                        16,
                                        666.6667f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(30);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep5
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1752,
                                                1,
                                                16,
                                                1000.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(owner.launchParticles(494, 0, 9, 11, 0.5f, 0.0f))
                                .append(owner.focusCamera(1, 0.4f))
                                .beginSequence()
                                .waitSeconds(0.4f)
                                .beginParallel()
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(
                                        owner.shakeCombatant(
                                                16, 2, 16, 0.0f, 0.032f, 0.30004883f, 0.0f)),
                        owner.tintCombatant(16, 0.25f, 0.0f, 0.75f, color),
                        0.6f);
        color = EncounterMoveAnimation.color(30);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep6, owner.tintCombatant(16, 0.25f, 0.75f, 0.0f, color), 0.8f);
        color = EncounterMoveAnimation.color(960);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep7, owner.tintCombatant(16, 0.25f, 0.0f, 0.75f, color), 1.0f);
        color = EncounterMoveAnimation.color(960);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep8, owner.tintCombatant(16, 0.25f, 0.75f, 0.0f, color), 1.2f);
        color = EncounterMoveAnimation.color(990);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep9, owner.tintCombatant(16, 0.25f, 0.0f, 0.75f, color), 1.4f);
        color = EncounterMoveAnimation.color(990);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep10, owner.tintCombatant(16, 0.25f, 0.75f, 0.0f, color))
                        .append(owner.restoreCamera(0.25f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
