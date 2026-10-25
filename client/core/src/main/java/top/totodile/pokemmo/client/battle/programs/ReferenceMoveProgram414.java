package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 大地之力（招式 ID：414）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手脚下释放出大地之力. 有时会降低对手的特防.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵位移；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110414（名称）和 120414（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/EarthPowerAnimation.java
 *
 * <p>源文件 SHA-256：53e26e3eebc59fd433bbd6321c7170f4f28e9adcb2887a2c295bc18fa5db479e
 */
final class ReferenceMoveProgram414 {

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
                                                                                        .tintEnvironment(
                                                                                                4,
                                                                                                0,
                                                                                                0.0f,
                                                                                                0.75f,
                                                                                                0.025f))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.4f))
                                                                        .endGroup()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1418,
                                                                                        3,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.46875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        3,
                                                                                        16,
                                                                                        3000.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1417,
                                                                                        1,
                                                                                        16,
                                                                                        0.0f,
                                                                                        0.8984375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                2560.0f,
                                                                                                640.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        16,
                                                                                        3416.6667f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1475,
                                                                                        2,
                                                                                        16,
                                                                                        166.66667f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1475,
                                                                                        2,
                                                                                        16,
                                                                                        1166.6666f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1475,
                                                                                        2,
                                                                                        16,
                                                                                        2000.0f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                589))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 589, 1,
                                                                                        11, 8,
                                                                                        0.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 589, 4,
                                                                                        11, 8,
                                                                                        0.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 589, 7,
                                                                                        11, 8,
                                                                                        0.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 589, 10,
                                                                                        11, 8,
                                                                                        0.0f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 589, 13,
                                                                                        11, 8,
                                                                                        0.0f)),
                                                                owner.shakeCombatant(
                                                                        16, 2, 2, 0.016f, 0.032f,
                                                                        0.5f, 0.0f),
                                                                1.0f)
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 589, 2, 11, 8, 0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 589, 5, 11, 8, 0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 589, 8, 11, 8, 0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 589, 11, 11, 8, 0.0f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 589, 14, 11, 8, 0.0f)),
                                                owner.shakeCombatant(
                                                        16, 2, 2, 0.016f, 0.032f, 0.5f, 0.0f),
                                                2.0f)
                                        .append(owner.emitParticles(-1, 589, 0, 11, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 589, 3, 11, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 589, 6, 11, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 589, 9, 11, 8, 0.0f))
                                        .append(owner.emitParticles(-1, 589, 12, 11, 8, 0.0f)),
                                owner.shakeCombatant(16, 2, 2, 0.016f, 0.032f, 0.5f, 0.0f))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.025f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
