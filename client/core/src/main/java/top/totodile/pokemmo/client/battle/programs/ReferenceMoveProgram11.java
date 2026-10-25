package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 夹住（招式 ID：11）的攻击／事件阶段动画。
 *
 * <p>招式描述：将对手从两侧夹住, 给予伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110011（名称）和 120011（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ViceGripAnimation.java
 *
 * <p>源文件 SHA-256：73c3dd507638fd61a21926eb2227f94071c3f58f68f0144c1aca8c0d6e97e3f9
 */
final class ReferenceMoveProgram11 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(171))
                                        .append(owner.emitParticles(-1, 171, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 171, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 171, 2, 11, 8, 0.5f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1424,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, -0.30004883f, 0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
