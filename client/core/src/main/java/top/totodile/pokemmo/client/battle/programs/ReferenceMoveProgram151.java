package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 溶化（招式 ID：151）的攻击／事件阶段动画。
 *
 * <p>招式描述：通过细胞的变化进行液化, 从而大幅提高自己的防御.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110151（名称）和 120151（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/AcidArmorAnimation.java
 *
 * <p>源文件 SHA-256：e2a24ef306e74154cb66b0e84749cb88b5d68cd6b57c1205986aaf6877e776f8
 */
final class ReferenceMoveProgram151 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1723,
                                        1,
                                        14,
                                        0.0f,
                                        0.9765625f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1120.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        2,
                                        14,
                                        666.6667f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        1500.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 560.0f, 800.0f))
                        .onStart(owner.preloadParticleEffect(315));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep4
                                .append(owner.emitParticles(-1, 315, 0, 9, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(14, 1))
                                .onStart(owner.setHealthHudHidden(14, true))
                                .append(owner.tintCombatant(14, 0.5f, 0.0f, 1.0f, color))
                                .onStart(owner.setHealthHudHidden(14, true)),
                        owner.scaleCombatant(14, 2, 1, 0.032f, 0.48f, 0.30004883f, -0.30004883f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep5.append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1452,
                                                1,
                                                14,
                                                333.33334f,
                                                0.9375f,
                                                owner.casterHandle)),
                                owner.tintCombatant(14, 0.5f, 1.0f, 0.0f, color2))
                        .onStart(owner.setHealthHudHidden(14, false))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
