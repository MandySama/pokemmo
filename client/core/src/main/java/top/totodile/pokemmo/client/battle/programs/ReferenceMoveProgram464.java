package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 暗黑洞（招式 ID：464）的攻击／事件阶段动画。
 *
 * <p>招式描述：将对手强制拖入黑暗的世界, 从而让对手陷入睡眠状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110464（名称）和 120464（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/DarkVoidAnimation.java
 *
 * <p>源文件 SHA-256：b7e6774e82aed2a601a0e503ca9ca3ccc3e402f58db86cafa6c3782262614a9b
 */
final class ReferenceMoveProgram464 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .appendThenNextParallel(
                                                                                                ReferenceMoveTimeline
                                                                                                        .sequence()
                                                                                                        .beginParallel()
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .setHealthHudHidden(
                                                                                                                                18,
                                                                                                                                true))
                                                                                                        .append(
                                                                                                                owner
                                                                                                                        .focusCamera(
                                                                                                                                1,
                                                                                                                                0.4f))
                                                                                                        .endGroup()
                                                                                                        .beginParallel()
                                                                                                        .onStart(
                                                                                                                owner
                                                                                                                        .loadBattleBackground(
                                                                                                                                39))
                                                                                                        .onStart(
                                                                                                                ReferenceMoveTimeline
                                                                                                                        .callback(
                                                                                                                                sceneFlagAction2)),
                                                                                                owner
                                                                                                        .tintEnvironment(
                                                                                                                4,
                                                                                                                0,
                                                                                                                0.0f,
                                                                                                                0.8125f,
                                                                                                                0.05f))
                                                                                        .onStart(
                                                                                                ReferenceMoveTimeline
                                                                                                        .callback(
                                                                                                                sceneFlagAction3))
                                                                                        .onStart(
                                                                                                ReferenceMoveTimeline
                                                                                                        .callback(
                                                                                                                sceneFlagAction4))
                                                                                        .append(
                                                                                                owner
                                                                                                        .tintEnvironment(
                                                                                                                3,
                                                                                                                0,
                                                                                                                0.8125f,
                                                                                                                0.0f,
                                                                                                                0.05f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scrollBattleBackground(
                                                                                                                4,
                                                                                                                0,
                                                                                                                -1,
                                                                                                                3.84f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1536,
                                                                                                                1,
                                                                                                                16,
                                                                                                                0.0f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1535,
                                                                                                                2,
                                                                                                                16,
                                                                                                                833.3333f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1630,
                                                                                                                1,
                                                                                                                16,
                                                                                                                1666.6666f,
                                                                                                                0.9375f,
                                                                                                                owner.casterHandle))
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                640))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                640,
                                                                                                                1,
                                                                                                                11,
                                                                                                                8,
                                                                                                                0.07501221f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                640,
                                                                                                                2,
                                                                                                                11,
                                                                                                                8,
                                                                                                                0.049987793f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                640,
                                                                                                                0,
                                                                                                                11,
                                                                                                                8,
                                                                                                                0.049987793f)),
                                                                                owner.emitParticles(
                                                                                        -1,
                                                                                        640,
                                                                                        4,
                                                                                        11,
                                                                                        8,
                                                                                        0.049987793f),
                                                                                0.2f),
                                                                owner.shakeCombatant(
                                                                        16,
                                                                        2,
                                                                        1,
                                                                        0.0f,
                                                                        0.032f,
                                                                        0.0f,
                                                                        -0.30004883f))
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        16, 2, 1, 0.0f, 0.16f, 0.0f,
                                                                        -1.0f)),
                                                owner.emitParticles(-1, 640, 3, 11, 8, 0.25f)),
                                        owner.shakeCombatant(16, 2, 1, 0.0f, 0.96f, 0.0f, -12.0f),
                                        0.02f)
                                .append(owner.setCombatantVisibility(16, 3))
                                .endGroup()
                                .endGroup()
                                .endGroup()
                                .beginParallel(),
                        owner.tintEnvironment(4, 0, 0.0f, 0.9375f, 0.05f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep9
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.9375f, 0.0f, 0.05f))
                        .append(owner.setCombatantVisibility(16, 4))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
