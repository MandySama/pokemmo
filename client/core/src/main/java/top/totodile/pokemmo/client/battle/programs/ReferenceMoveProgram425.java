package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 影子偷袭（招式 ID：425）的攻击／事件阶段动画。
 *
 * <p>招式描述：伸长影子, 从对手的背后进行攻击. 必定能够先制攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110425（名称）和 120425（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/ShadowSneakAnimation.java
 *
 * <p>源文件 SHA-256：c7aaa3c8f81051ec0ae5c10438f3a8d0189111264552ec03a77cd1ccab00fe9a
 */
final class ReferenceMoveProgram425 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .waitSeconds(0.6f)
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.tintEnvironment(
                                                        4, Short.MAX_VALUE, 0.0f, 0.3125f, 0.075f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1446,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1417,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        14,
                                                        1833.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 800.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 1280.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        1166.6666f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        1250.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        1333.3334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(600)),
                                owner.launchParticles(600, 0, 9, 11, 0.25f, 0.0f),
                                0.6f)
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.2f)
                        .beginParallel();
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep7
                                .append(owner.emitParticles(-1, 600, 1, 11, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(owner.tintCombatant(16, 0.25f, 0.0f, 0.75f, color)),
                        owner.shakeCombatant(16, 2, 4, 0.016f, 0.032f, 0.19995117f, 0.0f));
        color = EncounterMoveAnimation.color(0);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep8
                                .append(owner.tintCombatant(16, 0.25f, 0.75f, 0.0f, color))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.restoreCamera(0.4f))
                                .onStart(owner.setHealthHudHidden(18, false)),
                        owner.tintEnvironment(4, Short.MAX_VALUE, 0.3125f, 0.0f, 0.075f));
        return result;
    }
}
