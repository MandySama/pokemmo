package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 身体轻量化☆（自定义动画注册 ID：3475）的攻击／事件阶段动画。
 *
 * <p>招式描述：削掉身体上没用的部分. 大幅提高自己的速度, 同时体重也会变轻.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；恢复默认镜头。
 *
 * <p>变体映射：基础招式 ID 为 475；按参考本地化规则沿用基础描述，并在名称末尾添加 ☆。
 *
 * <p>编号对应参考动画注册表；语言条目为 110475（名称）和 120475（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove3475Animation.java
 *
 * <p>源文件 SHA-256：23c94783d9544f04421b69673e0c17494aae0b10d48fc2f6438aae8d325c5ec4
 */
final class ReferenceMoveProgram3475 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.625f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1410,
                                        1,
                                        14,
                                        0.0f,
                                        0.546875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1556,
                                        2,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1556,
                                        2,
                                        14,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.shakeCombatant(14, 2, 6, 0.0f, 0.048f, 1.0f, 0.0f))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.0f, 0.48f, 0.19995117f, -0.19995117f))
                        .onStart(owner.preloadParticleEffect(3475))
                        .append(owner.emitParticles(-1, 3475, 0, 9, 8, 0.25f))
                        .append(owner.emitParticles(-1, 3475, 1, 9, 8, 0.25f))
                        .append(owner.emitParticles(-1, 3475, 2, 9, 8, 0.25f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep7, owner.emitParticles(-1, 3475, 3, 9, 8, 0.25f))
                        .append(owner.tintCombatant(14, 0.5f, 0.625f, 0.0f, color2))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
