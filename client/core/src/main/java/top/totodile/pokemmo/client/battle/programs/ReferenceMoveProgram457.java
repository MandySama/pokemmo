package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 双刃头锤（招式 ID：457）的攻击／事件阶段动画。
 *
 * <p>招式描述：拼命使出浑身力气, 向对手进行头锤攻击. 自己也会受到非常大的伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110457（名称）和 120457（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/HeadSmashAnimation.java
 *
 * <p>源文件 SHA-256：3d45560d089d3b7d6232d58b85cbdffee1a2a588d3e852024a97cf0da71de5d6
 */
final class ReferenceMoveProgram457 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.shakeCombatant(14, 2, 1, 0.032f, 0.032f, 1.0f, 0.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1421,
                                        1,
                                        14,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.loadBattleBackground(35));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep8 =
                animationStep2
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.05f))
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .append(owner.tintEnvironment(3, 0, 0.8125f, 0.0f, 0.05f))
                        .append(owner.scrollBattleBackground(2, 8, 0, 0.032f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1448,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(632))
                        .append(owner.emitParticles(-1, 632, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 632, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 632, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 632, 3, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(30);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                        animationStep8
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 632, 4, 11, 8, 0.5f))
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .append(
                                                        owner.tintCombatant(
                                                                16, 0.75f, 0.0f, 0.75f, color)),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                4,
                                                0.016f,
                                                0.032f,
                                                0.19995117f,
                                                -0.19995117f)),
                                owner.tintEnvironment(3, 0, 0.0f, 0.8125f, 0.05f))
                        .append(owner.tintEnvironment(2, 0, 0.8125f, 0.0f, 0.05f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)));
        color = EncounterMoveAnimation.color(30);
        result =
                animationStep9
                        .append(owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
