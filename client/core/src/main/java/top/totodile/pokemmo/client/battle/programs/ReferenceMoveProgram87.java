package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 打雷（招式 ID：87）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手劈下暴雷进行攻击. 有时会让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110087（名称）和 120087（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ThunderAnimation.java
 *
 * <p>源文件 SHA-256：7cef19892408005f9ada23b8f3fc02533030ead43bbf2418ae5979d7998d6ee2
 */
final class ReferenceMoveProgram87 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1683,
                                        1,
                                        2,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f))
                        .append(owner.focusCamera(1, 0.65f))
                        .beginSequence()
                        .waitSeconds(0.52f)
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.92f)
                        .beginParallel()
                        .append(owner.tintEnvironment(3, 0, 0.875f, 0.0f, 0.05f))
                        .onStart(owner.loadBattleBackground(11));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, 0 != 0);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep2
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4)),
                                owner.scrollBattleBackground(4, 1, 0, 0.8f),
                                1.52f)
                        .append(owner.tintEnvironment(3, 0, 0.0f, 0.875f, 0.075f))
                        .append(owner.focusCamera(1, 0.2f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1710,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1710,
                                        2,
                                        16,
                                        250.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        250.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1514,
                                        2,
                                        16,
                                        416.66666f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(252))
                        .onStart(owner.preloadParticleEffect(253))
                        .onStart(owner.preloadParticleEffect(254))
                        .append(owner.emitParticles(-1, 254, 0, 11, 8, 0.75f))
                        .append(owner.emitParticles(-1, 252, 0, 11, 8, 0.0f))
                        .append(owner.emitParticles(-1, 252, 1, 11, 8, 0.75f))
                        .append(owner.emitParticles(-1, 253, 0, 11, 8, 0.0f));
        Color color = EncounterMoveAnimation.color(1023);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                animationStep10
                        .append(owner.emitParticles(-1, 253, 1, 11, 8, 0.0f))
                        .append(owner.tintCombatant(16, 0.75f, 0.875f, 0.0f, color))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, 0.30004883f, -0.30004883f))
                        .append(owner.shakeCombatant(16, 2, 8, 0.032f, 0.016f, 0.5f, 0.0f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.52f)
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.restoreCamera(0.8f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
