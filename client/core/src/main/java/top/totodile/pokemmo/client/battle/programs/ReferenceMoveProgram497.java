package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 回声（招式 ID：497）的攻击／事件阶段动画。
 *
 * <p>招式描述：用回声攻击对手. 如果每回合都有宝可梦接着使用该招式, 威力就会提高.
 *
 * <p>动画说明：镜头聚焦施术者；镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110497（名称）和 120497（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/EchoedVoiceAnimation.java
 *
 * <p>源文件 SHA-256：df259ed8f76ef6d5d7e6c071ad9181e3722899d755274ee95ba0c2f660da2377
 */
final class ReferenceMoveProgram497 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedParallel(
                                                ReferenceMoveTimeline.appendThenNextParallel(
                                                                ReferenceMoveTimeline.sequence()
                                                                        .beginParallel()
                                                                        .append(
                                                                                owner.focusCamera(
                                                                                        0, 0.4f)),
                                                                owner.tintEnvironment(
                                                                        4,
                                                                        Short.MAX_VALUE,
                                                                        0.0f,
                                                                        0.625f,
                                                                        0.075f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1467,
                                                                        1,
                                                                        14,
                                                                        0.0f,
                                                                        0.9375f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.adjustSoundTrack(
                                                                        1, 0, 0.0f, 640.0f))
                                                        .append(
                                                                owner.scheduleSound(
                                                                        (byte) 2,
                                                                        (short) 1589,
                                                                        2,
                                                                        14,
                                                                        0.0f,
                                                                        0.9921875f,
                                                                        owner.casterHandle))
                                                        .append(
                                                                owner.scaleCombatant(
                                                                        14,
                                                                        2,
                                                                        12,
                                                                        0.0f,
                                                                        0.032f,
                                                                        0.100097656f,
                                                                        -0.100097656f))
                                                        .onStart(owner.preloadParticleEffect(208))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 208, 0, 9, 8, 0.5f))
                                                        .append(
                                                                owner.emitParticles(
                                                                        -1, 208, 1, 9, 8, 0.5f))
                                                        .onStart(owner.preloadParticleEffect(664)),
                                                owner.emitParticles(-1, 664, 0, 9, 8, 0.5f),
                                                0.8f)
                                        .append(owner.focusCamera(1, 0.25f))
                                        .append(owner.setSpriteAnimationPaused(16, 1)),
                                owner.scaleCombatant(
                                        16, 2, 2, 0.0f, 0.096f, 0.19995117f, -0.19995117f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .append(owner.tintEnvironment(4, Short.MAX_VALUE, 0.625f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.25f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
