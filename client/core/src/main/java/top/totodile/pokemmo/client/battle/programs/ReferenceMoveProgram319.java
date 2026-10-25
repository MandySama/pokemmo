package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 金属音（招式 ID：319）的攻击／事件阶段动画。
 *
 * <p>招式描述：让对手听摩擦金属般讨厌的声音. 大幅降低对手的特防.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110319（名称）和 120319（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/MetalSoundAnimation.java
 *
 * <p>源文件 SHA-256：5b30f98d67e7909842049aca4facfeef50454053d31fed1ea26f7c312c2acf75
 */
final class ReferenceMoveProgram319 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .onStart(
                                                                                owner
                                                                                        .setHealthHudHidden(
                                                                                                18,
                                                                                                true))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.3f)),
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.0f, 0.75f, 0.1f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1870,
                                                                        1,
                                                                        14,
                                                                        166.66667f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1870,
                                                                        1,
                                                                        14,
                                                                        300.0f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1870,
                                                                        1,
                                                                        14,
                                                                        433.33334f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1870,
                                                                        1,
                                                                        14,
                                                                        566.6667f,
                                                                        0.625f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1870,
                                                                        1,
                                                                        16,
                                                                        700.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1836,
                                                                        0,
                                                                        16,
                                                                        700.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1435,
                                                                        2,
                                                                        14,
                                                                        166.66667f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        2, 1, 640.0f, 480.0f))
                                                        .onStart(owner.preloadParticleEffect(489)),
                                                owner.launchParticles(489, 0, 9, 11, 0.5f, 552.0f),
                                                0.3f)
                                        .append(owner.focusCamera(1, 0.25f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.6f)
                                        .beginParallel(),
                                owner.emitParticles(-1, 489, 1, 11, 8, 0.5f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.1f))
                        .append(owner.restoreCamera(0.3f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
