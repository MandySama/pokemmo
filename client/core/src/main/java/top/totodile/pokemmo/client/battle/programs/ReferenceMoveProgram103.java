package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 刺耳声（招式 ID：103）的攻击／事件阶段动画。
 *
 * <p>招式描述：发出不由自主想要捂起耳朵的刺耳声, 从而大幅降低对手的防御.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110103（名称）和 120103（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ScreechAnimation.java
 *
 * <p>源文件 SHA-256：75cd555fd5ab30aaab152cc949399e7f776a5325bf4f165fbac10baab504f9cb
 */
final class ReferenceMoveProgram103 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(0, 0.4f))
                                                        .endGroup()
                                                        .beginParallel()
                                                        .onStart(owner.preloadParticleEffect(270))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 270, 0, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 270, 1, 9, 8, 0.5f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1467,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        1467,
                                                                        2,
                                                                        16,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1467,
                                                        1,
                                                        16,
                                                        500.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(owner.restoreCamera(0.4f))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        2,
                                                        0.032f,
                                                        0.064f,
                                                        0.100097656f,
                                                        -0.100097656f)),
                                owner.shakeCombatant(16, 2, 2, 0.016f, 0.096f, 1.0f, 0.0f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
