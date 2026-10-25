package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 金属爆炸（招式 ID：368）的攻击／事件阶段动画。
 *
 * <p>招式描述：使出招式前, 将最后受到的招式的伤害大力返还给对手.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110368（名称）和 120368（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/MetalBurstAnimation.java
 *
 * <p>源文件 SHA-256：c8a532b2efa31ef38ecd46d875a4eb1df00195fc4fc56c9138df7993124641b1
 */
final class ReferenceMoveProgram368 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenNextParallel(
                                                                        ReferenceMoveTimeline
                                                                                .sequence()
                                                                                .beginParallel()
                                                                                .append(
                                                                                        owner
                                                                                                .focusCamera(
                                                                                                        0,
                                                                                                        0.6f)),
                                                                        owner.tintEnvironment(
                                                                                4, 0, 0.0f, 0.8125f,
                                                                                0.025f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1714,
                                                                                2,
                                                                                14,
                                                                                0.0f,
                                                                                0.859375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                2, 0, 240.0f,
                                                                                320.0f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1516,
                                                                                1,
                                                                                14,
                                                                                666.6667f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1775,
                                                                                1,
                                                                                16,
                                                                                833.3333f,
                                                                                0.859375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1424,
                                                                                2,
                                                                                16,
                                                                                833.3333f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                542))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 542, 0, 9, 8,
                                                                                0.4f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 542, 2, 9, 8,
                                                                                0.4f)),
                                                        owner.emitParticles(-1, 542, 3, 9, 8, 0.4f),
                                                        0.8f)
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .append(
                                                        owner.shakeCombatant(
                                                                16,
                                                                2,
                                                                6,
                                                                0.016f,
                                                                0.032f,
                                                                0.19995117f,
                                                                0.0f)),
                                        owner.emitParticles(-1, 542, 1, 11, 8, 0.4f))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .waitSeconds(0.6f),
                        owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.025f));
        return result;
    }
}
