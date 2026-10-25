package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 雷霆之击!（自定义动画注册 ID：1024）的攻击／事件阶段动画。
 *
 * <p>招式描述：爆炸!
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；切换战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111024（名称）和 121024（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1024Animation.java
 *
 * <p>源文件 SHA-256：f94e6c1d8118464f0970eedded83815e12082a6bd7ecadbb83b85e1d11b9f297
 */
final class ReferenceMoveProgram1024 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep23 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(0, 0.4f))
                                                .onStart(owner.loadBattleBackground(0))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2))
                                                .endGroup()
                                                .beginParallel()
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1497,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1376,
                                                                1,
                                                                14,
                                                                1166.6666f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 960.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1475,
                                                                1,
                                                                14,
                                                                1333.3334f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1418,
                                                                2,
                                                                14,
                                                                1083.3334f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1376,
                                                                2,
                                                                14,
                                                                3000.0f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                3, 1, 2240.0f, 640.0f))
                                                .onStart(owner.preloadParticleEffect(317))
                                                .append(owner.emitParticles(-1, 317, 5, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 317, 1, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 317, 2, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 317, 3, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 317, 6, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 317, 0, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 317, 4, 9, 8, 0.5f))
                                                .onStart(owner.preloadParticleEffect(524))
                                                .append(owner.emitParticles(-1, 524, 2, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 524, 3, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 524, 4, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 524, 0, 9, 8, 0.5f))
                                                .append(owner.emitParticles(-1, 524, 1, 9, 8, 0.5f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1497,
                                                                3,
                                                                14,
                                                                666.6667f,
                                                                0.78125f,
                                                                owner.casterHandle))
                                                .append(owner.adjustSoundTrack(1, 0, 0.0f, 1280.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1458,
                                                                4,
                                                                14,
                                                                1166.6666f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1458,
                                                                5,
                                                                14,
                                                                1500.0f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1683,
                                                                3,
                                                                14,
                                                                2166.6667f,
                                                                0.8984375f,
                                                                owner.casterHandle)),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1682,
                                                4,
                                                14,
                                                2166.6667f,
                                                0.8984375f,
                                                owner.casterHandle),
                                        1.4f),
                                owner.tintEnvironment(2, Short.MAX_VALUE, 0.0f, 0.8125f, 0.075f),
                                1.48f)
                        .onStart(owner.setHealthHudHidden(14, true))
                        .onStart(owner.setHealthHudHidden(16, true));
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Color color = EncounterMoveAnimation.color(31);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, true);
        ReferenceMoveTimeline animationStep24 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep23
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(
                                                ReferenceMoveTimeline.callback(
                                                        owner.sceneFlag(1, false)))
                                        .append(owner.tintCombatant(14, 0.75f, 0.0f, 0.625f, color))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.032f, 0.16f, 0.30004883f, -0.30004883f),
                                2.88f)
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintEnvironment(2, Short.MAX_VALUE, 0.8125f, 0.0f, 0.1f))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel();
        color = EncounterMoveAnimation.color(31);
        result =
                animationStep24
                        .append(owner.tintCombatant(14, 0.75f, 0.625f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
