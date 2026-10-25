package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 怨恨（招式 ID：180）的攻击／事件阶段动画。
 *
 * <p>招式描述：对对手最后使用的招式怀有怨恨, 减少4PP该招式.
 *
 * <p>动画说明：镜头聚焦施术者；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110180（名称）和 120180（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SpiteAnimation.java
 *
 * <p>源文件 SHA-256：7c0d159f96dee5166c6060dcc67b23669d588e1eebbf9f023c2717193a923fef
 */
final class ReferenceMoveProgram180 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1783, 2, 2, 0.0f, 0.3125f, owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep2
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        2,
                                                        2500.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 2080.0f, 320.0f))
                                        .append(owner.setSpriteAnimationPaused(14, 1)),
                                owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f))
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep3,
                                owner.scheduleSound(
                                        (byte) 2, 1448, 1, 14, 0.0f, 0.9375f, owner.casterHandle))
                        .append(owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color2))
                        .append(owner.shakeCombatant(16, 2, 2, 0.032f, 0.016f, 0.0f, 0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .beginParallel();
        Color color3 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep4
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1448,
                                                1,
                                                14,
                                                0.0f,
                                                0.9375f,
                                                owner.casterHandle))
                                .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color3)),
                        owner.shakeCombatant(16, 2, 2, 0.032f, 0.016f, 0.0f, 0.30004883f));
        color3 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep5.append(
                                        owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color3)),
                                owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
