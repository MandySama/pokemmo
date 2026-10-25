package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 花瓣舞（招式 ID：80）的攻击／事件阶段动画。
 *
 * <p>招式描述：在2-3回合内, 散落花瓣攻击对手. 之后自己会陷入混乱.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110080（名称）和 120080（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/PetalDanceAnimation.java
 *
 * <p>源文件 SHA-256：70f7dfa6760fd3811e45ee76f9562983047e3221470014abefe803856e7063f6
 */
final class ReferenceMoveProgram80 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .onStart(owner.preloadParticleEffect(244))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1445,
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
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 640.0f, 320.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1,
                                                                        244,
                                                                        0,
                                                                        9,
                                                                        8,
                                                                        0.049987793f)),
                                                owner.emitParticles(-1, 244, 1, 9, 8, 0.049987793f),
                                                0.64f)
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1497,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1280.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 320.0f))
                                        .append(owner.emitParticles(-1, 244, 0, 11, 8, 0.0f))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 244, 1, 11, 8, 0.049987793f)),
                                owner.shakeCombatant(16, 2, 12, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
