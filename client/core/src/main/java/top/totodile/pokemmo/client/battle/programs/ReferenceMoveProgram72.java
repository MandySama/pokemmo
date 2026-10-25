package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 超级吸取（招式 ID：72）的攻击／事件阶段动画。
 *
 * <p>招式描述：吸取对手的养分进行攻击. 可以回复给予对手伤害的一半HP.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110072（名称）和 120072（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/MegaDrainAnimation.java
 *
 * <p>源文件 SHA-256：71195ef3a17f72aa8ad1134829c284d9a8db6b474b1ef359d80ab86c9bc8c7b0
 */
final class ReferenceMoveProgram72 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 960, 0.0f, 0.25f, 0.125f))
                        .append(owner.focusCamera(0, 0.6f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        16,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1440.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1418,
                                        3,
                                        16,
                                        0.0f,
                                        0.625f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        3,
                                        16,
                                        1666.6666f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(3, 0, 0.0f, 1440.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1358,
                                        2,
                                        14,
                                        1666.6666f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1358,
                                        2,
                                        14,
                                        1916.6666f,
                                        0.390625f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(233))
                        .append(owner.emitParticles(-1, 233, 0, 11, 9, 0.4f))
                        .append(owner.emitParticles(-1, 233, 1, 11, 9, 0.4f))
                        .beginSequence()
                        .waitSeconds(1.8f)
                        .beginParallel();
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep9.append(owner.emitParticles(-1, 233, 2, 9, 8, 0.4f)),
                        owner.tintCombatant(14, 0.25f, 0.0f, 0.5f, color));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep10, owner.tintCombatant(14, 0.25f, 0.5f, 0.0f, color))
                        .append(owner.tintEnvironment(4, 960, 0.25f, 0.0f, 0.125f))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
