package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 防御气场（自定义动画注册 ID：1034）的攻击／事件阶段动画。
 *
 * <p>招式描述：竖起抵御伤害的屏障. 会替换掉已在场的所有屏障.
 *
 * <p>动画说明：播放参考粒子特效；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 111034（名称）和 121034（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/LightScreenAnimation.java
 *
 * <p>源文件 SHA-256：e5fbb8fd1a4b5e71d13091e1a5b0d4c0b1934717311e9b38cd57eaf42c727c95
 */
final class ReferenceMoveProgram1034 {

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
