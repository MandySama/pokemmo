package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 碎岩（招式 ID：249）的攻击／事件阶段动画。
 *
 * <p>招式描述：用拳头进行攻击. 有时会降低对手的防御.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110249（名称）和 120249（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/RockSmashAnimation.java
 *
 * <p>源文件 SHA-256：9aca43c2856320623947184554ab6d3555bbd6d7de83399fe42e5fbdc93ddb8a
 */
final class ReferenceMoveProgram249 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1460,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 48.0f, 128.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1885,
                                                        2,
                                                        16,
                                                        183.33333f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1488,
                                                        1,
                                                        16,
                                                        283.33334f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(415))
                                        .append(owner.emitParticles(-1, 415, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 415, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, 0.39990234f, -0.39990234f))
                        .append(owner.restoreCamera(0.25f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
