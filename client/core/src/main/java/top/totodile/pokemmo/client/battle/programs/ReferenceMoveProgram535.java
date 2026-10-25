package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 高温重压（招式 ID：535）的攻击／事件阶段动画。
 *
 * <p>招式描述：用燃烧的身体撞向对手进行攻击. 自己比对手越重, 威力越大.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110535（名称）和 120535（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/HeatCrashAnimation.java
 *
 * <p>源文件 SHA-256：347c3493a40c51f71b46b80bec4c9d5efa781246bc21eea630e64a9f96178b4d
 */
final class ReferenceMoveProgram535 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .onStart(owner.preloadParticleEffect(699))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1427,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1507,
                                                        2,
                                                        16,
                                                        250.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1535,
                                                        0,
                                                        16,
                                                        500.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1416,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 1280.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        16,
                                                        1666.6666f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 699, 1, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 699, 2, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 699, 3, 11, 8, 0.25f)),
                                owner.emitParticles(-1, 699, 4, 11, 8, 0.25f),
                                0.2f)
                        .append(owner.scaleCombatant(16, 2, 1, 0.016f, 0.064f, 0.5f, -0.5f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep10
                                        .append(owner.emitParticles(-1, 699, 0, 11, 8, 0.375f))
                                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.625f, color))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.shakeCombatant(16, 2, 8, 0.0f, 0.064f, 0.5f, 0.0f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(16, 0));
        color = EncounterMoveAnimation.color(31);
        result =
                animationStep11
                        .append(owner.tintCombatant(16, 0.75f, 0.625f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
