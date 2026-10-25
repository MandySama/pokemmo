package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 光墙（招式 ID：113）的攻击／事件阶段动画。
 *
 * <p>招式描述：在5回合内使用神奇的墙, 减弱从对手那受到的特殊攻击的伤害.
 *
 * <p>动画说明：播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110113（名称）和 120113（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/LightScreenAnimation.java
 *
 * <p>源文件 SHA-256：e5fbb8fd1a4b5e71d13091e1a5b0d4c0b1934717311e9b38cd57eaf42c727c95
 */
final class ReferenceMoveProgram113 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.waitThenParallel(0.4f)
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                278))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                279))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 278, 1, 9, 8,
                                                                                0.375f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 278, 0, 9, 8,
                                                                                0.5f)),
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1472,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle),
                                                        0.2f)
                                                .append(owner.emitParticles(-1, 279, 0, 9, 8, 0.5f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1470,
                                                                1,
                                                                14,
                                                                833.3333f,
                                                                0.8984375f,
                                                                owner.casterHandle)),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1471,
                                                2,
                                                14,
                                                1166.6666f,
                                                0.703125f,
                                                owner.casterHandle),
                                        1.4f),
                                owner.emitParticles(-1, 278, 2, 9, 8, 0.5f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
