package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 热水（招式 ID：503）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手喷射煮得翻滚的开水进行攻击. 有时会让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110503（名称）和 120503（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/ScaldAnimation.java
 *
 * <p>源文件 SHA-256：c2cb0f5afab0d31dc68db779fef6ff22dfa41fab40d1b1a898b8d79382f92643
 */
final class ReferenceMoveProgram503 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(0, 0.4f))
                                                                .onStart(
                                                                        owner.setHealthHudHidden(
                                                                                18, true))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                669)),
                                                        owner.emitParticles(
                                                                -1, 669, 2, 9, 11, 0.5f),
                                                        0.2f)
                                                .append(owner.focusCamera(1, 0.4f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1857,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1718,
                                                                1,
                                                                14,
                                                                583.3333f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1718,
                                                                2,
                                                                16,
                                                                833.3333f,
                                                                0.9921875f,
                                                                owner.casterHandle)),
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1718,
                                                1,
                                                16,
                                                1166.6666f,
                                                0.9921875f,
                                                owner.casterHandle),
                                        0.4f),
                                owner.emitParticles(-1, 669, 0, 11, 8, 0.5f),
                                1.2f),
                        owner.emitParticles(-1, 669, 1, 11, 8, 0.5f),
                        1.4f);
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep9 =
                animationStep8
                        .append(owner.emitParticles(-1, 669, 1, 11, 8, 0.5f))
                        .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.5f, color))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.6f)
                        .beginParallel();
        Color color2 = EncounterMoveAnimation.color(31);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep9, owner.emitParticles(-1, 669, 1, 11, 8, 0.5f))
                        .append(owner.tintCombatant(16, 0.75f, 0.5f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
