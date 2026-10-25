package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰封世界（招式 ID：549）的攻击／事件阶段动画。
 *
 * <p>招式描述：将冰冻的冷气吹向对手进行攻击. 会降低对手的速度.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110549（名称）和 120549（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/GlaciateAnimation.java
 *
 * <p>源文件 SHA-256：c2840379cbf9ffc92ed19f48cf80f2d9004186b758bd95b567b434a6762fae2a
 */
final class ReferenceMoveProgram549 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1440,
                                                                                1,
                                                                                14,
                                                                                333.33334f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .onStart(
                                                                        owner.setHealthHudHidden(
                                                                                18, true))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                716))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 716, 0, 9, 8,
                                                                                0.5f)),
                                                        owner.scaleCombatant(
                                                                14,
                                                                2,
                                                                1,
                                                                0.0f,
                                                                0.48f,
                                                                0.30004883f,
                                                                -0.30004883f))
                                                .append(owner.focusCamera(1, 0.4f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1440,
                                                                1,
                                                                16,
                                                                500.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1859,
                                                                2,
                                                                16,
                                                                666.6667f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1488,
                                                                2,
                                                                16,
                                                                1666.6666f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1488,
                                                                2,
                                                                16,
                                                                2833.3333f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1449,
                                                                1,
                                                                16,
                                                                1500.0f,
                                                                0.546875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1449,
                                                                1,
                                                                16,
                                                                3333.3333f,
                                                                0.390625f,
                                                                owner.casterHandle)),
                                        owner.tintEnvironment(2, 0, 0.0f, 0.5f, 0.075f),
                                        0.6f)
                                .append(owner.emitParticles(-1, 716, 6, 11, 8, 0.5f)),
                        owner.emitParticles(-1, 716, 7, 11, 8, 0.5f),
                        1.0f);
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep14 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                animationStep11,
                                                owner.emitParticles(-1, 716, 4, 11, 8, 0.75f),
                                                1.8f)
                                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.5f, color)),
                                owner.emitParticles(-1, 716, 2, 11, 8, 0.625f),
                                2.2f)
                        .append(owner.emitParticles(-1, 716, 3, 11, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.appendThenOuterParallel(
                                                animationStep14,
                                                owner.emitParticles(-1, 716, 2, 11, 8, 0.625f))
                                        .append(owner.tintEnvironment(2, 0, 0.5f, 0.0f, 0.075f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 1, 0.0f, 0.032f, 0.100097656f, -0.100097656f))
                        .append(owner.tintCombatant(16, 0.75f, 0.5f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
