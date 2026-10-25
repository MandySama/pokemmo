package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 点到为止（招式 ID：206）的攻击／事件阶段动画。
 *
 * <p>招式描述：对手的HP至少会留下1HP, 如此般手下留情地攻击.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110206（名称）和 120206（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/FalseSwipeAnimation.java
 *
 * <p>源文件 SHA-256：cd57d6af6a11ea9d6c1840ab09ec0a2d90212e7af79c82d5639e82359913e0d7
 */
final class ReferenceMoveProgram206 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1475,
                                        2,
                                        16,
                                        166.66667f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 160.0f, 240.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        16,
                                        500.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1423,
                                        1,
                                        16,
                                        0.0f,
                                        0.3125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        166.66667f,
                                        0.0f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(372))
                        .append(owner.emitParticles(-1, 372, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 372, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 372, 0, 11, 8, 0.5f))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.048f, 0.19995117f, -0.19995117f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
