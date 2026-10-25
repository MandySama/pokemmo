package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 吞下（招式 ID：256）的攻击／事件阶段动画。
 *
 * <p>招式描述：将积蓄的力量吞下, 从而回复自己的HP. 积蓄得越多, 回复越大.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110256（名称）和 120256（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SwallowAnimation.java
 *
 * <p>源文件 SHA-256：ea143d814b760b5ca6791347aa158e36c5749da8d6ab48084afa1b9829925c75
 */
final class ReferenceMoveProgram256 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1779,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1427,
                                                        1,
                                                        14,
                                                        1333.3334f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1473,
                                                        2,
                                                        14,
                                                        1500.0f,
                                                        0.3125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1473,
                                                        1,
                                                        14,
                                                        1783.3334f,
                                                        0.15625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.032f,
                                                        0.096f,
                                                        -0.30004883f,
                                                        0.30004883f))
                                        .onStart(owner.preloadParticleEffect(422)),
                                owner.emitParticles(-1, 422, 1, 9, 8, 0.5f),
                                0.8f)
                        .append(owner.emitParticles(-1, 422, 0, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep7, owner.emitParticles(-1, 422, 2, 9, 8, 0.5f), 1.6f),
                        owner.tintCombatant(14, 0.75f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                animationStep8
                        .append(owner.tintCombatant(14, 0.75f, 0.75f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
