package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 铁壁（招式 ID：334）的攻击／事件阶段动画。
 *
 * <p>招式描述：将皮肤变得坚硬如铁, 从而大幅提高自己的防御.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110334（名称）和 120334（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/IronDefenseAnimation.java
 *
 * <p>源文件 SHA-256：926bd27e1012c421169cc30dee1b97d9e41a40c252e0819025efc92264aff175
 */
final class ReferenceMoveProgram334 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1))
                                                                .append(
                                                                        owner.tintEnvironment(
                                                                                4,
                                                                                Short.MAX_VALUE,
                                                                                0.0f,
                                                                                0.75f,
                                                                                0.05f)),
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1461,
                                                                0,
                                                                14,
                                                                166.66667f,
                                                                0.625f,
                                                                owner.casterHandle),
                                                        0.44f),
                                                owner.tintEnvironment(
                                                        4, Short.MAX_VALUE, 0.75f, 0.0f, 0.05f),
                                                0.88f)
                                        .append(
                                                owner.tintEnvironment(
                                                        4, Short.MAX_VALUE, 0.0f, 0.75f, 0.05f)),
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1461,
                                        0,
                                        14,
                                        166.66667f,
                                        0.625f,
                                        owner.casterHandle),
                                1.32f)
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.75f, 0.0f, 0.05f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1470,
                                        1,
                                        14,
                                        83.333336f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1358,
                                        2,
                                        14,
                                        83.333336f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(504));
        Color color = EncounterMoveAnimation.color(23254);
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep5.append(owner.emitParticles(-1, 504, 0, 9, 8, 0.5f)),
                        owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color),
                        2.12f);
        color = EncounterMoveAnimation.color(23254);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep6, owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
