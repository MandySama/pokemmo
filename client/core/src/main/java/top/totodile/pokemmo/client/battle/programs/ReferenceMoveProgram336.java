package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 长嚎（招式 ID：336）的攻击／事件阶段动画。
 *
 * <p>招式描述：大声吼叫提高气势, 从而提高自己和同伴的攻击.
 *
 * <p>动画说明：播放参考粒子特效；执行精灵缩放。
 *
 * <p>编号对应参考动画注册表；语言条目为 110336（名称）和 120336（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/HowlAnimation.java
 *
 * <p>源文件 SHA-256：b76e0863c40192d5dad9ff019c9a60e0658a93ffbdcd5c63a41329fccead0a15
 */
final class ReferenceMoveProgram336 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.playPokemonCry(false, 0.0f))
                        .endGroup()
                        .beginParallel()
                        .onStart(owner.preloadParticleEffect(506))
                        .append(owner.emitParticles(-1, 506, 0, 9, 8, 0.4f))
                        .append(owner.emitParticles(-1, 506, 1, 9, 8, 0.4f))
                        .append(
                                owner.scaleCombatant(
                                        14, 2, 1, 0.032f, 0.144f, -0.39990234f, 0.39990234f))
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
