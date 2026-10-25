package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 化为己用（招式 ID：289）的攻击／事件阶段动画。
 *
 * <p>招式描述：将对手打算使用的回复招式或能力变化招式夺为己用.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110289（名称）和 120289（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SnatchAnimation.java
 *
 * <p>源文件 SHA-256：333ee1ee752cde6e6d911d8f05c9814f94e495bf3da06a71b7231bd42854a660
 */
final class ReferenceMoveProgram289 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline
                                                                        .waitThenParallel(0.6f)
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1452,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1510,
                                                                                        1,
                                                                                        16,
                                                                                        666.6667f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1420,
                                                                                        1,
                                                                                        16,
                                                                                        1000.0f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1543,
                                                                                        2,
                                                                                        2,
                                                                                        1166.6666f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                456))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 456, 0,
                                                                                        9, 8,
                                                                                        0.25f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 456, 2,
                                                                                        9, 8,
                                                                                        0.25f)),
                                                                owner.emitParticles(
                                                                        -1, 456, 3, 9, 8, 0.25f),
                                                                0.12f)
                                                        .append(owner.setCombatantVisibility(14, 3))
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(0.68f)
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f)),
                                                owner.launchParticles(456, 1, 9, 11, 0.25f, 240.0f),
                                                1.06f)
                                        .append(owner.restoreCamera(0.4f)),
                                owner.launchParticles(456, 1, 11, 9, 0.25f, 168.0f),
                                1.38f)
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
