package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 击落（招式 ID：479）的攻击／事件阶段动画。
 *
 * <p>招式描述：扔石头或炮弹, 攻击飞行的对手. 对手会被击落, 掉到地面.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110479（名称）和 120479（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SmackDownAnimation.java
 *
 * <p>源文件 SHA-256：e0951453505588f8a13dcc2b3ba4a0ebc3182a9ed9a08c7dc479ed334b9f4d83
 */
final class ReferenceMoveProgram479 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(owner.focusCamera(1, 0.6f))
                                        .onStart(owner.preloadParticleEffect(650))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1459,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.emitParticles(-1, 650, 2, 9, 11, 0.5f),
                                0.2f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        2,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1518,
                                        1,
                                        16,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .onStart(owner.loadBattleBackground(10));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep5
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .append(owner.emitParticles(-1, 650, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 650, 4, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1895,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        666.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.096f,
                                                        0.100097656f,
                                                        -0.100097656f)),
                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.025f),
                                0.24f)
                        .onStart(owner.setHealthHudHidden(16, true));
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        animationStep9
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction3))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction4))
                                                .endGroup()
                                                .endGroup()
                                                .endGroup()
                                                .beginParallel()
                                                .append(
                                                        owner.tintEnvironment(
                                                                3, 0, 1.0f, 0.0f, 0.025f)),
                                        owner.scrollBattleBackground(4, 0, 30, 0.48f),
                                        0.4f)
                                .append(
                                        owner.scaleCombatant(
                                                16, 2, 1, 0.0f, 0.096f, 0.8000488f, -0.8000488f))
                                .append(owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.025f))
                                .append(owner.emitParticles(-1, 650, 0, 11, 8, 0.125f)),
                        owner.emitParticles(-1, 650, 1, 11, 8, 0.0f),
                        0.44f);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep11
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.64f)
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
