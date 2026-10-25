package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 震级（招式 ID：222）的攻击／事件阶段动画。
 *
 * <p>招式描述：晃动地面, 攻击自己周围所有的宝可梦. 招式的威力会有各种变化.
 *
 * <p>动画说明：镜头聚焦施术者；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110222（名称）和 120222（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/MagnitudeAnimation.java
 *
 * <p>源文件 SHA-256：b1966db70684db4237e683356a7f6bac7534e5d487a43f7d0e549e55adb69b50
 */
final class ReferenceMoveProgram222 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.6f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1417,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1418,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        1,
                                                        16,
                                                        1833.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1376,
                                                        2,
                                                        16,
                                                        1833.3334f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 960.0f, 800.0f))
                                        .append(owner.adjustSoundTrack(2, 1, 960.0f, 800.0f))
                                        .append(owner.setSpriteAnimationPaused(14, 1)),
                                owner.shakeCombatant(
                                        16, 2, 12, 0.032f, 0.016f, 0.30004883f, 0.30004883f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
