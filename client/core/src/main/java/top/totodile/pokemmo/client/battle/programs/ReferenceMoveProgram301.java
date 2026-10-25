package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰球（招式 ID：301）的攻击／事件阶段动画。
 *
 * <p>招式描述：5回合内连续滚动攻击, 每次命中威力增加. 如果在对战中自身曾使用变圆, 则该招式威力翻倍.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110301（名称）和 120301（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/IceBallAnimation.java
 *
 * <p>源文件 SHA-256：bb7d42f0b4f42173ff67bce28a5a6af76c4f17c90e86656dd32a39c34135d0e3
 */
final class ReferenceMoveProgram301 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1457,
                                        1,
                                        16,
                                        0.0f,
                                        0.5078125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1358,
                                        2,
                                        16,
                                        0.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(467))
                        .append(owner.launchParticles(467, 0, 9, 11, 0.5f, 360.0f))
                        .append(owner.launchParticles(467, 1, 9, 11, 0.5f, 360.0f))
                        .append(owner.launchParticles(467, 2, 9, 11, 0.5f, 360.0f))
                        .append(owner.focusCamera(1, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep3, owner.emitParticles(-1, 467, 3, 11, 8, 0.5f), 0.8f)
                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, 0.19995117f, -0.19995117f))
                        .append(owner.shakeCombatant(16, 2, 6, 0.016f, 0.016f, 0.5f, 0.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1505, 1, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1719,
                                        2,
                                        16,
                                        33.333332f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1719,
                                        2,
                                        16,
                                        166.66667f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep7,
                                owner.scheduleSound(
                                        (byte) 2,
                                        1719,
                                        2,
                                        16,
                                        233.33333f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color2))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
