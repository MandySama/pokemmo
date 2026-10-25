package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 搏命（招式 ID：515）的攻击／事件阶段动画。
 *
 * <p>招式描述：拼命攻击对手. 虽然自己陷入濒死, 但会给予对手和自己目前HP等量的伤害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110515（名称）和 120515（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FinalGambitAnimation.java
 *
 * <p>源文件 SHA-256：3a5aff39f670d6a658d7ba4a728f5daba4f4179a491c9abe2a23a96e2c85f47d
 */
final class ReferenceMoveProgram515 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1589,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1654,
                                        2,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1489,
                                        1,
                                        14,
                                        1333.3334f,
                                        0.9921875f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(13014);
        ReferenceMoveTimeline animationStep5 =
                animationStep4
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1438,
                                        2,
                                        14,
                                        1666.6666f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.875f, color))
                        .onStart(owner.preloadParticleEffect(680));
        Color color2 = EncounterMoveAnimation.color(13014);
        result =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenNextParallel(
                                                                        ReferenceMoveTimeline
                                                                                .appendThenNextParallel(
                                                                                        ReferenceMoveTimeline
                                                                                                .appendThenNextParallel(
                                                                                                        animationStep5
                                                                                                                .append(
                                                                                                                        owner
                                                                                                                                .emitParticles(
                                                                                                                                        -1,
                                                                                                                                        680,
                                                                                                                                        0,
                                                                                                                                        9,
                                                                                                                                        8,
                                                                                                                                        0.5f))
                                                                                                                .append(
                                                                                                                        owner
                                                                                                                                .tintCombatant(
                                                                                                                                        14,
                                                                                                                                        0.5f,
                                                                                                                                        0.875f,
                                                                                                                                        0.0f,
                                                                                                                                        color2)),
                                                                                                        owner
                                                                                                                .tintEnvironment(
                                                                                                                        2,
                                                                                                                        Short
                                                                                                                                .MAX_VALUE,
                                                                                                                        0.0f,
                                                                                                                        0.5f,
                                                                                                                        0.025f)),
                                                                                        owner
                                                                                                .tintEnvironment(
                                                                                                        2,
                                                                                                        Short
                                                                                                                .MAX_VALUE,
                                                                                                        0.5f,
                                                                                                        0.0f,
                                                                                                        0.025f)),
                                                                        owner.tintEnvironment(
                                                                                2,
                                                                                Short.MAX_VALUE,
                                                                                0.0f,
                                                                                0.5f,
                                                                                0.025f))
                                                                .append(
                                                                        owner.tintEnvironment(
                                                                                2,
                                                                                Short.MAX_VALUE,
                                                                                0.5f,
                                                                                0.0f,
                                                                                0.025f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .beginSequence()
                                                                .waitSeconds(0.32f)
                                                                .beginParallel()
                                                                .append(owner.focusCamera(1, 0.4f)),
                                                        owner.emitParticles(
                                                                -1, 680, 0, 11, 8, 0.5f),
                                                        0.52f)
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 680, 2, 11, 8, 0.5f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                2,
                                                                16,
                                                                116.666664f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                1,
                                                                16,
                                                                216.66667f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1420,
                                                                2,
                                                                16,
                                                                366.66666f,
                                                                0.9921875f,
                                                                owner.casterHandle)),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1420,
                                                1,
                                                16,
                                                500.0f,
                                                0.9921875f,
                                                owner.casterHandle),
                                        0.72f),
                                owner.emitParticles(-1, 680, 1, 11, 8, 0.5f),
                                0.92f)
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 2, 0.0f, 0.064f, -0.19995117f, 0.19995117f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.32f)
                        .beginParallel()
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
