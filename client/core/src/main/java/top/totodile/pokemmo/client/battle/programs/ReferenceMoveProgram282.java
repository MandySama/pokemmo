package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 拍落（招式 ID：282）的攻击／事件阶段动画。
 *
 * <p>招式描述：拍落对手的持有物, 直到战斗结束都不能使用. 对手携带道具时会增加伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110282（名称）和 120282（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/KnockOffAnimation.java
 *
 * <p>源文件 SHA-256：78e0620fe9530b382b6c70288b3d7843a149bfa546f85220a34e2cf980d6d6b5
 */
final class ReferenceMoveProgram282 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                                        .append(
                                                                owner.shakeCombatant(
                                                                        14, 2, 1, 0.016f, 0.064f,
                                                                        1.0f, 0.0f)),
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1421,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1437,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1420,
                                                        1,
                                                        16,
                                                        333.33334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(449))
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(owner.emitParticles(-1, 449, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 449, 1, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 449, 2, 11, 8, 0.5f))
                                        .append(owner.focusCamera(1, 0.4f)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.048f, 0.19995117f, -0.19995117f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
