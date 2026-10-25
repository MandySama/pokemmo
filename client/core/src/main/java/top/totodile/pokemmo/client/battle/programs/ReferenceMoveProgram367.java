package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 点穴（招式 ID：367）的攻击／事件阶段动画。
 *
 * <p>招式描述：通过点穴让身体舒筋活络. 大幅提高某1项能力.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110367（名称）和 120367（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/AcupressureAnimation.java
 *
 * <p>源文件 SHA-256：acc12d04505d3d6736de4a2c912a6ba6ad4cf2992a9301ecad20687a5959dfb2
 */
final class ReferenceMoveProgram367 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1505,
                                        1,
                                        16,
                                        0.0f,
                                        0.703125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1721,
                                        2,
                                        16,
                                        0.0f,
                                        0.546875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1867,
                                        1,
                                        16,
                                        500.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 720.0f, 432.0f))
                        .append(owner.adjustSoundTrack(1, 0, 320.0f, 320.0f))
                        .onStart(owner.preloadParticleEffect(541))
                        .append(owner.emitParticles(-1, 541, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 541, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 541, 2, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(990);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep7.append(owner.emitParticles(-1, 541, 3, 11, 8, 0.5f)),
                        owner.tintCombatant(16, 0.25f, 0.0f, 0.625f, color));
        color = EncounterMoveAnimation.color(990);
        result =
                animationStep8
                        .append(owner.tintCombatant(16, 0.25f, 0.625f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
