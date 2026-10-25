package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 龙之俯冲（招式 ID：407）的攻击／事件阶段动画。
 *
 * <p>招式描述：释放出骇人的杀气, 一边威慑一边撞击对手. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110407（名称）和 120407（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/DragonRushAnimation.java
 *
 * <p>源文件 SHA-256：4b3fe81e74f5325a9b294aeca76a9ceb292ddad9cc9f6a57639aa2c494891345
 */
final class ReferenceMoveProgram407 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1438,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1475,
                                                        2,
                                                        16,
                                                        250.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(582))
                                        .append(owner.emitParticles(-1, 582, 0, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 582, 1, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 582, 2, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 582, 3, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 582, 4, 11, 8, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.064f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
