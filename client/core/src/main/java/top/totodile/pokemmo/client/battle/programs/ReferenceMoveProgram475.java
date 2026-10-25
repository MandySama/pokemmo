package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 身体轻量化（招式 ID：475）的攻击／事件阶段动画。
 *
 * <p>招式描述：削掉身体上没用的部分. 大幅提高自己的速度, 同时体重也会变轻.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110475（名称）和 120475（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/AutotomizeAnimation.java
 *
 * <p>源文件 SHA-256：84bffffa33cccbb685f056144eb6ef0eaf51bd2a37d29c37de0f1f5f283dfcf6
 */
final class ReferenceMoveProgram475 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep5 =
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
                        .onStart(owner.preloadParticleEffect(648))
                        .append(owner.emitParticles(-1, 648, 0, 9, 8, 0.25f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep5, owner.emitParticles(-1, 648, 1, 9, 8, 0.25f))
                        .append(owner.tintCombatant(14, 0.5f, 0.625f, 0.0f, color2))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
