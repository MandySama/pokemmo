package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 黑色目光（招式 ID：212）的攻击／事件阶段动画。
 *
 * <p>招式描述：用好似要勾人心魂的黑色目光一动不动地凝视对手, 使其不能从战斗中逃走.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110212（名称）和 120212（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen2/MeanLookAnimation.java
 *
 * <p>源文件 SHA-256：7ea4360634a6df4954ef29ca26291e3d941f75ca93a9064ea7cdaee8de8a5619
 */
final class ReferenceMoveProgram212 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenOuterParallel(
                                ReferenceMoveTimeline.appendThenDelayedOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(owner.focusCamera(1, 0.4f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1418,
                                                                                3,
                                                                                16,
                                                                                0.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                1376,
                                                                                3,
                                                                                16,
                                                                                1666.6666f,
                                                                                0.0f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                3, 0, 0.0f,
                                                                                1600.0f))
                                                                .append(
                                                                        owner
                                                                                .setSpriteAnimationPaused(
                                                                                        14, 1))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                378))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 378, 0, 11, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 378, 1, 11, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 378, 2, 11, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 378, 3, 11, 8,
                                                                                0.5f)),
                                                        owner.tintEnvironment(
                                                                4, 31775, 0.0f, 0.75f, 0.05f),
                                                        2.0f)
                                                .append(
                                                        owner.scheduleSound(
                                                                (byte) 2,
                                                                1509,
                                                                1,
                                                                16,
                                                                0.0f,
                                                                0.9375f,
                                                                owner.casterHandle)),
                                        owner.tintEnvironment(
                                                4, Short.MAX_VALUE, 0.75f, 0.75f, 0.05f),
                                        2.08f),
                                owner.tintEnvironment(4, Short.MAX_VALUE, 0.75f, 0.0f, 0.05f))
                        .append(owner.setSpriteAnimationPaused(14, 0))
                        .append(owner.restoreCamera(0.4f))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
