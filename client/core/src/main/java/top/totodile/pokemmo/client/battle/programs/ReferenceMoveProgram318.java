package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 银色旋风（招式 ID：318）的攻击／事件阶段动画。
 *
 * <p>招式描述：在风中掺入鳞粉攻击对手. 有时会提高自己的全部能力.
 *
 * <p>动画说明：播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110318（名称）和 120318（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/SilverWindAnimation.java
 *
 * <p>源文件 SHA-256：70974b0180c7f7ac41763ea32de9942d5171e030264565e0e3ec16d3de9f2ac2
 */
final class ReferenceMoveProgram318 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.waitThenParallel(0.6f)
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1763,
                                        1,
                                        14,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 320.0f, 1600.0f))
                        .append(owner.adjustSoundTrack(1, 1, 1280.0f, 480.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        2833.3333f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1517,
                                        2,
                                        14,
                                        83.333336f,
                                        0.78125f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(2, 1, 560.0f, 640.0f))
                        .onStart(owner.preloadParticleEffect(488))
                        .append(owner.emitParticles(-1, 488, 0, 0, 8, 1.0f))
                        .append(owner.emitParticles(-1, 488, 2, 0, 8, 0.0f))
                        .append(owner.emitParticles(-1, 488, 1, 0, 8, 1.0f));
        Color color = EncounterMoveAnimation.color(12684);
        ReferenceMoveTimeline animationStep8 =
                animationStep7
                        .append(owner.emitParticles(-1, 488, 3, 0, 8, 0.0f))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f))
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(12684);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        animationStep8,
                                        owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color),
                                        0.4f)
                                .append(owner.setSpriteAnimationPaused(16, 1)),
                        owner.shakeCombatant(16, 2, 13, 0.032f, 0.016f, 0.30004883f, 0.0f),
                        1.6f);
        color = EncounterMoveAnimation.color(12684);
        ReferenceMoveTimeline animationStep10 =
                animationStep9.append(owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color));
        color = EncounterMoveAnimation.color(12684);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep10.append(
                                        owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color)),
                                owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
