package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 挖洞（招式 ID：91）的攻击／事件阶段动画。
 *
 * <p>招式描述：第1回合钻入, 第2回合攻击对手.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵显隐；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110091（名称）和 120091（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DigAnimation.java
 *
 * <p>源文件 SHA-256：a68676b490cc96e90eec80d4900eb3314cc1722db184fe3f11bdf70c30b5edd5
 */
final class ReferenceMoveProgram91 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline
                                                                        .appendThenDelayedParallel(
                                                                                ReferenceMoveTimeline
                                                                                        .sequence()
                                                                                        .beginParallel()
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1475,
                                                                                                                1,
                                                                                                                16,
                                                                                                                83.333336f,
                                                                                                                0.9921875f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1416,
                                                                                                                1,
                                                                                                                14,
                                                                                                                1083.3334f,
                                                                                                                0.9921875f,
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
                                                                                                                1666.6666f,
                                                                                                                0.0f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .adjustSoundTrack(
                                                                                                                1,
                                                                                                                1,
                                                                                                                1280.0f,
                                                                                                                304.0f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1416,
                                                                                                                1,
                                                                                                                14,
                                                                                                                1750.0f,
                                                                                                                0.9921875f,
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
                                                                                                                2416.6667f,
                                                                                                                0.0f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .adjustSoundTrack(
                                                                                                                1,
                                                                                                                1,
                                                                                                                1920.0f,
                                                                                                                384.0f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .scheduleSound(
                                                                                                                (byte)
                                                                                                                        2,
                                                                                                                (short)
                                                                                                                        1416,
                                                                                                                1,
                                                                                                                14,
                                                                                                                2416.6667f,
                                                                                                                0.9921875f,
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
                                                                                                                3250.0f,
                                                                                                                0.0f,
                                                                                                                owner.casterHandle))
                                                                                        .append(
                                                                                                owner
                                                                                                        .adjustSoundTrack(
                                                                                                                1,
                                                                                                                1,
                                                                                                                2560.0f,
                                                                                                                560.0f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .focusCamera(
                                                                                                                1,
                                                                                                                0.4f))
                                                                                        .append(
                                                                                                owner
                                                                                                        .setSpriteAnimationPaused(
                                                                                                                16,
                                                                                                                1))
                                                                                        .onStart(
                                                                                                owner
                                                                                                        .preloadParticleEffect(
                                                                                                                260))
                                                                                        .append(
                                                                                                owner
                                                                                                        .emitParticles(
                                                                                                                -1,
                                                                                                                260,
                                                                                                                0,
                                                                                                                11,
                                                                                                                8,
                                                                                                                0.5f)),
                                                                                owner
                                                                                        .scaleCombatant(
                                                                                                16,
                                                                                                2,
                                                                                                1,
                                                                                                0.032f,
                                                                                                0.032f,
                                                                                                0.30004883f,
                                                                                                -0.30004883f),
                                                                                0.6f)
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .endGroup()
                                                                        .endGroup()
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                16,
                                                                                                0))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 260, 1,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 260, 2,
                                                                                        9, 8,
                                                                                        0.25f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 260, 3,
                                                                                        9, 8,
                                                                                        0.25f)),
                                                                owner.emitParticles(
                                                                        -1, 260, 4, 9, 8, 0.0f),
                                                                0.4f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 260, 4, 9, 8, 0.0f)),
                                                owner.shakeCombatant(
                                                        14, 2, 1, 0.016f, 0.256f, 0.0f, -6.0f),
                                                0.8f)
                                        .append(owner.setCombatantVisibility(14, 4)),
                                owner.emitParticles(-1, 260, 4, 9, 8, 0.0f),
                                1.8f)
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
