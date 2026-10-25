package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 草之誓约（招式 ID：520）的攻击／事件阶段动画。
 *
 * <p>招式描述：用草柱进行攻击. 如果和水组合, 威力就会提高, 周围会变成湿地.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110520（名称）和 120520（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/GrassPledgeAnimation.java
 *
 * <p>源文件 SHA-256：08b6e95708f85caf474931e771b624198eebfe0037e3f029faee6585e271d0fd
 */
final class ReferenceMoveProgram520 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                685))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.25f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1412,
                                                                                        1,
                                                                                        16,
                                                                                        166.66667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1412,
                                                                                        1,
                                                                                        16,
                                                                                        500.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1412,
                                                                                        1,
                                                                                        16,
                                                                                        833.3333f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1445,
                                                                                        2,
                                                                                        16,
                                                                                        166.66667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1445,
                                                                                        2,
                                                                                        16,
                                                                                        500.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1445,
                                                                                        2,
                                                                                        16,
                                                                                        833.3333f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                1920.0f,
                                                                                                160.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                1,
                                                                                                1920.0f,
                                                                                                16.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                2,
                                                                                                0,
                                                                                                0.0f,
                                                                                                3200.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 685, 1,
                                                                                        11, 8,
                                                                                        0.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 685, 4,
                                                                                        11, 8,
                                                                                        0.25f)),
                                                                owner.emitParticles(
                                                                        -1, 685, 7, 11, 8, 0.0f),
                                                                0.4f)
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        10,
                                                                        0.016f,
                                                                        0.032f,
                                                                        0.30004883f,
                                                                        -0.100097656f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 685, 2, 11, 8, 0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 685, 5, 11, 8, 0.25f)),
                                                owner.emitParticles(-1, 685, 8, 11, 8, 0.0f),
                                                0.8f)
                                        .append(owner.emitParticles(-1, 685, 0, 11, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 685, 3, 11, 8, 0.25f)),
                                owner.emitParticles(-1, 685, 6, 11, 8, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
