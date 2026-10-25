package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 高速移动（招式 ID：97）的攻击／事件阶段动画。
 *
 * <p>招式描述：让身体放松变得轻盈, 以便高速移动. 大幅提高自己的速度.
 *
 * <p>动画说明：镜头聚焦施术者；切换战斗背景；滚动战斗背景；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110097（名称）和 120097（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/AgilityAnimation.java
 *
 * <p>源文件 SHA-256：dc35391ef863f035907f0d94659a24d948b94a3692dbfb958ec9c7ec3508a5fe
 */
final class ReferenceMoveProgram97 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        333.33334f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        2,
                                                        14,
                                                        416.66666f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        500.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        2,
                                                        14,
                                                        583.3333f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        666.6667f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        916.6667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        1250.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1445,
                                                        2,
                                                        14,
                                                        750.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        14,
                                                        1500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 480.0f))
                                        .onStart(owner.loadBattleBackground(29)),
                                owner.tintEnvironment(2, 0, 0.0f, 1.0f, 0.025f),
                                0.32f)
                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.025f));
        Runnable sceneFlagAction2 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(1, true);
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep10
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                        .append(owner.scrollBattleBackground(4, 60, 0, 1.28f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.44f)
                                        .beginParallel()
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(1.24f)
                                        .beginParallel(),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f),
                                1.56f)
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
