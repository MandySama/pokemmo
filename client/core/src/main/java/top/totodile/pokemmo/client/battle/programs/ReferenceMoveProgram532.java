package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 木角（招式 ID：532）的攻击／事件阶段动画。
 *
 * <p>招式描述：将角刺入, 吸取对手的养分. 可以回复给予对手伤害的一半HP.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110532（名称）和 120532（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/HornLeechAnimation.java
 *
 * <p>源文件 SHA-256：067fef8da4f3da3c0536fd5313c5ea8336b366873e736b39101e6e7f61843319
 */
final class ReferenceMoveProgram532 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline
                                                                        .waitThenParallel(0.6f),
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1489,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle),
                                                                0.2f)
                                                        .append(owner.focusCamera(1, 0.65f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .onStart(owner.preloadParticleEffect(696)),
                                                owner.launchParticles(696, 0, 9, 11, 0.5f, 360.0f),
                                                0.4f)
                                        .append(owner.emitParticles(-1, 696, 1, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .endGroup()
                                        .endGroup()
                                        .beginSequence()
                                        .waitSeconds(0.6f)
                                        .beginParallel()
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        3,
                                                        0.032f,
                                                        0.016f,
                                                        0.30004883f,
                                                        0.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1904,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1905,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 160.0f, 1280.0f)),
                                owner.adjustSoundTrack(2, 1, 160.0f, 1280.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
