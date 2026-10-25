package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 十字剪（招式 ID：404）的攻击／事件阶段动画。
 *
 * <p>招式描述：将镰刀或爪子像剪刀般地交叉, 顺势劈开对手.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110404（名称）和 120404（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/XScissorAnimation.java
 *
 * <p>源文件 SHA-256：3cedc98c91ea9677c24e21b1fe9624c6137e1552b2e8435e5a6d637306c96f8d
 */
final class ReferenceMoveProgram404 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1421,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1423,
                                        2,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1651,
                                        1,
                                        16,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(579))
                        .append(owner.emitParticles(-1, 579, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 579, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 579, 2, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 579, 3, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 579, 4, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.shakeCombatant(16, 2, 6, 0.016f, 0.032f, 0.19995117f, 0.0f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
