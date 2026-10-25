package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 大字爆炎（招式 ID：126）的攻击／事件阶段动画。
 *
 * <p>招式描述：用大字形状的火焰烧尽对手. 有时会让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110126（名称）和 120126（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FireBlastAnimation.java
 *
 * <p>源文件 SHA-256：c79a947550138f6ec82b3931a46a62f716c3c013f66c956e2f34424e9f18d5da
 */
final class ReferenceMoveProgram126 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep15 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(291))
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1426,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1497,
                                                                        0,
                                                                        14,
                                                                        0.0f,
                                                                        0.546875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        0,
                                                                        14,
                                                                        833.3333f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        0, 0, 0.0f, 720.0f)),
                                                owner.emitParticles(-1, 291, 6, 9, 8, 0.5f),
                                                0.6f)
                                        .append(owner.focusCamera(1, 0.8f))
                                        .append(owner.emitParticles(-1, 291, 7, 9, 11, 0.5f))
                                        .append(owner.tintEnvironment(2, 31, 0.0f, 0.8125f, 0.05f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1425,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        0,
                                        16,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 291, 0, 11, 8, 0.75f))
                        .append(owner.emitParticles(-1, 291, 1, 11, 8, 0.75f))
                        .append(owner.emitParticles(-1, 291, 2, 11, 8, 0.75f))
                        .append(owner.emitParticles(-1, 291, 3, 11, 8, 0.75f))
                        .append(owner.emitParticles(-1, 291, 4, 11, 8, 0.75f));
        Color color = EncounterMoveAnimation.color(31);
        Runnable sceneFlagAction2 = owner.sceneFlag(1, false);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, false);
        ReferenceMoveTimeline animationStep16 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep15
                                .append(owner.emitParticles(-1, 291, 5, 11, 8, 0.75f))
                                .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.625f, color))
                                .append(
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                6,
                                                0.016f,
                                                0.032f,
                                                -0.19995117f,
                                                0.30004883f))
                                .onStart(owner.loadBattleBackground(15))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2))
                                .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                                .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, true))),
                        owner.scrollBattleBackground(2, 0, 8, 0.032f));
        color = EncounterMoveAnimation.color(31);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, true);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep16.append(
                                        owner.tintCombatant(16, 0.75f, 0.625f, 0.0f, color)),
                                owner.tintEnvironment(3, 0, 0.0f, 1.0f, 0.025f),
                                0.32f)
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.025f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(2, false)))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
