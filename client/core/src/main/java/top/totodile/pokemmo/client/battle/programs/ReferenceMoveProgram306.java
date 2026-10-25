package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 撕裂爪（招式 ID：306）的攻击／事件阶段动画。
 *
 * <p>招式描述：用坚硬的锐爪劈开对手进行攻击. 有时会降低对手的防御.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110306（名称）和 120306（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/CrushClawAnimation.java
 *
 * <p>源文件 SHA-256：20651885eb2ce1534b3006717af1bd479b7d0775e6af42678ba739641fa0e317
 */
final class ReferenceMoveProgram306 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        1,
                                                                        16,
                                                                        83.333336f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1423,
                                                                        2,
                                                                        16,
                                                                        533.3333f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .onStart(owner.preloadParticleEffect(472))
                                                        .onStart(owner.preloadParticleEffect(473))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 472, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 472, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 473, 0, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 473, 1, 11, 8, 0.5f),
                                                0.2f),
                                        owner.shakeCombatant(
                                                16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f),
                                        0.72f),
                                owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
