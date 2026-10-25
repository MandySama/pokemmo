package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 玉石俱碎（招式 ID：120）的攻击／事件阶段动画。
 *
 * <p>招式描述：引发爆炸, 攻击自己周围所有的宝可梦. 使用后陷入濒死.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110120（名称）和 120120（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SelfDestructAnimation.java
 *
 * <p>源文件 SHA-256：e832d9f7f274e9f23677703a608e938a2274cacae8da0f48e97d5355b7aadcdd
 */
final class ReferenceMoveProgram120 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(1, 0 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, 1 != 0);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f)),
                                owner.tintEnvironment(2, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(owner.loadBattleBackground(14))
                        .append(owner.scrollBattleBackground(2, 0, 8, 0.016f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        14,
                                        166.66667f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        14,
                                        333.33334f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        14,
                                        500.0f,
                                        0.7421875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        14,
                                        666.6667f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        14,
                                        833.3333f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        14,
                                        1000.0f,
                                        0.7421875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        14,
                                        1166.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        14,
                                        1166.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(285))
                        .append(owner.emitParticles(-1, 285, 1, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 285, 2, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(12);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep11
                                        .append(owner.emitParticles(-1, 285, 3, 9, 8, 0.5f))
                                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color)),
                                owner.shakeCombatant(14, 2, 12, 0.0f, 0.032f, 0.5f, 0.0f),
                                0.48f)
                        .append(owner.shakeCombatant(16, 2, 3, 0.0f, 0.032f, 0.5f, 0.0f));
        color = EncounterMoveAnimation.color(12);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep12,
                                        owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color)),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
