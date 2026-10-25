package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 力量宝石（招式 ID：408）的攻击／事件阶段动画。
 *
 * <p>招式描述：发射如宝石般闪耀的光芒攻击对手.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110408（名称）和 120408（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/PowerGemAnimation.java
 *
 * <p>源文件 SHA-256：94cc98b8fbdaee891d46ea6e8d0b70ca00ae984deb3dfee34bc91cb3142296f3
 */
final class ReferenceMoveProgram408 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f))
                        .append(owner.tintCombatant(14, 1.75f, 0.0f, 0.5f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1713,
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
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1200.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1510,
                                        2,
                                        14,
                                        1333.3334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(583))
                        .append(owner.emitParticles(-1, 583, 2, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 583, 1, 9, 11, 0.5f))
                        .append(owner.emitParticles(-1, 583, 0, 11, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                animationStep7,
                                                                owner.emitParticles(
                                                                        -1, 583, 3, 11, 8, 0.5f),
                                                                1.6f)
                                                        .append(
                                                                owner.tintCombatant(
                                                                        14, 0.5f, 0.5f, 0.0f,
                                                                        color2))
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1421,
                                                                        1,
                                                                        16,
                                                                        500.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1421,
                                                                        1,
                                                                        16,
                                                                        583.3333f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1421,
                                                                        1,
                                                                        16,
                                                                        666.6667f,
                                                                        0.8984375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1421,
                                                                        1,
                                                                        16,
                                                                        750.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1421,
                                                        1,
                                                        16,
                                                        833.3333f,
                                                        0.8984375f,
                                                        owner.casterHandle),
                                                2.0f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
