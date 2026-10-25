package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 祸不单行（招式 ID：506）的攻击／事件阶段动画。
 *
 * <p>招式描述：接二连三地进行攻击. 对处于异常状态的对手给予较大的伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110506（名称）和 120506（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/HexAnimation.java
 *
 * <p>源文件 SHA-256：da5bb8667f01511daec265f71c7855b67f37683a788a91ad17a7c3d8046c678d
 */
final class ReferenceMoveProgram506 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14, 2, 3, 0.0f, 0.048f,
                                                                        1.0f, 0.0f))
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
                                                                        1, 1, 112.0f, 160.0f)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        14,
                                                        333.33334f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .onStart(owner.preloadParticleEffect(672))
                                        .append(owner.emitParticles(-1, 672, 1, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1446,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.15625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1420,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 10, 0.0f, 0.032f, 0.19995117f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
