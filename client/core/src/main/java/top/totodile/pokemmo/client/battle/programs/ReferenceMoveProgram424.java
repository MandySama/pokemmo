package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 火焰牙（招式 ID：424）的攻击／事件阶段动画。
 *
 * <p>招式描述：用覆盖着火焰的牙齿咬住对手. 有时会使对手畏缩或陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110424（名称）和 120424（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/FireFangAnimation.java
 *
 * <p>源文件 SHA-256：f50a15b404ed80f5502d6d9512546ad9c1f0b3a9574c20d4e4c107823da15697
 */
final class ReferenceMoveProgram424 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1720,
                                        1,
                                        16,
                                        333.33334f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1426,
                                        2,
                                        16,
                                        416.66666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(599))
                        .append(owner.emitParticles(-1, 599, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 599, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 599, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 599, 3, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 599, 4, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 599, 5, 11, 8, 0.5f))
                        .beginSequence()
                        .waitSeconds(0.2f)
                        .beginParallel()
                        .append(owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
