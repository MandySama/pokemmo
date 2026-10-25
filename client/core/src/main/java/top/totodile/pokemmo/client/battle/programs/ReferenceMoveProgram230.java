package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 甜甜香气（招式 ID：230）的攻击／事件阶段动画。
 *
 * <p>招式描述：用香气降低对手的闪避率. 在有野外分布的地方使用时可以吸引宝可梦群怪, 但会消耗更多PP.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110230（名称）和 120230（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SweetScentAnimation.java
 *
 * <p>源文件 SHA-256：4b41a2a731ba8e79255506f60d48570e20ea7cc805509266fb0504c00b9d48ca
 */
final class ReferenceMoveProgram230 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(0, 0.6f))
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1667,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.8984375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(396))
                                        .append(owner.emitParticles(-1, 396, 2, 0, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 396, 0, 0, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 396, 4, 0, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 396, 3, 0, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 396, 1, 0, 8, 0.4f))
                                        .append(owner.emitParticles(-1, 396, 4, 0, 8, 0.4f)),
                                owner.tintEnvironment(4, 22943, 0.0f, 0.375f, 0.025f),
                                1.6f)
                        .append(owner.tintEnvironment(4, 22943, 0.375f, 0.0f, 0.05f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
