package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 疯狂伏特（招式 ID：528）的攻击／事件阶段动画。
 *
 * <p>招式描述：让电流覆盖全身撞向对手进行攻击. 自己也会受到少许伤害.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110528（名称）和 120528（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/WildChargeAnimation.java
 *
 * <p>源文件 SHA-256：30d1852949c5aea57b1b91646a66fff18f0bad398c90ab57d82aa7389e7e77ad
 */
final class ReferenceMoveProgram528 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(13014);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.tintEnvironment(2, 13311, 0.0f, 0.5f, 0.05f))
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.875f, color))
                        .onStart(owner.preloadParticleEffect(375))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1522,
                                        1,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1431,
                                        1,
                                        14,
                                        1250.0f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1431,
                                        1,
                                        14,
                                        1750.0f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1401,
                                        3,
                                        14,
                                        1166.6666f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1401,
                                        3,
                                        14,
                                        1666.6666f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(owner.emitParticles(-1, 375, 1, 9, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(13014);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenNextParallel(
                                                                        ReferenceMoveTimeline
                                                                                .appendThenNextParallel(
                                                                                        ReferenceMoveTimeline
                                                                                                .appendThenOuterParallel(
                                                                                                        ReferenceMoveTimeline
                                                                                                                .appendThenDelayedParallel(
                                                                                                                        animationStep7,
                                                                                                                        owner
                                                                                                                                .emitParticles(
                                                                                                                                        -1,
                                                                                                                                        375,
                                                                                                                                        2,
                                                                                                                                        9,
                                                                                                                                        8,
                                                                                                                                        0.5f),
                                                                                                                        0.6f)
                                                                                                                .append(
                                                                                                                        owner
                                                                                                                                .tintEnvironment(
                                                                                                                                        2,
                                                                                                                                        13311,
                                                                                                                                        0.5f,
                                                                                                                                        0.0f,
                                                                                                                                        0.05f)),
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
                                                                                                        13311,
                                                                                                        0.0f,
                                                                                                        0.5f,
                                                                                                        0.025f)),
                                                                        owner.tintEnvironment(
                                                                                2, 13311, 0.5f,
                                                                                0.0f, 0.025f)),
                                                        owner.tintEnvironment(
                                                                2, 13311, 0.0f, 0.5f, 0.025f)),
                                                owner.tintEnvironment(2, 13311, 0.5f, 0.0f, 0.025f))
                                        .append(owner.focusCamera(1, 0.4f))
                                        .beginSequence()
                                        .waitSeconds(0.24f)
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1514,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 375, 0, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 375, 3, 11, 8, 0.5f))
                                        .append(owner.emitParticles(-1, 375, 4, 11, 8, 0.5f)),
                                owner.emitParticles(-1, 375, 5, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
