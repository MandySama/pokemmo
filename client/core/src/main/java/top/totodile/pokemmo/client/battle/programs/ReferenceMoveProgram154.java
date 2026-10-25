package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 乱抓（招式 ID：154）的攻击／事件阶段动画。
 *
 * <p>招式描述：用爪子或镰刀等抓对手进行攻击. 连续攻击2-5次.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110154（名称）和 120154（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/FurySwipesAnimation.java
 *
 * <p>源文件 SHA-256：d2f1e7d3cdb272e2acd0b9675af7de835cd50d21bfd0fbd047ef35318ebde962
 */
final class ReferenceMoveProgram154 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1423,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1423,
                                                        2,
                                                        16,
                                                        333.33334f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(318))
                                        .append(owner.emitParticles(-1, 318, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 318, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 318, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 318, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.032f, 0.048f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
