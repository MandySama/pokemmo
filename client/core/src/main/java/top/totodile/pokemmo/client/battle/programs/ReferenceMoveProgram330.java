package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 浊流（招式 ID：330）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手喷射浑浊的水进行攻击. 有时会降低对手的命中率.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110330（名称）和 120330（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/MuddyWaterAnimation.java
 *
 * <p>源文件 SHA-256：b9953429713a66e8bdd63216069a528efbc59eef96bef4851dd848a5d029ce04
 */
final class ReferenceMoveProgram330 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(0, 0.4f))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.scaleCombatant(
                                                                14,
                                                                2,
                                                                1,
                                                                0.016f,
                                                                0.192f,
                                                                0.30004883f,
                                                                -0.30004883f))
                                                .onStart(owner.setHealthHudHidden(18, true))
                                                .onStart(owner.preloadParticleEffect(500))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1897,
                                                                1,
                                                                14,
                                                                83.333336f,
                                                                0.8984375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                1, 0, 80.0f, 1184.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                1,
                                                                14,
                                                                1333.3334f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1493,
                                                                2,
                                                                14,
                                                                500.0f,
                                                                0.78125f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                2,
                                                                16,
                                                                2333.3333f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1849,
                                                                1,
                                                                16,
                                                                1333.3334f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 500, 2, 11, 8, 0.0f))
                                                .append(owner.emitParticles(-1, 500, 3, 9, 8, 0.0f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 500, 4, 9, 8, 0.0f)),
                                        owner.emitParticles(-1, 500, 5, 9, 11, 0.0f),
                                        0.4f)
                                .append(owner.focusCamera(0, 1.0f))
                                .endGroup()
                                .endGroup()
                                .beginSequence()
                                .waitSeconds(1.0f)
                                .beginParallel()
                                .append(owner.focusCamera(1, 0.8f)),
                        owner.scaleCombatant(14, 2, 1, 0.016f, 0.08f, -0.30004883f, 0.30004883f),
                        1.4f);
        Color color = EncounterMoveAnimation.color(13023);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        animationStep10,
                                        owner.emitParticles(-1, 500, 1, 11, 8, 0.0f),
                                        1.6f)
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color)),
                        owner.shakeCombatant(16, 2, 8, 0.032f, 0.016f, 0.30004883f, 0.0f),
                        2.04f);
        color = EncounterMoveAnimation.color(13023);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep11, owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .beginSequence()
                        .waitSeconds(0.12f)
                        .beginParallel()
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
