package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 熔岩风暴（招式 ID：463）的攻击／事件阶段动画。
 *
 * <p>招式描述：将对手困在熊熊燃烧的火焰中, 在4-5回合内进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110463（名称）和 120463（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/MagmaStormAnimation.java
 *
 * <p>源文件 SHA-256：f2d5ef3d93bdbdcc64b78bd99f9a5b7fd4bde4a6826571cd3042d0350e47df8e
 */
final class ReferenceMoveProgram463 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .onStart(owner.setHealthHudHidden(18, true))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1976,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.loadBattleBackground(38));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep2.onStart(
                                        ReferenceMoveTimeline.callback(sceneFlagAction2)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.05f))
                        .append(owner.focusCamera(1, 0.4f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.05f))
                        .append(owner.scrollBattleBackground(4, 0, 24, 2.88f))
                        .onStart(owner.setHealthHudHidden(14, true))
                        .onStart(owner.setHealthHudHidden(16, true))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1425,
                                        1,
                                        16,
                                        333.33334f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        16,
                                        2666.6667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 2240.0f))
                        .append(owner.adjustSoundTrack(1, 1, 1920.0f, 640.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1475,
                                        2,
                                        16,
                                        666.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1475,
                                        2,
                                        16,
                                        833.3333f,
                                        0.9765625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1475,
                                        2,
                                        16,
                                        1000.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1475,
                                        2,
                                        16,
                                        1166.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1475,
                                        2,
                                        16,
                                        1333.3334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1475,
                                        2,
                                        16,
                                        1500.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1475,
                                        2,
                                        16,
                                        1666.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1475,
                                        2,
                                        16,
                                        1833.3334f,
                                        0.46875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep12
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1475,
                                                        2,
                                                        16,
                                                        2000.0f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(639))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.032f, 0.064f, -0.30004883f, 0.30004883f),
                                0.64f)
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.625f, color))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.96f)
                        .beginParallel();
        color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep14 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep13, owner.tintCombatant(16, 0.5f, 0.625f, 0.0f, color), 1.28f);
        color = EncounterMoveAnimation.color(31);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        ReferenceMoveTimeline animationStep15 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep14,
                                        owner.tintCombatant(16, 0.5f, 0.0f, 0.625f, color)),
                                owner.tintEnvironment(3, 0, 0.0f, 0.9375f, 0.05f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintEnvironment(2, 0, 0.9375f, 0.0f, 0.05f));
        color = EncounterMoveAnimation.color(31);
        result =
                animationStep15
                        .append(owner.tintCombatant(16, 0.5f, 0.625f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .append(owner.restoreCamera(0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 1, 16, 0.0f, 0.0f, owner.casterHandle))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
