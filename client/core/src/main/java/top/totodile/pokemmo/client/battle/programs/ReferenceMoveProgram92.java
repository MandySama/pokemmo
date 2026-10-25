package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 剧毒（招式 ID：92）的攻击／事件阶段动画。
 *
 * <p>招式描述：让对手陷入剧毒状态. 随着回合的推进, 中毒伤害会增加.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110092（名称）和 120092（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ToxicAnimation.java
 *
 * <p>源文件 SHA-256：e8db4a0acfee245e0b9f5db2541cca6a9b459bbcf93c5f664f8f283dcea1100f
 */
final class ReferenceMoveProgram92 {

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
                                                        (short) 1480,
                                                        2,
                                                        16,
                                                        166.66667f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1480,
                                                        1,
                                                        16,
                                                        666.6667f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 480.0f, 640.0f))
                                        .append(owner.adjustSoundTrack(1, 1, 480.0f, 640.0f))
                                        .onStart(owner.preloadParticleEffect(261))
                                        .append(
                                                owner.emitParticles(
                                                        -1, 261, 0, 11, 8, 0.049987793f))
                                        .append(owner.emitParticles(-1, 261, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 261, 2, 11, 8, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
