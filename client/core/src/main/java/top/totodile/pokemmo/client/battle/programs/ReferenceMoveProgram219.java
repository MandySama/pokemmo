package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 神秘守护（招式 ID：219）的攻击／事件阶段动画。
 *
 * <p>招式描述：在5回合内被神奇的力量守护, 从而不会陷入异常状态.
 *
 * <p>动画说明：播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110219（名称）和 120219（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SafeguardAnimation.java
 *
 * <p>源文件 SHA-256：63ac0881fa4660e3c9577cbdc5011d8e67d339ba441fd400e9deb3ef54ceabe0
 */
final class ReferenceMoveProgram219 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.waitThenParallel(0.4f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1483,
                                        1,
                                        14,
                                        0.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        2,
                                        14,
                                        0.0f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1166.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(386))
                        .append(owner.emitParticles(-1, 386, 0, 9, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep5
                                .append(owner.emitParticles(-1, 386, 1, 9, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(14, 1))
                                .beginSequence()
                                .waitSeconds(0.2f)
                                .beginParallel()
                                .append(owner.tintCombatant(14, 1.0f, 0.0f, 0.625f, color))
                                .append(
                                        owner.shakeCombatant(
                                                14, 2, 4, 0.016f, 0.016f, 0.19995117f, 0.0f)),
                        owner.scaleCombatant(14, 2, 1, 0.016f, 0.16f, 0.100097656f, -0.100097656f));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                animationStep6
                        .append(owner.tintCombatant(14, 1.0f, 0.625f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
