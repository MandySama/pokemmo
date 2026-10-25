package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 气象球（招式 ID：311）的攻击／事件阶段动画。
 *
 * <p>招式描述：根据使用时的天气, 招式属性和威力会改变.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110311（名称）和 120311（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/WeatherBallAnimation.java
 *
 * <p>源文件 SHA-256：36efa8fd46ea9f5802f0f5311931551a88d3dfccb6c3cadf9f9a0e2da6eabef9
 */
final class ReferenceMoveProgram311 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenNextParallel(
                                                                        ReferenceMoveTimeline
                                                                                .sequence()
                                                                                .beginParallel()
                                                                                .append(
                                                                                        owner
                                                                                                .focusCamera(
                                                                                                        0,
                                                                                                        0.4f))
                                                                                .onStart(
                                                                                        owner
                                                                                                .preloadParticleEffect(
                                                                                                        478))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1445,
                                                                                                        1,
                                                                                                        14,
                                                                                                        0.0f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1376,
                                                                                                        1,
                                                                                                        14,
                                                                                                        1000.0f,
                                                                                                        0.0f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .adjustSoundTrack(
                                                                                                        1,
                                                                                                        0,
                                                                                                        0.0f,
                                                                                                        800.0f))
                                                                                .append(
                                                                                        owner
                                                                                                .adjustSoundTrack(
                                                                                                        1,
                                                                                                        1,
                                                                                                        640.0f,
                                                                                                        320.0f))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1445,
                                                                                                        1,
                                                                                                        16,
                                                                                                        1083.3334f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1376,
                                                                                                        1,
                                                                                                        16,
                                                                                                        2083.3333f,
                                                                                                        0.0f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .adjustSoundTrack(
                                                                                                        1,
                                                                                                        0,
                                                                                                        1040.0f,
                                                                                                        800.0f))
                                                                                .append(
                                                                                        owner
                                                                                                .adjustSoundTrack(
                                                                                                        1,
                                                                                                        1,
                                                                                                        1680.0f,
                                                                                                        320.0f))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        (short)
                                                                                                                1471,
                                                                                                        2,
                                                                                                        16,
                                                                                                        1666.6666f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        478,
                                                                                                        4,
                                                                                                        9,
                                                                                                        8,
                                                                                                        0.5f)),
                                                                        owner.emitParticles(
                                                                                -1, 478, 2, 9, 8,
                                                                                0.5f))
                                                                .append(owner.focusCamera(1, 0.4f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 478, 3, 11, 8,
                                                                                0.75f)),
                                                        owner.emitParticles(
                                                                -1, 478, 1, 11, 8, 0.75f),
                                                        0.4f),
                                                owner.emitParticles(-1, 478, 0, 11, 8, 0.5f),
                                                1.0f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.0f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
