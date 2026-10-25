package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 反射壁（招式 ID：115）的攻击／事件阶段动画。
 *
 * <p>招式描述：在5回合内使用神奇的墙, 减弱从对手那受到的物理攻击的伤害.
 *
 * <p>动画说明：播放参考粒子特效；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110115（名称）和 120115（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/ReflectAnimation.java
 *
 * <p>源文件 SHA-256：538a80be45a4e8e4204d6d8158a917c82a24e73947ede079ff53c2488691ac29
 */
final class ReferenceMoveProgram115 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .waitSeconds(0.4f)
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1358,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1471,
                                                                2,
                                                                14,
                                                                166.66667f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .onStart(owner.preloadParticleEffect(281))
                                                .append(owner.emitParticles(-1, 281, 0, 9, 8, 0.5f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 281, 1, 9, 8, 0.5f)),
                                        owner.emitParticles(-1, 281, 2, 9, 8, 0.5f),
                                        0.8f),
                                owner.tintCombatant(
                                        14,
                                        1.0f,
                                        1.0f,
                                        0.0f,
                                        EncounterMoveAnimation.color(Short.MAX_VALUE)))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
