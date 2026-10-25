package top.totodile.pokemmo.client.battle.programs;

import com.badlogic.gdx.graphics.Color;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 虚张声势（招式 ID：207）的攻击／事件阶段动画。
 *
 * <p>招式描述：激怒对手, 使其混乱. 因为愤怒, 对手的攻击会大幅提高.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整精灵叠加颜色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110207（名称）和 120207（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/SwaggerAnimation.java
 *
 * <p>源文件 SHA-256：b6b60988c88b78c5cef8092263cd1913617f7da0caedf5a4a4956cf90c6d5d77
 */
final class ReferenceMoveProgram207 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        ReferenceMoveTimeline animationStep6 =
                ReferenceMoveTimeline.appendThenNextParallel(
                                ReferenceMoveTimeline.waitThenParallel(0.6f)
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1452,
                                                        1,
                                                        14,
                                                        0.0f,
                                                        0.9375f,
                                                        owner.casterHandle))
                                        .append(owner.setSpriteAnimationPaused(14, 1)),
                                owner.scaleCombatant(
                                        14, 2, 1, 0.016f, 0.16f, 0.39990234f, 0.39990234f))
                        .append(owner.focusCamera(1, 0.4f))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1440,
                                        1,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1440,
                                        2,
                                        16,
                                        500.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1463,
                                        3,
                                        16,
                                        0.0f,
                                        0.9375f,
                                        owner.casterHandle))
                        .append(
                                owner.scheduleSound(
                                        (byte) 2,
                                        (short) 1376,
                                        3,
                                        16,
                                        1333.3334f,
                                        0.0f,
                                        owner.casterHandle))
                        .append(owner.adjustSoundTrack(3, 0, 0.0f, 1280.0f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .onStart(owner.preloadParticleEffect(373));
        Color color = EncounterMoveAnimation.color(31);
        ReferenceMoveTimeline animationStep7 =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                        animationStep6
                                .append(owner.emitParticles(-1, 373, 0, 11, 8, 0.5f))
                                .append(
                                        owner.shakeCombatant(
                                                16, 2, 6, 0.032f, 0.016f, 0.19995117f, 0.0f)),
                        owner.tintCombatant(16, 0.5f, 0.0f, 0.75f, color),
                        0.6f);
        color = EncounterMoveAnimation.color(31);
        result =
                animationStep7
                        .append(owner.tintCombatant(16, 0.5f, 0.75f, 0.0f, color))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
