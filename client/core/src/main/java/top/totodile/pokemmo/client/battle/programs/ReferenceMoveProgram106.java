package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 变硬（招式 ID：106）的攻击／事件阶段动画。
 *
 * <p>招式描述：全身使劲, 让身体变硬, 从而提高自己的防御.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110106（名称）和 120106（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/HardenAnimation.java
 *
 * <p>源文件 SHA-256：7766b6bf234f0eb40529fc3c007e2aadffd21101f85f30988d0aa6d1fb87d556
 */
final class ReferenceMoveProgram106 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1445,
                                        1,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 1, 14, 666.6667f, 0.0f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 640.0f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep3
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1449,
                                                2,
                                                14,
                                                500.0f,
                                                0.625f,
                                                owner.casterHandle))
                                .onStart(owner.preloadParticleEffect(272))
                                .append(owner.setSpriteAnimationPaused(14, 1))
                                .append(
                                        owner.scaleCombatant(
                                                14,
                                                2,
                                                4,
                                                0.0f,
                                                0.032f,
                                                0.100097656f,
                                                0.100097656f)),
                        owner.tintCombatant(14, 0.25f, 0.0f, 0.625f, color));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep4, owner.emitParticles(-1, 272, 0, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintCombatant(14, 0.25f, 0.625f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
