package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 火之誓约（招式 ID：519）的攻击／事件阶段动画。
 *
 * <p>招式描述：用火柱进行攻击. 如果和草组合, 威力就会提高, 周围会变成火海.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110519（名称）和 120519（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FirePledgeAnimation.java
 *
 * <p>源文件 SHA-256：3ae8b10dd1f70a6967396641835022fa9f1bb506a50621c69b94b53a5477182a
 */
final class ReferenceMoveProgram519 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .onStart(owner.preloadParticleEffect(684))
                                        .append(owner.focusCamera(1, 0.25f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1489,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1805,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1489,
                                                        1,
                                                        16,
                                                        466.66666f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1805,
                                                        2,
                                                        16,
                                                        466.66666f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1489,
                                                        1,
                                                        16,
                                                        916.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1805,
                                                        2,
                                                        16,
                                                        916.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 684, 4, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 684, 7, 11, 8, 0.125f)),
                                owner.emitParticles(-1, 684, 1, 11, 8, 0.25f),
                                0.3f)
                        .append(owner.emitParticles(-1, 684, 5, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 684, 8, 11, 8, 0.125f));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep17 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                                animationStep12,
                                                owner.emitParticles(-1, 684, 2, 11, 8, 0.25f),
                                                0.6f)
                                        .append(owner.setSpriteAnimationPaused(16, 1))
                                        .append(
                                                owner.scaleCombatant(
                                                        16,
                                                        2,
                                                        6,
                                                        0.016f,
                                                        0.032f,
                                                        0.30004883f,
                                                        -0.100097656f))
                                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.625f, color))
                                        .append(owner.emitParticles(-1, 684, 3, 11, 8, 0.25f))
                                        .append(owner.emitParticles(-1, 684, 6, 11, 8, 0.125f)),
                                owner.emitParticles(-1, 684, 0, 11, 8, 0.25f),
                                0.9f),
                        owner.emitParticles(-1, 684, 9, 11, 8, 0.5f),
                        1.3f);
        Color color2 = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep17, owner.emitParticles(-1, 684, 9, 11, 8, 0.5f))
                        .append(owner.tintCombatant(16, 0.75f, 0.625f, 0.0f, color2))
                        .append(owner.restoreCamera(0.4f))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
