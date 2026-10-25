package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 薄雾球（招式 ID：296）的攻击／事件阶段动画。
 *
 * <p>招式描述：用围绕着雾状羽毛的球进行攻击. 有时会降低对手的特攻.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110296（名称）和 120296（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/MistBallAnimation.java
 *
 * <p>源文件 SHA-256：408e29b1981abdb36465731bef45d59b2074182d59f5942c0b7fcc99e0dd0b88
 */
final class ReferenceMoveProgram296 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .onStart(owner.setHealthHudHidden(18, true))
                        .append(owner.focusCamera(0, 0.3f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        1,
                                        14,
                                        583.3333f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 800.0f, 208.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1133.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .onStart(owner.loadBattleBackground(4));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                animationStep3.onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2)),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.05f))
                                        .append(owner.scrollBattleBackground(4, -20, 0, 2.4f))
                                        .onStart(owner.preloadParticleEffect(462))
                                        .append(owner.launchParticles(462, 0, 9, 11, 0.5f, 360.0f))
                                        .append(owner.focusCamera(1, 0.25f))
                                        .beginSequence()
                                        .waitSeconds(0.4f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        1,
                                                        16,
                                                        83.333336f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1438,
                                                        2,
                                                        16,
                                                        250.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 462, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 462, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 462, 1, 0, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f),
                                1.6f),
                        owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f),
                        2.2f);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        result =
                animationStep8
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.8f)
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
