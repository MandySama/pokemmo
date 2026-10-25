package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 自我再生（招式 ID：105）的攻击／事件阶段动画。
 *
 * <p>招式描述：让细胞再生, 从而回复自己最大HP的一半.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110105（名称）和 120105（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/RecoverAnimation.java
 *
 * <p>源文件 SHA-256：4a55e7b7c7419f6747e37858ee141de3428962fb24f0739e1ca54f0b3531b2b8
 */
final class ReferenceMoveProgram105 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1786,
                                                                        1,
                                                                        14,
                                                                        600.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1358,
                                                                        2,
                                                                        14,
                                                                        1250.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1358,
                                                                        2,
                                                                        14,
                                                                        1500.0f,
                                                                        0.46875f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1358,
                                                        2,
                                                        14,
                                                        1750.0f,
                                                        0.15625f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(271))
                                        .append(owner.emitParticles(-1, 271, 0, 9, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 271, 1, 9, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
