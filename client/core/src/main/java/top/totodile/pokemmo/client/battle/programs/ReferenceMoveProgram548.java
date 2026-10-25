package top.totodile.pokemmo.client.battle.programs;

import top.totodile.pokemmo.client.battle.EncounterMoveAnimation;
import top.totodile.pokemmo.client.battle.ReferenceMoveTimeline;

/**
 * 神秘之剑（招式 ID：548）的攻击／事件阶段动画。
 *
 * <p>招式描述：用长角切斩对手进行攻击. 角上拥有的神奇力量将给予物理伤害.
 *
 * <p>动画说明：镜头聚焦目标；播放参考粒子特效；调整场景着色；执行精灵缩放；控制精灵帧动画；恢复默认镜头。
 *
 * <p>编号对应参考动画注册表；语言条目为 110548（名称）和 120548（描述）。
 *
 * <p>参考源码：cn/pokemmo/battle/animation/move/gen5/SecretSwordAnimation.java
 *
 * <p>源文件 SHA-256：d7353bc2e31de79bc05b67c0e4ad7618e429617cc397ac34cf45af44c024bdbc
 */
final class ReferenceMoveProgram548 {

    static ReferenceMoveTimeline build(EncounterMoveAnimation owner) {
        ReferenceMoveTimeline result;
        result =
                ReferenceMoveTimeline.appendThenDelayedParallel(
                                ReferenceMoveTimeline.appendThenNextParallel(
                                                ReferenceMoveTimeline.sequence()
                                                        .beginParallel()
                                                        .append(owner.focusCamera(1, 0.4f)),
                                                owner.tintEnvironment(2, 0, 0.0f, 0.625f, 0.075f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1410,
                                                        1,
                                                        16,
                                                        0.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(1, 0, 0.0f, 560.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1445,
                                                        2,
                                                        16,
                                                        0.0f,
                                                        0.625f,
                                                        owner.casterHandle))
                                        .append(owner.adjustSoundTrack(2, 0, 0.0f, 592.0f))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 2011,
                                                        2,
                                                        16,
                                                        616.6667f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .append(
                                                owner.scheduleSound(
                                                        (byte) 2,
                                                        (short) 1423,
                                                        1,
                                                        16,
                                                        1000.0f,
                                                        0.9921875f,
                                                        owner.casterHandle))
                                        .onStart(owner.preloadParticleEffect(715))
                                        .append(owner.emitParticles(-1, 715, 6, 11, 8, 1.0f))
                                        .append(owner.emitParticles(-1, 715, 5, 11, 8, 1.0f))
                                        .append(owner.emitParticles(-1, 715, 7, 11, 8, 1.0f)),
                                owner.emitParticles(-1, 715, 4, 11, 8, 1.0f),
                                0.6f)
                        .append(owner.emitParticles(-1, 715, 0, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 715, 1, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 715, 3, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 715, 8, 11, 8, 0.5f))
                        .append(owner.emitParticles(-1, 715, 2, 11, 8, 0.5f))
                        .append(owner.setSpriteAnimationPaused(16, 1))
                        .append(
                                owner.scaleCombatant(
                                        16, 2, 1, 0.016f, 0.032f, 0.30004883f, -0.30004883f))
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .beginParallel()
                        .endGroup()
                        .beginParallel()
                        .append(owner.tintEnvironment(2, 0, 0.625f, 0.0f, 0.075f))
                        .append(owner.restoreCamera(0.4f))
                        .append(owner.setSpriteAnimationPaused(16, 0))
                        .endGroup()
                        .beginParallel()
                        .endGroup();
        return result;
    }
}
