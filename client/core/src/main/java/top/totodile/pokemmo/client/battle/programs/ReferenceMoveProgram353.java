package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 破灭之愿（招式 ID：353）的攻击／事件阶段动画。
 *
 * <p>招式描述：使用招式2回合后, 会用无数道光束攻击对手.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110353（名称）和 120353（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/DoomDesireAnimation.java
 *
 * <p>源文件 SHA-256：a562b3a6bffbe922a9559df897ec1328ff1f1b60d54e455fd310b4ad5d09443e
 */
final class ReferenceMoveProgram353 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep16 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.0f, 0.8125f, 0.075f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1497,
                                        1,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1600.0f))
                        .append(owner.adjustSoundTrack(1, 1, 1280.0f, 320.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1509,
                                        2,
                                        16,
                                        1000.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        1666.6666f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        1833.3334f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        2000.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        2166.6667f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        2333.3333f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        2500.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        2666.6667f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        2833.3333f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        3000.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        3166.6667f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        3333.3333f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        1,
                                        16,
                                        3500.0f,
                                        0.8984375f,
                                        owner.casterHandle));
        Runnable sceneFlagAction2 = owner.sceneFlag(0, false);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                animationStep16
                                                                        .onStart(
                                                                                ReferenceMoveTimeline
                                                                                        .callback(
                                                                                                sceneFlagAction2))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                526))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 526, 2,
                                                                                        0, 0,
                                                                                        0.0f)),
                                                                owner.emitParticles(
                                                                        -1, 526, 0, 0, 0, 0.0f),
                                                                1.0f)
                                                        .append(owner.focusCamera(1, 0.3f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        16, 1))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        16,
                                                                        2,
                                                                        6,
                                                                        0.032f,
                                                                        0.032f,
                                                                        -0.30004883f,
                                                                        0.30004883f)),
                                                owner.emitParticles(-1, 526, 1, 11, 8, 0.5f),
                                                1.4f),
                                        owner.emitParticles(-1, 526, 1, 11, 8, 0.25f),
                                        2.0f),
                                owner.emitParticles(-1, 526, 1, 11, 8, 0.25f))
                        .onStart(ReferenceMoveTimeline.callback(owner.sceneFlag(0, true)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.8125f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.3f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
