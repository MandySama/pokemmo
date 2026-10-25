package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冰息（招式 ID：524）的攻击／事件阶段动画。
 *
 * <p>招式描述：将冰冷的气息吹向对手进行攻击. 必定会击中要害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110524（名称）和 120524（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/FrostBreathAnimation.java
 *
 * <p>源文件 SHA-256：ceb5188efe6c9853b217d2bac878dc4bb06ca28c4499a73e93787b89081a1ef4
 */
final class ReferenceMoveProgram524 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep2 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.tintEnvironment(4, 0, 0.0f, 1.0f, 0.05f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1439, 1, 14, 0.0f, 0.9375f, owner.casterHandle));
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep2
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        1515,
                                                        2,
                                                        16,
                                                        500.0f,
                                                        0.78125f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 1, 1280.0f, 160.0f))
                                        .onStart(owner.preloadParticleEffect(689))
                                        .append(owner.launchParticles(689, 2, 9, 11, 0.5f, 0.0f)),
                                owner.launchParticles(689, 4, 9, 11, 0.5f, 0.0f),
                                0.6f)
                        .append(owner.focusCamera(1, 0.35f))
                        .append(owner.tintCombatant(16, 1.0f, 0.0f, 0.375f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1488,
                                        2,
                                        16,
                                        1000.0f,
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
                                        (short) 1449,
                                        2,
                                        16,
                                        3000.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.shakeCombatant(16, 2, 32, 0.0f, 0.032f, 0.19995117f, 0.0f))
                        .append(owner.emitParticles(-1, 689, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 689, 0, 11, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        animationStep8,
                                        owner.emitParticles(-1, 689, 3, 11, 8, 0.5f),
                                        4.5f),
                                owner.shakeCombatant(16, 2, 2, 0.0f, 0.032f, 0.19995117f, 0.0f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.tintEnvironment(4, 0, 1.0f, 0.0f, 0.05f))
                        .append(owner.tintCombatant(16, 1.0f, 0.375f, 0.0f, color2))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
