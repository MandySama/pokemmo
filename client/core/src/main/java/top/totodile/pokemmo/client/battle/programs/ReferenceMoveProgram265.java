package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 清醒（招式 ID：265）的攻击／事件阶段动画。
 *
 * <p>招式描述：对于麻痹状态下的对手, 威力会变成2倍. 但相反对手的麻痹也会被治愈.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110265（名称）和 120265（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SmellingSaltsAnimation.java
 *
 * <p>源文件 SHA-256：ef33033a1209e61984a206a2a2fe955f0ec8f8ee725e9ebe1341a022618f15f9
 */
final class ReferenceMoveProgram265 {

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
                                                        (short) 1422,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1422,
                                                        1,
                                                        16,
                                                        133.33333f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        2,
                                                        16,
                                                        133.33333f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .onStart(owner.preloadParticleEffect(431))
                                        .append(owner.emitParticles(-1, 431, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 431, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 431, 2, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 431, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 431, 4, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 431, 5, 11, 8, 0.5f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.048f, -0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
