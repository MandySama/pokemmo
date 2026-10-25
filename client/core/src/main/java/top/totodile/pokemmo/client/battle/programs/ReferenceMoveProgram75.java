package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 飞叶快刀（招式 ID：75）的攻击／事件阶段动画。
 *
 * <p>招式描述：飞出叶片, 切斩对手进行攻击. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110075（名称）和 120075（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/RazorLeafAnimation.java
 *
 * <p>源文件 SHA-256：9d7d02b0fee37c733331d6ac0f5947bd7daee813212d9e9e63744b5530c9de55
 */
final class ReferenceMoveProgram75 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .onStart(
                                                                                owner
                                                                                        .setHealthHudHidden(
                                                                                                18,
                                                                                                true))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                238))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1718,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1415,
                                                                                        1,
                                                                                        16,
                                                                                        1166.6666f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1415,
                                                                                        2,
                                                                                        16,
                                                                                        1416.6666f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1415,
                                                                                        1,
                                                                                        16,
                                                                                        1500.0f,
                                                                                        0.78125f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 238, 2,
                                                                                        9, 8,
                                                                                        0.5f)),
                                                                owner.launchParticles(
                                                                        238, 1, 9, 11, 0.5f, 0.0f),
                                                                1.8f)
                                                        .append(owner.focusCamera(1, 0.25f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        166.66667f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        333.33334f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        500.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1411,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.78125f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        16,
                                                        916.6667f,
                                                        0.0f,
                                                        owner.casterHandle),
                                                1.92f)
                                        .append(owner.emitParticles(-1, 238, 0, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        4,
                                                        0.016f,
                                                        0.032f,
                                                        0.19995117f,
                                                        0.0f)),
                                owner.scaleCombatant(
                                        16, 2, 4, 0.016f, 0.032f, 0.100097656f, -0.100097656f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
