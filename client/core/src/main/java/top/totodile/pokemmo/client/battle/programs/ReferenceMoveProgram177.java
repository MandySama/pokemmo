package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 气旋攻击（招式 ID：177）的攻击／事件阶段动画。
 *
 * <p>招式描述：发射空气旋涡进行攻击. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110177（名称）和 120177（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/AeroblastAnimation.java
 *
 * <p>源文件 SHA-256：7b0a1b2e0edbd77067a3ae5f33f4a28aa54d4a74086c76e64a00c880430837a3
 */
final class ReferenceMoveProgram177 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(1, 0 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, 1 != 0);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f)),
                                                owner.tintEnvironment(2, 0, 0.0f, 1.0f, 0.025f))
                                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.025f))
                                        .onStart(owner.loadBattleBackground(18))
                                        .append(owner.scrollBattleBackground(2, 8, 8, 0.016f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1445,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        916.6667f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 800.0f))
                                        .onStart(owner.preloadParticleEffect(344))
                                        .append(owner.emitParticles(-1, 344, 1, 9, 8, 0.5f)),
                                owner.emitParticles(-1, 344, 2, 9, 11, 0.5f),
                                1.0f)
                        .append(owner.focusCamera(1, 0.8f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1407,
                                        2,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        2,
                                        14,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1425,
                                        1,
                                        14,
                                        0.0f,
                                        0.703125f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep8
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1280.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 960.0f, 320.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 320.0f)),
                                owner.adjustSoundTrack(2, 0, 0.0f, 1280.0f),
                                1.4f)
                        .append(owner.tintCombatant(16, 0.25f, 0.0f, 0.75f, color))
                        .append(owner.emitParticles(-1, 344, 0, 11, 8, 0.5f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        0,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        0,
                                        16,
                                        166.66667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1420,
                                        0,
                                        16,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                animationStep13
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1420,
                                                                        0,
                                                                        16,
                                                                        500.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        8,
                                                                        0.016f,
                                                                        0.032f,
                                                                        0.30004883f,
                                                                        -0.19995117f)),
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        8,
                                                        0.032f,
                                                        0.016f,
                                                        0.30004883f,
                                                        0.0f),
                                                2.0f),
                                        owner.tintCombatant(16, 0.25f, 0.75f, 0.0f, color2),
                                        2.72f),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
