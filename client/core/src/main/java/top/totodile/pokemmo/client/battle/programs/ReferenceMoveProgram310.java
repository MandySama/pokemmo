package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 惊吓（招式 ID：310）的攻击／事件阶段动画。
 *
 * <p>招式描述：用尖叫声等突然惊吓对手进行攻击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110310（名称）和 120310（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/AstonishAnimation.java
 *
 * <p>源文件 SHA-256：38d4237048b2fbeb85f9aebb03ebe3fa7aa3fcbf98557afe8477eb631231aded
 */
final class ReferenceMoveProgram310 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1507,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.8984375f,
                                                                        owner.casterHandle)),
                                                owner.scaleCombatant(
                                                        14,
                                                        2,
                                                        1,
                                                        0.032f,
                                                        0.048f,
                                                        0.19995117f,
                                                        -0.19995117f))
                                        .append(owner.focusCamera(1, 0.3f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1508,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(477))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 477, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 477, 1, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.032f, 0.048f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
