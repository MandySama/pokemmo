package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 杂技（招式 ID：512）的攻击／事件阶段动画。
 *
 * <p>招式描述：轻巧地攻击对手. 自己没有携带道具时, 会给予较大的伤害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵显隐；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110512（名称）和 120512（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/AcrobaticsAnimation.java
 *
 * <p>源文件 SHA-256：d7f95c9ff7a5d63c753fc4eae40f03a1fa367e07782d6245d76b130d8611fa3b
 */
final class ReferenceMoveProgram512 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1450,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 400.0f))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 0.0f, 400.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1376,
                                                                        1,
                                                                        14,
                                                                        416.66666f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        0,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle)),
                                                owner.shakeCombatant(
                                                        14, 2, 4, 0.0f, 0.032f, 2.0f, 0.0f),
                                                0.32f)
                                        .append(owner.setCombatantVisibility(14, 3))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        2,
                                                        16,
                                                        250.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(677))
                                        .append(owner.emitParticles(-1, 677, 0, 11, 8, 0.5f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.56f)
                                        .beginParallel()
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        1,
                                                        0.0f,
                                                        0.064f,
                                                        0.30004883f,
                                                        -0.30004883f))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.96f)
                                        .beginParallel()
                                        .append(owner.restoreCamera(0.4f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        1637,
                                        1,
                                        14,
                                        250.0f,
                                        0.9921875f,
                                        owner.casterHandle),
                                1.36f)
                        .append(owner.shakeCombatant(14, 2, 2, 0.0f, 0.032f, 2.0f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setCombatantVisibility(14, 4))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
