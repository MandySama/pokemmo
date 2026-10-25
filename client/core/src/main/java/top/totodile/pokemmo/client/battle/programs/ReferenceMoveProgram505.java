package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 治愈波动（招式 ID：505）的攻击／事件阶段动画。
 *
 * <p>招式描述：放出治愈波动, 从而回复对手最大HP的一半.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110505（名称）和 120505（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/HealPulseAnimation.java
 *
 * <p>源文件 SHA-256：fead9ea3d9f7ad54f69544a46a81e2e489f162ecd88360c6a5831c580775cbf3
 */
final class ReferenceMoveProgram505 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 0, 0.0f, 0.375f, 0.075f))
                        .onStart(owner.preloadParticleEffect(671))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1589,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(16927);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep2
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                1504,
                                                2,
                                                14,
                                                250.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(owner.focusCamera(0, 0.4f))
                                .append(
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                6,
                                                0.016f,
                                                0.016f,
                                                0.020019531f,
                                                -0.020019531f))
                                .append(owner.tintCombatant(14, 0.75f, 0.0f, 0.3125f, color))
                                .append(owner.emitParticles(-1, 671, 0, 9, 8, 0.5f)),
                        owner.emitParticles(-1, 671, 1, 9, 8, 0.5f),
                        0.3f);
        Color color2 = EncounterMoveAnimation.color(16927);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep5, owner.emitParticles(-1, 671, 0, 9, 8, 0.5f))
                        .append(owner.tintCombatant(14, 0.75f, 0.3125f, 0.0f, color2))
                        .append(owner.tintEnvironment(2, 0, 0.375f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
