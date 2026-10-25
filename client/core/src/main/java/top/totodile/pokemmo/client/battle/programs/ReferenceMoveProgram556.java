package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰柱坠击（招式 ID：556）的攻击／事件阶段动画。
 *
 * <p>招式描述：用大冰柱激烈地撞向对手进行攻击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110556（名称）和 120556（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/IcicleCrashAnimation.java
 *
 * <p>源文件 SHA-256：f689f90512b166c3ed6d7bc920e4a657be22af8b65ee97e5e6f4db7ced82bd84
 */
final class ReferenceMoveProgram556 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .onStart(
                                                                                owner
                                                                                        .setHealthHudHidden(
                                                                                                18,
                                                                                                true))
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        1, 0.3f))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1945,
                                                                                        1,
                                                                                        16,
                                                                                        333.33334f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1376,
                                                                                        1,
                                                                                        16,
                                                                                        1000.0f,
                                                                                        0.0f,
                                                                                        owner.casterHandle))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1652,
                                                                                        2,
                                                                                        16,
                                                                                        666.6667f,
                                                                                        0.9921875f,
                                                                                        owner.casterHandle)),
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1652,
                                                                        1,
                                                                        16,
                                                                        1000.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .onStart(owner.preloadParticleEffect(727))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 727, 0, 11, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 727, 1, 11, 8, 0.5f)),
                                                owner.emitParticles(-1, 727, 2, 11, 8, 0.5f),
                                                0.2f)
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 6, 0.0f, 0.048f, 0.30004883f, -0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
