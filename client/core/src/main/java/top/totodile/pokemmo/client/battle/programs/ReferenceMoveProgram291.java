package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 潜水（招式 ID：291）的攻击／事件阶段动画。
 *
 * <p>招式描述：第1回合潜入, 第2回合浮上来进行攻击.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵显隐；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110291（名称）和 120291（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/DiveAnimation.java
 *
 * <p>源文件 SHA-256：07431c741895873fae43353810c4574edafe4b061eb3dd64060820a1615241b7
 */
final class ReferenceMoveProgram291 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.3f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.shakeCombatant(
                                                        14, 1, 0, 0.0f, 0.0f, 0.0f, -20.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1686,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1407,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 800.0f, 160.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        1166.6666f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 480.0f, 480.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 480.0f, 480.0f))
                                        .onStart(owner.preloadParticleEffect(458))
                                        .append(owner.emitParticles(-1, 458, 0, 11, 8, 0.4f)),
                                owner.emitParticles(-1, 458, 1, 11, 8, 0.4f),
                                0.4f)
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.shakeCombatant(14, 1, 0, 0.016f, 0.48f, 0.0f, 20.0f))
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .beginSequence()
                        .waitSeconds(0.4f)
                        .beginParallel()
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
