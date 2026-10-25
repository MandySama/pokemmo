package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 烈火深渊（招式 ID：517）的攻击／事件阶段动画。
 *
 * <p>招式描述：用烈焰包裹住对手进行攻击. 让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110517（名称）和 120517（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/InfernoAnimation.java
 *
 * <p>源文件 SHA-256：9793b1c08aff5b50abee52480fbaf7716a6b14bf5f713ded22459f397fd3bb86
 */
final class ReferenceMoveProgram517 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .onStart(owner.loadBattleBackground(38))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2)),
                                        owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.05f))
                                .append(owner.focusCamera(1, 0.4f))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                                .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.05f))
                                .append(owner.scrollBattleBackground(4, 0, 24, 2.88f))
                                .onStart(owner.setHealthHudHidden(18, true))
                                .onStart(owner.preloadParticleEffect(682))
                                .append(owner.emitParticles(-1, 682, 0, 11, 8, 0.0f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1536,
                                                1,
                                                16,
                                                0.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1536,
                                                2,
                                                16,
                                                416.66666f,
                                                0.78125f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1536,
                                                1,
                                                16,
                                                833.3333f,
                                                0.703125f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1536,
                                                2,
                                                16,
                                                1250.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1536,
                                                1,
                                                16,
                                                1666.6666f,
                                                0.78125f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1718,
                                                2,
                                                16,
                                                2083.3333f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                6,
                                                0.032f,
                                                0.064f,
                                                -0.30004883f,
                                                0.30004883f)),
                        owner.emitParticles(-1, 682, 0, 11, 8, 0.0f),
                        0.32f);
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep9.append(owner.emitParticles(-1, 682, 0, 11, 8, 0.0f)),
                        owner.tintCombatant(16, 0.5f, 0.0f, 0.625f, color),
                        0.64f);
        Color color2 = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep10.append(owner.emitParticles(-1, 682, 0, 11, 8, 0.0f)),
                        owner.tintCombatant(16, 0.5f, 0.625f, 0.0f, color2),
                        0.96f);
        color2 = EncounterMoveAnimation.color(28);
        ReferenceMoveTimeline animationStep13 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep11.append(
                                                owner.tintCombatant(
                                                        16, 0.5f, 0.0f, 0.625f, color2)),
                                        owner.emitParticles(-1, 682, false ? 1 : 0, 11, 8, 0.0f))
                                .append(owner.emitParticles(-1, 682, true ? 1 : 0, 11, 8, 0.5f)),
                        owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        Color color3 = EncounterMoveAnimation.color(31);
        result =
                animationStep13
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.05f))
                        .append(owner.tintCombatant(16, 0.5f, 0.625f, 0.0f, color3))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
