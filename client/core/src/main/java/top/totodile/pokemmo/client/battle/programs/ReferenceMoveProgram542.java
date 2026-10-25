package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 暴风（招式 ID：542）的攻击／事件阶段动画。
 *
 * <p>招式描述：用强烈的风席卷对手进行攻击. 有时会使对手混乱.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110542（名称）和 120542（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/HurricaneAnimation.java
 *
 * <p>源文件 SHA-256：37fc50ab8d489f4f34ba56cb2445dfa131d11ce55952ffb4216ec366969794d2
 */
final class ReferenceMoveProgram542 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.loadBattleBackground(30))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2)),
                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.focusCamera(1, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1920,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        16,
                                        4166.6665f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 2400.0f, 640.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1497, 2, 16, 0.0f, 0.0f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 0.0f, 800.0f))
                        .append(owner.adjustSoundTrack(2, 1, 1600.0f, 1600.0f));
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                        animationStep4
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction3))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction4))
                                                .append(
                                                        owner.tintEnvironment(
                                                                3, 0, 1.0f, 0.0f, 0.05f))
                                                .append(
                                                        owner.scrollBattleBackground(
                                                                4, 24, 0, 3.68f))
                                                .onStart(owner.setHealthHudHidden(14, true))
                                                .onStart(owner.setHealthHudHidden(16, true))
                                                .onStart(owner.preloadParticleEffect(705))
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .append(
                                                        owner.shakeCombatant(
                                                                16, 2, 13, 0.0f, 0.096f, 2.0f,
                                                                0.0f)),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                6,
                                                0.032f,
                                                0.064f,
                                                -0.30004883f,
                                                0.30004883f)),
                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.05f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
