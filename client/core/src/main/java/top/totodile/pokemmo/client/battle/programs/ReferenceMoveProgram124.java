package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 污泥攻击（招式 ID：124）的攻击／事件阶段动画。
 *
 * <p>招式描述：用污泥投掷对手进行攻击. 有时会让对手陷入中毒状态.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110124（名称）和 120124（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SludgeAnimation.java
 *
 * <p>源文件 SHA-256：f9ab3fcf4bf30ccca589cabba9cbfeb9105933506d3fac5db0c6577cfe017180
 */
final class ReferenceMoveProgram124 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep4 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1510,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.859375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1480,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1376,
                                                        2,
                                                        16,
                                                        1000.0f,
                                                        0.0f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(289)),
                                owner.launchParticles(289, 1, 9, 11, 0.5f, 360.0f),
                                0.4f)
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 1));
        Color color = EncounterMoveAnimation.color(30750);
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep4, owner.emitParticles(-1, 289, 0, 11, 8, 0.5f), 0.12f)
                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f));
        Color color2 = EncounterMoveAnimation.color(30750);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep5,
                                owner.scheduleSound(
                                        (byte) 2, 1478, 2, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
