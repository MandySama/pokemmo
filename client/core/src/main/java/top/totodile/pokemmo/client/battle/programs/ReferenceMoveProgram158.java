package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 终结门牙（招式 ID：158）的攻击／事件阶段动画。
 *
 * <p>招式描述：用锋利的门牙牢牢地咬住对手进行攻击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110158（名称）和 120158（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/HyperFangAnimation.java
 *
 * <p>源文件 SHA-256：51e00c91c031fb226c7b803b6b60b317c629daddf501f8ccc94c3b6a57555d7a
 */
final class ReferenceMoveProgram158 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.focusCamera(1, 0.4f))
                                        .endGroup()
                                        .beginParallel()
                                        .onStart(owner.preloadParticleEffect(322))
                                        .append(owner.emitParticles(-1, 322, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 322, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 322, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 322, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 322, 4, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1423,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
