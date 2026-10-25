package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 垃圾射击（招式 ID：441）的攻击／事件阶段动画。
 *
 * <p>招式描述：用肮脏的垃圾撞向对手进行攻击. 有时会让对手陷入中毒状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110441（名称）和 120441（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/GunkShotAnimation.java
 *
 * <p>源文件 SHA-256：e8560d62a4d843a5fb572e86e0e99bfcbcab9c99506f6cc5373c4f7c2d312bfb
 */
final class ReferenceMoveProgram441 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep14 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .onStart(owner.loadBattleBackground(5))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction2)),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.875f, 0.025f),
                                                0.12f)
                                        .append(owner.focusCamera(0, 0.4f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4)),
                                owner.scrollBattleBackground(4, -30, 0, 3.2f),
                                0.24f)
                        .append(owner.tintEnvironment(4, 0, 0.875f, 0.0f, 0.025f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        3,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        3,
                                        14,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 960.0f, 320.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1445,
                                        1,
                                        14,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        2,
                                        14,
                                        1333.3334f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 1440.0f, 320.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        1850.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        1933.3334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        2016.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1407,
                                        1,
                                        14,
                                        1333.3334f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        3333.3333f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 2560.0f, 640.0f))
                        .append(owner.adjustSoundTrack(1, 0, 1280.0f, 1280.0f))
                        .onStart(owner.preloadParticleEffect(616))
                        .append(owner.emitParticles(-1, 616, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 616, 1, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(12300);
        ReferenceMoveTimeline animationStep15 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep14,
                                owner.emitParticles(-1, 616, 2, 9, 11, 0.5f),
                                1.84f)
                        .append(owner.focusCamera(1, 0.8f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.24f)
                        .beginParallel()
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color));
        Color color2 = EncounterMoveAnimation.color(12300);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                ReferenceMoveTimeline
                                                        .appendThenDelayedOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedOuterParallel(
                                                                                animationStep15
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                616,
                                                                                                                3,
                                                                                                                11,
                                                                                                                8,
                                                                                                                0.5f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .setSpriteAnimationPaused(
                                                                                                                16,
                                                                                                                1)),
                                                                                owner
                                                                                        .shakeCombatant(
                                                                                                16,
                                                                                                2,
                                                                                                3,
                                                                                                0.032f,
                                                                                                0.016f,
                                                                                                0.30004883f,
                                                                                                0.0f),
                                                                                2.84f),
                                                                owner.tintCombatant(
                                                                        16, 0.5f, 0.75f, 0.0f,
                                                                        color2),
                                                                3.44f),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.875f, 0.025f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                                        .onStart(
                                                ReferenceMoveTimeline.callback(
                                                        owner.sceneFlag(1, true)))
                                        .endGroup()
                                        .beginParallel(),
                                owner.tintEnvironment(4, 0, 0.875f, 0.0f, 0.025f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
