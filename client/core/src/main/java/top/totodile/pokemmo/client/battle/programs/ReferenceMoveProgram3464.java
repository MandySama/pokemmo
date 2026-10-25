package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 暗黑洞☆（自定义动画注册 ID：3464）的攻击／事件阶段动画。
 *
 * <p>招式描述：将对手强制拖入黑暗的世界, 从而让对手陷入睡眠状态.
 *
 * <p>动画说明：镜头聚焦场地位置；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；控制精灵显隐；恢复默认镜头。
 *
 * <p>变体映射：基础招式 ID 为 464；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110464（名称）和 120464（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove3464Animation.java
 *
 * <p>源文件 SHA-256：1359ef746d94abd5014f73056c72227db4bf6122e17a48ab0432d6cfcbdf9543
 */
final class ReferenceMoveProgram3464 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.loadBattleBackground(39))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.05f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.05f))
                        .append(owner.scrollBattleBackground(4, 0, -1, 3.84f))
                        .append(owner.focusCamera(4, 0.4f))
                        .onStart(owner.playCustomParticles("3464"))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1536, 1, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1535,
                                        2,
                                        16,
                                        833.3333f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1630,
                                        1,
                                        16,
                                        1666.6666f,
                                        0.9375f,
                                        owner.casterHandle))
                        .endGroup()
                        .waitSeconds(1.4f)
                        .beginParallel();
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep4
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.05f))
                        .append(owner.setCombatantVisibility(16, 4))
                        .endGroup()
                        .append(owner.restoreCamera(0.4f));
        return result;
    }
}
