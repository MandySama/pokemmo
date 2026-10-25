package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 水之波动（招式 ID：352）的攻击／事件阶段动画。
 *
 * <p>招式描述：用水的震动攻击对手. 有时会使对手混乱.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110352（名称）和 120352（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/WaterPulseAnimation.java
 *
 * <p>源文件 SHA-256：7b5547128685fbc0fa55bc0aa73c793897227e06d0df9b01dd21ca1af353d363
 */
final class ReferenceMoveProgram352 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .onStart(owner.loadBattleBackground(28))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.075f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .endGroup()
                        .beginParallel()
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.075f))
                        .append(owner.scrollBattleBackground(2, 16, 0, 0.384f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
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
                        .append(owner.adjustSoundTrack(1, 1, 960.0f, 480.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1461,
                                        2,
                                        16,
                                        666.6667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1452,
                                        2,
                                        16,
                                        1333.3334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(525))
                        .append(owner.emitParticles(-1, 525, 0, 0, 0, 0.0f));
        Color color = EncounterMoveAnimation.color(32389);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        animationStep6,
                                                        owner.emitParticles(-1, 525, 1, 0, 0, 0.0f),
                                                        0.6f)
                                                .append(
                                                        owner.launchParticles(
                                                                525, 2, 9, 11, 0.5f, 480.0f))
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .endGroup()
                                                .endGroup()
                                                .beginSequence()
                                                .waitSeconds(1.2f)
                                                .beginParallel()
                                                .append(owner.focusCamera(1, 0.6f))
                                                .append(
                                                        owner.shakeCombatant(
                                                                16,
                                                                2,
                                                                4,
                                                                0.016f,
                                                                0.048f,
                                                                0.19995117f,
                                                                0.0f)),
                                        owner.tintCombatant(16, 1.0f, 0.0f, 0.625f, color)),
                                owner.tintEnvironment(3, 0, 0.0f, 0.8125f, 0.075f))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)));
        color = EncounterMoveAnimation.color(32389);
        result =
                animationStep7
                        .append(owner.tintCombatant(16, 1.0f, 0.625f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.075f))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
