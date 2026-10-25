package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 幸运咒语（招式 ID：381）的攻击／事件阶段动画。
 *
 * <p>招式描述：向天许愿, 从而在5回合内不会被对手的攻击打中要害.
 *
 * <p>动画说明：播放参考粒子特效；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110381（名称）和 120381（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/LuckyChantAnimation.java
 *
 * <p>源文件 SHA-256：fb283051c000876b719bd1d3089f08a70d81c80ce0fdeaa149ab92b0711271ac
 */
final class ReferenceMoveProgram381 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.waitThenParallel(0.4f)
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1474,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.625f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1784,
                                                                2,
                                                                14,
                                                                0.0f,
                                                                0.859375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1509,
                                                                1,
                                                                14,
                                                                666.6667f,
                                                                0.546875f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(556))
                                                .append(owner.emitParticles(-1, 556, 0, 9, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 556, 1, 9, 8, 0.5f)),
                                        owner.tintCombatant(
                                                14,
                                                0.25f,
                                                0.0f,
                                                0.75f,
                                                EncounterMoveAnimation.color(Short.MAX_VALUE)))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintCombatant(
                                14,
                                0.25f,
                                0.75f,
                                0.0f,
                                EncounterMoveAnimation.color(Short.MAX_VALUE)));
        return result;
    }
}
