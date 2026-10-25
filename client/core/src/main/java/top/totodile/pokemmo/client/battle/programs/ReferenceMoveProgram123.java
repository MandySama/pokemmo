package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 浊雾（招式 ID：123）的攻击／事件阶段动画。
 *
 * <p>招式描述：将肮脏的浓雾吹向对手进行攻击. 有时会让对手陷入中毒状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110123（名称）和 120123（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SmogAnimation.java
 *
 * <p>源文件 SHA-256：616562d676ed7419d3473f798edb9d6f443fba0dc6b9695864126fefe88a4587
 */
final class ReferenceMoveProgram123 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(288))
                        .append(owner.emitParticles(-1, 288, 0, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1478,
                                        1,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep3.append(
                                owner.scheduleSound(
                                        (byte) 2, 1479, 2, 16, 0.0f, 0.625f, owner.casterHandle)),
                        owner.tintCombatant(16, 0.25f, 0.0f, 0.625f, color));
        color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep4, owner.tintCombatant(16, 0.25f, 0.625f, 0.0f, color));
        color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep5, owner.tintCombatant(16, 0.25f, 0.0f, 0.625f, color));
        color = EncounterMoveAnimation.color(0);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep6, owner.tintCombatant(16, 0.25f, 0.625f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
