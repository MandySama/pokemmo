package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 瞪眼（招式 ID：43）的攻击／事件阶段动画。
 *
 * <p>招式描述：用犀利的眼神使其害怕, 从而降低对手的防御.
 *
 * <p>动画说明：镜头聚焦施术者；播放参考粒子特效；调整场景着色；执行精灵位移；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110043（名称）和 120043（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen1/LeerAnimation.java
 *
 * <p>源文件 SHA-256：b41467f38cdce07fff625eb3a53e994fd600a535c8da1531ccd6f6863c108695
 */
final class ReferenceMoveProgram43 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenNextParallel(
                                        ReferenceMoveTimeline.appendThenNextParallel(
                                                        ReferenceMoveTimeline
                                                                .appendThenNextParallel(
                                                                        ReferenceMoveTimeline
                                                                                .sequence()
                                                                                .beginParallel()
                                                                                .append(
                                                                                        owner
                                                                                                .tintEnvironment(
                                                                                                        4,
                                                                                                        0,
                                                                                                        0.0f,
                                                                                                        0.75f,
                                                                                                        0.05f))
                                                                                .append(
                                                                                        owner
                                                                                                .focusCamera(
                                                                                                        0,
                                                                                                        0.4f))
                                                                                .endGroup()
                                                                                .beginParallel()
                                                                                .onStart(
                                                                                        owner
                                                                                                .preloadParticleEffect(
                                                                                                        204))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        204,
                                                                                                        0,
                                                                                                        9,
                                                                                                        8,
                                                                                                        0.5f))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        204,
                                                                                                        1,
                                                                                                        9,
                                                                                                        8,
                                                                                                        0.5f))
                                                                                .append(
                                                                                        owner
                                                                                                .emitParticles(
                                                                                                        -1,
                                                                                                        204,
                                                                                                        2,
                                                                                                        9,
                                                                                                        8,
                                                                                                        0.5f))
                                                                                .append(
                                                                                        owner
                                                                                                .scheduleSound(
                                                                                                        (byte)
                                                                                                                2,
                                                                                                        1449,
                                                                                                        1,
                                                                                                        14,
                                                                                                        0.0f,
                                                                                                        0.8984375f,
                                                                                                        owner.casterHandle)),
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1449,
                                                                                2,
                                                                                14,
                                                                                250.0f,
                                                                                0.390625f,
                                                                                owner.casterHandle))
                                                                .append(owner.restoreCamera(0.25f))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        16, 1)),
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1836,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9921875f,
                                                                owner.casterHandle))
                                                .append(
                                                        owner.shakeCombatant(
                                                                16,
                                                                2,
                                                                6,
                                                                0.0f,
                                                                0.032f,
                                                                0.30004883f,
                                                                0.0f)),
                                        owner.scaleCombatant(
                                                16, 2, 1, 0.016f, 0.16f, -0.19995117f, 0.19995117f))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.tintEnvironment(4, 0, 0.75f, 0.0f, 0.05f));
        return result;
    }
}
