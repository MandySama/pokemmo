package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 礼物（招式 ID：217）的攻击／事件阶段动画。
 *
 * <p>招式描述：递给对手设有圈套的盒子进行攻击. 也有可能回复对手HP.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110217（名称）和 120217（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/PresentAnimation.java
 *
 * <p>源文件 SHA-256：019f9bfae7e92d5c1df9e034f5622129f15139cf70b727000165a303b68ada2f
 */
final class ReferenceMoveProgram217 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.325f))
                                        .onStart(owner.preloadParticleEffect(384))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1510,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1471,
                                                        2,
                                                        16,
                                                        666.6667f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.8984375f,
                                                        owner.casterHandle)),
                                owner.launchParticles(384, 3, 9, 11, 0.5f, 240.0f))
                        .append(owner.emitParticles(-1, 384, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 384, 1, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(992);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep6.append(owner.emitParticles(-1, 384, 2, 11, 8, 0.25f)),
                        owner.tintCombatant(16, 0.75f, 0.0f, 0.625f, color));
        color = EncounterMoveAnimation.color(992);
        result =
                animationStep7
                        .append(owner.tintCombatant(16, 0.75f, 0.625f, 0.0f, color))
                        .append(owner.restoreCamera(0.325f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
