package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 精神强念（招式 ID：94）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手发送强大的念力进行攻击. 有时会降低对手的特防.
 *
 * <p>动画说明：镜头聚焦目标；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110094（名称）和 120094（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PsychicAnimation.java
 *
 * <p>源文件 SHA-256：fa056968ae48b67316e779af65e0a7f7a9fb985d12bac0416e74bb58b9425ec5
 */
final class ReferenceMoveProgram94 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, 0 != 0);
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(14, 1)),
                                owner.tintEnvironment(2, 0, 0.0f, 1.0f, 0.025f))
                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.05f))
                        .onStart(owner.loadBattleBackground(13))
                        .append(owner.scrollBattleBackground(4, 0, 22, 1.28f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1450, 2, 16, 0.0f, 0.625f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1461,
                                        1,
                                        16,
                                        666.6667f,
                                        0.859375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                animationStep3.append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1524,
                                                                2,
                                                                16,
                                                                666.6667f,
                                                                0.78125f,
                                                                owner.casterHandle)),
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        3,
                                                        0.032f,
                                                        0.016f,
                                                        0.30004883f,
                                                        0.0f),
                                                0.72f)
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.016f,
                                                        0.064f,
                                                        0.19995117f,
                                                        0.19995117f))
                                        .append(owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color)),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.025f),
                                1.2f)
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
