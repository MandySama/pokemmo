package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 终焉之歌（招式 ID：195）的攻击／事件阶段动画。
 *
 * <p>招式描述：倾听歌声的宝可梦经过3回合陷入濒死. 替换后效果消失.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110195（名称）和 120195（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/PerishSongAnimation.java
 *
 * <p>源文件 SHA-256：747df5d1622714f11a821398e6a2b525fefc8efb4f2cbeb36e4f9e90dfdafbc6
 */
final class ReferenceMoveProgram195 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.setSpriteAnimationPaused(14, 1))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1533,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1505,
                                                        2,
                                                        14,
                                                        1083.3334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(361))
                                        .append(owner.emitParticles(-1, 361, 1, 0, 0, 1.25f))
                                        .append(owner.emitParticles(-1, 361, 0, 0, 0, 1.0f))
                                        .append(owner.emitParticles(-1, 361, 2, 0, 0, 1.0f))
                                        .append(owner.emitParticles(-1, 361, 3, 0, 0, 1.0f)),
                                owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.05f),
                                1.8f)
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.05f))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
