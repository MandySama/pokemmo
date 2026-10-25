package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 青草搅拌器（招式 ID：536）的攻击／事件阶段动画。
 *
 * <p>招式描述：用锋利的叶片包裹住对手进行攻击. 有时会降低对手的命中率.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110536（名称）和 120536（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/LeafTornadoAnimation.java
 *
 * <p>源文件 SHA-256：96370908a3ef61314bb303a14610068017509ec441e55c6c5f5f6c7777bdbc5a
 */
final class ReferenceMoveProgram536 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline
                                                                        .waitThenParallel(0.6f)
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1445,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                0,
                                                                                                0.0f,
                                                                                                960.0f))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                800.0f,
                                                                                                160.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        1376,
                                                                                        1,
                                                                                        14,
                                                                                        1000.0f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                14,
                                                                                                1))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                700))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1,
                                                                                        700,
                                                                                        1,
                                                                                        9,
                                                                                        8,
                                                                                        0.049987793f)),
                                                                owner.emitParticles(
                                                                        -1,
                                                                        700,
                                                                        1,
                                                                        9,
                                                                        8,
                                                                        0.049987793f),
                                                                0.64f)
                                                        .onStart(owner.setHealthHudHidden(18, true))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 700, 1, 11, 8, 0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1,
                                                                        700,
                                                                        1,
                                                                        11,
                                                                        8,
                                                                        0.049987793f)),
                                                owner.emitParticles(
                                                        -1, 700, 2, 11, 8, 0.049987793f),
                                                1.04f)
                                        .append(owner.emitParticles(-1, 700, 0, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1445,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 1280.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 800.0f, 480.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        16,
                                                        1333.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1568,
                                                        1,
                                                        16,
                                                        666.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.shakeCombatant(16, 2, 12, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
