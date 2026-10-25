package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 棉孢子（招式 ID：178）的攻击／事件阶段动画。
 *
 * <p>招式描述：将棉花般柔软的孢子紧贴对手, 从而大幅降低对手的速度.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110178（名称）和 120178（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/CottonSporeAnimation.java
 *
 * <p>源文件 SHA-256：ca8db85718a84b814f3a5689cf2bac2e4ab0fbe09f7fdf5967d7c4d6cd08b311
 */
final class ReferenceMoveProgram178 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(345))
                        .append(owner.emitParticles(-1, 345, 0, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(31710);
        ReferenceMoveTimeline animationStep5 =
                animationStep2
                        .append(owner.emitParticles(-1, 345, 1, 11, 8, 0.5f))
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
                                        (byte) 2,
                                        1438,
                                        2,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(31710);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                animationStep5
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1463,
                                                                        2,
                                                                        16,
                                                                        500.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 1280.0f, 320.0f)),
                                                owner.adjustSoundTrack(2, 0, 320.0f, 1760.0f))
                                        .append(
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
