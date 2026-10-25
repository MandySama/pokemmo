package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 雪景（招式 ID：258）的攻击／事件阶段动画。
 *
 * <p>招式描述：在5回合内会下雪. 冰属性的防御提高50%.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110258（名称）和 120258（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/HailAnimation.java
 *
 * <p>源文件 SHA-256：56ac46f991e7f3d2763595a1c251332e1ad55427cda877f847f04718407a0e7b
 */
final class ReferenceMoveProgram258 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel(),
                                                        owner.tintEnvironment(
                                                                4, 0, 0.0f, 0.8125f, 0.075f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1820,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.78125f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                1, 1, 320.0f, 320.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1700,
                                                                2,
                                                                14,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1700,
                                                                2,
                                                                14,
                                                                500.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1700,
                                                                2,
                                                                14,
                                                                1000.0f,
                                                                0.625f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(424))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 424, 0, 0, 0, 1.25f)),
                                        owner.emitParticles(-1, 424, 2, 0, 0, 1.5f),
                                        0.4f),
                                owner.emitParticles(-1, 424, 1, 0, 0, 1.5f)),
                        owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.075f));
        return result;
    }
}
