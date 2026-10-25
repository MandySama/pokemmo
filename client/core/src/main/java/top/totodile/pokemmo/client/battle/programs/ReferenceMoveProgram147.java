package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 蘑菇孢子（招式 ID：147）的攻击／事件阶段动画。
 *
 * <p>招式描述：沙沙沙地撒满具有催眠效果的孢子, 从而让对手陷入睡眠状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110147（名称）和 120147（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SporeAnimation.java
 *
 * <p>源文件 SHA-256：5c533e246768e0105ce2d7f7e901f4ef5c7db81e99858bdd7b0723412a841f06
 */
final class ReferenceMoveProgram147 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(312))
                        .append(owner.emitParticles(-1, 312, 0, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(990);
        ReferenceMoveTimeline animationStep6 =
                animationStep2
                        .append(owner.emitParticles(-1, 312, 1, 11, 8, 0.5f))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.8125f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1718,
                                        1,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1718, 2, 16, 500.0f, 0.3125f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1463,
                                        1,
                                        16,
                                        833.3333f,
                                        0.9375f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(990);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep6
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        16,
                                                        3000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 800.0f, 2080.0f)),
                                owner.adjustSoundTrack(1, 1, 2080.0f, 800.0f)),
                        owner.tintCombatant(16, 0.5f, 0.8125f, 0.0f, color2));
        color2 = EncounterMoveAnimation.color(990);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep7, owner.tintCombatant(16, 0.5f, 0.0f, 0.8125f, color2));
        color2 = EncounterMoveAnimation.color(990);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep8, owner.tintCombatant(16, 0.5f, 0.8125f, 0.0f, color2));
        color2 = EncounterMoveAnimation.color(990);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep9, owner.tintCombatant(16, 0.5f, 0.0f, 0.8125f, color2));
        color2 = EncounterMoveAnimation.color(990);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                animationStep10,
                                                owner.tintCombatant(
                                                        16, 0.5f, 0.8125f, 0.0f, color2))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.050048828f, -0.050048828f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
