package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 锁定（招式 ID：199）的攻击／事件阶段动画。
 *
 * <p>招式描述：紧紧瞄准对手, 下次攻击必定会打中.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110199（名称）和 120199（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/LockOnAnimation.java
 *
 * <p>源文件 SHA-256：7bbba56b468d7ff04355f81ca0672285b86997d41dc8fee85d73dec2d6bd4516
 */
final class ReferenceMoveProgram199 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f))
                                                        .append(
                                                                owner.setSpriteAnimationPaused(
                                                                        14, 1))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1534,
                                                                        1,
                                                                        16,
                                                                        0.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1534,
                                                                        1,
                                                                        16,
                                                                        500.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1534,
                                                                        1,
                                                                        16,
                                                                        1000.0f,
                                                                        0.859375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1461,
                                                                        2,
                                                                        16,
                                                                        1166.6666f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(365))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 365, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 365, 1, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 365, 2, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 365, 3, 11, 8, 0.5f),
                                                1.2f),
                                        owner.tintEnvironment(
                                                4, Short.MAX_VALUE, 0.0f, 0.75f, 0.05f),
                                        1.28f),
                                owner.tintEnvironment(4, Short.MAX_VALUE, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
