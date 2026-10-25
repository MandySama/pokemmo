package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 挠痒（招式 ID：321）的攻击／事件阶段动画。
 *
 * <p>招式描述：给对手挠痒, 使其发笑, 从而降低对手的攻击和防御.
 *
 * <p>动画说明：播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110321（名称）和 120321（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/TickleAnimation.java
 *
 * <p>源文件 SHA-256：467e485b39c31d3634db6a8d78e9ddd7ca715edaedfae4d26bc3ce390fb386ab
 */
final class ReferenceMoveProgram321 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep3 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1358,
                                        1,
                                        14,
                                        166.66667f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        1714,
                                        2,
                                        16,
                                        750.0f,
                                        0.8984375f,
                                        owner.casterHandle));
        Color color = EncounterMoveAnimation.color(0);
        ReferenceMoveTimeline animationStep4 =
                animationStep3
                        .append(
                                owner.scheduleSound(
                                        (byte) 2, 1376, 2, 16, 1500.0f, 0.0f, owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 1120.0f, 320.0f))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(owner.shakeCombatant(14, 3, 1, 0.016f, 0.064f, 0.60009766f, 0.0f))
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color))
                        .onStart(owner.preloadParticleEffect(491));
        Color color2 = EncounterMoveAnimation.color(0);
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep4, owner.emitParticles(-1, 491, 0, 9, 8, 0.4f), 0.4f)
                        .append(owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color2))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(0.6f)
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.shakeCombatant(16, 2, 2, 0.016f, 0.08f, 1.0f, 0.0f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .waitSeconds(0.6f)
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
