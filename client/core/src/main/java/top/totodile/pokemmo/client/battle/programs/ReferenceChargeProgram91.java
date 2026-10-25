package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 挖洞（招式 ID：91）的蓄力阶段动画。
 *
 * <p>招式描述：第1回合钻入, 第2回合攻击对手.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110091（名称）和 120091（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/DigAnimation.java
 *
 * <p>源文件 SHA-256：a68676b490cc96e90eec80d4900eb3314cc1722db184fe3f11bdf70c30b5edd5
 */
final class ReferenceChargeProgram91 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1416,
                                                                                        1,
                                                                                        14,
                                                                                        0.0f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        14,
                                                                                        666.6667f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                320.0f,
                                                                                                320.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1416,
                                                                                        1,
                                                                                        14,
                                                                                        666.6667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        14,
                                                                                        1333.3334f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                960.0f,
                                                                                                320.0f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1416,
                                                                                        1,
                                                                                        14,
                                                                                        1333.3334f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        14,
                                                                                        2000.0f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner
                                                                                        .adjustSoundTrack(
                                                                                                1,
                                                                                                1,
                                                                                                1600.0f,
                                                                                                320.0f))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .append(
                                                                                owner
                                                                                        .scaleCombatant(
                                                                                                14,
                                                                                                2,
                                                                                                3,
                                                                                                0.016f,
                                                                                                0.144f,
                                                                                                0.30004883f,
                                                                                                -0.30004883f))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                259))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 259, 0,
                                                                                        9, 8, 0.5f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 259, 1,
                                                                                        9, 8,
                                                                                        0.25f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 259, 2,
                                                                                        9, 8,
                                                                                        0.25f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 259, 3,
                                                                                        9, 8,
                                                                                        0.0f)),
                                                                owner.shakeCombatant(
                                                                        14,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.144f,
                                                                        0.0f,
                                                                        -0.30004883f),
                                                                0.6f)
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14,
                                                                        2,
                                                                        1,
                                                                        0.016f,
                                                                        0.144f,
                                                                        0.0f,
                                                                        -0.60009766f)),
                                                owner.emitParticles(-1, 259, 3, 9, 8, 0.0f),
                                                1.2f)
                                        .append(
                                                owner.shakeCombatant(
                                                        14, 2, 1, 0.016f, 0.48f, 0.0f, -6.0f)),
                                owner.emitParticles(-1, 259, 3, 9, 8, 0.0f),
                                1.8f)
                        .append(owner.setCombatantVisibility(14, 3))
                        .append(owner.shakeCombatant(14, 5, 0, 0.0f, 0.016f, 0.0f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
