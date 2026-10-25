package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 愤怒门牙（招式 ID：162）的攻击／事件阶段动画。
 *
 * <p>招式描述：用锋利的门牙猛烈地咬住对手进行攻击. 对手的HP减半.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110162（名称）和 120162（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SuperFangAnimation.java
 *
 * <p>源文件 SHA-256：6514c7b0489ed9c03ed428a0c18b5ad7064ab579aebf3c4dce3cb3b75e3dc431
 */
final class ReferenceMoveProgram162 {

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
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        166.66667f,
                                                        0.703125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1720,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(328))
                                        .append(owner.emitParticles(-1, 328, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 328, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 328, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 328, 3, 11, 8, 0.5f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
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
