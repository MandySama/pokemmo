package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 黑夜魔影（招式 ID：101）的攻击／事件阶段动画。
 *
 * <p>招式描述：显示恐怖幻影, 只给予对手和自己等级相同的伤害.
 *
 * <p>动画说明：镜头聚焦施术者；调整场景着色；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110101（名称）和 120101（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/NightShadeAnimation.java
 *
 * <p>源文件 SHA-256：0b7cb95b777d33aea14adfce81e46b85c40a894d6a0a27e2b9f6325bc3046c8a
 */
final class ReferenceMoveProgram101 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(owner.focusCamera(0, 0.6f)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.625f, 0.05f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1535,
                                        1,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep2
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1536,
                                                2,
                                                14,
                                                0.0f,
                                                0.8984375f,
                                                owner.casterHandle))
                                .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.5f, color))
                                .onStart(owner.setHealthHudHidden(14, true)),
                        owner.scaleCombatant(14, 1, 1, 0.016f, 0.064f, 1.1999512f, 1.1999512f),
                        0.44f);
        color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep3.append(
                                        owner.tintCombatant(16, 0.75f, 0.0f, 0.875f, color)),
                                owner.shakeCombatant(16, 2, 5, 0.032f, 0.016f, 0.5f, 0.0f))
                        .onStart(owner.setHealthHudHidden(14, false));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep5 =
                animationStep4
                        .append(owner.tintCombatant(14, 0.5f, 0.5f, 0.0f, color))
                        .append(owner.scaleCombatant(14, 1, 1, 0.016f, 0.064f, 1.0f, 1.0f));
        color = EncounterMoveAnimation.color(0);
        result =
                animationStep5
                        .append(owner.tintCombatant(16, 0.5f, 0.875f, 0.0f, color))
                        .append(owner.tintEnvironment(4, 0, 0.625f, 0.0f, 0.05f))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
