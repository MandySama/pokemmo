package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 制裁光砾（招式 ID：449）的攻击／事件阶段动画。
 *
 * <p>招式描述：向对手放出无数的光弹. 属性会根据自己携带的石板不同而改变.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110449（名称）和 120449（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen4/JudgmentAnimation.java
 *
 * <p>源文件 SHA-256：3b4f4d48897152b367f031de2638314b6949c89c9ec6e2e4311dd6fb4f3458c4
 */
final class ReferenceMoveProgram449 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendAndClose(
                        ReferenceMoveTimeline.appendThenOuterParallel(
                                        ReferenceMoveTimeline.appendThenDelayedParallel(
                                                        ReferenceMoveTimeline.sequence()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.tintEnvironment(
                                                                                4,
                                                                                Short.MAX_VALUE,
                                                                                0.0f,
                                                                                1.0f,
                                                                                0.075f))
                                                                .append(owner.focusCamera(1, 0.4f))
                                                                .endGroup()
                                                                .beginParallel()
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1445,
                                                                                1,
                                                                                16,
                                                                                0.0f,
                                                                                0.9375f,
                                                                                owner.casterHandle))
                                                                .append(
                                                                        owner.adjustSoundTrack(
                                                                                1, 0, 0.0f,
                                                                                2320.0f))
                                                                .append(
                                                                        owner.scheduleSound(
                                                                                (byte) 2,
                                                                                (short) 1509,
                                                                                2,
                                                                                16,
                                                                                1500.0f,
                                                                                0.9921875f,
                                                                                owner.casterHandle))
                                                                .onStart(
                                                                        owner.preloadParticleEffect(
                                                                                624))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 624, 0, 11, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 624, 1, 11, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 624, 2, 11, 8,
                                                                                0.5f))
                                                                .append(
                                                                        owner.emitParticles(
                                                                                -1, 624, 3, 11, 8,
                                                                                0.5f)),
                                                        owner.emitParticles(
                                                                -1, 624, 4, 11, 8, 0.5f),
                                                        1.2f)
                                                .append(owner.setSpriteAnimationPaused(16, 1)),
                                        owner.scaleCombatant(
                                                16,
                                                2,
                                                7,
                                                0.016f,
                                                0.032f,
                                                0.19995117f,
                                                -0.19995117f))
                                .append(owner.restoreCamera(0.4f))
                                .append(owner.setSpriteAnimationPaused(16, 0)),
                        owner.tintEnvironment(4, Short.MAX_VALUE, 1.0f, 0.0f, 0.075f));
        return result;
    }
}
