package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 出奇一击（招式 ID：185）的攻击／事件阶段动画。
 *
 * <p>招式描述：悄悄地靠近对手, 趁其不备进行殴打. 攻击必定会命中.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵显隐；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110185（名称）和 120185（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/FeintAttackAnimation.java
 *
 * <p>源文件 SHA-256：52626a9bbf05ba5eaac19a3f04e5d3bdfd4bdba0b8d3a89e61e7817a1e673baf
 */
final class ReferenceMoveProgram185 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .waitSeconds(0.6f),
                                                                owner.tintEnvironment(
                                                                        4, 0, 0.0f, 0.8125f,
                                                                        0.025f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1445,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1376,
                                                                        1,
                                                                        14,
                                                                        833.3333f,
                                                                        0.0f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 1, 480.0f, 320.0f))
                                                        .beginSequence()
                                                        .waitSeconds(0.6f)
                                                        .beginParallel()
                                                        .waitSeconds(0.6f)
                                                        .endGroup()
                                                        .endGroup()
                                                        .beginSequence()
                                                        .waitSeconds(1.2f)
                                                        .beginParallel()
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        250.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1420,
                                                                        2,
                                                                        16,
                                                                        316.66666f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(owner.setCombatantVisibility(14, 3))
                                                        .onStart(owner.preloadParticleEffect(351)),
                                                owner.emitParticles(-1, 351, 0, 11, 8, 0.4f),
                                                1.4f)
                                        .append(owner.emitParticles(-1, 351, 1, 11, 8, 0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setCombatantVisibility(14, 4))
                        .append(owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.025f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
