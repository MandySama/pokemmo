package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 爱心印章（招式 ID：531）的攻击／事件阶段动画。
 *
 * <p>招式描述：以可爱的动作使对手疏忽, 乘机给出强烈的一击. 有时会使对手畏缩.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110531（名称）和 120531（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/HeartStampAnimation.java
 *
 * <p>源文件 SHA-256：ee4f008791fe34f6fe474728622ff358a9b2a22f6d090639c33f23238cd839cf
 */
final class ReferenceMoveProgram531 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1414,
                                                                                1,
                                                                                16,
                                                                                0.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1432,
                                                                                2,
                                                                                16,
                                                                                416.66666f,
                                                                                0.9921875f,
                                                                                owner.casterHandle)),
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1548,
                                                                1,
                                                                16,
                                                                666.6667f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(owner.focusCamera(1, 0.4f))
                                                .append(owner.setSpriteAnimationPaused(16, 1))
                                                .onStart(owner.preloadParticleEffect(695))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 695, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 695, 1, 11, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 695, 4, 11, 8, 0.5f)),
                                        owner.emitParticles(-1, 695, 0, 11, 8, 0.5f),
                                        0.6f),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.096f, -0.30004883f, 0.30004883f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
