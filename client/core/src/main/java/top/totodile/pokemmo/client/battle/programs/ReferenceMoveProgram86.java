package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 电磁波（招式 ID：86）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发出微弱的电击, 从而让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110086（名称）和 120086（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ThunderWaveAnimation.java
 *
 * <p>源文件 SHA-256：8a9e702bc78eae77b01dce478ac5486116df2dcc82a158f5463f86a4d356bb28
 */
final class ReferenceMoveProgram86 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, 1 != 0);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(1, 0.4f)),
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.875f, 0.1f))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction3))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1522,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1458,
                                                                2,
                                                                16,
                                                                1000.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(251))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 251, 0, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 251, 2, 11, 8, 0.5f),
                                        0.08f),
                                owner.emitParticles(-1, 251, 0, 11, 8, 0.5f),
                                0.16f)
                        .append(owner.emitParticles(-1, 251, 0, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(1023);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, true);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep7
                                        .append(owner.emitParticles(-1, 251, 1, 11, 8, 0.5f))
                                        .append(owner.tintCombatant(16, 1.0f, 0.0f, 0.75f, color))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        6,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        0.0f)),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.016f, 0.032f, 0.050048828f, -0.050048828f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintEnvironment(4, 0, 0.875f, 0.0f, 0.1f));
        color = EncounterMoveAnimation.color(1023);
        result =
                animationStep8
                        .append(owner.tintCombatant(16, 1.0f, 0.75f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
