package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 暗影球（招式 ID：247）的攻击／事件阶段动画。
 *
 * <p>招式描述：投掷一团黑影进行攻击. 有时会降低对手的特防.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110247（名称）和 120247（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/ShadowBallAnimation.java
 *
 * <p>源文件 SHA-256：51ad9b685ad81e7c49e3658351f22938c90fadb748facc3b677f0f64899edb6c
 */
final class ReferenceMoveProgram247 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1434,
                                                                1,
                                                                14,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1815,
                                                                1,
                                                                14,
                                                                1666.6666f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1376,
                                                                1,
                                                                14,
                                                                1333.3334f,
                                                                0.0f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                1, 0, 1600.0f, 320.0f))
                                                .append(
                                                        owner.adjustSoundTrack(
                                                                1, 1, 1600.0f, 320.0f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1475,
                                                                2,
                                                                16,
                                                                1833.3334f,
                                                                0.9375f,
                                                                owner.casterHandle))
                                                .onStart(owner.preloadParticleEffect(414))
                                                .append(owner.emitParticles(-1, 414, 0, 0, 0, 1.0f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 414, 1, 0, 0, 1.0f)),
                                        owner.emitParticles(-1, 414, 2, 0, 0, 1.0f),
                                        1.6f)
                                .append(owner.focusCamera(1, 0.4f)),
                        owner.emitParticles(-1, 414, 3, 1, 0, 0.0f),
                        1.8f);
        Color color = EncounterMoveAnimation.color(12300);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        animationStep9
                                .append(owner.emitParticles(-1, 414, 4, 11, 8, 0.5f))
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(owner.tintCombatant(16, 0.25f, 0.0f, 1.0f, color)),
                        owner.shakeCombatant(16, 2, 3, 0.032f, 0.016f, 0.30004883f, 0.0f),
                        2.2f);
        color = EncounterMoveAnimation.color(12300);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep10, owner.tintCombatant(16, 0.5f, 1.0f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
