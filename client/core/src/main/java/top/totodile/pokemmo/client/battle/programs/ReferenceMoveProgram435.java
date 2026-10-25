package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 放电（招式 ID：435）的攻击／事件阶段动画。
 *
 * <p>招式描述：用耀眼的电击攻击自己周围所有的宝可梦. 有时会陷入麻痹状态.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110435（名称）和 120435（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/DischargeAnimation.java
 *
 * <p>源文件 SHA-256：3a8376a403469794c9b4da423880767a2484c4e267b1892acb239e4adc73de81
 */
final class ReferenceMoveProgram435 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        Color color = EncounterMoveAnimation.color(13311);
        ReferenceMoveTimeline animationStep8 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(0, 0.4f))
                        .append(owner.tintEnvironment(4, 0, 0.0f, 0.75f, 0.05f))
                        .append(owner.tintCombatant(14, 1.75f, 0.0f, 0.75f, color))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1445,
                                        1,
                                        14,
                                        166.66667f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        1,
                                        14,
                                        1000.0f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(1, 0, 160.0f, 640.0f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1505,
                                        1,
                                        14,
                                        1000.0f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1458,
                                        2,
                                        14,
                                        333.33334f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1458,
                                        2,
                                        14,
                                        666.6667f,
                                        0.859375f,
                                        owner.casterHandle))
                        .append(owner.setSpriteAnimationPaused(14, 1))
                        .onStart(owner.preloadParticleEffect(610))
                        .append(owner.emitParticles(-1, 610, 0, 9, 8, 0.5f))
                        .append(owner.emitParticles(-1, 610, 2, 9, 8, 0.5f));
        Color color2 = EncounterMoveAnimation.color(13311);
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                animationStep8, owner.emitParticles(-1, 610, 3, 9, 8, 0.5f), 1.4f)
                        .append(owner.tintCombatant(14, 0.5f, 0.75f, 0.0f, color2))
                        .append(owner.focusCamera(1, 0.8f));
        Color color3 = EncounterMoveAnimation.color(13311);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                animationStep9,
                                owner.scheduleSound(
                                        (byte) 2,
                                        1458,
                                        2,
                                        16,
                                        666.6667f,
                                        0.9375f,
                                        owner.casterHandle),
                                1.8f)
                        .append(owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color3));
        Color color4 = EncounterMoveAnimation.color(13311);
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        animationStep10
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 610, 1, 11, 8, 0.5f))
                                                .append(owner.setSpriteAnimationPaused(16, 1)),
                                        owner.shakeCombatant(
                                                16, 2, 6, 0.032f, 0.016f, 0.30004883f, 0.0f))
                                .append(owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color4))
                                .append(owner.setSpriteAnimationPaused(14, 0))
                                .append(owner.setSpriteAnimationPaused(16, 0))
                                .append(owner.restoreCamera(0.4f)),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f));
        return result;
    }
}
