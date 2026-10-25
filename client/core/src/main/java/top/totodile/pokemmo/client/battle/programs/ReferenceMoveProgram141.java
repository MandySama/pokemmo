package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 汲取（招式 ID：141）的攻击／事件阶段动画。
 *
 * <p>招式描述：吸取血液攻击对手. 可以回复给予对手伤害的一半HP.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110141（名称）和 120141（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/LeechLifeAnimation.java
 *
 * <p>源文件 SHA-256：ef9bd0db4fdf0efa94f5d2dd1d60278aab0da4945177034a905479c56183b619
 */
final class ReferenceMoveProgram141 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.4f))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1466,
                                                                1,
                                                                14,
                                                                166.66667f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                2,
                                                                16,
                                                                333.33334f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(306))
                                                .append(
                                                        owner.launchParticles(
                                                                306, 3, 9, 11, 0.5f, 240.0f)),
                                        owner.emitParticles(-1, 306, 1, 11, 8, 0.5f),
                                        0.2f),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f),
                                0.4f)
                        .append(owner.focusCamera(0, 0.8f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1504,
                                        2,
                                        16,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        1,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 320.0f, 480.0f))
                        .append(owner.adjustSoundTrack(1, 1, 1120.0f, 480.0f))
                        .append(owner.emitParticles(-1, 306, 0, 11, 9, 0.5f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep8, owner.emitParticles(-1, 306, 2, 9, 8, 0.5f), 1.2f),
                        owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color),
                        1.84f);
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep9, owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
