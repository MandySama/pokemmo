package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 奇迹之眼（招式 ID：357）的攻击／事件阶段动画。
 *
 * <p>招式描述：对恶属性宝可梦没有效果的招式以及闪避率高的对手, 使用后变得能够打中.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；调整精灵叠加颜色。
 *
 * <p>编号对应参考动画注册表；语言条目为 110357（名称）和 120357（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/MiracleEyeAnimation.java
 *
 * <p>源文件 SHA-256：1ceafc64ef4e9a227181b24eab7eeb48e84d3d92c5bab802474d89160b7c8eb6
 */
final class ReferenceMoveProgram357 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.waitThenParallel(0.6f),
                                                owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.025f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1782,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1782,
                                                        2,
                                                        14,
                                                        250.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(531)),
                                owner.emitParticles(-1, 531, 0, 9, 8, 0.4f),
                                0.24f)
                        .append(owner.emitParticles(-1, 531, 2, 9, 8, 0.4f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep9 =
                animationStep5
                        .append(owner.emitParticles(-1, 531, 1, 9, 8, 0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .onStart(owner.setHealthHudHidden(18, true))
                        .append(owner.tintCombatant(16, 0.25f, 0.0f, 0.625f, color))
                        .append(owner.emitParticles(-1, 531, 4, 11, 8, 0.4f))
                        .append(owner.emitParticles(-1, 531, 3, 11, 8, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1452,
                                        2,
                                        16,
                                        166.66667f,
                                        0.859375f,
                                        owner.casterHandle));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep9,
                                owner.scheduleSound(
                                        (byte) 2,
                                        1358,
                                        1,
                                        16,
                                        666.6667f,
                                        0.46875f,
                                        owner.casterHandle))
                        .append(owner.tintCombatant(16, 0.25f, 0.625f, 0.0f, color2))
                        .append(owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.025f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
