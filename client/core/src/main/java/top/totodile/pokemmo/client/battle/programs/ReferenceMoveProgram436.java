package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 喷烟（招式 ID：436）的攻击／事件阶段动画。
 *
 * <p>招式描述：用熊熊烈火攻击自己周围所有的宝可梦. 有时会陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110436（名称）和 120436（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/LavaPlumeAnimation.java
 *
 * <p>源文件 SHA-256：d2fbfabf383f691c8da62dafca0a599a8a64f1c4304b2165954587b63cec892d
 */
final class ReferenceMoveProgram436 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.6f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1425,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 1, 320.0f, 800.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        2,
                                                        666.6667f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1419,
                                                        3,
                                                        16,
                                                        750.0f,
                                                        0.390625f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1535,
                                                        1,
                                                        16,
                                                        750.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(611))
                                        .append(owner.emitParticles(-1, 611, 0, 9, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 611, 1, 9, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 611, 2, 9, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .beginSequence()
                                        .waitSeconds(0.8f)
                                        .beginParallel()
                                        .append(
                                                owner.shakeCombatant(
                                                        16,
                                                        2,
                                                        4,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        0.0f)),
                                owner.tintCombatant(
                                        16, 0.75f, 0.0f, 0.8125f, EncounterMoveAnimation.color(31)))
                        .append(
                                owner.tintCombatant(
                                        16, 0.75f, 0.8125f, 0.0f, EncounterMoveAnimation.color(31)))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
