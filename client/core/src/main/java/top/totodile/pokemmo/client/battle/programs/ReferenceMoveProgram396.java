package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 波导弹（招式 ID：396）的攻击／事件阶段动画。
 *
 * <p>招式描述：从体内产生出波导之力, 然后向对手发出. 攻击必定会命中.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110396（名称）和 120396（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/AuraSphereAnimation.java
 *
 * <p>源文件 SHA-256：4a62414e0319d4538f91f1ef4d4571cf8b00820e5902eff152b64c701e8c3b03
 */
final class ReferenceMoveProgram396 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .onStart(
                                                                                owner
                                                                                        .loadBattleBackground(
                                                                                                7))
                                                                        .onStart(
                                                                                ReferenceMoveTimeline
                                                                                        .callback(
                                                                                                sceneFlagAction2)),
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.0f, 0.9375f,
                                                                        0.075f))
                                                        .append(
                                                                owner.scrollBattleBackground(
                                                                        4, -22, 0, 2.4f))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction3))
                                                        .onStart(
                                                                ReferenceMoveTimeline.callback(
                                                                        sceneFlagAction4))
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.9375f, 0.0f,
                                                                        0.075f))
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
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        3, 0, 0.0f, 1280.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        3, 1, 960.0f, 320.0f))
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1785,
                                                                        1,
                                                                        14,
                                                                        1333.3334f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1686,
                                                                        2,
                                                                        16,
                                                                        1666.6666f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 1280.0f, 480.0f))
                                                        .onStart(owner.preloadParticleEffect(571))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 571, 0, 9, 8, 0.5f)),
                                                owner.emitParticles(-1, 571, 4, 9, 8, 0.5f),
                                                1.2f),
                                        owner.emitParticles(-1, 571, 3, 9, 8, 0.5f),
                                        1.4f)
                                .append(owner.launchParticles(571, 1, 9, 11, 0.5f, 0.0f))
                                .append(owner.launchParticles(571, 2, 9, 11, 0.5f, 0.0f))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(owner.restoreCamera(0.25f))
                                .endGroup()
                                .endGroup()
                                .beginSequence()
                                .waitSeconds(1.64f)
                                .beginParallel()
                                .append(
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                1,
                                                0.016f,
                                                0.032f,
                                                -0.30004883f,
                                                0.30004883f))
                                .append(
                                        owner.shakeCombatant(
                                                16, 2, 4, 0.016f, 0.032f, 0.30004883f, 0.0f)),
                        owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.075f));
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep8
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                                .endGroup()
                                .beginParallel()
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.075f));
        return result;
    }
}
