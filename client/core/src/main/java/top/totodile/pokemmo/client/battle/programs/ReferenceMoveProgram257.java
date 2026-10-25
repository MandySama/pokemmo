package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 热风（招式 ID：257）的攻击／事件阶段动画。
 *
 * <p>招式描述：将炎热的气息吹向对手进行攻击. 有时会让对手陷入灼伤状态.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画。
 *
 * <p>编号对应参考动画注册表；语言条目为 110257（名称）和 120257（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen3/HeatWaveAnimation.java
 *
 * <p>源文件 SHA-256：7b3897ce5ce0a42397dab173314015145a1b59159e061007ca1888f24c42645a
 */
final class ReferenceMoveProgram257 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep5 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.6f))
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1497,
                                        1,
                                        14,
                                        0.0f,
                                        0.546875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        2000.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 1, 1120.0f, 800.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1425,
                                        2,
                                        14,
                                        0.0f,
                                        0.9921875f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        2,
                                        14,
                                        2000.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 1920.0f))
                        .append(owner.adjustSoundTrack(2, 1, 1120.0f, 800.0f))
                        .onStart(owner.preloadParticleEffect(423));
        Color color = EncounterMoveAnimation.color(8470);
        ReferenceMoveTimeline animationStep6 =
                animationStep5
                        .append(owner.emitParticles(-1, 423, 0, 0, 8, 0.5f))
                        .append(owner.tintEnvironment(4, 8470, 0.0f, 0.75f, 0.05f))
                        .append(owner.tintCombatant(14, 0.5f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(8470);
        ReferenceMoveTimeline animationStep7 =
                animationStep6
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color))
                        .beginSequence()
                        .waitSeconds(0.2f)
                        .beginParallel()
                        .append(owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f))
                        .endGroup()
                        .endGroup()
                        .beginSequence()
                        .waitSeconds(2.04f)
                        .beginParallel()
                        .append(owner.tintEnvironment(4, 8470, 0.75f, 0.0f, 0.05f));
        color = EncounterMoveAnimation.color(8470);
        ReferenceMoveTimeline animationStep8 =
                animationStep7.append(owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color));
        color = EncounterMoveAnimation.color(8470);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep8, owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .waitSeconds(0.6f)
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
