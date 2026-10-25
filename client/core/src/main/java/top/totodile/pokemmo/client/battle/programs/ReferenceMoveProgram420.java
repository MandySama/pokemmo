package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰砾（招式 ID：420）的攻击／事件阶段动画。
 *
 * <p>招式描述：瞬间制作冰块, 快速地扔向对手. 必定能够先制攻击.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110420（名称）和 120420（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/IceShardAnimation.java
 *
 * <p>源文件 SHA-256：0b1a5a70eb99cdf49b5de540332af660666a89dfac880e0987d01c02054b2bc6
 */
final class ReferenceMoveProgram420 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.sequence()
                                        .beginParallel()
                                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.8125f, 0.075f))
                                        .onStart(owner.preloadParticleEffect(595))
                                        .append(owner.focusCamera(0, 0.4f))
                                        .onStart(owner.setHealthHudHidden(18, true))
                                        .endGroup()
                                        .beginParallel()
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1440,
                                                        2,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1488,
                                                        2,
                                                        14,
                                                        1000.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1488,
                                                        2,
                                                        14,
                                                        1333.3334f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1449,
                                                        1,
                                                        14,
                                                        666.6667f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1945,
                                                        0,
                                                        14,
                                                        1000.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1521,
                                                        1,
                                                        16,
                                                        2083.3333f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1549,
                                                        2,
                                                        16,
                                                        2333.3333f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.emitParticles(-1, 595, 1, 9, 8, 0.5f))
                                        .append(owner.launchParticles(595, 2, 9, 11, 0.5f, 0.0f)),
                                owner.emitParticles(-1, 595, 3, 9, 8, 0.5f),
                                1.8f)
                        .append(owner.focusCamera(1, 0.2f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(1.88f)
                        .beginParallel()
                        .append(owner.emitParticles(-1, 595, 0, 11, 8, 0.5f));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenOuterParallel(
                        animationStep11.append(owner.emitParticles(-1, 595, 4, 11, 8, 0.5f)),
                        owner.tintCombatant(16, 0.75f, 0.0f, 0.8125f, color));
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        animationStep12
                                .append(owner.tintCombatant(16, 0.75f, 0.8125f, 0.0f, color))
                                .append(owner.restoreCamera(0.25f))
                                .onStart(owner.setHealthHudHidden(18, false)),
                        owner.tintEnvironment(4, 0, 0.8125f, 0.0f, 0.075f));
        return result;
    }
}
