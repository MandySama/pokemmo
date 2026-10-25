package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 胃液（招式 ID：380）的攻击／事件阶段动画。
 *
 * <p>招式描述：将胃液吐向对手的身体. 沾上的胃液会消除对手的特性效果.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110380（名称）和 120380（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/GastroAcidAnimation.java
 *
 * <p>源文件 SHA-256：773aa56f459b3df391393e8d1af5a173e03cf519775d26eba9e0e6a4c5d19af8
 */
final class ReferenceMoveProgram380 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep9 =
                ReferenceMoveTimeline.sequence()
                        .beginParallel()
                        .append(owner.focusCamera(1, 0.4f))
                        .endGroup()
                        .beginParallel()
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1480,
                                        1,
                                        16,
                                        0.0f,
                                        0.8984375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1452,
                                        1,
                                        16,
                                        833.3333f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1724,
                                        2,
                                        16,
                                        583.3333f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1724,
                                        2,
                                        16,
                                        833.3333f,
                                        0.3125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1724,
                                        2,
                                        16,
                                        1083.3334f,
                                        0.078125f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1724,
                                        2,
                                        16,
                                        1333.3334f,
                                        0.0234375f,
                                        owner.casterHandle))
                        .onStart(owner.preloadParticleEffect(555))
                        .append(owner.emitParticles(-1, 555, 0, 11, 8, 0.25f))
                        .append(owner.emitParticles(-1, 555, 1, 11, 8, 0.25f));
        Color color = EncounterMoveAnimation.color(970);
        ReferenceMoveTimeline animationStep10 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep9
                                .append(owner.emitParticles(-1, 555, 2, 11, 8, 0.25f))
                                .append(
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                3,
                                                0.016f,
                                                0.032f,
                                                0.100097656f,
                                                -0.100097656f))
                                .append(owner.setSpriteAnimationPaused(16, 1)),
                        owner.tintCombatant(16, 0.25f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(970);
        ReferenceMoveTimeline animationStep11 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep10, owner.tintCombatant(16, 0.25f, 0.75f, 0.0f, color));
        color = EncounterMoveAnimation.color(970);
        ReferenceMoveTimeline animationStep12 =
                ReferenceMoveTimeline.appendThenNextParallel(
                        animationStep11, owner.tintCombatant(16, 0.25f, 0.0f, 0.75f, color));
        color = EncounterMoveAnimation.color(970);
        result =
                ReferenceMoveTimeline.appendThenNextParallel(
                                animationStep12, owner.tintCombatant(16, 0.25f, 0.75f, 0.0f, color))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
