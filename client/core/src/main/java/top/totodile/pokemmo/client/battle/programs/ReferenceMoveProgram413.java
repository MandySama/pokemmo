package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 勇鸟猛攻（招式 ID：413）的攻击／事件阶段动画。
 *
 * <p>招式描述：收拢翅膀, 通过低空飞行突击对手. 自己也会受到不小的伤害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵显隐；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110413（名称）和 120413（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/BraveBirdAnimation.java
 *
 * <p>源文件 SHA-256：c6889b794b97b2d2a635f7074e52d3b8034eef5b327a6ac3926f4efa98202da2
 */
final class ReferenceMoveProgram413 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.25f))
                        .onStart(owner.loadBattleBackground(30))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1736,
                                        2,
                                        14,
                                        0.0f,
                                        0.234375f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 1280.0f, 160.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1500.0f,
                                        0.0f,
                                        owner.casterHandle));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep15 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep3.onStart(
                                        ReferenceMoveTimeline.callback(sceneFlagAction2)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.025f))
                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.05f))
                        .append(owner.scrollBattleBackground(4, 36, 0, 1.92f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(owner.setHealthHudHidden(14, true))
                        .onStart(owner.setHealthHudHidden(16, true))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1849,
                                        1,
                                        16,
                                        0.0f,
                                        0.546875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1819,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        1083.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1424,
                                        1,
                                        16,
                                        500.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1424,
                                        1,
                                        16,
                                        750.0f,
                                        0.234375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(588))
                        .append(owner.setCombatantVisibility(14, 3))
                        .append(owner.emitParticles(-1, 588, 1, 9, 11, 0.5f))
                        .append(owner.emitParticles(-1, 588, 2, 9, 11, 0.5f))
                        .append(owner.emitParticles(-1, 588, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 588, 3, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 588, 4, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 588, 5, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 588, 7, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(30);
        ReferenceMoveTimeline animationStep16 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep15, owner.emitParticles(-1, 588, 8, 9, 8, 0.5f), 0.12f)
                        .append(owner.focusCamera(1, 0.25f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color))
                        .append(owner.shakeCombatant(16, 2, 8, 0.016f, 0.032f, 0.30004883f, 0.0f));
        Color color2 = EncounterMoveAnimation.color(30);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep16,
                                        owner.emitParticles(-1, 588, 6, 11, 8, 0.5f)),
                                owner.tintEnvironment(3, 0, 0.0f, 0.8125f, 0.05f))
                        .append(owner.setCombatantVisibility(14, 4))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color2))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
