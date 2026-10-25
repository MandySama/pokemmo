package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 旋风刀（招式 ID：13）的攻击／事件阶段动画。
 *
 * <p>招式描述：制造风之刃, 于第2回合攻击对手. 容易击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110013（名称）和 120013（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/RazorWindAnimation.java
 *
 * <p>源文件 SHA-256：3a104a965fe651634f7e5f1048ede76209971ea9bffcb47c3b63f2076ba6cfed
 */
final class ReferenceMoveProgram13 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        2,
                                                        16,
                                                        83.333336f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        2,
                                                        16,
                                                        250.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        16,
                                                        333.33334f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        2,
                                                        16,
                                                        416.66666f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 480.0f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .onStart(owner.preloadParticleEffect(174))
                                        .append(owner.emitParticles(-1, 174, 0, 11, 8, 0.5f)),
                                owner.shakeCombatant(16, 2, 5, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
