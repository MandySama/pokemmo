package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 镜面属性（招式 ID：513）的攻击／事件阶段动画。
 *
 * <p>招式描述：反射对手的属性, 让自己也变成一样的属性.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110513（名称）和 120513（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/ReflectTypeAnimation.java
 *
 * <p>源文件 SHA-256：ae959495ff94b1793b491b170686fa8358c88f22533921cfe20463ff085ec4ec
 */
final class ReferenceMoveProgram513 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(678))
                        .append(owner.tintCombatant(16, 0.25f, 0.0f, 0.5f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1450,
                                        1,
                                        16,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 480.0f))
                        .append(owner.adjustSoundTrack(1, 1, 400.0f, 160.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        666.6667f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1450,
                                        2,
                                        16,
                                        483.33334f,
                                        0.3125f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 0, 464.0f, 480.0f))
                        .append(owner.adjustSoundTrack(2, 1, 896.0f, 320.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        1166.6666f,
                                        0.0f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep5
                                        .append(owner.emitParticles(-1, 678, 1, 11, 8, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(0.6f)
                                        .beginParallel(),
                                owner.tintCombatant(16, 0.25f, 0.5f, 0.0f, color2))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1450,
                                        1,
                                        14,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 320.0f, 1920.0f))
                        .append(owner.adjustSoundTrack(1, 1, 320.0f, 1920.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        2000.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 0));
        Color color3 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep8
                                .append(owner.emitParticles(-1, 678, 0, 9, 8, 0.25f))
                                .append(owner.focusCamera(0, 0.4f))
                                .beginSequence()
                                .waitSeconds(0.6f)
                                .beginParallel(),
                        owner.tintCombatant(14, 0.25f, 0.0f, 0.5f, color3));
        color3 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                animationStep9
                        .append(owner.tintCombatant(14, 0.25f, 0.5f, 0.0f, color3))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
