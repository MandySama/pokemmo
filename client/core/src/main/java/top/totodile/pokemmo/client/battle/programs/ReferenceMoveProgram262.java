package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 临别礼物（招式 ID：262）的攻击／事件阶段动画。
 *
 * <p>招式描述：虽然会使自己陷入濒死, 但是能够大幅降低对手的攻击和特攻.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110262（名称）和 120262（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/MementoAnimation.java
 *
 * <p>源文件 SHA-256：2e916004c85ba9c33906c22e95f1c705e472e6108dcfc7365ee7b5241426fb98
 */
final class ReferenceMoveProgram262 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1497,
                                        1,
                                        14,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1000.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 800.0f))
                        .append(owner.adjustSoundTrack(1, 1, 640.0f, 320.0f))
                        .onStart(owner.preloadParticleEffect(428));
        Color color = EncounterMoveAnimation.color(6150);
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep3
                                .append(owner.emitParticles(-1, 428, 0, 9, 8, 0.0f))
                                .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color)),
                        owner.scaleCombatant(14, 2, 1, 0.016f, 0.192f, 0.0f, 0.8000488f),
                        0.4f);
        color = EncounterMoveAnimation.color(6150);
        ReferenceMoveTimeline animationStep5 =
                animationStep4
                        .append(owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color))
                        .append(owner.focusCamera(1, 0.8f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.0f)
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 1));
        color = EncounterMoveAnimation.color(6150);
        ReferenceMoveTimeline animationStep7 =
                animationStep5
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1497,
                                        2,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        1000.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 800.0f))
                        .append(owner.adjustSoundTrack(2, 1, 640.0f, 320.0f));
        Color color2 = EncounterMoveAnimation.color(6150);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        animationStep7,
                                        owner.emitParticles(-1, 428, 1, 11, 8, 0.0f),
                                        1.4f),
                                owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
