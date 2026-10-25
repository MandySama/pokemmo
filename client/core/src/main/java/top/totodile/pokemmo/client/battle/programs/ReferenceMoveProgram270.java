package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 帮助（招式 ID：270）的攻击／事件阶段动画。
 *
 * <p>招式描述：帮助伙伴. 被帮助的宝可梦, 其招式威力变得比平时大.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110270（名称）和 120270（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/HelpingHandAnimation.java
 *
 * <p>源文件 SHA-256：cd0199ae9eabcb1e33dfbd9163f2789293d609bb672838a60c58af9e7c057756
 */
final class ReferenceMoveProgram270 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f))
                                                                        .append(
                                                                                owner
                                                                                        .setSpriteAnimationPaused(
                                                                                                14,
                                                                                                1))
                                                                        .append(
                                                                                owner.scheduleSound(
                                                                                        (byte) 2,
                                                                                        (short)
                                                                                                1422,
                                                                                        1,
                                                                                        14,
                                                                                        333.33334f,
                                                                                        0.9375f,
                                                                                        owner.casterHandle))
                                                                        .onStart(
                                                                                owner
                                                                                        .preloadParticleEffect(
                                                                                                436))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 436, 0,
                                                                                        9, 8,
                                                                                        0.75f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 436, 1,
                                                                                        9, 8,
                                                                                        0.75f))
                                                                        .append(
                                                                                owner.emitParticles(
                                                                                        -1, 436, 6,
                                                                                        9, 8,
                                                                                        0.75f)),
                                                                owner.shakeCombatant(
                                                                        14, 2, 1, 0.0f, 0.096f,
                                                                        1.0f, 0.0f),
                                                                0.4f)
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1422,
                                                                        1,
                                                                        14,
                                                                        333.33334f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 436, 3, 9, 8, 0.75f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 436, 4, 9, 8, 0.75f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 436, 6, 9, 8, 0.75f)),
                                                owner.shakeCombatant(
                                                        14, 2, 1, 0.0f, 0.096f, 1.0f, 0.0f),
                                                0.8f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1422,
                                                        1,
                                                        14,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 436, 5, 9, 8, 0.75f))
                                        .append(owner.emitParticles(-1, 436, 2, 9, 8, 0.75f))
                                        .append(owner.emitParticles(-1, 436, 6, 9, 8, 0.75f)),
                                owner.shakeCombatant(14, 2, 1, 0.0f, 0.096f, 1.0f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
