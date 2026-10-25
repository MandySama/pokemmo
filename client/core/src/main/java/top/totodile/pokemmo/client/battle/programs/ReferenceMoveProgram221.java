package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 神圣之火（招式 ID：221）的攻击／事件阶段动画。
 *
 * <p>招式描述：用神秘的火焰烧尽对手进行攻击. 有时会让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110221（名称）和 120221（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SacredFireAnimation.java
 *
 * <p>源文件 SHA-256：810592d4dbc92dd9d52e960b5ed14c5dc0e8819d692b2b20d9ad0f4f1d2d8abd
 */
final class ReferenceMoveProgram221 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f)),
                                owner.tintEnvironment(2, 0, 0.0f, 1.0f, 0.025f))
                        .append(owner.tintEnvironment(3, 0, 1.0f, 0.0f, 0.025f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1815,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1434,
                                        2,
                                        16,
                                        166.66667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1600.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1420,
                                        1,
                                        16,
                                        1750.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        1583.3334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.loadBattleBackground(22));
        Runnable sceneFlagAction2 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(1, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(2, 1 != 0);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        animationStep6
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction3))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction4))
                                                .append(
                                                        owner.scrollBattleBackground(
                                                                1, -1600, 0, 2.24f))
                                                .onStart(owner.preloadParticleEffect(388)),
                                        owner.emitParticles(-1, 388, 3, 11, 8, 0.5f),
                                        0.2f)
                                .append(owner.emitParticles(-1, 388, 0, 11, 8, 0.5f)),
                        owner.emitParticles(-1, 388, 1, 11, 8, 0.5f),
                        0.8f);
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep9
                                        .append(owner.emitParticles(-1, 388, 2, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.tintCombatant(
                                                        16, 0.25f, 0.0f, 0.8125f, color)),
                                owner.shakeCombatant(16, 2, 10, 0.016f, 0.032f, 0.5f, 0.0f))
                        .append(owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.025f));
        color = EncounterMoveAnimation.color(31);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(1, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep10,
                                owner.tintCombatant(16, 0.25f, 0.8125f, 0.0f, color))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
