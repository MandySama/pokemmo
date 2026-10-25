package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 掉包（招式 ID：415）的攻击／事件阶段动画。
 *
 * <p>招式描述：用一闪而过的速度交换自己和对手的持有物.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110415（名称）和 120415（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/SwitcherooAnimation.java
 *
 * <p>源文件 SHA-256：77ce87e50a7721b116579e5eeccc2dc44bba961e3b536d19ea81d5ecb3ae8e0a
 */
final class ReferenceMoveProgram415 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1))
                                                                .append(
                                                                        owner.scaleCombatant(
                                                                                14,
                                                                                3,
                                                                                1,
                                                                                0.016f,
                                                                                0.032f,
                                                                                0.0f,
                                                                                0.30004883f)),
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1452,
                                                                2,
                                                                14,
                                                                0.0f,
                                                                0.8984375f,
                                                                owner.casterHandle),
                                                        0.24f),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f),
                                                0.48f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1497,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        1,
                                                        14,
                                                        333.33334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 320.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1394,
                                                        1,
                                                        2,
                                                        333.33334f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1394,
                                                        1,
                                                        2,
                                                        666.6667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1560,
                                                        2,
                                                        2,
                                                        1083.3334f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1560,
                                                        2,
                                                        2,
                                                        1583.3334f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1560,
                                                        2,
                                                        2,
                                                        2166.6667f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 2240.0f, 480.0f))
                                        .onStart(owner.preloadParticleEffect(590))
                                        .append(owner.emitParticles(-1, 590, 0, 0, 0, 0.0f))
                                        .append(owner.emitParticles(-1, 590, 1, 0, 0, 0.0f))
                                        .append(owner.emitParticles(-1, 590, 2, 0, 0, 0.0f))
                                        .append(owner.emitParticles(-1, 590, 3, 0, 0, 0.0f)),
                                owner.emitParticles(-1, 590, 4, 0, 0, 0.0f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
