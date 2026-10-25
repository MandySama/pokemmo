package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 雷击（招式 ID：550）的攻击／事件阶段动画。
 *
 * <p>招式描述：让强大的电流覆盖全身, 猛撞向对手进行攻击. 有时会让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110550（名称）和 120550（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/BoltStrikeAnimation.java
 *
 * <p>源文件 SHA-256：3b52e8fe580648341ef876d27764c9c23aaff93facbaf01a8ee3a5d617f9eb9a
 */
final class ReferenceMoveProgram550 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.loadBattleBackground(20))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .append(owner.focusCamera(0, 0.3f))
                                        .endGroup()
                                        .beginParallel(),
                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.025f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1522,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1522,
                                        2,
                                        14,
                                        750.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1522,
                                        1,
                                        14,
                                        833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1908,
                                        2,
                                        14,
                                        1333.3334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1438,
                                        1,
                                        14,
                                        2166.6667f,
                                        0.9921875f,
                                        owner.casterHandle));
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Color color = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep15 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                animationStep6
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction3))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        owner.sceneFlag(1, false)))
                                                        .append(
                                                                owner.tintCombatant(
                                                                        14, 1.25f, 0.0f, 0.5f,
                                                                        color))
                                                        .onStart(owner.preloadParticleEffect(717))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 717, 2, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 717, 4, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 717, 9, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 717, 7, 9, 8, 0.5f),
                                                1.2f),
                                        owner.emitParticles(-1, 717, 5, 9, 8, 0.5f)),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.096f, 0.39990234f, -0.39990234f),
                                0.2f)
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.6f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1593,
                                        2,
                                        16,
                                        0.0f,
                                        0.5859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1885,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1522, 2, 16, 750.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1748, 3, 16, 0.0f, 0.46875f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(3, 0, 0.0f, 1600.0f))
                        .append(owner.adjustSoundTrack(3, 1, 1280.0f, 160.0f));
        Color color2 = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep16 =
                animationStep15
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        3,
                                        16,
                                        1833.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.tintCombatant(14, 1.25f, 0.5f, 0.0f, color2));
        color2 = EncounterMoveAnimation.color(1023);
        ReferenceMoveTimeline animationStep22 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        animationStep16
                                                .append(
                                                        owner.tintCombatant(
                                                                16, 0.75f, 0.0f, 0.5f, color2))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 717, 8, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 717, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 717, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 717, 6, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 717, 3, 11, 8, 0.5f))
                                                .append(
                                                        owner.scaleCombatant(
                                                                16,
                                                                2,
                                                                18,
                                                                0.0f,
                                                                0.032f,
                                                                0.19995117f,
                                                                -0.19995117f)),
                                        owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f),
                                        1.0f),
                                owner.emitParticles(-1, 717, 5, 11, 8, 0.25f),
                                1.4f),
                        owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f));
        Runnable sceneFlagAction4 = owner.sceneFlag(1, true);
        Color color3 = EncounterMoveAnimation.color(1023);
        result =
                animationStep22
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.075f))
                        .append(owner.tintCombatant(16, 1.25f, 0.5f, 0.0f, color3))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
