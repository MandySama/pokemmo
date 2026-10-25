package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 火焰弹（招式 ID：545）的攻击／事件阶段动画。
 *
 * <p>招式描述：用熊熊烈火攻击自己周围所有的宝可梦. 有时会陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；切换战斗背景；滚动战斗背景；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110545（名称）和 120545（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SearingShotAnimation.java
 *
 * <p>源文件 SHA-256：0a2cf705537e90c5d458a612c830fc5527d8ce454265812520a83b7e40b40800
 */
final class ReferenceMoveProgram545 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Runnable sceneFlagAction2 = owner.sceneFlag(2, 1 != 0);
        Runnable sceneFlagAction3 = owner.sceneFlag(0, 0 != 0);
        Runnable sceneFlagAction4 = owner.sceneFlag(1, 0 != 0);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .onStart(owner.loadBattleBackground(1))
                                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction2)),
                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.05f),
                                0.2f)
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction3))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction4))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1487,
                                        1,
                                        14,
                                        0.0f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 720.0f, 144.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1536,
                                        1,
                                        14,
                                        916.6667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(708))
                        .append(owner.emitParticles(-1, 708, 5, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 708, 6, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 708, 2, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 708, 3, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep7.append(owner.emitParticles(-1, 708, 0, 9, 8, 0.5f)),
                        owner.tintCombatant(14, 1.0f, 0.0f, 0.625f, color),
                        1.8f);
        color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep15 =
                animationStep8
                        .append(owner.tintCombatant(14, 1.0f, 0.625f, 0.0f, color))
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.2f)
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1460,
                                        2,
                                        16,
                                        0.0f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1536,
                                        1,
                                        16,
                                        166.66667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1460,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1536,
                                        1,
                                        16,
                                        500.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1460,
                                        2,
                                        16,
                                        666.6667f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1536,
                                        2,
                                        16,
                                        833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1895,
                                        1,
                                        16,
                                        833.3333f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.128f, 0.39990234f, -0.39990234f))
                        .append(owner.setSpriteAnimationPaused(16, 1));
        Color color2 = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep16 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep15,
                                owner.emitParticles(-1, 708, 1, 11, 8, 0.375f),
                                2.4f)
                        .append(owner.tintCombatant(16, 1.0f, 0.0f, 0.625f, color2))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 9, 0.0f, 0.064f, 0.30004883f, -0.30004883f));
        Color color3 = EncounterMoveAnimation.color(31);
        Runnable sceneFlagAction5 = owner.sceneFlag(0, true);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                animationStep16
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 708, 4, 11, 8, 0.25f))
                                                        .append(
                                                                owner.tintEnvironment(
                                                                        3, 0, 1.0f, 0.0f, 0.075f)),
                                                owner.scrollBattleBackground(2, 6, 0, 0.032f))
                                        .append(
                                                owner.tintCombatant(
                                                        16, 1.0f, 0.625f, 0.0f, color3)),
                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.075f))
                        .append(owner.tintEnvironment(2, 0, 1.0f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(ReferenceMoveTimeline.callback(sceneFlagAction5))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(1, true)))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
