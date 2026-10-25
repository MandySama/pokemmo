package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 百万吨重踢（招式 ID：25）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出力大无穷的重踢踢飞对手进行攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110025（名称）和 120025（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/MegaKickAnimation.java
 *
 * <p>源文件 SHA-256：b708a4692ed0fb8c6a49b1bb8996ead3c5b504da0b6a8d7fe17bbf99c9738c7e
 */
final class ReferenceMoveProgram25 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, false);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(1, 0.4f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .onStart(
                                                                        owner.loadBattleBackground(
                                                                                1)),
                                                        owner.tintEnvironment(
                                                                2, 0, 0.0f, 1.0f, 0.025f))
                                                .append(
                                                        owner.scrollBattleBackground(
                                                                2, 0, 4, 0.016f))
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
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1421,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1526,
                                                                2,
                                                                16,
                                                                250.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1424,
                                                                1,
                                                                16,
                                                                250.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(187))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 187, 0, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 187, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 187, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 187, 3, 11, 8, 0.5f))
                                                .append(owner.setSpriteAnimationPaused(16, 1)),
                                        owner.shakeCombatant(
                                                16, 2, 16, 0.016f, 0.016f, 0.60009766f, 0.0f)),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.025f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f));
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction6 = owner.sceneFlag(1, true);
        result =
                animationStep8
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction6))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
