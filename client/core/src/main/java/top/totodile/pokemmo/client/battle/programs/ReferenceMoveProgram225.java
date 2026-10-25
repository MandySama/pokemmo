package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 龙息（招式 ID：225）的攻击／事件阶段动画。
 *
 * <p>招式描述：将强烈的气息吹向对手进行攻击. 有时会让对手陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110225（名称）和 120225（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/DragonBreathAnimation.java
 *
 * <p>源文件 SHA-256：58a7e8e42036324316569036dcf1585acf944ae9b5af2809773ba4d8fb3e6c41
 */
final class ReferenceMoveProgram225 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1492, 1, 14, 0.0f, 0.78125f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1376,
                                        1,
                                        14,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 800.0f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        animationStep3
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1491,
                                                                                2,
                                                                                16,
                                                                                1166.6666f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                391))
                                                                .append(
                                                                        owner.launchParticles(
                                                                                391, 0, 9, 11, 0.5f,
                                                                                0.0f)),
                                                        owner.launchParticles(
                                                                391, 1, 9, 11, 0.5f, 0.0f),
                                                        0.6f)
                                                .append(owner.focusCamera(1, 0.4f))
                                                .append(
                                                        owner.shakeCombatant(
                                                                16, 2, 4, 0.032f, 0.032f, 0.5f,
                                                                0.0f)),
                                        owner.tintCombatant(16, 1.0f, 0.0f, 0.75f, color),
                                        1.2f)
                                .append(owner.setSpriteAnimationPaused(16, 1)),
                        owner.scaleCombatant(
                                16, 2, 1, 0.048f, 0.032f, -0.100097656f, 0.100097656f));
        color = EncounterMoveAnimation.color(31);
        result =
                animationStep4
                        .append(owner.tintCombatant(16, 1.0f, 0.75f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
