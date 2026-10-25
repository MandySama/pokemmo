package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 爆裂拳（招式 ID：223）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出浑身力气出拳进行攻击. 必定会使对手混乱.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110223（名称）和 120223（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/DynamicPunchAnimation.java
 *
 * <p>源文件 SHA-256：ac129a94be4618909075adfa098940534085723cd78912e08bf0abd70ca7117b
 */
final class ReferenceMoveProgram223 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep15 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1425,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        1666.6666f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 640.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        666.6667f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        833.3333f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        1000.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .onStart(owner.loadBattleBackground(1))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(389))
                                        .append(owner.emitParticles(-1, 389, 6, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 389, 5, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 389, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 389, 4, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 389, 3, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 389, 1, 11, 8, 0.5f),
                                0.2f)
                        .append(owner.emitParticles(-1, 389, 0, 11, 8, 0.5f))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 2, 0.0f, 0.064f, -0.19995117f, 0.19995117f))
                        .append(owner.tintEnvironment(2, 0, 0.0f, 0.8125f, 0.05f))
                        .append(owner.scrollBattleBackground(2, 0, 4, 0.016f));
        Runnable sceneFlagAction2 = owner.sceneFlag(2, true);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        Color color = EncounterMoveAnimation.color(0);
        Runnable sceneFlagAction4 = owner.sceneFlag(0, true);
        Runnable sceneFlagAction5 = owner.sceneFlag(1, true);
        ReferenceMoveTimeline animationStep16 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep15
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction2))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                sceneFlagAction3))
                                                .onStart(
                                                        ReferenceMoveTimeline.callback(
                                                                owner.sceneFlag(1, false)))
                                                .append(
                                                        owner.tintCombatant(
                                                                16, 0.75f, 0.0f, 0.75f, color))
                                                .append(owner.setSpriteAnimationPaused(16, 1)),
                                        owner.shakeCombatant(
                                                16, 2, 16, 0.016f, 0.016f, 0.30004883f, 0.0f)),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.05f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.05f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.restoreCamera(0.4f));
        color = EncounterMoveAnimation.color(0);
        result =
                animationStep16
                        .append(owner.tintCombatant(16, 0.25f, 0.75f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
