package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 冲浪（招式 ID：57）的攻击／事件阶段动画。
 *
 * <p>招式描述：利用大浪攻击自己周围所有的宝可梦.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110057（名称）和 120057（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/SurfAnimation.java
 *
 * <p>源文件 SHA-256：149f1a5fd69d6934853dd77662260762e8d2b9f755c6aba7a69e412ccc26fb54
 */
final class ReferenceMoveProgram57 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                        ReferenceMoveTimeline.sequence()
                                                .beginParallel()
                                                .append(owner.focusCamera(0, 0.4f))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1897,
                                                                1,
                                                                14,
                                                                500.0f,
                                                                0.46875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1511,
                                                                2,
                                                                14,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                (short) 1511,
                                                                2,
                                                                14,
                                                                833.3333f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(owner.setSpriteAnimationPaused(14, 1))
                                                .onStart(owner.setHealthHudHidden(18, true))
                                                .append(
                                                        owner.scaleCombatant(
                                                                14,
                                                                2,
                                                                1,
                                                                0.016f,
                                                                0.192f,
                                                                0.30004883f,
                                                                -0.30004883f))
                                                .onStart(owner.preloadParticleEffect(218))
                                                .append(owner.emitParticles(-1, 218, 3, 9, 8, 0.0f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 218, 1, 11, 8, 0.0f))
                                                .append(
                                                        owner.emitParticles(
                                                                -1, 218, 4, 9, 8, 0.0f)),
                                        owner.emitParticles(-1, 218, 5, 9, 11, 0.0f),
                                        0.4f)
                                .append(owner.focusCamera(0, 1.0f))
                                .endGroup()
                                .endGroup()
                                .beginSequence()
                                .waitSeconds(1.0f)
                                .beginParallel()
                                .append(owner.focusCamera(1, 0.8f))
                                .append(
                                        owner.scaleCombatant(
                                                14, 2, 1, 0.016f, 0.08f, -0.30004883f, 0.30004883f))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1511,
                                                1,
                                                16,
                                                500.0f,
                                                0.9921875f,
                                                owner.casterHandle))
                                .append(
                                        owner.scheduleSound(
                                                (byte) 2,
                                                (short) 1897,
                                                2,
                                                16,
                                                500.0f,
                                                0.9921875f,
                                                owner.casterHandle)),
                        owner.scheduleSound(
                                (byte) 2,
                                (short) 1376,
                                2,
                                16,
                                1666.6666f,
                                0.0f,
                                owner.casterHandle),
                        1.4f);
        Color color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                        ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        animationStep11.append(
                                                owner.emitParticles(-1, 218, 2, 11, 8, 0.0f)),
                                        owner.adjustSoundTrack(2, 1, 320.0f, 400.0f),
                                        1.6f)
                                .append(owner.setSpriteAnimationPaused(16, 1))
                                .append(owner.tintCombatant(16, 0.75f, 0.0f, 0.75f, color)),
                        owner.shakeCombatant(16, 2, 8, 0.032f, 0.016f, 0.30004883f, 0.0f),
                        2.04f);
        color = EncounterMoveAnimation.color(Short.MAX_VALUE);
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                animationStep12, owner.tintCombatant(16, 0.75f, 0.75f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .beginSequence()
                        .waitSeconds(0.12f)
                        .beginParallel()
                        .onStart(owner.setHealthHudHidden(18, false))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
