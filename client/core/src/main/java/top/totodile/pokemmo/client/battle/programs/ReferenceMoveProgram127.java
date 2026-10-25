package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 攀瀑（招式 ID：127）的攻击／事件阶段动画。
 *
 * <p>招式描述：以惊人的气势扑向对手. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110127（名称）和 120127（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/WaterfallAnimation.java
 *
 * <p>源文件 SHA-256：ebf62f6c1fd0063c261f30fb588ba055333554e48b56bf8f7697761a7f6b5e16
 */
final class ReferenceMoveProgram127 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, 1 != 0);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.loadBattleBackground(16))
                                        .append(owner.tintEnvironment(2, 0, 0.0f, 1.0f, 0.05f))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .beginSequence()
                                        .waitSeconds(0.48f)
                                        .beginParallel()
                                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.05f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(owner.scrollBattleBackground(4, 0, 20, 2.24f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1407,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 640.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 0.0f, 640.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1497,
                                                        0,
                                                        14,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.preloadParticleEffect(292))
                                        .append(owner.emitParticles(-1, 292, 1, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 292, 0, 11, 8, 0.0f)),
                                owner.emitParticles(-1, 292, 2, 11, 8, 0.125f),
                                1.08f)
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.focusCamera(1, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1407, 2, 16, 0.0f, 0.78125f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        2,
                                        16,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1475,
                                        0,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep10
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1376,
                                                0,
                                                16,
                                                1333.3334f,
                                                0.0f,
                                                owner.casterHandle))
                                .append(owner.adjustSoundTrack(2, 1, 960.0f, 320.0f))
                                .append(owner.adjustSoundTrack(0, 1, 960.0f, 320.0f))
                                .append(owner.shakeCombatant(16, 2, 6, 0.032f, 0.016f, 0.5f, 0.0f)),
                        owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color),
                        1.68f);
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(1, true);
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        animationStep11,
                                        owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color),
                                        2.28f),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f),
                                2.6f)
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
