package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 催眠粉（招式 ID：79）的攻击／事件阶段动画。
 *
 * <p>招式描述：撒出催眠粉, 从而让对手陷入睡眠状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110079（名称）和 120079（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SleepPowderAnimation.java
 *
 * <p>源文件 SHA-256：0d0e253fbc02c6845d2694b6314097c786bc87a68a1b8a4364636606585628b5
 */
final class ReferenceMoveProgram79 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(243))
                        .append(owner.emitParticles(-1, 243, 0, 11, 8, 0.75f))
                        .append(owner.emitParticles(-1, 243, 1, 11, 8, 0.75f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1718,
                                        2,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1463,
                                        1,
                                        16,
                                        666.6667f,
                                        0.78125f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(21440);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep5
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        16,
                                                        2166.6667f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 640.0f, 1440.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 560.0f, 320.0f)),
                                owner.adjustSoundTrack(1, 1, 1440.0f, 640.0f),
                                0.6f)
                        .append(owner.tintCombatant(16, 3.25f, 0.0f, 0.5f, color));
        color = EncounterMoveAnimation.color(21440);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep6.append(
                                        owner.tintCombatant(16, 3.25f, 0.5f, 0.0f, color)),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.032f, 0.016f, 0.020019531f, -0.020019531f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
