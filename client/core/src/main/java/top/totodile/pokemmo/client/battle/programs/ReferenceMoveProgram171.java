package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 恶梦（招式 ID：171）的攻击／事件阶段动画。
 *
 * <p>招式描述：让在睡眠状态下的对手做恶梦, 每回合会缓缓减少HP.
 *
 * <p>动画说明：切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110171（名称）和 120171（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/NightmareAnimation.java
 *
 * <p>源文件 SHA-256：bae63847059d591eef6055e2845459b4d713b78b3d198c6ce2b32805afb72428
 */
final class ReferenceMoveProgram171 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1783,
                                                        0,
                                                        2,
                                                        0.0f,
                                                        0.46875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        0,
                                                        2,
                                                        3666.6667f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(4, 1, 3200.0f, 320.0f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.tintEnvironment(2, 0, 0.0f, 1.0f, 0.05f))
                        .onStart(owner.loadBattleBackground(17))
                        .append(owner.scrollBattleBackground(4, 0, -1, 1.28f));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, false);
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        animationStep3
                                                                .onStart(
                                                                        ReferenceMoveTimeline
                                                                                .callback(
                                                                                        sceneFlagAction2))
                                                                .onStart(
                                                                        ReferenceMoveTimeline
                                                                                .callback(
                                                                                        sceneFlagAction3))
                                                                .onStart(
                                                                        ReferenceMoveTimeline
                                                                                .callback(
                                                                                        owner
                                                                                                .sceneFlag(
                                                                                                        0,
                                                                                                        false)))
                                                                .onStart(
                                                                        owner.setHealthHudHidden(
                                                                                16, true))
                                                                .append(
                                                                        owner.tintCombatant(
                                                                                16, 0.5f, 0.0f,
                                                                                0.5f, color))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1535,
                                                                                1,
                                                                                16,
                                                                                0.0f,
                                                                                0.8984375f,
                                                                                owner.casterHandle)),
                                                        owner.scaleCombatant(
                                                                16,
                                                                2,
                                                                1,
                                                                0.016f,
                                                                0.128f,
                                                                0.0f,
                                                                0.30004883f),
                                                        0.6f),
                                                owner.tintCombatant(16, 0.5f, 0.5f, 0.0f, color2))
                                        .onStart(owner.setHealthHudHidden(16, false))
                                        .append(owner.scrollBattleBackground(4, 0, -1, 1.28f))
                                        .append(
                                                owner.shakeCombatant(
                                                        16, 2, 4, 0.032f, 0.032f, 0.5f, 0.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1779,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1779,
                                        2,
                                        16,
                                        333.33334f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.05f));
        Runnable sceneFlagAction4 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                animationStep6
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
