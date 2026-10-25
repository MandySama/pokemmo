package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 闪电（自定义动画注册 ID：1025）的攻击／事件阶段动画。
 *
 * <p>招式描述：参考语言资源仅提供占位说明（--），未定义具体招式效果。
 *
 * <p>动画说明：播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 111025（名称）和 121025（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/special/CustomMove1025Animation.java
 *
 * <p>源文件 SHA-256：5f5a907f879aad3e4abd3d947d1ec41051b6f16ca397630a5d3230cdc5baed18
 */
final class ReferenceMoveProgram1025 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .beginSequence()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1710, 1, 16, 0.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1710, 2, 16, 250.0f, 0.9375f, owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1445,
                                        1,
                                        16,
                                        333.33334f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 1, 16, 1500.0f, 0.0f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 320.0f, 1120.0f));
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep8 =
                animationStep5
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1710,
                                        2,
                                        16,
                                        333.33334f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color))
                        .onStart(owner.preloadParticleEffect(249))
                        .onStart(owner.preloadParticleEffect(250))
                        .append(owner.emitParticles(-1, 249, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 250, 0, 11, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(0);
        result =
                animationStep8
                        .append(owner.emitParticles(-1, 249, 1, 11, 8, 0.5f))
                        .append(owner.shakeCombatant(16, 2, 8, 0.032f, 0.016f, 0.5f, 0.0f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .beginParallel()
                        .append(owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color2))
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .endGroup();
        return result;
    }
}
