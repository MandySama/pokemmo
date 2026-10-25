package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 宇宙力量（招式 ID：322）的攻击／事件阶段动画。
 *
 * <p>招式描述：汲取宇宙中神秘的力量, 从而提高自己的防御和特防.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110322（名称）和 120322（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/CosmicPowerAnimation.java
 *
 * <p>源文件 SHA-256：b16b6151012308f32c5b3d0313e3a1f0b82ea188579a38191e8104833b222df8
 */
final class ReferenceMoveProgram322 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(0);
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.8125f, color))
                        .onStart(owner.loadBattleBackground(25))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.075f))
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.075f))
                        .onStart(owner.setHealthHudHidden(14, true))
                        .onStart(owner.setHealthHudHidden(16, true))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1483,
                                        1,
                                        14,
                                        0.0f,
                                        0.15625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1872,
                                        2,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.scrollBattleBackground(4, 0, 1, 1.6f))
                        .onStart(owner.preloadParticleEffect(492))
                        .append(owner.emitParticles(-1, 492, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 492, 1, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 492, 2, 9, 8, 0.25f));
        Color color2 = EncounterMoveAnimation.color(0);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep6
                                        .append(
                                                owner.emitParticles(
                                                        -1, 492, 3, 9, 8, -0.049987793f))
                                        .append(owner.tintEnvironment(3, 0, 0.0f, 0.8125f, 0.05f)),
                                owner.tintCombatant(16, 0.75f, 0.8125f, 0.0f, color2),
                                1.5f)
                        .onStart(owner.setHealthHudHidden(14, false))
                        .onStart(owner.setHealthHudHidden(16, false))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.05f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
